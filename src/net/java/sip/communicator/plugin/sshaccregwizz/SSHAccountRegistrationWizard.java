/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 * SSHAccountRegistrationWizard.java
 *
 * Created on 22 May, 2007, 8:51 AM
 *
 * SSH Suport in SIP Communicator - GSoC' 07 Project
 *
 */

package net.java.sip.communicator.plugin.sshaccregwizz;

import java.awt.*;
import java.util.*;

import org.osgi.framework.*;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.impl.protocol.ssh.*;

/**
 * The <tt>SSHAccountRegistrationWizard</tt> is an implementation of the
 * <tt>AccountRegistrationWizard</tt> for the SSH protocol. It allows
 * the user to create and configure a new SSH account.
 *
 * @author Shobhit Jindal
 */
public class SSHAccountRegistrationWizard
    implements AccountRegistrationWizard
{
    /**
     * The first page of the ssh account registration wizard.
     */
    private FirstWizardPage firstWizardPage;

    /**
     * The object that we use to store details on an account that we will be
     * creating.
     */
    private SSHAccountRegistration registration
        = new SSHAccountRegistration();

    private WizardContainer wizardContainer;

    private ProtocolProviderService protocolProvider;

    private String propertiesPackage
        = "net.java.sip.communicator.plugin.sshaccregwizz";

    private boolean isModification;

    /**
     * Creates an instance of <tt>SSHAccountRegistrationWizard</tt>.
     * @param wizardContainer the wizard container, where this wizard
     * is added
     */
    public SSHAccountRegistrationWizard(WizardContainer wizardContainer)
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
        return Resources.getImage(Resources.SSH_LOGO);
    }

    /**
     * Implements the <code>AccountRegistrationWizard.getPageImage</code>
     *  method.
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

        /*
         * Hashtable arranges the entries alphabetically so the order of
         * appearance is:
         * - Computer Name / IP
         * - Port
         * - User ID
         */

        summaryTable.put("Account ID", registration.getAccountID());
        summaryTable.put("Known Hosts", registration.getKnownHostsFile());
        summaryTable.put("Identity", registration.getIdentityFile());

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
            = SSHAccRegWizzActivator.getSSHProtocolProviderFactory();

        return this.installAccount(factory,
                                   registration.getAccountID());
    }

    /**
     * Creates an account for the given Account ID, Identity File and Known
     *  Hosts File
     *
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

        accountProperties.put(ProtocolProviderFactorySSHImpl.IDENTITY_FILE,
                                    registration.getIdentityFile());

        accountProperties.put(ProtocolProviderFactorySSH.KNOWN_HOSTS_FILE,
                            String.valueOf(registration.getKnownHostsFile()));

        if (isModification)
        {
            providerFactory.uninstallAccount(protocolProvider.getAccountID());
            this.protocolProvider = null;
            this.isModification = false;
        }

        try
        {
            AccountID accountID = providerFactory.installAccount(
                user, accountProperties);

            ServiceReference serRef = providerFactory
                .getProviderForAccount(accountID);

            protocolProvider = (ProtocolProviderService)
                SSHAccRegWizzActivator.bundleContext
                .getService(serRef);
        }
        catch (IllegalArgumentException exc)
        {
            SSHAccRegWizzActivator.getUIService().getPopupDialog()
                .showMessagePopupDialog(
                    exc.getMessage(),
                    Resources.getString("error"),
                    PopupDialog.ERROR_MESSAGE);
        }
        catch (IllegalStateException exc)
        {
            SSHAccRegWizzActivator.getUIService().getPopupDialog()
                .showMessagePopupDialog(
                    exc.getMessage(),
                    Resources.getString("error"),
                    PopupDialog.ERROR_MESSAGE);
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
        this.isModification = true;

        this.protocolProvider = protocolProvider;

        this.registration = new SSHAccountRegistration();

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
    public SSHAccountRegistration getRegistration()
    {
        return registration;
    }
    
    /**
     * Returns the size of this wizard.
     * @return the size of this wizard
     */
    public Dimension getSize()
    {
        return new Dimension(600, 500);
    }
}
