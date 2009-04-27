/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.sip;

import java.util.*;
import javax.sip.*;

import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.service.media.*;

/**
 * A SIP implementation of the Call abstract class encapsulating SIP dialogs.
 *
 * @author Emil Ivov
 */
public class CallSipImpl
    extends Call
    implements CallParticipantListener
{
    private static final Logger logger = Logger.getLogger(CallSipImpl.class);

    /**
     * A list containing all <tt>CallParticipant</tt>s of this call.
     */
    private Vector<CallParticipantSipImpl> callParticipants =
        new Vector<CallParticipantSipImpl>();

    /**
     * The <tt>CallSession</tt> that the media service has created for this
     * call.
     */
    private CallSession mediaCallSession = null;

    /**
     * Crates a CallSipImpl instance belonging to <tt>sourceProvider</tt> and
     * initiated by <tt>CallCreator</tt>.
     *
     * @param sourceProvider the ProtocolProviderServiceSipImpl instance in the
     *            context of which this call has been created.
     */
    protected CallSipImpl(ProtocolProviderServiceSipImpl sourceProvider)
    {
        super(sourceProvider);
    }

    /**
     * Adds <tt>callParticipant</tt> to the list of participants in this call.
     * If the call participant is already included in the call, the method has
     * no effect.
     *
     * @param callParticipant the new <tt>CallParticipant</tt>
     */
    public void addCallParticipant(CallParticipantSipImpl callParticipant)
    {
        if (callParticipants.contains(callParticipant))
            return;

        callParticipant.addCallParticipantListener(this);

        this.callParticipants.add(callParticipant);
        fireCallParticipantEvent(callParticipant,
            CallParticipantEvent.CALL_PARTICIPANT_ADDED);
    }

    /**
     * Removes <tt>callParticipant</tt> from the list of participants in this
     * call. The method has no effect if there was no such participant in the
     * call.
     *
     * @param callParticipant the <tt>CallParticipant</tt> leaving the call;
     */
    public void removeCallParticipant(CallParticipantSipImpl callParticipant)
    {
        if (!callParticipants.contains(callParticipant))
            return;

        this.callParticipants.remove(callParticipant);
        callParticipant.removeCallParticipantListener(this);

        try
        {
            fireCallParticipantEvent(callParticipant,
                CallParticipantEvent.CALL_PARTICIPANT_REMVOVED);
        }
        finally
        {

            /*
             * The participant should loose its state once it has finished
             * firing its events in order to allow the listeners to undo.
             */
            callParticipant.setCall(null);
        }

        if (callParticipants.size() == 0)
            setCallState(CallState.CALL_ENDED);
    }

    /**
     * Returns an iterator over all call participants.
     *
     * @return an Iterator over all participants currently involved in the call.
     */
    public Iterator<CallParticipant> getCallParticipants()
    {
        return new LinkedList<CallParticipant>(callParticipants).iterator();
    }

    /**
     * Returns the number of participants currently associated with this call.
     *
     * @return an <tt>int</tt> indicating the number of participants currently
     *         associated with this call.
     */
    public int getCallParticipantsCount()
    {
        return callParticipants.size();
    }

    /**
     * Dummy implementation of a method (inherited from CallParticipantListener)
     * that we don't need.
     *
     * @param evt unused.
     */
    public void participantImageChanged(CallParticipantChangeEvent evt)
    {
    }

    /**
     * Dummy implementation of a method (inherited from CallParticipantListener)
     * that we don't need.
     *
     * @param evt unused.
     */
    public void participantAddressChanged(CallParticipantChangeEvent evt)
    {
    }

    /**
     * Dummy implementation of a method (inherited from CallParticipantListener)
     * that we don't need.
     *
     * @param evt unused.
     */
    public void participantTransportAddressChanged(
        CallParticipantChangeEvent evt)
    {
    }

    /**
     * Dummy implementation of a method (inherited from CallParticipantListener)
     * that we don't need.
     *
     * @param evt unused.
     */
    public void participantDisplayNameChanged(CallParticipantChangeEvent evt)
    {
    }

    /**
     * Verifies whether the call participant has entered a state.
     *
     * @param evt The <tt>CallParticipantChangeEvent</tt> instance containing
     *            the source event as well as its previous and its new status.
     */
    public void participantStateChanged(CallParticipantChangeEvent evt)
    {
        CallParticipantState newState =
            (CallParticipantState) evt.getNewValue();
        if (newState == CallParticipantState.DISCONNECTED
            || newState == CallParticipantState.FAILED)
        {
            removeCallParticipant((CallParticipantSipImpl) evt
                .getSourceCallParticipant());
        }
        else if ((newState == CallParticipantState.CONNECTED
               || newState == CallParticipantState.CONNECTING_WITH_EARLY_MEDIA))
        {
            setCallState(CallState.CALL_IN_PROGRESS);
        }
    }

    /**
     * Returns <tt>true</tt> if <tt>dialog</tt> matches the jain sip dialog
     * established with one of the participants in this call.
     *
     * @param dialog the dialog whose corresponding participant we're looking
     *            for.
     * @return true if this call contains a call participant whose jain sip
     *         dialog is the same as the specified and false otherwise.
     */
    public boolean contains(Dialog dialog)
    {
        return findCallParticipant(dialog) != null;
    }

    /**
     * Returns the call participant whose associated jain sip dialog matches
     * <tt>dialog</tt>.
     *
     * @param dialog the jain sip dialog whose corresponding participant we're
     *            looking for.
     * @return the call participant whose jain sip dialog is the same as the
     *         specified or null if no such call participant was found.
     */
    public CallParticipantSipImpl findCallParticipant(Dialog dialog)
    {
        Iterator callParticipants = this.getCallParticipants();

        if (logger.isTraceEnabled())
        {
            logger.trace("Looking for participant with dialog: " + dialog
                + "among " + this.callParticipants.size() + " calls");
        }

        while (callParticipants.hasNext())
        {
            CallParticipantSipImpl cp =
                (CallParticipantSipImpl) callParticipants.next();

            if (cp.getDialog() == dialog)
            {
                logger.trace("Returing cp=" + cp);
                return cp;
            }
            else
            {
                logger.trace("Ignoring cp=" + cp + " because cp.dialog="
                    + cp.getDialog() + " while dialog=" + dialog);
            }
        }

        return null;
    }

    /**
     * Sets the <tt>CallSession</tt> that the media service has created for this
     * call.
     *
     * @param callSession the <tt>CallSession</tt> that the media service has
     *            created for this call.
     */
    public void setMediaCallSession(CallSession callSession)
    {
        this.mediaCallSession = callSession;
    }

    /**
     * Sets the <tt>CallSession</tt> that the media service has created for this
     * call.
     *
     * @return the <tt>CallSession</tt> that the media service has created for
     *         this call or null if no call session has been created so far.
     */
    public CallSession getMediaCallSession()
    {
        return this.mediaCallSession;
    }
}
