/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.service.protocol.media;

import java.beans.*;
import java.util.*;

import net.java.sip.communicator.service.neomedia.*;
import net.java.sip.communicator.service.neomedia.device.*;
import net.java.sip.communicator.service.neomedia.event.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;

/**
 * A utility class implementing media control code shared between current
 * telephony implementations. This class is only meant for use by protocol
 * implementations and should/could not be accessed by bundles that are simply
 * using the telephony functionalities.
 *
 * @param <T> the peer extension class like for example <tt>CallPeerSipImpl</tt>
 * or <tt>CallPeerJabberImpl</tt>
 * @param <U> the provider extension class like for example
 * <tt>OperationSetBasicTelephonySipImpl</tt> or
 * <tt>OperationSetBasicTelephonySipImpl</tt>
 * @param <V> the provider extension class like for example
 * <tt>ProtocolProviderServiceSipImpl</tt> or
 * <tt>ProtocolProviderServiceJabberImpl</tt>
 *
 * @author Emil Ivov
 */
public abstract class MediaAwareCall<
                T extends MediaAwareCallPeer<?, ?, V>,
                U extends OperationSetBasicTelephony<V>,
                V extends ProtocolProviderService>
    extends AbstractCall<T, V>
    implements CallPeerListener
{
    /**
     * The <tt>MediaDevice</tt> which performs audio mixing for this
     * <tt>Call</tt> and its <tt>CallPeer</tt>s when the local peer represented
     * by this <tt>Call</tt> is acting as a conference focus i.e.
     * {@link #conferenceFocus} is <tt>true</tt>.
     */
    private MediaDevice conferenceAudioMixer;

    /**
     * The indicator which determines whether the local peer represented by this
     * <tt>Call</tt> is acting as a conference focus and may thus be specifying
     * a related parameter in its signaling, like for example the
     * &quot;isfocus&quot; parameter in the Contact headers of its outgoing SIP
     * signaling.
     */
    private boolean conferenceFocus = false;

    /**
     * Our video streaming policy.
     */
    protected boolean localVideoAllowed = false;

    /**
     * The <tt>OperationSetBasicTelephony</tt> implementation which created us.
     */
    protected final U parentOpSet;

    /**
     * Holds listeners registered for level changes in local audio.
     */
    private final List<SoundLevelListener> localUserAudioLevelListeners
        = new ArrayList<SoundLevelListener>();

    /**
     * The indicator which determines whether this <tt>Call</tt> is set
     * to transmit "silence" instead of the actual media.
     */
    private boolean mute = false;

    /**
     * Device used in call will be chosen according to <tt>MediaUseCase</tt>.
     */
    protected MediaUseCase mediaUseCase = MediaUseCase.ANY;

    /**
     * The <tt>MediaDevice</tt> for video we should use in the call.
     * In case this member is null, a lookup corresponding to MediaType.VIDEO is
     * performed to the <tt>MediaService</tt>.
     */
    private MediaDevice videoDevice = null;

    /**
     * The <tt>MediaDevice</tt> for audio we should use in the call.
     * In case this member is null, a lookup corresponding to MediaType.AUDIO is
     * performed to the <tt>MediaService</tt>.
     */
    private MediaDevice audioDevice = null;

    /**
     * The listener that would actually subscribe for level events from the
     * media handler if there's at least one listener in
     * <tt>localUserAudioLevelListeners</tt>.
     */
    private final SimpleAudioLevelListener localAudioLevelDelegator
        = new SimpleAudioLevelListener()
        {
            public void audioLevelChanged(int level)
            {
                fireLocalUserAudioLevelChangeEvent(level);
            }
        };

    /**
     * Crates a <tt>Call</tt> instance belonging to <tt>parentOpSet</tt>.
     *
     * @param parentOpSet a reference to the operation set that's creating us
     * and that we would be able to use for even dispatching.
     */
    protected MediaAwareCall(U parentOpSet)
    {
        super(parentOpSet.getProtocolProvider());
        this.parentOpSet = parentOpSet;
    }

    /**
     * Adds <tt>callPeer</tt> to the list of peers in this call.
     * If the call peer is already included in the call, the method has
     * no effect.
     *
     * @param callPeer the new <tt>CallPeer</tt>
     */
    protected void addCallPeer(T callPeer)
    {
        if (getCallPeersVector().contains(callPeer))
            return;

        callPeer.addCallPeerListener(this);

        synchronized(localUserAudioLevelListeners)
        {
            // if there's someone listening for audio level events then they'd
            // also like to know about the new peer.
            if(getCallPeersVector().isEmpty())
            {
                callPeer.getMediaHandler().setLocalUserAudioLevelListener(
                                localAudioLevelDelegator);
            }
        }

        getCallPeersVector().add(callPeer);
        fireCallPeerEvent(callPeer, CallPeerEvent.CALL_PEER_ADDED);
    }

    /**
     * Removes <tt>callPeer</tt> from the list of peers in this
     * call. The method has no effect if there was no such peer in the
     * call.
     *
     * @param evt the event containing the <tt>CallPeer</tt> leaving the call;
     * also we can obtain the reason for the <tt>CallPeerChangeEvent</tt> if
     * any. Use the event as cause for the call state change event..
     */
    @SuppressWarnings("unchecked")
    private void removeCallPeer(CallPeerChangeEvent evt)
    {
        T callPeer = (T)evt.getSourceCallPeer();

        if (!getCallPeersVector().contains(callPeer))
            return;

        getCallPeersVector().remove(callPeer);
        callPeer.removeCallPeerListener(this);

        synchronized(localUserAudioLevelListeners)
        {
            // remove sound level listeners from the peer
            callPeer.getMediaHandler().setLocalUserAudioLevelListener(null);
        }

        try
        {
            fireCallPeerEvent(callPeer,
                CallPeerEvent.CALL_PEER_REMOVED);
        }
        finally
        {

            /*
             * The peer should loose its state once it has finished
             * firing its events in order to allow the listeners to undo.
             */
            callPeer.setCall(null);
        }

        if (getCallPeersVector().isEmpty())
            setCallState(CallState.CALL_ENDED, evt);
    }

    /**
     * Dummy implementation of a method (inherited from CallPeerListener)
     * that we don't need.
     *
     * @param evt unused.
     */
    public void peerImageChanged(CallPeerChangeEvent evt)
    {
        //does not concern us
    }

    /**
     * Dummy implementation of a method (inherited from CallPeerListener)
     * that we don't need.
     *
     * @param evt unused.
     */
    public void peerAddressChanged(CallPeerChangeEvent evt)
    {
      //does not concern us
    }

    /**
     * Dummy implementation of a method (inherited from CallPeerListener)
     * that we don't need.
     *
     * @param evt unused.
     */
    public void peerTransportAddressChanged(
        CallPeerChangeEvent evt)
    {
      //does not concern us
    }

    /**
     * Dummy implementation of a method (inherited from CallPeerListener)
     * that we don't need.
     *
     * @param evt unused.
     */
    public void peerDisplayNameChanged(CallPeerChangeEvent evt)
    {
      //does not concern us
    }

    /**
     * Verifies whether the call peer has entered a state.
     *
     * @param evt The <tt>CallPeerChangeEvent</tt> instance containing
     * the source event as well as its previous and its new status.
     */
    public void peerStateChanged(CallPeerChangeEvent evt)
    {
        Object newState = evt.getNewValue();

        if (CallPeerState.DISCONNECTED.equals(newState)
                || CallPeerState.FAILED.equals(newState))
        {
            removeCallPeer(evt);
        }
        else if (CallPeerState.CONNECTED.equals(newState)
                || CallPeerState.CONNECTING_WITH_EARLY_MEDIA.equals(newState))
        {
            setCallState(CallState.CALL_IN_PROGRESS);
        }
        else if (CallPeerState.REFERRED.equals(newState))
        {
            setCallState(CallState.CALL_REFERRED);
        }
    }

    /**
     * Returns a reference to the <tt>OperationSetBasicTelephony</tt>
     * implementation instance that created this call.
     *
     * @return a reference to the <tt>OperationSetBasicTelephony</tt>
     * instance that created this call.
     */
    public U getParentOperationSet()
    {
        return parentOpSet;
    }

    /**
     * Gets the indicator which determines whether the local peer represented by
     * this <tt>Call</tt> is acting as a conference focus and thus may need to
     * send the corresponding parameters in its outgoing signaling.
     *
     * @return <tt>true</tt> if the local peer represented by this <tt>Call</tt>
     * is acting as a conference focus; otherwise, <tt>false</tt>
     */
    public boolean isConferenceFocus()
    {
        return conferenceFocus;
    }

    /**
     * Sets the indicator which determines whether the local peer represented by
     * this <tt>Call</tt> is acting as a conference focus and thus may need to
     * send the corresponding parameters in its outgoing signaling.
     *
     * @param conferenceFocus <tt>true</tt> if the local peer represented by
     * this <tt>Call</tt> is to act as a conference focus; otherwise,
     * <tt>false</tt>
     */
    public void setConferenceFocus(boolean conferenceFocus)
    {
        if (this.conferenceFocus != conferenceFocus)
        {
            this.conferenceFocus = conferenceFocus;

            /*
             * If this Call switches from being a conference focus to not being
             * one, dispose of the audio mixer used when it was a conference
             * focus.
             */
            if (!this.conferenceFocus)
                conferenceAudioMixer = null;

            // fire that the focus property has changed
            fireCallChangeEvent(
                CallChangeEvent.CALL_FOCUS_CHANGE,
                !this.conferenceFocus, this.conferenceFocus);
        }
    }

    /**
     * Gets a <tt>MediaDevice</tt> which is capable of capture and/or playback
     * of media of the specified <tt>MediaType</tt>, is the default choice of
     * the user for a <tt>MediaDevice</tt> with the specified <tt>MediaType</tt>
     * and is appropriate for the current state of this <tt>Call</tt>.
     * <p>
     * For example, when the local peer represented by this <tt>Call</tt>
     * instance is acting as a conference focus, the audio device must be a
     * mixer.
     * </p>
     *
     * @param mediaType the <tt>MediaType</tt> in which the retrieved
     * <tt>MediaDevice</tt> is to capture and/or play back media
     * @return a <tt>MediaDevice</tt> which is capable of capture and/or
     * playback of media of the specified <tt>mediaType</tt>, is the default
     * choice of the user for a <tt>MediaDevice</tt> with the specified
     * <tt>mediaType</tt> and is appropriate for the current state of this
     * <tt>Call</tt>
     */
    public MediaDevice getDefaultDevice(MediaType mediaType)
    {
        MediaDevice device = null;
        MediaService mediaService = ProtocolMediaActivator.getMediaService();

        switch(mediaType)
        {
        case AUDIO:
            device = audioDevice;
            break;
        case VIDEO:
            device = videoDevice;
            break;
        default:
            /* no other type supported */
            return null;
        }

        if(device == null)
        {
            device = mediaService.getDefaultDevice(mediaType,
                    mediaUseCase);
        }

        if (MediaType.AUDIO.equals(mediaType))
        {
            if (conferenceAudioMixer == null)
            {
                if (device != null)
                    conferenceAudioMixer = mediaService.createMixer(device);
            }
            return conferenceAudioMixer;
        }
        return device;
    }

    /**
     * Adds a specific <tt>SoundLevelListener</tt> to the list of
     * listeners interested in and notified about changes in local sound level
     * related information. When the first listener is being registered the
     * method also registers its single listener with the call peer media
     * handlers so that it would receive level change events and delegate them
     * to the listeners that have registered with us.
     *
     * @param l the <tt>SoundLevelListener</tt> to add
     */
    public void addLocalUserSoundLevelListener(SoundLevelListener l)
    {
        synchronized(localUserAudioLevelListeners)
        {
            if (localUserAudioLevelListeners.isEmpty())
            {
                //if this is the first listener that's being registered with
                //us, we also need to register ourselves as an audio level
                //listener with the media handler. we do this so that audio
                //level would only be calculated if anyone is interested in
                //receiving them.
                Iterator<T> cps = getCallPeers();

                while (cps.hasNext())
                {
                    T callPeer = cps.next();

                    callPeer.getMediaHandler()
                            .setLocalUserAudioLevelListener(
                                                localAudioLevelDelegator);
                }
            }

            localUserAudioLevelListeners.add(l);
        }
    }

    /**
     * Removes a specific <tt>SoundLevelListener</tt> from the list of listeners
     * interested in and notified about changes in local sound level related
     * information. If <tt>l</tt> is the last listener that we had here we are
     * also going to unregister our own level event delegate in order to stop
     * level calculations.
     *
     * @param l the <tt>SoundLevelListener</tt> to remove
     */
    public void removeLocalUserSoundLevelListener(SoundLevelListener l)
    {
        synchronized(localUserAudioLevelListeners)
        {
            localUserAudioLevelListeners.add(l);

            if (localUserAudioLevelListeners.isEmpty())
            {
                //if this was the last listener that was registered with us then
                //no long need to have a delegator registered with the call
                //peer media handlers. We therefore remove it so that audio
                //level calculations would be ceased.
                Iterator<T> cps = getCallPeers();

                while (cps.hasNext())
                {
                    T callPeer = cps.next();

                    callPeer.getMediaHandler()
                            .setLocalUserAudioLevelListener(null);
                }
            }
        }
    }

    /**
     * Notified by its very majesty the media service about changes in the
     * audio level of the local user, this listener generates the corresponding
     * events and delivers them to the listeners that have registered here.
     *
     * @param newLevel the new audio level of the local user.
     */
    private void fireLocalUserAudioLevelChangeEvent(int newLevel)
    {
        SoundLevelChangeEvent evt
            = new SoundLevelChangeEvent(this, newLevel);

        synchronized( localUserAudioLevelListeners )
        {
            for(SoundLevelListener listener : localUserAudioLevelListeners)
                 listener.soundLevelChanged(evt);
        }
    }

    /**
     * Determines whether this call is mute.
     *
     * @return <tt>true</tt> if an audio streams being sent to the call
     *         peers are currently muted; <tt>false</tt>, otherwise
     */
    public boolean isMute()
    {
        return this.mute;
    }

    /**
     * Sets the mute property for this call.
     *
     * @param mute the new value of the mute property for this call
     */
    public void setMute(boolean mute)
    {
        if (this.mute != mute)
        {
            this.mute = mute;

            Iterator<T> peers = getCallPeers();

            while (peers.hasNext())
                peers.next().setMute(this.mute);
        }
    }

    /**
     * Modifies the local media setup of all peers in the call to reflect the
     * requested setting for the streaming of the local video and then passes
     * the setting to the participating <tt>MediaAwareCallPeer</tt> instances.
     *
     * @param allowed <tt>true</tt> if local video transmission is allowed and
     * <tt>false</tt> otherwise.
     * @param useCase the use case of the video (i.e video call or desktop
     * streaming/sharing session)
     *
     *  @throws OperationFailedException if video initialization fails.
     */
    public void setLocalVideoAllowed(boolean allowed, MediaUseCase useCase)
        throws OperationFailedException
    {
        localVideoAllowed = allowed;
        mediaUseCase = useCase;

        // Record the setting locally and notify all peers.
        Iterator<T> peers = getCallPeers();

        while (peers.hasNext())
            peers.next().setLocalVideoAllowed(allowed);
    }

    /**
     * Determines whether the streaming of local video in this <tt>Call</tt>
     * is currently allowed. The setting does not reflect the availability of
     * actual video capture devices, it just expresses the local policy (or
     * desire of the user) to have the local video streamed in the case the
     * system is actually able to do so.
     *
     * @return <tt>true</tt> if the streaming of local video for this
     * <tt>Call</tt> is allowed; otherwise, <tt>false</tt>
     */
    public boolean isLocalVideoAllowed()
    {
        return localVideoAllowed;
    }

    /**
     * Determines whether we are currently streaming video toward at least one
     * of the peers in this call.
     *
     * @return <tt>true</tt> if we are currently streaming video toward at least
     * one of the peers in this call and <tt>false</tt> otherwise.
     */
    public boolean isLocalVideoStreaming()
    {
        Iterator<T> peers = getCallPeers();

        while (peers.hasNext())
        {
            if (peers.next().isLocalVideoStreaming())
                return true;
        }
        return false;
    }

    /**
     * Registers a <tt>listener</tt> with all <tt>CallPeer</tt> currently
     * participating with the call so that it would be notified of changes in
     * video related properties (e.g. <tt>LOCAL_VIDEO_STREAMING</tt>).
     *
     * @param listener the <tt>PropertyChangeListener</tt> to be notified
     * when the properties associated with member <tt>CallPeer</tt>s change
     * their values.
     */
    public void addVideoPropertyChangeListener(
                                          PropertyChangeListener listener)
    {
        Iterator<T> peers = getCallPeers();

        while (peers.hasNext())
            peers.next().addVideoPropertyChangeListener(listener);
    }

    /**
     * Removes <tt>listener</tt> from all <tt>CallPeer</tt>s currently
     * participating with the call so that it won't receive further notifications
     * on changes in video related properties (e.g.
     * <tt>LOCAL_VIDEO_STREAMING</tt>).
     *
     * @param listener the <tt>PropertyChangeListener</tt> to unregister from
     * member <tt>CallPeer</tt>s change their values.
     */
    public void removeVideoPropertyChangeListener(
                                             PropertyChangeListener listener)
    {
        Iterator<T> peers = getCallPeers();

        while (peers.hasNext())
            peers.next().removeVideoPropertyChangeListener(listener);
    }

    /**
     * Creates a new <tt>Recorder</tt> which is to record this <tt>Call</tt>
     * (into a file which is to be specified when starting the returned
     * <tt>Recorder</tt>).
     *
     * @return a new <tt>Recorder</tt> which is to record this <tt>Call</tt>
     * (into a file which is to be specified when starting the returned
     * <tt>Recorder</tt>)
     * @throws OperationFailedException if anything goes wrong while creating
     * the new <tt>Recorder</tt> for this <tt>Call</tt>
     */
    public Recorder createRecorder()
        throws OperationFailedException
    {
        final Recorder recorder
            = ProtocolMediaActivator.getMediaService().createRecorder(
                    getDefaultDevice(MediaType.AUDIO));

        if (recorder != null)
        {
            // Make sure the recorder is stopped when this call ends.
            final CallChangeListener callChangeListener
                = new CallChangeAdapter()
                {
                    @Override
                    public void callStateChanged(CallChangeEvent evt)
                    {
                        if (CallState.CALL_ENDED.equals(evt.getNewValue()))
                            recorder.stop();
                    }
                };

            addCallChangeListener(callChangeListener);

            /*
             * If the recorder gets stopped earlier than this call ends, don't
             * wait for the end of the call because callChangeListener will keep
             * a reference to the stopped recorder.
             */
            recorder.addListener(
                    new Recorder.Listener()
                    {
                        public void recorderStopped(Recorder recorder)
                        {
                            removeCallChangeListener(callChangeListener);
                        }
                    });
        }
        return recorder;
    }

    /**
     * Set the <tt>MediaDevice</tt> used for the video.
     *
     * @param dev video <tt>MediaDevice</tt>
     */
    public void setVideoDevice(MediaDevice dev)
    {
        videoDevice = dev;
    }

    /**
     * Set the <tt>MediaDevice</tt> used for the audio.
     *
     * @param dev audio <tt>MediaDevice</tt>
     */
    public void setAudioDevice(MediaDevice dev)
    {
        this.audioDevice = dev;
    }
}
