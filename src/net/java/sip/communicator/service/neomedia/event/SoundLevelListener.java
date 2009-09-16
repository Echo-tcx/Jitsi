/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.service.neomedia.event;

import java.util.*;

/**
 * This listener allows interested parties to register for events notifying them
 * that the sound level for a conference participant currently being mixed by
 * our interlocutor has changed.
 *
 * @author Emil Ivov
 */
public interface SoundLevelListener
    extends EventListener
{

    /**
     * Delivers <tt>SoundLevelChangeEvent</tt>s to the implementing class. These
     * events may be delivered if the remote party is a mixer which supports
     * the RTP sound levels extension.
     *
     * @param evt the notification event containing the list of changes.
     */
    public void soundLevelChanged(SoundLevelChangeEvent evt);
}
