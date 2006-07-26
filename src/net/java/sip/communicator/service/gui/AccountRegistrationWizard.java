/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.service.gui;

import java.util.Iterator;

/**
 * The <tt>AccountRegistrationWizard</tt> is meant to provide a wizard which
 * will guide the user through a protocol account registration. Each
 * <tt>AccountRegistrationWizard</tt> should provide a set of
 * <tt>WizardPage</tt>s, an icon, the name and the description of the
 * corresponding protocol.
 * <p>
 * Note that the <tt>AccountRegistrationWizard</tt> is NOT a real wizard, it
 * doesn't handle wizard events. Each UI Service implementation should provide
 * its own wizard ui control, which should manage all the events, panels and
 * buttons, etc.
 * <p>
 * It depends on the wizard implementation in the UI for whether or not a
 * summary will be shown to the user before "Finish".
 * 
 * @author Yana Stamcheva
 */
public interface AccountRegistrationWizard {

    /**
     * Returns the protocol icon that will be shown on the left of the protocol
     * name in the list, where user will choose the protocol to register to.
     * 
     * @return a short description of the protocol.
     */
    public byte[] getIcon();
    
    /**
     * Returns the protocol name that will be shown in the list, where user
     * will choose the protocol to register to.
     * 
     * @return the protocol name.
     */
    public String getProtocolName();
    
    /**
     * Returns a short description of the protocol that will be shown on the
     * right of the protocol name in the list, where user will choose the
     * protocol to register to.
     * 
     * @return a short description of the protocol.
     */
    public String getProtocolDescription();
    
    /**
     * Returns the set of <tt>AccountRegistrationWizardPage</tt>-s for this
     * wizard. 
     * 
     * @return the set of <tt>AccountRegistrationWizardPage</tt>-s for this
     * wizard. 
     */
    public Iterator getPages();
    
    /**
     * Returns a set of key-value pairs that will represent the summary for
     * this wizard.
     * 
     * @return a set of key-value pairs that will represent the summary for
     * this wizard. 
     */
    public Iterator getSummary();
    
    /**
     * Defines the operations that will be executed when the user clicks on
     * the wizard "Finish" button.
     */
    public void finish();
}
