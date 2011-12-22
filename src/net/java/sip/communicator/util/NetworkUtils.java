/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.util;

import java.beans.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import net.java.sip.communicator.service.netaddr.event.*;
import net.java.sip.communicator.util.dns.*;

import org.xbill.DNS.*;

import java.text.*;

/**
 * Utility methods and fields to use when working with network addresses.
 *
 * @author Emil Ivov
 * @author Damian Minkov
 * @author Vincent Lucas
 * @author Alan Kelly
 */
public class NetworkUtils
{
    /**
     * The <tt>Logger</tt> used by the <tt>NetworkUtils</tt> class for logging
     * output.
     */
    private static final Logger logger = Logger.getLogger(NetworkUtils.class);

    /**
     * A string containing the "any" local address for IPv6.
     */
    public static final String IN6_ADDR_ANY = "::0";

    /**
     * A string containing the "any" local address for IPv4.
     */
    public static final String IN4_ADDR_ANY = "0.0.0.0";

    /**
     * A string containing the "any" local address.
     */
    public static final String IN_ADDR_ANY = determineAnyAddress();

    /**
     * The length of IPv6 addresses.
     */
    private final static int IN6_ADDR_SIZE = 16;

    /**
     * The size of the tokens in a <tt>String</tt> representation of IPv6
     * addresses.
     */
    private final static int IN6_ADDR_TOKEN_SIZE = 2;

    /**
     * The length of IPv4 addresses.
     */
    private final static int IN4_ADDR_SIZE = 4;

    /**
     * The maximum int value that could correspond to a port number.
     */
    public static final int    MAX_PORT_NUMBER = 65535;

    /**
     * The minimum int value that could correspond to a port number bindable
     * by the SIP Communicator.
     */
    public static final int    MIN_PORT_NUMBER = 1024;

    /**
     * The random port number generator that we use in getRandomPortNumer()
     */
    private static Random portNumberGenerator = new Random();

    /**
     * The name of the property that users may use to override the
     * address of our backup DNS resolver.
     */
    public static final String PNAME_BACKUP_RESOLVER
        = "net.java.sip.communicator.util.dns.BACKUP_RESOLVER";

    /**
     * The name of the property that users may use to disable
     * our backup DNS resolver.
     */
    public static final String PNAME_BACKUP_RESOLVER_ENABLED
        = "net.java.sip.communicator.util.dns.BACKUP_RESOLVER_ENABLED";

    /**
     * The default of the property that users may use to disable
     * our backup DNS resolver.
     */
    public static final boolean PDEFAULT_BACKUP_RESOLVER_ENABLED = true;

    /**
     * The name of the property that users may use to override the port
     * of our backup DNS resolver.
     */
    public static final String PNAME_BACKUP_RESOLVER_PORT
        = "net.java.sip.communicator.util.dns.BACKUP_RESOLVER_PORT";

    /**
     * The address of the backup resolver we would use by default.
     */
    public static final String DEFAULT_BACKUP_RESOLVER
        = "backup-resolver.jitsi.net";

    /**
     * The name of the property that users may use to override the
     * IP address of our backup DNS resolver. This is only used when the
     * backup resolver name cannot be determined.
     */
    public static final String PNAME_BACKUP_RESOLVER_FALLBACK_IP
        = "net.java.sip.communicator.util.dns.BACKUP_RESOLVER_FALLBACK_IP";

    /**
     * The name of the boolean property that defines whether all domain names
     * looked up from Jitsi should be treated as absolute.
     */
    public static final String PNAME_DNS_ALWAYS_ABSOLUTE
        = "net.java.sip.communicator.util.dns.DNSSEC_ALWAYS_ABSOLUTE";

    /**
     * Default value of {@link #PNAME_DNS_ALWAYS_ABSOLUTE}.
     */
    public static final boolean PDEFAULT_DNS_ALWAYS_ABSOLUTE = false;

    /**
     * The DNSjava resolver that we use with SRV and NAPTR queries in order to
     * try and smooth the problem of DNS servers that silently drop them.
     */
    private static Resolver parallelResolver = null;

    /**
     * Monitor object to set or reset the parallel resolver.
     */
    private final static Object parallelResolverLock = new Object();

    /**
     * Initialization flag for {@link #netListener}
     */
    private static final AtomicBoolean netListenerAdded = new AtomicBoolean();

    /**
     * Listener for network change events to reset the DNS resolvers.
     */
    private static final NetworkListener netListener = new NetworkListener();

    /**
     * Determines whether the address is the result of windows auto configuration.
     * (i.e. One that is in the 169.254.0.0 network)
     * @param add the address to inspect
     * @return true if the address is autoconfigured by windows, false otherwise.
     */
    public static boolean isWindowsAutoConfiguredIPv4Address(InetAddress add)
    {
        return (add.getAddress()[0] & 0xFF) == 169
            && (add.getAddress()[1] & 0xFF) == 254;
    }

    /**
     * Determines whether the address is an IPv4 link local address. IPv4 link
     * local addresses are those in the following networks:
     *
     * 10.0.0.0    to 10.255.255.255
     * 172.16.0.0  to 172.31.255.255
     * 192.168.0.0 to 192.168.255.255
     *
     * @param add the address to inspect
     * @return true if add is a link local ipv4 address and false if not.
     */
    public static boolean isLinkLocalIPv4Address(InetAddress add)
    {
        if (add instanceof Inet4Address)
        {
            byte address[] = add.getAddress();
            if ( (address[0] & 0xFF) == 10)
                return true;
            if ( (address[0] & 0xFF) == 172
                && (address[1] & 0xFF) >= 16 && address[1] <= 31)
                return true;
            if ( (address[0] & 0xFF) == 192
                && (address[1] & 0xFF) == 168)
                return true;
            return false;
        }
        return false;
    }

    /**
     * Returns a random local port number that user applications could bind to.
     * (i.e. above 1024).
     * @return a random int located between 1024 and 65 535.
     */
    public static int getRandomPortNumber()
    {
        return getRandomPortNumber(MIN_PORT_NUMBER, MAX_PORT_NUMBER);
    }

    /**
     * Returns a random local port number, greater than min and lower than max.
     *
     * @param min the minimum allowed value for the returned port number.
     * @param max the maximum allowed value for the returned port number.
     *
     * @return a random int located between greater than min and lower than max.
     */
    public static int getRandomPortNumber(int min, int max)
    {
        return portNumberGenerator.nextInt(max - min) + min;
    }

    /**
     * Returns a random local port number, greater than min and lower than max.
     * If the pair flag is set to true, then the returned port number is
     * guaranteed to be pair. This is useful for protocols that require this
     * such as RTP
     *
     * @param min the minimum allowed value for the returned port number.
     * @param max the maximum allowed value for the returned port number.
     * @param pair specifies whether the caller would like the returned port to
     * be pair.
     *
     * @return a random int located between greater than min and lower than max.
     */
    public static int getRandomPortNumber(int min, int max, boolean pair)
    {
        if(pair)
        {
            int delta = max - min;
            delta /= 2;
            int port = getRandomPortNumber(min, min + delta);
            return port * 2;
        }
        else
        {
            return getRandomPortNumber(min, max);
        }
    }

    /**
     * Verifies whether <tt>address</tt> could be an IPv4 address string.
     *
     * @param address the String that we'd like to determine as an IPv4 address.
     *
     * @return true if the address contained by <tt>address</tt> is an IPv4
     * address and false otherwise.
     */
    public static boolean isIPv4Address(String address)
    {
        return strToIPv4(address) != null;
    }

    /**
     * Verifies whether <tt>address</tt> could be an IPv6 address string.
     *
     * @param address the String that we'd like to determine as an IPv6 address.
     *
     * @return true if the address contained by <tt>address</tt> is an IPv6
     * address and false otherwise.
     */
    public static boolean isIPv6Address(String address)
    {
        return strToIPv6(address) != null;
    }

    /**
     * Checks whether <tt>address</tt> is a valid IP address string.
     *
     * @param address the address that we'd like to check
     * @return true if address is an IPv4 or IPv6 address and false otherwise.
     */
    public static boolean isValidIPAddress(String address)
    {
        // empty string
        if (address == null || address.length() == 0)
        {
            return false;
        }

        // look for IPv6 brackets and remove brackets for parsing
        boolean ipv6Expected = false;
        if (address.charAt(0) == '[')
        {
            // This is supposed to be an IPv6 literal
            if (address.length() > 2
                            && address.charAt(address.length() - 1) == ']')
            {
                // remove brackets from IPv6
                address = address.substring(1, address.length() - 1);
                ipv6Expected = true;
            }
            else
            {
                return false;
            }
        }

        // look for IP addresses
        if (Character.digit(address.charAt(0), 16) != -1
                        || (address.charAt(0) == ':'))
        {
            byte[] addr = null;

            // see if it is IPv4 address
            addr = strToIPv4(address);
            // if not, see if it is IPv6 address
            if (addr == null)
            {
                addr = strToIPv6(address);
            }
            // if IPv4 is found when IPv6 is expected
            else if (ipv6Expected)
            {
                // invalid address: IPv4 address surrounded with brackets!
                return false;
            }
            // if an IPv4 or IPv6 address is found
            if (addr != null)
            {
                // is an IP address
                return true;
            }
        }
        // no matches found
        return false;
    }

    /**
     * Creates a byte array containing the specified <tt>ipv4AddStr</tt>.
     *
     * @param ipv4AddrStr a <tt>String</tt> containing an IPv4 address.
     *
     * @return a byte array containing the four bytes of the address represented
     * by ipv4AddrStr or <tt>null</tt> if <tt>ipv4AddrStr</tt> does not contain
     * a valid IPv4 address string.
     */
    public static byte[] strToIPv4(String ipv4AddrStr)
    {
        if (ipv4AddrStr == null || ipv4AddrStr.length() == 0)
            return null;

        byte[] address = new byte[IN4_ADDR_SIZE];
        String[] tokens = ipv4AddrStr.split("\\.", -1);
        long currentTkn;
        try
        {
            switch(tokens.length)
            {
                case 1:
                    //If the address was specified as a single String we can
                    //directly copy it into the byte array.
                   currentTkn = Long.parseLong(tokens[0]);
                   if (currentTkn < 0 || currentTkn > 0xffffffffL)
                       return null;
                   address[0] = (byte) ((currentTkn >> 24) & 0xff);
                   address[1] = (byte) (((currentTkn & 0xffffff) >> 16) & 0xff);
                   address[2] = (byte) (((currentTkn & 0xffff) >> 8) & 0xff);
                   address[3] = (byte) (currentTkn & 0xff);
                   break;
                case 2:
                    // If the address was passed in two parts (e.g. when dealing
                    // with a Class A address representation), we place the
                    // first one in the leftmost byte and the rest in the three
                    // remaining bytes of the address array.
                    currentTkn = Integer.parseInt(tokens[0]);

                    if (currentTkn < 0 || currentTkn > 0xff)
                        return null;

                    address[0] = (byte) (currentTkn & 0xff);
                    currentTkn = Integer.parseInt(tokens[1]);

                    if (currentTkn < 0 || currentTkn > 0xffffff)
                        return null;

                    address[1] = (byte) ((currentTkn >> 16) & 0xff);
                    address[2] = (byte) (((currentTkn & 0xffff) >> 8) &0xff);
                    address[3] = (byte) (currentTkn & 0xff);
                    break;
                case 3:
                    // If the address was passed in three parts (e.g. when
                    // dealing with a Class B address representation), we place
                    // the first two parts in the two leftmost bytes and the
                    // rest in the two remaining bytes of the address array.
                    for (int i = 0; i < 2; i++)
                    {
                        currentTkn = Integer.parseInt(tokens[i]);

                        if (currentTkn < 0 || currentTkn > 0xff)
                            return null;

                        address[i] = (byte) (currentTkn & 0xff);
                    }

                    currentTkn = Integer.parseInt(tokens[2]);

                    if (currentTkn < 0 || currentTkn > 0xffff)
                        return null;

                    address[2] = (byte) ((currentTkn >> 8) & 0xff);
                    address[3] = (byte) (currentTkn & 0xff);
                    break;
                case 4:
                    // And now for the most common - four part case. This time
                    // there's a byte for every part :). Yuppiee! :)
                    for (int i = 0; i < 4; i++)
                    {
                        currentTkn = Integer.parseInt(tokens[i]);

                        if (currentTkn < 0 || currentTkn > 0xff)
                            return null;

                        address[i] = (byte) (currentTkn & 0xff);
                    }
                    break;
                default:
                    return null;
            }
        }
        catch(NumberFormatException e)
        {
            return null;
        }

        return address;
    }

    /**
     * Creates a byte array containing the specified <tt>ipv6AddStr</tt>.
     *
     * @param ipv6AddrStr a <tt>String</tt> containing an IPv6 address.
     *
     * @return a byte array containing the four bytes of the address represented
     * by <tt>ipv6AddrStr</tt> or <tt>null</tt> if <tt>ipv6AddrStr</tt> does
     * not contain a valid IPv6 address string.
     */
    public static byte[] strToIPv6(String ipv6AddrStr)
    {
        // Bail out if the string is shorter than "::"
        if (ipv6AddrStr == null || ipv6AddrStr.length() < 2)
            return null;

        int colonIndex;
        char currentChar;
        boolean sawtDigit;
        int currentTkn;
        char[] addrBuff = ipv6AddrStr.toCharArray();
        byte[] dst = new byte[IN6_ADDR_SIZE];

        int srcb_length = addrBuff.length;
        int scopeID = ipv6AddrStr.indexOf ("%");

        if (scopeID == srcb_length -1)
            return null;

        if (scopeID != -1)
            srcb_length = scopeID;

        colonIndex = -1;
        int i = 0, j = 0;
        // Starting : mean we need to have at least one more.
        if (addrBuff[i] == ':')
            if (addrBuff[++i] != ':')
                return null;

        int curtok = i;
        sawtDigit = false;
        currentTkn = 0;
        while (i < srcb_length)
        {
            currentChar = addrBuff[i++];
            int chval = Character.digit(currentChar, 16);
            if (chval != -1)
            {
                currentTkn <<= 4;
                currentTkn |= chval;
                if (currentTkn > 0xffff)
                    return null;
                sawtDigit = true;
                continue;
            }

            if (currentChar == ':')
            {
                curtok = i;

                if (!sawtDigit)
                {
                    if (colonIndex != -1)
                        return null;
                    colonIndex = j;
                    continue;
                }
                else if (i == srcb_length)
                {
                    return null;
                }

                if (j + IN6_ADDR_TOKEN_SIZE > IN6_ADDR_SIZE)
                    return null;

                dst[j++] = (byte) ((currentTkn >> 8) & 0xff);
                dst[j++] = (byte) (currentTkn & 0xff);
                sawtDigit = false;
                currentTkn = 0;
                continue;
            }

            if (currentChar == '.' && ((j + IN4_ADDR_SIZE) <= IN6_ADDR_SIZE))
            {
                String ia4 = ipv6AddrStr.substring(curtok, srcb_length);
                // check this IPv4 address has 3 dots, ie. A.B.C.D
                int dot_count = 0, index=0;
                while ((index = ia4.indexOf ('.', index)) != -1)
                {
                    dot_count ++;
                    index ++;
                }

                if (dot_count != 3)
                    return null;

                byte[] v4addr = strToIPv4(ia4);
                if (v4addr == null)
                    return null;

                for (int k = 0; k < IN4_ADDR_SIZE; k++)
                {
                    dst[j++] = v4addr[k];
                }

                sawtDigit = false;
                break;  /* '\0' was seen by inet_pton4(). */
            }
            return null;
        }

        if (sawtDigit)
        {
            if (j + IN6_ADDR_TOKEN_SIZE > IN6_ADDR_SIZE)
                return null;

            dst[j++] = (byte) ((currentTkn >> 8) & 0xff);
            dst[j++] = (byte) (currentTkn & 0xff);
        }

        if (colonIndex != -1)
        {
            int n = j - colonIndex;

            if (j == IN6_ADDR_SIZE)
                return null;

            for (i = 1; i <= n; i++)
            {
                dst[IN6_ADDR_SIZE - i] = dst[colonIndex + n - i];
                dst[colonIndex + n - i] = 0;
            }

            j = IN6_ADDR_SIZE;
        }

        if (j != IN6_ADDR_SIZE)
            return null;

        byte[] newdst = mappedIPv4ToRealIPv4(dst);

        if (newdst != null)
        {
            return newdst;
        }
        else
        {
            return dst;
        }
    }

    /**
     * Returns array of hosts from the SRV record of the specified domain.
     * The records are ordered against the SRV record priority
     * @param domain the name of the domain we'd like to resolve (_proto._tcp
     * included).
     * @return an array of SRVRecord containing records returned by the DNS
     * server - address and port .
     * @throws ParseException if <tt>domain</tt> is not a valid domain name.
     * @throws DnssecException when a DNSSEC validation failure occurred.
     */
    public static SRVRecord[] getSRVRecords(String domain)
        throws ParseException, DnssecException
    {
        Record[] records = null;
        try
        {
            Lookup lookup = createLookup(domain, Type.SRV);
            records = lookup.run();
        }
        catch (TextParseException tpe)
        {
            logger.error("Failed to parse domain=" + domain, tpe);
            throw new ParseException(tpe.getMessage(), 0);
        }
        catch(DnssecRuntimeException e)
        {
            throw new DnssecException(e);
        }
        if (records == null)
        {
            return null;
        }

        //String[][] pvhn = new String[records.length][4];
        SRVRecord srvRecords[] = new SRVRecord[records.length];

        for (int i = 0; i < records.length; i++)
        {
            org.xbill.DNS.SRVRecord srvRecord =
                (org.xbill.DNS.SRVRecord) records[i];
            srvRecords[i] = new SRVRecord(srvRecord);
        }

        /* sort the SRV RRs by RR value (lower is preferred) */
        Arrays.sort(srvRecords, new Comparator<SRVRecord>()
        {
            public int compare(SRVRecord obj1, SRVRecord obj2)
            {
                return (obj1.getPriority() - obj2.getPriority());
            }
        });

        if (logger.isTraceEnabled())
        {
            logger.trace("DNS SRV query for domain " + domain + " returned:");
            for (int i = 0; i < srvRecords.length; i++)
            {
                if (logger.isTraceEnabled())
                    logger.trace(srvRecords[i]);
            }
        }
        return srvRecords;
    }

    /**
     * Returns an <tt>InetSocketAddress</tt> representing the first SRV
     * record available for the specified domain or <tt>null</tt> if there are
     * not SRV records for <tt>domain</tt>.
     *
     * @param domain the name of the domain we'd like to resolve.
     * @param service the service that we are trying to get a record for.
     * @param proto the protocol that we'd like <tt>service</tt> on.
     *
     * @return the first InetSocketAddress containing records returned by the
     * DNS server - address and port .
     * @throws ParseException if <tt>domain</tt> is not a valid domain name.
     * @throws DnssecException when a DNSSEC validation failure occurred.
     */
    public static SRVRecord getSRVRecord(String service,
                                         String proto,
                                         String domain)
        throws ParseException, DnssecException
    {
        SRVRecord[] records = getSRVRecords("_" + service
                                                 + "._" + proto
                                                 + "."  + domain);

        if(records == null || records.length == 0)
            return null;

        return records[0];
    }

    /**
     * Returns an <tt>InetSocketAddress</tt> representing the first SRV
     * record available for the specified domain or <tt>null</tt> if there are
     * not SRV records for <tt>domain</tt>.
     *
     * @param domain the name of the domain we'd like to resolve.
     * @param service the service that we are trying to get a record for.
     * @param proto the protocol that we'd like <tt>service</tt> on.
     *
     * @return the InetSocketAddress[] containing records returned by the
     * DNS server - address and port .
     * @throws ParseException if <tt>domain</tt> is not a valid domain name.
     * @throws DnssecException when a DNSSEC validation failure occurred.
     */
    public static SRVRecord[] getSRVRecords(String service,
                                                   String proto,
                                                   String domain)
        throws ParseException, DnssecException
    {
        SRVRecord[] records = getSRVRecords("_" + service
                                                 + "._" + proto
                                                 + "."  + domain);

        if(records == null || records.length == 0)
            return null;

        return records;
    }

    /**
     * Makes a NAPTR query and returns the result. The returned records are an
     * array of [Order, Service(Transport) and Replacement
     * (the srv to query for servers and ports)] this all for supplied
     * <tt>domain</tt>.
     *
     * @param domain the name of the domain we'd like to resolve.
     * @return an array with the values or null if no records found.
     *
     * @throws ParseException if <tt>domain</tt> is not a valid domain name.
     * @throws DnssecException when a DNSSEC validation failure occurred.
     */
    public static String[][] getNAPTRRecords(String domain)
        throws ParseException, DnssecException
    {
        Record[] records = null;
        try
        {
            Lookup lookup = createLookup(domain, Type.NAPTR);
            records = lookup.run();
        }
        catch (TextParseException tpe)
        {
            logger.error("Failed to parse domain="+domain, tpe);
            throw new ParseException(tpe.getMessage(), 0);
        }
        catch(DnssecRuntimeException e)
        {
            throw new DnssecException(e);
        }
        if (records == null)
        {

            if(logger.isTraceEnabled())
                logger.trace("No NAPTRs found for " + domain);
            return null;
        }

        String[][] recVals = new String[records.length][3];
        for (int i = 0; i < records.length; i++)
        {
            NAPTRRecord r = (NAPTRRecord)records[i];

            // todo - check here for broken records as missing transport
            recVals[i][0] = "" + r.getOrder();
            recVals[i][1] = getProtocolFromNAPTRRecords(r.getService());
            String replacement = r.getReplacement().toString();

            if (replacement.endsWith("."))
            {
                recVals[i][2] =
                        replacement.substring(0, replacement.length() - 1);
            }
            else
                recVals[i][2] = replacement;
        }

        /* sort the SRV RRs by RR value (lower is preferred) */
        Arrays.sort(recVals, new Comparator<String[]>()
        {
            public int compare(String array1[], String array2[])
            {
                return (Integer.parseInt(   array1[0])
                        - Integer.parseInt( array2[0]));
            }
        });

        if(logger.isTraceEnabled())
            logger.trace("NAPTRs for " + domain + "="
                + Arrays.toString(recVals));
        return recVals;
    }

    /**
     * Returns the mapping from rfc3263 between service and the protocols.
     *
     * @param service the service from NAPTR record.
     * @return the protocol TCP, UDP or TLS.
     */
    private static String getProtocolFromNAPTRRecords(String service)
    {
        if(service.equalsIgnoreCase("SIP+D2U"))
            return "UDP";
        else if(service.equalsIgnoreCase("SIP+D2T"))
            return "TCP";
        else if(service.equalsIgnoreCase("SIPS+D2T"))
            return "TLS";
        else
            return null;
    }

    /**
     * Creates an InetAddress from the specified <tt>hostAddress</tt>. The point
     * of using the method rather than creating the address by yourself is that
     * it would first check whether the specified <tt>hostAddress</tt> is indeed
     * a valid ip address. It this is the case, the method would create the
     * <tt>InetAddress</tt> using the <tt>InetAddress.getByAddress()</tt>
     * method so that no DNS resolution is attempted by the JRE. Otherwise
     * it would simply use <tt>InetAddress.getByName()</tt> so that we would an
     * <tt>InetAddress</tt> instance even at the cost of a potential DNS
     * resolution.
     *
     * @param hostAddress the <tt>String</tt> representation of the address
     * that we would like to create an <tt>InetAddress</tt> instance for.
     *
     * @return an <tt>InetAddress</tt> instance corresponding to the specified
     * <tt>hostAddress</tt>.
     *
     * @throws UnknownHostException if any of the <tt>InetAddress</tt> methods
     * we are using throw an exception.
     */
    public static InetAddress getInetAddress(String hostAddress)
        throws UnknownHostException
    {
        //is null
        if (hostAddress == null || hostAddress.length() == 0)
        {
            throw new UnknownHostException(
                            hostAddress + " is not a valid host address");
        }

        //transform IPv6 literals into normal addresses
        if (hostAddress.charAt(0) == '[')
        {
            // This is supposed to be an IPv6 literal
            if (hostAddress.length() > 2
                && hostAddress.charAt(hostAddress.length()-1) == ']')
            {
                hostAddress = hostAddress.substring(1, hostAddress.length() -1);
            }
            else
            {
                // This was supposed to be a IPv6 address, but it's not!
                throw new UnknownHostException(hostAddress);
            }
        }

        if (NetworkUtils.isValidIPAddress(hostAddress))
        {
            byte[] addr = null;

            // attempt parse as IPv4 address
            addr = strToIPv4(hostAddress);

            // if not IPv4, parse as IPv6 address
            if (addr == null)
            {
                addr = strToIPv6(hostAddress);
            }
            return InetAddress.getByAddress(hostAddress, addr);
        }
        else
        {
            return InetAddress.getByName(hostAddress);
        }
    }

    /**
     * Returns array of hosts from the A and AAAA records of the specified
     * domain. The records are ordered against the IPv4/IPv6 protocol priority
     * 
     * @param domain the name of the domain we'd like to resolve.
     * @param port the port number of the returned <tt>InetSocketAddress</tt>
     * @return an array of InetSocketAddress containing records returned by the
     *         DNS server - address and port .
     * @throws ParseException if <tt>domain</tt> is not a valid domain name.
     * @throws DnssecException when a DNSSEC validation failure occurred.
     */
    public static InetSocketAddress[] getAandAAAARecords(String domain, int port)
        throws ParseException, DnssecException
    {
        byte[] address = null;
        if((address = strToIPv4(domain)) != null
            || (address = strToIPv6(domain)) != null)
        {
            try
            {
                return new InetSocketAddress[]
                {
                    new InetSocketAddress(
                        InetAddress.getByAddress(domain, address), port)
                };
            }
            catch (UnknownHostException e)
            {
                //should not happen
                logger.error(
                    "Unable to create InetAddress for <" + domain + ">", e);
                return null;
            }
        }

        List<InetSocketAddress> addresses = new LinkedList<InetSocketAddress>();
        boolean v6lookup = Boolean.getBoolean("java.net.preferIPv6Addresses");

        for(int i = 0; i < 2; i++)
        {
            Lookup lookup;
            try
            {
                lookup = createLookup(domain, v6lookup ? Type.AAAA : Type.A);
            }
            catch (TextParseException tpe)
            {
                logger.error("Failed to parse domain <" + domain + ">", tpe);
                throw new ParseException(tpe.getMessage(), 0);
            }
            Record[] records = null;
            try
            {
                records = lookup.run();
            }
            catch(DnssecRuntimeException e)
            {
                throw new DnssecException(e);
            }
            if(records != null)
            {
                for(Record r : records)
                {
                    try
                    {
                        addresses.add(
                            new InetSocketAddress(
                                // create a new InetAddress filled with the
                                // domain name to avoid PTR queries
                                InetAddress.getByAddress(
                                    domain,
                                    v6lookup
                                      ? ((AAAARecord)r).getAddress().getAddress()
                                      : ((ARecord)r).getAddress().getAddress()
                                ),
                                port
                            )
                        );
                    }
                    catch (UnknownHostException e)
                    {
                        logger.error("Invalid record returned from DNS", e);
                    }
                }
            }
            v6lookup = !v6lookup;
        }
        if(logger.isTraceEnabled())
            logger.trace("A or AAAA addresses: " + addresses);
        return addresses.toArray(new InetSocketAddress[0]);
    }

    /**
     * Returns array of hosts from the A record of the specified domain.
     * The records are ordered against the A record priority
     * @param domain the name of the domain we'd like to resolve.
     * @param port the port number of the returned <tt>InetSocketAddress</tt>
     * @return an array of InetSocketAddress containing records returned by the
     * DNS server - address and port .
     * @throws ParseException if <tt>domain</tt> is not a valid domain name.
     * @throws DnssecException when a DNSSEC validation failure occurred.
     */
    public static InetSocketAddress getARecord(String domain, int port)
        throws ParseException, DnssecException
    {
        byte[] address;
        if((address = strToIPv4(domain)) != null)
        {
            try
            {
                return new InetSocketAddress(
                        InetAddress.getByAddress(domain, address), port);
            }
            catch (UnknownHostException e)
            {
                //should not happen
                logger.error(
                    "Unable to create InetAddress for <" + domain + ">", e);
                return null;
            }
        }

        Record[] records;
        try
        {
            //note that we intentionally do not use our parallel resolver here.
            //for starters we'd like to make sure that it works well enough
            //with SRV and NAPTR queries. We may then also adopt it for As
            //and AAAAs once it proves to be reliable (posted on: 2010-11-24)
            Lookup lookup = createLookup(domain, Type.A);
            records = lookup.run();
        }
        catch (TextParseException tpe)
        {
            logger.error("Failed to parse domain="+domain, tpe);
            throw new ParseException(tpe.getMessage(), 0);
        }
        catch(DnssecRuntimeException e)
        {
            throw new DnssecException(e);
        }
        if (records != null && records.length > 0)
        {
            if(logger.isTraceEnabled())
                logger.trace("A record for " + domain + "="
                    + ((ARecord)records[0]).getAddress());
            try
            {
                return new InetSocketAddress(
                    InetAddress.getByAddress(domain,
                        ((ARecord)records[0]).getAddress().getAddress()),
                    port);
            }
            catch (UnknownHostException e)
            {
                return null;
            }
        }
        else
        {
            if(logger.isTraceEnabled())
                logger.trace("No A record found for " + domain);
            return null;
        }
    }

    /**
     * Returns array of hosts from the AAAA record of the specified domain.
     * The records are ordered against the AAAA record priority
     * @param domain the name of the domain we'd like to resolve.
     * @param port the port number of the returned <tt>InetSocketAddress</tt>
     * @return an array of InetSocketAddress containing records returned by the
     * DNS server - address and port .
     * @throws ParseException if <tt>domain</tt> is not a valid domain name.
     * @throws DnssecException 
     */
    public static InetSocketAddress getAAAARecord(String domain, int port)
        throws ParseException, DnssecException
    {
        byte[] address;
        if((address = strToIPv6(domain)) != null)
        {
            try
            {
                return new InetSocketAddress(
                        InetAddress.getByAddress(domain, address), port);
            }
            catch (UnknownHostException e)
            {
                //should not happen
                logger.error(
                    "Unable to create InetAddress for <" + domain + ">", e);
                return null;
            }
        }

        Record[] records;
        try
        {
            //note that we intentionally do not use our parallel resolver here.
            //for starters we'd like to make sure that it works well enough
            //with SRV and NAPTR queries. We may then also adopt it for As
            //and AAAAs once it proves to be reliable (posted on: 2010-11-24)
            Lookup lookup = createLookup(domain, Type.AAAA);
            records = lookup.run();
        }
        catch (TextParseException tpe)
        {
            logger.error("Failed to parse domain="+domain, tpe);
            throw new ParseException(tpe.getMessage(), 0);
        }
        catch(DnssecRuntimeException e)
        {
            throw new DnssecException(e);
        }
        if (records != null && records.length > 0)
        {
            if(logger.isTraceEnabled())
                logger.trace("AAAA record for " + domain + "="
                    + ((AAAARecord)records[0]).getAddress());
            try
            {
                return new InetSocketAddress(
                    InetAddress.getByAddress(domain,
                        ((AAAARecord)records[0]).getAddress().getAddress()),
                    port);
            }
            catch (UnknownHostException e)
            {
                return null;
            }
        }
        else
        {
            if(logger.isTraceEnabled())
                logger.trace("No AAAA record found for " + domain);
            return null;
        }
    }

    /**
     * Tries to determine if this host supports IPv6 addresses (i.e. has at
     * least one IPv6 address) and returns IN6_ADDR_ANY or IN4_ADDR_ANY
     * accordingly. This method is only used to initialize IN_ADDR_ANY so that
     * it could be used when binding sockets. The reason we need it is because
     * on mac (contrary to lin or win) binding a socket on 0.0.0.0 would make
     * it deaf to IPv6 traffic. Binding on ::0 does the trick but that would
     * fail on hosts that have no IPv6 support. Using the result of this method
     * provides an easy way to bind sockets in cases where we simply want any
     * IP packets coming on the port we are listening on (regardless of IP
     * version).
     *
     * @return IN6_ADDR_ANY or IN4_ADDR_ANY if this host supports or not IPv6.
     */
    private static String determineAnyAddress()
    {
        Enumeration<NetworkInterface> ifaces;
        try
        {
            ifaces = NetworkInterface.getNetworkInterfaces();
        }
        catch (SocketException e)
        {
            if (logger.isDebugEnabled())
                logger.debug("Couldn't retrieve local interfaces.", e);
            return IN4_ADDR_ANY;
        }

        while(ifaces.hasMoreElements())
        {
            Enumeration<InetAddress> addrs
                                = ifaces.nextElement().getInetAddresses();
            while (addrs.hasMoreElements())
            {
                if(addrs.nextElement() instanceof Inet6Address)
                    return IN6_ADDR_ANY;
            }
        }

        return IN4_ADDR_ANY;
    }

    /**
     * Determines whether <tt>port</tt> is a valid port number bindable by an
     * application (i.e. an integer between 1024 and 65535).
     *
     * @param port the port number that we'd like verified.
     *
     * @return <tt>true</tt> if port is a valid and bindable port number and
     * <tt>alse</tt> otherwise.
     */
    public static boolean isValidPortNumber(int port)
    {
        return MIN_PORT_NUMBER < port && port < MAX_PORT_NUMBER;
    }

    /**
     * Returns an IPv4 address matching the one mapped in the IPv6
     * <tt>addr</tt>. Both input and returned value are in network order.
     *
     * @param addr a String representing an IPv4-Mapped address in textual
     * format
     *
     * @return a byte array numerically representing the IPv4 address
     */
    public static byte[] mappedIPv4ToRealIPv4(byte[] addr)
    {
        if (isMappedIPv4Addr(addr))
        {
            byte[] newAddr = new byte[IN4_ADDR_SIZE];
            System.arraycopy(addr, 12, newAddr, 0, IN6_ADDR_SIZE);
            return newAddr;
        }

        return null;
    }

    /**
     * Utility method to check if the specified <tt>address</tt> is an IPv4
     * mapped IPv6 address.
     *
     * @param address the address that we'd like to determine as an IPv4 mapped
     * one or not.
     *
     * @return <tt>true</tt> if address is an IPv4 mapped IPv6 address and
     * <tt>false</tt> otherwise.
     */
    private static boolean isMappedIPv4Addr(byte[] address)
    {
        if (address.length < IN6_ADDR_SIZE)
        {
            return false;
        }

        if ((address[0] == 0x00) && (address[1] == 0x00)
            && (address[2] == 0x00) && (address[3] == 0x00)
            && (address[4] == 0x00) && (address[5] == 0x00)
            && (address[6] == 0x00) && (address[7] == 0x00)
            && (address[8] == 0x00) && (address[9] == 0x00)
            && (address[10] == (byte)0xff)
            && (address[11] == (byte)0xff))
        {
            return true;
        }

        return false;
    }

    /**
     * Creates a new {@link Lookup} instance using our own {@link
     * ParallelResolver} if it is enabled and DNSSEC is not active.
     *
     * @param domain the domain we will be resolving
     * @param type the type of the record we will be trying to obtain.
     *
     * @return the newly created {@link Lookup} instance.
     *
     * @throws TextParseException if <tt>domain</tt> is not a valid domain name.
     */
    private static Lookup createLookup(String domain, int type)
        throws TextParseException
    {
        // listens for network changes up/down so we can reset
        // dns configuration
        if(netListenerAdded.compareAndSet(false, true))
        {
            if(logger.isDebugEnabled())
                logger.debug("NetConfigChange listener added: "
                    + netListener.hashCode());
            UtilActivator.getNetworkAddressManagerService()
                .addNetworkConfigurationChangeListener(netListener);
        }

        // make domain name absolute if requested
        if(UtilActivator.getConfigurationService().getBoolean(
            PNAME_DNS_ALWAYS_ABSOLUTE,
            PDEFAULT_DNS_ALWAYS_ABSOLUTE))
        {
            if(!Name.fromString(domain).isAbsolute())
                domain = domain + ".";
        }

        Lookup lookup = new Lookup(domain, type);

        if(logger.isTraceEnabled())
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Active DNS servers in default resolver: ");
            for(String s : ResolverConfig.getCurrentConfig().servers())
            {
                sb.append(s);
                sb.append(", ");
            }
            logger.trace(sb.toString());
        }

        if(!UtilActivator.getConfigurationService()
                .getBoolean(PNAME_BACKUP_RESOLVER_ENABLED,
                    PDEFAULT_BACKUP_RESOLVER_ENABLED)
            || UtilActivator.getConfigurationService().getBoolean(
                DnsUtilActivator.PNAME_DNSSEC_RESOLVER_ENABLED,
                DnsUtilActivator.PDEFAULT_DNSSEC_RESOLVER_ENABLED
            ))
        {
            return lookup;
        }

        //Initiate our global parallel resolver if this is our first ever
        //DNS query. The lock here is heavy but necessary as a) the config
        //form can cause an intermittent reset and b) multiple accounts signing
        //in at the same time could cause multiple ParallelResolver instances
        synchronized(parallelResolverLock)
        {
            if(parallelResolver == null)
            {
                try
                {
                    String rslvrAddrStr
                        = UtilActivator.getConfigurationService()
                            .getString(PNAME_BACKUP_RESOLVER,
                                DEFAULT_BACKUP_RESOLVER);
                    String customRslvrIP
                        = UtilActivator.getConfigurationService().getString(
                            PNAME_BACKUP_RESOLVER_FALLBACK_IP,
                            UtilActivator.getResources().getSettingsString(
                                PNAME_BACKUP_RESOLVER_FALLBACK_IP));

                    InetAddress resolverAddress = null;

                    try
                    {
                        resolverAddress = getInetAddress(rslvrAddrStr);
                    }
                    catch(UnknownHostException exc)
                    {
                        logger.warn("Oh! Seems like our primary DNS is down!"
                                    + "Don't panic! We'll try to fall back to "
                                    + customRslvrIP);
                    }

                    if(resolverAddress == null)
                    {
                        /* name resolution failed for backup DNS resolver,
                         * try with the IP address of the default backup resolver
                         */
                        resolverAddress = getInetAddress(customRslvrIP);
                    }

                    int rslvrPort = UtilActivator.getConfigurationService().getInt(
                        PNAME_BACKUP_RESOLVER_PORT, SimpleResolver.DEFAULT_PORT);

                    InetSocketAddress resolverSockAddr
                        = new InetSocketAddress(resolverAddress, rslvrPort);

                    parallelResolver = new ParallelResolver(
                                    new InetSocketAddress[]{resolverSockAddr});

                    //listens for changes on the parallel DNS settings
                    UtilActivator.getConfigurationService()
                        .addPropertyChangeListener(
                            new DnsConfigurationChangeListener());
                }
                catch(Throwable t)
                {
                    //We don't want to a problem with our parallel resolver to
                    //make our entire DNS resolution to fail so in case something
                    //goes wrong during initialization so we default to the
                    //dns java default resolver
                    logger.info("failed to initialize parallel resolver. we will "
                                    +"be using dnsjava's default one instead");

                    if(logger.isDebugEnabled())
                        logger.debug("exception was: ", t);

                    parallelResolver = Lookup.getDefaultResolver();
                }
            }

            lookup.setResolver(parallelResolver);
        }

        return lookup;
    }

    /**
     * Gets the default port used by DNS servers obtained through
     * SimpleResolver.DEFAULT_PORT.
     * @return The default DNS server port
     */
    public static short getDefaultDnsPort()
    {
        return SimpleResolver.DEFAULT_PORT;
    }

    /**
     * Listens when network is going from down to up and
     * resets dns configuration.
     */
    private static class NetworkListener
        implements NetworkConfigurationChangeListener
    {
        /**
         * Fired when a change has occurred in the
         * computer network configuration.
         *
         * @param event the change event.
         */
        public void configurationChanged(ChangeEvent event)
        {
            if((event.getType() == ChangeEvent.IFACE_UP
                || event.getType() == ChangeEvent.DNS_CHANGE)
                && !event.isInitial())
            {
                reloadDnsResolverConfig();
            }
        }
    }

    /**
     * Listens for changes in the DNS configuration and resets
     * the parallelResolver when necessary
     */
    private static class DnsConfigurationChangeListener
        implements PropertyChangeListener
    {
        @SuppressWarnings("serial")
        private final Set<String> configNames = new HashSet<String>(5){{
            add(PNAME_BACKUP_RESOLVER);
            add(PNAME_BACKUP_RESOLVER_FALLBACK_IP);
            add(PNAME_BACKUP_RESOLVER_PORT);
            add(ParallelResolver.PNAME_DNS_PATIENCE);
            add(ParallelResolver.PNAME_DNS_REDEMPTION);
        }};

        public void propertyChange(PropertyChangeEvent evt)
        {
            if(configNames.contains(evt.getPropertyName()) &&
                parallelResolver != null)
            {
                parallelResolver = null;
                logger.info("Parallel DNS resolver reset");
            }
        }
    }

    /**
     * Reloads dns server configuration in the resolver.
     */
    public static void reloadDnsResolverConfig()
    {
        // reread system dns configuration
        ResolverConfig.refresh();
        DnsUtilActivator.refreshResolver();
        if(parallelResolver instanceof ParallelResolver)
        {
            //needs a separate lock object because the parallelResolver could
            //be set to null in between
            synchronized(parallelResolverLock)
            {
                ((ParallelResolver)parallelResolver).reset();
            }
        }

        if(logger.isTraceEnabled())
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Reloaded resolver config, active DNS servers are: ");
            for(String s : ResolverConfig.getCurrentConfig().servers())
            {
                sb.append(s);
                sb.append(", ");
            }
            logger.trace(sb.toString());
        }
    }

    /**
     * Compares two DNS names against each other. Helper method to avoid the
     * export of DNSJava.
     * @param dns1 The first DNS name
     * @param dns2 The DNS name that is compared against dns1
     * @return The value 0 if dns2 is a name equivalent to dns1;
     * a value less than 0 if dns2 is less than dns1 in the canonical ordering,
     * and a value greater than 0 if dns2 is greater than dns1 in the canonical
     * ordering.
     * @throws ParseException if the dns1 or dns2 is not a DNS Name
     */
    public static int compareDnsNames(String dns1, String dns2)
        throws ParseException
    {
        try
        {
            return Name.fromString(dns1).compareTo(Name.fromString(dns2));
        }
        catch(TextParseException e)
        {
            throw new ParseException(e.getMessage(), 0);
        }
    }
}
