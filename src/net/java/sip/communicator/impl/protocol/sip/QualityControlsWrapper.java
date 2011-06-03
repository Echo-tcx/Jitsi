/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.sip;

import net.java.sip.communicator.service.neomedia.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;

/**
 * A wrapper of media quality control.
 * @author Damian Minkov
 */
public class QualityControlsWrapper
    implements QualityControls
{
    /**
     * Our class logger.
     */
    private static final Logger logger
        = Logger.getLogger(QualityControlsWrapper.class);

    /**
     * The peer we are controlling.
     */
    private CallPeerSipImpl peer;

    /**
     * The media quality control.
     */
    private QualityControls qualityControls;

    /**
     * The currently used video quality preset.
     */
    protected QualityPresets remoteSendMaxPreset = null;

    /**
     * The frame rate.
     */
    private float maxFrameRate = -1;

    /**
     * Creates quality control for peer.
     * @param peer
     */
    QualityControlsWrapper(CallPeerSipImpl peer)
    {
        this.peer = peer;
    }

    /**
     * Checks and obtains quality control from media stream.
     * @return
     */
    private QualityControls getMediaQualityControls()
    {
        if(qualityControls != null)
            return qualityControls;

        MediaStream stream = peer.getMediaHandler().getStream(MediaType.VIDEO);

        if(stream != null && stream instanceof VideoMediaStream)
            qualityControls = ((VideoMediaStream)stream).getQualityControls();

        return qualityControls;
    }

    /**
     * The currently used quality preset announced as receive by remote party.
     * @return the current quality preset.
     */
    public QualityPresets getRemoteReceivePreset()
    {
        QualityControls qControls = getMediaQualityControls();

        if(qControls == null)
        {
            return null;
        }

        return qControls.getRemoteReceivePreset();
    }

    /**
     * The minimum preset that the remote party is sending and we are receiving.
     * Not Used.
     * @return the minimum remote preset.
     */
    public QualityPresets getRemoteSendMinPreset()
    {
        QualityControls qControls = getMediaQualityControls();

        if(qControls == null)
            return null;

        return qControls.getRemoteSendMinPreset();
    }

    /**
     * The maximum preset that the remote party is sending and we are receiving.
     * @return the maximum preset announced from remote party as send.
     */
    public QualityPresets getRemoteSendMaxPreset()
    {
        QualityControls qControls = getMediaQualityControls();

        if(qControls == null)
            return remoteSendMaxPreset;

        QualityPresets qp = qControls.getRemoteSendMaxPreset();

        // there is info about max frame rate
        if(qp != null && maxFrameRate > 0)
            qp = new QualityPresets(qp.getResolution(), (int)maxFrameRate);

        return qp;
    }

    /**
     * Changes local value of frame rate, the one we have received from
     * remote party.
     * @param f new frame rate.
     */
    public void setMaxFrameRate(float f)
    {
        this.maxFrameRate = f;
    }

    /**
     * Changes remote send preset. This doesn't have impact of current stream.
     * But will have on next media changes.
     * With this we can try to change the resolution that the remote part
     * is sending.
     * @param preset the new preset value.
     */
    public void setRemoteSendMaxPreset(QualityPresets preset)
    {
        QualityControls qControls = getMediaQualityControls();

        if(qControls != null)
            qControls.setRemoteSendMaxPreset(preset);
        else
            remoteSendMaxPreset = preset;
    }

    /**
     * Changes the current video settings for the peer with the desired
     * quality settings and inform the peer to stream the video
     * with those settings.
     *
     * @param preset the desired video settings
     * @throws OperationFailedException
     */
    public void setPreferredRemoteSendMaxPreset(QualityPresets preset)
        throws OperationFailedException
    {
        QualityControls qControls = getMediaQualityControls();

        if(qControls != null)
        {
            qControls.setRemoteSendMaxPreset(preset);

            try
            {
                // re-invites the peer with the new settings
                peer.sendReInvite();
            }
            catch (Throwable ex)
            {
                ProtocolProviderServiceSipImpl.throwOperationFailedException(
                    "Failed to re-invite for video quality change.",
                    OperationFailedException.INTERNAL_ERROR, ex, logger);
            }
        }
    }
}
