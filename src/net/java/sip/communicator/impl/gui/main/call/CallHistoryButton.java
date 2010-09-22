/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.call;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.main.contactlist.*;
import net.java.sip.communicator.impl.gui.utils.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.skin.*;
import net.java.sip.communicator.util.swing.*;

/**
 * The <tt>CallHistoryButton</tt> is the button shown on the top of the contact
 * list.
 * @author Yana Stamcheva
 * @author Adam Netocny
 */
public class CallHistoryButton
    extends SIPCommTextButton
    implements  MissedCallsListener,
                Skinnable
{
    /**
     * The history icon.
     */
    private Image historyImage;

    /**
     * The history pressed icon.
     */
    private Image pressedImage;

    /**
     * Indicates if the history is visible.
     */
    private boolean isHistoryVisible = false;

    /**
     * Indicates if this button currently shows the number of missed calls or
     * the just the history icon.
     */
    private boolean isMissedCallView = false;

    /**
     * The tool tip shown when there are missed calls.
     */
    private final static String missedCallsToolTip
        = GuiActivator.getResources().getI18NString(
            "service.gui.MISSED_CALLS_TOOL_TIP");

    /**
     * The tool tip shown by default over the history button. 
     */
    private final static String callHistoryToolTip
        = GuiActivator.getResources().getI18NString(
            "service.gui.CALL_HISTORY_TOOL_TIP");

    /**
     * The tool tip shown when we're in history view.
     */
    private final static String showContactListToolTip
        = GuiActivator.getResources().getI18NString(
            "service.gui.SHOW_CONTACT_LIST_TOOL_TIP");

    /**
     * Creates a <tt>CallHistoryButton</tt>.
     */
    public CallHistoryButton()
    {
        super("");

        CallManager.setMissedCallsListener(this);

        this.setPreferredSize(new Dimension(29, 22));
        this.setForeground(Color.WHITE);
        this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        this.setFont(getFont().deriveFont(Font.BOLD, 10f));
        this.setToolTipText(callHistoryToolTip);
        this.setBackground(new Color(255, 255, 255, 160));

        // All items are now instantiated and could safely load the skin.
        loadSkin();

        this.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (isHistoryVisible && !isMissedCallView)
                {
                    GuiActivator.getContactList()
                        .setDefaultFilter(TreeContactList.presenceFilter);
                    GuiActivator.getContactList().applyDefaultFilter();

                    isHistoryVisible = false;
                }
                else
                {
                    GuiActivator.getContactList()
                        .setDefaultFilter(TreeContactList.historyFilter);
                    GuiActivator.getContactList().applyDefaultFilter();

                    CallManager.clearMissedCalls();

                    isHistoryVisible = true;
                }
                setHistoryView();

                GuiActivator.getContactList().requestFocusInWindow();
                repaint();
            }
        });
    }

    /**
     * Indicates that missed calls count has changed.
     * @param missedCalls the list of missed calls
     */
    public void missedCallCountChanged(Collection<MissedCall> missedCalls)
    {
        if (!missedCalls.isEmpty())
        {
            setMissedCallsView(missedCalls);
        }
        else
        {
            setHistoryView();
        }

        this.revalidate();
        this.repaint();
    }

    /**
     * Sets the history view.
     */
    private void setHistoryView()
    {
        isMissedCallView = false;

        if (isHistoryVisible)
        {
            setBgImage(pressedImage);
            setToolTipText(showContactListToolTip);
        }
        else
        {
            setBgImage(historyImage);
            setToolTipText(callHistoryToolTip);
        }
        setText("");
    }

    /**
     * Sets the missed calls view of this button.
     * @param missedCalls the list of missed calls
     */
    private void setMissedCallsView(Collection<MissedCall> missedCalls)
    {
        isMissedCallView = true;
        this.setBgImage(null);

        String tooltipText = "<html><b>" + missedCallsToolTip + "</b><br/>";

        int visibleCallCount = 5;
        Iterator<MissedCall> callsIter = missedCalls.iterator();
        while (callsIter.hasNext() && visibleCallCount > 0)
        {
            MissedCall missedCall = callsIter.next();
            tooltipText += GuiUtils.formatTime(missedCall.getCallTime())
                        + "    "+ missedCall.getCallName() + "<br/>";
            visibleCallCount--;

            if (visibleCallCount == 0 && callsIter.hasNext())
                tooltipText
                    += GuiActivator.getResources()
                        .getI18NString("service.gui.MISSED_CALLS_MORE_TOOL_TIP",
                            new String[]{
                                new Integer(missedCalls.size() - 5).toString()});
        }
        this.setToolTipText(tooltipText + "</html>");

        this.setBackground(new Color(200, 0, 0));
        this.setText(new Integer(missedCalls.size()).toString());
    }

    /**
     * Loads images and sets history view.
     */
    public void loadSkin()
    {
        historyImage
            = ImageLoader.getImage(ImageLoader.CALL_HISTORY_BUTTON);

        pressedImage
            = ImageLoader.getImage(ImageLoader.CALL_HISTORY_BUTTON_PRESSED);

        setHistoryView();
    }
}