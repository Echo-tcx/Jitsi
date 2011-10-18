/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.service.protocol;

import net.java.sip.communicator.service.protocol.event.*;

/**
 * The WhiteboardParticipant is an interface that represents participants in a
 * whiteboard.
 *
 * @author Julien Waechter
 * @author Emil Ivov
 */
public interface WhiteboardParticipant
{
    /**
     * Returns the chat room that this member is participating in.
     *
     * @return the <tt>WhiteboardSession</tt> instance that this member belongs to.
     */
    public WhiteboardSession getWhiteboardSession();

    /**
     * Returns the protocol provider instance that this member has originated
     * in.
     *
     * @return the <tt>ProtocolProviderService</tt> instance that created this
     * member and its containing cht room
     */
    public ProtocolProviderService getProtocolProvider();

    /**
     * Returns the contact identifier representing this contact.
     *
     * @return a String contact address
     */
    public String getContactAddress();

    /**
     * Returns the name of this member
     *
     * @return the name of this member in the room (nickname).
     */
    public String getName();

    /**
     * Returns an object representing the current state of that participant.
     * WhiteboardParticipantState may vary among CONNECTING, BUSY,
     * CONNECTED...
     * @return a WhiteboardParticipantState instance representin the participant's
     * state.
     */
    public WhiteboardParticipantState getState();

    /**
     * Allows the user interface to register a listener interested in changes
     * @param listener a listener instance to register with this participant.
     */
    public void addWhiteboardParticipantListener(
                                    WhiteboardParticipantListener listener);

    /**
     * Unregisters the specified listener.
     * @param listener the listener to unregister.
     */
    public void removeWhiteboardParticipantListener(
                                    WhiteboardParticipantListener listener);
}
