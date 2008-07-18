/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.service.media;

import net.java.sip.communicator.service.protocol.*;
import java.text.*;
import java.net.*;

/**
 * A CallSession contains parameters associated with a particular Call such as
 * ports used for transmitting and sending media (audio video), a reference to
 * the call itself and others. Call session instances are created through the
 * <tt>openCallSession(Call)</tt> method of the MediaService.
 * <p>
 * One <tt>CallSession</tt> pertains to a single <tt>Call</tt> instance and a
 * single <tt>Call</tt> may only be associated one <tt>CallSession</tt>
 * instance.
 * <p>
 * A call session also allows signaling protocols to generate SDP offers and
 * construct sdp answers.
 *
 * @author Emil Ivov
 * @author Lubomir Marinov
 */
public interface CallSession
{
    /**
     * The method is meant for use by protocol service implementations when
     * willing to send an invitation to a remote callee.
     *
     * @return a String containing an SDP offer describing parameters of the
     * <tt>Call</tt> associated with this session.
     * @throws MediaException code INTERNAL_ERROR if generating the offer fails
     * for some reason.
     */
    public String createSdpOffer()
        throws MediaException;

    /**
     * The method is meant for use by protocol service implementations when
     * willing to send an invitation to a remote callee. The intendedDestination
     * parameter, may contain the address that the offer is to be sent to. In
     * case it is null we'll try our best to determine a default local address.
     *
     * @param intendedDestination the address of the call participant that the
     * descriptions is to be sent to.
     * @return a new SDP description String advertising all params of
     * <tt>callSession</tt>.
     *
     * @throws MediaException code SERVICE_NOT_STARTED if this method is called
     * before the service was started.
     */
    public String createSdpOffer(InetAddress intendedDestination)
        throws MediaException;

    /**
     * The method is meant for use by protocol service implementations when
     * willing to send an in-dialog invitation to a remote callee to put her
     * on/off hold or to send an answer to an offer to be put on/off hold.
     * 
     * @param participantSdpDescription the last SDP description of the remote
     *            callee
     * @param on <tt>true</tt> if the SDP description should offer the remote
     *            callee to be put on hold or answer an offer from the remote
     *            callee to be put on hold; <tt>false</tt> to work in the
     *            context of a put-off-hold offer
     * @return an SDP description <tt>String</tt> which offers the remote
     *         callee to be put her on/off hold or answers an offer from the
     *         remote callee to be put on/off hold
     * @throws MediaException
     */
    public String createSdpDescriptionForHold(String participantSdpDescription,
        boolean on) throws MediaException;

    /**
     * Determines whether a specific SDP description <tt>String</tt> offers
     * this party to be put on hold.
     * 
     * @param sdpOffer the SDP description <tt>String</tt> to be examined for
     *            an offer to this party to be put on hold
     * @return <tt>true</tt> if the specified SDP description <tt>String</tt>
     *         offers this party to be put on hold; <tt>false</tt>, otherwise
     * @throws MediaException
     */
    public boolean isSdpOfferToHold(String sdpOffer) throws MediaException;

    /**
     * Puts the media of this <tt>CallSession</tt> on/off hold depending on
     * the origin of the request.
     * <p>
     * For example, a remote request to have this party put off hold cannot
     * override an earlier local request to put the remote party on hold.
     * </p>
     * 
     * @param on <tt>true</tt> to request the media of this
     *            <tt>CallSession</tt> be put on hold; <tt>false</tt>,
     *            otherwise
     * @param here <tt>true</tt> if the request comes from this side of the
     *            call; <tt>false</tt> if the remote party is the issuer of
     *            the request i.e. it's the result of a remote offer
     */
    public void putOnHold(boolean on, boolean here);

    /**
     * The method is meant for use by protocol service implementations when
     * willing to respond to an invitation received from a remote caller. Apart
     * from simply generating an SDP response description, the method records
     * details
     *
     * @param sdpOffer the SDP offer that we'd like to create an answer for.
     * @param offerer the participant that has sent the offer.
     *
     * @return a String containing an SDP answer describing parameters of the
     * <tt>Call</tt> associated with this session and matching those advertised
     * by the caller in their <tt>sdpOffer</tt>.
     *
     * @throws MediaException code INTERNAL_ERROR if processing the offer and/or
     * generating the anser fail for some reason.
     * @throws ParseException if <tt>sdpOfferStr</tt> does not contain a valid
     * sdp string.
     */
    public String processSdpOffer(CallParticipant offerer, String sdpOffer)
        throws MediaException, ParseException;

    /**
     * The method is meant for use by protocol service implementations upon
     * reception of an SDP answer in response to an offer sent by us earlier.
     *
     * @param sdpAnswer the SDP answer that we'd like to handle.
     * @param responder the participant that has sent the answer.
     *
     * @throws MediaException code SERVICE_NOT_STARTED if this method is called
     * before the service was started.
     * @throws ParseException if <tt>sdpAnswerStr</tt> does not contain a valid
     * sdp string.
     */
    public void processSdpAnswer(CallParticipant responder, String sdpAnswer)
        throws MediaException, ParseException;

    /**
     * Returns the port that we are using for receiving video data in this
     * <tt>CallSession</tt>.
     * <p>
     * @return the port number we are using for receiving video data in this
     * <tt>CallSession</tt>.
     */
    public int getVideoPort();

    /**
     * Returns the port that we are using for receiving audio data in this
     * <tt>CallSession</tt>.
     * <p>
     * @return the port number we are using for receiving audio data in this
     * <tt>CallSession</tt>.
     */
    public int getAudioPort();
    
    /**
     * Returns a URL pointing to a location with call control information for 
     * this call or <tt>null</tt> if no such URL is available.
     * 
     * @return a URL link to a location with call information or a call control
     * web interface for the specified participant or <tt>null</tt> if no such 
     * URL is available.
     */
    public URL getCallInfoURL();
}
