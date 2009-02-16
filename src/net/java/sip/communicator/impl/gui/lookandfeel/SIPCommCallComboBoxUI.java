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

public class SIPCommCallComboBoxUI extends SIPCommComboBoxUI
{
    public static ComponentUI createUI(JComponent c)
    {
        return new SIPCommCallComboBoxUI();
    }

    /**
     * Creates the popup portion of the combo box.
     *
     * @return an instance of <code>ComboPopup</code>
     * @see ComboPopup
     */
    protected ComboPopup createPopup()
    {
        SIPCommComboPopup popup = new SIPCommComboPopup( comboBox );
        popup.getAccessibleContext().setAccessibleParent(comboBox);

        return popup;
    }
    
    private class SIPCommComboPopup extends BasicComboPopup
    {
        private static final long serialVersionUID = 0L;

		public SIPCommComboPopup(JComboBox combo)
        {
            super(combo);
        }

        /**
         * Makes the popup visible if it is hidden and makes it hidden if it is 
         * visible.
         */
        protected void togglePopup() {
            if ( isVisible() ) {
                hide();
            }
            else {
                setListSelection(comboBox.getSelectedIndex());

                Point location = getPopupLocation();
                show( comboBox, location.x, location.y );
            }
        }
        
        /**
         * Configures the popup portion of the combo box. This method is called
         * when the UI class is created.
         */
        protected void configurePopup() {
            setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
            setBorderPainted( true );
            setBorder(SIPCommBorders.getTextFieldBorder());
            setOpaque( false );
            add( scroller );
            setDoubleBuffered( true );
            setFocusable( false );
        }
        
        /**
         * Sets the list selection index to the selectedIndex. This 
         * method is used to synchronize the list selection with the 
         * combo box selection.
         * 
         * @param selectedIndex the index to set the list
         */
        private void setListSelection(int selectedIndex) {
            if ( selectedIndex == -1 ) {
                list.clearSelection();
            }
            else {
                list.setSelectedIndex( selectedIndex );
            list.ensureIndexIsVisible( selectedIndex );
            }
        }
        
        /**
         * Calculates the upper left location of the Popup.
         */
        private Point getPopupLocation() {
            Dimension popupSize = comboBox.getSize();
            Insets insets = getInsets();

            // reduce the width of the scrollpane by the insets so that the popup
            // is the same width as the combo box.
            int popupHeight = getPopupHeightForRowCount(
                    comboBox.getMaximumRowCount());
            
            popupSize.setSize(popupSize.width - (insets.right + insets.left), 
                              popupHeight);
            Rectangle popupBounds = computePopupBounds(
                    0,
                    comboBox.getEditor().getEditorComponent().getBounds().y
                        - popupHeight - 4,
                    popupSize.width, popupSize.height);
            
            Dimension scrollSize = popupBounds.getSize();
            Point popupLocation = popupBounds.getLocation();
                
            scroller.setMaximumSize( scrollSize );
            scroller.setPreferredSize( scrollSize );
            scroller.setMinimumSize( scrollSize );
            
            list.revalidate();
    
            return popupLocation;
        }
    }
}
