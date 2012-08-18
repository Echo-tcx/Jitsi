/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.jabber;

import java.lang.reflect.*;
import java.util.*;

import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.GenericDetail;
import net.java.sip.communicator.util.*;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smackx.packet.*;

/**
 * Handles and retrieves all info of our contacts or our account info
 *
 * @author Damian Minkov
 */
public class InfoRetreiver
{
    private static final Logger logger =
        Logger.getLogger(InfoRetreiver.class);

    /**
     * A callback to the Jabber provider that created us.
     */
    private ProtocolProviderServiceJabberImpl jabberProvider = null;

    // here is kept all the details retrieved so far
    private final Map<String, List<GenericDetail>> retreivedDetails
        = new Hashtable<String, List<GenericDetail>>();

    private static final String TAG_FN_OPEN = "<FN>";
    private static final String TAG_FN_CLOSE = "</FN>";

    /**
     * The timeout to wait before considering vcard has time outed.
     */
    private final long vcardTimeoutReply;

    protected InfoRetreiver(
            ProtocolProviderServiceJabberImpl jabberProvider,
            String ownerUin)
    {
        this.jabberProvider = jabberProvider;

        vcardTimeoutReply
            = JabberActivator.getConfigurationService().getLong(
                    ProtocolProviderServiceJabberImpl
                        .VCARD_REPLY_TIMEOUT_PROPERTY,
                    -1);
    }

    /**
     * returns the user details from the specified class or its descendants
     * the class is one from the
     * net.java.sip.communicator.service.protocol.ServerStoredDetails
     * or implemented one in the operation set for the user info
     *
     * @param uin String
     * @param detailClass Class
     * @return Iterator
     */
    <T extends GenericDetail> Iterator<T> getDetailsAndDescendants(
        String uin,
        Class<T> detailClass)
    {
        List<GenericDetail> details = getContactDetails(uin);
        List<T> result = new LinkedList<T>();

        for (GenericDetail item : details)
            if(detailClass.isInstance(item))
            {
                @SuppressWarnings("unchecked")
                T t = (T) item;

                result.add(t);
            }

        return result.iterator();
    }

    /**
     * returns the user details from the specified class
     * exactly that class not its descendants
     *
     * @param uin String
     * @param detailClass Class
     * @return Iterator
     */
    Iterator<GenericDetail> getDetails(
        String uin,
        Class<? extends GenericDetail> detailClass)
    {
        List<GenericDetail> details = getContactDetails(uin);
        List<GenericDetail> result = new LinkedList<GenericDetail>();

        for (GenericDetail item : details)
            if(detailClass.equals(item.getClass()))
                result.add(item);

        return result.iterator();
    }

    /**
     * request the full info for the given contactAddress
     * waits and return this details
     *
     * @param contactAddress String
     * @return Vector the details
     */
    List<GenericDetail> getContactDetails(String contactAddress)
    {
        List<GenericDetail> result = getCachedContactDetails(contactAddress);

        if(result == null)
        {
            return retrieveDetails(contactAddress);
        }

        return result;
    }

    /**
     * Retrieve details and return them or if missing return an empty list.
     * @param contactAddress the address to search for.
     * @return the details or empty list.
     */
    protected List<GenericDetail> retrieveDetails(String contactAddress)
    {
        List<GenericDetail> result = new LinkedList<GenericDetail>();
        try
        {
            XMPPConnection connection = jabberProvider.getConnection();

            if(connection == null || !connection.isAuthenticated())
                return null;

            VCard card = new VCard();

            // if there is no value or is equals to the default one
            // load vcard using smack load method
            if(vcardTimeoutReply == -1
               || vcardTimeoutReply == SmackConfiguration.getPacketReplyTimeout())
                card.load(connection, contactAddress);
            else
                load(card, connection, contactAddress, vcardTimeoutReply);

            String tmp;

            tmp = checkForFullName(card);
            if(tmp != null)
                result.add(new ServerStoredDetails.DisplayNameDetail(tmp));

            tmp = card.getFirstName();
            if(tmp != null)
                result.add(new ServerStoredDetails.FirstNameDetail(tmp));

            tmp = card.getMiddleName();
            if(tmp != null)
                result.add(new ServerStoredDetails.MiddleNameDetail(tmp));

            tmp = card.getLastName();
            if(tmp != null)
                result.add(new ServerStoredDetails.LastNameDetail(tmp));

            tmp = card.getNickName();
            if(tmp != null)
                result.add(new ServerStoredDetails.NicknameDetail(tmp));

            // Home Details
            // addrField one of
            // POSTAL, PARCEL, (DOM | INTL), PREF, POBOX, EXTADR, STREET,
            // LOCALITY, REGION, PCODE, CTRY
            tmp = card.getAddressFieldHome("STREET");
            if(tmp != null)
                result.add(new ServerStoredDetails.AddressDetail(tmp));

            tmp = card.getAddressFieldHome("LOCALITY");
            if(tmp != null)
                result.add(new ServerStoredDetails.CityDetail(tmp));

            tmp = card.getAddressFieldHome("REGION");
            if(tmp != null)
                result.add(new ServerStoredDetails.ProvinceDetail(tmp));

            tmp = card.getAddressFieldHome("PCODE");
            if(tmp != null)
                result.add(new ServerStoredDetails.PostalCodeDetail(tmp));

//                tmp = card.getAddressFieldHome("CTRY");
//                if(tmp != null)
//                    result.add(new ServerStoredDetails.CountryDetail(tmp);

            // phoneType one of
            //VOICE, FAX, PAGER, MSG, CELL, VIDEO, BBS, MODEM, ISDN, PCS, PREF

            tmp = card.getPhoneHome("VOICE");
            if(tmp != null)
                result.add(new ServerStoredDetails.PhoneNumberDetail(tmp));

            tmp = card.getPhoneHome("FAX");
            if(tmp != null)
                result.add(new ServerStoredDetails.FaxDetail(tmp));

            tmp = card.getPhoneHome("PAGER");
            if(tmp != null)
                result.add(new ServerStoredDetails.PagerDetail(tmp));

            tmp = card.getPhoneHome("CELL");
            if(tmp != null)
                result.add(new ServerStoredDetails.MobilePhoneDetail(tmp));

            tmp = card.getEmailHome();
            if(tmp != null)
                result.add(new ServerStoredDetails.EmailAddressDetail(tmp));

            // Work Details
            // addrField one of
            // POSTAL, PARCEL, (DOM | INTL), PREF, POBOX, EXTADR, STREET,
            // LOCALITY, REGION, PCODE, CTRY
            tmp = card.getAddressFieldWork("STREET");
            if(tmp != null)
                result.add(new ServerStoredDetails.WorkAddressDetail(tmp));

            tmp = card.getAddressFieldWork("LOCALITY");
            if(tmp != null)
                result.add(new ServerStoredDetails.WorkCityDetail(tmp));

            tmp = card.getAddressFieldWork("REGION");
            if(tmp != null)
                result.add(new ServerStoredDetails.WorkProvinceDetail(tmp));

            tmp = card.getAddressFieldWork("PCODE");
            if(tmp != null)
                result.add(new ServerStoredDetails.WorkPostalCodeDetail(tmp));

//                tmp = card.getAddressFieldWork("CTRY");
//                if(tmp != null)
//                    result.add(new ServerStoredDetails.WorkCountryDetail(tmp);

            // phoneType one of
            //VOICE, FAX, PAGER, MSG, CELL, VIDEO, BBS, MODEM, ISDN, PCS, PREF

            tmp = card.getPhoneWork("VOICE");
            if(tmp != null)
                result.add(new ServerStoredDetails.WorkPhoneDetail(tmp));

            tmp = card.getPhoneWork("FAX");
            if(tmp != null)
                result.add(new WorkFaxDetail(tmp));

            tmp = card.getPhoneWork("PAGER");
            if(tmp != null)
                result.add(new WorkPagerDetail(tmp));

            tmp = card.getPhoneWork("CELL");
            if(tmp != null)
                result.add(new ServerStoredDetails.WorkMobilePhoneDetail(tmp));


            tmp = card.getEmailWork();
            if(tmp != null)
                result.add(new ServerStoredDetails.EmailAddressDetail(tmp));

            tmp = card.getOrganization();
            if(tmp != null)
                result.add(new ServerStoredDetails.WorkOrganizationNameDetail(tmp));

            tmp = card.getOrganizationUnit();
            if(tmp != null)
                result.add(new WorkDepartmentNameDetail(tmp));

            byte[] imageBytes = card.getAvatar();
            if(imageBytes != null && imageBytes.length > 0)
                result.add(new ServerStoredDetails.ImageDetail(
                    "Image", imageBytes));
        }
        catch (Throwable exc)
        {
            String msg = "Cannot load details for contact "
                + contactAddress + " : " + exc.getMessage();
            if(logger.isTraceEnabled())
                logger.error(msg, exc);
            else
                logger.error(msg);
        }

        retreivedDetails.put(contactAddress, result);

        return result;
    }

    /**
     * request the full info for the given contactAddress if available
     * in cache.
     *
     * @param contactAddress to search for
     * @return list of the details if any.
     */
    List<GenericDetail> getCachedContactDetails(String contactAddress)
    {
        return retreivedDetails.get(contactAddress);
    }

    /**
     * Checks for full name tag in the <tt>card</tt>.
     * @param card the card to check.
     * @return the Full name if existing, null otherwise.
     */
    private String checkForFullName(VCard card)
    {
        String vcardXml = card.toXML();

        int indexOpen = vcardXml.indexOf(TAG_FN_OPEN);

        if(indexOpen == -1)
            return null;

        int indexClose = vcardXml.indexOf(TAG_FN_CLOSE, indexOpen);

        // something is wrong!
        if(indexClose == -1)
            return null;

        return vcardXml.substring(indexOpen + TAG_FN_OPEN.length(), indexClose);
    }

    /**
     * Load VCard for the given user.
     * Using the specified timeout.
     *
     * @param vcard VCard
     * @param connection XMPP connection
     * @param user the user
     * @param timeout timeout in second
     * @throws XMPPException if something went wrong during VCard loading
     */
    public void load(VCard vcard,
                     Connection connection,
                     String user,
                     long timeout)
        throws XMPPException
    {
        vcard.setTo(user);

        vcard.setType(IQ.Type.GET);
        PacketCollector collector = connection.createPacketCollector(
                new PacketIDFilter(vcard.getPacketID()));
        connection.sendPacket(vcard);

        VCard result = null;
        try
        {
            result = (VCard) collector.nextResult(timeout);

            if (result == null)
            {
                String errorMessage = "Timeout getting VCard information";
                throw new XMPPException(errorMessage, new XMPPError(
                        XMPPError.Condition.request_timeout, errorMessage));
            }

            if (result.getError() != null)
            {
                throw new XMPPException(result.getError());
            }
        }
        catch (ClassCastException e)
        {
            logger.error("No vcard for " + user);
        }

        if (result == null)
            result = new VCard();

        // copy loaded vcard fields in the supplied one.
        Field[] fields = VCard.class.getDeclaredFields();
        for (Field field : fields)
        {
            if (field.getDeclaringClass() == VCard.class &&
                    !Modifier.isFinal(field.getModifiers()))
            {
                try
                {
                    field.setAccessible(true);
                    field.set(vcard, field.get(result));
                }
                catch (IllegalAccessException e)
                {
                    throw new RuntimeException("Cannot set field:" + field, e);
                }
            }
        }
    }

    /**
     * Work department
     */
    public static class WorkDepartmentNameDetail
        extends ServerStoredDetails.NameDetail
    {
        /**
         * Constructor.
         *
         * @param workDepartmentName name of the work department
         */
        public WorkDepartmentNameDetail(String workDepartmentName)
        {
            super("Work Department Name", workDepartmentName);
        }
    }

    /**
     * Fax at work
     */
    public static class WorkFaxDetail
        extends ServerStoredDetails.FaxDetail
    {
        /**
         * Constructor.
         *
         * @param number work fax number
         */
        public WorkFaxDetail(String number)
        {
            super(number);
            super.detailDisplayName = "WorkFax";
        }
    }

    /**
     * Pager at work
     */
    public static class WorkPagerDetail
        extends ServerStoredDetails.PhoneNumberDetail
    {
        /**
         * Constructor.
         *
         * @param number work pager number
         */
        public WorkPagerDetail(String number)
        {
            super(number);
            super.detailDisplayName = "WorkPager";
        }
    }
}
