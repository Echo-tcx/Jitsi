/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package net.java.sip.communicator.impl.gui.main.chatroomslist;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.i18n.*;
import net.java.sip.communicator.impl.gui.main.chat.conference.*;
import net.java.sip.communicator.impl.gui.main.chatroomslist.joinforms.*;
import net.java.sip.communicator.impl.gui.utils.*;
import net.java.sip.communicator.util.*;

/**
 * The <tt>ChatRoomsListRightButtonMenu</tt> is the menu, opened when user clicks
 * with the right mouse button on the chat rooms list panel. It's the one that
 * contains the create chat room item.
 *
 * @author Yana Stamcheva
 */
public class ChatRoomRightButtonMenu
    extends JPopupMenu
    implements  ActionListener
{
    private Logger logger = Logger.getLogger(ChatRoomRightButtonMenu.class);
    
    private I18NString joinChatRoomString
        = Messages.getI18NString("join");
    
    private I18NString joinAsChatRoomString
        = Messages.getI18NString("joinAs");

    private I18NString leaveChatRoomString
        = Messages.getI18NString("leave");

    private I18NString removeChatRoomString
        = Messages.getI18NString("remove");

    private JMenuItem leaveChatRoomItem = new JMenuItem(
        leaveChatRoomString.getText(),
        new ImageIcon(ImageLoader.getImage(ImageLoader.LEAVE_ICON)));

    private JMenuItem joinChatRoomItem = new JMenuItem(
        joinChatRoomString.getText(),
        new ImageIcon(ImageLoader.getImage(ImageLoader.JOIN_ICON)));

    private JMenuItem joinAsChatRoomItem = new JMenuItem(
        joinAsChatRoomString.getText(),
        new ImageIcon(ImageLoader.getImage(ImageLoader.JOIN_AS_ICON)));

    private JMenuItem removeChatRoomItem = new JMenuItem(
        removeChatRoomString.getText(),
        new ImageIcon(ImageLoader.getImage(ImageLoader.DELETE_16x16_ICON)));

    private ChatRoomWrapper chatRoomWrapper = null;

    /**
     * Creates an instance of <tt>ChatRoomsListRightButtonMenu</tt>.
     * @param mainFrame the main application window
     * @param chatRoomWrapper the chat room wrapper, corresponding to the
     * selected chat room
     */
    public ChatRoomRightButtonMenu(ChatRoomWrapper chatRoomWrapper)
    {
        super();

        this.chatRoomWrapper = chatRoomWrapper;

        this.setLocation(getLocation());

        this.init();
    }

    /**
     * Initializes the menu, by adding all containing menu items.
     */
    private void init()
    {
        this.add(joinChatRoomItem);
        this.add(joinAsChatRoomItem);
        this.add(leaveChatRoomItem);
        this.add(removeChatRoomItem);

        this.joinChatRoomItem.setName("joinChatRoom");
        this.joinAsChatRoomItem.setName("joinAsChatRoom");
        this.leaveChatRoomItem.setName("leaveChatRoom");
        this.removeChatRoomItem.setName("removeChatRoom");

        this.joinChatRoomItem
            .setMnemonic(joinChatRoomString.getMnemonic());

        this.joinAsChatRoomItem
            .setMnemonic(joinAsChatRoomString.getMnemonic());

        this.leaveChatRoomItem
            .setMnemonic(leaveChatRoomString.getMnemonic());

        this.removeChatRoomItem
            .setMnemonic(removeChatRoomString.getMnemonic());

        this.joinChatRoomItem.addActionListener(this);
        this.joinAsChatRoomItem.addActionListener(this);
        this.leaveChatRoomItem.addActionListener(this);
        this.removeChatRoomItem.addActionListener(this);

        if (chatRoomWrapper.getChatRoom() != null
            && chatRoomWrapper.getChatRoom().isJoined())
        {
            this.joinAsChatRoomItem.setEnabled(false);
            this.joinChatRoomItem.setEnabled(false);
        }
        else
            this.leaveChatRoomItem.setEnabled(false);
    }

    /**
     * Handles the <tt>ActionEvent</tt>. Determines which menu item was
     * selected and makes the appropriate operations.
     */
    public void actionPerformed(ActionEvent e)
    {
        JMenuItem menuItem = (JMenuItem) e.getSource();
        String itemName = menuItem.getName();

        ConferenceChatManager conferenceManager
            = GuiActivator.getUIService().getConferenceChatManager();

        if (itemName.equals("removeChatRoom"))
        {
            conferenceManager.removeChatRoom(chatRoomWrapper);

        }
        else if (itemName.equals("leaveChatRoom"))
        {
            conferenceManager.leaveChatRoom(chatRoomWrapper);
        }
        else if (itemName.equals("joinChatRoom"))
        {
            conferenceManager.joinChatRoom(chatRoomWrapper);
        }
        else if(itemName.equals("joinAsChatRoom"))
        {
            ChatRoomAuthenticationWindow authWindow
                = new ChatRoomAuthenticationWindow(chatRoomWrapper);

            authWindow.setVisible(true);
        }
    }
}
