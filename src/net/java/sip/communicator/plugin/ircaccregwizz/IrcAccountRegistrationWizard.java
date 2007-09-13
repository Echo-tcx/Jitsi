/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.plugin.ircaccregwizz;

import java.util.*;

import org.osgi.framework.*;
import net.java.sip.communicator.impl.gui.customcontrols.*;
import net.java.sip.communicator.plugin.ircaccregwizz.Resources;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.protocol.*;

/**
 * The <tt>IrcAccountRegistrationWizard</tt> is an implementation of the
 * <tt>AccountRegistrationWizard</tt> for the IRC protocol. It allows
 * the user to create and configure a new IRC account.
 *
 * @author Lionel Ferreira & Michael Tarantino
 */
public class IrcAccountRegistrationWizard
    implements AccountRegistrationWizard
{

    /**
     * The first page of the IRC account registration wizard.
     */
    private FirstWizardPage firstWizardPage;

    /**
     * The object that we use to store details on an account that we will be
     * creating.
     */
    private IrcAccountRegistration registration
        = new IrcAccountRegistration();

    private WizardContainer wizardContainer;

    private ProtocolProviderService protocolProvider;

    /**
     * Creates an instance of <tt>IrcAccountRegistrationWizard</tt>.
     * @param wizardContainer the wizard container, where this wizard
     * is added
     */
    public IrcAccountRegistrationWizard(WizardContainer wizardContainer)
    {
        this.wizardContainer = wizardContainer;
    }

    /**
     * Implements the <code>AccountRegistrationWizard.getIcon</code> method.
     * Returns the icon to be used for this wizard.
     * @return byte[]
     */
    public byte[] getIcon()
    {
        return Resources.getImage(Resources.IRC_LOGO);
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
        firstWizardPage = new FirstWizardPage(registration, wizardContainer);

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
        String pass = new String();
        String port = new String();

        if (registration.isRequiredPassword())
            pass = "required";
        else
            pass = "not required";

        if (!(port = registration.getPort()).equals(""))
            port = ":" + port;

        summaryTable.put("Password", pass);
        summaryTable.put("Nickname", registration.getUserID());
        summaryTable.put("Server IRC", registration.getServer() + port);

        return summaryTable.entrySet().iterator();
    }

    /**
     * Installs the account created through this wizard.
     * @return ProtocolProviderService
     */
    public ProtocolProviderService finish()
    {
        firstWizardPage = null;
        ProtocolProviderFactory factory
            = IrcAccRegWizzActivator.getIrcProtocolProviderFactory();

        return this.installAccount(factory,
                                   registration.getUserID());
    }

    /**
     * Creates an account for the given user and password.
     * @param providerFactory the ProtocolProviderFactory which will create
     * the account
     * @param user the user identifier
     * @return the <tt>ProtocolProviderService</tt> for the new account.
     */
    public ProtocolProviderService installAccount(
                                        ProtocolProviderFactory providerFactory,
                                        String user)
    {

        Hashtable accountProperties = new Hashtable();
        
        accountProperties.put(ProtocolProviderFactory.SERVER_ADDRESS,
            registration.getServer());
        
        if (registration.isRememberPassword()
                && registration.isRequiredPassword()
                && !registration.getPassword().equals(""))
        {
            accountProperties.put(
                ProtocolProviderFactory.PASSWORD, registration.getPassword());
        }
        
        if (!registration.getPort().equals(""))
        {
            accountProperties.put(
                ProtocolProviderFactory.SERVER_PORT, registration.getPort());
        }
        
        accountProperties.put(
                ProtocolProviderFactory.AUTO_CHANGE_USER_NAME,
                new Boolean(registration.isAutoChangeNick()).toString());

        accountProperties.put(
                ProtocolProviderFactory.PASSWORD_REQUIRED,
                new Boolean(registration.isRequiredPassword()).toString());
        
        try
        {
            AccountID accountID = providerFactory.installAccount(
                user, accountProperties);

            ServiceReference serRef = providerFactory
                .getProviderForAccount(accountID);

            protocolProvider = (ProtocolProviderService)
                IrcAccRegWizzActivator.bundleContext
                .getService(serRef);
        }
        catch (IllegalArgumentException exc)
        {
            new ErrorDialog(null, exc.getMessage(), exc).showDialog();
        }
        catch (IllegalStateException exc)
        {
            new ErrorDialog(null, exc.getMessage(), exc).showDialog();
        }

        return protocolProvider;
    }

    /**
     * Fills the UserID and Password fields in this panel with the data coming
     * from the given protocolProvider.
     * @param protocolProvider The <tt>ProtocolProviderService</tt> to load the
     * data from.
     */
    public void loadAccount(ProtocolProviderService protocolProvider)
    {

        this.protocolProvider = protocolProvider;

        this.firstWizardPage.loadAccount(protocolProvider);
    }
}
