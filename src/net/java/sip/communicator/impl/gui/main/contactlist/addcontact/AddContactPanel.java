/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.contactlist.addcontact;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.customcontrols.*;
import net.java.sip.communicator.impl.gui.utils.*;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.util.swing.*;

/**
 * The <tt>AddContactPanel</tt> is the form for adding a contact. It's used
 * in the "Add Contact" wizard and the "Add Contact" dialog.
 *  
 * @author Yana Stamcheva
 */
public class AddContactPanel
    extends TransparentPanel
    implements DocumentListener
{

    private JLabel uinLabel = new JLabel(
        GuiActivator.getResources().getI18NString("service.gui.IDENTIFIER"));

    private JTextField textField = new JTextField();

    private TransparentPanel dataPanel
        = new TransparentPanel(new BorderLayout(5, 5));

    private SIPCommMsgTextArea infoLabel 
        = new SIPCommMsgTextArea(GuiActivator.getResources()
                .getI18NString("service.gui.ADD_CONTACT_IDENTIFIER"));

    private JLabel infoTitleLabel = new JLabel(
        GuiActivator.getResources().getI18NString("service.gui.ADD_CONTACT"));

    private JLabel iconLabel = new JLabel(new ImageIcon(ImageLoader
            .getImage(ImageLoader.ADD_CONTACT_WIZARD_ICON)));

    private TransparentPanel labelsPanel
        = new TransparentPanel(new GridLayout(0, 1, 10, 10));

    private TransparentPanel rightPanel
        = new TransparentPanel(new BorderLayout());

    private WizardContainer parentWizard;

    /**
     * Creates and initializes the <tt>AddContactPanel</tt>.
     */
    public AddContactPanel()
    {
        this(null);
    }
    
    /**
     * Creates and initializes the <tt>AddContactPanel</tt>.
     * @param wizard The parent wizard, where this add contact panel is added
     */
    public AddContactPanel(WizardContainer wizard)
    {
        super(new BorderLayout());

        this.parentWizard = wizard;

        this.setPreferredSize(new Dimension(650, 300));

        this.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10));

        this.iconLabel.setBorder(
            BorderFactory.createEmptyBorder(0, 10, 10, 10));

        this.infoLabel.setEditable(false);

        this.dataPanel.add(uinLabel, BorderLayout.WEST);

        this.dataPanel.add(textField, BorderLayout.CENTER);

        this.infoTitleLabel.setHorizontalAlignment(JLabel.CENTER);
        this.infoTitleLabel.setFont(Constants.FONT.deriveFont(Font.BOLD, 18));

        this.labelsPanel.add(infoTitleLabel);
        this.labelsPanel.add(infoLabel);
        this.labelsPanel.add(dataPanel);

        this.rightPanel.setBorder(
            BorderFactory.createEmptyBorder(0, 10, 10, 10));

        this.rightPanel.add(labelsPanel, BorderLayout.NORTH);

        this.add(iconLabel, BorderLayout.WEST);
        this.add(rightPanel, BorderLayout.CENTER);

        this.textField.getDocument().addDocumentListener(this);
    }
    
    /**
     * Returns the string identifier entered by user.
     * @return the string identifier entered by user
     */
    public String getUIN()
    {
        return textField.getText();
    }
    
    public void setUIN(String uin)
    {
        textField.setText(uin);
    }
    
    public void requestFocusInField() {
        this.textField.requestFocus();
    }

    public void changedUpdate(DocumentEvent e)
    {   
    }

    public void insertUpdate(DocumentEvent e)
    {
        this.setNextFinishButtonAccordingToUIN();
    }

    public void removeUpdate(DocumentEvent e)
    {
        this.setNextFinishButtonAccordingToUIN();
    }
    
    public void setNextFinishButtonAccordingToUIN()
    {        
        if(parentWizard != null) {
            if(textField.getText() != null && textField.getText().length() > 0){                
                parentWizard.setNextFinishButtonEnabled(true);
            }
            else {                
                parentWizard.setNextFinishButtonEnabled(false);
            }
        }
    }
}
