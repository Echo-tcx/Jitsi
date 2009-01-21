/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license. See terms of license at gnu.org.
 */
package net.java.sip.communicator.plugin.sipaccregwizz;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.swing.*;

/**
 * The <tt>FirstWizardPage</tt> is the page, where user could enter the uin
 * and the password of the account.
 *
 * @author Yana Stamcheva
 * @author Damian Minkov
 */
@SuppressWarnings("serial")
public class FirstWizardPage
    extends TransparentPanel
    implements WizardPage, DocumentListener, ItemListener
{
    public static final String FIRST_PAGE_IDENTIFIER = "FirstPageIdentifier";

    public static final String USER_NAME_EXAMPLE
        = "Ex: john@voiphone.net or simply \"john\" for no server";

    private JPanel firstTabPanel = new TransparentPanel(new BorderLayout());

    private JPanel uinPassPanel = new TransparentPanel(new BorderLayout(10, 10));

    private JPanel labelsPanel = new TransparentPanel();

    private JPanel valuesPanel = new TransparentPanel();
    
    private JLabel uinLabel
        = new JLabel(Resources.getString("plugin.sipaccregwizz.USERNAME"));

    private JLabel passLabel
        = new JLabel(Resources.getString("service.gui.PASSWORD"));

    private JPanel emptyPanel = new TransparentPanel();

    private JLabel uinExampleLabel = new JLabel(USER_NAME_EXAMPLE);

    private JTextField uinField = new JTextField();

    private JPasswordField passField = new JPasswordField();

    private JCheckBox rememberPassBox =
        new SIPCommCheckBox(
            Resources.getString("service.gui.REMEMBER_PASSWORD"));

    private JPanel advancedOpPanel
        = new TransparentPanel(new BorderLayout(10, 10));

    private JPanel labelsAdvOpPanel
        = new TransparentPanel(new GridLayout(0, 1, 10, 10));

    private JPanel valuesAdvOpPanel
        = new TransparentPanel(new GridLayout(0, 1, 10, 10));

    private JLabel serverLabel
        = new JLabel(Resources.getString("plugin.sipaccregwizz.REGISTRAR"));

    private JCheckBox enableDefaultEncryption =
        new SIPCommCheckBox(Resources
            .getString("plugin.sipaccregwizz.ENABLE_DEFAULT_ENCRYPTION"), true); 

    private JLabel proxyLabel
        = new JLabel(Resources.getString("plugin.sipaccregwizz.PROXY"));

    private JLabel serverPortLabel =
        new JLabel(Resources.getString("plugin.sipaccregwizz.SERVER_PORT"));

    private JLabel proxyPortLabel =
        new JLabel(Resources.getString("plugin.sipaccregwizz.PROXY_PORT"));

    private JLabel transportLabel =
        new JLabel(Resources.getString(
            "plugin.sipaccregwizz.PREFERRED_TRANSPORT"));

    private JTextField serverField = new JTextField();

    private JTextField proxyField = new JTextField();

    private JTextField serverPortField
        = new JTextField(SIPAccountRegistration.DEFAULT_PORT);

    private JTextField proxyPortField
        = new JTextField(SIPAccountRegistration.DEFAULT_PORT);

    private JComboBox transportCombo = new JComboBox(new Object[]
    { "UDP", "TCP", "TLS" });

    private JPanel presenceOpPanel = new TransparentPanel(new BorderLayout(10, 10));

    private JPanel buttonsPresOpPanel =
        new TransparentPanel(new GridLayout(0, 1, 10, 10));

    private JPanel labelsPresOpPanel
        = new TransparentPanel(new GridLayout(0, 1, 10, 10));

    private JPanel valuesPresOpPanel
        = new TransparentPanel(new GridLayout(0, 1, 10, 10));

    private JCheckBox enablePresOpButton =
        new SIPCommCheckBox(Resources
            .getString("plugin.sipaccregwizz.ENABLE_PRESENCE"), true);

    private JCheckBox forceP2PPresOpButton =
        new SIPCommCheckBox(Resources
            .getString("plugin.sipaccregwizz.FORCE_P2P_PRESENCE"), true);

    private JLabel pollPeriodLabel = new JLabel(
        Resources.getString(
            "plugin.sipaccregwizz.OFFLINE_CONTACT_POLLING_PERIOD"));

    private JLabel subscribeExpiresLabel = new JLabel(
        Resources.getString("plugin.sipaccregwizz.SUBSCRIPTION_EXPIRATION"));

    private JTextField pollPeriodField
        = new JTextField(SIPAccountRegistration.DEFAULT_POLL_PERIOD);

    private JTextField subscribeExpiresField =
        new JTextField(SIPAccountRegistration.DEFAULT_SUBSCRIBE_EXPIRES);

    private JPanel keepAlivePanel
        = new TransparentPanel(new BorderLayout(10, 10));

    private JPanel keepAliveLabels
        = new TransparentPanel(new GridLayout(0, 1, 5, 5));

    private JPanel keepAliveValues
        = new TransparentPanel(new GridLayout(0, 1, 5, 5));

    private JLabel keepAliveMethodLabel = new JLabel(
        Resources.getString("plugin.sipaccregwizz.KEEP_ALIVE_METHOD"));

    private JLabel keepAliveIntervalLabel = new JLabel(
        Resources.getString("plugin.sipaccregwizz.KEEP_ALIVE_INTERVAL"));

    private JLabel keepAliveIntervalExampleLabel = new JLabel(
        Resources.getString("plugin.sipaccregwizz.KEEP_ALIVE_INTERVAL_INFO"));

    private JComboBox keepAliveMethodBox
        = new JComboBox(new Object []
                                    {
                                        "REGISTER",
                                        "OPTIONS"
                                    });

    private JTextField keepAliveIntervalValue = new JTextField();

    private JTabbedPane tabbedPane = new SIPCommTabbedPane(false, false);

    private JPanel advancedPanel = new TransparentPanel();

    private Object nextPageIdentifier = WizardPage.SUMMARY_PAGE_IDENTIFIER;

    private SIPAccountRegistrationWizard wizard;

    private boolean isCommitted = false;

    private boolean isServerOverridden = false;

    /**
     * Creates an instance of <tt>FirstWizardPage</tt>.
     *
     * @param wizard the parent wizard
     */
    public FirstWizardPage(SIPAccountRegistrationWizard wizard)
    {
        super(new BorderLayout());

        this.wizard = wizard;

        advancedPanel.setLayout(new BoxLayout(advancedPanel, BoxLayout.Y_AXIS));

        this.init();

        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        this.labelsPanel
            .setLayout(new BoxLayout(labelsPanel, BoxLayout.Y_AXIS));

        this.valuesPanel
            .setLayout(new BoxLayout(valuesPanel, BoxLayout.Y_AXIS));
    }

    /**
     * Initializes all panels, buttons, etc.
     */
    private void init()
    {
        this.labelsPanel.setOpaque(false);
        this.valuesPanel.setOpaque(false);
        this.uinPassPanel.setOpaque(false);
        this.emptyPanel.setOpaque(false);

        this.uinField.getDocument().addDocumentListener(this);
        this.transportCombo.addItemListener(this);
        this.rememberPassBox.setSelected(true);

        this.uinExampleLabel.setForeground(Color.GRAY);
        this.uinExampleLabel.setFont(uinExampleLabel.getFont().deriveFont(8));
        this.emptyPanel.setMaximumSize(new Dimension(40, 35));
        this.uinExampleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8,
            0));

        labelsPanel.add(uinLabel);
        labelsPanel.add(emptyPanel);
        labelsPanel.add(passLabel);

        valuesPanel.add(uinField);
        valuesPanel.add(uinExampleLabel);
        valuesPanel.add(passField);

        uinPassPanel.add(labelsPanel, BorderLayout.WEST);
        uinPassPanel.add(valuesPanel, BorderLayout.CENTER);
        uinPassPanel.add(rememberPassBox, BorderLayout.SOUTH);

        uinPassPanel.setBorder(BorderFactory.createTitledBorder(Resources
            .getString("plugin.sipaccregwizz.USERNAME_AND_PASSWORD")));

        firstTabPanel.add(uinPassPanel, BorderLayout.NORTH);

        tabbedPane.addTab(  Resources.getString("service.gui.SUMMARY"),
                            firstTabPanel);

        transportCombo
            .setSelectedItem(SIPAccountRegistration.DEFAULT_TRANSPORT);

        labelsAdvOpPanel.add(serverLabel);
        labelsAdvOpPanel.add(serverPortLabel);
        labelsAdvOpPanel.add(proxyLabel);
        labelsAdvOpPanel.add(proxyPortLabel);
        labelsAdvOpPanel.add(transportLabel);

        valuesAdvOpPanel.add(serverField);
        valuesAdvOpPanel.add(serverPortField);
        valuesAdvOpPanel.add(proxyField);
        valuesAdvOpPanel.add(proxyPortField);
        valuesAdvOpPanel.add(transportCombo);

        advancedOpPanel.add(labelsAdvOpPanel, BorderLayout.WEST);
        advancedOpPanel.add(valuesAdvOpPanel, BorderLayout.CENTER);
        advancedOpPanel.add(enableDefaultEncryption, BorderLayout.SOUTH);

        advancedOpPanel.setBorder(BorderFactory.createTitledBorder(Resources
            .getString("plugin.aimaccregwizz.ADVANCED_OPTIONS")));

        advancedPanel.add(advancedOpPanel);

        enablePresOpButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                // Perform action
                JCheckBox cb = (JCheckBox) evt.getSource();

                setPresenceOptionsEnabled(cb.isSelected());
            }
        });

        labelsPresOpPanel.add(pollPeriodLabel);
        labelsPresOpPanel.add(subscribeExpiresLabel);

        valuesPresOpPanel.add(pollPeriodField);
        valuesPresOpPanel.add(subscribeExpiresField);

        buttonsPresOpPanel.add(enablePresOpButton);
        buttonsPresOpPanel.add(forceP2PPresOpButton);

        presenceOpPanel.add(buttonsPresOpPanel, BorderLayout.NORTH);
        presenceOpPanel.add(labelsPresOpPanel, BorderLayout.WEST);
        presenceOpPanel.add(valuesPresOpPanel, BorderLayout.CENTER);

        presenceOpPanel.setBorder(BorderFactory.createTitledBorder(
            Resources.getString("plugin.sipaccregwizz.PRESENCE_OPTIONS")));

        advancedPanel.add(presenceOpPanel);

        JPanel emptyLabelPanel = new TransparentPanel();
        emptyLabelPanel.setMaximumSize(new Dimension(40, 35));

        keepAliveLabels.add(keepAliveMethodLabel);
        keepAliveLabels.add(keepAliveIntervalLabel);
        keepAliveLabels.add(emptyLabelPanel);

        this.keepAliveIntervalExampleLabel.setForeground(Color.GRAY);
        this.keepAliveIntervalExampleLabel
            .setFont(uinExampleLabel.getFont().deriveFont(8));
        this.keepAliveIntervalExampleLabel
            .setMaximumSize(new Dimension(40, 35));
        this.keepAliveIntervalExampleLabel
            .setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        keepAliveIntervalValue
            .setText(SIPAccountRegistration.DEFAULT_KEEP_ALIVE_INTERVAL);

        keepAliveMethodBox.setSelectedItem(
            SIPAccountRegistration.DEFAULT_KEEP_ALIVE_METHOD);

        keepAliveValues.add(keepAliveMethodBox);
        keepAliveValues.add(keepAliveIntervalValue);
        keepAliveValues.add(keepAliveIntervalExampleLabel);

        keepAlivePanel.add(keepAliveLabels, BorderLayout.WEST);
        keepAlivePanel.add(keepAliveValues, BorderLayout.CENTER);

        keepAlivePanel.setBorder(BorderFactory.createTitledBorder(
            Resources.getString("plugin.sipaccregwizz.KEEP_ALIVE")));

        advancedPanel.add(keepAlivePanel);

        tabbedPane.addTab(
            Resources.getString("service.gui.ADVANCED"),
            advancedPanel);

        this.add(tabbedPane, BorderLayout.NORTH);
    }

    /**
     * Implements the <code>WizardPage.getIdentifier</code> to return this
     * page identifier.
     */
    public Object getIdentifier()
    {
        return FIRST_PAGE_IDENTIFIER;
    }

    /**
     * Implements the <code>WizardPage.getNextPageIdentifier</code> to return
     * the next page identifier - the summary page.
     */
    public Object getNextPageIdentifier()
    {
        return nextPageIdentifier;
    }

    /**
     * Implements the <code>WizardPage.getBackPageIdentifier</code> to return
     * the next back identifier - the default page.
     */
    public Object getBackPageIdentifier()
    {
        return WizardPage.DEFAULT_PAGE_IDENTIFIER;
    }

    /**
     * Implements the <code>WizardPage.getWizardForm</code> to return this
     * panel.
     */
    public Object getWizardForm()
    {
        return this;
    }

    /**
     * Before this page is displayed enables or disables the "Next" wizard
     * button according to whether the UIN field is empty.
     */
    public void pageShowing()
    {
        this.setNextButtonAccordingToUIN();
        wizard.getWizardContainer().setBackButtonEnabled(false);
    }

    /**
     * Saves the user input when the "Next" wizard buttons is clicked.
     */
    public void commitPage()
    {
        String uin = uinField.getText();
        int indexOfSeparator = uin.indexOf('@');
        if (indexOfSeparator > -1) {
            uin = uin.substring(0, indexOfSeparator);
        }

        SIPAccountRegistration registration = wizard.getRegistration();

        registration.setId(uinField.getText());

        if (passField.getPassword() != null)
            registration.setPassword(new String(passField.getPassword()));

        registration.setRememberPassword(rememberPassBox.isSelected());

        registration.setServerAddress(serverField.getText());
        registration.setServerPort(serverPortField.getText());
        registration.setProxy(proxyField.getText());
        registration.setProxyPort(proxyPortField.getText());
        registration.setPreferredTransport(transportCombo.getSelectedItem()
            .toString());

        registration.setEnablePresence(enablePresOpButton.isSelected());
        registration.setForceP2PMode(forceP2PPresOpButton.isSelected());
        registration.setDefaultEncryption(enableDefaultEncryption.isSelected()); 
        registration.setPollingPeriod(pollPeriodField.getText());
        registration.setSubscriptionExpiration(subscribeExpiresField
            .getText());
        registration.setKeepAliveMethod(
            keepAliveMethodBox.getSelectedItem().toString());
        registration.setKeepAliveInterval(keepAliveIntervalValue.getText());

        wizard.getWizardContainer().setBackButtonEnabled(true);

        nextPageIdentifier = SUMMARY_PAGE_IDENTIFIER;

        this.isCommitted = true;
    }

    /**
     * Enables or disables the "Next" wizard button according to whether the UIN
     * field is empty.
     */
    private void setNextButtonAccordingToUIN()
    {
        if (uinField.getText() == null || uinField.getText().equals(""))
        {
            wizard.getWizardContainer().setNextFinishButtonEnabled(false);
        }
        else
        {
            wizard.getWizardContainer().setNextFinishButtonEnabled(true);
        }
    }

    /**
     * Handles the <tt>DocumentEvent</tt> triggered when user types in the UIN
     * field. Enables or disables the "Next" wizard button according to whether
     * the UIN field is empty.
     */
    public void insertUpdate(DocumentEvent e)
    {
        this.setNextButtonAccordingToUIN();
        this.setServerFieldAccordingToUIN();
    }

    /**
     * Handles the <tt>DocumentEvent</tt> triggered when user deletes letters
     * from the UIN field. Enables or disables the "Next" wizard button
     * according to whether the UIN field is empty.
     */
    public void removeUpdate(DocumentEvent e)
    {
        this.setNextButtonAccordingToUIN();
        this.setServerFieldAccordingToUIN();
    }

    public void changedUpdate(DocumentEvent e)
    {
    }

    public void pageHiding()
    {
    }

    public void pageShown()
    {
    }

    public void pageBack()
    {
    }

    /**
     * Fills the UIN and Password fields in this panel with the data coming from
     * the given protocolProvider.
     *
     * @param protocolProvider The <tt>ProtocolProviderService</tt> to load
     *            the data from.
     */
    public void loadAccount(ProtocolProviderService protocolProvider)
    {
        AccountID accountID = protocolProvider.getAccountID();
        String password = accountID.getAccountPropertyString(
                            ProtocolProviderFactory.PASSWORD);

        String serverAddress = accountID.getAccountPropertyString(
                            ProtocolProviderFactory.SERVER_ADDRESS);

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

        boolean enabledDefaultEncryption = accountID.getAccountPropertyBoolean(
                            ProtocolProviderFactory.DEFAULT_ENCRYPTION, false);

        String pollingPeriod = accountID.getAccountPropertyString(
                            ProtocolProviderFactory.POLLING_PERIOD);

        String subscriptionPeriod = accountID.getAccountPropertyString(
                            ProtocolProviderFactory.SUBSCRIPTION_EXPIRATION);

        String keepAliveMethod =
            accountID.getAccountPropertyString("KEEP_ALIVE_METHOD");

        String keepAliveInterval =
            accountID.getAccountPropertyString("KEEP_ALIVE_INTERVAL");

        this.isServerOverridden = accountID.getAccountPropertyBoolean(
            ProtocolProviderFactory.IS_SERVER_OVERRIDDEN, false);

        uinField.setEnabled(false);
        this.uinField.setText((serverAddress == null) ? accountID.getUserID()
            : (accountID.getUserID() + "@" + serverAddress));

        if (password != null)
        {
            this.passField.setText(password);
            this.rememberPassBox.setSelected(true);
        }

        serverField.setText(serverAddress);
        serverField.setEnabled(false);
        serverPortField.setText(serverPort);
        proxyField.setText(proxyAddress);

        // The order of the next two fields is important, as a change listener
        // of the transportCombo sets the proxyPortField to its default
        transportCombo.setSelectedItem(preferredTransport);
        proxyPortField.setText(proxyPort);

        enablePresOpButton.setSelected(enablePresence);
        forceP2PPresOpButton.setSelected(forceP2P);
        enableDefaultEncryption.setSelected(enabledDefaultEncryption); 
        pollPeriodField.setText(pollingPeriod);
        subscribeExpiresField.setText(subscriptionPeriod);

        if (!enablePresence)
        {
            setPresenceOptionsEnabled(enablePresence);
        }

        keepAliveMethodBox.setSelectedItem(keepAliveMethod);
        keepAliveIntervalValue.setText(keepAliveInterval);
    }

    /**
     * Parse the server part from the sip id and set it to server as default
     * value. If Advanced option is enabled Do nothing.
     */
    private void setServerFieldAccordingToUIN()
    {
        String serverAddress
            = wizard.getServerFromUserName(uinField.getText());

        if (!wizard.isModification() || !isServerOverridden)
        {
            serverField.setText(serverAddress);
            proxyField.setText(serverAddress);
        }
    }

    /**
     * Enables or disable all presence related options.
     * 
     * @param isEnabled <code>true</code> to enable the presence related
     * options, <code>false</code> - to disable them.
     */
    private void setPresenceOptionsEnabled(boolean isEnabled)
    {
        forceP2PPresOpButton.setEnabled(isEnabled);
        pollPeriodField.setEnabled(isEnabled);
        subscribeExpiresField.setEnabled(isEnabled);
    }

    public void itemStateChanged(ItemEvent e)
    {
        if (e.getStateChange() == ItemEvent.SELECTED
            && e.getItem().equals("TLS"))
        {
            serverPortField.setText(SIPAccountRegistration.DEFAULT_TLS_PORT);
            proxyPortField.setText(SIPAccountRegistration.DEFAULT_TLS_PORT);
        }
        else
        {
            serverPortField.setText(SIPAccountRegistration.DEFAULT_PORT);
            proxyPortField.setText(SIPAccountRegistration.DEFAULT_PORT);
        }
    }

    public Object getSimpleForm()
    {
        return uinPassPanel;
    }

    public boolean isCommitted()
    {
        return isCommitted;
    }
}
