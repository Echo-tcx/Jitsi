/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.chatroomslist;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.customcontrols.*;
import net.java.sip.communicator.impl.gui.main.chat.*;
import net.java.sip.communicator.impl.gui.main.chat.conference.*;
import net.java.sip.communicator.impl.gui.main.chatroomslist.joinforms.*;
import net.java.sip.communicator.impl.gui.utils.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.resources.*;
import net.java.sip.communicator.util.swing.*;

/**
 * The <tt>ChatRoomsListRightButtonMenu</tt> is the menu, opened when user clicks
 * with the right mouse button on the chat rooms list panel. It's the one that
 * contains the create chat room item.
 *
 * @author Yana Stamcheva
 * @author Lubomir Marinov
 */
public class ChatRoomRightButtonMenu
    extends SIPCommPopupMenu
    implements  ActionListener
{
    /**
     * The current chat room wrapper.
     */
    private ChatRoomWrapper chatRoomWrapper;

    /**
     * Creates an instance of <tt>ChatRoomsListRightButtonMenu</tt>.
     * @param chatRoomWrapper the chat room wrapper, corresponding to the
     * selected chat room
     */
    public ChatRoomRightButtonMenu(ChatRoomWrapper chatRoomWrapper)
    {
        this.chatRoomWrapper = chatRoomWrapper;

        this.setLocation(getLocation());

        createMenuItem(
            "service.gui.OPEN",
            ImageLoader.CHAT_ROOM_16x16_ICON,
            "openChatRoom");
        JMenuItem joinChatRoomItem
            = createMenuItem(
                "service.gui.JOIN",
                ImageLoader.JOIN_ICON,
                "joinChatRoom");
        JMenuItem joinAsChatRoomItem
            = createMenuItem(
                "service.gui.JOIN_AS",
                ImageLoader.JOIN_AS_ICON,
                "joinAsChatRoom");
        JMenuItem leaveChatRoomItem
            = createMenuItem(
                "service.gui.LEAVE",
                ImageLoader.LEAVE_ICON,
                "leaveChatRoom");
        createMenuItem(
            "service.gui.REMOVE",
            ImageLoader.DELETE_16x16_ICON,
            "removeChatRoom");
        JMenuItem nickNameChatRoomItem
        = createMenuItem(
            "service.gui.CHANGE_NICK",
            ImageLoader.LEAVE_ICON,
            "nickNameChatRoom");

        ChatRoom chatRoom = chatRoomWrapper.getChatRoom();

        if ((chatRoom != null) && chatRoom.isJoined())
        {
            joinAsChatRoomItem.setEnabled(false);
            joinChatRoomItem.setEnabled(false);
        }
        else
            leaveChatRoomItem.setEnabled(false);
    }

    /**
     * Handles the <tt>ActionEvent</tt>. Determines which menu item was
     * selected and makes the appropriate operations.
     * @param e the event.
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
            String nickName = null;

            nickName =
                ConfigurationManager.getChatRoomProperty(
                    chatRoomWrapper.getParentProvider()
                        .getProtocolProvider(), chatRoomWrapper
                        .getChatRoomID(), "userNickName");
            if(nickName == null)
                nickName = getNickname();

            if (nickName != null)
                conferenceManager.joinChatRoom(chatRoomWrapper, nickName, null);
            else
                conferenceManager.joinChatRoom(chatRoomWrapper);    
        }
        else if (itemName.equals("openChatRoom"))
        {
            if(chatRoomWrapper.getChatRoom() != null)
            {
                if(!chatRoomWrapper.getChatRoom().isJoined())
                {
                    String nickName = null;

                    nickName =
                        ConfigurationManager.getChatRoomProperty(
                            chatRoomWrapper.getParentProvider()
                                .getProtocolProvider(), chatRoomWrapper
                                .getChatRoomID(), "userNickName");
                    if(nickName == null)
                        nickName = getNickname();

                    if (nickName != null)
                        conferenceManager.joinChatRoom(chatRoomWrapper,
                            nickName, null);
                    else
                        conferenceManager.joinChatRoom(chatRoomWrapper);
                }
            }
            else
            {
                // this is not a server persistent room we must create it
                // and join
                chatRoomWrapper =
                    GuiActivator.getUIService().getConferenceChatManager()
                        .createChatRoom(
                            chatRoomWrapper.getChatRoomName(),
                            chatRoomWrapper.getParentProvider()
                                .getProtocolProvider(),
                            new ArrayList<String>(),
                            "",
                            false,
                            true);
                
                String nickName = null;
                
                nickName =
                    ConfigurationManager.getChatRoomProperty(
                        chatRoomWrapper.getParentProvider()
                            .getProtocolProvider(), chatRoomWrapper
                            .getChatRoomID(), "userNickName");
                
                if(nickName == null)
                    nickName = getNickname();

                if (nickName != null)
                    conferenceManager.joinChatRoom(chatRoomWrapper, nickName,
                        null);
                else
                    conferenceManager.joinChatRoom(chatRoomWrapper);
            }

            ChatWindowManager chatWindowManager
                = GuiActivator.getUIService().getChatWindowManager();
            ChatPanel chatPanel
                = chatWindowManager.getMultiChat(chatRoomWrapper, true);

            chatWindowManager.openChat(chatPanel, true);
        }
        else if(itemName.equals("joinAsChatRoom"))
        {
            ChatRoomAuthenticationWindow authWindow
                = new ChatRoomAuthenticationWindow(chatRoomWrapper);

            authWindow.setVisible(true);
        }
        else if(itemName.equals("nickNameChatRoom"))
        {
            String nickName = null;
            
            nickName =
                ConfigurationManager.getChatRoomProperty(
                    chatRoomWrapper.getParentProvider()
                        .getProtocolProvider(), chatRoomWrapper
                        .getChatRoomID(), "userNickName");
            
            ChatOperationReasonDialog reasonDialog =
                new ChatOperationReasonDialog(GuiActivator.getResources()
                    .getI18NString("service.gui.CHANGE_NICKNAME"), GuiActivator
                    .getResources().getI18NString(
                        "service.gui.CHANGE_NICKNAME_LABEL"));

            reasonDialog.setReasonFieldText(nickName == null ? chatRoomWrapper
                .getParentProvider().getProtocolProvider().getAccountID()
                .getUserID() : nickName);

            int result = reasonDialog.showDialog();

            if (result == MessageDialog.OK_RETURN_CODE)
            {
                nickName = reasonDialog.getReason().trim();
            }
            
            ConfigurationManager.updateChatRoomProperty(chatRoomWrapper
                .getParentProvider().getProtocolProvider(), chatRoomWrapper
                .getChatRoomID(), "userNickName", nickName);
            
        }
    }

    /**
     * Creates a new <tt>JMenuItem</tt> and adds it to this <tt>JPopupMenu</tt>.
     *
     * @param textKey the key of the internationalized string in the resources
     * of the application which represents the text of the new
     * <tt>JMenuItem</tt>
     * @param iconID the <tt>ImageID</tt> of the image in the resources of the
     * application which represents the icon of the new <tt>JMenuItem</tt>
     * @param name the name of the new <tt>JMenuItem</tt>
     * @return a new <tt>JMenuItem</tt> instance which has been added to this
     * <tt>JPopupMenu</tt>
     */
    private JMenuItem createMenuItem(
            String textKey,
            ImageID iconID,
            String name)
    {
        ResourceManagementService resources = GuiActivator.getResources();
        JMenuItem menuItem
            = new JMenuItem(
                    resources.getI18NString(textKey),
                    new ImageIcon(ImageLoader.getImage(iconID)));

        menuItem.setMnemonic(resources.getI18nMnemonic(textKey));
        menuItem.setName(name);

        menuItem.addActionListener(this);

        add(menuItem);

        return menuItem;
    }
    
    private String getNickname()
    {
        String nickName = null;
        ChatOperationReasonDialog reasonDialog =
            new ChatOperationReasonDialog(GuiActivator.getResources()
                .getI18NString("service.gui.CHANGE_NICKNAME"), GuiActivator
                .getResources().getI18NString(
                    "service.gui.CHANGE_NICKNAME_LABEL"));

        reasonDialog.setReasonFieldText("");

        int result = reasonDialog.showDialog();

        if (result == MessageDialog.OK_RETURN_CODE)
        {
            nickName = reasonDialog.getReason().trim();
        }

        return nickName;
    }
}
