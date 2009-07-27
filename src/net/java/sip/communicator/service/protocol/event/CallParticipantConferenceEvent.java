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
 * Represents an event fired by a <code>CallParticipant</code> to notify
 * interested <code>CallParticipantConferenceListener</code>s about changes in
 * its conference-related information such as it acting or not acting as a
 * conference focus and conference membership details.
 * 
 * @author Lubomir Marinov
 */
public class CallParticipantConferenceEvent
    extends EventObject
{

    /**
     * The ID of <code>CallParticipantConferenceEvent</code> which notifies
     * about a change in the characteristic of a specific
     * <code>CallParticipant</code> being a conference focus. The event does not
     * carry information about a specific <code>ConferenceMember</code> i.e. the
     * <code>conferenceMember</code> property is of value <tt>null</tt>.
     */
    public static final int CONFERENCE_FOCUS_CHANGED = 1;

    /**
     * The ID of <code>CallParticipantConferenceEvent</code> which notifies
     * about an addition to the list of <code>ConferenceMember</code>s managed
     * by a specific <code>CallParticipant</code>. The
     * <code>conferenceMember</code> property specifies the
     * <code>ConferenceMember</code> which was added and thus caused the event
     * to be fired.
     */
    public static final int CONFERENCE_MEMBER_ADDED = 2;

    /**
     * The ID of <code>CallParticipantConferenceEvent</code> which notifies
     * about a removal from the list of <code>ConferenceMember</code>s managed
     * by a specific <code>CallParticipant</code>. The
     * <code>conferenceMember</code> property specifies the
     * <code>ConferenceMember</code> which was removed and thus caused the event
     * to be fired.
     */
    public static final int CONFERENCE_MEMBER_REMOVED = 3;

    /**
     * The <code>ConferenceMember</code> which has been changed (e.g. added to
     * or removed from the conference) if this event has been fired because of
     * such a change; otherwise, <tt>null</tt>.
     */
    private final ConferenceMember conferenceMember;

    /**
     * The ID of this event which may be one of
     * {@link #CONFERENCE_FOCUS_CHANGED}, {@link #CONFERENCE_MEMBER_ADDED} and
     * {@link #CONFERENCE_MEMBER_REMOVED} and indicates the specifics of the
     * change in the conference-related information and the details this event
     * carries.
     */
    private final int eventID;

    /**
     * Initializes a new <code>CallParticipantConferenceEvent</code> which is to
     * be fired by a specific <code>CallParticipant</code> and which notifies
     * about a change in its conference-related information not including a
     * change pertaining to a specific <code>ConferenceMember</code>.
     * 
     * @param source
     *            the <code>CallParticipant</code> which is to fire the new
     *            event
     * @param eventID
     *            the ID of this event which may be
     *            {@link #CONFERENCE_FOCUS_CHANGED} and indicates the specifics
     *            of the change in the conference-related information and the
     *            details this event carries
     */
    public CallParticipantConferenceEvent(CallParticipant source, int eventID)
    {
        this(source, eventID, null);
    }

    /**
     * Initializes a new <code>CallParticipantConferenceEvent</code> which is to
     * be fired by a specific <code>CallParticipant</code> and which notifies
     * about a change in its conference-related information pertaining to a
     * specific <code>ConferenceMember</code>.
     * 
     * @param source
     *            the <code>CallParticipant</code> which is to fire the new
     *            event
     * @param eventID
     *            the ID of this event which may be
     *            {@link #CONFERENCE_MEMBER_ADDED} and
     *            {@link #CONFERENCE_MEMBER_REMOVED} and indicates the specifics
     *            of the change in the conference-related information and the
     *            details this event carries
     * @param conferenceMember
     *            the <code>ConferenceMember</code> which caused the new event
     *            to be fired
     */
    public CallParticipantConferenceEvent(
        CallParticipant source,
        int eventID,
        ConferenceMember conferenceMember)
    {
        super(source);

        this.eventID = eventID;
        this.conferenceMember = conferenceMember;
    }

    /**
     * Gets the <code>ConferenceMember</code> which has been changed (e.g. added
     * to or removed from the conference) if this event has been fired because
     * of such a change.
     * 
     * @return the <code>ConferenceMember</code> which has been changed if this
     *         event has been fired because of such a change; otherwise,
     *         <tt>null</tt>
     */
    public ConferenceMember getConferenceMember()
    {
        return conferenceMember;
    }

    /**
     * Gets the ID of this event which may be one of
     * {@link #CONFERENCE_FOCUS_CHANGED}, {@link #CONFERENCE_MEMBER_ADDED} and
     * {@link #CONFERENCE_MEMBER_REMOVED} and indicates the specifics of the
     * change in the conference-related information and the details this event
     * carries.
     * 
     * @return the ID of this event which may be one of
     *         {@link #CONFERENCE_FOCUS_CHANGED},
     *         {@link #CONFERENCE_MEMBER_ADDED} and
     *         {@link #CONFERENCE_MEMBER_REMOVED} and indicates the specifics of
     *         the change in the conference-related information and the details
     *         this event carries
     */
    public int getEventID()
    {
        return eventID;
    }

    /**
     * Gets the <code>CallParticipant</code> which is the source of/fired the
     * event.
     * 
     * @return the <code>CallParticipant</code> which is the source of/fired the
     *         event
     */
    public CallParticipant getSourceCallParticipant()
    {
        return (CallParticipant) getSource();
    }
}
