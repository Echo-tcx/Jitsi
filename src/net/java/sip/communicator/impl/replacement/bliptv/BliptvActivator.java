/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.replacement.bliptv;

import java.util.*;

import net.java.sip.communicator.service.replacement.*;
import net.java.sip.communicator.util.*;

import org.osgi.framework.*;

/**
 * Activator for the Blip.tv source bundle.
 * 
 * @author Purvesh Sahoo
 */
public class BliptvActivator
    implements BundleActivator
{
    /**
     * The <tt>Logger</tt> used by the <tt>BliptvActivator</tt> class.
     */
    private static final Logger logger =
        Logger.getLogger(BliptvActivator.class);

    /**
     * The blip tv service registration.
     */
    private ServiceRegistration bliptvServReg = null;

    /**
     * The source implementation reference.
     */
    private static ReplacementService bliptvSource = null;

    /**
     * Starts the Blip.tv replacement source bundle
     *
     * @param context the <tt>BundleContext</tt> as provided from the OSGi
     *            framework
     * @throws Exception if anything goes wrong
     */
    public void start(BundleContext context) throws Exception
    {
        Hashtable<String, String> hashtable = new Hashtable<String, String>();
        hashtable.put(ReplacementService.SOURCE_NAME,
            ReplacementServiceBliptvImpl.BLIPTV_CONFIG_LABEL);
        bliptvSource = new ReplacementServiceBliptvImpl();

        bliptvServReg =
            context.registerService(ReplacementService.class.getName(),
                bliptvSource, hashtable);

        logger.info("Blip.TV source implementation [STARTED].");
    }

    /**
     * Unregisters the Blip.tv replacement service.
     *
     * @param context BundleContext
     * @throws Exception if anything goes wrong
     */
    public void stop(BundleContext context) throws Exception
    {
        bliptvServReg.unregister();
        logger.info("Blip.TV source implementation [STOPPED].");
    }
}