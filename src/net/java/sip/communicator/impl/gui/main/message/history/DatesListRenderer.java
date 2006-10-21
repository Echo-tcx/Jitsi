/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.message.history;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import net.java.sip.communicator.impl.gui.utils.*;

public class DatesListRenderer
    extends JPanel
    implements ListCellRenderer
{
    private JLabel label = new JLabel();
    private boolean isSelected;
    
    private Calendar calendar = Calendar.getInstance();
    
    public DatesListRenderer()
    {
        super(new BorderLayout());
        
        this.add(label);
    }
    
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus)
    {
        Date dateValue = (Date) value;
        
        calendar.setTime(dateValue);
        String text = GuiUtils.formatDate(dateValue);
        
        this.label.setText(text);
        this.isSelected = isSelected;
        
        return this;
    }
    
    /**
     * Paint a round background for all selected cells.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        AntialiasingManager.activateAntialiasing(g2);
        
        if (this.isSelected) {

            g2.setColor(Constants.SELECTED_END_COLOR);
            g2.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 7, 7);

            g2.setColor(Constants.BLUE_GRAY_BORDER_DARKER_COLOR);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1,
                    7, 7);
        }
    }   
}
