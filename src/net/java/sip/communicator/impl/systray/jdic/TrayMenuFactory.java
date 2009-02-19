/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.systray.jdic;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import net.java.sip.communicator.impl.systray.*;
import net.java.sip.communicator.service.gui.*;

/**
 * The <tt>TrayMenu</tt> is the menu that appears when the user right-click
 * on the Systray icon.
 *
 * @author Nicolas Chamouard
 * @author Lubomir Marinov
 */
public final class TrayMenuFactory
{

    /**
     * Handles the <tt>ActionEvent</tt> when one of the menu items is selected.
     * 
     * @param evt the event containing the menu item name
     */
    private static void actionPerformed(ActionEvent evt)
    {
        Object source = evt.getSource();
        String itemName;
        if (source instanceof JMenuItem)
        {
            JMenuItem menuItem = (JMenuItem) source;
            itemName = menuItem.getName();
        }
        else
        {
            MenuItem menuItem = (MenuItem) source;
            itemName = menuItem.getName();
        }

        if (itemName.equals("settings"))
        {
            SystrayActivator.getUIService().setConfigurationWindowVisible(true);
        }
        else if (itemName.equals("service.gui.CLOSE"))
        {
            SystrayActivator.getUIService().beginShutdown();
        }
        else if (itemName.equals("addContact"))
        {
            ExportedWindow dialog =
                SystrayActivator.getUIService().getExportedWindow(
                    ExportedWindow.ADD_CONTACT_WINDOW);

            if (dialog != null)
                dialog.setVisible(true);
            else
                SystrayActivator
                    .getUIService()
                    .getPopupDialog()
                    .showMessagePopupDialog(
                        Resources
                            .getString("impl.systray.FAILED_TO_OPEN_ADD_CONTACT_DIALOG"));
        }
    }

    private static void add(Object trayMenu, Object trayMenuItem)
    {
        if (trayMenu instanceof JPopupMenu)
            ((JPopupMenu) trayMenu).add((JMenuItem) trayMenuItem);
        else
            ((PopupMenu) trayMenu).add((MenuItem) trayMenuItem);
    }

    public static void addPopupMenuListener(Object trayMenu,
        PopupMenuListener listener)
    {
        if (trayMenu instanceof JPopupMenu)
            ((JPopupMenu) trayMenu).addPopupMenuListener(listener);
    }

    private static void addSeparator(Object trayMenu)
    {
        if (trayMenu instanceof JPopupMenu)
            ((JPopupMenu) trayMenu).addSeparator();
        else
            ((PopupMenu) trayMenu).addSeparator();
    }

    public static Object createTrayMenu(SystrayServiceJdicImpl tray, boolean swing)
    {
    	swing = true;
        JPopupMenu trayMenu = new JPopupMenu();
        ActionListener listener = new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                TrayMenuFactory.actionPerformed(event);
            }
        };

        add(trayMenu, createTrayMenuItem("settings", "service.gui.SETTINGS",
            "service.gui.icons.QUICK_MENU_CONFIGURE_ICON", listener, swing));
        add(trayMenu, createTrayMenuItem("addContact",
            "service.gui.ADD_CONTACT",
            "service.gui.icons.ADD_CONTACT_16x16_ICON", listener, swing));
        addSeparator(trayMenu);
        add(trayMenu, new StatusSubMenu(tray, swing).getMenu());
        addSeparator(trayMenu);
        add(trayMenu, createTrayMenuItem("service.gui.CLOSE",
            "service.gui.CLOSE", "service.systray.CLOSE_MENU_ICON", listener,
            swing));

        return trayMenu;
    }

    private static Object createTrayMenuItem(String name, String textID, String iconID,
        ActionListener listener, boolean swing)
    {
        String text = Resources.getString(textID);
        
        JMenuItem menuItem = new JMenuItem(text, Resources.getImage(iconID));
        menuItem.setName(name);
        menuItem.addActionListener(listener);
        return menuItem;
    }

    public static boolean isVisible(Object trayMenu)
    {
        if (trayMenu instanceof JPopupMenu)
            return ((JPopupMenu) trayMenu).isVisible();
        return false;
    }
}
