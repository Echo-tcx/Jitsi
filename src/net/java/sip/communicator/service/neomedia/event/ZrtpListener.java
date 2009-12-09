/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.service.neomedia.event;

/**
 * The <tt>ZrtpListener</tt> is meant to be used by the media stream
 * creator, as the name indicates in order to be notified when a security event
 * has occured that concerns zrt.
 * 
 * @author Yana Stamcheva
 */
public interface ZrtpListener
{
    /**
     * Indicates that the security has been turned on. When we are in the case
     * of using multistreams when the master stream zrtp is inited and
     * established the param multiStreamData holds the data needed for the
     * slave streams to establish their sessions. If this is a securityTurnedOn
     * event on non master stream the multiStreamData is null.
     * 
     * @param sessionType the type of the call session - audio or video.
     * @param cipher the cipher
     * @param securityString the SAS
     * @param isVerified indicates if the SAS has been verified
     * @param multiStreamData the data for the multistream
     *        used by non master streams.
     */
    public void securityTurnedOn( int sessionType,
                            String cipher,
                            String securityString,
                            boolean isVerified,
                            byte[] multiStreamData);

    /**
     * Indicates that the security has been turned off.
     * 
     * @param sessionType the type of the call session - audio or video.
     */
    public void securityTurnedOff(int sessionType);

    /**
     * Indicates that a security message has occurred associated with a
     * failure/warning or information coming from the encryption protocol.
     * 
     * @param message the message.
     * @param i18nMessage the internationalized message
     * @param severity severity level 
     */
    public void securityMessageReceived(String message,
                                String i18nMessage,
                                int severity);
}
