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
import net.java.sip.communicator.impl.gui.customcontrols.*;
import net.java.sip.communicator.impl.gui.utils.*;
import net.java.sip.communicator.service.contactlist.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.swing.*;

/**
 * The dialog created when an incoming call is received.
 *
 * @author Yana Stamcheva
 */
public class ReceivedCallDialog
    extends SIPCommFrame
    implements  ActionListener,
                CallListener
{
    private static final String CALL_BUTTON = "CallButton";

    private static final String HANGUP_BUTTON = "HangupButton";

    private static final int HGAP = 5;

    private Call incomingCall;

    /**
     * Creates a <tt>ReceivedCallDialog</tt> by specifying the associated call.
     *
     * @param call The associated with this dialog incoming call.
     */
    public ReceivedCallDialog(Call call)
    {
        super();

        this.incomingCall = call;

        this.setUndecorated(true);

        this.initComponents();

        OperationSetBasicTelephony telephonyOpSet
            = (OperationSetBasicTelephony) call.getProtocolProvider()
                .getOperationSet(OperationSetBasicTelephony.class);

        telephonyOpSet.addCallListener(this);
    }

    /**
     * Initializes all components in this panel.
     */
    private void initComponents()
    {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        JLabel callLabel = new JLabel();

        JPanel buttonsPanel = new TransparentPanel(new GridBagLayout());

        SIPCommButton callButton = new SIPCommButton(
            ImageLoader.getImage(ImageLoader.CALL_BUTTON_BG));

        SIPCommButton hangupButton = new SIPCommButton(
            ImageLoader.getImage(ImageLoader.HANGUP_BUTTON_BG));

        mainPanel.setPreferredSize(new Dimension(400, 90));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        callButton.setName(CALL_BUTTON);
        hangupButton.setName(HANGUP_BUTTON);

        callButton.addActionListener(this);
        hangupButton.addActionListener(this);

        this.initCallLabel(callLabel);

        this.getContentPane().add(mainPanel);

        GridBagConstraints mainConstraints = new GridBagConstraints();
        mainConstraints.anchor = GridBagConstraints.WEST;
        mainConstraints.gridx = 0;
        mainConstraints.gridy = 0;
        mainConstraints.weightx = 1;
        mainPanel.add(callLabel, mainConstraints);
        mainConstraints.anchor = GridBagConstraints.CENTER;
        mainConstraints.gridx = 1;
        mainConstraints.weightx = 0;
        mainPanel.add(Box.createHorizontalStrut(HGAP), mainConstraints);
        mainConstraints.anchor = GridBagConstraints.CENTER;
        mainConstraints.gridx = 2;
        mainConstraints.weightx = 0;
        mainPanel.add(buttonsPanel, mainConstraints);

        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridx = 0;
        buttonConstraints.gridy = 0;
        buttonsPanel.add(callButton, buttonConstraints);
        buttonConstraints.gridx = 1;
        buttonsPanel.add(Box.createHorizontalStrut(HGAP));
        buttonConstraints.gridx = 2;
        buttonsPanel.add(hangupButton, buttonConstraints);
    }

    /**
     * Initializes the label of the received call.
     *
     * @param callLabel The label to initialize.
     */
    private void initCallLabel(JLabel callLabel)
    {
        Iterator<CallParticipant> participantsIter
            = incomingCall.getCallParticipants();

        boolean hasMoreParticipants = false;
        String text = "";

        ImageIcon imageIcon =
            ImageUtils.getScaledRoundedIcon(ImageLoader
                .getImage(ImageLoader.DEFAULT_USER_PHOTO), 40, 45);

        while (participantsIter.hasNext())
        {
            CallParticipant participant = participantsIter.next();

            // More participants.
            if (participantsIter.hasNext())
            {
                text = callLabel.getText()
                    + participant.getDisplayName() + ", ";

                hasMoreParticipants = true;
            }
            // Only one participant.
            else
            {
                text = callLabel.getText()
                    + participant.getDisplayName()
                    + " "
                    + GuiActivator.getResources().getI18NString("service.gui.IS_CALLING");

                imageIcon = getParticipantImage(participant);
            }
        }

        if (hasMoreParticipants)
            text += GuiActivator.getResources()
                .getI18NString("service.gui.ARE_CALLING");

        callLabel.setIcon(imageIcon);
        callLabel.setText(text);
    }

    /**
     * Handles <tt>ActionEvent</tt>s triggered by pressing the call or the
     * hangup buttons.
     * @param e The <tt>ActionEvent</tt> to handle.
     */
    public void actionPerformed(ActionEvent e)
    {
        JButton button = (JButton) e.getSource();
        String buttonName = button.getName();

        if (buttonName.equals(CALL_BUTTON))
        {
            CallManager.answerCall(incomingCall);
        }
        else if (buttonName.equals(HANGUP_BUTTON))
        {
            CallManager.hangupCall(incomingCall);
        }

        this.dispose();
    }

    /**
     * Returns the participant image.
     *
     * @param participant The call participant, for which we're returning an
     * image.
     * @return the participant image.
     */
    private ImageIcon getParticipantImage(CallParticipant participant)
    {
        ImageIcon icon = null;
        // We search for a contact corresponding to this call participant and
        // try to get its image.
        if (participant.getContact() != null)
        {
            MetaContact metaContact = GuiActivator.getMetaContactListService()
                .findMetaContactByContact(participant.getContact());

            byte[] avatar = metaContact.getAvatar();

            if(avatar != null && avatar.length > 0)
                icon = new ImageIcon(avatar);
        }

        // If the icon is still null we try to get an image from the call
        // participant.
        if (icon == null && participant.getImage() != null)
            icon = new ImageIcon(participant.getImage());

        return icon;
    }

    @Override
    protected void close(boolean isEscaped)
    {
    }

    public void callEnded(CallEvent event)
    {
        this.dispose();
    }

    public void incomingCallReceived(CallEvent event)
    {
    }

    public void outgoingCallCreated(CallEvent event)
    {
    }
}
