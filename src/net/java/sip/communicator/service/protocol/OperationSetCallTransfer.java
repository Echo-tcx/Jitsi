/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.service.protocol;

/**
 * An Operation Set defining operations that allow transfering calls to a new
 * location.
 *
 * @author Emil Ivov
 */
public interface OperationSetCallTransfer
    extends OperationSet
{
    /**
     * Indicates a user request to transfer the specified call particiapant to a
     * new (target) uri.
     * @param participant the call participant we'd like to transfer
     * @param targetURI the uri that we'd like this call participant to be
     * transferred to.
     */
    public void transferCallParticipant(CallPeer participant,
                                        String targetURI);
}
