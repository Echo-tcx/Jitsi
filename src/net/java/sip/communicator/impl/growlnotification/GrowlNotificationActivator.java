/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.growlnotification;

import net.java.sip.communicator.service.resources.*;
import net.java.sip.communicator.service.systray.*;
import net.java.sip.communicator.util.*;

import org.jitsi.service.resources.*;
import org.jitsi.util.OSUtils; // disambiguation
import org.osgi.framework.*;

/**
 * Activates the GrowlNotificationService
 *
 * @author Romain Kuntz
 * @author Egidijus Jankauskas
 * @author Lyubomir Marinov
 */
public class GrowlNotificationActivator
    implements BundleActivator
{
    /**
     * The bundle context in which we started
     */
    public static BundleContext bundleContext;

    /**
     * The <tt>Logger</tt> used by the <tt>GrowlNotificationActivator</tt> class
     * and its instances for logging output.
     */
    private static final Logger logger
        = Logger.getLogger(GrowlNotificationActivator.class);

    /**
     * A reference to the resource management service.
     */
    private static ResourceManagementService resourcesService;

    /**
     * A reference to the Growl notification service
     */
    private static GrowlNotificationServiceImpl handler;

    /**
     * Initializes and starts a new <tt>GrowlNotificationService</tt>
     * implementation on Mac OS X.
     *
     * @param bundleContext the <tt>BundleContext</tt> to register the new
     * <tt>GrowlNotificationService</tt> implementation into
     * @throws Exception if initializing and/or starting the new
     * <tt>GrowlNotificationService</tt> implementation fails
     */
    public void start(BundleContext bundleContext)
        throws Exception
    {
        // This bundle is available for Mac OS X only.
        if (!OSUtils.IS_MAC)
            return;

        if (logger.isInfoEnabled())
            logger.info("Growl Notification... [Starting]");

        GrowlNotificationActivator.bundleContext  = bundleContext;

        handler = new GrowlNotificationServiceImpl();
        handler.start(bundleContext);
        bundleContext.registerService(
                PopupMessageHandler.class.getName(),
                handler,
                null);

        if (logger.isInfoEnabled())
            logger.info("Growl Notification... [Started]");
    }

    /**
     * Stops this bundle.
     *
     * @param bundleContext the <tt>BundleContext</tt> to stop this bundle into
     * @throws Exception if stopping this bundle fails
     */
    public void stop(BundleContext bundleContext)
        throws Exception
    {
        // This bundle is available for Mac OS X only.
        if (!OSUtils.IS_MAC)
            return;

        handler.stop(bundleContext);
        if (logger.isInfoEnabled())
            logger.info("Growl Notification Service... [Stopped]");
    }

    /**
     * Returns the <tt>ResourceManagementService</tt> obtained from the bundle
     * context.
     * @return the <tt>ResourceManagementService</tt> obtained from the bundle
     * context
     */
    public static ResourceManagementService getResources()
    {
        if (resourcesService == null)
        {
            resourcesService
                = ResourceManagementServiceUtils.getService(bundleContext);
        }
        return resourcesService;
    }
}
