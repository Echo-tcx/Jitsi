/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.util;

import org.osgi.framework.*;
import java.lang.Thread.*;

/**
 * The only raison d'etre for this Activator is so that it would set a global
 * exception handler. It doesn't export any services and neither it runs any
 * iniitialization - all it does is call
 * <tt>Thread.setUncaughtExceptionHandler()</tt>
 *
 * @author Emil Ivov
 */
public class UtilActivator
    implements BundleActivator, 
               Thread.UncaughtExceptionHandler
{
    private static final Logger logger
        = Logger.getLogger(UtilActivator.class);
    /**
     * Calls <tt>Thread.setUncaughtExceptionHandler()</tt>
     *
     * @param context The execution context of the bundle being started
     * (unused).
     * @throws Exception If this method throws an exception, this bundle is
     *   marked as stopped and the Framework will remove this bundle's
     *   listeners, unregister all services registered by this bundle, and
     *   release all services used by this bundle.
     */
    public void start(BundleContext context)
        throws Exception
    {
        logger.trace("Setting default uncaught exception handler.");

        Thread.setDefaultUncaughtExceptionHandler(this);
        Thread.currentThread().setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * Method invoked when a thread would terminate due to the given uncaught
     * exception. All we do here is simply log the exception using the system
     * logger.
     *
     * <p>Any exception thrown by this method will be ignored by the
     * Java Virtual Machine and thus won't screw our application.
     *
     * @param thread the thread
     * @param exc the exception
     */
    public void uncaughtException(Thread thread, Throwable exc)
    {
        logger.error("An uncaught exception occurred in thread="
                     + thread
                     + " and message was: "
                     + exc.getMessage()
                     , exc);
    }

    /**
     * Doesn't do anything.
     *
     * @param context The execution context of the bundle being stopped.
     * @throws Exception If this method throws an exception, the bundle is
     *   still marked as stopped, and the Framework will remove the bundle's
     *   listeners, unregister all services registered by the bundle, and
     *   release all services used by the bundle.
     */
    public void stop(BundleContext context)
        throws Exception
    {

    }
}
