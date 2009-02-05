/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.contactlist;

import net.java.sip.communicator.service.contactlist.*;
import net.java.sip.communicator.service.fileaccess.*;
import net.java.sip.communicator.util.*;

import org.osgi.framework.*;

/**
 * @author Emil Ivov
 */
public class ContactlistActivator
    implements BundleActivator
{
    private static final Logger logger =
        Logger.getLogger(ContactlistActivator.class);

    private MetaContactListServiceImpl mclServiceImpl  = null;

    private static FileAccessService fileAccessService;

    private static BundleContext bundleContext;

    /**
     * Called when this bundle is started.
     *
     * @param context The execution context of the bundle being started.
     * @throws Exception If
     */
    public void start(BundleContext context) throws Exception
    {
        bundleContext = context;

        logger.debug("Service Impl: " + getClass().getName() + " [  STARTED ]");

        mclServiceImpl = new MetaContactListServiceImpl();

        //reg the icq account man.
        context.registerService(MetaContactListService.class.getName(),
                mclServiceImpl, null);

        mclServiceImpl.start(context);

        logger.debug("Service Impl: " + getClass().getName() + " [REGISTERED]");
    }

    /**
     * Called when this bundle is stopped so the Framework can perform the
     * bundle-specific activities necessary to stop the bundle.
     *
     * @param context The execution context of the bundle being stopped.
     * @throws Exception If this method throws an exception, the bundle is
     *   still marked as stopped, and the Framework will remove the bundle's
     *   listeners, unregister all services registered by the bundle, and
     *   release all services used by the bundle.
     */
    public void stop(BundleContext context) throws Exception
    {
        logger.trace("Stopping the contact list.");
        if(mclServiceImpl != null)
            mclServiceImpl.stop(context);
    }

    /**
     * Returns the <tt>FileAccessService</tt> obtained from the bundle context.
     * 
     * @return the <tt>FileAccessService</tt> obtained from the bundle context
     */
    public static FileAccessService getFileAccessService()
    {
        if (fileAccessService == null)
        {
            ServiceReference serviceReference = bundleContext
                .getServiceReference(FileAccessService.class.getName());

            if (serviceReference != null)
                fileAccessService = (FileAccessService) bundleContext
                    .getService(serviceReference);
        }

        return fileAccessService;
    }
}
