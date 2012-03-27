/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.call;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import net.java.sip.communicator.util.swing.*;

/**
 * The dialog created for a given call.
 *
 * @author Yana Stamcheva
 * @author Adam Netocny
 */
public class CallDialog
    extends SIPCommFrame
    implements  CallContainer,
                CallTitleListener
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 0L;

    /**
     * The panel, where all call components are added.
     */
    private CallPanel callPanel;

    /**
     * Creates a <tt>CallDialog</tt> by specifying the underlying call panel.
     */
    public CallDialog()
    {
        super(false);

        setMinimumSize(new Dimension(360, 300));
    }

    /**
     * Overrides getMinimumSize and checks the minimum size that
     * is needed to display buttons and use it for minimum size if
     * needed.
     * @return minimum size.
     */
    public Dimension getMinimumSize()
    {
        Dimension minSize = super.getMinimumSize();
        if(callPanel != null)
        {
            int minButtonWidth = callPanel.getMinimumButtonWidth();
            if(minButtonWidth > minSize.getWidth())
            {
                return new Dimension(minButtonWidth, 300);
            }
        }

        return minSize;
    }

    /**
     * Adds a call panel.
     *
     * @param callPanel the call panel to add to this dialog
     */
    public void addCallPanel(CallPanel callPanel)
    {
        this.callPanel = callPanel;

        getContentPane().add(callPanel);

        this.setTitle(callPanel.getCallTitle());
        callPanel.addCallTitleListener(this);

        if (!isVisible())
        {
            pack();
            setVisible(true);
        }
    }

    /**
     * Closes the given call panel.
     *
     * @param callPanel the <tt>CallPanel</tt> to close
     */
    public void close(CallPanel callPanel)
    {
        if (this.callPanel.equals(callPanel))
        {
            this.callPanel.disposeCallInfoFrame();
            dispose();
        }
    }

    /**
     * Closes the given call panel.
     *
     * @param callPanel the <tt>CallPanel</tt> to close
     */
    public void closeWait(CallPanel callPanel)
    {
        if (this.callPanel.equals(callPanel))
        {
            disposeWait();
        }
    }

    /**
     * Hang ups the current call on close.
     * @param isEscaped indicates if the window was close by pressing the escape
     * button
     */
    protected void close(boolean isEscaped)
    {
        callPanel.actionPerformedOnHangupButton(isEscaped);
    }

    /**
     * Indicates if the given <tt>callPanel</tt> is currently visible.
     *
     * @param callPanel the <tt>CallPanel</tt>, for which we verify
     * @return <tt>true</tt> if the given call container is visible in this
     * call window, otherwise - <tt>false</tt>
     */
    public boolean isCallVisible(CallPanel callPanel)
    {
        if (this.callPanel.equals(callPanel))
            return isVisible();

        return false;
    }

    /**
     * Returns the frame of the call window.
     *
     * @return the frame of the call window
     */
    public JFrame getFrame()
    {
        return this;
    }

    /**
     * Called when the title of the given <tt>CallPanel</tt> changes.
     *
     * @param callPanel the <tt>CallPanel</tt>, which title has changed
     */
    public void callTitleChanged(CallPanel callPanel)
    {
        if (this.callPanel.equals(callPanel))
            this.setTitle(callPanel.getCallTitle());
    }

    /**
     * Removes the given call panel tab.
     */
    public void disposeWait()
    {
        Timer timer
            = new Timer(5000, new DisposeCallDialogListener());

        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Removes the given CallPanel from the main tabbed pane.
     */
    private class DisposeCallDialogListener
        implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            dispose();
        }
    }
}
