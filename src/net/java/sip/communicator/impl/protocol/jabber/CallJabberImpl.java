/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.jabber;

import org.jivesoftware.smackx.packet.*;

import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.service.protocol.media.*;
import net.java.sip.communicator.util.*;

/**
 * A Jabber implementation of the <tt>Call</tt> abstract class encapsulating
 * Jabber jingle sessions.
 *
 * @author Emil Ivov
 */
public class CallJabberImpl
    extends MediaAwareCall<
                    CallPeerJabberImpl,
                    OperationSetBasicTelephonyJabberImpl,
                    ProtocolProviderServiceJabberImpl>
{
    /**
     * The <tt>Logger</tt> used by the <tt>CallJabberImpl</tt> class and its
     * instances for logging output.
     */
    private static final Logger logger = Logger.getLogger(CallJabberImpl.class);

    /**
     * Indicates if the <tt>CallPeer</tt> will support </tt>inputevt</tt>
     * extension (i.e. will be able to be remote-controlled).
     */
    private boolean localInputEvtAware = false;

    /**
     * Initializes a new <tt>CallJabberImpl</tt> instance belonging to
     * <tt>sourceProvider</tt> and associated with the jingle session with the
     * specified <tt>jingleSID</tt>. If the new instance corresponds to an
     * incoming jingle session, then the jingleSID would come from there.
     * Otherwise, one could generate one using {@link JingleIQ#generateSID()}.
     *
     * @param parentOpSet the {@link OperationSetBasicTelephonyJabberImpl}
     * instance in the context of which this call has been created.
     */
    protected CallJabberImpl(
                        OperationSetBasicTelephonyJabberImpl parentOpSet)
    {
        super(parentOpSet);

        //let's add ourselves to the calls repo. we are doing it ourselves just
        //to make sure that no one ever forgets.
        parentOpSet.getActiveCallsRepository().addCall(this);
    }

    /**
     * Enable or disable <tt>inputevt</tt> support (remote control).
     *
     * @param enable new state of inputevt support
     */
    public void setLocalInputEvtAware(boolean enable)
    {
        localInputEvtAware = enable;
    }

    /**
     * Returns if the call support <tt>inputevt</tt> (remote control).
     *
     * @return true if the call support <tt>inputevt</tt>, false otherwise
     */
    public boolean getLocalInputEvtAware()
    {
        return localInputEvtAware;
    }

    /**
     * Creates a new call peer and sends a RINGING response.
     *
     * @param jingleIQ the {@link JingleIQ} that created the session.
     *
     * @return the newly created {@link CallPeerJabberImpl} (the one that sent
     * the INVITE).
     */
    public CallPeerJabberImpl processSessionInitiate(JingleIQ jingleIQ)
    {
        String remoteParty = jingleIQ.getInitiator();

        //according to the Jingle spec initiator may be null.
        if (remoteParty == null)
            remoteParty = jingleIQ.getFrom();

        CallPeerJabberImpl callPeer = new CallPeerJabberImpl(remoteParty, this);

        //before notifying about this call, make sure that it looks alright
        callPeer.processSessionInitiate(jingleIQ);

        if( callPeer.getState() == CallPeerState.FAILED)
            return null;

        addCallPeer(callPeer);

        callPeer.setState( CallPeerState.INCOMING_CALL );

        // if this was the first peer we added in this call then the call is
        // new and we also need to notify everyone of its creation.
        if(this.getCallPeerCount() == 1)
            parentOpSet.fireCallEvent( CallEvent.CALL_RECEIVED, this);

        return callPeer;
    }

    /**
     * Creates a <tt>CallPeerJabberImpl</tt> from <tt>calleeJID</tt> and sends
     * them <tt>session-initiate</tt> IQ request.
     *
     * @param calleeJID the party that we would like to invite to this call.
     * @param discoverInfo any discovery information that we have for the jid
     * we are trying to reach and that we are passing in order to avoid having
     * to ask for it again.
     *
     * @return the newly created <tt>Call</tt> corresponding to
     * <tt>calleeJID</tt>. All following state change events will be
     * delivered through this call peer.
     *
     * @throws OperationFailedException  with the corresponding code if we fail
     *  to create the call.
     */
    public CallPeerJabberImpl initiateSession(String       calleeJID,
                                              DiscoverInfo discoverInfo)
        throws OperationFailedException
    {
        // create the session-initiate IQ
        CallPeerJabberImpl callPeer = new CallPeerJabberImpl(calleeJID, this);

        callPeer.setDiscoverInfo(discoverInfo);

        addCallPeer(callPeer);

        callPeer.setState( CallPeerState.INITIATING_CALL);

        // if this was the first peer we added in this call then the call is
        // new and we also need to notify everyone of its creation.
        if(this.getCallPeerCount() == 1)
            parentOpSet.fireCallEvent( (CallEvent.CALL_INITIATED), this);

        /* enable video if it is a videocall */
        callPeer.getMediaHandler().setLocalVideoTransmissionEnabled(
                                                            localVideoAllowed);
        /* enable remote-control if it is a desktop sharing session */
        callPeer.getMediaHandler().setLocalInputEvtAware(localInputEvtAware);

        //set call state to connecting so that the user interface would start
        //playing the tones. we do that here because we may be harvesting
        //STUN/TURN addresses in initiateSession() which would take a while.
        callPeer.setState( CallPeerState.CONNECTING);

        callPeer.initiateSession();

        return callPeer;
    }

    /**
     * Send a <tt>content-modify</tt> message for all current <tt>CallPeer</tt>
     * to reflect possible video change in media setup.
     *
     * @param allowed if the local video is allowed or not
     * @throws OperationFailedException if problem occurred during message
     * generation or network problem
     */
    public void modifyVideoContent(boolean allowed)
        throws OperationFailedException
    {
        if(logger.isInfoEnabled())
            logger.info(allowed ? "Start local video streaming" :
                "Stop local video streaming");

        for(CallPeerJabberImpl peer : getCallPeersVector())
            peer.sendModifyVideoContent(allowed);
    }

    /**
     * Determines if this call contains a peer whose corresponding session has
     * the specified <tt>sid</tt>.
     *
     * @param sid the ID of the session whose peer we are looking for.
     *
     * @return <tt>true</tt> if this call contains a peer with the specified
     * jingle <tt>sid</tt> and false otherwise.
     */
    public boolean containsJingleSID(String sid)
    {
        return (getPeer(sid) != null);
    }

    /**
     * Returns the peer whose corresponding session has the specified
     * <tt>sid</tt>.
     *
     * @param sid the ID of the session whose peer we are looking for.
     *
     * @return the {@link CallPeerJabberImpl} with the specified jingle
     * <tt>sid</tt> and <tt>null</tt> if no such peer exists in this call.
     */
    public CallPeerJabberImpl getPeer(String sid)
    {
        for(CallPeerJabberImpl peer : getCallPeersVector())
        {
            if (peer.getJingleSID().equals(sid))
                return peer;
        }
        return null;
    }
}
