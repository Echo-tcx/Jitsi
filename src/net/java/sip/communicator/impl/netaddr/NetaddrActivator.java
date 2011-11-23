/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.netaddr;

import net.java.sip.communicator.service.packetlogging.*;
import org.osgi.framework.*;
import net.java.sip.communicator.service.configuration.*;
import net.java.sip.communicator.service.netaddr.*;
import net.java.sip.communicator.util.*;

/**
 * The activator manage the the bundles between OSGi framework and the
 * Network address manager
 *
 * @author Emil Ivov
 */
public class NetaddrActivator
    implements BundleActivator
{
    /**
     * The logger for this class.
     */
    private static Logger logger =
        Logger.getLogger(NetworkAddressManagerServiceImpl.class);

    /**
     * The OSGi bundle context.
     */
    private static BundleContext        bundleContext         = null;

    /**
     * The network address manager implementation.
     */
    private NetworkAddressManagerServiceImpl networkAMS = null;

    /**
     * The configuration service.
     */
    private static ConfigurationService configurationService = null;

    /**
     * The OSGi <tt>PacketLoggingService</tt> in
     * {@link #bundleContext} and used for debugging.
     */
    private static PacketLoggingService packetLoggingService  = null;

    /**
     * Creates a NetworkAddressManager, starts it, and registers it as a
     * NetworkAddressManagerService.
     *
     * @param bundleContext  OSGI bundle context
     * @throws Exception if starting the NetworkAddressManagerFails.
     */
    public void start(BundleContext bundleContext) throws Exception
    {
        try{

            logger.logEntry();

            //in here we load stati properties that should be else where
            //System.setProperty("java.net.preferIPv4Stack", "false");
            //System.setProperty("java.net.preferIPv6Addresses", "true");
            //end ugly property set

            //keep a reference to the bundle context for later usage.
            NetaddrActivator.bundleContext = bundleContext;

            //Create and start the network address manager.
            networkAMS =
                new NetworkAddressManagerServiceImpl();

            // give references to the NetworkAddressManager implementation
            networkAMS.start();

            if (logger.isInfoEnabled())
                logger.info("Network Address Manager         ...[  STARTED ]");

            bundleContext.registerService(
                NetworkAddressManagerService.class.getName(), networkAMS, null);

            if (logger.isInfoEnabled())
                logger.info("Network Address Manager Service ...[REGISTERED]");
        }
        finally
        {
            logger.logExit();
        }
    }

    /**
     * Returns a reference to a ConfigurationService implementation currently
     * registered in the bundle context or null if no such implementation was
     * found.
     *
     * @return a currently valid implementation of the ConfigurationService.
     */
    public static ConfigurationService getConfigurationService()
    {
        if (configurationService == null)
        {
            ServiceReference confReference
                = bundleContext.getServiceReference(
                    ConfigurationService.class.getName());
            configurationService
                = (ConfigurationService) bundleContext.getService(confReference);
        }
        return configurationService;
    }

    /**
     * Returns a reference to the <tt>PacketLoggingService</tt> implementation
     * currently registered in the bundle context or null if no such
     * implementation was found.
     *
     * @return a reference to a <tt>PacketLoggingService</tt> implementation
     * currently registered in the bundle context or null if no such
     * implementation was found.
     */
    public static PacketLoggingService getPacketLogging()
    {
        if (packetLoggingService == null)
        {
            ServiceReference plReference
                = bundleContext.getServiceReference(
                        PacketLoggingService.class.getName());

            packetLoggingService
                = (PacketLoggingService)bundleContext.getService(plReference);
        }
        return packetLoggingService;
    }

    /**
     * Stops the Network Address Manager bundle
     *
     * @param bundleContext  the OSGI bundle context
     *
     */
    public void stop(BundleContext bundleContext)
    {
        if(networkAMS != null)
            networkAMS.stop();
        if (logger.isInfoEnabled())
            logger.info("Network Address Manager Service ...[STOPPED]");

        configurationService = null;
        packetLoggingService = null;
    }

    /**
     * Returns a reference to the bundle context that we were started with.
     *
     * @return a reference to the BundleContext instance that we were started
     * with.
     */
    static BundleContext getBundleContext()
    {
        return bundleContext;
    }
}
