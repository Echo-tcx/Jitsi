package net.java.sip.communicator.slick.configuration;

import org.osgi.framework.*;
import junit.framework.*;
import net.java.sip.communicator.service.configuration.*;
import java.util.*;
import net.java.sip.communicator.util.*;


/**
 * @author Emil Ivov
 */
public class ConfigurationServiceLick
    extends TestSuite
    implements BundleActivator
{
    private Logger logger = Logger.getLogger(getClass().getName());

    protected static ConfigurationService configurationService = null;
    protected static BundleContext bc = null;
    public static TestCase tcase = new TestCase(){};

    /**
     * Start the Configuration Sevice Implementation Compatibility Kit.
     *
     * @param bundleContext BundleContext
     * @throws Exception
     */
    public void start(BundleContext bundleContext) throws Exception
    {
        ConfigurationServiceLick.bc = bundleContext;
        setName("ConfigurationServiceLick");
        Hashtable properties = new Hashtable();
        properties.put("service.pid", getName());

        addTestSuite(TestConfigurationService.class);
        addTestSuite(TestConfigurationServicePersistency.class);
        bundleContext.registerService(getClass().getName(), this, properties);

        logger.debug("Successfully registered " + getClass().getName());
    }

    /**
     * stop
     *
     * @param bundlecontext BundleContext
     * @throws Exception
     */
    public void stop(BundleContext bundlecontext) throws Exception
    {
    }



}
