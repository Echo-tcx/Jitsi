/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.jabber;

import java.util.*;

import org.jivesoftware.smack.*;
import org.jivesoftware.smackx.*;
import org.jivesoftware.smackx.jingle.*;
import org.jivesoftware.smackx.jingle.media.*;
import org.jivesoftware.smackx.jingle.listeners.*;
import org.jivesoftware.smackx.jingle.nat.*;
import org.jivesoftware.smackx.packet.DiscoverInfo;

import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.impl.protocol.jabber.mediamgr.*;

/**
 * Implements all call management logic and exports basic telephony support by
 * implementing OperationSetBasicTelephony.
 *
 * @author Symphorien Wanko
 */
public class OperationSetBasicTelephonyJabberImpl
   extends AbstractOperationSetBasicTelephony
   implements RegistrationStateChangeListener,
        JingleMediaListener,
        JingleTransportListener,
        JingleSessionRequestListener,
        CreatedJingleSessionListener,
        JingleSessionStateListener,
        JingleSessionListener
{

    /**
     * The logger used by this class
     */
    private static final Logger logger
            = Logger.getLogger(OperationSetBasicTelephonyJabberImpl.class);

    /**
     * A reference to the <tt>ProtocolProviderServiceJabberImpl</tt> instance
     * that created us.
     */
    private ProtocolProviderServiceJabberImpl protocolProvider = null;

    /**
     * Contains references for all currently active (non ended) calls.
     */
    private ActiveCallsRepository activeCallsRepository
            = new ActiveCallsRepository(this);

    /**
     * The manager we use to initiate, receive and ... manage jingle session.
     */
    private JingleManager jingleManager = null;

    /**
     * The transport manager is used by the <tt>jingleManager</tt> to handle transport
     * method.
     */
    private JingleTransportManager transportManager = null;

    /**
     * The media manager is used by the <tt>jingleManager</tt> to handle media
     * session.
     */
    private JingleMediaManager mediaManager = null;

    /**
     * Creates a new instance.
     *
     * @param protocolProvider a reference to the
     * <tt>ProtocolProviderServiceJabberImpl</tt> instance that created us.
     */
    public OperationSetBasicTelephonyJabberImpl(
            ProtocolProviderServiceJabberImpl protocolProvider)
    {

        this.protocolProvider = protocolProvider;
        protocolProvider.addRegistrationStateChangeListener(this);
        transportManager = new BasicTransportManager();
        mediaManager = new JingleScMediaManager();
    }

    /**
     * Implementation of method <tt>registrationStateChange</tt> from
     * interface RegistrationStateChangeListener for setting up (or down)
     * our <tt>JingleManager</tt> when an <tt>XMPPConnection</tt> is available
     *
     * @param evt the event received
     */
    public void registrationStateChanged(RegistrationStateChangeEvent evt)
    {
        if ((evt.getNewState() == RegistrationState.REGISTERED))
        {
            transportManager = new ICETransportManager(
                    protocolProvider.getConnection(),
                    "stun.iptel.org", 3478);

            jingleManager = new JingleManager(
                    protocolProvider.getConnection(),
                    transportManager,
                    mediaManager);

            jingleManager.addCreationListener(this);
            jingleManager.addJingleSessionRequestListener(this);

            logger.info("Jingle : ON ");
        }
        else if ((evt.getNewState() == RegistrationState.UNREGISTERED))
        {
            if (jingleManager != null)
            {
                jingleManager.removeCreationListener(this);
                jingleManager.removeJingleSessionRequestListener(this);
                jingleManager = null;

                logger.info("Jingle : OFF ");
            }
        }
    }

    /**
     * Create a new call and invite the specified CallParticipant to it.
     *
     * @param callee the jabber address of the callee that we should invite to a
     * new call.
     * @return CallParticipant the CallParticipant that will represented by
     *   the specified uri. All following state change events will be
     *   delivered through that call participant. The Call that this
     *   participant is a member of could be retrieved from the
     *   CallParticipatn instance with the use of the corresponding method.
     * @throws OperationFailedException with the corresponding code if we fail
     * to create the call.
     */
    public Call createCall(String callee)
            throws OperationFailedException
    {

        return createOutgoingCall(callee);
    }

    /**
     * Create a new call and invite the specified CallParticipant to it.
     *
     * @param callee the address of the callee that we should invite to a
     *   new call.
     * @return CallParticipant the CallParticipant that will represented by
     *   the specified uri. All following state change events will be
     *   delivered through that call participant. The Call that this
     *   participant is a member of could be retrieved from the
     *   CallParticipatn instance with the use of the corresponding method.
     * @throws OperationFailedException with the corresponding code if we fail
     * to create the call.
     */
    public Call createCall(Contact callee)
            throws OperationFailedException
    {

        return createOutgoingCall(callee.getAddress());
    }

    /**
     * Init and establish the specified call.
     *
     * @param calleeAddress the address of the callee that we'd like to connect
     * with.
     *
     * @return CallParticipant the CallParticipant that represented by
     *   the specified uri. All following state change events will be
     *   delivered through that call participant. The Call that this
     *   participant is a member of could be retrieved from the
     *   CallParticipatn instance with the use of the corresponding method.
     *
     * @throws OperationFailedException with the corresponding code if we fail
     * to create the call.
     */
    private CallJabberImpl createOutgoingCall(String calleeAddress)
            throws OperationFailedException
    {
        OutgoingJingleSession outJS;

        logger.info("creating outgoing call...");
        if (protocolProvider.getConnection() == null) {
            throw new OperationFailedException(
                    "Failed to create OutgoingJingleSession.\n"
                    + "we don't have a valid XMPPConnection."
                    , OperationFailedException.INTERNAL_ERROR);
        }

        // we determine on which resource the remote user is connected if the
        // resource isn't already provided
        String fullCalleeURI = null;
        if (calleeAddress.indexOf('/') > 0)
        {
            fullCalleeURI = calleeAddress;
        }
        else
        {
            fullCalleeURI = protocolProvider.getConnection().
                    getRoster().getPresence(calleeAddress).getFrom();
        }
        if (fullCalleeURI.indexOf('/') < 0)
        {
            throw new OperationFailedException(
                    "Failed to create OutgoingJingleSession.\n"
                    + "User " + calleeAddress + " is unknown to us."
                    , OperationFailedException.INTERNAL_ERROR);
        }

        try
        {
            // with discovered info, we can check if the remote clients
            // supports telephony but not if he don't, because
            // a non conforming client can supports a feature
            // without advertising it. So we don't rely on it (for the moment)
            DiscoverInfo di = ServiceDiscoveryManager
                    .getInstanceFor(protocolProvider.getConnection())
                    .discoverInfo(fullCalleeURI);
            if (di.containsFeature("http://www.xmpp.org/extensions/xep-0166.html#ns"))
            {
                logger.info(fullCalleeURI + ": jingle supported ");
            }
            else
            {
                logger.info(calleeAddress + ": jingle not supported ??? ");
//
//                throw new OperationFailedException(
//                        "Failed to create OutgoingJingleSession.\n"
//                        + fullCalleeURI + " do not supports jingle"
//                        , OperationFailedException.INTERNAL_ERROR);
            }
        }
        catch (XMPPException ex)
        {
            logger.warn("could not retrieve info for " + fullCalleeURI, ex);
        }

        try
        {
            outJS = jingleManager.createOutgoingJingleSession(fullCalleeURI);
        }
        catch (XMPPException ex)
        {
            throw new OperationFailedException(
                    "Failed to create OutgoingJingleSession.\n"
                    + "This is most probably a network connection error."
                    , OperationFailedException.INTERNAL_ERROR
                    , ex);
        }

        CallJabberImpl call = new CallJabberImpl(protocolProvider);

        CallParticipantJabberImpl callParticipant =
                new CallParticipantJabberImpl(calleeAddress, call);

        callParticipant.setJingleSession(outJS);
        callParticipant.setState(CallParticipantState.INITIATING_CALL);

        fireCallEvent(CallEvent.CALL_INITIATED, call);

        activeCallsRepository.addCall(call);

        outJS.start();

        return (CallJabberImpl) callParticipant.getCall();
    }

    /**
     * Returns an iterator over all currently active calls.
     *
     * @return an iterator over all currently active calls.
     */
    public Iterator getActiveCalls()
    {
        return activeCallsRepository.getActiveCalls();
    }

    /**
     * Resumes communication with a call participant previously put on hold.
     *
     * @param participant the call participant to put on hold.
     */
    public void putOffHold(CallParticipant participant)
    {
        /** @todo implement putOffHold() */
        ((CallParticipantJabberImpl) participant).getJingleSession().
                getJingleMediaSession().setTrasmit(true);
    }

    /**
     * Puts the specified CallParticipant "on hold".
     *
     * @param participant the participant that we'd like to put on hold.
     */
    public void putOnHold(CallParticipant participant)
    {
        /** @todo implement putOnHold() */
        ((CallParticipantJabberImpl) participant).getJingleSession().
                getJingleMediaSession().setTrasmit(false);
    }

    /**
     * Implements method <tt>hangupCallParticipant</tt>
     * from <tt>OperationSetBasicTelephony</tt>.
     *
     * @param participant the participant that we'd like to hang up on.
     * @throws ClassCastException if participant is not an instance of
     * CallParticipantJabberImpl.
     *
     * @throws OperationFailedException if we fail to terminate the call.
     *
     * // TODO: ask for suppression of OperationFailedException from the interface.
     * // what happens if hangup fails ? are we forced to continue to talk ? :o)
     */
    public void hangupCallParticipant(CallParticipant participant)
            throws ClassCastException, OperationFailedException
    {
        CallParticipantJabberImpl callParticipant
                = (CallParticipantJabberImpl)participant;
        try
        {
            callParticipant.getJingleSession().terminate();
        }
        catch (XMPPException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            callParticipant.setState(CallParticipantState.DISCONNECTED);
        }
    }



    /**
     * Implements method <tt>answerCallParticipant</tt>
     * from <tt>OperationSetBasicTelephony</tt>.
     *
     * @param participant the call participant that we want to answer
     * @throws OperationFailedException if we fails to answer
     */
    public void answerCallParticipant(CallParticipant participant)
            throws OperationFailedException
    {
        CallParticipantJabberImpl callParticipant
                = (CallParticipantJabberImpl)participant;
        try
        {
            ((IncomingJingleSession)callParticipant.getJingleSession()).
                    start();
        }
        catch (XMPPException ex)
        {
            throw new OperationFailedException(
                    "Failed to answer an incoming call"
                    , OperationFailedException.INTERNAL_ERROR);
        }
    }

    /**
     * Closes all active calls. And releases resources.
     */
    public void shutdown()
    {
        logger.trace("Ending all active calls. ");
        Iterator activeCalls = this.activeCallsRepository.getActiveCalls();

        // this is fast, but events aren't triggered ...
        //jingleManager.disconnectAllSessions();

        //go through all active calls.
        while(activeCalls.hasNext())
        {
            CallJabberImpl call = (CallJabberImpl) activeCalls.next();

            Iterator callParticipants = call.getCallParticipants();

            //go through all call participants and say bye to every one.
            while (callParticipants.hasNext())
            {
                CallParticipant participant
                        = (CallParticipant) callParticipants.next();
                try
                {
                    this.hangupCallParticipant(participant);
                }
                catch (Exception ex)
                {
                    logger.warn("Failed to properly hangup participant "
                            + participant
                            , ex);
                }
            }
        }
    }

    /**
     * Implements method sessionRequested from JingleSessionRequestListener.
     *
     * @param jingleSessionRequest the session requested
     */
    public void sessionRequested(JingleSessionRequest jingleSessionRequest)
    {
        IncomingJingleSession inJS;
        logger.info("session requested ");
        try
        {
            inJS = jingleSessionRequest.accept();
        }
        catch (XMPPException ex)
        {
            logger.error("Failed to accept incoming jingle request : " + ex);
            return;
        }

        CallJabberImpl call = new CallJabberImpl(protocolProvider);

        String from = jingleSessionRequest.getFrom();

        // we remove the ressource information at ends if any, as it is for
        // no meaning for the user
        if (from.indexOf("/") > 0)
        {
            from = from.substring(0, from.indexOf("/"));
        }
        CallParticipantJabberImpl callParticipant
                = new CallParticipantJabberImpl(from, call);

        callParticipant.setJingleSession(inJS);
        callParticipant.setState(CallParticipantState.INCOMING_CALL);

        activeCallsRepository.addCall(call);

        fireCallEvent(CallEvent.CALL_RECEIVED, call);
    }

    /**
     * Implements method sessionCreated from CreatedJingleSessionListener.
     *
     * @param jingleSession the newly created jingle session
     */
    public void sessionCreated(JingleSession jingleSession)
    {
        logger.info("session created : " + jingleSession);

        jingleSession.addListener(this);
        jingleSession.addMediaListener(this);
        jingleSession.addStateListener(this);
        jingleSession.addTransportListener(this);
    }

    /**
     * Implements method mediaClosed from JingleMediaListener.
     *
     * @param payloadType payload supported by the closed media
     */
    public void mediaClosed(PayloadType payloadType)
    {
        logger.info(" media closed ");
    }

    /**
     * Implements method <tt>mediaEstablished</tt> from JingleMediaListener.
     *
     * @param payloadType payload used by the established media
     */
    public void mediaEstablished(PayloadType payloadType)
    {
        logger.info("media established ");
    }

    /**
     * Implements method <tt>transportClosed</tt> from JingleTransportListener.
     *
     * @param transportCandidate <tt>transportCandiate</tt> with which
     * we were dealing
     */
    public void transportClosed(TransportCandidate transportCandidate)
    {
        logger.info("transport closed ");
    }

    /**
     * Implements method <tt>transportClosedOnError</tt> from JingleTransportListener.
     *
     * @param ex the exception accompagning this error
     */
    public void transportClosedOnError(XMPPException ex)
    {
        logger.error("transport closed on error ", ex);
    }

    /**
     * Implements method <tt>transportEstablished</tt> from JingleTransportListener.
     *
     * @param local local <tt>TransportCandidate</tt> for this transport link
     * @param remote remote <tt>TransportCandidate</tt> for this transport link
     */
    public void transportEstablished(TransportCandidate local,
            TransportCandidate remote)
    {
        logger.info("transport established " + local + " -:- " + remote);
    }

    /**
     * Implements method <tt>beforeChange</tt> from JingleSessionStateListener.
     * This method is called before the change occurs in the session.
     * We can cancel the change by throwing a <tt>JingleException</tt>
     *
     * @param oldState old state of the session
     * @param newState state in which we will go
     *
     * @throws JingleException we have the ability to cancel a state change by
     * throwing a <tt>JingleException</tt>
     */
    public void beforeChange(JingleNegotiator.State oldState
            , JingleNegotiator.State newState)
                    throws JingleNegotiator.JingleException
    {
        if (newState instanceof IncomingJingleSession.Active)
        {
            JingleSession session = (JingleSession) newState.getNegotiator();

            CallParticipantJabberImpl callParticipant =
                    activeCallsRepository.findCallParticipant(session);
            if (callParticipant == null)
            {
                return;
            }
            callParticipant.setState(CallParticipantState.CONNECTED);
        }
        else if (newState instanceof OutgoingJingleSession.Inviting)
        {
            JingleSession session = (JingleSession) newState.getNegotiator();

            CallParticipantJabberImpl callParticipant =
                    activeCallsRepository.findCallParticipant(session);
            if (callParticipant == null)
            {
                return;
            }
            callParticipant.setState(CallParticipantState.CONNECTING);
        }
        else if (newState instanceof OutgoingJingleSession.Pending)
        {
            JingleSession session = (JingleSession) newState.getNegotiator();

            CallParticipantJabberImpl callParticipant =
                    activeCallsRepository.findCallParticipant(session);
            if (callParticipant == null)
            {
                return;
            }
            callParticipant.setState(CallParticipantState.ALERTING_REMOTE_SIDE);
        }
        else if (newState instanceof OutgoingJingleSession.Active)
        {
            JingleSession session = (JingleSession) newState.getNegotiator();

            CallParticipantJabberImpl callParticipant =
                    activeCallsRepository.findCallParticipant(session);
            if (callParticipant == null)
            {
                return;
            }
            callParticipant.setState(CallParticipantState.CONNECTED);
        }

        if ((newState == null) && (oldState != null))
        { //hanging
            JingleSession session = (JingleSession) oldState.getNegotiator();

            CallParticipantJabberImpl callParticipant =
                    activeCallsRepository.findCallParticipant(session);
            if (callParticipant == null)
            {
                logger.debug("Received a stray trying response.");
                return;
            }
            try
            {
                hangupCallParticipant(callParticipant);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Implements method <tt>afterChanged</tt> from JingleSessionStateListener.
     * called when we are effectivly in the <tt>newState</tt>
     *
     * @param oldState old session state
     * @param newState new session state
     */
    public void afterChanged(JingleNegotiator.State oldState,
            JingleNegotiator.State newState)
    {
        logger.info("session state changed : " + oldState + " => " + newState);
    }

    /**
     * Implements <tt>sessionEstablished</tt> from <tt>JingleSessionListener</tt>
     *
     *
     * @param payloadType the <tt>payloadType</tt> used for media in thi session
     * @param remoteCandidate the remote end point of thi session
     * @param localCandidate the local end point of this session
     * @param jingleSession the session which is now fully established
     */
    public void sessionEstablished(PayloadType payloadType,
            TransportCandidate remoteCandidate,
            TransportCandidate localCandidate,
            JingleSession jingleSession)
    {
        logger.info("session established ");
    }

    /**
     * Implements <tt>sessionDeclined</tt> from <tt>JingleSessionListener</tt>
     *
     * @param reason why the session has been declined
     * @param jingleSession the declined session
     */
    public void sessionDeclined(String reason, JingleSession jingleSession)
    {
        logger.info("session declined : " + reason);
    }

    /**
     * Implements <tt>sessionRedirected</tt> from <tt>JingleSessionListener</tt>
     *
     * @param redirection redirection information
     * @param jingleSession the session which redirected
     */
    public void sessionRedirected(String redirection,
            JingleSession jingleSession)
    {
        logger.info("session redirected : " + redirection);
    }

    /**
     * Implements <tt>sessionClosed</tt> from <tt>JingleSessionListener</tt>
     *
     * @param reason why the session has been closed
     * @param jingleSession the session which is closed
     */
    public void sessionClosed(String reason, JingleSession jingleSession)
    {
        logger.info("session closed : " + reason);
    }

    /**
     * Implements <tt>sessionClosedOnError</tt> from <tt>JingleSessionListener</tt>
     *
     * @param ex execption which caused the error
     * @param jingleSession the session which is closed
     */
    public void sessionClosedOnError(XMPPException ex,
            JingleSession jingleSession)
    {
        logger.error("session closed on error ", ex);
    }

    /**
     * Implements <tt>sessionMediaReceived</tt> from <tt>JingleSessionListener</tt>
     *
     * @param jingleSession the session where the media is established
     * @param participant the participant for this media session
     */
    public void sessionMediaReceived(JingleSession jingleSession,
            String participant)
    {
        logger.info("session media received ");
    }
}

