/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */
package net.java.sip.communicator.plugin.securityconfig;

import java.util.*;

import net.java.sip.communicator.service.credentialsstorage.*;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.resources.*;
import net.java.sip.communicator.util.*;

import org.jitsi.service.configuration.*;
import org.jitsi.service.resources.*;
import org.osgi.framework.*;

/**
 * @author Yana Stamcheva
 * @author Dmitri Melnikov
 */
public class SecurityConfigActivator
    implements BundleActivator
{
    /**
     * The logger.
     */
    private static Logger logger
        = Logger.getLogger(SecurityConfigActivator.class);

    /**
     * The {@link BundleContext} of the {@link SecurityConfigActivator}.
     */
    public static BundleContext bundleContext;

    /**
     * The {@link ResourceManagementService} of the
     * {@link SecurityConfigActivator}. Can also be obtained from the
     * {@link SecurityConfigActivator#bundleContext} on demand, but we add it
     * here for convenience.
     */
    private static ResourceManagementService resources;

    /**
     * The <tt>ConfigurationService</tt> registered in {@link #bundleContext}
     * and used by the <tt>SecurityConfigActivator</tt> instance to read and
     * write configuration properties.
     */
    private static ConfigurationService configurationService;

    /**
     * The <tt>CredentialsStorageService</tt> registered in
     * {@link #bundleContext}.
     */
    private static CredentialsStorageService credentialsStorageService;

    /**
     * The <tt>UIService</tt> registered in {@link #bundleContext}.
     */
    private static UIService uiService;

    /**
     * Indicates if the security configuration form should be disabled, i.e.
     * not visible to the user.
     */
    private static final String DISABLED_PROP
        = "net.java.sip.communicator.plugin.securityconfig.DISABLED";

    /**
     * Indicates if the master password config form should be disabled, i.e.
     * not visible to the user.
     */
    private static final String MASTER_PASSWORD_DISABLED_PROP
        = "net.java.sip.communicator.plugin.securityconfig.masterpasswordconfig.DISABLED";

    /**
     * Starts this plugin.
     * @param bc the BundleContext
     * @throws Exception if some of the operations executed in the start method
     * fails
     */
    public void start(BundleContext bc) throws Exception
    {
        bundleContext = bc;

        // If the security configuration form is disabled don't continue.
        if (getConfigurationService().getBoolean(DISABLED_PROP, false))
            return;

        // Register the configuration form.
        Dictionary<String, String> properties;

        properties = new Hashtable<String, String>();
        properties.put( ConfigurationForm.FORM_TYPE,
                        ConfigurationForm.GENERAL_TYPE);
        bundleContext.registerService(
            ConfigurationForm.class.getName(),
            new LazyConfigurationForm(
                "net.java.sip.communicator.plugin.securityconfig.SecurityConfigurationPanel",
                getClass().getClassLoader(),
                "plugin.securityconfig.ICON",
                "plugin.securityconfig.TITLE",
                20),
            properties);

        // If the master password config form is disabled don't register it.
        if(!getConfigurationService()
                .getBoolean(MASTER_PASSWORD_DISABLED_PROP, false))
        {
            properties = new Hashtable<String, String>();
            properties.put( ConfigurationForm.FORM_TYPE,
                            ConfigurationForm.SECURITY_TYPE);
            bundleContext.registerService(
                ConfigurationForm.class.getName(),
                new LazyConfigurationForm(
                    "net.java.sip.communicator.plugin.securityconfig.masterpassword.ConfigurationPanel",
                    getClass().getClassLoader(),
                    null /* iconID */,
                    "plugin.securityconfig.masterpassword.TITLE",
                    3),
                properties);
        }
    }

    /**
     * Invoked when this bundle is stopped.
     * @param bc the BundleContext
     * @throws Exception if some of the operations executed in the start method
     * fails
     */
    public void stop(BundleContext bc) throws Exception {}

    /**
     * Returns a reference to the ResourceManagementService implementation
     * currently registered in the bundle context or null if no such
     * implementation was found.
     *
     * @return a currently valid implementation of the ResourceManagementService
     */
    public static ResourceManagementService getResources()
    {
        if (resources == null)
        {
            resources
                = ResourceManagementServiceUtils.getService(bundleContext);
        }
        return resources;
    }

    /**
     * Returns a reference to the ConfigurationService implementation currently
     * registered in the bundle context or null if no such implementation was
     * found.
     *
     * @return a currently valid implementation of the ConfigurationService.
     */
    public static ConfigurationService getConfigurationService()
    {
        if (configurationService == null)
        {
            configurationService
                = ServiceUtils.getService(
                        bundleContext,
                        ConfigurationService.class);
        }
        return configurationService;
    }

    /**
     * Returns the <tt>CredentialsStorageService</tt> obtained from the bundle
     * context.
     * @return the <tt>CredentialsStorageService</tt> obtained from the bundle
     * context
     */
    public static CredentialsStorageService getCredentialsStorageService()
    {
        if (credentialsStorageService == null)
        {
            credentialsStorageService
                = ServiceUtils.getService(
                        bundleContext,
                        CredentialsStorageService.class);
        }
        return credentialsStorageService;
    }

    /**
     * Gets the <tt>UIService</tt> instance registered in the
     * <tt>BundleContext</tt> of the <tt>SecurityConfigActivator</tt>.
     *
     * @return the <tt>UIService</tt> instance registered in the
     * <tt>BundleContext</tt> of the <tt>SecurityConfigActivator</tt>
     */
    public static UIService getUIService()
    {
        if (uiService == null)
            uiService = ServiceUtils.getService(bundleContext, UIService.class);
        return uiService;
    }

    /**
     * Gets all the available accounts in SIP Communicator.
     * 
     * @return a {@link List} of {@link AccountID}.
     */
    public static List<AccountID> getAllAccountIDs()
    {
        Map<Object, ProtocolProviderFactory> providerFactoriesMap
            = getProtocolProviderFactories();

        if (providerFactoriesMap == null)
            return null;

        List<AccountID> accountIDs = new Vector<AccountID>();
        for (ProtocolProviderFactory providerFactory : providerFactoriesMap
            .values())
        {
            for (AccountID accountID : providerFactory.getRegisteredAccounts())
            {
                accountIDs.add(accountID);
            }
        }

        return accountIDs;
    }

    /**
     * Returns a <tt>Map</tt> of <ProtocolName, ProtocolProviderFactory> pairs.
     * @return a <tt>Map</tt> of <ProtocolName, ProtocolProviderFactory> pairs
     */
    private static Map<Object, ProtocolProviderFactory>
        getProtocolProviderFactories()
    {
        ServiceReference[] serRefs = null;
        try
        {
            // get all registered provider factories
            serRefs =
                bundleContext.getServiceReferences(
                    ProtocolProviderFactory.class.getName(), null);

        }
        catch (InvalidSyntaxException ex)
        {
            logger.error("Error while retrieving service refs", ex);
            return null;
        }

        Map<Object, ProtocolProviderFactory> providerFactoriesMap =
            new Hashtable<Object, ProtocolProviderFactory>();
        if (serRefs != null)
        {
            for (ServiceReference serRef : serRefs)
            {
                ProtocolProviderFactory providerFactory =
                    (ProtocolProviderFactory) bundleContext.getService(serRef);

                providerFactoriesMap.put(serRef
                    .getProperty(ProtocolProviderFactory.PROTOCOL),
                    providerFactory);
            }
        }
        return providerFactoriesMap;
    }

    /**
     * Finds all accounts with saved encrypted passwords.
     *
     * @return a {@link List} of {@link AccountID} with the saved encrypted
     * password. 
     */
    public static Map<AccountID, String> getAccountIDsWithSavedPasswords()
    {
        Map<?, ProtocolProviderFactory> providerFactoriesMap
            = getProtocolProviderFactories();

        if (providerFactoriesMap == null)
            return null;

        CredentialsStorageService credentialsStorageService
            = getCredentialsStorageService();
        Map<AccountID, String> accountIDs = new HashMap<AccountID, String>();

        for (ProtocolProviderFactory providerFactory
                : providerFactoriesMap.values())
        {
            String sourcePackageName
                = getFactoryImplPackageName(providerFactory);
            for (AccountID accountID : providerFactory.getRegisteredAccounts())
            {
                String accountPrefix
                    = ProtocolProviderFactory.findAccountPrefix(
                            bundleContext,
                            accountID,
                            sourcePackageName);
                if (credentialsStorageService.isStoredEncrypted(accountPrefix))
                    accountIDs.put(accountID, accountPrefix);
            }
        }
        return accountIDs;
    }

    /**
     * @return a String containing the package name of the concrete factory
     * class that extends the abstract factory.
     */
    private static String getFactoryImplPackageName(
            ProtocolProviderFactory providerFactory)
    {
        String className = providerFactory.getClass().getName();
        return className.substring(0, className.lastIndexOf('.'));
    }
}
