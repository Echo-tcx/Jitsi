/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.jabber;

import java.util.*;

import net.java.sip.communicator.impl.protocol.jabber.extensions.keepalive.*;
import net.java.sip.communicator.impl.protocol.jabber.extensions.version.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.Message;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.service.protocol.jabberconstants.*;
import net.java.sip.communicator.util.*;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.provider.*;
import org.jivesoftware.smack.util.*;
import org.jivesoftware.smackx.*;
import org.jivesoftware.smackx.packet.XHTMLExtension;

/**
 * A straightforward implementation of the basic instant messaging operation
 * set.
 *
 * @author Damian Minkov
 */
public class OperationSetBasicInstantMessagingJabberImpl
    extends AbstractOperationSetBasicInstantMessaging
{
    private static final Logger logger =
        Logger.getLogger(OperationSetBasicInstantMessagingJabberImpl.class);

    /**
     * KeepAlive interval for sending packets
     */
    private static final long KEEPALIVE_INTERVAL = 180000l; // 3 minutes

    /**
     * The interval after which a packet is considered to be lost
     */
    private static final long KEEPALIVE_WAIT = 20000l;

    private boolean keepAliveEnabled = false;

    /**
     * The task sending packets
     */
    private KeepAliveSendTask keepAliveSendTask = null;
    /**
     * The timer executing tasks on specified intervals
     */
    private Timer keepAliveTimer = new Timer();
    /**
     * The queue holding the received packets
     */
    private LinkedList receivedKeepAlivePackets = new LinkedList();

    private int failedKeepalivePackets = 0;

    /**
     * The provider that created us.
     */
    private ProtocolProviderServiceJabberImpl jabberProvider = null;

    /**
     * A reference to the persistent presence operation set that we use
     * to match incoming messages to <tt>Contact</tt>s and vice versa.
     */
    private OperationSetPersistentPresenceJabberImpl opSetPersPresence = null;
    
    /**
     * The opening BODY HTML TAG: &ltbody&gt
     */
    private static final String OPEN_BODY_TAG = "<body>";
    
    /**
     * The closing BODY HTML TAG: &ltbody&gt
     */
    private static final String CLOSE_BODY_TAG = "</body>";
    
    /**
     * The instance of the html2Txt converter that we use to send messages in 
     * both the text/html and text/plain content types.
     */
    private Html2Text html2Txt = new Html2Text();

    /**
     * Creates an instance of this operation set.
     * @param provider a ref to the <tt>ProtocolProviderServiceImpl</tt>
     * that created us and that we'll use for retrieving the underlying aim
     * connection.
     */
    OperationSetBasicInstantMessagingJabberImpl(
        ProtocolProviderServiceJabberImpl provider)
    {
        this.jabberProvider = provider;
        provider.addRegistrationStateChangeListener(new RegistrationStateListener());

        // register the KeepAlive Extension in the smack library
        ProviderManager.getInstance().addIQProvider(KeepAliveEventProvider.ELEMENT_NAME,
                                             KeepAliveEventProvider.NAMESPACE,
                                             new KeepAliveEventProvider());
    }

    /**
     * Create a Message instance for sending arbitrary MIME-encoding content.
     *
     * @param content content value
     * @param contentType the MIME-type for <tt>content</tt>
     * @return the newly created message.
     */
    public Message createMessage(String content, String contentType)
    {
        return createMessage(content, contentType, DEFAULT_MIME_ENCODING, null);
    }

    public Message createMessage(String content, String contentType,
        String encoding, String subject)
    {
        return new MessageJabberImpl(content, contentType, encoding, subject);
    }

    /**
     * Determines wheter the protocol provider (or the protocol itself) support
     * sending and receiving offline messages. Most often this method would
     * return true for protocols that support offline messages and false for
     * those that don't. It is however possible for a protocol to support these
     * messages and yet have a particular account that does not (i.e. feature
     * not enabled on the protocol server). In cases like this it is possible
     * for this method to return true even when offline messaging is not
     * supported, and then have the sendMessage method throw an
     * OperationFailedException with code - OFFLINE_MESSAGES_NOT_SUPPORTED.
     *
     * @return <tt>true</tt> if the protocol supports offline messages and
     * <tt>false</tt> otherwise.
     */
    public boolean isOfflineMessagingSupported()
    {
        return true;
    }

    /**
     * Determines wheter the protocol supports the supplied content type
     *
     * @param contentType the type we want to check
     * @return <tt>true</tt> if the protocol supports it and
     * <tt>false</tt> otherwise.
     */
    public boolean isContentTypeSupported(String contentType)
    {
        if(contentType.equals(DEFAULT_MIME_TYPE)
            || contentType.equals(HTML_MIME_TYPE))
            return true;
        else
           return false;
    }
    
    /**
     * Sends the <tt>message</tt> to the destination indicated by the
     * <tt>to</tt> contact.
     *
     * @param to the <tt>Contact</tt> to send <tt>message</tt> to
     * @param message the <tt>Message</tt> to send.
     * @throws java.lang.IllegalStateException if the underlying stack is
     * not registered and initialized.
     * @throws java.lang.IllegalArgumentException if <tt>to</tt> is not an
     * instance of ContactImpl.
     */
    public void sendInstantMessage(Contact to, Message message)
        throws IllegalStateException, IllegalArgumentException
    {
        if( !(to instanceof ContactJabberImpl) )
           throw new IllegalArgumentException(
               "The specified contact is not a Jabber contact."
               + to);

        try
        {
            assertConnected();

            org.jivesoftware.smack.MessageListener msgListener = 
                new org.jivesoftware.smack.MessageListener() {
                    public void processMessage(
                        Chat arg0, 
                        org.jivesoftware.smack.packet.Message arg1) {}
            };

            Chat chat = jabberProvider.getConnection().getChatManager()
                .createChat(to.getAddress(), msgListener);
            
            org.jivesoftware.smack.packet.Message msg = 
                new org.jivesoftware.smack.packet.Message();
            
            String content = message.getContent();
            
            if(message.getContentType().equals(HTML_MIME_TYPE))
            {
                msg.setBody(html2Txt.extractText(content));
                
                // Add the XHTML text to the message
                XHTMLManager.addBody(msg, 
                    OPEN_BODY_TAG + content + CLOSE_BODY_TAG);
            }
            else
            {
                // this is plain text so keep it as it is.
                msg.setBody(content);
            }

            msg.addExtension(new Version());
            
            MessageEventManager.
                addNotificationsRequests(msg, true, false, false, true);
             
            chat.sendMessage(msg);

            MessageDeliveredEvent msgDeliveredEvt
                = new MessageDeliveredEvent(
                    message, to, new Date());

            fireMessageEvent(msgDeliveredEvt);
        }
        catch (XMPPException ex)
        {
            logger.error("message not send", ex);
        }
    }

    /**
     * Utility method throwing an exception if the stack is not properly
     * initialized.
     * @throws java.lang.IllegalStateException if the underlying stack is
     * not registered and initialized.
     */
    private void assertConnected() throws IllegalStateException
    {
        if (jabberProvider == null)
            throw new IllegalStateException(
                "The provider must be non-null and signed on the "
                +"service before being able to communicate.");
        if (!jabberProvider.isRegistered())
            throw new IllegalStateException(
                "The provider must be signed on the service before "
                +"being able to communicate.");
    }

    /**
     * Our listener that will tell us when we're registered to
     */
    private class RegistrationStateListener
        implements RegistrationStateChangeListener
    {
        /**
         * The method is called by a ProtocolProvider implementation whenver
         * a change in the registration state of the corresponding provider had
         * occurred.
         * @param evt ProviderStatusChangeEvent the event describing the status
         * change.
         */
        public void registrationStateChanged(RegistrationStateChangeEvent evt)
        {
            logger.debug("The provider changed state from: "
                         + evt.getOldState()
                         + " to: " + evt.getNewState());

            if (evt.getNewState() == RegistrationState.REGISTERED)
            {
                opSetPersPresence =
                    (OperationSetPersistentPresenceJabberImpl) jabberProvider
                        .getOperationSet(OperationSetPersistentPresence.class);

                jabberProvider.getConnection().addPacketListener(
                        new SmackMessageListener(),
                        new AndFilter(
                            new PacketFilter[]{new GroupMessagePacketFilter(),
                            new PacketTypeFilter(
                            org.jivesoftware.smack.packet.Message.class)}));

                // run keepalive thread
                if(keepAliveSendTask == null && keepAliveEnabled)
                {
                    jabberProvider.getConnection().addPacketListener(
                        new KeepalivePacketListener(),
                        new PacketTypeFilter(
                            KeepAliveEvent.class));
                    
                    keepAliveSendTask = new KeepAliveSendTask();

                    keepAliveTimer.scheduleAtFixedRate(
                        keepAliveSendTask, KEEPALIVE_INTERVAL, KEEPALIVE_INTERVAL);
                }
            }
        }
    }
    
    /**
     * The listener that we use in order to handle incoming messages.
     */
    private class SmackMessageListener
        implements PacketListener
    {
        public void processPacket(Packet packet)
        {
            if(!(packet instanceof org.jivesoftware.smack.packet.Message))
                return;

            org.jivesoftware.smack.packet.Message msg =
                (org.jivesoftware.smack.packet.Message)packet;

            if(msg.getBody() == null)
                return;

            String fromUserID = StringUtils.parseBareAddress(msg.getFrom());

            if(logger.isDebugEnabled())
            {
                logger.debug("Received from "
                             + fromUserID
                             + " the message "
                             + msg.toXML());
            }

            Message newMessage = createMessage(msg.getBody());
            
            //check if the message is available in xhtml
            PacketExtension ext = msg.getExtension("http://jabber.org/protocol/xhtml-im");
            
            if(ext != null)
            {
                XHTMLExtension xhtmlExt 
                    = (XHTMLExtension)ext;
                
                //parse all bodies
                Iterator bodies = xhtmlExt.getBodies();
                StringBuffer messageBuff = new StringBuffer();
                while (bodies.hasNext())
                {
                    String body = (String)bodies.next();
                    messageBuff.append(body);
                }

                if (messageBuff.length() > 0)
                    newMessage =
                        createMessage(messageBuff.toString(), HTML_MIME_TYPE);
            }

            Contact sourceContact =
                opSetPersPresence.findContactByID(fromUserID);

            if(msg.getType() == org.jivesoftware.smack.packet.Message.Type.error)
            {
                logger.info("Message error received from " + fromUserID);

                int errorCode = packet.getError().getCode();
                int errorResultCode = MessageDeliveryFailedEvent.UNKNOWN_ERROR;

                if(errorCode == 503)
                {
                    org.jivesoftware.smackx.packet.MessageEvent msgEvent =
                        (org.jivesoftware.smackx.packet.MessageEvent)
                            packet.getExtension("x", "jabber:x:event");
                    if(msgEvent != null && msgEvent.isOffline())
                    {
                        errorResultCode =
                            MessageDeliveryFailedEvent.OFFLINE_MESSAGES_NOT_SUPPORTED;
                    }
                }

                MessageDeliveryFailedEvent ev =
                    new MessageDeliveryFailedEvent(newMessage,
                                                   sourceContact,
                                                   errorResultCode,
                                                   new Date());
                fireMessageEvent(ev);
                return;
            }

            // In the second condition we filter all group chat messages,
            // because they are managed by the multi user chat operation set.
            if(sourceContact == null)
            {
                logger.debug("received a message from an unknown contact: "
                                   + fromUserID);
                //create the volatile contact
                sourceContact = opSetPersPresence
                    .createVolatileContact(fromUserID);
            }

            MessageReceivedEvent msgReceivedEvt
                = new MessageReceivedEvent(
                    newMessage, sourceContact , new Date() );

            fireMessageEvent(msgReceivedEvt);
        }
    }
    
    /**
     * Receives incoming KeepAlive Packets
     */ 
    private class KeepalivePacketListener
        implements PacketListener
    {
        public void processPacket(Packet packet)
        {
            if(packet != null &&  !(packet instanceof KeepAliveEvent))
                return;
            
            KeepAliveEvent keepAliveEvent = (KeepAliveEvent)packet;
            
            if(logger.isDebugEnabled())
            {
                logger.debug("Received keepAliveEvent from "
                             + keepAliveEvent.getFromUserID()
                             + " the message : "
                             + keepAliveEvent.toXML());
            }
            
            receivedKeepAlivePackets.addLast(keepAliveEvent);
        }
    }

    /**
     * Task sending packets on intervals.
     * The task is runned on specified intervals by the keepAliveTimer
     */
    private class KeepAliveSendTask
        extends TimerTask
    {
        public void run()
        {
            // if we are not registerd do nothing
            if(!jabberProvider.isRegistered())
            {
                logger.trace("provider not registered. "
                             +"won't send keep alive. acc.id="
                             + jabberProvider.getAccountID()
                                .getAccountUniqueID());
                return;
            }

            KeepAliveEvent keepAliveEvent = 
                new KeepAliveEvent(jabberProvider.getConnection().getUser());

            keepAliveEvent.setSrcOpSetHash(
                OperationSetBasicInstantMessagingJabberImpl.this.hashCode());
            keepAliveEvent.setSrcProviderHash(jabberProvider.hashCode());

            // schedule the check task
            keepAliveTimer.schedule(
                new KeepAliveCheckTask(), KEEPALIVE_WAIT);

            logger.trace(
                "send keepalive for acc: "
                + jabberProvider.getAccountID().getAccountUniqueID());

            jabberProvider.getConnection().sendPacket(keepAliveEvent);
        }
    }

    /**
     * Check if the first received packet in the queue
     * is ok and if its not or the queue has no received packets
     * the this means there is some network problem, so fire event
     */
    private class KeepAliveCheckTask
        extends TimerTask
    {
        public void run()
        {
            try
            {
                // check till we find a correct message
                // or if NoSuchElementException is thrown
                // there is no message
                while(!checkFirstPacket());
                failedKeepalivePackets = 0;
            }
            catch (NoSuchElementException ex)
            {
                logger.error(
                    "Did not receive last keep alive packet for account "
                    + jabberProvider.getAccountID().getAccountUniqueID());

                failedKeepalivePackets++;

                // if we have 3 keepalive fails then unregister
                if(failedKeepalivePackets == 3)
                {
                    logger.error("unregistering.");
//                    fireUnregisterd();
                    jabberProvider
                        .reregister(SecurityAuthority.CONNECTION_FAILED);
                    failedKeepalivePackets = 0;
                }
            }
        }

        /**
         * Checks whether first packet in queue is ok
         * @return boolean
         * @throws NoSuchElementException
         */
        boolean checkFirstPacket()
            throws NoSuchElementException
        {
            KeepAliveEvent receivedEvent =
                    (KeepAliveEvent)receivedKeepAlivePackets.removeLast();

            if(jabberProvider.hashCode() != receivedEvent.getSrcProviderHash() ||
                    OperationSetBasicInstantMessagingJabberImpl.this.hashCode() !=
                    receivedEvent.getSrcOpSetHash() ||
                    !jabberProvider.getAccountID().getUserID().
                                        equals(receivedEvent.getFromUserID()) )
                return false;
            else
                return true;
        }

        /**
         * Fire Unregistered event
         */
        void fireUnregisterd()
        {
            jabberProvider.fireRegistrationStateChanged(
                jabberProvider.getRegistrationState(),
                RegistrationState.CONNECTION_FAILED,
                RegistrationStateChangeEvent.REASON_INTERNAL_ERROR, null);

            opSetPersPresence.fireProviderPresenceStatusChangeEvent(
                opSetPersPresence.getPresenceStatus(), jabberProvider
                    .getJabberStatusEnum().getStatus(JabberStatusEnum.OFFLINE));
        }
    }

    /**
     * Set is keep alive sendin packets to be enabled
     * @param keepAliveEnabled boolean
     */
    public void setKeepAliveEnabled(boolean keepAliveEnabled)
    {
        this.keepAliveEnabled = keepAliveEnabled;
    }
    
    private class GroupMessagePacketFilter implements PacketFilter
    {   
        public boolean accept(Packet packet)
        {            
            if(!(packet instanceof org.jivesoftware.smack.packet.Message))
                return false;
            
            org.jivesoftware.smack.packet.Message msg
                = (org.jivesoftware.smack.packet.Message) packet;
            
            if(msg.getType().equals(
                org.jivesoftware.smack.packet.Message.Type.groupchat))
                return false;
            
            return true;
        }        
    }
}
