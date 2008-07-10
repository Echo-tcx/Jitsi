/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.plugin.googletalkaccregwizz;

import java.awt.*;
import java.util.*;

import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.protocol.*;

import org.osgi.framework.*;

/**
 * The <tt>GoogleTalkAccountRegistrationWizard</tt> is an implementation of the
 * <tt>AccountRegistrationWizard</tt> for the Google Talk protocol. It should
 * allow the user to create and configure a new Google Talk account.
 *
 * @author Lubomir Marinov
 */
public class GoogleTalkAccountRegistrationWizard
    implements AccountRegistrationWizard
{
    private static final String GOOGLE_USER_SUFFIX = "gmail.com";

    private static final String GOOGLE_CONNECT_SRV = "talk.google.com";

    public static final String PROTOCOL = "Google Talk";

    private FirstWizardPage firstWizardPage;

    private GoogleTalkAccountRegistration registration
        = new GoogleTalkAccountRegistration();

    private WizardContainer wizardContainer;

    private ProtocolProviderService protocolProvider;

    private boolean isModification;

    /**
     * Creates an instance of <tt>GoogleTalkAccountRegistrationWizard</tt>.
     * @param wizardContainer the wizard container, where this wizard
     * is added
     */
    public GoogleTalkAccountRegistrationWizard(WizardContainer wizardContainer)
    {
        this.wizardContainer = wizardContainer;

        this.wizardContainer.setFinishButtonText(Resources.getString("signin"));
    }

    /**
     * Implements the <code>AccountRegistrationWizard.getIcon</code> method.
     * Returns the icon to be used for this wizard.
     * @return byte[]
     */
    public byte[] getIcon()
    {
        return Resources.getImage(Resources.PROTOCOL_ICON);
    }
    
    /**
     * Implements the <code>AccountRegistrationWizard.getPageImage</code> method.
     * Returns the image used to decorate the wizard page
     * 
     * @return byte[] the image used to decorate the wizard page
     */
    public byte[] getPageImage()
    {
        return Resources.getImage(Resources.PAGE_IMAGE);
    }

    /**
     * Implements the <code>AccountRegistrationWizard.getProtocolName</code>
     * method. Returns the protocol name for this wizard.
     * @return String
     */
    public String getProtocolName()
    {
        return Resources.getString("protocolName");
    }

    /**
     * Implements the <code>AccountRegistrationWizard.getProtocolDescription
     * </code> method. Returns the description of the protocol for this wizard.
     * @return String
     */
    public String getProtocolDescription()
    {
        return Resources.getString("protocolDescription");
    }

    /**
     * Returns the set of pages contained in this wizard.
     * @return Iterator
     */
    public Iterator getPages()
    {
        ArrayList pages = new ArrayList();
        firstWizardPage = new FirstWizardPage(this);

        pages.add(firstWizardPage);

        return pages.iterator();
    }

    /**
     * Returns the set of data that user has entered through this wizard.
     * @return Iterator
     */
    public Iterator getSummary()
    {
        Hashtable summaryTable = new Hashtable();

        summaryTable.put(   Resources.getString("username"),
                            registration.getUserID());

        summaryTable.put(   Resources.getString("rememberPassword"),
                            new Boolean(registration.isRememberPassword()));

        summaryTable.put(   Resources.getString("server"),
                            registration.getServerAddress());

        summaryTable.put(   Resources.getString("port"),
                            String.valueOf(registration.getPort()));

        summaryTable.put(   Resources.getString("enableKeepAlive"),
            String.valueOf(registration.isSendKeepAlive()));

        summaryTable.put(   Resources.getString("resource"),
                            registration.getResource());

        summaryTable.put(   Resources.getString("priority"),
                            String.valueOf(registration.getPriority()));

        return summaryTable.entrySet().iterator();
    }

    /**
     * Installs the account created through this wizard.
     * 
     * @return ProtocolProviderService
     */
    public ProtocolProviderService signin()
    {
        if (!firstWizardPage.isCommitted())
            firstWizardPage.commitPage();

        return signin(  registration.getUserID(),
                        registration.getPassword());
    }

    public ProtocolProviderService signin(String userName, String password)
    {
        firstWizardPage = null;
        ProtocolProviderFactory factory
            = GoogleTalkAccRegWizzActivator.getGoogleTalkProtocolProviderFactory();

        return this.installAccount(factory,
                                   userName,
                                   password);
    }

    /**
     * Creates an account for the given user and password.
     * 
     * @param providerFactory the ProtocolProviderFactory which will create
     * the account
     * @param user the user identifier
     * @param passwd the password
     * @return the <tt>ProtocolProviderService</tt> for the new account.
     */
    public ProtocolProviderService installAccount(
        ProtocolProviderFactory providerFactory,
        String userName,
        String passwd)
    {
        Hashtable accountProperties = new Hashtable();

        /* Make the account use the resources specific to Google Talk. */
        accountProperties.put(ProtocolProviderFactory.PROTOCOL, PROTOCOL);
        accountProperties
            .put(ProtocolProviderFactory.PROTOCOL_ICON_PATH,
                GoogleTalkAccountRegistrationWizard.class.getClassLoader()
                    .getResource("resources/images/protocol/googletalk")
                    .toString());

        if (registration.isRememberPassword())
        {
            accountProperties.put(ProtocolProviderFactory.PASSWORD, passwd);
        }

        accountProperties.put("SEND_KEEP_ALIVE",
                              String.valueOf(registration.isSendKeepAlive()));

        String serverName = null;
        if (registration.getServerAddress() != null)
        {
            serverName = registration.getServerAddress();
        }
        else
        {
            serverName = getServerFromUserName(userName);
        }
        accountProperties.put(ProtocolProviderFactory.SERVER_ADDRESS,
            serverName);

        accountProperties.put(ProtocolProviderFactory.SERVER_PORT,
            String.valueOf(registration.getPort()));

        accountProperties.put(ProtocolProviderFactory.RESOURCE,
                              registration.getResource());

        accountProperties.put(ProtocolProviderFactory.RESOURCE_PRIORITY,
                              String.valueOf(registration.getPriority()));

        if (isModification)
        {
            providerFactory.modifyAccount(  protocolProvider,
                accountProperties);

            this.isModification  = false;

            return protocolProvider;
        }

        try
        {
            AccountID accountID = providerFactory.installAccount(
                userName, accountProperties);

            ServiceReference serRef = providerFactory
                .getProviderForAccount(accountID);

            protocolProvider = (ProtocolProviderService)
                GoogleTalkAccRegWizzActivator.bundleContext
                .getService(serRef);
        }
        catch (IllegalArgumentException exc)
        {
            GoogleTalkAccRegWizzActivator.getUIService().getPopupDialog()
                .showMessagePopupDialog(exc.getMessage(),
                    Resources.getString("error"),
                    PopupDialog.ERROR_MESSAGE);
        }
        catch (IllegalStateException exc)
        {
            GoogleTalkAccRegWizzActivator.getUIService().getPopupDialog()
                .showMessagePopupDialog(exc.getMessage(),
                    Resources.getString("error"),
                    PopupDialog.ERROR_MESSAGE);
        }

        return protocolProvider;
    }

    /**
     * Fills the User ID and Password fields in this panel with the data coming
     * from the given protocolProvider.
     * @param protocolProvider The <tt>ProtocolProviderService</tt> to load the
     * data from.
     */
    public void loadAccount(ProtocolProviderService protocolProvider)
    {
        this.isModification = true;

        this.protocolProvider = protocolProvider;

        this.registration = new GoogleTalkAccountRegistration();

        this.firstWizardPage.loadAccount(protocolProvider);
    }

    /**
     * Indicates if this wizard is opened for modification or for creating a
     * new account.
     * 
     * @return <code>true</code> if this wizard is opened for modification and
     * <code>false</code> otherwise.
     */
    public boolean isModification()
    {
        return isModification;
    }

    /**
     * Returns the wizard container, where all pages are added.
     * 
     * @return the wizard container, where all pages are added
     */
    public WizardContainer getWizardContainer()
    {
        return wizardContainer;
    }

    /**
     * Returns the registration object, which will store all the data through
     * the wizard.
     * 
     * @return the registration object, which will store all the data through
     * the wizard
     */
    public GoogleTalkAccountRegistration getRegistration()
    {
        return registration;
    }

    /**
     * Returns the size of this wizard.
     * @return the size of this wizard
     */
    public Dimension getSize()
    {
        return new Dimension(300, 480);
    }
    
    /**
     * Returns the identifier of the page to show first in the wizard.
     * @return the identifier of the page to show first in the wizard.
     */
    public Object getFirstPageIdentifier()
    {
        return firstWizardPage.getIdentifier();
    }

    /**
     * Returns the identifier of the page to show last in the wizard.
     * @return the identifier of the page to show last in the wizard.
     */
    public Object getLastPageIdentifier()
    {
        return firstWizardPage.getIdentifier();
    }

    /**
     * Sets the modification property to indicate if this wizard is opened for
     * a modification.
     * 
     * @param isModification indicates if this wizard is opened for modification
     * or for creating a new account. 
     */
    public void setModification(boolean isModification)
    {
        this.isModification = isModification;
    }

    /**
     * Returns an example string, which should indicate to the user how the
     * user name should look like.
     * @return an example string, which should indicate to the user how the
     * user name should look like.
     */
    public String getUserNameExample()
    {
        return FirstWizardPage.USER_NAME_EXAMPLE;
    }

    /**
     * Enables the simple "Sign in" form.
     */
    public boolean isSimpleFormEnabled()
    {
        return true;
    }

    /**
     * Parse the server part from the Google Talk id and set it to server as
     * default value. If Advanced option is enabled Do nothing.
     */
    protected String getServerFromUserName(String userName)
    {
        int delimIndex = userName.indexOf("@");
        if (delimIndex != -1)
        {
            String newServerAddr = userName.substring(delimIndex + 1);
            if (newServerAddr.equals(GOOGLE_USER_SUFFIX))
            {
                return GOOGLE_CONNECT_SRV;
            }
            else
            {
                return newServerAddr;
            }
        }

        return null;
    }

    public void webSignup()
    {
        GoogleTalkAccRegWizzActivator.getBrowserLauncher()
            .openURL("https://www.google.com/accounts/NewAccount");
    }

    /**
     * Returns <code>true</code> if the web sign up is supported by the current
     * implementation, <code>false</code> - otherwise.
     * @return <code>true</code> if the web sign up is supported by the current
     * implementation, <code>false</code> - otherwise
     */
    public boolean isWebSignupSupported()
    {
        return true;
    }

    public Object getSimpleForm()
    {
        firstWizardPage = new FirstWizardPage(this);
        return firstWizardPage.getSimpleForm();
    }
}
