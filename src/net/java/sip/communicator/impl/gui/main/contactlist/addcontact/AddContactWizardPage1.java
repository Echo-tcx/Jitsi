/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package net.java.sip.communicator.impl.gui.main.contactlist.addcontact;

import java.util.*;

import javax.swing.event.*;

import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.protocol.*;

/**
 * The <tt>AddContactWizardPage1</tt> is the first page of the "Add Contact"
 * wizard. Contains the <tt>SelectAccountPanel</tt>, where the user should
 * select the account, where the new contact will be created.
 * 
 * @author Yana Stamcheva
 */
public class AddContactWizardPage1
    implements WizardPage, ListSelectionListener
{

    public static final String IDENTIFIER = "SELECT_ACCOUNT_PANEL";

    private SelectAccountPanel selectAccountPanel;

    private WizardContainer wizard;

    /**
     * Creates an instance of <tt>AddContactWizardPage1</tt>.
     * 
     * @param newContact An object that collects all user choices through the
     *            wizard.
     * @param providerList The list of available
     *            <tt>ProtocolProviderServices</tt>, from which the user
     *            could select.
     */
    public AddContactWizardPage1(WizardContainer wizard,
                                 NewContact newContact,
                                 Iterator<ProtocolProviderService> providerList)
    {

        this.wizard = wizard;

        selectAccountPanel
            = new SelectAccountPanel(newContact, providerList);
        selectAccountPanel.addListSelectionListener(this);
    }

    /**
     * Before the panel is displayed checks the selections and enables the next
     * button if a checkbox is already selected or disables it if nothing is
     * selected.
     */
    public void pageShowing()
    {
        setNextButtonAccordingToCheckBox();
    }

    /**
     * Enables the next button when the user makes a choice and disables it if
     * nothing is selected.
     */
    private void setNextButtonAccordingToCheckBox()
    {
        if (selectAccountPanel.isAccountSelected())
            this.wizard.setNextFinishButtonEnabled(true);
        else
            this.wizard.setNextFinishButtonEnabled(false);
    }

    /**
     * If there's no protocol selected by default the "Next" button would 
     * start as disabled. This method should make sure we re-enable it 
     * once a protocol has been selected
     * 
     * @param e the <tt>ListSelectionEvent</tt> that has just occurred.
     */
    public void valueChanged(ListSelectionEvent e)
    {
        setNextButtonAccordingToCheckBox();
    }

    public Object getIdentifier()
    {
        return IDENTIFIER;
    }

    public Object getNextPageIdentifier()
    {
        return AddContactWizardPage2.IDENTIFIER;
    }

    public Object getBackPageIdentifier()
    {
        return IDENTIFIER;
    }

    public Object getWizardForm()
    {
        return selectAccountPanel;
    }

    public void pageHiding()
    {
    }

    public void pageShown()
    {
    }

    public void commitPage()
    {
        selectAccountPanel.initSelectedAccount();
    }

    public void pageBack()
    {
    }
}
