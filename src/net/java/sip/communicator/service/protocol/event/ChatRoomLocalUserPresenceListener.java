/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.service.protocol.event;

import java.util.*;

/**
 * A listener that will be notified of changes in our presence in the chat
 * room such as us being kicked, join, left.
 * 
 * @author Emil Ivov
 */
public interface ChatRoomLocalUserPresenceListener
    extends EventListener
{
    /**
     * Called to notify interested parties that a change in our presence in the
     * source chat room has occured. Changes may include us being kicked, join,
     * left.
     * @param evt the <tt>ChatRoomLocalUserPresenceChangeEvent</tt> instance
     * containing the source chat room and the type, and reason of the change
     */
    public void localUserPresenceChanged(ChatRoomLocalUserPresenceChangeEvent evt);
}
