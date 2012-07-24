/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.osdependent.jdic;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import net.java.sip.communicator.impl.osdependent.*;
import net.java.sip.communicator.impl.osdependent.SystemTray;
import net.java.sip.communicator.impl.osdependent.TrayIcon;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.systray.*;
import net.java.sip.communicator.service.systray.event.*;
import net.java.sip.communicator.util.Logger;

import org.jitsi.service.configuration.*;
import org.jitsi.util.*;
import org.osgi.framework.*;

import com.apple.eawt.*;

/**
 * The <tt>Systray</tt> provides a Icon and the associated <tt>TrayMenu</tt>
 * in the system tray using the Jdic library.
 *
 * @author Nicolas Chamouard
 * @author Yana Stamcheva
 * @author Lyubomir Marinov
 * @author Symphorien Wanko
 */
public class SystrayServiceJdicImpl
    implements SystrayService
{

    /**
     * The systray.
     */
    private final SystemTray systray;

    /**
     * The icon in the system tray.
     */
    private TrayIcon trayIcon;

    /**
     * The menu that spring with a right click.
     */
    private Object menu;

    /**
     * The popup handler currently used to show popup messages
     */
    private PopupMessageHandler activePopupHandler;

    /**
     * A set of usable <tt>PopupMessageHandler</tt>
     */
    private final Hashtable<String, PopupMessageHandler> popupHandlerSet
        = new Hashtable<String, PopupMessageHandler>();

    /**
     * The <tt>ConfigurationService</tt> obtained from the associated
     * <tt>BundleActivator</tt>.
     */
    private final ConfigurationService cfg
        = OsDependentActivator.getConfigurationService();

    /**
     * The <tt>Logger</tt> used by the <tt>SystrayServiceJdicImpl</tt> class and
     * its instances for logging output.
     */
    private static final Logger logger
        = Logger.getLogger(SystrayServiceJdicImpl.class);

    /**
     * The various icons used on the systray
     */
    private ImageIcon currentIcon;

    private ImageIcon logoIcon;

    private ImageIcon logoIconOffline;

    private ImageIcon logoIconAway;

    private ImageIcon logoIconFFC;

    private ImageIcon logoIconDND;

    private ImageIcon logoIconWhite;

    private ImageIcon envelopeIcon;

    private ImageIcon envelopeIconWhite;

    /**
     * The dock Icons used only in Mac version
     */
    private URL dockIconOnline;
    
    private URL dockIconOffline;

    private URL dockIconAway;

    private URL dockIconFFC;

    private URL dockIconDND;

    private Image originalDockImage = null;

    private boolean initialized = false;

    /**
     * The listener which gets notified about pop-up message events (e.g. clicks
     * on the pop-up).
     */
    private final SystrayPopupMessageListener popupMessageListener
        = new SystrayPopupMessageListenerImpl();

    /**
     * Initializes a new <tt>SystrayServiceJdicImpl</tt> instance.
     */
    public SystrayServiceJdicImpl()
    {
        SystemTray systray;

        try
        {
            systray = SystemTray.getDefaultSystemTray();
        }
        catch (Throwable t)
        {
            if (t instanceof ThreadDeath)
                throw (ThreadDeath) t;
            else
            {
                systray = null;
                if (!GraphicsEnvironment.isHeadless())
                    logger.error("Failed to create a systray!", t);
            }
        }
        this.systray = systray;

        if (this.systray != null)
            initSystray();
    }

    /**
     * Initializes the systray icon and related listeners.
     */
    private void initSystray()
    {
        UIService uiService = OsDependentActivator.getUIService();

        if (uiService == null)
        {
            /*
             * Delay the call to the #initSystray() method until the UIService
             * implementation becomes available.
             */
            try
            {
                OsDependentActivator.bundleContext.addServiceListener(
                        new DelayedInitSystrayServiceListener(),
                        '('
                            + Constants.OBJECTCLASS
                            + '='
                            + UIService.class.getName()
                            + ')');
            }
            catch (InvalidSyntaxException ise)
            {
                /*
                 * Oh, it should not really happen. Besides, it is not clear at
                 * the time of this writing what is supposed to happen in the
                 * case of such an exception here.
                 */
            }
            return;
        }

        menu = TrayMenuFactory.createTrayMenu(this, systray.isSwing());

        boolean isMac = OSUtils.IS_MAC;

        // If we're running under Windows, we use a special icon without
        // background.
        if (OSUtils.IS_WINDOWS)
        {
            logoIcon = Resources.getImage("service.systray.TRAY_ICON_WINDOWS");
            logoIconOffline = Resources.getImage(
                "service.systray.TRAY_ICON_WINDOWS_OFFLINE");
            logoIconAway = Resources.getImage(
                "service.systray.TRAY_ICON_WINDOWS_AWAY");
            logoIconFFC = Resources.getImage(
                "service.systray.TRAY_ICON_WINDOWS_FFC");
            logoIconDND = Resources.getImage(
                "service.systray.TRAY_ICON_WINDOWS_DND");
            envelopeIcon = Resources.getImage(
                "service.systray.MESSAGE_ICON_WINDOWS");
        }
        /*
         * If we're running under Mac OS X, we use special black and white icons
         * without background.
         */
        else if (isMac)
        {
            logoIcon = Resources.getImage("service.systray.TRAY_ICON_MACOSX");
            logoIconWhite = Resources.getImage(
                "service.systray.TRAY_ICON_MACOSX_WHITE");
            envelopeIcon = Resources.getImage(
                "service.systray.MESSAGE_ICON_MACOSX");
            envelopeIconWhite = Resources.getImage(
                "service.systray.MESSAGE_ICON_MACOSX_WHITE");
        }
        else
        {
            logoIcon = Resources.getImage("service.systray.TRAY_ICON");
            logoIconOffline = Resources.getImage(
                "service.systray.TRAY_ICON_OFFLINE");
            logoIconAway = Resources.getImage("service.systray.TRAY_ICON_AWAY");
            logoIconFFC = Resources.getImage("service.systray.TRAY_ICON_FFC");
            logoIconDND = Resources.getImage("service.systray.TRAY_ICON_DND");
            envelopeIcon = Resources.getImage("service.systray.MESSAGE_ICON");
        }

        /*
         * Default to set offline , if any protocols become online will set it
         * to online.
         */
        currentIcon = isMac ? logoIcon : logoIconOffline;

        trayIcon
            = new TrayIcon(
                    currentIcon,
                    Resources.getApplicationString(
                            "service.gui.APPLICATION_NAME"),
                    menu);
        trayIcon.setIconAutoSize(true);

        if (isMac)
        {
            // init dock Icons
            dockIconOnline = Resources.getImageURL(
                "service.systray.DOCK_ICON_ONLINE");
            dockIconOffline = Resources.getImageURL(
                "service.systray.DOCK_ICON_OFFLINE");
            dockIconAway = Resources.getImageURL(
                "service.systray.DOCK_ICON_AWAY");
            dockIconFFC =
                    Resources.getImageURL("service.systray.DOCK_ICON_FFC");
            dockIconDND = 
                    Resources.getImageURL("service.systray.DOCK_ICON_DND");
        }

        //Show/hide the contact list when user clicks on the systray.
        trayIcon.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        UIService uiService
                            = OsDependentActivator.getUIService();
                        ExportedWindow mainWindow
                            = uiService.getExportedWindow(
                                    ExportedWindow.MAIN_WINDOW);
                        boolean setIsVisible = !mainWindow.isVisible();
        
                        uiService.setVisible(setIsVisible);
                    }
                });

        /*
         * Change the Mac OS X icon with the white one when the pop-up menu
         * appears.
         */
        if (isMac)
        {
            TrayMenuFactory.addPopupMenuListener(
                    menu,
                    new PopupMenuListener()
                    {
                        public void popupMenuWillBecomeVisible(PopupMenuEvent e)
                        {
                            ImageIcon newIcon
                                = (currentIcon == envelopeIcon)
                                    ? envelopeIconWhite
                                    : logoIconWhite;

                            trayIcon.setIcon(newIcon);
                            currentIcon = newIcon;
                        }

                        public void popupMenuWillBecomeInvisible(
                                PopupMenuEvent e)
                        {
                            ImageIcon newIcon
                                = (currentIcon == envelopeIconWhite)
                                    ? envelopeIcon
                                    : logoIcon;

                            getTrayIcon().setIcon(newIcon);
                            currentIcon = newIcon;
                        }

                        public void popupMenuCanceled(PopupMenuEvent e)
                        {
                            popupMenuWillBecomeInvisible(e);
                        }
                    });
        }

        PopupMessageHandler pmh = null;

        if (!isMac)
        {
            pmh = new PopupMessageHandlerTrayIconImpl(trayIcon);
            popupHandlerSet.put(pmh.getClass().getName(), pmh);
            OsDependentActivator.bundleContext.registerService(
                    PopupMessageHandler.class.getName(),
                    pmh,
                    null);
        }
        try
        {
            OsDependentActivator.bundleContext.addServiceListener(
                    new ServiceListenerImpl(),
                    "(objectclass="
                        + PopupMessageHandler.class.getName()
                        + ")");
        }
        catch (Exception e)
        {
            logger.warn(e);
        }

        // now we look if some handler has been registered before we start
        // to listen
        ServiceReference[] handlerRefs = null;

        try
        {
            handlerRefs
                = OsDependentActivator.bundleContext.getServiceReferences(
                        PopupMessageHandler.class.getName(),
                        null);
        }
        catch (InvalidSyntaxException ise)
        {
            logger.error("Error while retrieving service refs", ise);
        }
        if (handlerRefs != null)
        {
            String configuredHandler
                = (String) cfg.getProperty("systray.POPUP_HANDLER");

            for (ServiceReference handlerRef : handlerRefs)
            {
                PopupMessageHandler handler
                    = (PopupMessageHandler)
                        OsDependentActivator.bundleContext.getService(
                                handlerRef);
                String handlerName = handler.getClass().getName();

                if (!popupHandlerSet.containsKey(handlerName))
                {
                    popupHandlerSet.put(handlerName, handler);
                    if (logger.isInfoEnabled())
                    {
                        logger.info(
                                "added the following popup handler : "
                                    + handler);
                    }
                    if ((configuredHandler != null)
                            && configuredHandler.equals(
                                    handler.getClass().getName()))
                    {
                        setActivePopupMessageHandler(handler);
                    }
                }
            }
            
            if (configuredHandler == null)
                selectBestPopupMessageHandler();
        }

        /*
         * Either we have an incorrect configuration value or the default pop-up
         * handler is not available yet. We will use the available pop-up
         * handler and will automatically switch to the configured one when it
         * becomes available. We will be aware of it since we listen for new
         * registered services in the BundleContext.
         */
        if ((activePopupHandler == null) && (pmh != null))
            setActivePopupMessageHandler(pmh);

        systray.addTrayIcon(trayIcon);

        initialized = true;

        uiService.setExitOnMainWindowClose(false);
    }

    /**
     * Implements <tt>SystraService#showPopupMessage()</tt>
     *
     * @param popupMessage the message we will show
     */
    public void showPopupMessage(PopupMessage popupMessage)
    {
        // since popup handler could be loaded and unloader on the fly,
        // we have to check if we currently have a valid one.
        if (activePopupHandler != null)
            activePopupHandler.showPopupMessage(popupMessage);
    }

    /**
     * Implements the <tt>SystrayService.addPopupMessageListener</tt> method.
     *
     * @param listener the listener to add
     */
    public void addPopupMessageListener(SystrayPopupMessageListener listener)
    {
        if (activePopupHandler != null)
            activePopupHandler.addPopupMessageListener(listener);
    }

    /**
     * Implements the <tt>SystrayService.removePopupMessageListener</tt> method.
     *
     * @param listener the listener to remove
     */
    public void removePopupMessageListener(SystrayPopupMessageListener listener)
    {
        if (activePopupHandler != null)
            activePopupHandler.removePopupMessageListener(listener);
    }

    /**
     * Sets a new Systray icon.
     *
     * @param imageType the type of the image to set.
     */
    public void setSystrayIcon(int imageType)
    {
        if (!checkInitialized())
            return;

        boolean isMac = OSUtils.IS_MAC;
        ImageIcon systrayIconToSet = null;

        switch (imageType)
        {
        case SystrayService.SC_IMG_TYPE:
            systrayIconToSet
                = (isMac && TrayMenuFactory.isVisible(menu))
                    ? logoIconWhite
                    : logoIcon;
            break;
        case SystrayService.SC_IMG_OFFLINE_TYPE:
            if (!isMac)
                systrayIconToSet = logoIconOffline;
            break;
        case SystrayService.SC_IMG_AWAY_TYPE:
            if (!isMac)
                systrayIconToSet = logoIconAway;
            break;
        case SystrayService.SC_IMG_FFC_TYPE:
            if (!isMac)
                systrayIconToSet = logoIconFFC;
            break;
        case SystrayService.SC_IMG_DND_TYPE:
            if (!isMac)
                systrayIconToSet = logoIconDND;
            break;
        case SystrayService.ENVELOPE_IMG_TYPE:
            systrayIconToSet
                = (isMac && TrayMenuFactory.isVisible(menu))
                    ? envelopeIconWhite
                    : envelopeIcon;
            break;
        }

        if (systrayIconToSet != null)
        {
            this.trayIcon.setIcon(systrayIconToSet);
            this.currentIcon = systrayIconToSet;
        }

        if (isMac)
        {
            URL dockIconURLToSet;

            switch (imageType)
            {
            case SystrayService.SC_IMG_TYPE:
                dockIconURLToSet = dockIconOnline;
                break;
            case SystrayService.SC_IMG_OFFLINE_TYPE:
                dockIconURLToSet = dockIconOffline;
                break;
            case SystrayService.SC_IMG_AWAY_TYPE:
                dockIconURLToSet = dockIconAway;
                break;
            case SystrayService.SC_IMG_FFC_TYPE:
                dockIconURLToSet = dockIconFFC;
                break;
            case SystrayService.SC_IMG_DND_TYPE:
                dockIconURLToSet = dockIconDND;
                break;
            default:
                dockIconURLToSet = null;
                break;
            }
            try
            {
                Application application = Application.getApplication();

                if (originalDockImage == null)
                    originalDockImage = application.getDockIconImage();

                if (dockIconURLToSet != null)
                {
                    application.setDockIconImage(
                            Toolkit.getDefaultToolkit().getImage(
                                    dockIconURLToSet));
                }
                else if (originalDockImage != null)
                {
                    application.setDockIconImage(originalDockImage);
                }
            }
            catch (Exception e)
            {
                logger.error("failed to change dock icon", e);
            }
        }
    }

    private boolean checkInitialized()
    {
        if (!initialized)
            logger.error("Systray not init");
        return initialized;
    }

    /**
     * @return the trayIcon
     */
    public TrayIcon getTrayIcon()
    {
        return trayIcon;
    }

    /**
     * Set the handler which will be used for popup message
     * @param newHandler the handler to set. providing a null handler is like
     * disabling popup.
     * @return the previously used popup handler
     */
    public PopupMessageHandler setActivePopupMessageHandler(
            PopupMessageHandler newHandler)
    {
        PopupMessageHandler oldHandler = activePopupHandler;

        if (oldHandler != null)
            oldHandler.removePopupMessageListener(popupMessageListener);
        if (newHandler != null)
            newHandler.addPopupMessageListener(popupMessageListener);
        if (logger.isInfoEnabled())
        {
            logger.info(
                    "setting the following popup handler as active: "
                        + newHandler);
        }
        activePopupHandler = newHandler;
        return oldHandler;
    }

    /**
     * Get the handler currently used by this implementation to popup message
     * @return the current handler
     */
    public PopupMessageHandler getActivePopupMessageHandler()
    {
        return activePopupHandler;
    }
    
    /**
     * Sets activePopupHandler to be the one with the highest preference index.
     */
    public void selectBestPopupMessageHandler()
    {
        PopupMessageHandler preferedHandler = null;
        int highestPrefIndex = 0;

        if (!popupHandlerSet.isEmpty())
        {
            Enumeration<String> keys = popupHandlerSet.keys();

            while (keys.hasMoreElements())
            {
                String handlerName = keys.nextElement();
                PopupMessageHandler h = popupHandlerSet.get(handlerName);

                if (h.getPreferenceIndex() > highestPrefIndex)
                {
                    highestPrefIndex = h.getPreferenceIndex();
                    preferedHandler = h;
                }
            }
            setActivePopupMessageHandler(preferedHandler);
        }
    }

    /** our listener for popup message click */
    private static class SystrayPopupMessageListenerImpl
        implements SystrayPopupMessageListener
    {

        /**
         * Handles a user click on a systray popup message. If the popup
         * notification was the result of an incoming message from a contact,
         * the chat window with that contact will be opened, if not already, and
         * brought to front.
         *
         * @param evt the event triggered when user clicks on a systray popup
         * message
         */
        public void popupMessageClicked(SystrayPopupMessageEvent evt)
        {
            Object o = evt.getTag();

            if (o instanceof Contact)
                OsDependentActivator.getUIService().
                    getChat((Contact) o).setChatVisible(true);
        }
    }

    /** An implementation of <tt>ServiceListener</tt> we will use */
    private class ServiceListenerImpl
        implements ServiceListener
    {

        /**
         * implements <tt>ServiceListener.serviceChanged</tt>
         * @param serviceEvent
         */
        public void serviceChanged(ServiceEvent serviceEvent)
        {
            try
            {
                PopupMessageHandler handler =
                    (PopupMessageHandler) OsDependentActivator.bundleContext.
                    getService(serviceEvent.getServiceReference());

                if (serviceEvent.getType() == ServiceEvent.REGISTERED)
                {
                    if (!popupHandlerSet.containsKey(
                        handler.getClass().getName()))
                    {
                        if (logger.isInfoEnabled())
                            logger.info(
                            "adding the following popup handler : " + handler);
                        popupHandlerSet.put(
                            handler.getClass().getName(), handler);
                    }
                    else
                        logger.warn("the following popup handler has not " +
                            "been added since it is already known : " + handler);

                    String configuredHandler
                        = (String) cfg.getProperty("systray.POPUP_HANDLER");

                    if ((configuredHandler == null)
                            && ((activePopupHandler == null)
                                || (handler.getPreferenceIndex()
                                    > activePopupHandler.getPreferenceIndex())))
                    {
                        // The user doesn't have a preferred handler set and new 
                        // handler with better preference index has arrived, 
                        // thus setting it as active.
                        setActivePopupMessageHandler(handler);
                    }
                    if ((configuredHandler != null)
                            && configuredHandler.equals(
                                    handler.getClass().getName()))
                    {
                        // The user has a preferred handler set and it just
                        // became available, thus setting it as active
                        setActivePopupMessageHandler(handler);
                    }
                }
                else if (serviceEvent.getType() == ServiceEvent.UNREGISTERING)
                {
                    if (logger.isInfoEnabled())
                        logger.info(
                        "removing the following popup handler : " + handler);
                    popupHandlerSet.remove(handler.getClass().getName());
                    if (activePopupHandler == handler)
                    {
                        activePopupHandler.removePopupMessageListener(
                            popupMessageListener);
                        activePopupHandler = null;
                        
                        // We just lost our default handler, so we replace it
                        // with the one that has the highest preference index.
                        selectBestPopupMessageHandler();
                    }
                }
            }
            catch (IllegalStateException e)
            {
                if (logger.isDebugEnabled())
                    logger.debug(e);
            }
        }
    }

    /**
     * Implements a <tt>ServiceListener</tt> which waits for an
     * <tt>UIService</tt> implementation to become available, invokes
     * {@link #initSystray()} and unregisters itself.
     */
    private class DelayedInitSystrayServiceListener
        implements ServiceListener
    {
        public void serviceChanged(ServiceEvent serviceEvent)
        {
            if (serviceEvent.getType() == ServiceEvent.REGISTERED)
            {
                UIService uiService = OsDependentActivator.getUIService();

                if (uiService != null)
                {
                    /*
                     * This ServiceListener has successfully waited for an
                     * UIService implementation to become available so it no
                     * longer need to listen.
                     */
                    OsDependentActivator.bundleContext.removeServiceListener(
                            this);

                    if (!initialized)
                        initSystray();
                }
            }
        }
    }
}
