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

import net.java.sip.communicator.service.configuration.*;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.netaddr.*;
import net.java.sip.communicator.service.netaddr.event.*;
import net.java.sip.communicator.service.notification.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.service.resources.*;
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
     * The resources service.
     */
    private static ResourceManagementService resourcesService;

    /**
     * A reference to the ConfigurationService implementation instance that
     * is currently registered with the bundle context.
     */
    private static ConfigurationService configurationService = null;

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
    private Set<ProtocolProviderService> unregisteredProviders
        = new HashSet<ProtocolProviderService>();

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
     *
     */
    public static final String ATLEAST_ONE_CONNECTION_PROP =
        "net.java.sip.communicator.plugin.reconnectplugin." +
            "ATLEAST_ONE_SUCCESSFUL_CONNECTION";

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
            if (logger.isDebugEnabled())
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
     * Returns resource service.
     * @return the resource service.
     */
    public static ResourceManagementService getResources()
    {
        if (resourcesService == null)
        {
            ServiceReference serviceReference = bundleContext
                .getServiceReference(ResourceManagementService.class.getName());

            if(serviceReference == null)
                return null;

            resourcesService = (ResourceManagementService) bundleContext
                .getService(serviceReference);
        }

        return resourcesService;
    }

    /**
     * Returns a reference to a ConfigurationService implementation currently
     * registered in the bundle context or null if no such implementation was
     * found.
     *
     * @return a currently valid implementation of the ConfigurationService.
     */
    public static ConfigurationService getConfigurationService()
    {
        if (configurationService == null)
        {
            ServiceReference confReference
                = bundleContext.getServiceReference(
                    ConfigurationService.class.getName());
            configurationService
                = (ConfigurationService) bundleContext
                                        .getService(confReference);
        }
        return configurationService;
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
        // know we are shutting down
        if (serviceRef.getBundle().getState() == Bundle.STOPPING)
        {
            return;
        }

        Object sService = bundleContext.getService(serviceRef);

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
        if (logger.isTraceEnabled())
            logger.trace("New protocol provider is comming "
            + provider.getProtocolName());

        provider.addRegistrationStateChangeListener(this);
    }

    /**
     * Stop listening for events as the provider is removed.
     * Providers are removed this way only when there are modified
     * in the configuration. So as the provider is modified we will erase
     * every instance we got.
     *
     * @param provider the ProtocolProviderService that has been unregistered.
     */
    private void handleProviderRemoved(ProtocolProviderService provider)
    {
        if (logger.isTraceEnabled())
            logger.trace("Provider modified forget every instance of it");

        if(hasAtLeastOneSuccessfulConnection(provider))
        {
            setAtLeastOneSuccessfulConnection(provider, false);
        }

        provider.removeRegistrationStateChangeListener(this);

        autoReconnEnabledProviders.remove(provider);
        needsReconnection.remove(provider);

        if(currentlyReconnecting.containsKey(provider))
        {
            currentlyReconnecting.remove(provider).cancel();
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
                        currentlyReconnecting.remove(pp).cancel();
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
                            currentlyReconnecting.remove(pp).cancel();
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
                        // if provider is scheduled for reconnect,
                        // cancel it there is no network
                        if(currentlyReconnecting.containsKey(pp))
                        {
                            currentlyReconnecting.remove(pp).cancel();
                        }

                        unregisteredProviders.add(pp);
                        pp.unregister();
                    } catch (Exception e)
                    {
                        logger.error("Cannot unregister provider", e);
                    }
                }

                connectedInterfaces.clear();

                if (logger.isTraceEnabled())
                    logger.trace("Network is down!");
                notify("", "plugin.reconnectplugin.NETWORK_DOWN", new String[0]);
            }
        }
        
        if(logger.isTraceEnabled())
        {
            logger.trace("Event received " + event
                    + " src=" + event.getSource());
            traceCurrentPPState();
        }
    }

    /**
     * Trace prints of current status of the lists with protocol providers,
     * that are currently in interest of the reconnect plugin.
     */
    private void traceCurrentPPState()
    {
        logger.trace("connectedInterfaces: " + connectedInterfaces);
        logger.trace("autoReconnEnabledProviders: "
            + autoReconnEnabledProviders.keySet());
        logger.trace("currentlyReconnecting: "
            + currentlyReconnecting.keySet());
        logger.trace("needsReconnection: " + needsReconnection);
        logger.trace("unregisteredProviders: " + unregisteredProviders);
        logger.trace("----");
    }

    /**
     * Sends network notification.
     * @param title the title.
     * @param i18nKey the resource key of the notification.
     * @param params and parameters in any.
     */
    private void notify(String title, String i18nKey, String[] params)
    {
        getNotificationService().fireNotification(
                    NETWORK_NOTIFICATIONS,
                    title,
                    getResources().getI18NString(i18nKey, params),
                    null,
                    null);
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

        try
        {
            ProtocolProviderService pp = (ProtocolProviderService)evt.getSource();

            if(evt.getNewState().equals(RegistrationState.CONNECTION_FAILED))
            {
                if(!hasAtLeastOneSuccessfulConnection(pp))
                {
                    // ignore providers which haven't registered successfully
                    // till now, they maybe misconfigured
                    // todo show dialog
                    notify(
                        getResources().getI18NString("service.gui.ERROR"),
                        "plugin.reconnectplugin.CONNECTION_FAILED_MSG",
                        new String[]
                        {   pp.getAccountID().getUserID(),
                            pp.getAccountID().getService() });

                    return;
                }

                // if this pp is already in needsReconnection, it means
                // we got conn failed cause the pp has tried to unregister
                // with sending network packet
                // but this unregister is scheduled from us so skip
                if(needsReconnection.contains(pp))
                    return;

                if(connectedInterfaces.size() == 0)
                {
                    needsReconnection.add(pp);

                    if(currentlyReconnecting.containsKey(pp))
                        currentlyReconnecting.remove(pp).cancel();
                }
                else
                {
                    // network is up but something happen and cannot reconnect
                    // strange lets try again after some time
                    reconnect(pp);
                }

                // unregister can finish and with connection failed,
                // the protocol is unable to unregister
                unregisteredProviders.remove(pp);

                if(logger.isTraceEnabled())
                {
                    logger.trace("Got Connection Failed for " + pp);
                    traceCurrentPPState();
                }
            }
            else if(evt.getNewState().equals(RegistrationState.REGISTERED))
            {
                if(!hasAtLeastOneSuccessfulConnection(pp))
                {
                    setAtLeastOneSuccessfulConnection(pp, true);
                }

                autoReconnEnabledProviders.put(
                    pp,
                    new ArrayList<String>(connectedInterfaces));

                if(currentlyReconnecting.containsKey(pp))
                    currentlyReconnecting.remove(pp).cancel();

                if(logger.isTraceEnabled())
                {
                    logger.trace("Got Registered for " + pp);
                    traceCurrentPPState();
                }
            }
            else if(evt.getNewState().equals(RegistrationState.UNREGISTERED))
            {
                autoReconnEnabledProviders.remove(pp);

                if(!unregisteredProviders.contains(pp)
                    && currentlyReconnecting.containsKey(pp))
                {
                    currentlyReconnecting.remove(pp).cancel();
                }
                unregisteredProviders.remove(pp);

                if(logger.isTraceEnabled())
                {
                    logger.trace("Got Unregistered for " + pp);
                    traceCurrentPPState();
                }
            }
        }
        catch(Throwable ex)
        {
            logger.error("Error dispatching protocol registration change", ex);
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
        
        if (logger.isTraceEnabled())
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
                if (logger.isTraceEnabled())
                    logger.trace("Start reconnecting!");

                provider.register(
                    getUIService().getDefaultSecurityAuthority(provider));
            } catch (OperationFailedException ex)
            {
                logger.error("cannot reregister provider will keep going", ex);
            }
        }
    }

    /**
     * Check does the supplied protocol has the property set for at least
     * one successful connection.
     * @param pp the protocol provider
     * @return true if property exists.
     */
    private boolean hasAtLeastOneSuccessfulConnection(ProtocolProviderService pp)
    {
       String value = (String)getConfigurationService().getProperty(
           ATLEAST_ONE_CONNECTION_PROP + "." 
           + pp.getAccountID().getAccountUniqueID());
       
       if(value == null || !value.equals(Boolean.TRUE.toString()))
           return false;
       else
           return true;
    }

    /**
     * Changes the property about at least one successful connection.
     * @param pp the protocol provider
     * @param value the new value true or false.
     */
    private void setAtLeastOneSuccessfulConnection(
        ProtocolProviderService pp, boolean value)
    {
       getConfigurationService().setProperty(
           ATLEAST_ONE_CONNECTION_PROP + "."
            + pp.getAccountID().getAccountUniqueID(),
           Boolean.valueOf(value).toString());
    }
}
