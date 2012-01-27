package net.java.sip.communicator.impl.protocol.jabber.extensions.keepalive;

import net.java.sip.communicator.impl.protocol.jabber.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.provider.*;

import java.util.*;

/**
 * XEP-0199: XMPP Ping. Tracks received packets and if for some interval
 * there is nothing received.
 *
 * @author Damian Minkov
 */
public class KeepAliveManager
    implements RegistrationStateChangeListener,
               PacketListener
{
    /**
     * Our class logger
     */
    private static final Logger logger =
        Logger.getLogger(KeepAliveManager.class);

    /**
     * The task sending packets
     */
    private KeepAliveSendTask keepAliveSendTask = null;

    /**
     * The timer executing tasks on specified intervals
     */
    private Timer keepAliveTimer;

    /**
     * The last received packet from server.
     */
    private long lastReceiveActivity = 0;

    /**
     * The interval between checks.
     */
    private int keepAliveCheckInterval;

    /**
     * If we didn't receive a packet between two checks, we send a packet,
     * so we can receive something error or reply.
     */
    private String waitingForPacketWithID = null;

    /**
     * Our parent provider.
     */
    private ProtocolProviderServiceJabberImpl parentProvider = null;

    /**
     * Creates manager.
     * @param parentProvider the parent provider.
     */
    public KeepAliveManager(ProtocolProviderServiceJabberImpl parentProvider)
    {
        this.parentProvider = parentProvider;

        this.parentProvider.addRegistrationStateChangeListener(this);

        // register the KeepAlive Extension in the smack library
        // used only if somebody ping us
        ProviderManager.getInstance()
            .addIQProvider(KeepAliveEvent.ELEMENT_NAME,
                           KeepAliveEvent.NAMESPACE,
                           new KeepAliveEventProvider());
    }

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
            parentProvider.getConnection().removePacketListener(this);
            parentProvider.getConnection().addPacketListener(this, null);

            // if for some unknown reason we got two registered events
            // and we have already created those tasks make sure we cancel them
            if(keepAliveSendTask != null)
            {
                logger.error("Those task is not supposed to be available for "
                    + parentProvider.getAccountID().getDisplayName());
                keepAliveSendTask.cancel();
                keepAliveSendTask = null;
            }
            if(keepAliveTimer != null)
            {
                logger.error("Those timer is not supposed to be available for "
                    + parentProvider.getAccountID().getDisplayName());
                keepAliveTimer.cancel();
                keepAliveTimer = null;
            }

            keepAliveSendTask = new KeepAliveSendTask();
            waitingForPacketWithID = null;

            keepAliveCheckInterval =
                SmackConfiguration.getKeepAliveInterval();
            if(keepAliveCheckInterval == 0)
                keepAliveCheckInterval = 30000;
            
            keepAliveTimer = new Timer("Jabber keepalive timer for <"
                + parentProvider.getAccountID() + ">", true);
            keepAliveTimer.scheduleAtFixedRate(
                keepAliveSendTask,
                keepAliveCheckInterval,
                keepAliveCheckInterval);
        }
        else if(evt.getNewState() == RegistrationState.UNREGISTERED
            || evt.getNewState() == RegistrationState.CONNECTION_FAILED
            || evt.getNewState() == RegistrationState.AUTHENTICATION_FAILED)
        {
            waitingForPacketWithID = null;

            if(parentProvider.getConnection() != null)
                parentProvider.getConnection().removePacketListener(this);

            if(keepAliveSendTask != null)
            {
                keepAliveSendTask.cancel();
                keepAliveSendTask = null;
            }
            if(keepAliveTimer != null)
            {
                keepAliveTimer.cancel();
                keepAliveTimer = null;
            }
        }
    }

    /**
     * A packet Listener for all incoming packets.
     * @param packet an incoming packet
     */
    public void processPacket(Packet packet)
    {
        // store that we have received
        lastReceiveActivity = System.currentTimeMillis();

        if(waitingForPacketWithID != null &&
            waitingForPacketWithID.equals(packet.getPacketID()))
        {
            // we are no more waiting for this packet
            waitingForPacketWithID = null;
        }

        if(packet instanceof KeepAliveEvent)
        {
            // replay only to server pings, to avoid leak of presence
            KeepAliveEvent evt = (KeepAliveEvent)packet;
            if(evt.getFrom() != null
               && evt.getFrom()
                    .equals(parentProvider.getAccountID().getService()))
            {
                parentProvider.getConnection().sendPacket(
                    IQ.createResultIQ(evt));
            }
        }
    }

    /**
     * Task sending packets on intervals.
     * The task is runned on specified intervals by the keepAliveTimer
     */
    private class KeepAliveSendTask
        extends TimerTask
    {
        /**
         * Sends a single <tt>KeepAliveEvent</tt>.
         */
        public void run()
        {
            // if we are not registered do nothing
            if(!parentProvider.isRegistered())
            {
                if (logger.isTraceEnabled())
                    logger.trace("provider not registered. "
                        +"won't send keep alive for "
                        + parentProvider.getAccountID().getDisplayName());
                return;
            }

            if(System.currentTimeMillis() - lastReceiveActivity >
                    keepAliveCheckInterval)
            {
                if(waitingForPacketWithID != null)
                {
                    logger.error("un-registering not received ping packet " +
                        "for: " + parentProvider.getAccountID().getDisplayName());

                    parentProvider.unregister(false);

                    parentProvider.fireRegistrationStateChanged(
                        parentProvider.getRegistrationState(),
                        RegistrationState.CONNECTION_FAILED,
                        RegistrationStateChangeEvent.REASON_SERVER_NOT_FOUND,
                        null);

                    return;
                }

                try
                {
                    // lets send a ping
                    KeepAliveEvent ping = new KeepAliveEvent(
                        parentProvider.getOurJID(),
                        parentProvider.getAccountID().getService()
                    );

                    if (logger.isTraceEnabled())
                        logger.trace("send keepalive for acc: "
                            + parentProvider.getAccountID().getDisplayName());

                    waitingForPacketWithID = ping.getPacketID();
                    parentProvider.getConnection().sendPacket(ping);
                }
                catch(Throwable t)
                {
                    logger.error("Error sending ping request!", t);
                    waitingForPacketWithID = null;
                }
            }
        }
    }
}
