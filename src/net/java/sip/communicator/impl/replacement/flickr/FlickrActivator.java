/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.replacement.flickr;

import java.util.*;

import net.java.sip.communicator.service.replacement.*;
import net.java.sip.communicator.util.*;

import org.osgi.framework.*;

/**
 * Activator for the Flickr source bundle.
 * 
 * @author Purvesh Sahoo
 */
public class FlickrActivator
    implements BundleActivator
{
    /**
     * The <tt>Logger</tt> used by the <tt>FlickrActivator</tt> class.
     */
    private static final Logger logger =
        Logger.getLogger(FlickrActivator.class);

    /**
     * Flickr service registration.
     */
    private ServiceRegistration flickrServReg = null;

    /**
     * The source implementation reference.
     */
    private static ReplacementService flickrSource = null;

    /**
     * Starts the Flickr replacement source bundle
     *
     * @param context the <tt>BundleContext</tt> as provided from the OSGi
     *            framework
     * @throws Exception if anything goes wrong
     */
    public void start(BundleContext context) throws Exception
    {
        Hashtable<String, String> hashtable = new Hashtable<String, String>();
        hashtable.put(ReplacementService.SOURCE_NAME,
            ReplacementServiceFlickrImpl.FLICKR_CONFIG_LABEL);
        flickrSource = new ReplacementServiceFlickrImpl();

        flickrServReg =
            context.registerService(ReplacementService.class.getName(),
                flickrSource, hashtable);

        logger.info("Flickr source implementation [STARTED].");
    }

    /**
     * Unregisters the Flickr replacement service.
     *
     * @param context BundleContext
     * @throws Exception if anything goes wrong
     */
    public void stop(BundleContext context) throws Exception
    {
        flickrServReg.unregister();
        logger.info("Flickr source implementation [STOPPED].");
    }
}
