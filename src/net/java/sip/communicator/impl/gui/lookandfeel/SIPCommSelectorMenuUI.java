/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.lookandfeel;

import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

import net.java.sip.communicator.impl.gui.utils.*;
import net.java.sip.communicator.util.swing.*;

/**
 * The SIPCommSelectorMenuUI implementation.
 * 
 * @author Yana Stamcheva
 */
public class SIPCommSelectorMenuUI
    extends BasicMenuUI
{
    private final Image menuBgImage
        = ImageLoader.getImage(ImageLoader.STATUS_SELECTOR_BOX);
    
    /**
     * Creates a new SIPCommSelectorMenuUI instance.
     */
    public static ComponentUI createUI(JComponent x)
    {
        return new SIPCommSelectorMenuUI();
    }

    /**
     * Draws the background of the menu item.
     * 
     * @param g the paint graphics
     * @param menuItem menu item to be painted
     * @param bgColor selection background color
     * @since 1.4
     */
    protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor)
    {
        g = g.create();
        try
        {
            AntialiasingManager.activateAntialiasing(g);

            int menuWidth = menuItem.getWidth();
            int menuHeight = menuItem.getHeight();

            g.drawImage(menuBgImage, 0, 0, menuWidth, menuHeight, null);
        }
        finally
        {
            g.dispose();
        }
    }
}
