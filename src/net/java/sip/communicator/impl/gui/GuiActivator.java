/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui;

import java.util.*;

import net.java.sip.communicator.impl.gui.main.account.*;
import net.java.sip.communicator.impl.gui.main.contactlist.*;
import net.java.sip.communicator.impl.gui.utils.*;
import net.java.sip.communicator.service.audionotifier.*;
import net.java.sip.communicator.service.browserlauncher.*;
import net.java.sip.communicator.service.callhistory.*;
import net.java.sip.communicator.service.configuration.*;
import net.java.sip.communicator.service.contactlist.*;
import net.java.sip.communicator.service.contactsource.*;
import net.java.sip.communicator.service.desktop.*;
import net.java.sip.communicator.service.fileaccess.*;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.keybindings.*;
import net.java.sip.communicator.service.metahistory.*;
import net.java.sip.communicator.service.neomedia.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.replacement.*;
import net.java.sip.communicator.service.replacement.smilies.*;
import net.java.sip.communicator.service.resources.*;
import net.java.sip.communicator.service.shutdown.*;
import net.java.sip.communicator.service.systray.*;
import net.java.sip.communicator.util.*;

import org.osgi.framework.*;

/**
 * The GUI Activator class.
 *
 * @author Yana Stamcheva
 */
public class GuiActivator implements BundleActivator
{
    /**
     * The <tt>Logger</tt> used by the <tt>GuiActivator</tt> class and its
     * instances for logging output.
     */
    private static final Logger logger = Logger.getLogger(GuiActivator.class);

    private static UIServiceImpl uiService = null;

    /**
     * OSGi bundle context.
     */
    public static BundleContext bundleContext;

    private static ConfigurationService configService;

    private static MetaHistoryService metaHistoryService;

    private static MetaContactListService metaCListService;

    private static CallHistoryService callHistoryService;

    private static AudioNotifierService audioNotifierService;

    private static BrowserLauncherService browserLauncherService;

    private static SystrayService systrayService;

    private static ResourceManagementService resourcesService;

    private static KeybindingsService keybindingsService;

    private static FileAccessService fileAccessService;

    private static DesktopService desktopService;

    private static MediaService mediaService;

    private static SmiliesReplacementService smiliesService;

    private static PhoneNumberI18nService phoneNumberService;

    private static AccountManager accountManager;

    private static List<ContactSourceService> contactSources;

    private static SecurityAuthority securityAuthority;

    private static final Map<Object, ProtocolProviderFactory>
        providerFactoriesMap = new Hashtable<Object, ProtocolProviderFactory>();

    private static final Map<String, ReplacementService>
        replacementSourcesMap = new Hashtable<String, ReplacementService>();

    /**
     * Indicates if this bundle has been started.
     */
    public static boolean isStarted = false;

    /**
     * The contact list object.
     */
    private static TreeContactList contactList;

    /**
     * Called when this bundle is started.
     *
     * @param bContext The execution context of the bundle being started.
     * @throws Exception if the bundle is not correctly started
     */
    public void start(BundleContext bContext)
        throws Exception
    {
        isStarted = true;
        GuiActivator.bundleContext = bContext;

        ConfigurationManager.loadGuiConfigurations();

        try
        {
            // Create the ui service
            uiService = new UIServiceImpl();
            uiService.loadApplicationGui();

            if (logger.isInfoEnabled())
                logger.info("UI Service...[  STARTED ]");

            bundleContext.registerService(UIService.class.getName(),
                                          uiService,
                                          null);
            if (logger.isInfoEnabled())
                logger.info("UI Service ...[REGISTERED]");

            // UIServiceImpl also implements ShutdownService.
            bundleContext.registerService(ShutdownService.class.getName(),
                                          (ShutdownService) uiService,
                                          null);

            logger.logEntry();
        }
        finally
        {
            logger.logExit();
        }

        GuiActivator.getConfigurationService()
            .addPropertyChangeListener(uiService);

        bundleContext.addServiceListener(uiService);
    }

    /**
     * Called when this bundle is stopped so the Framework can perform the
     * bundle-specific activities necessary to stop the bundle.
     *
     * @param bContext The execution context of the bundle being stopped.
     * @throws Exception If this method throws an exception, the bundle is
     *   still marked as stopped, and the Framework will remove the bundle's
     *   listeners, unregister all services registered by the bundle, and
     *   release all services used by the bundle.
     */
    public void stop(BundleContext bContext) throws Exception
    {
        if (logger.isInfoEnabled())
            logger.info("UI Service ...[STOPPED]");
        isStarted = false;

        GuiActivator.getConfigurationService()
            .removePropertyChangeListener(uiService);

        bContext.removeServiceListener(uiService);
    }

    /**
     * Returns all <tt>ProtocolProviderFactory</tt>s obtained from the bundle
     * context.
     *
     * @return all <tt>ProtocolProviderFactory</tt>s obtained from the bundle
     *         context
     */
    public static Map<Object, ProtocolProviderFactory>
        getProtocolProviderFactories()
    {
        ServiceReference[] serRefs = null;
        try
        {
            // get all registered provider factories
            serRefs
                = bundleContext.getServiceReferences(
                        ProtocolProviderFactory.class.getName(),
                        null);
        }
        catch (InvalidSyntaxException e)
        {
            logger.error("LoginManager : " + e);
        }

        if (serRefs != null)
        {
            for (ServiceReference serRef : serRefs)
            {
                ProtocolProviderFactory providerFactory
                    = (ProtocolProviderFactory)
                        bundleContext.getService(serRef);

                providerFactoriesMap.put(
                        serRef.getProperty(ProtocolProviderFactory.PROTOCOL),
                        providerFactory);
            }
        }
        return providerFactoriesMap;
    }

    /**
     * Returns a <tt>ProtocolProviderFactory</tt> for a given protocol
     * provider.
     * @param protocolProvider the <tt>ProtocolProviderService</tt>, which
     * factory we're looking for
     * @return a <tt>ProtocolProviderFactory</tt> for a given protocol
     * provider
     */
    public static ProtocolProviderFactory getProtocolProviderFactory(
            ProtocolProviderService protocolProvider)
    {
        return getProtocolProviderFactory(protocolProvider.getProtocolName());
    }

    /**
     * Returns a <tt>ProtocolProviderFactory</tt> for a given protocol
     * provider.
     * @param protocolName the name of the protocol
     * @return a <tt>ProtocolProviderFactory</tt> for a given protocol
     * provider
     */
    public static ProtocolProviderFactory getProtocolProviderFactory(
            String protocolName)
    {
        String osgiFilter = "("
            + ProtocolProviderFactory.PROTOCOL
            + "="+protocolName+")";

        ProtocolProviderFactory protocolProviderFactory = null;
        try
        {
            ServiceReference[] serRefs
                = bundleContext.getServiceReferences(
                    ProtocolProviderFactory.class.getName(), osgiFilter);

            if (serRefs != null && serRefs.length > 0)
                protocolProviderFactory
                    = (ProtocolProviderFactory) bundleContext
                        .getService(serRefs[0]);
        }
        catch (InvalidSyntaxException ex)
        {
            logger.error("GuiActivator : " + ex);
        }

        return protocolProviderFactory;
    }

    /**
     * Returns the <tt>ProtocolProviderService</tt> corresponding to the given
     * account identifier that is registered in the given factory
     * @param accountID the identifier of the account
     * @return the <tt>ProtocolProviderService</tt> corresponding to the given
     * account identifier that is registered in the given factory
     */
    public static ProtocolProviderService getRegisteredProviderForAccount(
        AccountID accountID)
    {
        for (ProtocolProviderFactory factory
                : getProtocolProviderFactories().values())
        {
            if (factory.getRegisteredAccounts().contains(accountID))
            {
                ServiceReference serRef
                    = factory.getProviderForAccount(accountID);

                if (serRef != null)
                {
                    return
                        (ProtocolProviderService)
                            bundleContext.getService(serRef);
                }
            }
        }
        return null;
    }

    /**
     * Returns a list of all currently registered telephony providers for the
     * given protocol name.
     * @param protocolName the protocol name
     * @param operationSetClass the operation set class for which we're looking
     * for providers
     * @return a list of all currently registered providers for the given
     * <tt>protocolName</tt> and supporting the given <tt>operationSetClass</tt>
     */
    public static List<ProtocolProviderService> getRegisteredProviders(
        String protocolName, Class<? extends OperationSet> operationSetClass)
    {
        List<ProtocolProviderService> opSetProviders
            = new LinkedList<ProtocolProviderService>();

        ProtocolProviderFactory providerFactory
            = GuiActivator.getProtocolProviderFactory(protocolName);

        if (providerFactory != null)
        {
            ServiceReference serRef;
            ProtocolProviderService protocolProvider;

            for (AccountID accountID : providerFactory.getRegisteredAccounts())
            {
                serRef = providerFactory.getProviderForAccount(accountID);

                protocolProvider
                    = (ProtocolProviderService) GuiActivator.bundleContext
                        .getService(serRef);

                if (protocolProvider.getOperationSet(operationSetClass) != null
                    && protocolProvider.isRegistered())
                {
                    opSetProviders.add(protocolProvider);
                }
            }
        }
        return opSetProviders;
    }

    /**
     * Returns a list of all currently registered providers, which support the
     * given <tt>operationSetClass</tt>.
     *
     * @param opSetClass the operation set class for which we're looking
     * for providers
     * @return a list of all currently registered providers, which support the
     * given <tt>operationSetClass</tt>
     */
    public static List<ProtocolProviderService> getRegisteredProviders(
        Class<? extends OperationSet> opSetClass)
    {
        List<ProtocolProviderService> opSetProviders
            = new LinkedList<ProtocolProviderService>();

        for (ProtocolProviderFactory providerFactory : GuiActivator
            .getProtocolProviderFactories().values())
        {
            ServiceReference serRef;
            ProtocolProviderService protocolProvider;

            for (AccountID accountID : providerFactory.getRegisteredAccounts())
            {
                serRef = providerFactory.getProviderForAccount(accountID);

                protocolProvider
                    = (ProtocolProviderService) GuiActivator.bundleContext
                        .getService(serRef);

                if (protocolProvider.getOperationSet(opSetClass) != null
                    && protocolProvider.isRegistered())
                {
                    opSetProviders.add(protocolProvider);
                }
            }
        }
        return opSetProviders;
    }

    /**
     * Returns a list of all registered protocol providers that could be used
     * for the operation given by the operation set. Prefers the given preferred
     * protocol provider and preferred protocol name if they're available and
     * registered.
     *
     * @param opSet
     * @param preferredProvider
     * @param preferredProtocolName
     * @return a list of all registered protocol providers that could be used
     * for the operation given by the operation set
     */
    public static List<ProtocolProviderService> getOpSetRegisteredProviders(
        Class<? extends OperationSet> opSet,
        ProtocolProviderService preferredProvider,
        String                  preferredProtocolName)
    {
        List<ProtocolProviderService> providers
            = new ArrayList<ProtocolProviderService>();

        if (preferredProvider != null)
        {
            if (preferredProvider.isRegistered())
                providers.add(preferredProvider);

            // If we have a provider, but it's not registered we try to
            // obtain all registered providers for the same protocol as the
            // given preferred provider.
            else
            {
                providers
                    = GuiActivator.getRegisteredProviders(
                        preferredProvider.getProtocolName(), opSet);
            }
        }
        // If we don't have a preferred provider we try to obtain a
        // preferred protocol name and all registered providers for it.
        else
        {
            if (preferredProtocolName != null)
                providers
                    = GuiActivator.getRegisteredProviders(
                        preferredProtocolName, opSet);
            // If the protocol name is null we simply obtain all telephony
            // providers.
            else
                providers = GuiActivator.getRegisteredProviders(opSet);
        }

        return providers;
    }

    /**
     * Returns the <tt>AccountManager</tt> obtained from the bundle context.
     * @return the <tt>AccountManager</tt> obtained from the bundle context
     */
    public static AccountManager getAccountManager()
    {
        if(accountManager == null)
        {
            accountManager
                = ServiceUtils.getService(bundleContext, AccountManager.class);
        }
        return accountManager;
    }

    /**
     * Returns the <tt>ConfigurationService</tt> obtained from the bundle
     * context.
     * @return the <tt>ConfigurationService</tt> obtained from the bundle
     * context
     */
    public static ConfigurationService getConfigurationService()
    {
        if(configService == null)
        {
            configService
                = ServiceUtils.getService(
                        bundleContext,
                        ConfigurationService.class);
        }
        return configService;
    }

    /**
     * Returns the <tt>MetaHistoryService</tt> obtained from the bundle
     * context.
     * @return the <tt>MetaHistoryService</tt> obtained from the bundle
     * context
     */
    public static MetaHistoryService getMetaHistoryService()
    {
        if (metaHistoryService == null)
        {
            metaHistoryService
                = ServiceUtils.getService(
                        bundleContext,
                        MetaHistoryService.class);
        }
        return metaHistoryService;
    }

    /**
     * Returns the <tt>MetaContactListService</tt> obtained from the bundle
     * context.
     * @return the <tt>MetaContactListService</tt> obtained from the bundle
     * context
     */
    public static MetaContactListService getContactListService()
    {
        if (metaCListService == null)
        {
            metaCListService
                = ServiceUtils.getService(
                        bundleContext,
                        MetaContactListService.class);
        }
        return metaCListService;
    }

    /**
     * Returns the <tt>CallHistoryService</tt> obtained from the bundle
     * context.
     * @return the <tt>CallHistoryService</tt> obtained from the bundle
     * context
     */
    public static CallHistoryService getCallHistoryService()
    {
        if (callHistoryService == null)
        {
            callHistoryService
                = ServiceUtils.getService(
                        bundleContext,
                        CallHistoryService.class);
        }
        return callHistoryService;
    }

    /**
     * Returns the <tt>AudioNotifierService</tt> obtained from the bundle
     * context.
     * @return the <tt>AudioNotifierService</tt> obtained from the bundle
     * context
     */
    public static AudioNotifierService getAudioNotifier()
    {
        if (audioNotifierService == null)
        {
            audioNotifierService
                = ServiceUtils.getService(
                        bundleContext,
                        AudioNotifierService.class);
        }
        return audioNotifierService;
    }

    /**
     * Returns the <tt>BrowserLauncherService</tt> obtained from the bundle
     * context.
     * @return the <tt>BrowserLauncherService</tt> obtained from the bundle
     * context
     */
    public static BrowserLauncherService getBrowserLauncher()
    {
        if (browserLauncherService == null)
        {
            browserLauncherService
                = ServiceUtils.getService(
                        bundleContext,
                        BrowserLauncherService.class);
        }
        return browserLauncherService;
    }

    /**
     * Returns the current implementation of the <tt>UIService</tt>.
     * @return the current implementation of the <tt>UIService</tt>
     */
    public static UIServiceImpl getUIService()
    {
        return uiService;
    }

    /**
     * Returns the <tt>SystrayService</tt> obtained from the bundle context.
     *
     * @return the <tt>SystrayService</tt> obtained from the bundle context
     */
    public static SystrayService getSystrayService()
    {
        if (systrayService == null)
        {
            systrayService
                = ServiceUtils.getService(bundleContext, SystrayService.class);
        }
        return systrayService;
    }

    /**
     * Returns the <tt>KeybindingsService</tt> obtained from the bundle context.
     *
     * @return the <tt>KeybindingsService</tt> obtained from the bundle context
     */
    public static KeybindingsService getKeybindingsService()
    {
        if (keybindingsService == null)
        {
            keybindingsService
                = ServiceUtils.getService(
                        bundleContext,
                        KeybindingsService.class);
        }
        return keybindingsService;
    }

    /**
     * Returns the <tt>ResourceManagementService</tt>, through which we will
     * access all resources.
     *
     * @return the <tt>ResourceManagementService</tt>, through which we will
     * access all resources.
     */
    public static ResourceManagementService getResources()
    {
        if (resourcesService == null)
        {
            resourcesService
                = ServiceUtils.getService(
                        bundleContext,
                        ResourceManagementService.class);
        }
        return resourcesService;
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
            fileAccessService
                = ServiceUtils.getService(
                        bundleContext,
                        FileAccessService.class);
        }
        return fileAccessService;
    }

    /**
     * Returns the <tt>DesktopService</tt> obtained from the bundle context.
     *
     * @return the <tt>DesktopService</tt> obtained from the bundle context
     */
    public static DesktopService getDesktopService()
    {
        if (desktopService == null)
        {
            desktopService
                = ServiceUtils.getService(bundleContext, DesktopService.class);
        }
        return desktopService;
    }

    /**
     * Returns an instance of the <tt>MediaService</tt> obtained from the
     * bundle context.
     * @return an instance of the <tt>MediaService</tt> obtained from the
     * bundle context
     */
    public static MediaService getMediaService()
    {
        if (mediaService == null)
        {
            mediaService
                = ServiceUtils.getService(bundleContext, MediaService.class);
        }
        return mediaService;
    }

    /**
     * Returns a list of all registered contact sources.
     * @return a list of all registered contact sources
     */
    public static List<ContactSourceService> getContactSources()
    {
        if (contactSources != null)
            return contactSources;

        contactSources = new Vector<ContactSourceService>();

        ServiceReference[] serRefs = null;
        try
        {
            // get all registered provider factories
            serRefs =
                bundleContext.getServiceReferences(
                    ContactSourceService.class.getName(), null);
        }
        catch (InvalidSyntaxException e)
        {
            logger.error("GuiActivator : " + e);
        }

        if (serRefs != null)
        {
            for (ServiceReference serRef : serRefs)
            {
                ContactSourceService contactSource
                    = (ContactSourceService) bundleContext.getService(serRef);

                contactSources.add(contactSource);
            }
        }
        return contactSources;
    }

    /**
     * Returns all <tt>ReplacementService</tt>s obtained from the bundle
     * context.
     *
     * @return all <tt>ReplacementService</tt> implementation obtained from the
     *         bundle context
     */
    public static Map<String, ReplacementService> getReplacementSources()
    {
        ServiceReference[] serRefs = null;
        try
        {
            // get all registered sources
            serRefs
                = bundleContext.getServiceReferences(ReplacementService.class
                    .getName(), null);

        }
        catch (InvalidSyntaxException e)
        {
            logger.error("Error : " + e);
        }

        if (serRefs != null)
        {
            for (int i = 0; i < serRefs.length; i++)
            {
                ReplacementService replacementSources =
                    (ReplacementService) bundleContext.getService(serRefs[i]);

                replacementSourcesMap.put((String)serRefs[i]
                    .getProperty(ReplacementService.SOURCE_NAME),
                    replacementSources);
            }
        }
        return replacementSourcesMap;
    }

    /**
     * Returns the <tt>SmiliesReplacementService</tt> obtained from the bundle
     * context.
     *
     * @return the <tt>SmiliesReplacementService</tt> implementation obtained
     * from the bundle context
     */
    public static SmiliesReplacementService getSmiliesReplacementSource()
    {
        if (smiliesService == null)
        {
            smiliesService
                = ServiceUtils.getService(bundleContext,
                    SmiliesReplacementService.class);
        }
        return smiliesService;
    }

    /**
     * Returns the <tt>PhoneNumberI18nService</tt> obtained from the bundle
     * context.
     *
     * @return the <tt>PhoneNumberI18nService</tt> implementation obtained
     * from the bundle context
     */
    public static PhoneNumberI18nService getPhoneNumberService()
    {
        if (phoneNumberService == null)
        {
            phoneNumberService
                = ServiceUtils.getService(bundleContext,
                    PhoneNumberI18nService.class);
        }
        return phoneNumberService;
    }

    /**
     * Returns the <tt>SecurityAuthority</tt> implementation registered to
     * handle security authority events.
     *
     * @return the <tt>SecurityAuthority</tt> implementation obtained
     * from the bundle context
     */
    public static SecurityAuthority getSecurityAuthority()
    {
        if (securityAuthority == null)
        {
            securityAuthority
                = ServiceUtils.getService(bundleContext,
                    SecurityAuthority.class);
        }
        return securityAuthority;
    }

    /**
     * Returns the <tt>SecurityAuthority</tt> implementation registered to
     * handle security authority events.
     *
     * @param protocolName protocol name
     * @return the <tt>SecurityAuthority</tt> implementation obtained
     * from the bundle context
     */
    public static SecurityAuthority getSecurityAuthority(String protocolName)
    {
        String osgiFilter = "("
            + ProtocolProviderFactory.PROTOCOL
            + "=" + protocolName + ")";

        SecurityAuthority securityAuthority = null;
        try
        {
            ServiceReference[] serRefs
                = bundleContext.getServiceReferences(
                    SecurityAuthority.class.getName(), osgiFilter);

            if (serRefs != null && serRefs.length > 0)
                securityAuthority
                    = (SecurityAuthority) bundleContext
                        .getService(serRefs[0]);
        }
        catch (InvalidSyntaxException ex)
        {
            logger.error("GuiActivator : " + ex);
        }

        return securityAuthority;
    }

    /**
     * Sets the <tt>contactList</tt> component currently used to show the
     * contact list.
     * @param list the contact list object to set
     */
    public static void setContactList(TreeContactList list)
    {
        contactList = list;
    }

    /**
     * Returns the component used to show the contact list.
     * @return the component used to show the contact list
     */
    public static TreeContactList getContactList()
    {
        return contactList;
    }

    /**
     * Returns the list of wrapped protocol providers.
     *
     * @param providers the list of protocol providers
     * @return an array of wrapped protocol providers
     */
    public static Object[] getAccounts(List<ProtocolProviderService> providers)
    {
        ArrayList<Account> accounts = new ArrayList<Account>();
        Iterator<ProtocolProviderService> accountsIter = providers.iterator();

        while (accountsIter.hasNext())
        {
            accounts.add(new Account(accountsIter.next()));
        }

        return accounts.toArray();
    }

    /**
     * Returns the preferred account if there's one.
     *
     * @return the <tt>ProtocolProviderService</tt> corresponding to the
     * preferred account
     */
    public static ProtocolProviderService getPreferredAccount()
    {
        // check for preferred wizard
        String prefWName = GuiActivator.getResources().
            getSettingsString("impl.gui.PREFERRED_ACCOUNT_WIZARD");
        if(prefWName == null || prefWName.length() <= 0)
            return null;

        ServiceReference[] accountWizardRefs = null;
        try
        {
            accountWizardRefs = GuiActivator.bundleContext
                .getServiceReferences(
                    AccountRegistrationWizard.class.getName(),
                    null);
        }
        catch (InvalidSyntaxException ex)
        {
            // this shouldn't happen since we're providing no parameter string
            // but let's log just in case.
            logger.error(
                "Error while retrieving service refs", ex);
            return null;
        }

        // in case we found any, add them in this container.
        if (accountWizardRefs != null)
        {
            if (logger.isDebugEnabled())
                logger.debug("Found "
                         + accountWizardRefs.length
                         + " already installed providers.");

            for (int i = 0; i < accountWizardRefs.length; i++)
            {
                AccountRegistrationWizard wizard
                    = (AccountRegistrationWizard) GuiActivator.bundleContext
                        .getService(accountWizardRefs[i]);

                // is it the preferred protocol ?
                if(wizard.getClass().getName().equals(prefWName))
                {
                    for (ProtocolProviderFactory providerFactory : GuiActivator
                            .getProtocolProviderFactories().values())
                    {
                        ServiceReference serRef;
                        ProtocolProviderService protocolProvider;

                        for (AccountID accountID
                                : providerFactory.getRegisteredAccounts())
                        {
                            serRef = providerFactory.getProviderForAccount(
                                        accountID);

                            protocolProvider = (ProtocolProviderService)
                                GuiActivator.bundleContext
                                    .getService(serRef);

                            if (protocolProvider.getAccountID()
                                    .getProtocolDisplayName()
                                        .equals(wizard.getProtocolName()))
                            {
                                return protocolProvider;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
