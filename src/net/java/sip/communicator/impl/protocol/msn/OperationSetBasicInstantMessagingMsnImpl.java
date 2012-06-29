/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.msn;

import java.text.*;
import java.util.*;

import net.java.sip.communicator.impl.protocol.msn.mail.utils.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;
import net.sf.jml.*;
import net.sf.jml.event.*;
import net.sf.jml.message.*;

/**
 * A straightforward implementation of the basic instant messaging operation
 * set.
 *
 * @author Damian Minkov
 */
public class OperationSetBasicInstantMessagingMsnImpl
    extends AbstractOperationSetBasicInstantMessaging
{
    private static final Logger logger
        = Logger.getLogger(OperationSetBasicInstantMessagingMsnImpl.class);

    /**
     * The provider that created us.
     */
    private final ProtocolProviderServiceMsnImpl msnProvider;

    /**
     * A reference to the persistent presence operation set that we use
     * to match incoming messages to <tt>Contact</tt>s and vice versa.
     */
    private OperationSetPersistentPresenceMsnImpl opSetPersPresence = null;

    private final OperationSetAdHocMultiUserChatMsnImpl opSetMuc;

    /**
     * The thread that will send messages.
     */
    private SenderThread senderThread = null;

    /**
     * Creates an instance of this operation set.
     * @param provider a ref to the <tt>ProtocolProviderServiceImpl</tt>
     * that created us and that we'll use for retrieving the underlying aim
     * connection.
     */
    OperationSetBasicInstantMessagingMsnImpl(
        ProtocolProviderServiceMsnImpl provider)
    {
        this.msnProvider = provider;
        opSetMuc
            = (OperationSetAdHocMultiUserChatMsnImpl)
                msnProvider
                    .getOperationSet(OperationSetAdHocMultiUserChat.class);
        provider.addRegistrationStateChangeListener(new RegistrationStateListener());
    }

    /**
     * Determines whether the protocol provider (or the protocol itself) support
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
     * Determines whether the protocol supports the supplied content type
     *
     * @param contentType the type we want to check
     * @return <tt>true</tt> if the protocol supports it and
     * <tt>false</tt> otherwise.
     */
    public boolean isContentTypeSupported(String contentType)
    {
        return DEFAULT_MIME_TYPE.equals(contentType);
    }

    public Message createMessage(String content, String contentType,
        String encoding, String subject)
    {
        return new MessageMsnImpl(content, contentType, encoding, subject);
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
    public void sendInstantMessage(final Contact to, Message message)
        throws IllegalStateException, IllegalArgumentException
    {
        assertConnected();

        if( !(to instanceof ContactMsnImpl) )
           throw new IllegalArgumentException(
               "The specified contact is not an MSN contact."
               + to);

        final MessageDeliveredEvent msgDeliveryPendingEvt
            = messageDeliveryPendingTransform(
                new MessageDeliveredEvent(message, to));

        if (msgDeliveryPendingEvt == null)
            return;

        MessageDeliveredEvent msgDeliveredEvt
            = new MessageDeliveredEvent(message, to);

        fireMessageEvent(msgDeliveredEvt);

        // send message in separate thread so we won't block ui if
        // it takes time.
        if(senderThread == null)
        {
            senderThread = new SenderThread();
            senderThread.start();
        }

        senderThread.sendMessage(
                (ContactMsnImpl)to,
                msgDeliveryPendingEvt.getSourceMessage().getContent());
    }

    /**
     * Utility method throwing an exception if the stack is not properly
     * initialized.
     * @throws java.lang.IllegalStateException if the underlying stack is
     * not registered and initialized.
     */
    private void assertConnected() throws IllegalStateException
    {
        if (msnProvider == null)
            throw new IllegalStateException(
                "The provider must be non-null and signed on the "
                +"service before being able to communicate.");
        if (!msnProvider.isRegistered())
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
         * The method is called by a ProtocolProvider implementation whenever
         * a change in the registration state of the corresponding provider had
         * occurred.
         * @param evt ProviderStatusChangeEvent the event describing the status
         * change.
         */
        public void registrationStateChanged(RegistrationStateChangeEvent evt)
        {
            if (logger.isDebugEnabled())
                logger.debug("The provider changed state from: "
                         + evt.getOldState()
                         + " to: " + evt.getNewState());

            if (evt.getNewState() == RegistrationState.REGISTERED)
            {
                opSetPersPresence =
                    (OperationSetPersistentPresenceMsnImpl) msnProvider
                        .getOperationSet(OperationSetPersistentPresence.class);

                MsnMessenger msnMessenger = msnProvider.getMessenger();

                if(msnMessenger != null)
                {
                    MsnMessageListener listener = new MsnMessageListener();
                    msnMessenger.addMessageListener(listener);
                    msnMessenger.addEmailListener(listener);
                }
                else if(logger.isInfoEnabled())
                    logger.info("Registered but msnMessenger is missing!",
                                new Exception());
            }
            else if(evt.getNewState() == RegistrationState.UNREGISTERED
                || evt.getNewState() == RegistrationState.CONNECTION_FAILED
                || evt.getNewState() == RegistrationState.AUTHENTICATION_FAILED)
            {
                if(senderThread != null)
                {
                    senderThread.stopRunning();
                    senderThread = null;
                }
            }
        }
    }

    private class MsnMessageListener
        extends MsnMessageAdapter
        implements MsnEmailListener
    {
        public void instantMessageReceived(MsnSwitchboard switchboard,
                                           MsnInstantMessage message,
                                           MsnContact contact)
        {
           // FILTER OUT THE GROUP MESSAGES
           if (opSetMuc.isGroupChatMessage(switchboard))
               return;

           Message newMessage = createMessage(message.getContent());
            Contact sourceContact = opSetPersPresence.
                findContactByID(contact.getEmail().getEmailAddress());

            if(sourceContact == null)
            {
                if (logger.isDebugEnabled())
                    logger.debug("received a message from an unknown contact: "
                                   + contact);
                //create the volatile contact
                sourceContact = opSetPersPresence.
                    createVolatileContact(contact);
            }

            MessageReceivedEvent msgReceivedEvt
                = new MessageReceivedEvent(
                    newMessage, sourceContact , System.currentTimeMillis() );
    
            // msgReceivedEvt = messageReceivedTransform(msgReceivedEvt);
            
            if (msgReceivedEvt != null)
                fireMessageEvent(msgReceivedEvt);
        }
        
        /**
         * Received offline text message.
         * 
         * @param body of message
         * @param contentType of message
         * @param encoding of message
         * @param contact the user who sent this message
         */
        public void offlineMessageReceived(String body,
                                           String contentType, 
                                           String encoding,
                                           MsnContact contact)
        {
            Message newMessage =
                createMessage(body, contentType, encoding, null);

            Contact sourceContact = opSetPersPresence.
                findContactByID(contact.getEmail().getEmailAddress());

            if(sourceContact == null)
            {
                if (logger.isDebugEnabled())
                    logger.debug("received a message from an unknown contact: "
                                   + contact);
                //create the volatile contact
                sourceContact = opSetPersPresence.
                    createVolatileContact(contact);
            }

            MessageReceivedEvent msgReceivedEvt
                = new MessageReceivedEvent(
                    newMessage, sourceContact , System.currentTimeMillis() );

            fireMessageEvent(msgReceivedEvt);
        }

        public void initialEmailNotificationReceived(MsnSwitchboard switchboard,
                                                     MsnEmailInitMessage message, 
                                                     MsnContact contact)
        {
        }

        public void initialEmailDataReceived(MsnSwitchboard switchboard,
                                             MsnEmailInitEmailData message,
                                             MsnContact contact)
        {
        }

        public void newEmailNotificationReceived(MsnSwitchboard switchboard,
                                                 MsnEmailNotifyMessage message,
                                                 MsnContact contact)
        {
            // we don't process incoming event without email.
            String messageFromAddr = message.getFromAddr();
            if ((messageFromAddr == null)
                    || (messageFromAddr.indexOf('@') < 0))
                return;

            String subject = message.getSubject();

            try
            {
                subject = MimeUtility.decodeText(subject);
            }
            catch (Exception ex)
            {
                logger.warn("Failed to decode the subject of a new e-mail", ex);
            }

            String messageFrom = message.getFrom();
            Message newMailMessage = new MessageMsnImpl(
                    MessageFormat.format(
                        MsnActivator.getResources()
                                .getI18NString("service.gui.NEW_MAIL"),
                        messageFrom,
                        messageFromAddr,
                        subject,
                        "" /*
                            * TODO What is {3} meant for? The Yahoo! protocol
                            * implementation seems to put a link to a mail login
                            * page.
                            */),
                     HTML_MIME_TYPE,
                     DEFAULT_MIME_ENCODING,
                     subject);

             Contact sourceContact
                 = opSetPersPresence.findContactByID(messageFromAddr);

             if (sourceContact == null)
             {
                 if (logger.isDebugEnabled())
                     logger.debug("received a new mail from an unknown contact: "
                                    + messageFrom
                                    + " &lt;" + messageFromAddr + "&gt;");
                 //create the volatile contact
                 String id = contact.getId();
                 Email email = contact.getEmail();
                 String displayName = contact.getDisplayName();

                 if (id == null)
                     id = messageFromAddr;
                 if (email == null)
                     email = Email.parseStr(messageFromAddr);
                 if (displayName == null)
                     displayName = messageFrom;
                 sourceContact
                     = opSetPersPresence.createVolatileContact(id, email, displayName);
             }

             MessageReceivedEvent msgReceivedEvt
                 = new MessageReceivedEvent(
                     newMailMessage,
                     sourceContact,
                     System.currentTimeMillis(),
                     MessageReceivedEvent.SYSTEM_MESSAGE_RECEIVED);

             fireMessageEvent(msgReceivedEvt);
        }

        public void activityEmailNotificationReceived(MsnSwitchboard switchboard,
                                                      MsnEmailActivityMessage message,
                                                      MsnContact contact)
        {
        }
    }

    /**
     * Sends instant messages in separate thread so we don't block
     * our calling thread.
     * When sending offline messages msn uses soap
     * and http and xml exchange can be time consuming. 
     */
    private class SenderThread
        extends Thread
    {
        /**
         * start/stop indicator.
         */
        private boolean stopped = true;

        /**
         * List of messages queued to be sent.
         */
        private List<MessageToSend> messagesToSend =
                new ArrayList<MessageToSend>();

        /**
         * Sends instant messages in separate thread so we don't block
         * our calling thread.
         */
        public void run()
        {
            stopped = false;

            while(!stopped)
            {
                MessageToSend msgToSend = null;

                synchronized(this)
                {
                    if(messagesToSend.isEmpty())
                    {
                        try
                        {
                            wait();

                        }
                        catch (InterruptedException iex)
                        {
                        }
                    }

                    if(!messagesToSend.isEmpty())
                        msgToSend = messagesToSend.remove(0);
                }

                if(msgToSend != null)
                {
                    try
                    {
                        msnProvider.getMessenger().sendText(
                                msgToSend.to.getSourceContact().getEmail(),
                                msgToSend.content);
                    }
                    catch(Throwable t)
                    {
                        fireMessageDeliveryFailed(
                            createMessage(msgToSend.content,
                                    DEFAULT_MIME_TYPE,
                                    DEFAULT_MIME_ENCODING,
                                    null),
                            msgToSend.to,
                            MessageDeliveryFailedEvent.UNKNOWN_ERROR);
                    }
                }
            }
        }

        /**
         * Interrupts this sender so that it would no longer send messages.
         */
        public synchronized void stopRunning()
        {
            stopped = true;
            notifyAll();
        }

        /**
         * Schedule new message to be sent.
         * @param to destination.
         * @param content content.
         */
        public synchronized void sendMessage(ContactMsnImpl to, String content)
        {
            messagesToSend.add(new MessageToSend(to , content));
            notifyAll();
        }

        /**
         * Structure used to store data to be sent. 
         */
        private class MessageToSend
        {
            /**
             * Message sent to.
             */
            private ContactMsnImpl to;

            /**
             * Content of message.
             */
            private String content;

            /**
             * Creates MessageToSend.
             * @param to contact to.
             * @param content content to be sent.
             */
            MessageToSend(ContactMsnImpl to, String content)
            {
                this.to = to;
                this.content = content;
            }
        }
    }
}
