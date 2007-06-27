/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.mock;

import java.util.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;

/**
 * Multiuser chat functionalites for the mock protocol.
 * @author Damian Minkov
 */
public class MockMultiUserChat
    implements OperationSetMultiUserChat
{
    /**
     * The protocol provider that created us.
     */
    private MockProvider provider = null;
    
    private List existingChatRooms = new Vector();
    private List joinedChatRooms = new Vector();
    
    /**
     * Currently registered invitation listeners.
     */
    private Vector invitationListeners = new Vector();
    
    /**
     * Currently registered invitation reject listeners.
     */
    private Vector invitationRejectListeners = new Vector();
    
    /**
     * Currently registered local user chat room presence listeners.
     */
    private Vector localUserChatRoomPresenceListeners = new Vector();
    
    /**
     * Creates an instance of this operation set keeping a reference to the
     * parent protocol provider and presence operation set.
     *
     * @param provider The provider instance that creates us.
     */
    public MockMultiUserChat(MockProvider provider)
    {
        this.provider = provider;
    }
    
    /**
     * Returns the <tt>List</tt> of <tt>String</tt>s indicating chat rooms
     * currently available on the server that this protocol provider is
     * connected to.
     *
     * @return a <tt>java.util.List</tt> of the name <tt>String</tt>s for chat
     * rooms that are currently available on the server that this protocol
     * provider is connected to.
     *
     * @throws OperationFailedException if we faile retrieving this list from
     * the server.
     * @throws OperationNotSupportedException if the server does not support
     * multi user chat
     */
    public List getExistingChatRooms() 
        throws OperationFailedException, 
               OperationNotSupportedException
    {
        return existingChatRooms;
    }
    
    /**
     * Returns a list of the chat rooms that we have joined and are currently
     * active in.
     *
     * @return a <tt>List</tt> of the rooms where the user has joined using a
     * given connection.
     */
    public List getCurrentlyJoinedChatRooms()
    {
        return joinedChatRooms;
    }
    
    /**
     * Returns a list of the chat rooms that <tt>chatRoomMember</tt> has joined
     * and is currently active in.
     *
     * @param chatRoomMember the chatRoomMember whose current ChatRooms we will
     * be querying.
     * @return a list of the chat rooms that <tt>chatRoomMember</tt> has
     * joined and is currently active in.
     *
     * @throws OperationFailedException if an error occurs while trying to
     * discover the room on the server.
     * @throws OperationNotSupportedException if the server does not support
     * multi user chat
     */
    public List getCurrentlyJoinedChatRooms(ChatRoomMember chatRoomMember) 
        throws OperationFailedException, 
               OperationNotSupportedException
    {
        List result = new Vector();
        
        Iterator iter = joinedChatRooms.iterator();
        
        while(iter.hasNext())
        {
            ChatRoom elem = (ChatRoom)iter.next();
            if(elem.getMembers().contains(chatRoomMember))
                result.add(elem);
        }

        return result;
    }
    
    /**
     * Creates a room with the named <tt>roomName</tt> and according to the
     * specified <tt>roomProperties</tt> on the server that this protocol
     * provider is currently connected to. When the method returns the room the
     * local user will not have joined it and thus will not receive messages on
     * it until the <tt>ChatRoom.join()</tt> method is called.
     * <p>
     * @param roomName the name of the <tt>ChatRoom</tt> to create.
     * @param roomProperties properties specifying how the room should be
     * created.
     * @throws OperationFailedException if the room couldn't be created for some
     * reason (e.g. room already exists; user already joined to an existant
     * room or user has no permissions to create a chat room).
     * @throws OperationNotSupportedException if chat room creation is not
     * supported by this server
     *
     * @return the newly created <tt>ChatRoom</tt> named <tt>roomName</tt>.
     */
    public ChatRoom createChatRoom(String roomName, Hashtable roomProperties) 
        throws OperationFailedException, 
               OperationNotSupportedException
    {
        MockChatRoom room = new MockChatRoom(provider, this, roomName);
        existingChatRooms.add(room);
        return room;
    }
    
    /**
     * Returns a reference to a chatRoom named <tt>roomName</tt> or null if no
     * such room exists.
     * <p>
     * @param roomName the name of the <tt>ChatRoom</tt> that we're looking for.
     * @return the <tt>ChatRoom</tt> named <tt>roomName</tt> or null if no such
     * room exists on the server that this provider is currently connected to.
     *
     * @throws OperationFailedException if an error occurs while trying to
     * discover the room on the server.
     * @throws OperationNotSupportedException if the server does not support
     * multi user chat
     */
    public ChatRoom findRoom(String roomName) 
        throws OperationFailedException, 
               OperationNotSupportedException
    {
        Iterator iter = existingChatRooms.iterator();
        while(iter.hasNext())
        {
            ChatRoom elem =  (ChatRoom)iter.next();
            if(elem.getName().equals(roomName))
                return elem;
        }
        
        return null;
    }
    
    /**
     * Informs the sender of an invitation that we decline their invitation.
     *
     * @param invitation the invitation we are rejecting.
     */
    public void rejectInvitation(ChatRoomInvitation invitation)
    {
        ChatRoomInvitationRejectedEvent evt = 
            new ChatRoomInvitationRejectedEvent(
                    invitation,
                    null,
                    null,
                    invitation.getReason(),
                    new Date());
        
        Iterator iter = invitationRejectListeners.iterator();
        while(iter.hasNext())
        {
            ChatRoomInvitationRejectionListener elem =  
                (ChatRoomInvitationRejectionListener)iter.next();
            elem.invitationRejected(evt);
        }
    }
    
    /**
     * Adds a listener to invitation notifications. The listener will be fired
     * anytime an invitation is received.
     *
     * @param listener an invitation listener.
     */
    public void addInvitationListener(ChatRoomInvitationListener listener)
    {
        if(!invitationListeners.contains(listener))
            invitationListeners.add(listener);
    }
    
    /**
     * Removes <tt>listener</tt> from the list of invitation listeners
     * registered to receive invitation events.
     *
     * @param listener the invitation listener to remove.
     */
    public void removeInvitationListener(ChatRoomInvitationListener listener)
    {
        invitationListeners.remove(listener);
    }
    
    /**
     * Adds a listener to invitation notifications. The listener will be fired
     * anytime an invitation is received.
     *
     * @param listener an invitation listener.
     */
    public void addInvitationRejectionListener(
        ChatRoomInvitationRejectionListener listener)
    {
        if(!invitationRejectListeners.contains(listener))
            invitationRejectListeners.add(listener);
    }
    
    /**
     * Removes the given listener from the list of invitation listeners
     * registered to receive events every time an invitation has been rejected.
     *
     * @param listener the invitation listener to remove.
     */
    public void removeInvitationRejectionListener(
        ChatRoomInvitationRejectionListener listener)
    {
        invitationRejectListeners.remove(listener);
    }
    
    /**
     * Returns true if <tt>contact</tt> supports multi user chat sessions.
     *
     * @param contact reference to the contact whose support for chat rooms
     * we are currently querying.
     * @return a boolean indicating whether <tt>contact</tt> supports chatrooms.
     */
    public boolean isMultiChatSupportedByContact(Contact contact)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Adds a listener that will be notified of changes in our participation in
     * a chat room such as us being kicked, joined, left.
     *
     * @param listener a local user participation listener.
     */
    public void addPresenceListener(
        LocalUserChatRoomPresenceListener listener)
    {
        if(!localUserChatRoomPresenceListeners.contains(listener))
            localUserChatRoomPresenceListeners.add(listener);
    }

    /**
     * Removes a listener that was being notified of changes in our
     * participation in a room such as us being kicked, joined, left.
     * 
     * @param listener a local user participation listener.
     */
    public void removePresenceListener(
        LocalUserChatRoomPresenceListener listener)
    {
        localUserChatRoomPresenceListeners.remove(listener);
    }
    
    void fireLocalUserChatRoomPresenceChangeEvent(
        LocalUserChatRoomPresenceChangeEvent evt)
    {
        Iterator iter = localUserChatRoomPresenceListeners.iterator();
        while(iter.hasNext())
        {
            LocalUserChatRoomPresenceListener elem =  
                (LocalUserChatRoomPresenceListener)iter.next();
            elem.localUserPresenceChanged(evt);
        }
    }
}
