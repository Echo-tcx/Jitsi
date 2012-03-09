/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.service.neomedia;

import net.sf.fmj.media.rtp.*;

/**
 * Class used to compute stats concerning a MediaStream.
 *
 * @author Vincent Lucas
 */
public interface MediaStreamStats
{
    /**
     * Computes and updates information for a specific stream.
     */
    public void updateStats();

    /**
     * Returns the MediaStream enconding.
     *
     * @return the encoding used by the stream.
     */
    public String getEncoding();

    /**
     * Returns the MediaStream enconding rate (in Hz)..
     *
     * @return the encoding rate used by the stream.
     */
    public String getEncodingClockRate();

    /**
     * Returns the local IP address of the MediaStream.
     *
     * @return the local IP address of the stream.
     */
    public String getLocalIPAddress();

    /**
     * Returns the local port of the MediaStream.
     *
     * @return the local port of the stream.
     */
    public int getLocalPort();

    /**
     * Returns the remote IP address of the MediaStream.
     *
     * @return the remote IP address of the stream.
     */
    public String getRemoteIPAddress();

    /**
     * Returns the remote port of the MediaStream.
     *
     * @return the remote port of the stream.
     */
    public int getRemotePort();

    /**
     * Returns the percent loss of the download stream.
     *
     * @return the last loss rate computed (in %).
     */
    public double getDownloadPercentLoss();

    /**
     * Returns the percent loss of the upload stream.
     *
     * @return the last loss rate computed (in %).
     */
    public double getUploadPercentLoss();

    /**
     * Returns the bandwidth used by this download stream.
     *
     * @return the last used download bandwidth computed (in Kbit/s).
     */
    public double getDownloadRateKiloBitPerSec();

    /**
     * Returns the bandwidth used by this download stream.
     *
     * @return the last used upload bandwidth computed (in Kbit/s).
     */
    public double getUploadRateKiloBitPerSec();

    /**
     * Returns the jitter average of this download stream.
     *
     * @return the last jitter average computed (in ms).
     */
    public double getDownloadJitterMs();

    /**
     * Returns the jitter average of this upload stream.
     *
     * @return the last jitter average computed (in ms).
     */
    public double getUploadJitterMs();

    /**
     * Updates this stream stats with the new feedback sent.
     *
     * @param feedback The last RTCP feedback sent by the MediaStream.
     */
    public void updateNewSentFeedback(RTCPFeedback feedback);

    /**
     * Updates this stream stats with the new feedback received.
     *
     * @param feedback The last RTCP feedback received by the MediaStream.
     */
    public void updateNewReceivedFeedback(RTCPFeedback feedback);
}
