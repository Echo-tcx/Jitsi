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
 * <tt>MessageReceivedEvent</tt>s indicate reception of an instant message.
 *
 * @author Emil Ivov
 */
public class ChatRoomMessageReceivedEvent
    extends EventObject
{
    /**
     * The chat room member that has sent this message.
     */
    private ChatRoomMember from = null;

    /**
     * A timestamp indicating the exact date when the event occurred.
     */
    private Date timestamp = null;

    /**
     * The received <tt>Message</tt>.
     */
    private Message message = null;

    /**
     * The type of message event that this instance represents.
     */
    private int eventType = -1;

    /**
     * An event type indicating that the message being received is a standard
     * conversation message sent by another member of the chatroom to all
     * current participants.
     */
    public static final int CONVERSATION_MESSAGE_RECEIVED = 1;

    /**
     * An event type indicating that the message being received is a special
     * message that sent by either another member or the server itself,
     * indicating that some kind of action (other than the delivery of a
     * conversation message) has occurred. Action messages are widely used
     * in IRC through the /action and /me commands
     */
    public static final int ACTION_MESSAGE_RECEIVED = 2;

    /**
     * An event type indicting that the message being received is a system
     * message being sent by the server or a system administrator, possibly
     * notifying us of something important such as ongoing maintenance
     * activities or server downtime.
     */
    public static final int SYSTEM_MESSAGE_RECEIVED = 3;

    /**
     * Creates a <tt>MessageReceivedEvent</tt> representing reception of the
     * <tt>source</tt> message received from the specified <tt>from</tt>
     * contact.
     *
     * @param source the <tt>ChatRoom</tt> for which the message is received.
     * @param from the <tt>ChatRoomMember</tt> that has sent this message.
     * @param timestamp the exact date when the event ocurred.
     * @param message the received <tt>Message</tt>.
     */
    public ChatRoomMessageReceivedEvent(ChatRoom source,
                                        ChatRoomMember from,
                                        Date timestamp,
                                        Message message)
    {
        this(source, from, timestamp, message, CONVERSATION_MESSAGE_RECEIVED);
    }

    /**
     * Creates a <tt>MessageReceivedEvent</tt> representing reception of the
     * <tt>source</tt> message received from the specified <tt>from</tt>
     * contact.
     *
     * @param source the <tt>ChatRoom</tt> for which the message is received.
     * @param from the <tt>ChatRoomMember</tt> that has sent this message.
     * @param timestamp the exact date when the event ocurred.
     * @param message the received <tt>Message</tt>.
     * @param eventType the type of message event that this instance represents
     * (one of the XXX_MESSAGE_RECEIVED static fields).
     */
    public ChatRoomMessageReceivedEvent(ChatRoom        source,
                                        ChatRoomMember  from,
                                        Date            timestamp,
                                        Message         message,
                                        int             eventType)
    {
        super(source);

        this.from = from;
        this.timestamp = timestamp;
        this.message = message;
        this.eventType = eventType;
    }

    /**
     * Returns a reference to the <tt>ChatRoomMember</tt> that has send the
     * <tt>Message</tt> whose reception this event represents.
     *
     * @return a reference to the <tt>ChatRoomMember</tt> that has send the
     * <tt>Message</tt> whose reception this event represents.
     */
    public ChatRoomMember getSourceChatRoomMember()
    {
        return from;
    }

    /**
     * Returns the received message.
     * @return the <tt>Message</tt> that triggered this event.
     */
    public Message getMessage()
    {
        return message;
    }

    /**
     * A timestamp indicating the exact date when the event ocurred.
     * @return a Date indicating when the event ocurred.
     */
    public Date getTimestamp()
    {
        return timestamp;
    }

    /**
     * Returns the <tt>ChatRoom</tt> that triggered this event.
     * @return the <tt>ChatRoom</tt> that triggered this event.
     */
    public ChatRoom getSourceChatRoom()
    {
        return (ChatRoom) getSource();
    }

    /**
     * Returns the type of message event represented by this event instance.
     * Message event types are generally one of the values of the
     * XXX_MESSAGE_RECEIVED fields of this class.
     * @return one of the values of the XXX_MESSAGE_RECEIVED fields of this
     * class indicating the type of message received event received.
     */
    public int getEventType()
    {
        return eventType;
    }
}
