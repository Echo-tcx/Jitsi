/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.chat.conference;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import net.java.sip.communicator.impl.gui.customcontrols.*;
import net.java.sip.communicator.impl.gui.i18n.*;
import net.java.sip.communicator.impl.gui.main.chat.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.swing.*;
import net.java.sip.communicator.util.*;

/**
 * The panel containing the subject of the chat room and the configuration
 * button.
 * 
 * @author Yana Stamcheva
 */
public class ChatRoomSubjectPanel
    extends JPanel
{
    private Logger logger = Logger.getLogger(ChatRoomSubjectPanel.class);

    private JLabel subjectLabel = new JLabel(
        Messages.getI18NString("subject").getText() + ": ");

    private JTextField subjectField = new JTextField();

    private JButton configButton = new JButton(new ImageIcon(
        ImageLoader.getImage(ImageLoader.QUICK_MENU_CONFIGURE_ICON)));

    private ConferenceChatSession chatSession;

    /**
     * The parent window.
     */
    private ChatWindow chatWindow;
    
    /**
     * Creates the panel containing the chat room subject.
     * 
     * @param chatWindow the chat window, where this panel is added
     * @param chatRoomWrapper the chat room wrapper, from which we obtain the
     * chat room subject and the configuration information.
     */
    public ChatRoomSubjectPanel(ChatWindow chatWindow,
                                ConferenceChatSession chatSession)
    {
        super(new BorderLayout(5, 5));

        this.chatSession = chatSession;
        this.chatWindow = chatWindow;

        this.add(subjectLabel, BorderLayout.WEST);
        this.add(subjectField, BorderLayout.CENTER);
        this.add(configButton, BorderLayout.EAST);

        this.configButton.setPreferredSize(new Dimension(26, 26));

        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        this.configButton.addActionListener(new ConfigButtonActionListener());

        this.subjectField.setText(chatSession.getChatSubject());

        // The subject is set not editable until we implement this functionality.
        // TODO: Implement the editing of the chat room subject
        this.subjectField.setEditable(false);
    }

    /**
     * Sets the subject in the corresponding field.
     * 
     * @param subject the subject of the chat room
     */
    public void setSubject(String subject)
    {
        this.subjectField.setText(subject);
    }

    /*
     * Opens the configuration dialog when the configure buttons is pressed.
     */
    private class ConfigButtonActionListener implements ActionListener
    {
        /**
         * Obtains and opens the configuration form of the corresponding chat
         * room when user clicks on the configuration button.
         */
        public void actionPerformed(ActionEvent evt)
        {
            try
            {
                ChatRoomConfigurationForm configForm
                    = chatSession.getChatConfigurationForm();

                ChatRoomConfigurationWindow configWindow
                    = new ChatRoomConfigurationWindow(
                        chatSession.getChatName(), configForm);

                configWindow.setVisible(true);
            }
            catch (OperationFailedException e)
            {
                logger.error(
                    "Failed to obtain the chat room configuration form.",
                    e);
                
                if(e.getErrorCode()
                    == OperationFailedException.NOT_ENOUGH_PRIVILEGES)
                {
                    new ErrorDialog(
                        chatWindow,
                        Messages.getI18NString("warning").getText(),
                        Messages.getI18NString(
                            "chatRoomOpenConfigForbidden",
                            new String[]{chatSession.getChatName()})
                            .getText(),
                        ErrorDialog.WARNING)
                            .showDialog();
                }
                else
                {
                    new ErrorDialog(
                        chatWindow,
                        Messages.getI18NString("error").getText(),
                        Messages.getI18NString(
                            "chatRoomOpenConfigFailed",
                            new String[]{
                            chatSession.getChatName()})
                            .getText(),
                        e).showDialog();
                }
            }
        }
    }
}
