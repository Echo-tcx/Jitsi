/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.contactlist;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.customcontrols.*;
import net.java.sip.communicator.impl.gui.event.*;
import net.java.sip.communicator.impl.gui.main.*;
import net.java.sip.communicator.impl.gui.main.chat.*;
import net.java.sip.communicator.service.contacteventhandler.*;
import net.java.sip.communicator.service.contactlist.*;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.gui.Container;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.swing.*;

import org.osgi.framework.*;

/**
 * The contactlist panel not only contains the contact list but it has the role
 * of a message dispatcher. It process all sent and received messages as well as
 * all typing notifications. Here are managed all contact list mouse events.
 *
 * @author Yana Stamcheva
 */
public class ContactListPane
    extends SCScrollPane
    implements  MessageListener,
                TypingNotificationsListener,
                FileTransferListener,
                ContactListListener,
                PluginComponentListener
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 0L;

    private final MainFrame mainFrame;

    private TreeContactList contactList;

    private final TypingTimer typingTimer = new TypingTimer();

    private CommonRightButtonMenu commonRightButtonMenu;

    private final Logger logger = Logger.getLogger(ContactListPane.class);

    private final ChatWindowManager chatWindowManager;

    /**
     * Creates the contactlist scroll panel defining the parent frame.
     *
     * @param mainFrame The parent frame.
     */
    public ContactListPane(MainFrame mainFrame)
    {
        this.mainFrame = mainFrame;

        this.chatWindowManager
            = GuiActivator.getUIService().getChatWindowManager();

        this.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));

        this.initPluginComponents();
    }

    /**
     * Initializes the contact list.
     *
     * @param contactListService The MetaContactListService which will be used
     *            for a contact list data model.
     */
    public void initList(MetaContactListService contactListService)
    {
        this.contactList = new TreeContactList();
        // We should first set the contact list to the GuiActivator, so that
        // anybody could get it from there.
        GuiActivator.setContactList(contactList);

        // By default we set the current filter to be the presence filter.
        contactList.applyFilter(TreeContactList.presenceFilter);

        TransparentPanel transparentPanel
            = new TransparentPanel(new BorderLayout());

        transparentPanel.add(contactList, BorderLayout.NORTH);

        this.setViewportView(transparentPanel);

        transparentPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        this.contactList.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        this.contactList.addContactListListener(this);
        this.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0)
                {
                    commonRightButtonMenu = new CommonRightButtonMenu(mainFrame);

                    commonRightButtonMenu.setInvoker(ContactListPane.this);

                    commonRightButtonMenu.setLocation(e.getX()
                            + mainFrame.getX() + 5, e.getY() + mainFrame.getY()
                            + 105);

                    commonRightButtonMenu.setVisible(true);
                }
            }
        });
    }

    /**
     * Returns the contact list.
     *
     * @return the contact list
     */
    public TreeContactList getContactList()
    {
        return this.contactList;
    }

    /**
     * Implements the ContactListListener.contactSelected method.
     * @param evt the <tt>ContactListEvent</tt> that notified us
     */
    public void contactClicked(ContactListEvent evt)
    {
        // We're interested only in two click events.
        if (evt.getClickCount() < 2)
            return;

        UIContact descriptor = evt.getSourceContact();

        // We're currently only interested in MetaContacts.
        if (!(descriptor.getDescriptor() instanceof MetaContact))
            return;

        MetaContact metaContact = (MetaContact) descriptor.getDescriptor();

        // Searching for the right proto contact to use as default for the
        // chat conversation.
        Contact defaultContact = metaContact.getDefaultContact(
                                    OperationSetBasicInstantMessaging.class);

        // do nothing
        if(defaultContact == null)
            return;

        ProtocolProviderService defaultProvider
            = defaultContact.getProtocolProvider();

        OperationSetBasicInstantMessaging
            defaultIM = defaultProvider.getOperationSet(
                          OperationSetBasicInstantMessaging.class);

        ProtocolProviderService protoContactProvider;
        OperationSetBasicInstantMessaging protoContactIM;

        boolean isOfflineMessagingSupported
            = defaultIM != null && !defaultIM.isOfflineMessagingSupported();

        if (defaultContact.getPresenceStatus().getStatus() < 1
                && (!isOfflineMessagingSupported
                    || !defaultProvider.isRegistered()))
        {
            Iterator<Contact> protoContacts = metaContact.getContacts();

            while(protoContacts.hasNext())
            {
                Contact contact = protoContacts.next();

                protoContactProvider = contact.getProtocolProvider();

                protoContactIM = protoContactProvider.getOperationSet(
                                    OperationSetBasicInstantMessaging.class);

                if(protoContactIM != null
                        && protoContactIM.isOfflineMessagingSupported()
                        && protoContactProvider.isRegistered())
                {
                    defaultContact = contact;
                }
            }
        }

        ContactEventHandler contactHandler = mainFrame
            .getContactHandler(defaultContact.getProtocolProvider());

        contactHandler.contactClicked(defaultContact, evt.getClickCount());
    }

    /**
     * Implements the ContactListListener.groupSelected method.
     * @param evt the <tt>ContactListEvent</tt> that notified us
     */
    public void groupClicked(ContactListEvent evt)
    {}

    /**
     * When a message is received determines whether to open a new chat window
     * or chat window tab, or to indicate that a message is received from a
     * contact which already has an open chat. When the chat is found checks if
     * in mode "Auto popup enabled" and if this is the case shows the message in
     * the appropriate chat panel.
     *
     * @param evt the event containing details on the received message
     */
    public void messageReceived(MessageReceivedEvent evt)
    {
        if (logger.isTraceEnabled())
            logger.trace("MESSAGE RECEIVED from contact: "
            + evt.getSourceContact().getAddress());

        Contact protocolContact = evt.getSourceContact();
        Message message = evt.getSourceMessage();
        int eventType = evt.getEventType();
        MetaContact metaContact = GuiActivator.getContactListService()
                .findMetaContactByContact(protocolContact);

        if(metaContact != null)
        {
            messageReceived(protocolContact,
                metaContact, message, eventType, evt.getTimestamp());
        }
        else
        {
            if (logger.isTraceEnabled())
                logger.trace("MetaContact not found for protocol contact: "
                    + protocolContact + ".");
        }
    }

    /**
     * When a message is received determines whether to open a new chat window
     * or chat window tab, or to indicate that a message is received from a
     * contact which already has an open chat. When the chat is found checks if
     * in mode "Auto popup enabled" and if this is the case shows the message in
     * the appropriate chat panel.
     *
     * @param protocolContact the source contact of the event
     * @param metaContact the metacontact containing <tt>protocolContact</tt>
     * @param message the message to deliver
     * @param eventType the event type
     * @param timestamp the timestamp of the event
     */
    private void messageReceived(final Contact protocolContact,
                                 final MetaContact metaContact,
                                 final Message message,
                                 final int eventType,
                                 final long timestamp)
    {
        if(!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    messageReceived(protocolContact,
                        metaContact, message, eventType, timestamp);
                }
            });
            return;
        }

        // Obtain the corresponding chat panel.
        final ChatPanel chatPanel
            = chatWindowManager.getContactChat( metaContact,
                                                protocolContact,
                                                message.getMessageUID());

        // Show an envelope on the sender contact in the contact list and
        // in the systray.
        if (!chatPanel.isChatFocused())
            contactList.setActiveContact(metaContact, true);

        // Distinguish the message type, depending on the type of event that
        // we have received.
        String messageType = null;

        if(eventType == MessageReceivedEvent.CONVERSATION_MESSAGE_RECEIVED)
        {
            messageType = Chat.INCOMING_MESSAGE;
        }
        else if(eventType == MessageReceivedEvent.SYSTEM_MESSAGE_RECEIVED)
        {
            messageType = Chat.SYSTEM_MESSAGE;
        }
        else if(eventType == MessageReceivedEvent.SMS_MESSAGE_RECEIVED)
        {
            messageType = Chat.SMS_MESSAGE;
        }

        chatPanel.addMessage(
            protocolContact.getAddress(),
            protocolContact.getDisplayName(),
            timestamp,
            messageType,
            message.getContent(),
            message.getContentType());

        chatWindowManager.openChat(chatPanel, false);

        ChatTransport chatTransport
            = chatPanel.getChatSession()
                .findChatTransportForDescriptor(protocolContact);

        chatPanel.setSelectedChatTransport(chatTransport);
    }

    /**
     * When a sent message is delivered shows it in the chat conversation panel.
     *
     * @param evt the event containing details on the message delivery
     */
    public void messageDelivered(MessageDeliveredEvent evt)
    {
        Contact contact = evt.getDestinationContact();
        MetaContact metaContact = GuiActivator.getContactListService()
            .findMetaContactByContact(contact);

        if (logger.isTraceEnabled())
            logger.trace("MESSAGE DELIVERED to contact: " + contact.getAddress());

        ChatPanel chatPanel
            = chatWindowManager.getContactChat(metaContact, false);

        if (chatPanel != null)
        {
            Message msg = evt.getSourceMessage();
            ProtocolProviderService protocolProvider
                = contact.getProtocolProvider();

            if (logger.isTraceEnabled())
                logger.trace(
                "MESSAGE DELIVERED: process message to chat for contact: "
                + contact.getAddress()
                + " MESSAGE: " + msg.getContent());

            chatPanel.addMessage(
                this.mainFrame.getAccountAddress(protocolProvider),
                this.mainFrame.getAccountDisplayName(protocolProvider),
                evt.getTimestamp(),
                Chat.OUTGOING_MESSAGE,
                msg.getContent(),
                msg.getContentType());
        }
    }

    /**
     * Shows a warning message to the user when message delivery has failed.
     *
     * @param evt the event containing details on the message delivery failure
     */
    public void messageDeliveryFailed(MessageDeliveryFailedEvent evt)
    {
        logger.error(evt.getReason());

        String errorMsg = null;

        Message sourceMessage = (Message) evt.getSource();

        Contact sourceContact = evt.getDestinationContact();

        MetaContact metaContact = GuiActivator.getContactListService()
            .findMetaContactByContact(sourceContact);

        if (evt.getErrorCode()
                == MessageDeliveryFailedEvent.OFFLINE_MESSAGES_NOT_SUPPORTED)
        {
            errorMsg = GuiActivator.getResources().getI18NString(
                    "service.gui.MSG_DELIVERY_NOT_SUPPORTED",
                    new String[] {sourceContact.getDisplayName()});
        }
        else if (evt.getErrorCode()
                == MessageDeliveryFailedEvent.NETWORK_FAILURE)
        {
            errorMsg = GuiActivator.getResources().getI18NString(
                    "service.gui.MSG_NOT_DELIVERED");
        }
        else if (evt.getErrorCode()
                == MessageDeliveryFailedEvent.PROVIDER_NOT_REGISTERED)
        {
            errorMsg = GuiActivator.getResources().getI18NString(
                    "service.gui.MSG_SEND_CONNECTION_PROBLEM");
        }
        else if (evt.getErrorCode()
                == MessageDeliveryFailedEvent.INTERNAL_ERROR)
        {
            errorMsg = GuiActivator.getResources().getI18NString(
                    "service.gui.MSG_DELIVERY_INTERNAL_ERROR");
        }
        else
        {
            errorMsg = GuiActivator.getResources().getI18NString(
                    "service.gui.MSG_DELIVERY_ERROR");
        }

        String reason = evt.getReason();
        if (reason != null)
            errorMsg += " " + GuiActivator.getResources().getI18NString(
                "service.gui.ERROR_WAS",
                new String[]{reason});

        ChatPanel chatPanel
            = chatWindowManager.getContactChat(metaContact, sourceContact);

        chatPanel.addMessage(
                sourceContact.getAddress(),
                metaContact.getDisplayName(),
                System.currentTimeMillis(),
                Chat.OUTGOING_MESSAGE,
                sourceMessage.getContent(),
                sourceMessage.getContentType());

        chatPanel.addErrorMessage(
                metaContact.getDisplayName(),
                errorMsg);

        chatWindowManager.openChat(chatPanel, false);
    }

    /**
     * Informs the user what is the typing state of his chat contacts.
     *
     * @param evt the event containing details on the typing notification
     */
    public void typingNotificationReceived(TypingNotificationEvent evt)
    {
        if (typingTimer.isRunning())
            typingTimer.stop();

        String notificationMsg = "";

        MetaContact metaContact = GuiActivator.getContactListService()
                .findMetaContactByContact(evt.getSourceContact());
        String contactName = metaContact.getDisplayName() + " ";

        if (contactName.equals(""))
        {
            contactName = GuiActivator.getResources()
                .getI18NString("service.gui.UNKNOWN") + " ";
        }

        int typingState = evt.getTypingState();

        ChatPanel chatPanel
            = chatWindowManager.getContactChat(metaContact, false);

        if (typingState == OperationSetTypingNotifications.STATE_TYPING)
        {
            notificationMsg
                = GuiActivator.getResources().getI18NString(
                    "service.gui.CONTACT_TYPING",
                    new String[]{contactName});

            // Proactive typing notification
            if (!chatWindowManager.isChatOpenedFor(metaContact))
            {
                return;
            }

            if (chatPanel != null)
                chatPanel.addTypingNotification(notificationMsg);

            typingTimer.setMetaContact(metaContact);
            typingTimer.start();
        }
        else if (typingState == OperationSetTypingNotifications.STATE_PAUSED)
        {
            notificationMsg = GuiActivator.getResources().getI18NString(
                    "service.gui.CONTACT_PAUSED_TYPING",
                    new String[]{contactName});

            if (chatPanel != null)
                chatPanel.addTypingNotification(notificationMsg);

            typingTimer.setMetaContact(metaContact);
            typingTimer.start();
        }
        else
        {
            if (chatPanel != null)
                chatPanel.removeTypingNotification();
        }
    }

    /**
     * When a request has been received we show it to the user through the
     * chat session renderer.
     *
     * @param event <tt>FileTransferRequestEvent</tt>
     * @see FileTransferListener#fileTransferRequestReceived(FileTransferRequestEvent)
     */
    public void fileTransferRequestReceived(FileTransferRequestEvent event)
    {
        IncomingFileTransferRequest request = event.getRequest();

        Contact sourceContact = request.getSender();

        MetaContact metaContact = GuiActivator.getContactListService()
            .findMetaContactByContact(sourceContact);

        final ChatPanel chatPanel
            = chatWindowManager.getContactChat(metaContact, sourceContact);

        chatPanel.addIncomingFileTransferRequest(
            event.getFileTransferOperationSet(), request, event.getTimestamp());

        ChatTransport chatTransport
            = chatPanel.getChatSession()
                .findChatTransportForDescriptor(sourceContact);

        chatPanel.setSelectedChatTransport(chatTransport);

        // Opens the chat panel with the new message in the UI thread.
        chatWindowManager.openChat(chatPanel, false);
    }

    /**
     * Nothing to do here, because we already know when a file transfer is
     * created.
     * @param event the <tt>FileTransferCreatedEvent</tt> that notified us
     */
    public void fileTransferCreated(FileTransferCreatedEvent event)
    {}

    /**
     * Called when a new <tt>IncomingFileTransferRequest</tt> has been rejected.
     * Nothing to do here, because we are the one who rejects the request.
     *
     * @param event the <tt>FileTransferRequestEvent</tt> containing the
     * received request which was rejected.
     */
    public void fileTransferRequestRejected(FileTransferRequestEvent event)
    {
    }

    /**
     * Called when an <tt>IncomingFileTransferRequest</tt> has been canceled
     * from the contact who sent it.
     *
     * @param event the <tt>FileTransferRequestEvent</tt> containing the
     * request which was canceled.
     */
    public void fileTransferRequestCanceled(FileTransferRequestEvent event)
    {
    }

    /**
     * Returns the right button menu of the contact list.
     * @return the right button menu of the contact list
     */
    public CommonRightButtonMenu getCommonRightButtonMenu()
    {
        return commonRightButtonMenu;
    }

    /**
     * The TypingTimer is started after a PAUSED typing notification is
     * received. It waits 5 seconds and if no other typing event occurs removes
     * the PAUSED message from the chat status panel.
     */
    private class TypingTimer extends Timer
    {
        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 0L;

        private MetaContact metaContact;

        public TypingTimer()
        {
            // Set delay
            super(5 * 1000, null);

            this.addActionListener(new TimerActionListener());
        }

        private class TimerActionListener implements ActionListener
        {
            public void actionPerformed(ActionEvent e)
            {
                ChatPanel chatPanel
                    = chatWindowManager.getContactChat(metaContact, false);

                if (chatPanel != null)
                    chatPanel.removeTypingNotification();
            }
        }

        private void setMetaContact(MetaContact metaContact)
        {
            this.metaContact = metaContact;
        }
    }

    private void initPluginComponents()
    {
        // Search for plugin components registered through the OSGI bundle
        // context.
        ServiceReference[] serRefs = null;

        String osgiFilter = "("
            + Container.CONTAINER_ID
            + "="+Container.CONTAINER_CONTACT_LIST.getID()+")";

        try
        {
            serRefs = GuiActivator.bundleContext.getServiceReferences(
                PluginComponent.class.getName(),
                osgiFilter);
        }
        catch (InvalidSyntaxException exc)
        {
            logger.error("Could not obtain plugin reference.", exc);
        }

        if (serRefs != null)
        {
            for (ServiceReference serRef : serRefs)
            {
                PluginComponent component
                    = (PluginComponent)
                        GuiActivator.bundleContext.getService(serRef);

                Object selectedValue = getContactList().getSelectedValue();

                if(selectedValue instanceof MetaContact)
                {
                    component.setCurrentContact((MetaContact)selectedValue);
                }
                else if(selectedValue instanceof MetaContactGroup)
                {
                    component
                        .setCurrentContactGroup((MetaContactGroup)selectedValue);
                }

                String pluginConstraints = component.getConstraints();
                Object constraints = null;

                if (pluginConstraints != null)
                    constraints = UIServiceImpl
                        .getBorderLayoutConstraintsFromContainer(
                            pluginConstraints);
                else
                    constraints = BorderLayout.SOUTH;

                this.add((Component)component.getComponent(), constraints);

                this.repaint();
            }
        }

        GuiActivator.getUIService().addPluginComponentListener(this);
    }

    /**
     * Adds the plugin component given by <tt>event</tt> to this panel if it's
     * its container.
     * @param event the <tt>PluginComponentEvent</tt> that notified us
     */
    public void pluginComponentAdded(PluginComponentEvent event)
    {
        PluginComponent pluginComponent = event.getPluginComponent();

        // If the container id doesn't correspond to the id of the plugin
        // container we're not interested.
        if(!pluginComponent.getContainer()
                .equals(Container.CONTAINER_CONTACT_LIST))
            return;

        Object constraints = UIServiceImpl
            .getBorderLayoutConstraintsFromContainer(
                    pluginComponent.getConstraints());

        if (constraints == null)
            constraints = BorderLayout.SOUTH;

        this.add((Component) pluginComponent.getComponent(), constraints);

        Object selectedValue = getContactList().getSelectedValue();

        if(selectedValue instanceof MetaContact)
        {
            pluginComponent
                .setCurrentContact((MetaContact)selectedValue);
        }
        else if(selectedValue instanceof MetaContactGroup)
        {
            pluginComponent
                .setCurrentContactGroup((MetaContactGroup)selectedValue);
        }

        this.revalidate();
        this.repaint();
    }

    /**
     * Removes the plugin component given by <tt>event</tt> if previously added
     * in this panel.
     * @param event the <tt>PluginComponentEvent</tt> that notified us
     */
    public void pluginComponentRemoved(PluginComponentEvent event)
    {
        PluginComponent c = event.getPluginComponent();

        // If the container id doesn't correspond to the id of the plugin
        // container we're not interested.
        if(!c.getContainer()
                .equals(Container.CONTAINER_CONTACT_LIST))
            return;

        this.remove((Component) c.getComponent());
    }
}
