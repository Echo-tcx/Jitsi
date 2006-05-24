/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package net.java.sip.communicator.impl.gui.main.contactlist;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import net.java.sip.communicator.impl.gui.main.MainFrame;
import net.java.sip.communicator.impl.gui.main.customcontrols.MessageDialog;
import net.java.sip.communicator.impl.gui.main.history.HistoryWindow;
import net.java.sip.communicator.impl.gui.main.i18n.Messages;
import net.java.sip.communicator.impl.gui.utils.ImageLoader;
import net.java.sip.communicator.service.contactlist.MetaContact;

/**
 * The ContactRightButtonMenu is the menu, which the user could open by clicking
 * on a contact in the contact list with the right button of the mouse.
 * 
 * @author Yana Stamcheva
 */
public class ContactRightButtonMenu extends JPopupMenu implements
        ActionListener {

    private JMenu moveToMenu = new JMenu(Messages.getString("moveToGroup"));

    private JMenu addSubcontactMenu = new JMenu(Messages
            .getString("addSubcontact"));

    private JMenuItem sendMessageItem = new JMenuItem(Messages
            .getString("sendMess age"), new ImageIcon(ImageLoader
            .getImage(ImageLoader.SEND_MESSAGE_16x16_ICON)));

    private JMenuItem sendFileItem = new JMenuItem(Messages
            .getString("sendFile"), new ImageIcon(ImageLoader
            .getImage(ImageLoader.SEND_FILE_16x16_ICON)));

    private JMenuItem removeContactItem = new JMenuItem(Messages
            .getString("removeContact"), new ImageIcon(ImageLoader
            .getImage(ImageLoader.DELETE_16x16_ICON)));

    private JMenuItem renameContactItem = new JMenuItem(Messages
            .getString("renameContact"), new ImageIcon(ImageLoader
            .getImage(ImageLoader.RENAME_16x16_ICON)));

    private JMenuItem userInfoItem = new JMenuItem(Messages
            .getString("userInfo"), new ImageIcon(ImageLoader
            .getImage(ImageLoader.INFO_16x16_ICON)));

    private JMenuItem viewHistoryItem = new JMenuItem(Messages
            .getString("viewHistory"), new ImageIcon(ImageLoader
            .getImage(ImageLoader.HISTORY_16x16_ICON)));

    private MetaContact contactItem;

    private MainFrame mainFrame;

    /**
     * Creates an instance of ContactRightButtonMenu.
     * @param mainFrame The parent MainFrame window. 
     * @param contactItem The MetaContact for which the menu is opened.
     */
    public ContactRightButtonMenu(MainFrame mainFrame, 
                                MetaContact contactItem) {
        super();

        this.mainFrame = mainFrame;

        this.contactItem = contactItem;

        this.init();
    }

    /**
     * Initializes the menu, by adding all containing menu items.
     */
    private void init() {
        // This feature is disabled until it's implemented
        this.sendFileItem.setEnabled(false);
        
        /*
         * String[] userProtocols = contactItem.getProtocols();
         * 
         * for (int i = 0; i < userProtocols.length; i++) {
         * 
         * JMenuItem protocolMenuItem = new JMenuItem( userProtocols[i], new
         * ImageIcon(Constants.getProtocolIcon(userProtocols[i])));
         * 
         * this.addSubcontactMenu.add(protocolMenuItem); }
         */

        this.moveToMenu.setIcon(new ImageIcon(ImageLoader
                .getImage(ImageLoader.GROUPS_16x16_ICON)));

        this.addSubcontactMenu.setIcon(new ImageIcon(ImageLoader
                .getImage(ImageLoader.ADD_CONTACT_16x16_ICON)));

        this.add(sendMessageItem);
        this.add(sendFileItem);

        this.addSeparator();

        this.add(moveToMenu);

        this.addSeparator();

        this.add(addSubcontactMenu);

        this.addSeparator();

        this.add(removeContactItem);
        this.add(renameContactItem);

        this.addSeparator();

        this.add(viewHistoryItem);
        this.add(userInfoItem);

        this.sendMessageItem.setName("sendMessage");
        this.sendFileItem.setName("sendFile");
        this.moveToMenu.setName("moveToGroup");
        this.addSubcontactMenu.setName("addSubcontact");
        this.removeContactItem.setName("removeContact");
        this.renameContactItem.setName("renameContact");
        this.viewHistoryItem.setName("viewHistory");
        this.userInfoItem.setName("userInfo");

        this.sendMessageItem.addActionListener(this);
        this.sendFileItem.addActionListener(this);
        this.moveToMenu.addActionListener(this);
        this.addSubcontactMenu.addActionListener(this);
        this.removeContactItem.addActionListener(this);
        this.renameContactItem.addActionListener(this);
        this.viewHistoryItem.addActionListener(this);
        this.userInfoItem.addActionListener(this);

        // Disable all menu items that do nothing.
        this.sendFileItem.setEnabled(false);
        this.moveToMenu.setEnabled(false);
        this.addSubcontactMenu.setEnabled(false);
        this.removeContactItem.setEnabled(false);
        this.renameContactItem.setEnabled(false);
        this.viewHistoryItem.setEnabled(false);
        this.userInfoItem.setEnabled(false);
    }

    /**
     * Returns the related popup location where the menu will appear.
     * @return The related popup location where the menu will appear.
     */
    public Point getPopupLocation() {

        Component component = this.getInvoker();
        Point point = new Point();
        int x = component.getX();
        int y = component.getY();

        while (component.getParent() != null) {

            component = component.getParent();

            x += component.getX();
            y += component.getY();
        }

        point.x = x;
        point.y = y + this.getInvoker().getHeight();

        return point;
    }

    public void actionPerformed(ActionEvent e) {

        JMenuItem menuItem = (JMenuItem) e.getSource();
        String itemName = menuItem.getName();

        if (itemName.equalsIgnoreCase("sendMessage")) {
            ContactListPanel clistPanel = mainFrame.getTabbedPane()
                    .getContactListPanel();
            SwingUtilities.invokeLater(clistPanel.new RunMessageWindow(
                    contactItem));
        } 
        else if (itemName.equalsIgnoreCase("sendFile")) {
            // disabled
        } 
        else if (itemName.equalsIgnoreCase("removeContact")) {

            MessageDialog warning = new MessageDialog(this.mainFrame);

            String message = "<HTML>Are you sure you want to remove <B>"
                    + this.contactItem.getDisplayName()
                    + "</B><BR>from your contact list?</html>";

            warning.setMessage(message);

            warning.setVisible(true);
        } 
        else if (itemName.equalsIgnoreCase("renameContact")) {

        } 
        else if (itemName.equalsIgnoreCase("viewHistory")) {

            HistoryWindow history = new HistoryWindow();

            history.setContact(this.contactItem);
            history.setVisible(true);
        } 
        else if (itemName.equalsIgnoreCase("userInfo")) {

        }
    }
}
