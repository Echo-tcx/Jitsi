/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main;

import java.util.*;

/**
 * The <tt>UINotificationManager</tt> manages all notifications dedicated to
 * be shown in the main application window. These could be missed calls, voice
 * messages, etc.
 *
 * @author Yana Stamcheva
 */
public class UINotificationManager
{
    /**
     * The list of all notification groups.
     */
    private static Collection<UINotificationGroup> notificationGroups
        = new ArrayList<UINotificationGroup>();

    /**
     * Listener notified for changes in missed calls count.
     */
    private static Collection<UINotificationListener> notificationListeners
        = new ArrayList<UINotificationListener>();

    /**
     * Adds the given <tt>UINotificationListener</tt> to the list of listeners
     * that would be notified on any changes in missed calls count.
     *
     * @param l the <tt>UINotificationListener</tt> to add
     */
    public static void addNotificationListener(UINotificationListener l)
    {
        synchronized (l)
        {
            notificationListeners.add(l);
        }
    }

    /**
     * Removes the given <tt>UINotificationListener</tt> from the list of
     * listeners that are notified on any changes in missed calls count.
     *
     * @param l the <tt>UINotificationListener</tt> to remove
     */
    public static void removeNotificationListener(UINotificationListener l)
    {
        synchronized (l)
        {
            notificationListeners.remove(l);
        }
    }

    /**
     * Adds the given notification to the list of unread notifications and
     * notifies interested listeners.
     *
     * @param notification the <tt>UINotification</tt> to add
     */
    public static void addNotification(UINotification notification)
    {
        UINotificationGroup group = notification.getGroup();

        if (!notificationGroups.contains(group))
            notificationGroups.add(group);

        group.addNotification(notification);

        fireNotificationEvent(notification);
    }

    /**
     * Removes all unread notifications.
     *
     * @param group removes all unread notifications for the given notification
     * group
     */
    public static void removeAllNotifications(UINotificationGroup group)
    {
        group.removeAllNotifications();
    }

    /**
     * Removes all unread notifications from all notification groups.
     */
    public static void removeAllNotifications()
    {
        Iterator<UINotificationGroup> groups = notificationGroups.iterator();

        while (groups.hasNext())
        {
            groups.next().removeAllNotifications();
        }
    }

    /**
     * Returns a list of all unread notifications.
     *
     * @param group the notification group, which notification we're looking
     * for
     * @return a list of all unread notifications
     */
    public static Iterator<UINotification> getUnreadNotifications(
        UINotificationGroup group)
    {
        return group.getUnreadNotifications();
    }

    /**
     * Returns a list of all notification groups.
     *
     * @return a list of all notification groups
     */
    public static Collection<UINotificationGroup> getNotificationGroups()
    {
        return new ArrayList<UINotificationGroup>(notificationGroups);
    }

    /**
     * Notifies interested <tt>UINotificationListener</tt> that a new
     * notification has been received.
     *
     * @param notification the new notification
     */
    private static void fireNotificationEvent(UINotification notification)
    {
        synchronized (notificationListeners)
        {
            Iterator<UINotificationListener> listeners
                = notificationListeners.iterator();

            while (listeners.hasNext())
                listeners.next().notificationReceived(notification);
        }
    }
}
