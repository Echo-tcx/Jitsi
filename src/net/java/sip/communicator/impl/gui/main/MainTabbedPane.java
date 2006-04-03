/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package net.java.sip.communicator.impl.gui.main;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.text.Position;

import net.java.sip.communicator.impl.gui.main.contactlist.CListKeySearchListener;
import net.java.sip.communicator.impl.gui.main.contactlist.ContactList;
import net.java.sip.communicator.impl.gui.main.contactlist.ContactListPanel;
import net.java.sip.communicator.impl.gui.main.customcontrols.tabbedPane.SIPCommTabbedPane;
import net.java.sip.communicator.impl.gui.main.i18n.Messages;
import net.java.sip.communicator.impl.gui.main.utils.AntialiasingManager;

/**
 * @author Yana Stamcheva
 *
 * The main tabbed pane containing the contact list panel, the 
 * call list panel and the dial panel. 
 */
public class MainTabbedPane extends SIPCommTabbedPane {
	
	private DialPanel dialPanel = new DialPanel();
	
	private ContactListPanel contactListPanel;
	
	public MainTabbedPane(MainFrame parent){
        super(true);
        
        this.setCloseIcon(false);
        this.setMaxIcon(false);
        
		contactListPanel = new ContactListPanel(parent);
		
		dialPanel.setPhoneNumberCombo(parent.getCallPanel().getPhoneNumberCombo());
		
		this.addTab(Messages.getString("contacts"), contactListPanel);
		this.addTab(Messages.getString("callList"), new JPanel());
		this.addTab(Messages.getString("dial"), dialPanel);
        
        // Disable the tabs that containt not yet implemented functionalities.
        this.setEnabledAt(1, false);
        this.setEnabledAt(2, false);        
	}
	
    public ContactListPanel getContactListPanel() {
        return contactListPanel;
    }
}
