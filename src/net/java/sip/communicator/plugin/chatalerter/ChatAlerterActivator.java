/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.plugin.chatalerter;

import java.util.*;
import javax.swing.*;
import org.osgi.framework.*;

import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;

import org.jdesktop.jdic.misc.*;

/**
 * Chat Alerter plugin.
 * 
 * Sends alerts to the user when new message arrives and the application is not in
 * the foreground. On Mac OS X this will bounce the dock icon until
 * the user selects the chat windows. On Windows, Gnome and KDE this will flash the
 * taskbar button/icon until the user selects the chat window.                                                                                                                                                   
 * 
 * @author Damian Minkov
 */
public class ChatAlerterActivator 
    implements  BundleActivator,
                ServiceListener,
                MessageListener,
                ChatRoomMessageListener,
                LocalUserChatRoomPresenceListener
{
    /**
     * The logger for this class.
     */
    private static Logger logger = Logger
            .getLogger(ChatAlerterActivator.class);
    
    /**
     * The BundleContext that we got from the OSGI bus.
     */
    private BundleContext bundleContext = null;
    
    /**
     * UIService reference.
     */
    private UIService uiService;

    /**
     * Starts this bundle.
     */
    public void start(BundleContext bc) throws Exception
    {
        try
        {
            // try to load native libs, if it fails don't do anything
            Alerter.newInstance();
        }
        catch (Exception exception)
        {
            logger.info("The Alerter not supported or problem loading it!", 
                exception);
            return;
        }
        
        ServiceReference uiServiceRef
            = bc.getServiceReference(UIService.class.getName());

        uiService = (UIService) bc.getService(uiServiceRef);

        this.bundleContext = bc;

        // start listening for newly register or removed protocol providers
        bc.addServiceListener(this);

        ServiceReference[] protocolProviderRefs = null;
        try
        {
            protocolProviderRefs = bc.getServiceReferences(
                ProtocolProviderService.class.getName(),
                null);
        }
        catch (InvalidSyntaxException ex)
        {
            // this shouldn't happen since we're providing no parameter string
            // but let's log just in case.
            logger.error(
                "Error while retrieving service refs", ex);
            return;
        }

        // in case we found any
        if (protocolProviderRefs != null)
        {
            logger.debug("Found "
                         + protocolProviderRefs.length
                         + " already installed providers.");
            for (int i = 0; i < protocolProviderRefs.length; i++)
            {
                ProtocolProviderService provider = (ProtocolProviderService) bc
                    .getService(protocolProviderRefs[i]);

                this.handleProviderAdded(provider);
            }
        }
    }

    public void stop(BundleContext bc) throws Exception
    {
        // start listening for newly register or removed protocol providers
        bc.removeServiceListener(this);

        ServiceReference[] protocolProviderRefs = null;
        try
        {
            protocolProviderRefs = bc.getServiceReferences(
                ProtocolProviderService.class.getName(),
                null);
        }
        catch (InvalidSyntaxException ex)
        {
            // this shouldn't happen since we're providing no parameter string
            // but let's log just in case.
            logger.error(
                "Error while retrieving service refs", ex);
            return;
        }

        // in case we found any
        if (protocolProviderRefs != null)
        {
            for (int i = 0; i < protocolProviderRefs.length; i++)
            {
                ProtocolProviderService provider = (ProtocolProviderService) bc
                    .getService(protocolProviderRefs[i]);

                this.handleProviderRemoved(provider);
            }
        }
    }
    
    /**
     * Used to attach the Alerter plugin to existing or
     * just registered protocol provider. Checks if the provider has implementation
     * of OperationSetBasicInstantMessaging
     *
     * @param provider ProtocolProviderService
     */
    private void handleProviderAdded(ProtocolProviderService provider)
    {
        logger.debug("Adding protocol provider " + provider.getProtocolName());

        // check whether the provider has a basic im operation set
        OperationSetBasicInstantMessaging opSetIm =
            (OperationSetBasicInstantMessaging) provider
                .getOperationSet(OperationSetBasicInstantMessaging.class);

        if (opSetIm != null)
        {
            opSetIm.addMessageListener(this);
        }
        else
        {
            logger.trace("Service did not have a im op. set.");
        }
        
        // check whether the provider has a sms operation set
        OperationSetSmsMessaging opSetSms =
            (OperationSetSmsMessaging) provider
                .getOperationSet(OperationSetSmsMessaging.class);

        if (opSetSms != null)
        {
            opSetSms.addMessageListener(this);
        }
        else
        {
            logger.trace("Service did not have a sms op. set.");
        }
        
        OperationSetMultiUserChat opSetMultiUChat =
            (OperationSetMultiUserChat) provider
                .getOperationSet(OperationSetMultiUserChat.class);

        if (opSetMultiUChat != null)
        {
            Iterator iter = 
                opSetMultiUChat.getCurrentlyJoinedChatRooms().iterator();

            while(iter.hasNext())
            {
                ChatRoom room =  (ChatRoom)iter.next();
                room.addMessageListener(this);
            }
            
            opSetMultiUChat.addPresenceListener(this);
        }
        else
        {
            logger.trace("Service did not have a multi im op. set.");
        }
    }

    /**
     * Removes the specified provider from the list of currently known providers
     * and ignores all the messages exchanged by it
     *
     * @param provider the ProtocolProviderService that has been unregistered.
     */
    private void handleProviderRemoved(ProtocolProviderService provider)
    {
        OperationSetBasicInstantMessaging opSetIm =
            (OperationSetBasicInstantMessaging) provider
                .getOperationSet(OperationSetBasicInstantMessaging.class);

        if (opSetIm != null)
        {
            opSetIm.removeMessageListener(this);
        }
        
         OperationSetMultiUserChat opSetMultiUChat =
            (OperationSetMultiUserChat) provider
                .getOperationSet(OperationSetMultiUserChat.class);

        if (opSetMultiUChat != null)
        {
            Iterator iter = 
                opSetMultiUChat.getCurrentlyJoinedChatRooms().iterator();
            
            while(iter.hasNext())
            {
                ChatRoom room =  (ChatRoom)iter.next();
                room.removeMessageListener(this);
            }
        }
    }

    /**
     * Called to notify interested parties that a change in our presence in
     * a chat room has occurred. Changes may include us being kicked, join,
     * left.
     * @param evt the <tt>LocalUserChatRoomPresenceChangeEvent</tt> instance
     * containing the chat room and the type, and reason of the change
     */
    public void localUserPresenceChanged(LocalUserChatRoomPresenceChangeEvent evt)
    {
        if(evt.getEventType() == 
            LocalUserChatRoomPresenceChangeEvent.LOCAL_USER_JOINED)
        {
            if (!evt.getChatRoom().isSystem())
                evt.getChatRoom().addMessageListener(this);
        }
        else
        {
            evt.getChatRoom().removeMessageListener(this);
        }
    }

    public void messageReceived(MessageReceivedEvent evt)
    {
        try
        {
            ExportedWindow win = 
                uiService.getExportedWindow(ExportedWindow.CHAT_WINDOW);
            
            if(win == null || win.getSource() == null || 
                !(win.getSource() instanceof JFrame))
                return;
            
            JFrame fr = (JFrame)win.getSource(); 

            if(fr != null)
                Alerter.newInstance().alert(fr);
        }
        catch (Exception ex)
        {
            logger.error("Cannot alert chat window!");
        }
    }

    public void messageDelivered(MessageDeliveredEvent evt)
    {
        // do nothing
    }

    public void messageDeliveryFailed(MessageDeliveryFailedEvent evt)
    {
        // do nothing
    }

    public void messageReceived(ChatRoomMessageReceivedEvent evt)
    {
        try
        {
            ExportedWindow win = 
                uiService.getExportedWindow(ExportedWindow.CHAT_WINDOW);
            
            if(win == null || win.getSource() == null || 
                !(win.getSource() instanceof JFrame))
                return;
            
            JFrame fr = (JFrame)win.getSource(); 

            if(fr != null)
                Alerter.newInstance().alert(fr);
        }
        catch (Exception ex)
        {
            logger.error("Cannot alert chat window!");
        }
    }

    public void messageDelivered(ChatRoomMessageDeliveredEvent evt)
    {
        // do nothing
    }

    public void messageDeliveryFailed(ChatRoomMessageDeliveryFailedEvent evt)
    {
        // do nothing
    }

    /**
     * When new protocol provider is registered we check
     * does it supports needed Op. Sets and if so add a listener to it
     *
     * @param serviceEvent ServiceEvent
     */
    public void serviceChanged(ServiceEvent serviceEvent)
    {
        Object sService = 
            bundleContext.getService(serviceEvent.getServiceReference());

        logger.trace("Received a service event for: " + 
            sService.getClass().getName());

        // we don't care if the source service is not a protocol provider
        if (! (sService instanceof ProtocolProviderService))
        {
            return;
        }

        logger.debug("Service is a protocol provider.");
        if (serviceEvent.getType() == ServiceEvent.REGISTERED)
        {
            logger.debug("Handling registration of a new Protocol Provider.");

            this.handleProviderAdded((ProtocolProviderService)sService);
        }
        else if (serviceEvent.getType() == ServiceEvent.UNREGISTERING)
        {
            this.handleProviderRemoved( (ProtocolProviderService) sService);
        }
    }
}
