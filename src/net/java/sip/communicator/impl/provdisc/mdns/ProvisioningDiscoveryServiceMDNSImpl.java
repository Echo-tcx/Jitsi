/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.provdisc.mdns;

import net.java.sip.communicator.service.provdisc.*;
import net.java.sip.communicator.service.provdisc.event.*;
import net.java.sip.communicator.util.*;

/**
 * Class that uses mDNS to retrieve provisioning URL.
 *
 * @author Sebastien Vincent
 */
public class ProvisioningDiscoveryServiceMDNSImpl
    extends AbstractProvisioningDiscoveryService
    implements DiscoveryListener
{
    /**
     * Logger.
     */
    private final Logger logger
        = Logger.getLogger(ProvisioningDiscoveryServiceMDNSImpl.class);

    /**
     * Name of the method used to retrieve provisioning URL.
     */
    private static final String METHOD_NAME = "Bonjour";

    /**
     * MDNS provisioning discover object.
     */
    private MDNSProvisioningDiscover discover = null;

    /**
     * Constructor.
     */
    public ProvisioningDiscoveryServiceMDNSImpl()
    {
        try
        {
            discover = new MDNSProvisioningDiscover();
            discover.addDiscoveryListener(this);
        }
        catch(Exception e)
        {
            logger.warn("Cannot create JmDNS instance", e);
        }
    }

    /**
     * Get the name of the method name used to retrieve provisioning URL.
     *
     * @return method name
     */
    public String getMethodName()
    {
        return METHOD_NAME;
    }

    /**
     * Launch a discovery for a provisioning URL. This method is synchronous and
     * may block for some time. Note that you don't have to call
     * <tt>startDiscovery</tt> method prior to this one to retrieve URL.
     *
     * @return provisioning URL
     */
    public String discoverURL()
    {
        if(discover != null)
        {
            return discover.discoverProvisioningURL();
        }

        return null;
    }

    /**
     * Launch a mDNS discovery for a provisioning URL.
     *
     * This method is asynchronous, the response will be notified to any
     * <tt>ProvisioningListener</tt> registered.
     */
    public void startDiscovery()
    {
        if(discover != null)
        {
            new Thread(discover).start();
        }
    }

    /**
     * Notify the provisioning URL.
     *
     * @param event provisioning event
     */
    public void notifyProvisioningURL(DiscoveryEvent event)
    {
        fireDiscoveryEvent(event);
    }
}
