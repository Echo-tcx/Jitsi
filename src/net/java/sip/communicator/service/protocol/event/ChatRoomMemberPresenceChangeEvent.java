/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.service.protocol.event;

import java.util.*;
import net.java.sip.communicator.service.protocol.*;

/**
 * Dispatched to notify interested parties that a change in the presence of a
 * chat room member has occurred. Changes may include the participant
 * being kicked, join, left...
 *
 * @author Emil Ivov
 */
public class ChatRoomMemberPresenceChangeEvent
    extends EventObject
{
    /**
     * Indicates that this event was triggered as a result of the participant
     * joining the source chat room.
     */
    public static final String MEMBER_JOINED = "MemberJoined";

    
    /**
     * Indicates that this event was triggered as a result of the participant
     * leaving the source chat room.
     */
    public static final String MEMBER_LEFT = "MemberLeft";

    /**
     * Indicates that this event was triggered as a result of the participant
     * being "kicked" out of the chat room.
     */
    public static final String MEMBER_KICKED = "MemberKicked";
    
    /**
     * Indicates that this event was triggered as a result of the participant
     * being disconnected from the server brutally, or due to a ping timeout.
     */
     public static final String MEMBER_QUIT = "MemberQuit";

    /**
     * The chat room member that the event relates to.
     */
    private ChatRoomMember sourceMember = null;

    /**
     * The type of this event. Values can be any of the MEMBER_XXX fields.
     */
    private String eventType = null;

    /**
     * An optional String indicating a possible reason as to why the event
     * might have occurred.
     */
    private String reason = null;

    /**
     * Creates a <tt>ChatRoomMemberPresenceChangeEvent</tt> representing that
     * a change in the presence of a <tt>ChatRoomMember</tt> has occurred.
     * Changes may include the participant being kicked, join, left, etc.
     * 
     * @param sourceRoom the <tt>ChatRoom</tt> that produced this event
     * @param sourceMember the <tt>ChatRoomMember</tt> that this event is about 
     * @param eventType the event type; one of the MEMBER_XXX constants
     * @param reason the reason explaining why this event might have occurred
     */
    public ChatRoomMemberPresenceChangeEvent(   ChatRoom       sourceRoom,
                                                ChatRoomMember sourceMember,
                                                String         eventType,
                                                String         reason )
    {
        super(sourceRoom);
        this.sourceMember = sourceMember;
        this.eventType = eventType;
        this.reason = reason;
    }

    /**
     * Returns the chat room that produced this event.
     *
     * @return the <tt>ChatRoom</tt> that produced this event
     */
    public ChatRoom getChatRoom()
    {
        return (ChatRoom)getSource();
    }

    /**
     * Returns the chat room member that this event is about.
     * 
     * @return the <tt>ChatRoomMember</tt> that this event is about.
     */
    public ChatRoomMember getChatRoomMember()
    {
        return sourceMember;
    }

    /**
     * A reason String indicating a human readable reason for this event.
     *
     * @return a human readable String containing the reason for this event,
     * or null if no particular reason was specified.
     */
    public String getReason()
    {
        return reason;
    }

    /**
     * Returns the type of this event which could be one of the MEMBER_XXX
     * member field values.
     *
     * @return one of the MEMBER_XXX member field values indicating the type
     * of this event.
     */
    public String getEventType()
    {
        return eventType;
    }

    /**
     * Returns a String representation of this event.
     */
    public String toString()
    {
        return "ChatRoomMemberPresenceChangeEvent[type="
            + getEventType()
            + " sourceRoom="
            + getChatRoom().toString()
            + " member="
            + getChatRoomMember().toString()
            + "]";
    }
}
