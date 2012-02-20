/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.plugin.otr;

import net.java.otr4j.*;
import net.java.otr4j.session.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;

/**
 * The Off-the-Record {@link TransformLayer} implementation.
 * 
 * @author George Politis
 */
public class OtrTransformLayer
    implements TransformLayer
{
    /*
     * Implements TransformLayer#messageDelivered(MessageDeliveredEvent).
     */
    public MessageDeliveredEvent messageDelivered(MessageDeliveredEvent evt)
    {
        Contact contact = evt.getDestinationContact();
        
        OtrPolicy policy = OtrActivator.scOtrEngine.getContactPolicy(contact);
        SessionStatus sessionStatus =
            OtrActivator.scOtrEngine.getSessionStatus(contact);
        // If OTR is disabled and we are not over an encrypted session, don't
        // process anything.
        if (!policy.getEnableManual()
            && sessionStatus == SessionStatus.PLAINTEXT)
            return evt;

        if (OtrActivator.scOtrEngine.isMessageUIDInjected(evt
            .getSourceMessage().getMessageUID()))
            // If this is a message otr4j injected earlier, don't display it,
            // this may have to change when we add support for fragmentation..
            return null;
        else
            return evt;
    }

    /*
     * Implements
     * TransformLayer#messageDeliveryFailed(MessageDeliveryFailedEvent).
     */
    public MessageDeliveryFailedEvent messageDeliveryFailed(
        MessageDeliveryFailedEvent evt)
    {
        return evt;
    }

    /*
     * Implements TransformLayer#messageDeliveryPending(MessageDeliveredEvent).
     */
    public MessageDeliveredEvent messageDeliveryPending(
        MessageDeliveredEvent evt)
    {
        Contact contact = evt.getDestinationContact();

        OtrPolicy policy = OtrActivator.scOtrEngine.getContactPolicy(contact);
        SessionStatus sessionStatus =
            OtrActivator.scOtrEngine.getSessionStatus(contact);
        // If OTR is disabled and we are not over an encrypted session, don't
        // process anything.
        if (!policy.getEnableManual()
            && sessionStatus == SessionStatus.PLAINTEXT)
            return evt;

        // If this is a message otr4j injected earlier, return the event as is.
        if (OtrActivator.scOtrEngine.isMessageUIDInjected(evt
            .getSourceMessage().getMessageUID()))
            return evt;

        // Process the outgoing message.
        String msgContent = evt.getSourceMessage().getContent();
        String processedMessageContent =
            OtrActivator.scOtrEngine.transformSending(contact, msgContent);

        if (processedMessageContent == null
            || processedMessageContent.length() < 1)
            return null;

        if (processedMessageContent.equals(msgContent))
            return evt;

        // Forge a new message based on the new contents.
        OperationSetBasicInstantMessaging imOpSet =
            contact.getProtocolProvider().getOperationSet(
                OperationSetBasicInstantMessaging.class);
        Message processedMessage =
            imOpSet.createMessage(processedMessageContent);

        // Create a new event and return.
        MessageDeliveredEvent processedEvent =
            new MessageDeliveredEvent(processedMessage, contact, evt
                .getTimestamp());

        return processedEvent;
    }

    /*
     * Implements TransformLayer#messageReceived(MessageReceivedEvent).
     */
    public MessageReceivedEvent messageReceived(MessageReceivedEvent evt)
    {
        Contact contact = evt.getSourceContact();

        OtrPolicy policy = OtrActivator.scOtrEngine.getContactPolicy(contact);
        SessionStatus sessionStatus =
            OtrActivator.scOtrEngine.getSessionStatus(contact);
        // If OTR is disabled and we are not over an encrypted session, don't
        // process anything.
        if (!policy.getEnableManual()
            && sessionStatus == SessionStatus.PLAINTEXT)
            return evt;

        // Process the incoming message.
        String msgContent = evt.getSourceMessage().getContent();

        String processedMessageContent =
            OtrActivator.scOtrEngine.transformReceiving(contact, msgContent);

        if (processedMessageContent == null
            || processedMessageContent.length() < 1)
            return null;

        if (processedMessageContent.equals(msgContent))
            return evt;

        // Forge a new message based on the new contents.
        OperationSetBasicInstantMessaging imOpSet =
            contact.getProtocolProvider().getOperationSet(
                OperationSetBasicInstantMessaging.class);
        Message processedMessage =
            imOpSet.createMessage(processedMessageContent);

        // Create a new event and return.
        MessageReceivedEvent processedEvent =
            new MessageReceivedEvent(processedMessage, contact, evt
                .getTimestamp());

        return processedEvent;
    }
}
