/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.replacement.hulu;

import java.util.*;

import net.java.sip.communicator.service.replacement.*;
import net.java.sip.communicator.util.*;

import org.osgi.framework.*;

/**
 * Activator for the Hulu source bundle.
 * 
 * @author Purvesh Sahoo
 */
public class HuluActivator
    implements BundleActivator
{
    /**
     * The <tt>Logger</tt> used by the <tt>HuluActivator</tt> class.
     */
    private static final Logger logger = Logger.getLogger(HuluActivator.class);

    /**
     * The hulu service registration.
     */
    private ServiceRegistration huluServReg = null;

    /**
     * The source implementation reference.
     */
    private static ReplacementService huluSource = null;

    /**
     * Starts the Hulu replacement source bundle
     *
     * @param context the <tt>BundleContext</tt> as provided from the OSGi
     *            framework
     * @throws Exception if anything goes wrong
     */
    public void start(BundleContext context) throws Exception
    {
        Hashtable<String, String> hashtable = new Hashtable<String, String>();
        hashtable.put(ReplacementService.SOURCE_NAME,
            ReplacementServiceHuluImpl.HULU_CONFIG_LABEL);
        huluSource = new ReplacementServiceHuluImpl();

        huluServReg =
            context.registerService(ReplacementService.class.getName(),
                huluSource, hashtable);

        logger.info("HULU source implementation [STARTED].");
    }

    /**
     * Unregisters the Hulu replacement service.
     *
     * @param context BundleContext
     * @throws Exception if anything goes wrong
     */
    public void stop(BundleContext context) throws Exception
    {
        huluServReg.unregister();
        logger.info("Hulu source implementation [STOPPED].");
    }
}
