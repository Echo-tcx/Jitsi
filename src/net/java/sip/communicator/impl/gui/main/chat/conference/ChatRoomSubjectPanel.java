/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.chat.conference;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.customcontrols.*;
import net.java.sip.communicator.impl.gui.utils.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.resources.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.skin.*;
import net.java.sip.communicator.util.swing.*;

/**
 * The panel containing the subject of the chat room and the configuration
 * button.
 * 
 * @author Yana Stamcheva
 * @author Lubomir Marinov
 * @author Adam Netocny
 */
public class ChatRoomSubjectPanel
    extends TransparentPanel
    implements Skinnable
{
    /**
     * The <tt>Logger</tt> used by the <tt>ChatRoomSubjectPanel</tt> class and
     * its instances for logging output.
     */
    private static final Logger logger
        = Logger.getLogger(ChatRoomSubjectPanel.class);

    /**
     * The corresponding chat session.
     */
    private final ConferenceChatSession chatSession;

    /**
     * The field containing the subject of the chat room.
     */
    private final JTextField subjectField = new JTextField();

    /**
     * Config button.
     */
    private JButton configButton;

    /**
     * Creates the panel containing the chat room subject.
     *
     * @param chatSession the chat session
     * chat room subject and the configuration information.
     */
    public ChatRoomSubjectPanel(ConferenceChatSession chatSession)
    {
        super(new BorderLayout(5, 5));

        this.chatSession = chatSession;

        JLabel subjectLabel
            = new JLabel(
                    GuiActivator.getResources().getI18NString(
                            "service.gui.SUBJECT") + ": ");

        subjectField.setText(chatSession.getChatSubject());
        // TODO Implement the editing of the chat room subject.
        subjectField.setEditable(false);

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(subjectLabel, BorderLayout.WEST);
        add(subjectField, BorderLayout.CENTER);

        ChatRoomMemberRole role = ((ChatRoomWrapper)chatSession.getDescriptor())
                .getChatRoom().getUserRole();

        if(role.equals(ChatRoomMemberRole.ADMINISTRATOR)
            || role.equals(ChatRoomMemberRole.MODERATOR)
            || role.equals(ChatRoomMemberRole.OWNER))
        {
            configButton
                = new JButton(
                        new ImageIcon(
                                ImageLoader.getImage(
                                        ImageLoader.CHAT_ROOM_CONFIG)));
            configButton.setPreferredSize(new Dimension(26, 26));
            configButton.addActionListener(new ConfigButtonActionListener());

            add(configButton, BorderLayout.EAST);
        }
    }

    /**
     * Gets the (chat room) subject displayed in this
     * <tt>ChatRoomSubjectPanel</tt>.
     *
     * @return the (chat room) subject displayed in this
     * <tt>ChatRoomSubjectPanel</tt>
     */
    public String getSubject()
    {
        return subjectField.getText();
    }

    /**
     * Sets the (chat room) subject to be displayed in this
     * <tt>ChatRoomSubjectPanel</tt>.
     * 
     * @param subject the (chat room) subject to be displayed in this
     * <tt>ChatRoomSubjectPanel</tt>
     */
    public void setSubject(String subject)
    {
        subjectField.setText(subject);
    }

    /**
     * Opens the configuration dialog when the configure buttons is pressed.
     */
    private class ConfigButtonActionListener
        implements ActionListener
    {
        /**
         * Obtains and opens the configuration form of the corresponding chat
         * room when user clicks on the configuration button.
         * @param evt the <tt>ActionEvent</tt> that notified us
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

                configWindow.pack();
                configWindow.setVisible(true);
            }
            catch (OperationFailedException e)
            {
                logger.error(
                    "Failed to obtain the chat room configuration form.", e);

                ResourceManagementService resources
                    = GuiActivator.getResources();

                if(e.getErrorCode()
                    == OperationFailedException.NOT_ENOUGH_PRIVILEGES)
                {
                    new ErrorDialog(
                        null,
                        resources.getI18NString("service.gui.WARNING"),
                        resources.getI18NString(
                                "service.gui.CHAT_ROOM_CONFIGURATION_FORBIDDEN",
                                new String[]{chatSession.getChatName()}),
                        ErrorDialog.WARNING)
                    .showDialog();
                }
                else
                {
                    new ErrorDialog(
                        null,
                        resources.getI18NString("service.gui.ERROR"),
                        resources.getI18NString(
                                "service.gui.CHAT_ROOM_CONFIGURATION_FAILED",
                                new String[]{chatSession.getChatName()}),
                        e)
                    .showDialog();
                }
            }
        }
    }

    /**
     * Reload config button if exists.
     */
    public void loadSkin()
    {
        if(configButton != null)
            configButton.setIcon(new ImageIcon(
                    ImageLoader.getImage(ImageLoader.CHAT_ROOM_CONFIG)));
    }
}
