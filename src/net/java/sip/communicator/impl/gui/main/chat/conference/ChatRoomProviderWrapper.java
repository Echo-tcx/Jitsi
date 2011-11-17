/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.chat.conference;

import java.util.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.utils.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;

/**
 * @author Yana Stamcheva
 * @author Damian Minkov
 */
public class ChatRoomProviderWrapper
{
    private static final Logger logger
        = Logger.getLogger(ChatRoomProviderWrapper.class);

    private final ProtocolProviderService protocolProvider;

    private final ChatRoomWrapper systemRoomWrapper;

    private final List<ChatRoomWrapper> chatRoomsOrderedCopy
        = new LinkedList<ChatRoomWrapper>();

    /**
     * Creates an instance of <tt>ChatRoomProviderWrapper</tt> by specifying
     * the protocol provider, corresponding to the multi user chat account.
     * 
     * @param protocolProvider protocol provider, corresponding to the multi
     * user chat account.
     */
    public ChatRoomProviderWrapper(
        ProtocolProviderService protocolProvider)
    {
        this.protocolProvider = protocolProvider;

        String accountIdService = protocolProvider.getAccountID().getService();
        this.systemRoomWrapper
            = new ChatRoomWrapper(this, accountIdService, accountIdService);
    }

    /**
     * Returns the name of this chat room provider.
     * @return the name of this chat room provider.
     */
    public String getName()
    {
        return protocolProvider.getProtocolDisplayName();
    }

    public byte[] getIcon()
    {
        return protocolProvider.getProtocolIcon()
            .getIcon(ProtocolIcon.ICON_SIZE_64x64);
    }

    public byte[] getImage()
    {
        byte[] logoImage = null;
        ProtocolIcon protocolIcon = protocolProvider.getProtocolIcon();

        if(protocolIcon.isSizeSupported(ProtocolIcon.ICON_SIZE_64x64))
            logoImage = protocolIcon.getIcon(ProtocolIcon.ICON_SIZE_64x64);
        else if(protocolIcon.isSizeSupported(ProtocolIcon.ICON_SIZE_48x48))
            logoImage = protocolIcon.getIcon(ProtocolIcon.ICON_SIZE_48x48);

        return logoImage;
    }

    /**
     * Returns the system room wrapper corresponding to this server.
     * 
     * @return the system room wrapper corresponding to this server.
     */
    public ChatRoomWrapper getSystemRoomWrapper()
    {
        return systemRoomWrapper;
    }

    /**
     * Sets the system room corresponding to this server.
     * 
     * @param systemRoom the system room to set
     */
    public void setSystemRoom(ChatRoom systemRoom)
    {
        systemRoomWrapper.setChatRoom(systemRoom);
    }

    /**
     * Returns the protocol provider service corresponding to this server
     * wrapper.
     * 
     * @return the protocol provider service corresponding to this server
     * wrapper.
     */
    public ProtocolProviderService getProtocolProvider()
    {
        return protocolProvider;
    }

    /**
     * Adds the given chat room to this chat room provider.
     * 
     * @param chatRoom the chat room to add.
     */
    public void addChatRoom(ChatRoomWrapper chatRoom)
    {
        this.chatRoomsOrderedCopy.add(chatRoom);
    }

    /**
     * Removes the given chat room from this provider.
     * 
     * @param chatRoom the chat room to remove.
     */
    public void removeChatRoom(ChatRoomWrapper chatRoom)
    {
        this.chatRoomsOrderedCopy.remove(chatRoom);
    }

    /**
     * Returns <code>true</code> if the given chat room is contained in this
     * provider, otherwise - returns <code>false</code>.
     * 
     * @param chatRoom the chat room to search for.
     * @return <code>true</code> if the given chat room is contained in this
     * provider, otherwise - returns <code>false</code>.
     */
    public boolean containsChatRoom(ChatRoomWrapper chatRoom)
    {
        synchronized (chatRoomsOrderedCopy)
        {
            return chatRoomsOrderedCopy.contains(chatRoom);
        }
    }

    /**
     * Returns the chat room wrapper contained in this provider that corresponds
     * to the given chat room.
     * 
     * @param chatRoom the chat room we're looking for.
     * @return the chat room wrapper contained in this provider that corresponds
     * to the given chat room.
     */
    public ChatRoomWrapper findChatRoomWrapperForChatRoom(ChatRoom chatRoom)
    {
        // Compare ids, cause saved chatrooms don't have ChatRoom object
        // but Id's are the same.
        for (ChatRoomWrapper chatRoomWrapper : chatRoomsOrderedCopy)
        {
            if (chatRoomWrapper.getChatRoomID()
                    .equals(chatRoom.getIdentifier()))
            {
                return chatRoomWrapper;
            }
        }

        return null;
    }

    /**
     * Returns the number of chat rooms contained in this provider.
     * 
     * @return the number of chat rooms contained in this provider.
     */
    public int countChatRooms()
    {
        return chatRoomsOrderedCopy.size();
    }

    public ChatRoomWrapper getChatRoom(int index)
    {
        return chatRoomsOrderedCopy.get(index);
    }

    /**
     * Returns the index of the given chat room in this provider.
     * 
     * @param chatRoomWrapper the chat room to search for.
     * 
     * @return the index of the given chat room in this provider.
     */
    public int indexOf(ChatRoomWrapper chatRoomWrapper)
    {
        return chatRoomsOrderedCopy.indexOf(chatRoomWrapper);
    }

    /**
     * Goes through the locally stored chat rooms list and for each
     * {@link ChatRoomWrapper} tries to find the corresponding server stored
     * {@link ChatRoom} in the specified operation set. Joins automatically all
     * found chat rooms.
     */
    public void synchronizeProvider()
    {
        final OperationSetMultiUserChat groupChatOpSet
            = protocolProvider.getOperationSet(OperationSetMultiUserChat.class);

        for(final ChatRoomWrapper chatRoomWrapper : chatRoomsOrderedCopy)
        {
            new Thread()
            {
                public void run()
                {
                    ChatRoom chatRoom = null;

                    try
                    {
                        chatRoom = groupChatOpSet.findRoom(
                                    chatRoomWrapper.getChatRoomName());
                    }
                    catch (OperationFailedException e1)
                    {
                        logger.error("Failed to find chat room with name:"
                            + chatRoomWrapper.getChatRoomName(), e1);
                    }
                    catch (OperationNotSupportedException e1)
                    {                        
                        logger.error("Failed to find chat room with name:"
                            + chatRoomWrapper.getChatRoomName(), e1);
                    }

                    if(chatRoom != null)
                    {
                        chatRoomWrapper.setChatRoom(chatRoom);

                        /*String lastChatRoomStatus
                            = ConfigurationManager.getChatRoomStatus(
                                protocolProvider,
                                chatRoomWrapper.getChatRoomID());
                        if((lastChatRoomStatus == null
                            || lastChatRoomStatus.equals(
                                Constants.ONLINE_STATUS))
                        */

                        if(chatRoomWrapper.isAutojoin())
                        {
                            String nickName =
                                ConfigurationManager.getChatRoomProperty(
                                    chatRoomWrapper.getParentProvider()
                                        .getProtocolProvider(), chatRoomWrapper
                                        .getChatRoomID(), "userNickName");

                            if (nickName != null)
                            {
                                GuiActivator.getUIService()
                                    .getConferenceChatManager()
                                    .joinChatRoom(chatRoom, nickName, null);
                            }
                            else
                            {
                                GuiActivator.getUIService()
                                    .getConferenceChatManager()
                                    .joinChatRoom(chatRoom);
                            }
                        }
                    }
                    else
                    {
                        if(chatRoomWrapper.isAutojoin())
                        {
                            // chat room is not existent we must create it and join
                            // it
                            ChatRoomWrapper roomWrapper =
                                GuiActivator
                                    .getUIService()
                                    .getConferenceChatManager()
                                    .createChatRoom(
                                        chatRoomWrapper.getChatRoomName(),
                                        chatRoomWrapper.getParentProvider()
                                            .getProtocolProvider(),
                                        new ArrayList<String>(), "", false,
                                        true);

                            String nickName =
                                ConfigurationManager.getChatRoomProperty(
                                    chatRoomWrapper.getParentProvider()
                                        .getProtocolProvider(), chatRoomWrapper
                                        .getChatRoomID(), "userNickName");

                            if (nickName != null)
                            {
                                GuiActivator
                                    .getUIService()
                                    .getConferenceChatManager()
                                    .joinChatRoom(roomWrapper.getChatRoom(),
                                        nickName, null);
                            }
                            else
                            {
                                GuiActivator.getUIService()
                                    .getConferenceChatManager()
                                    .joinChatRoom(roomWrapper);
                            }
                        }
                    }
                }
            }.start();
        }
    }
}
