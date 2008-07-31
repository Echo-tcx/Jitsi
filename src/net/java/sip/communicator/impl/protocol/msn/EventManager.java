/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.msn;

import java.util.*;

import net.sf.jml.*;
import net.sf.jml.impl.*;
import net.sf.jml.net.*;
import net.sf.jml.protocol.*;
import net.sf.jml.protocol.incoming.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.sf.jml.net.Message;


/**
 * Manager which listens for changing of the contact list
 * and fires some events
 *
 * @author Damian Minkov
 */
public class EventManager
    extends SessionAdapter
{
    private static final Logger logger = Logger.getLogger(EventManager.class);

    private BasicMessenger msnMessenger = null;
    private Vector listeners = new Vector();

    /**
     * The provider that is on top of us.
     */
    private ProtocolProviderServiceMsnImpl msnProvider = null;
    
    /**
     * Creates the manager
     * @param msnMessenger BasicMessenger the messenger
     */
    public EventManager(ProtocolProviderServiceMsnImpl msnProvider, 
        BasicMessenger msnMessenger)
    {
        this.msnProvider = msnProvider;
        this.msnMessenger = msnMessenger;

        msnMessenger.addSessionListener(this);
    }

    /**
     * Adds listener of the modification fired events
     * @param listener the modification listener we're adding
     */
    public void addModificationListener(MsnContactListEventListener listener)
    {
        synchronized(listeners)
        {
            listeners.add(listener);
        }
    }

    /**
     * Removes listener of the modification fired events
     * @param listener EventListener
     */
    public void removeModificationListener(MsnContactListEventListener listener)
    {
        synchronized(listeners)
        {
            listeners.remove(listener);
        }
    }

    /**
     * Called from the underling lib when message is sent to the server
     * @param session Session
     * @param message Message
     * @throws Exception
     */
    public void messageSent(Session session, Message message) throws Exception
    {
        logger.trace(msnMessenger.getOwner().getEmail().getEmailAddress() +
                     " outgoing " + message);
    }

    /**
     * Called from the underling lib when message is received from the server
     * @param session Session
     * @param message Message
     * @throws Exception
     */
    public void messageReceived(Session session, Message message)
        throws Exception
    {
        MsnIncomingMessage incoming = (MsnIncomingMessage)((WrapperMessage)message)
            .getMessage();

        logger.trace(msnMessenger.getOwner().getEmail().getEmailAddress() +
                     " incoming : " + incoming);

        if(incoming instanceof IncomingACK)
        {
            //indicate the message has successed send to remote user.
            fireMessageDelivered(((IncomingACK)incoming).getTransactionId());
        }
        else if(incoming instanceof IncomingNAK)
        {
            //indicate the message has not successed send to remote user.
            fireMessageDeliveredFailed(((IncomingNAK)incoming).getTransactionId());
        }
        else if(incoming instanceof IncomingREG)
        {
            //indicate the group name has changed successfully.
            IncomingREG incomingREG  = (IncomingREG)incoming;

            MsnGroupImpl group = (MsnGroupImpl)msnMessenger.getContactList().
                getGroup(incomingREG.getGroupId());
            fireGroupRenamed(group);
        }
        else if(incoming instanceof IncomingOUT)
        {
            IncomingOUT incomingOUT  = (IncomingOUT)incoming;
            if(incomingOUT.isLoggingFromOtherLocation())
                fireLoggingFromOtherLocation();
        }
        else if(incoming instanceof IncomingQNG)
        {
            IncomingQNG incomingQNG  = (IncomingQNG)incoming;
            
            connected = true;
        }
    }

    private boolean connected = false;
    private Timer connectionTimer = new Timer();
            
    public void sessionTimeout(Session socketSession) throws Exception
    {
        connectionTimer.schedule(new TimerTask()
        {
            public void run()
            {
                if(!connected && msnProvider.isRegistered())
                {
                    msnProvider.unregister(false);
                    msnProvider.reconnect(SecurityAuthority.CONNECTION_FAILED);
                }
            }
        }, 20000);
        connected = false;
    }

    /**
     * Fired when a message is delivered successfully
     * @param transactionID int
     */
    private void fireMessageDelivered(int transactionID)
    {
        synchronized(listeners){
            Iterator iter = listeners.iterator();
            while (iter.hasNext())
            {
                ((MsnContactListEventListener)iter.next()).
                    messageDelivered(transactionID);
            }
        }
    }

    /**
     * Fired when a message is not delivered successfully
     * @param transactionID int
     */
    private void fireMessageDeliveredFailed(int transactionID)
    {
        synchronized(listeners){
            Iterator iter = listeners.iterator();
            while (iter.hasNext())
            {
                ((MsnContactListEventListener)iter.next()).
                    messageDeliveredFailed(transactionID);
            }
        }
    }

    /**
     * Fired when a group is renamed successfully
     * @param group MsnGroup
     */
    private void fireGroupRenamed(MsnGroup group)
    {
        synchronized(listeners){
            Iterator iter = listeners.iterator();
            while (iter.hasNext())
            {
                ((MsnContactListEventListener)iter.next()).groupRenamed(group);
            }
        }
    }

    /**
     * Fired when we received event for logging in from other location
     */
    private void fireLoggingFromOtherLocation()
    {
        synchronized (listeners)
        {
            Iterator iter = listeners.iterator();
            while (iter.hasNext())
            {
                ( (MsnContactListEventListener) iter.next())
                    .loggingFromOtherLocation();
            }
        }
    }
}
