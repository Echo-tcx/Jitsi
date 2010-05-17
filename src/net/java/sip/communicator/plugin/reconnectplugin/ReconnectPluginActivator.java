/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.plugin.reconnectplugin;

import java.net.*;
import java.util.*;
import java.util.ArrayList;

import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.netaddr.*;
import net.java.sip.communicator.service.netaddr.event.*;
import net.java.sip.communicator.service.notification.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;

import org.osgi.framework.*;

/**
 * Activates the reconnect plug-in.
 * 
 * @author Damian Minkov
 */
public class ReconnectPluginActivator
    implements BundleActivator,
               ServiceListener,
               NetworkConfigurationChangeListener,
               RegistrationStateChangeListener
{
    /**
     * Logger of this class
     */
    private static final Logger logger
        = Logger.getLogger(ReconnectPluginActivator.class);

    /**
     * The current BundleContext.
     */
    private static BundleContext bundleContext = null;

    /**
     * The ui service.
     */
    private static UIService uiService;

    /**
     * Notification service.
     */
    private static NotificationService notificationService;

    /**
     * Network address manager service will inform us for changes in
     * network configuration.
     */
    private NetworkAddressManagerService networkAddressManagerService = null;
    
    /**
     * Holds every protocol provider which is can be reconnected and
     * a list of the available and up interfaces when the provider was
     * registered. When a provider is unregistered it is removed
     * from this collection.
     */
    private Map<ProtocolProviderService, List<String>> autoReconnEnabledProviders =
        new Hashtable<ProtocolProviderService, List<String>>();

    /**
     * Holds the currently reconnecting providers and their reconnect tasks.
     * When they get connected they are removed from this collection.
     */
    private Map<ProtocolProviderService, ReconnectTask> currentlyReconnecting =
        new Hashtable<ProtocolProviderService, ReconnectTask>();

    /**
     * If network is down we save here the providers which need to be reconnected.
     */
    private Set<ProtocolProviderService> needsReconnection =
        new HashSet<ProtocolProviderService>();

    /**
     * A list of providers on which we have called unregister. This is a
     * way to differ our unregister calls from calls coming from user, wanting
     * to stop all reconnects.
     */
    private List<ProtocolProviderService> unregisteredProviders
        = new ArrayList<ProtocolProviderService>();

    /**
     * A list of currently connected interfaces. If empty network is down.
     */
    private Set<String> connectedInterfaces = new HashSet<String>();
    
    /**
     * Timer for scheduling all reconnect operations.
     */
    private Timer timer = null;

    /**
     * Start of the delay interval when starting a reconnect.
     */
    private static final int RECONNECT_DELAY_MIN = 8; // sec

    /**
     * The end of the interval for the initial reconnect.
     */
    private static final int RECONNECT_DELAY_MAX = 30; // sec

    /**
     * Max value for growing the reconnect delay, all subsequent reconnects
     * use this maximum delay.
     */
    private static final int MAX_RECONNECT_DELAY = 300; // sec

    /**
     * Network notifications event type.
     */
    public static final String NETWORK_NOTIFICATIONS = "NetowrkNotifications";

    /**
     * Starts this bundle
     *
     * @param bundleContext BundleContext
     * @throws Exception
     */
    public void start(BundleContext bundleContext) throws Exception
    {
        try
        {
            logger.logEntry();
            ReconnectPluginActivator.bundleContext = bundleContext;
        }
        finally
        {
            logger.logExit();
        }

        bundleContext.addServiceListener(this);

        if(timer == null)
            timer = new Timer("Reconnect timer");

        ServiceReference serviceReference = bundleContext.getServiceReference(
            NetworkAddressManagerService.class.getName());

        this.networkAddressManagerService = 
            (NetworkAddressManagerService)bundleContext
                .getService(serviceReference);

        this.networkAddressManagerService
            .addNetworkConfigurationChangeListener(this);

        ServiceReference[] protocolProviderRefs = null;
        try
        {
            protocolProviderRefs = bundleContext.getServiceReferences(
                ProtocolProviderService.class.getName(), null);
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
                ProtocolProviderService provider
                    = (ProtocolProviderService) bundleContext
                        .getService(protocolProviderRefs[i]);

                this.handleProviderAdded(provider);
            }
        }
    }

    /**
     * Stop the bundle. Nothing to stop for now.
     * @param bundleContext
     * @throws Exception 
     */
    public void stop(BundleContext bundleContext)
        throws Exception
    {
        if(timer != null)
        {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * Returns the <tt>UIService</tt> obtained from the bundle context.
     *
     * @return the <tt>UIService</tt> obtained from the bundle context
     */
    public static UIService getUIService()
    {
        if (uiService == null)
        {
            ServiceReference uiReference =
                bundleContext.getServiceReference(UIService.class.getName());

            uiService =
                (UIService) bundleContext
                    .getService(uiReference);
        }

        return uiService;
    }

    /**
     * Returns the <tt>NotificationService</tt> obtained from the bundle context.
     *
     * @return the <tt>NotificationService</tt> obtained from the bundle context
     */
    public static NotificationService getNotificationService()
    {
        if (notificationService == null)
        {
            ServiceReference serviceReference = bundleContext
                .getServiceReference(NotificationService.class.getName());

            notificationService = (NotificationService) bundleContext
                .getService(serviceReference);

            notificationService.registerDefaultNotificationForEvent(
                NETWORK_NOTIFICATIONS,
                NotificationService.ACTION_POPUP_MESSAGE,
                null,
                null);
        }

        return notificationService;
    }

    /**
     * When new protocol provider is registered we add needed listeners.
     *
     * @param serviceEvent ServiceEvent
     */
    public void serviceChanged(ServiceEvent serviceEvent)
    {
        ServiceReference serviceRef = serviceEvent.getServiceReference();

        // if the event is caused by a bundle being stopped, we don't want to
        // know
        if (serviceRef.getBundle().getState() == Bundle.STOPPING)
        {
            return;
        }

        Object sService = bundleContext.getService(serviceRef);

        logger.trace("Received a service event for: " +
            sService.getClass().getName());

        if(sService instanceof NetworkAddressManagerService)
        {
            switch (serviceEvent.getType())
            {
                case ServiceEvent.REGISTERED:
                    if(this.networkAddressManagerService != null)
                        break;

                    this.networkAddressManagerService =
                        (NetworkAddressManagerService)sService;
                    networkAddressManagerService
                        .addNetworkConfigurationChangeListener(this);
                    break;
                case ServiceEvent.UNREGISTERING:
                    ((NetworkAddressManagerService)sService)
                        .removeNetworkConfigurationChangeListener(this);
                    break;
            }

            return;
        }

        // we don't care if the source service is not a protocol provider
        if (!(sService instanceof ProtocolProviderService))
            return;

        logger.debug("Service is a protocol provider.");
        switch (serviceEvent.getType())
        {
        case ServiceEvent.REGISTERED:
            this.handleProviderAdded((ProtocolProviderService)sService);
            break;

        case ServiceEvent.UNREGISTERING:
            this.handleProviderRemoved( (ProtocolProviderService) sService);
            break;
        }
    }

    /**
     * Add listeners to newly registered protocols.
     *
     * @param provider ProtocolProviderService
     */
    private void handleProviderAdded(ProtocolProviderService provider)
    {
        logger.debug("Adding protocol provider " + provider.getProtocolName());

        if(provider instanceof ProtocolProviderService)
        {
            provider.addRegistrationStateChangeListener(this);
        }
    }

    /**
     * Stop listening for events as the provider is removed.
     *
     * @param provider the ProtocolProviderService that has been unregistered.
     */
    private void handleProviderRemoved(ProtocolProviderService provider)
    {
        if(provider instanceof ProtocolProviderService)
        {
            provider.removeRegistrationStateChangeListener(this);
        }
    }

    /**
     * Fired when a change has occurred in the computer network configuration.
     *
     * @param event the change event.
     */
    public synchronized void configurationChanged(ChangeEvent event)
    {
        if(!(event.getSource() instanceof NetworkInterface))
            return;

        NetworkInterface iface = (NetworkInterface)event.getSource();

        if(event.getType() == ChangeEvent.IFACE_UP)
        {
            // no connection so one is up, lets connect
            if(connectedInterfaces.size() == 0)
            {
                Iterator<ProtocolProviderService> iter =
                    needsReconnection.iterator();
                while (iter.hasNext())
                {
                    ProtocolProviderService pp = iter.next();
                    if(currentlyReconnecting.containsKey(pp))
                    {
                        // now lets cancel it and schedule it again
                        // so it will use this iface
                        currentlyReconnecting.get(pp).cancel();
                        currentlyReconnecting.remove(pp);
                    }

                    reconnect(pp);
                }
                
                needsReconnection.clear();
            }

            connectedInterfaces.add(iface.getName());
        }
        else if(event.getType() == ChangeEvent.IFACE_DOWN)
        {
            connectedInterfaces.remove(iface.getName());

            // one is down and at least one more is connected
            if(connectedInterfaces.size() > 0)
            {
                // lets reconnect all that was connected when this one was
                // available, cause they maybe using it
                Iterator<Map.Entry<ProtocolProviderService, List<String>>> iter =
                    autoReconnEnabledProviders.entrySet().iterator();
                while (iter.hasNext())
                {
                    Map.Entry<ProtocolProviderService, List<String>> entry
                        = iter.next();

                    if(entry.getValue().contains(iface.getName()))
                    {
                        ProtocolProviderService pp = entry.getKey();
                        // hum someone is reconnecting, lets cancel and
                        // schedule it again
                        if(currentlyReconnecting.containsKey(pp))
                        {
                            currentlyReconnecting.get(pp).cancel();
                            currentlyReconnecting.remove(pp);
                        }

                        reconnect(pp);
                    }
                }
            }
            else
            {
                // we must disconnect every pp and put all to be need of reconnecting
                needsReconnection.addAll(autoReconnEnabledProviders.keySet());

                Iterator<ProtocolProviderService> iter =
                    needsReconnection.iterator();
                while (iter.hasNext())
                {
                    ProtocolProviderService pp = iter.next();
                    try
                    {
                        unregisteredProviders.add(pp);
                        pp.unregister();
                    } catch (Exception e)
                    {
                        logger.error("Cannot unregister provider", e);
                    }
                }

                connectedInterfaces.clear();

                logger.trace("Network is down!");
                getNotificationService().fireNotification(
                    NETWORK_NOTIFICATIONS,
                    "Network is down!",
                    "",
                    null,
                    null);
            }
        }
    }

    /**
     * The method is called by a <code>ProtocolProviderService</code>
     * implementation whenever a change in the registration state of the
     * corresponding provider had occurred.
     *
     * @param evt the event describing the status change.
     */
    public synchronized void registrationStateChanged(RegistrationStateChangeEvent evt)
    {
        // we don't care about protocol providers that don't support
        // reconnection
        if(!(evt.getSource() instanceof ProtocolProviderService))
            return;

        ProtocolProviderService pp = (ProtocolProviderService)evt.getSource();

        if(evt.getNewState().equals(RegistrationState.CONNECTION_FAILED))
        {
            // if this pp is already in needsReconnection, it means
            // we got conn failed cause the pp has tried to unregister
            // with sending network packet
            // but this unregister is scheduled from us so skip
            if(needsReconnection.contains(pp))
                return;

            if(connectedInterfaces.size() == 0)
                needsReconnection.add(pp);
            else
            {
                // network is up but something happen and cannot reconnect
                // strange lets try again after some time
                reconnect(pp);
            }

            // unregister can finish and with connection failed,
            // the protocol is unable to unregister
            unregisteredProviders.remove(pp);
        }
        else if(evt.getNewState().equals(RegistrationState.REGISTERED))
        {
            autoReconnEnabledProviders.put(
                (ProtocolProviderService)evt.getSource(),
                new ArrayList<String>(connectedInterfaces));

            currentlyReconnecting.remove(pp);
        }
        else if(evt.getNewState().equals(RegistrationState.UNREGISTERED))
        {
            autoReconnEnabledProviders.remove(
                (ProtocolProviderService)evt.getSource());

            if(!unregisteredProviders.contains(pp)
                && currentlyReconnecting.containsKey(pp))
            {
                currentlyReconnecting.remove(pp).cancel();
            }
            unregisteredProviders.remove(pp);
        }
    }

    /**
     * Method to schedule a reconnect for a protocol provider.
     * @param pp the provider.
     */
    private void reconnect(ProtocolProviderService pp)
    {
        long delay;
        
        if(currentlyReconnecting.containsKey(pp))
        {
            delay = currentlyReconnecting.get(pp).delay;

            // we never stop trying
            //if(delay == MAX_RECONNECT_DELAY*1000)
            //    return;

            delay = Math.min(delay * 2, MAX_RECONNECT_DELAY*1000);
        }
        else
        {
            delay = (long)(RECONNECT_DELAY_MIN
                + Math.random() * RECONNECT_DELAY_MAX)*1000;
        }

        // as we will reconnect, lets unregister
        try
        {
            unregisteredProviders.add(pp);
            pp.unregister();
        } catch (OperationFailedException e)
        {
            logger.error("Cannot unregister provider", e);
        }

        ReconnectTask task = new ReconnectTask(pp);
        task.delay = delay;
        currentlyReconnecting.put(pp, task);
        
        logger.trace("Reconnect " + pp + " after " + delay + " ms.");
        timer.schedule(task, delay);
    }

    /**
     * The task executed by the timer when time for reconnect comes.
     */
    private class ReconnectTask
        extends TimerTask
    {
        /**
         * The provider to reconnect.
         */
        private ProtocolProviderService provider;

        /**
         * The delay with which was this task scheduled.
         */
        private long delay;

        /**
         * Creates the task.
         * @param provider
         */
        public ReconnectTask(ProtocolProviderService provider)
        {
            this.provider = provider;
        }

        /**
         * Reconnects the provider.
         */
        public void run()
        {
            try
            {
                logger.trace("Start reconnecting!");

                provider.register(
                    getUIService().getDefaultSecurityAuthority(provider));
            } catch (OperationFailedException ex)
            {
                logger.error("cannot reregister provider will keep going", ex);
            }
        }
    }
}
