/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.call;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.util.List; // disambiguation

import javax.swing.*;
import javax.swing.text.*;

import net.java.sip.communicator.impl.gui.main.call.conference.*;
import net.java.sip.communicator.impl.gui.utils.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.swing.*;

/**
 * The <tt>OneToOneCallPeerPanel</tt> is the panel containing data for a call
 * peer in a given call. It contains information like call peer
 * name, photo, call duration, etc.
 *
 * @author Yana Stamcheva
 * @author Lubomir Marinov
 * @author Sebastien Vincent
 */
public class OneToOneCallPeerPanel
    extends TransparentPanel
    implements CallPeerRenderer
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 0L;

    /**
     * The <tt>Logger</tt> used by the <tt>OneToOneCallPeerPanel</tt> class and
     * its instances for logging output.
     */
    private static final Logger logger
        = Logger.getLogger(OneToOneCallPeerPanel.class);

    /**
     * The <tt>CallPeerAdapter</tt> that implements all common tt>CallPeer</tt>
     * related listeners.
     */
    private CallPeerAdapter callPeerAdapter;

    /**
     * The component showing the name of the underlying call peer.
     */
    private final JLabel nameLabel = new JLabel("", JLabel.CENTER);

    /**
     * The component showing the status of the underlying call peer.
     */
    private final JLabel callStatusLabel = new JLabel();

    /**
     * The security status of the peer
     */
    private SecurityStatusLabel securityStatusLabel = new SecurityStatusLabel();

    /**
     * The label showing whether the voice has been set to mute.
     */
    private final JLabel muteStatusLabel = new JLabel();

    /**
     * The label showing whether the call is on or off hold.
     */
    private final JLabel holdStatusLabel = new JLabel();

    /**
     * The DTMF label.
     */
    private final JLabel dtmfLabel = new JLabel();

    /**
     * The component responsible for displaying an error message.
     */
    private JTextComponent errorMessageComponent;

    /**
     * The <tt>Component</tt>s showing the avatar of the underlying call peer.
     * <p>
     * Because the <tt>Component</tt>s showing the avatar of the underlying call
     * peer are managed by their respective <tt>VideoContainer</tt>s and are
     * automatically displayed whenever there is no associated remote video,
     * each <tt>VideoContainer</tt> must have its own. Otherwise, the various
     * <tt>VideoContainer</tt>s might start fighting over which one is to
     * contain the one and only photoLabel.
     * </p>
     */
    private final List<JLabel> photoLabels = new LinkedList<JLabel>();

    /**
     * The panel containing security related components.
     */
    private SecurityPanel securityPanel;

    /**
     * The <tt>Icon</tt> which represents the avatar of the associated call
     * peer.
     */
    private ImageIcon peerImage;

    /**
     * The name of the peer.
     */
    private final String peerName;

    /**
     * The list containing all video containers.
     */
    private final List<Container> videoContainers
        = new LinkedList<Container>();

    /**
     * The operation set through which we do all video operations.
     */
    private OperationSetVideoTelephony videoTelephony;

    /**
     * The renderer of the call.
     */
    private final CallRenderer callRenderer;

    /**
     * The component showing the local video.
     */
    private Component localVideo;

    /**
     * The component showing the remote video.
     */
    private Component remoteVideo;

    /**
     * The <tt>CallPeer</tt>, which is rendered in this panel.
     */
    private CallPeer callPeer;

    /**
     * In case of desktop streaming (client-side) if the local peer can control
     * remote peer's computer.
     */
    private boolean allowRemoteControl = false;

    /**
     * Listener for all key and mouse events. It is used for desktop sharing
     * purposes.
     */
    private MouseAndKeyListener mouseAndKeyListener = null;

    /**
     * Creates a <tt>CallPeerPanel</tt> for the given call peer.
     *
     * @param callRenderer the renderer of the call
     * @param callPeer the <tt>CallPeer</tt> represented in this panel
     */
    public OneToOneCallPeerPanel(   CallRenderer callRenderer,
                                    CallPeer callPeer)
    {
        this.callRenderer = callRenderer;
        this.callPeer = callPeer;
        this.peerName = callPeer.getDisplayName();

        this.setPeerImage(CallManager.getPeerImage(callPeer));

        /* Create the main Components of the UI. */
        nameLabel.setText(peerName);
        nameLabel.setAlignmentX(JLabel.CENTER);

        Component center = createCenter();
        Component statusBar = createStatusBar();

        /* Lay out the main Components of the UI. */
        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        if (nameLabel != null)
        {
            constraints.fill = GridBagConstraints.NONE;
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.weightx = 0;

            add(nameLabel, constraints);
        }
        if (center != null)
        {
            /*
             * Don't let the center dictate the preferred size because it may
             * display large videos. Otherwise, the large video will make this
             * panel expand and then the panel's container will show scroll
             * bars.
             */
            center.setPreferredSize(new Dimension(1, 1));

            constraints.fill = GridBagConstraints.BOTH;
            constraints.gridx = 0;
            constraints.gridy = 1;
            constraints.weightx = 1;
            constraints.weighty = 1;

            add(center, constraints);
        }
        if (statusBar != null)
        {
            constraints.fill = GridBagConstraints.NONE;
            constraints.gridx = 0;
            constraints.gridy = 3;
            constraints.weightx = 0;
            constraints.weighty = 0;
            constraints.insets = new Insets(5, 0, 0, 0);

            add(statusBar, constraints);
        }

        this.createSoundLevelIndicators();

        addVideoListener();
        addRemoteControlListener();
    }

    /**
     * Creates the <code>Component</code> hierarchy of the central area of this
     * <code>CallPeerPanel</code> which displays the photo of the
     * <code>CallPeer</code> or the video if any.
     *
     * @return the root of the <code>Component</code> hierarchy of the central
     *         area of this <code>CallPeerPanel</code> which displays the
     *         photo of the <code>CallPeer</code> or the video if any
     */
    Component createCenter()
    {
        final JLabel photoLabel = new JLabel(getPhotoLabelIcon());

        photoLabel.setPreferredSize(new Dimension(90, 90));

        final Container videoContainer = createVideoContainer(photoLabel);

        videoContainer.addHierarchyListener(new HierarchyListener()
        {
            public void hierarchyChanged(HierarchyEvent event)
            {
                int changeFlags = HierarchyEvent.DISPLAYABILITY_CHANGED;

                if ((event.getChangeFlags() & changeFlags) == changeFlags)
                {
                    synchronized (videoContainers)
                    {
                        boolean changed = false;

                        if (videoContainer.isDisplayable())
                        {
                            if (!videoContainers.contains(videoContainer))
                                changed = videoContainers.add(videoContainer);
                            if (!photoLabels.contains(photoLabel))
                                photoLabels.add(photoLabel);
                        }
                        else
                        {
                            changed = videoContainers.remove(videoContainer);
                            photoLabels.remove(photoLabel);
                        }
                        if (changed)
                            handleVideoEvent(null);
                    }
                }
            }
        });
        return videoContainer;
    }

    /**
     * Creates a new AWT <code>Container</code> which can display a single
     * <code>Component</code> at a time (supposedly, one which represents video)
     * and, in the absence of such a <code>Component</code>, displays a
     * predefined default <code>Component</code> (in accord with the previous
     * supposition, one which is the default when there is no video). The
     * returned <code>Container</code> will track the <code>Components</code>s
     * added to and removed from it in order to make sure that
     * <code>noVideoContainer</code> is displayed as described.
     *
     * @param noVideoComponent the predefined default <code>Component</code> to
     *            be displayed in the returned <code>Container</code> when there
     *            is no other <code>Component</code> in it
     * @return a new <code>Container</code> which can display a single
     *         <code>Component</code> at a time and, in the absence of such a
     *         <code>Component</code>, displays <code>noVideoComponent</code>
     */
    private Container createVideoContainer(Component noVideoComponent)
    {
        return new VideoContainer(noVideoComponent);
    }

    /**
     * Creates the <code>Component</code> hierarchy of the area of
     * status-related information such as <code>CallPeer</code> display
     * name, call duration, security status.
     *
     * @return the root of the <code>Component</code> hierarchy of the area of
     *         status-related information such as <code>CallPeer</code>
     *         display name, call duration, security status
     */
    private Component createStatusBar()
    {
        // stateLabel
        callStatusLabel.setForeground(Color.WHITE);
        dtmfLabel.setForeground(Color.WHITE);
        callStatusLabel.setText(callPeer.getState().getStateString());

        PeerStatusPanel statusPanel = new PeerStatusPanel(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;
        statusPanel.add(securityStatusLabel, constraints);

        constraints.gridx++;
        statusPanel.add(holdStatusLabel, constraints);

        constraints.gridx++;
        statusPanel.add(muteStatusLabel, constraints);

        constraints.gridx++;
        statusPanel.add(callStatusLabel, constraints);

        constraints.gridx++;
        constraints.weightx = 1f;
        statusPanel.add(dtmfLabel, constraints);

        return statusPanel;
    }

    /**
     * Creates sound level related components.
     */
    private void createSoundLevelIndicators()
    {
        TransparentPanel localLevelPanel
            = new TransparentPanel(new BorderLayout(5, 0));
        TransparentPanel remoteLevelPanel
            = new TransparentPanel(new BorderLayout(5, 0));

        JLabel localLevelLabel
            = new JLabel(new ImageIcon(
                ImageLoader.getImage(ImageLoader.MICROPHONE)));
        JLabel remoteLevelLabel
            = new JLabel(new ImageIcon(
                ImageLoader.getImage(ImageLoader.HEADPHONE)));

        final SoundLevelIndicator localLevelIndicator
            = new SoundLevelIndicator(  SoundLevelChangeEvent.MIN_LEVEL,
                                        SoundLevelChangeEvent.MAX_LEVEL);

        final SoundLevelIndicator remoteLevelIndicator
            = new SoundLevelIndicator(  SoundLevelChangeEvent.MIN_LEVEL,
                                        SoundLevelChangeEvent.MAX_LEVEL);

        localLevelPanel.add(localLevelLabel, BorderLayout.WEST);
        localLevelPanel.add(localLevelIndicator, BorderLayout.CENTER);
        remoteLevelPanel.add(remoteLevelLabel, BorderLayout.WEST);
        remoteLevelPanel.add(remoteLevelIndicator, BorderLayout.CENTER);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.insets = new Insets(10, 0, 0, 0);

        add(localLevelPanel, constraints);

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.insets = new Insets(5, 0, 10, 0);

        add(remoteLevelPanel, constraints);

        this.callPeer.addStreamSoundLevelListener(new SoundLevelListener()
        {
            public void soundLevelChanged(SoundLevelChangeEvent event)
            {
                remoteLevelIndicator.updateSoundLevel(event.getLevel());
            }
        });

        this.callPeer.getCall().addLocalUserSoundLevelListener(
            new SoundLevelListener()
            {
                public void soundLevelChanged(SoundLevelChangeEvent event)
                {
                    localLevelIndicator.updateSoundLevel(event.getLevel());
                }
            });
    }

    /**
     * Listener that will process change related to video such as if local
     * streaming has been turned on/off or a video component has been
     * added/removed.
     */
    private class VideoTelephonyListener
        implements PropertyChangeListener,
                   VideoListener
    {
        /**
         * {@inheritDoc}
         */
        public void propertyChange(PropertyChangeEvent event)
        {
            if (OperationSetVideoTelephony.LOCAL_VIDEO_STREAMING
                    .equals(event.getPropertyName()))
                handleLocalVideoStreamingChange(this);
        }

        /**
         * {@inheritDoc}
         */
        public void videoAdded(VideoEvent event)
        {
            handleVideoEvent(event);
        }

        /**
         * {@inheritDoc}
         */
        public void videoRemoved(VideoEvent event)
        {
            handleVideoEvent(event);
        }

        /**
         * {@inheritDoc}
         */
        public void videoUpdate(VideoEvent event)
        {
            handleVideoEvent(event);
        }
    }

    /**
     * Sets up listening to remote-control notifications (granted or revoked).
     *
     * @return reference to <tt>OperationSetDesktopSharingClient</tt>
     */
    private OperationSetDesktopSharingClient addRemoteControlListener()
    {
        final Call call = callPeer.getCall();

        if (call == null)
            return null;

        OperationSetDesktopSharingClient desktopSharingClient =
            call.getProtocolProvider()
            .getOperationSet(OperationSetDesktopSharingClient.class);

        if(desktopSharingClient != null)
        {
            mouseAndKeyListener = new MouseAndKeyListener(desktopSharingClient);
            desktopSharingClient.addRemoteControlListener(mouseAndKeyListener);
        }
        return desktopSharingClient;
    }

    /**
     * Sets up listening to notifications about adding or removing video for the
     * <code>CallPeer</code> this panel depicts and displays the video in
     * question in the last-known of {@link #videoContainers} (because the video
     * is represented by a <code>Component</code> and it cannot be displayed in
     * multiple <code>Container</code>s at one and the same time) as soon as it
     * arrives.
     * @return the video telephony operation set, where the vide listener was
     * added
     */
    private OperationSetVideoTelephony addVideoListener()
    {
        final Call call = callPeer.getCall();
        if (call == null)
            return null;

        final OperationSetVideoTelephony telephony =
            call.getProtocolProvider()
                .getOperationSet(OperationSetVideoTelephony.class);
        if (telephony == null)
            return null;

        final VideoTelephonyListener videoTelephonyListener
            = new VideoTelephonyListener();

        /**
         * The video is only available while the #callPeer is in a Call
         * and that call is in progress so only listen to VideoEvents during
         * that time.
         */
        CallChangeListener callListener = new CallChangeListener()
        {
            private boolean videoListenerIsAdded;

            private void addVideoListener()
            {
                telephony.addVideoListener(
                        callPeer, videoTelephonyListener);
                telephony.addPropertyChangeListener(
                        call, videoTelephonyListener);
                videoListenerIsAdded = true;

                synchronized (videoContainers)
                {
                    videoTelephony = telephony;

                    handleVideoEvent(null);

                    handleLocalVideoStreamingChange(
                            videoTelephonyListener);
                }
            }

            /**
             * When the #callPeer of this CallPeerPanel gets added
             * to the Call, starts listening for changes in the video in order
             * to display it.
             *
             * @param event the <tt>CallPeerEvent</tt> received
             */
            public synchronized void callPeerAdded(
                CallPeerEvent event)
            {
                if (callPeer.equals(event.getSourceCallPeer())
                        && !videoListenerIsAdded)
                {
                    Call call = callPeer.getCall();

                    if ((call != null)
                            && CallState.CALL_IN_PROGRESS.equals(
                                    call.getCallState()))
                        addVideoListener();
                }
            }

            /**
             * When the #callPeer of this CallPeerPanel leaves the
             * Call, stops listening for changes in the video because it should
             * no longer be updated anyway.
             *
             * @param event the <tt>CallPeerEvent</tt> received
             */
            public synchronized void callPeerRemoved(
                CallPeerEvent event)
            {
                if (callPeer.equals(event.getSourceCallPeer())
                        && videoListenerIsAdded)
                {
                    if (callPeer.getCall() != null)
                        removeVideoListener();
                }
            }

            /**
             * When the Call of #callPeer ends, stops tracking the
             * updates in the video because there should no longer be any video
             * anyway. When the Call in question starts, starts tracking any
             * changes to the video because it's negotiated and it should be
             * displayed in this CallPeerPanel.
             *
             * @param event the <tt>CallChangeEvent</tt> received
             */
            public synchronized void callStateChanged(CallChangeEvent event)
            {
                // we are interested only in CALL_STATE_CHANGEs
                if(!event.getEventType().equals(
                        CallChangeEvent.CALL_STATE_CHANGE))
                    return;

                CallState newCallState = (CallState) event.getNewValue();

                if (CallState.CALL_ENDED.equals(newCallState))
                {
                    if (videoListenerIsAdded)
                        removeVideoListener();
                    call.removeCallChangeListener(this);

                    if(allowRemoteControl)
                    {
                        allowRemoteControl = false;
                        removeMouseAndKeyListeners();
                    }
                }
                else if (CallState.CALL_IN_PROGRESS.equals(newCallState))
                {
                    if (!videoListenerIsAdded)
                        addVideoListener();
                }
            }

            private void removeVideoListener()
            {
                telephony.removeVideoListener(
                        callPeer, videoTelephonyListener);
                telephony.removePropertyChangeListener(
                        call, videoTelephonyListener);
                videoListenerIsAdded = false;

                if (localVideo != null)
                {
                    telephony.disposeLocalVisualComponent(
                            callPeer, localVideo);
                    localVideo = null;
                }

                synchronized (videoContainers)
                {
                    if (telephony.equals(videoTelephony))
                        videoTelephony = null;
                }

                callRenderer.exitFullScreen();
            }
        };
        call.addCallChangeListener(callListener);
        callListener.callStateChanged(new CallChangeEvent(call,
            CallChangeEvent.CALL_STATE_CHANGE, null, call.getCallState()));

        return telephony;
    }

    /**
     * When a video is added or removed for the <tt>callPeer</tt>, makes sure to
     * display or hide it respectively.
     *
     * @param event a <tt>VideoEvent</tt> describing the added visual
     * <tt>Component</tt> representing video and the provider it was added into
     * or <tt>null</tt> if such information is not available
     */
    private void handleVideoEvent(final VideoEvent event)
    {
        synchronized (videoContainers)
        {
            if ((event != null)
                    && !event.isConsumed())
            {
                Component video = event.getVisualComponent();

                switch (event.getType())
                {
                case VideoEvent.VIDEO_ADDED:
                    if(event.getOrigin() == VideoEvent.LOCAL)
                    {
                        this.localVideo = video;
                    }
                    else if(event.getOrigin() == VideoEvent.REMOTE)
                    {
                        this.remoteVideo = video;

                        if(allowRemoteControl)
                        {
                            addMouseAndKeyListeners();
                        }
                    }

                    /*
                     * Let the creator of the local visual Component know it
                     * shouldn't be disposed of because we're going to use it.
                     */
                    event.consume();
                    break;

                case VideoEvent.VIDEO_REMOVED:
                    if (event.getOrigin() == VideoEvent.LOCAL &&
                            localVideo == video)
                    {
                        this.localVideo = null;
                    }
                    else if(event.getOrigin() == VideoEvent.REMOTE &&
                            remoteVideo == video)
                    {
                        this.remoteVideo = video;
                    }
                    break;
                }
            }
        }

        if (!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    handleVideoEvent(event);
                }
            });
            return;
        }

        synchronized (videoContainers)
        {
            int videoContainerCount;

            if ((videoTelephony != null)
                    && ((videoContainerCount = videoContainers.size()) > 0))
            {
                Container videoContainer
                    = videoContainers.get(videoContainerCount - 1);

                handleVideoEvent(event, videoContainer);
            }
        }
    }

    /**
     * Handles a specific <tt>VideoEvent</tt> related to a specific visual
     * <tt>Component</tt> depicting video knowing that it is to be displayed or
     * is already displayed in a specific <tt>Container</tt>.
     *
     * @param videoEvent the <tt>VideoEvent</tt> describing the visual
     * <tt>Component</tt> which was added, removed or updated
     * @param videoContainer the <tt>Container</tt> which is to contain or
     * already contains the visual <tt>Component</tt> described by
     * <tt>videoEvent</tt>
     */
    private void handleVideoEvent(
            VideoEvent videoEvent,
            Container videoContainer)
    {
        if (videoEvent != null)
        {
            if ((videoEvent.getOrigin() == VideoEvent.REMOTE)
                    && (videoEvent instanceof SizeChangeVideoEvent))
            {
                SizeChangeVideoEvent sizeChangeVideoEvent
                    = (SizeChangeVideoEvent) videoEvent;
                Component visualComponent
                    = sizeChangeVideoEvent.getVisualComponent();
                int width = sizeChangeVideoEvent.getWidth();
                int height = sizeChangeVideoEvent.getHeight();

                if (visualComponent.getParent() == null)
                {
                    visualComponent.setPreferredSize(
                            new Dimension(width, height));
                }
                else if (isAncestor(videoContainer, visualComponent))
                {
                    callRenderer.ensureSize(visualComponent, width, height);

                    /*
                     * Even if ensureSize hasn't changed the Frame size,
                     * videoContainer may still need to lay out visualComponent
                     * again because the size-related properties of the latter
                     * have likely changed.
                     */
                    videoContainer.doLayout();
                }
                return;
            }

            /*
             * We only care about VIDEO_ADDED and VIDEO_REMOVED from now on till
             * the end of this method. And null, of course.
             */
            switch (videoEvent.getType())
            {
                case VideoEvent.VIDEO_ADDED:
                case VideoEvent.VIDEO_REMOVED:
                    break;
                default:
                    return;
            }
        }

        videoContainer.removeAll();

        // REMOTE
        Component video = videoTelephony.getVisualComponent(callPeer);

        if (video != null)
        {
            Container videoParent = video.getParent();

            if (videoParent != null)
                videoParent.remove(video);

            videoContainer.add(video, VideoLayout.CENTER_REMOTE, -1);
        }

        // LOCAL
        if (localVideo != null)
        {
            Container localVideoParent = localVideo.getParent();

            if (localVideoParent != null)
                localVideoParent.remove(localVideo);

            videoContainer.add(localVideo, VideoLayout.LOCAL, 0);

            CallDialog callDialog = callRenderer.getCallDialog();

            /*
             * If the local video or desktop sharing is turned on, we ensure
             * that the button is selected.
             */
            if (callDialog.isDesktopSharing())
            {
                callDialog.setDesktopSharingButtonSelected(true);
            }
            else if (!callDialog.isDesktopSharing()
                    && !callDialog.isVideoButtonSelected())
                callDialog.setVideoButtonSelected(true);
        }

        videoContainer.validate();

        /*
         * Without explicit repainting, the remote visual Component will not
         * stay small after entering fullscreen, the Component shown when there
         * is no video will be shown beneath the video Component though the
         * former has already been removed...
         */
        videoContainer.repaint();
    }

    /**
     * Handles the change when we turn on/off local video streaming such as
     * creating/releasing visual component.
     *
     * @param listener Listener that will be callbacked
     */
    private void handleLocalVideoStreamingChange(
            VideoTelephonyListener listener)
    {
        synchronized (videoContainers)
        {
            if (videoTelephony == null)
                return;

            if (videoTelephony.isLocalVideoStreaming(callPeer.getCall()))
            {
                try
                {
                    videoTelephony.createLocalVisualComponent(
                            callPeer, listener);
                }
                catch (OperationFailedException ex)
                {
                    logger.error(
                            "Failed to create local video/visual Component.",
                            ex);
                }
            }
            else if (localVideo != null)
            {
                videoTelephony.disposeLocalVisualComponent(
                        callPeer, localVideo);
                localVideo = null;
            }
        }
    }

    /**
     * Returns the name of the peer, contained in this panel.
     *
     * @return the name of the peer, contained in this panel
     */
    public String getPeerName()
    {
        return peerName;
    }

    /**
     * The <tt>TransparentPanel</tt> that will display the peer status.
     */
    private static class PeerStatusPanel
        extends TransparentPanel
    {
        /**
         * Silence the serial warning. Though there isn't a plan to serialize
         * the instances of the class, there're no fields so the default
         * serialization routine will work.
         */
        private static final long serialVersionUID = 0L;

        /**
         * Constructs a new <tt>PeerStatusPanel</tt>.
         *
         * @param layout the <tt>LayoutManager</tt> to use
         */
        public PeerStatusPanel(LayoutManager layout)
        {
            super(layout);
            this.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        }

        /**
         * @{inheritDoc}
         */
        @Override
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            g = g.create();

            try
            {
                AntialiasingManager.activateAntialiasing(g);

                g.setColor(Color.DARK_GRAY);
                g.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 20, 20);
            }
            finally
            {
                g.dispose();
            }
        }
    }

    /**
     * Sets the name of the peer.
     * @param name the name of the peer
     */
    public void setPeerName(String name)
    {
        this.nameLabel.setText(name);
    }

    /**
     * Set the image of the peer
     *
     * @param image new image
     */
    public void setPeerImage(byte[] image)
    {
        if (image == null || image.length <= 0)
            return;

        this.peerImage = ImageUtils.getScaledRoundedIcon(image, 100, 100);

        this.peerImage = getPhotoLabelIcon();
        synchronized (videoContainers)
        {
            for (JLabel photoLabel : photoLabels)
                photoLabel.setIcon(this.peerImage);
        }
    }

    /**
     * Gets the <tt>Icon</tt> to be displayed in {@link #photoLabels}.
     *
     * @return the <tt>Icon</tt> to be displayed in {@link #photoLabels}
     */
    private ImageIcon getPhotoLabelIcon()
    {
        return
            (peerImage == null)
                ? new ImageIcon(
                        ImageLoader.getImage(ImageLoader.DEFAULT_USER_PHOTO))
                : peerImage;
    }

    /**
     * Sets the state of the contained call peer by specifying the
     * state name and icon.
     *
     * @param state the state of the contained call peer
     */
    public void setPeerState(String state)
    {
        this.callStatusLabel.setText(state);
    }

    /**
     * Sets the mute status icon to the status panel.
     *
     * @param isMute indicates if the call with this peer is
     * muted
     */
    public void setMute(boolean isMute)
    {
        if(isMute)
            muteStatusLabel.setIcon(new ImageIcon(
                ImageLoader.getImage(ImageLoader.MUTE_STATUS_ICON)));
        else
            muteStatusLabel.setIcon(null);

        this.revalidate();
        this.repaint();
    }

    /**
     * Sets the "on hold" property value.
     * @param isOnHold indicates if the call with this peer is put on hold
     */
    public void setOnHold(boolean isOnHold)
    {
        if(isOnHold)
            holdStatusLabel.setIcon(new ImageIcon(
                ImageLoader.getImage(ImageLoader.HOLD_STATUS_ICON)));
        else
            holdStatusLabel.setIcon(null);

        this.revalidate();
        this.repaint();
    }

    /**
     * Indicates that the security is turned on.
     * <p>
     * Sets the secured status icon to the status panel and initializes/updates
     * the corresponding security details.
     * @param securityString the security string
     * @param isSecurityVerified indicates if the security string has been
     * already verified by the underlying <tt>CallPeer</tt>
     */
    public void securityOn( String securityString,
                            boolean isSecurityVerified)
    {
        securityStatusLabel.setIcon(new ImageIcon(ImageLoader
            .getImage(ImageLoader.SECURE_BUTTON_ON)));

        if (securityPanel == null)
        {
            securityPanel = new SecurityPanel(callPeer);

            GridBagConstraints constraints = new GridBagConstraints();

            constraints.fill = GridBagConstraints.NONE;
            constraints.gridx = 0;
            constraints.gridy = 2;
            constraints.weightx = 0;
            constraints.weighty = 0;
            constraints.insets = new Insets(5, 0, 0, 0);
            this.add(securityPanel, constraints);
        }

        securityPanel.refreshStates(securityString, isSecurityVerified);

        this.revalidate();
    }

    /**
     * Determines whether a specific <tt>Container</tt> is an ancestor of a
     * specific <tt>Component</tt> (in the UI hierarchy).
     *
     * @param container the <tt>Container</tt> which is to be tested as an
     * ancestor of <tt>component</tt>
     * @param component the <tt>Component</tt> which is to be tested as having
     * <tt>container</tt> as its ancestor
     * @return <tt>true</tt> if <tt>container</tt> is an ancestor of
     * <tt>component</tt> (in the UI hierarchy); otherwise, <tt>false</tt>
     */
    private static boolean isAncestor(Container container, Component component)
    {
        do
        {
            Container parent = component.getParent();

            if (parent == null)
                return false;
            else if (parent.equals(container))
                return true;
            else
                component = parent;
        }
        while (true);
    }

    /**
     * Indicates that the security has gone off.
     */
    public void securityOff()
    {
        securityStatusLabel.setIcon(new ImageIcon(ImageLoader
            .getImage(ImageLoader.SECURE_BUTTON_OFF)));
    }

    /**
     * Updates all related components to fit the new value.
     * @param isAudioSecurityOn indicates if the audio security is turned on
     * or off.
     */
    public void setAudioSecurityOn(boolean isAudioSecurityOn)
    {
        securityStatusLabel.setAudioSecurityOn(isAudioSecurityOn);
    }

    /**
     * Updates all related components to fit the new value.
     * @param encryptionCipher the encryption cipher to show
     */
    public void setEncryptionCipher(String encryptionCipher)
    {
        securityStatusLabel.setEncryptionCipher(encryptionCipher);
    }

    /**
     * Updates all related components to fit the new value.
     * @param isVideoSecurityOn indicates if the video security is turned on
     * or off.
     */
    public void setVideoSecurityOn(boolean isVideoSecurityOn)
    {
        securityStatusLabel.setVideoSecurityOn(isVideoSecurityOn);
    }

    /**
     * Sets the call peer adapter managing all related listeners.
     * @param adapter the adapter to set
     */
    public void setCallPeerAdapter(CallPeerAdapter adapter)
    {
        this.callPeerAdapter = adapter;
    }

    /**
     * Returns the call peer adapter managing all related listeners.
     * @return the call peer adapter
     */
    public CallPeerAdapter getCallPeerAdapter()
    {
        return callPeerAdapter;
    }

    /**
     * Returns the parent <tt>CallDialog</tt> containing this renderer.
     * @return the parent <tt>CallDialog</tt> containing this renderer
     */
    public CallDialog getCallDialog()
    {
        return callRenderer.getCallDialog();
    }

    /**
     * Prints the given DTMG character through this <tt>CallPeerRenderer</tt>.
     * @param dtmfChar the DTMF char to print
     */
    public void printDTMFTone(char dtmfChar)
    {
        dtmfLabel.setText(dtmfLabel.getText() + dtmfChar);
    }

    /**
     * Add <tt>KeyListener</tt>, <tt>MouseListener</tt>,
     * <tt>MouseWheelListener</tt> and <tt>MouseMotionListener</tt> to remote
     * video component.
     */
    private void addMouseAndKeyListeners()
    {
        if(remoteVideo != null)
        {
            remoteVideo.addKeyListener(mouseAndKeyListener);
            remoteVideo.addMouseListener(mouseAndKeyListener);
            remoteVideo.addMouseMotionListener(mouseAndKeyListener);
            remoteVideo.addMouseWheelListener(mouseAndKeyListener);
        }
    }

    /**
     * Remove <tt>KeyListener</tt>, <tt>MouseListener</tt>,
     * <tt>MouseWheelListener</tt> and <tt>MouseMotionListener</tt> to remote
     * video component.
     */
    private void removeMouseAndKeyListeners()
    {
        if(remoteVideo != null)
        {
            remoteVideo.removeKeyListener(mouseAndKeyListener);
            remoteVideo.removeMouseListener(mouseAndKeyListener);
            remoteVideo.removeMouseMotionListener(mouseAndKeyListener);
            remoteVideo.removeMouseWheelListener(mouseAndKeyListener);
        }
    }

    /**
     * Sets the reason of a call failure if one occurs. The renderer should
     * display this reason to the user.
     * @param reason the reason to display
     */
    public void setErrorReason(String reason)
    {
        if (errorMessageComponent == null)
        {
            errorMessageComponent = new JTextPane();

            JTextPane textPane = (JTextPane) errorMessageComponent;
            textPane.setEditable(false);
            textPane.setOpaque(false);

            StyledDocument doc = textPane.getStyledDocument();

            MutableAttributeSet standard = new SimpleAttributeSet();
            StyleConstants.setAlignment(standard, StyleConstants.ALIGN_CENTER);
            StyleConstants.setFontFamily(standard,
                                        textPane.getFont().getFamily());
            StyleConstants.setFontSize(standard, 12);
            doc.setParagraphAttributes(0, 0, standard, true);

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.gridx = 0;
            constraints.gridy = 4;
            constraints.weightx = 1;
            constraints.weighty = 0;
            constraints.insets = new Insets(5, 0, 0, 0);

            add(errorMessageComponent, constraints);
            this.revalidate();
        }

        errorMessageComponent.setText(reason);

        if (isVisible())
            errorMessageComponent.repaint();
    }

    /**
     * Listener for all key and mouse events and will transfer them to
     * the <tt>OperationSetDesktopSharingClient</tt>.
     *
     * @author Sebastien Vincent
     */
    private class MouseAndKeyListener
        implements RemoteControlListener,
                   KeyListener,
                   MouseListener,
                   MouseMotionListener,
                   MouseWheelListener
    {
        /**
         * Desktop sharing clien-side <tt>OperationSet</tt>.
         */
        private final OperationSetDesktopSharingClient desktopSharingClient;

        /**
         * Last time the mouse has moved inside remote video. It is used mainly
         * to avoid sending too much <tt>MouseEvent</tt> which can take a lot of
         * bandwidth.
         */
        private long lastMouseMovedTime = 0;

        /**
         * Constructor.
         *
         * @param opSet <tt>OperationSetDesktopSharingClient</tt> object
         */
        public MouseAndKeyListener(OperationSetDesktopSharingClient opSet)
        {
            desktopSharingClient = opSet;
        }

        /**
         * {@inheritDoc}
         */
        public void mouseMoved(MouseEvent event)
        {
            if(System.currentTimeMillis() > lastMouseMovedTime + 50)
            {
                desktopSharingClient.sendMouseEvent(callPeer, event,
                        remoteVideo.getSize());
                lastMouseMovedTime = System.currentTimeMillis();
            }
        }

        /**
         * {@inheritDoc}
         */
        public void mousePressed(MouseEvent event)
        {
            desktopSharingClient.sendMouseEvent(callPeer, event);
        }

        /**
         * {@inheritDoc}
         */
        public void mouseReleased(MouseEvent event)
        {
            desktopSharingClient.sendMouseEvent(callPeer, event);
        }

        /**
         * {@inheritDoc}
         */
        public void mouseClicked(MouseEvent event)
        {
            /* do nothing */
        }

        /**
         * {@inheritDoc}
         */
        public void mouseEntered(MouseEvent event)
        {
            /* do nothing */
        }

        /**
         * {@inheritDoc}
         */
        public void mouseExited(MouseEvent event)
        {
            /* do nothing */
        }

        /**
         * {@inheritDoc}
         */
        public void mouseWheelMoved(MouseWheelEvent event)
        {
            desktopSharingClient.sendMouseEvent(callPeer, event);
        }

        /**
         * {@inheritDoc}
         */
        public void mouseDragged(MouseEvent event)
        {
             desktopSharingClient.sendMouseEvent(callPeer, event,
                     remoteVideo.getSize());
        }

        /**
         * {@inheritDoc}
         */
        public void keyPressed(KeyEvent event)
        {
            char key = event.getKeyChar();
            int code = event.getKeyCode();

            if(key == KeyEvent.CHAR_UNDEFINED ||
                    code == KeyEvent.VK_CLEAR ||
                    code == KeyEvent.VK_DELETE ||
                    code == KeyEvent.VK_BACK_SPACE ||
                    code == KeyEvent.VK_ENTER)
            {
                desktopSharingClient.sendKeyboardEvent(callPeer, event);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void keyReleased(KeyEvent event)
        {
            char key = event.getKeyChar();
            int code = event.getKeyCode();

            if(key == KeyEvent.CHAR_UNDEFINED ||
                    code == KeyEvent.VK_CLEAR ||
                    code == KeyEvent.VK_DELETE ||
                    code == KeyEvent.VK_BACK_SPACE ||
                    code == KeyEvent.VK_ENTER)
            {
                desktopSharingClient.sendKeyboardEvent(callPeer, event);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void keyTyped(KeyEvent event)
        {
            char key = event.getKeyChar();

            if(key != '\n' && key != '\b')
            {
                desktopSharingClient.sendKeyboardEvent(callPeer, event);
            }
        }

        /**
         * This method is called when remote control has been granted.
         *
         * @param event <tt>RemoteControlGrantedEvent</tt>
         */
        public void remoteControlGranted(RemoteControlGrantedEvent event)
        {
            allowRemoteControl = true;

            if(remoteVideo != null)
            {
                addMouseAndKeyListeners();
            }
        }

        /**
         * This method is called when remote control has been revoked.
         *
         * @param event <tt>RemoteControlRevokedEvent</tt>
         */
        public void remoteControlRevoked(RemoteControlRevokedEvent event)
        {
            if(allowRemoteControl)
            {
                allowRemoteControl = false;
                removeMouseAndKeyListeners();
            }
        }
    }
}
