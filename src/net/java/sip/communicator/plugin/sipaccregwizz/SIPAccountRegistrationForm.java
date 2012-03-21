package net.java.sip.communicator.plugin.sipaccregwizz;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.swing.*;

/**
 * The <tt>SIPAccountRegistrationForm</tt>.
 *
 * @author Yana Stamcheva
 * @author Grogorii Balutsel
 */
public class SIPAccountRegistrationForm
    extends TransparentPanel
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 0L;

    private final AccountPanel accountPanel;
    private final ConnectionPanel connectionPanel;
    private final SecurityPanel securityPanel;
    private final PresencePanel presencePanel;

    private boolean isModification;

    private final SIPAccountRegistrationWizard wizard;

    private final JTabbedPane tabbedPane = new SIPCommTabbedPane();

    /**
     * The panels which value needs validation before we continue.
     */
    private List<ValidatingPanel> validatingPanels =
            new ArrayList<ValidatingPanel>();

    /**
     * Creates an instance of <tt>SIPAccountRegistrationForm</tt>.
     * @param wizard the parent wizard
     */
    public SIPAccountRegistrationForm(SIPAccountRegistrationWizard wizard)
    {
        super(new BorderLayout());
        this.wizard = wizard;

        accountPanel = new AccountPanel(this);
        connectionPanel = new ConnectionPanel(this);
        securityPanel = new SecurityPanel(this);
        presencePanel = new PresencePanel(this);
    }

    /**
     * Initializes all panels, buttons, etc.
     */
    void init()
    {
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        accountPanel.initAdvancedForm();

        SIPAccountCreationFormService createService = getCreateAccountService();
        if (createService != null)
            createService.clear();

        if (!SIPAccRegWizzActivator.isAdvancedAccountConfigDisabled())
        {
            if (accountPanel.getParent() != tabbedPane)
                tabbedPane.addTab(  Resources.getString("service.gui.ACCOUNT"),
                                    accountPanel);

            if (connectionPanel.getParent() != tabbedPane)
                tabbedPane.addTab(Resources.getString("service.gui.CONNECTION"),
                                    connectionPanel);

            if (securityPanel.getParent() != tabbedPane)
                tabbedPane.addTab(Resources.getString("service.gui.SECURITY"),
                    securityPanel);

            if (presencePanel.getParent() != tabbedPane)
                tabbedPane.addTab(Resources.getString("service.gui.PRESENCE"),
                                    presencePanel);

            if (tabbedPane.getParent() != this)
                this.add(tabbedPane, BorderLayout.NORTH);

            tabbedPane.setSelectedIndex(0);
        }
        else
            add(accountPanel, BorderLayout.NORTH);
    }

    /**
     * Parse the server part from the sip id and set it to server as default
     * value. If Advanced option is enabled Do nothing.
     * @param userName the account user name
     * @return the server address
     */
    String setServerFieldAccordingToUIN(String userName)
    {
        String serverAddress = getServerFromUserName(userName);

        connectionPanel.setServerFieldAccordingToUIN(serverAddress);

        return serverAddress;
    }

    /**
     * Enables/disables the next/finish button of the parent wizard.
     * @param isEnabled <tt>true</tt> to enable the next button, <tt>false</tt>
     * otherwise
     */
    private void setNextFinishButtonEnabled(boolean isEnabled)
    {
        SIPAccRegWizzActivator.getUIService().getAccountRegWizardContainer()
            .setNextFinishButtonEnabled(isEnabled);
    }

    /**
     * Call this to trigger revalidation of all the input values
     * and change the state of next/finish button.
     */
    void reValidateInput()
    {
        for(ValidatingPanel panel : validatingPanels)
        {
            if(!panel.isValidated())
            {
                setNextFinishButtonEnabled(false);
                return;
            }
        }

        setNextFinishButtonEnabled(true);
    }

    /**
     * Adds panel to the list of panels with values which need validation.
     * @param panel ValidatingPanel.
     */
    public void addValidatingPanel(ValidatingPanel panel)
    {
        validatingPanels.add(panel);
    }

    /**
     * Return the server part of the sip user name.
     *
     * @param userName the username.
     * @return the server part of the sip user name.
     */
    static String getServerFromUserName(String userName)
    {
        int delimIndex = userName.indexOf("@");
        if (delimIndex != -1)
        {
            return userName.substring(delimIndex + 1);
        }

        return null;
    }

    /**
     * Indicates if this wizard is modifying an existing account or is creating
     * a new one.
     *
     * @return <code>true</code> to indicate that this wizard is currently in
     * modification mode, <code>false</code> - otherwise.
     */
    public boolean isModification()
    {
        return isModification;
    }

    /**
     * Saves the user input when the "Next" wizard buttons is clicked.
     *
     * @param registration the SIPAccountRegistration
     * @return
     */
    public boolean commitPage(SIPAccountRegistration registration)
    {
        String userID = null;
        char[] password = null;
        String serverAddress = null;
        String proxyAddress = null;
        String xcapRoot = null;
        if (accountPanel.isCreateAccount())
        {
            NewAccount newAccount
                = getCreateAccountService().createAccount();
            if (newAccount != null)
            {
                userID = newAccount.getUserName();
                password = newAccount.getPassword();
                serverAddress = newAccount.getServerAddress();
                proxyAddress = newAccount.getProxyAddress();
                xcapRoot = newAccount.getXcapRoot();

                if (serverAddress == null)
                    serverAddress = setServerFieldAccordingToUIN(userID);

                if (proxyAddress == null)
                    proxyAddress = serverAddress;
            }
            else
            {
                // If we didn't succeed to create our new account, we have
                // nothing more to do here.
                return false;
            }
        }
        else
        {
            userID = accountPanel.getUserID();

            if(getServerFromUserName(userID) == null
                && registration.getDefaultDomain() != null)
            {
                // we have only a username and we want to add
                // a defautl domain
                userID = userID + "@" + registration.getDefaultDomain();
                setServerFieldAccordingToUIN(userID);
            }

            password = accountPanel.getPassword();
            serverAddress = connectionPanel.getServerAddress();
            proxyAddress = connectionPanel.getProxy();
        }

        if(userID == null || userID.trim().length() == 0)
            throw new IllegalStateException("No user ID provided.");

        registration.setUserID(userID);

        if (password != null)
            registration.setPassword(new String(password));

        registration.setRememberPassword(accountPanel.isRememberPassword());

        registration.setServerAddress(serverAddress);

        registration.setProxy(proxyAddress);

        String displayName = accountPanel.getDisplayName();
        registration.setDisplayName(displayName);

        String authName = connectionPanel.getAuthenticationName();
        if(authName != null && authName.length() > 0)
            registration.setAuthorizationName(authName);

        registration.setServerPort(connectionPanel.getServerPort());
        registration.setProxyPort(connectionPanel.getProxyPort());

        registration.setPreferredTransport(
            connectionPanel.getSelectedTransport());

        registration.setProxyAutoConfigure(
            connectionPanel.isProxyAutoConfigureEnabled());

        registration.setEnablePresence(
            presencePanel.isPresenceEnabled());
        registration.setForceP2PMode(
            presencePanel.isForcePeerToPeerMode());
        registration.setTlsClientCertificate(
            connectionPanel.getCertificateId());
        registration.setPollingPeriod(
            presencePanel.getPollPeriod());
        registration.setSubscriptionExpiration(
            presencePanel.getSubscriptionExpiration());

        // set the keepalive method only if its not already set by some custom
        // extending wizard like sip2sip
        if(registration.getKeepAliveMethod() == null)
            registration.setKeepAliveMethod(
                connectionPanel.getKeepAliveMethod());

        registration.setKeepAliveInterval(
            connectionPanel.getKeepAliveInterval());

        registration.setDTMFMethod(
            connectionPanel.getDTMFMethod());

        SIPAccRegWizzActivator.getUIService().getAccountRegWizardContainer()
            .setBackButtonEnabled(true);

        securityPanel.commitPanel(registration);

        if(xcapRoot != null)
        {
            registration.setXCapEnable(true);
            registration.setClistOptionServerUri(xcapRoot);
        }
        else
        {
            registration.setXCapEnable(presencePanel.isXCapEnable());
            registration.setXiVOEnable(presencePanel.isXiVOEnable());
            registration.setClistOptionServerUri(
                    presencePanel.getClistOptionServerUri());
        }

        registration.setClistOptionUseSipCredentials(
                presencePanel.isClistOptionUseSipCredentials());
        registration.setClistOptionUser(presencePanel.getClistOptionUser());
        registration.setClistOptionPassword(
            new String(presencePanel.getClistOptionPassword()));
        registration.setMessageWaitingIndications(
            connectionPanel.isMessageWaitingEnabled());
        registration.setVoicemailURI(connectionPanel.getVoicemailURI());

        return true;
    }

    /**
     * Loads the account with the given identifier.
     * @param accountID the account identifier
     */
    public void loadAccount(AccountID accountID)
    {
        String password = SIPAccRegWizzActivator.getSIPProtocolProviderFactory()
            .loadPassword(accountID);

        String serverAddress = accountID.getAccountPropertyString(
                    ProtocolProviderFactory.SERVER_ADDRESS);

        String displayName = accountID.getAccountPropertyString(
                    ProtocolProviderFactory.DISPLAY_NAME);

        String authName = accountID.getAccountPropertyString(
                    ProtocolProviderFactory.AUTHORIZATION_NAME);

        String serverPort = accountID.getAccountPropertyString(
                    ProtocolProviderFactory.SERVER_PORT);

        String proxyAddress = accountID.getAccountPropertyString(
                    ProtocolProviderFactory.PROXY_ADDRESS);

        String proxyPort = accountID.getAccountPropertyString(
                    ProtocolProviderFactory.PROXY_PORT);

        String preferredTransport = accountID.getAccountPropertyString(
                    ProtocolProviderFactory.PREFERRED_TRANSPORT);

        boolean enablePresence = accountID.getAccountPropertyBoolean(
                    ProtocolProviderFactory.IS_PRESENCE_ENABLED, false);

        boolean forceP2P = accountID.getAccountPropertyBoolean(
                    ProtocolProviderFactory.FORCE_P2P_MODE, false);

        String clientTlsCertificateId = accountID.getAccountPropertyString(
                    ProtocolProviderFactory.CLIENT_TLS_CERTIFICATE);

        boolean proxyAutoConfigureEnabled = accountID.getAccountPropertyBoolean(
                    ProtocolProviderFactory.PROXY_AUTO_CONFIG, false);

        String pollingPeriod = accountID.getAccountPropertyString(
                    ProtocolProviderFactory.POLLING_PERIOD);

        String subscriptionPeriod = accountID.getAccountPropertyString(
                    ProtocolProviderFactory.SUBSCRIPTION_EXPIRATION);

        String keepAliveMethod =
        accountID.getAccountPropertyString(
            ProtocolProviderFactory.KEEP_ALIVE_METHOD);

        String keepAliveInterval =
        accountID.getAccountPropertyString(
            ProtocolProviderFactory.KEEP_ALIVE_INTERVAL);

        String dtmfMethod =
        accountID.getAccountPropertyString("DTMF_METHOD");

        String voicemailURI = accountID.getAccountPropertyString(
                    ProtocolProviderFactory.VOICEMAIL_URI);

        boolean xCapEnable = accountID
                .getAccountPropertyBoolean("XCAP_ENABLE", false);
        boolean xivoEnable = accountID
                .getAccountPropertyBoolean("XIVO_ENABLE", false);

        boolean isServerOverridden = accountID.getAccountPropertyBoolean(
                ProtocolProviderFactory.IS_SERVER_OVERRIDDEN, false);

        connectionPanel.setServerOverridden(isServerOverridden);

        accountPanel.setUserIDEnabled(false);
        accountPanel.setUserID((serverAddress == null) ? accountID.getUserID()
            : accountID.getAccountPropertyString(
                ProtocolProviderFactory.USER_ID));

        if (password != null)
        {
            accountPanel.setPassword(password);
            accountPanel.setRememberPassword(true);
        }
        else
        {
            accountPanel.setRememberPassword(false);
        }

        connectionPanel.setServerAddress(serverAddress);
        connectionPanel.setServerEnabled(isServerOverridden);

        if (displayName != null && displayName.length() > 0)
            accountPanel.setDisplayName(displayName);

        if(authName != null && authName.length() > 0)
            connectionPanel.setAuthenticationName(authName);
        connectionPanel.setCertificateId(clientTlsCertificateId);

        connectionPanel.enablesProxyAutoConfigure(proxyAutoConfigureEnabled);
        connectionPanel.setServerPort(serverPort);
        connectionPanel.setProxy(proxyAddress);

        // The order of the next two fields is important, as a change listener
        // of the transportCombo sets the proxyPortField to its default
        connectionPanel.setSelectedTransport(preferredTransport);
        connectionPanel.setProxyPort(proxyPort);

        securityPanel.loadAccount(accountID);

        presencePanel.setPresenceEnabled(enablePresence);
        presencePanel.setForcePeerToPeerMode(forceP2P);
        presencePanel.setPollPeriod(pollingPeriod);
        presencePanel.setSubscriptionExpiration(subscriptionPeriod);

        if (!enablePresence)
        {
            presencePanel.setPresenceOptionsEnabled(enablePresence);
        }

        connectionPanel.setKeepAliveMethod(keepAliveMethod);
        connectionPanel.setKeepAliveInterval(keepAliveInterval);

        connectionPanel.setDTMFMethod(dtmfMethod);

        boolean mwiEnabled = accountID.getAccountPropertyBoolean(
            ProtocolProviderFactory.VOICEMAIL_ENABLED, true);
        connectionPanel.setMessageWaitingIndications(mwiEnabled);

        if(!StringUtils.isNullOrEmpty(voicemailURI))
            connectionPanel.setVoicemailURI(voicemailURI);

        if(xCapEnable)
        {
            boolean xCapUseSipCredentials = accountID
                .getAccountPropertyBoolean("XCAP_USE_SIP_CREDETIALS", true);

            presencePanel.setXCapEnable(xCapEnable);
            presencePanel.setClistOptionEnableEnabled(xCapEnable);
            presencePanel.setClistOptionUseSipCredentials(
                    xCapUseSipCredentials);
            presencePanel.setClistOptionUseSipCredentialsEnabled(
                    xCapUseSipCredentials);
            presencePanel.setClistOptionServerUri(
                    accountID.getAccountPropertyString("XCAP_SERVER_URI"));
            presencePanel.setClistOptionUser(
                    accountID.getAccountPropertyString("XCAP_USER"));
            presencePanel.setClistOptionPassword(
                    accountID.getAccountPropertyString("XCAP_PASSWORD"));
        }
        else if(xivoEnable)
        {
            boolean xCapUseSipCredentials = accountID
                .getAccountPropertyBoolean("XIVO_USE_SIP_CREDETIALS", true);

            presencePanel.setXiVOEnable(xivoEnable);
            presencePanel.setClistOptionEnableEnabled(xivoEnable);
            presencePanel.setClistOptionUseSipCredentials(
                    xCapUseSipCredentials);
            presencePanel.setClistOptionUseSipCredentialsEnabled(
                    xCapUseSipCredentials);
            presencePanel.setClistOptionServerUri(
                    accountID.getAccountPropertyString("XIVO_SERVER_URI"));
            presencePanel.setClistOptionUser(
                    accountID.getAccountPropertyString("XIVO_USER"));
            presencePanel.setClistOptionPassword(
                    accountID.getAccountPropertyString("XIVO_PASSWORD"));
        }
    }

    /**
     * Returns a simple version of this registration form.
     * @return the simple form component
     */
    public Component getSimpleForm()
    {
        SIPAccountCreationFormService createAccountService
            = getCreateAccountService();

        if (createAccountService != null)
            createAccountService.clear();

        // Indicate that this panel is opened in a simple form.
        accountPanel.setSimpleForm(true);

        return accountPanel;
    }

    /**
     * Sets the isModification property.
     * @param isModification indicates if this form is created for modification
     */
    public void setModification(boolean isModification)
    {
        this.isModification = isModification;
    }

    /**
     * Returns the username example.
     * @return the username example string
     */
    public String getUsernameExample()
    {
        return wizard.getUserNameExample();
    }

    /**
     * Sign ups through the web.
     */
    public void webSignup()
    {
        wizard.webSignup();
    }

    /**
     * Returns the sign up link name.
     * @return the sign up link name
     */
    public String getWebSignupLinkName()
    {
        return wizard.getWebSignupLinkName();
    }

    /**
     * Returns the forgot password link name.
     *
     * @return the forgot password link name
     */
    public String getForgotPasswordLinkName()
    {
        return wizard.getForgotPasswordLinkName();
    }

    /**
     * Returns the forgot password link.
     *
     * @return the forgot password link
     */
    public String getForgotPasswordLink()
    {
        return wizard.getForgotPasswordLink();
    }

    /**
     * Returns an instance of <tt>CreateAccountService</tt> through which the
     * user could create an account. This method is meant to be implemented by
     * specific protocol provider wizards.
     * @return an instance of <tt>CreateAccountService</tt>
     */
    public SIPAccountCreationFormService getCreateAccountService()
    {
         return wizard.getCreateAccountService();
    }

    /**
     * Returns the display label used for the sip id field.
     * @return the sip id display label string.
     */
    protected String getUsernameLabel()
    {
        return wizard.getUsernameLabel();
    }

    /**
     * Returns the current sip registration holding all values.
     * @return sip registration.
     */
    public SIPAccountRegistration getRegistration()
    {
        return wizard.getRegistration();
    }

    /**
     * Return the string for add existing account button.
     * @return the string for add existing account button.
     */
    protected String getExistingAccountLabel()
    {
        return wizard.getExistingAccountLabel();
    }

    /**
     * Return the string for create new account button.
     * @return the string for create new account button.
     */
    protected String getCreateAccountLabel()
    {
        return wizard.getCreateAccountLabel();
    }

    /**
     * Selects the create account button.
     */
    void setCreateButtonSelected()
    {
        accountPanel.setCreateButtonSelected();
    }
}
