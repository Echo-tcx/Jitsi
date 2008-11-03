/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package net.java.sip.communicator.impl.gui.main.chat.toolBars;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.event.*;
import net.java.sip.communicator.impl.gui.i18n.*;
import net.java.sip.communicator.impl.gui.main.chat.*;
import net.java.sip.communicator.impl.gui.main.chat.history.*;
import net.java.sip.communicator.impl.gui.main.contactlist.addcontact.*;
import net.java.sip.communicator.impl.gui.utils.*;
import net.java.sip.communicator.service.contactlist.*;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.gui.Container;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;

import org.osgi.framework.*;

/**
 * The <tt>MainToolBar</tt> is a <tt>JToolBar</tt> which contains buttons
 * for file operations, like save and print, for copy-paste operations, etc.
 * It's the main toolbar in the <tt>ChatWindow</tt>. It contains only
 * <tt>ChatToolbarButton</tt>s, which have a specific background icon and
 * rollover behaviour to differentiates them from normal buttons.
 * 
 * @author Yana Stamcheva
 */
public class ExtendedMainToolBar
    extends MainToolBar
    implements  MouseListener,
                PluginComponentListener
{
    private Logger logger = Logger.getLogger(ExtendedMainToolBar.class);

    BufferedImage backgroundImage
        = ImageLoader.getImage(ImageLoader.TOOL_BAR_BACKGROUND);

    Rectangle rectangle
        = new Rectangle(0, 0,
                    backgroundImage.getWidth(null),
                    backgroundImage.getHeight(null));

    TexturePaint texture = new TexturePaint(backgroundImage, rectangle);

    private ToolBarButton copyButton = new ToolBarButton(
        ImageLoader.getImage(ImageLoader.COPY_ICON));

    private ToolBarButton cutButton = new ToolBarButton(
        ImageLoader.getImage(ImageLoader.CUT_ICON));

    private ToolBarButton pasteButton = new ToolBarButton(
        ImageLoader.getImage(ImageLoader.PASTE_ICON));

    private ToolBarButton saveButton = new ToolBarButton(
        ImageLoader.getImage(ImageLoader.SAVE_ICON));

    private ToolBarButton printButton = new ToolBarButton(
        ImageLoader.getImage(ImageLoader.PRINT_ICON));

    private ToolBarButton previousButton = new ToolBarButton(
        ImageLoader.getImage(ImageLoader.PREVIOUS_ICON));

    private ToolBarButton nextButton = new ToolBarButton(
        ImageLoader.getImage(ImageLoader.NEXT_ICON));

    private ToolBarButton historyButton = new ToolBarButton(
        ImageLoader.getImage(ImageLoader.HISTORY_ICON));
    
    private ToolBarButton addButton = new ToolBarButton(
        ImageLoader.getImage(ImageLoader.QUICK_MENU_ADD_ICON));
    
    private ToolBarButton sendFileButton = new ToolBarButton(
        ImageLoader.getImage(ImageLoader.SEND_FILE_ICON));

    private ToolBarButton fontButton = new ToolBarButton(
        ImageLoader.getImage(ImageLoader.FONT_ICON));

    private ToolBarButton settingsButton = new ToolBarButton(
        ImageLoader.getImage(ImageLoader.QUICK_MENU_CONFIGURE_ICON));

    private static int DEFAULT_BUTTON_HEIGHT
        = GuiActivator.getResources().getSettingsInt("mainToolbarButtonHeight");

    private static int DEFAULT_BUTTON_WIDTH
        = GuiActivator.getResources().getSettingsInt("mainToolbarButtonWidth");

    private ChatWindow messageWindow;
    
    private Contact currentChatContact = null;

    /**
     * Creates an instance and constructs the <tt>MainToolBar</tt>.
     * 
     * @param messageWindow The parent <tt>ChatWindow</tt>.
     */
    public ExtendedMainToolBar(ChatWindow messageWindow)
    {
        this.messageWindow = messageWindow;

        this.setRollover(true);
        this.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.setPreferredSize(new Dimension(300, DEFAULT_BUTTON_HEIGHT + 5));

//        this.setBorder(BorderFactory.createEmptyBorder(2, 2, 5, 2));

//        this.add(saveButton);
//        this.add(printButton);

//        this.addSeparator();

        this.add(cutButton);
        this.add(copyButton);
        this.add(pasteButton);

        this.addSeparator();

        this.add(settingsButton);

        this.addSeparator();

        this.add(previousButton);
        this.add(nextButton);

        this.addSeparator();

//        this.add(sendFileButton);
        this.add(historyButton);
        
        this.addSeparator();
        this.add(addButton);

//        this.addSeparator();
//
//        this.add(fontButton);

        this.saveButton.setName("save");
        this.saveButton.setToolTipText(
            Messages.getI18NString("save").getText() + " Ctrl-S");

        this.printButton.setName("print");
        this.printButton.setToolTipText(
            Messages.getI18NString("print").getText());

        this.cutButton.setName("cut");
        this.cutButton.setToolTipText(
            Messages.getI18NString("cut").getText() + " Ctrl-X");

        this.copyButton.setName("copy");
        this.copyButton.setToolTipText(
            Messages.getI18NString("copy").getText() + " Ctrl-C");

        this.pasteButton.setName("paste");
        this.pasteButton.setToolTipText(
            Messages.getI18NString("paste").getText() + " Ctrl-P");

        this.previousButton.setName("previous");
        this.previousButton.setToolTipText(
            Messages.getI18NString("previousTooltip").getText());

        this.nextButton.setName("next");
        this.nextButton.setToolTipText(
            Messages.getI18NString("nextTooltip").getText());

        this.sendFileButton.setName("sendFile");
        this.sendFileButton.setToolTipText(
            Messages.getI18NString("sendFile").getText());

        this.historyButton.setName("history");
        this.historyButton.setToolTipText(
            Messages.getI18NString("history").getText() + " Ctrl-H");

        this.addButton.setName("addContact");
        this.addButton.setToolTipText(
            Messages.getI18NString("addContact").getText());

        this.fontButton.setName("font");
        this.fontButton.setToolTipText(
            Messages.getI18NString("font").getText());

        this.settingsButton.setName("settings");
        this.settingsButton.setToolTipText(
            Messages.getI18NString("settings").getText());

        this.saveButton.setPreferredSize(
            new Dimension(DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT));
        this.printButton.setPreferredSize(
            new Dimension(DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT));
        this.cutButton.setPreferredSize(
            new Dimension(DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT));
        this.copyButton.setPreferredSize(
            new Dimension(DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT));
        this.pasteButton.setPreferredSize(
            new Dimension(DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT));
        this.previousButton.setPreferredSize(
            new Dimension(DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT));
        this.nextButton.setPreferredSize(
            new Dimension(DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT));
        this.sendFileButton.setPreferredSize(
            new Dimension(DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT));
        this.historyButton.setPreferredSize(
            new Dimension(DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT));
        this.fontButton.setPreferredSize(
            new Dimension(DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT));
        this.settingsButton.setPreferredSize(
            new Dimension(DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT));

        this.saveButton.addMouseListener(this);
        this.printButton.addMouseListener(this);
        this.cutButton.addMouseListener(this);
        this.copyButton.addMouseListener(this);
        this.pasteButton.addMouseListener(this);
        this.previousButton.addMouseListener(this);
        this.nextButton.addMouseListener(this);
        this.sendFileButton.addMouseListener(this);
        this.historyButton.addMouseListener(this);
        this.addButton.addMouseListener(this);
        this.fontButton.addMouseListener(this);
        this.settingsButton.addMouseListener(this);

        // Disable all buttons that do nothing.
        this.saveButton.setEnabled(false);
        this.printButton.setEnabled(false);
        this.sendFileButton.setEnabled(false);
        this.fontButton.setEnabled(false);
        
        this.initPluginComponents();
        
        messageWindow.addChatChangeListener(new ChatChangeListener() 
        {
            public void chatChanged(ChatPanel panel) 
            {
                if(panel.getChatSession() instanceof MetaContactChatSession)
                { 
                    MetaContact contact = 
                        (MetaContact) panel.getChatSession().getDescriptor();

                    if(contact == null) return;

                    Contact defaultContact = contact.getDefaultContact();
                    if(defaultContact == null) return;

                    ContactGroup parent = defaultContact.getParentContactGroup();
                    boolean isParentPersist = true;
                    boolean isParentResolved = true;
                    if(parent != null)
                    {
                        isParentPersist = parent.isPersistent();
                        isParentResolved = parent.isResolved();
                    }
                    
                    if(!defaultContact.isPersistent() &&
                       !defaultContact.isResolved() &&
                       !isParentPersist &&
                       !isParentResolved)
                    {
                       addButton.setVisible(true);
                       currentChatContact = defaultContact;
                    }
                    else
                    {
                        addButton.setVisible(false);
                        currentChatContact = null;
                    }  
                }
            }
        });
    }        

    /**
     * Handles the <tt>ActionEvent</tt>, when one of the toolbar buttons is
     * clicked.
     */
    public void mousePressed(MouseEvent e)
    {
        ToolBarButton button = (ToolBarButton) e.getSource();
        button.setMousePressed(true);
    }

    private void initPluginComponents()
    {
        // Search for plugin components registered through the OSGI bundle
        // context.
        ServiceReference[] serRefs = null;

        String osgiFilter = "("
            + Container.CONTAINER_ID
            + "="+Container.CONTAINER_CHAT_TOOL_BAR.getID()+")";

        try
        {
            serRefs = GuiActivator.bundleContext.getServiceReferences(
                PluginComponent.class.getName(),
                osgiFilter);
        }
        catch (InvalidSyntaxException exc)
        {
            logger.error("Could not obtain plugin reference.", exc);
        }

        if (serRefs != null)
        {
            for (int i = 0; i < serRefs.length; i ++)
            {
                PluginComponent component = (PluginComponent) GuiActivator
                    .bundleContext.getService(serRefs[i]);;

                this.add((Component)component.getComponent());

                this.revalidate();
                this.repaint();
            }
        }

        GuiActivator.getUIService().addPluginComponentListener(this);
    }

    private class ToolBarButton
        extends JLabel
    {
        private Image iconImage;

        private boolean isMouseOver = false;

        private boolean isMousePressed = false;

        public ToolBarButton(Image iconImage)
        {
            super(new ImageIcon(iconImage));

            this.setFont(getFont().deriveFont(Font.BOLD, 10f));
            this.setForeground(new Color(
                GuiActivator.getResources().getColor("toolBarForeground")));

            this.setVerticalTextPosition(SwingConstants.BOTTOM);
            this.setHorizontalTextPosition(SwingConstants.CENTER);
        }

        public void setMouseOver(boolean isMouseOver)
        {
            this.isMouseOver = isMouseOver;
            this.repaint();
        }

        public void setMousePressed(boolean isMousePressed)
        {
            this.isMousePressed = isMousePressed;
            this.repaint();
        }

        public void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g;

            AntialiasingManager.activateAntialiasing(g2);

            Color color = null;

            if(isMouseOver)
            {
                color = new Color(
                    GuiActivator.getResources()
                    .getColor("toolbarRolloverBackground"));

                g2.setColor(color);

                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 2, 8, 8);
            }

            if (isMousePressed)
            {
                color = new Color(
                    GuiActivator.getResources().getColor("toolbarBackground"));

                g2.setColor(new Color(   color.getRed(),
                                        color.getGreen(),
                                        color.getBlue(),
                                        100));

                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 2, 8, 8);
            }

            super.paintComponent(g2);
        }

        public Action getAction()
        {
            return null;
        }
    }

    public void mouseClicked(MouseEvent e)
    {
        JLabel button = (JLabel) e.getSource();
        String buttonText = button.getName();

        ChatPanel chatPanel = messageWindow.getCurrentChatPanel();
        
        if (buttonText.equalsIgnoreCase("save")) {
            // TODO: Implement the save operation in chat MainToolBar.
        }
        else if (buttonText.equalsIgnoreCase("print")) {
            // TODO: Implement the print operation in chat MainToolBar.
        }
        else if (buttonText.equalsIgnoreCase("cut")) {

            chatPanel.cut();
        }
        else if (buttonText.equalsIgnoreCase("copy")) {

            chatPanel.copy();
        }
        else if (buttonText.equalsIgnoreCase("paste")) {

            chatPanel.paste();
        }
        else if (buttonText.equalsIgnoreCase("previous"))
        {   
            chatPanel.loadPreviousPageFromHistory();
        }
        else if (buttonText.equalsIgnoreCase("next"))
        {   
            chatPanel.loadNextPageFromHistory();
        }
        else if (buttonText.equalsIgnoreCase("sendFile")) {

        }
        else if (buttonText.equalsIgnoreCase("history"))
        {
            HistoryWindow history;

            HistoryWindowManager historyWindowManager
                = GuiActivator.getUIService().getHistoryWindowManager();

            Object historyContact = chatPanel.getChatSession().getDescriptor();

            if(historyWindowManager
                .containsHistoryWindowForContact(historyContact))
            {
                history = historyWindowManager
                    .getHistoryWindowForContact(historyContact);

                if(history.getState() == JFrame.ICONIFIED)
                    history.setState(JFrame.NORMAL);
                
                history.toFront();
            }
            else
            {
                history = new HistoryWindow(
                    chatPanel.getChatSession().getDescriptor());

                history.setVisible(true);

                historyWindowManager.addHistoryWindowForContact(historyContact,
                                                                history);
            }
        }
        else if (buttonText.equalsIgnoreCase("addContact")) 
        {
            if(currentChatContact != null)
            {
                AddContactWizard addCWizz = 
                        new AddContactWizard(
                            GuiActivator.getUIService().getMainFrame(),
                            currentChatContact.getAddress(),
                            currentChatContact.getProtocolProvider()
                        );

                addCWizz.setVisible(true);
            }
        }
        else if (buttonText.equalsIgnoreCase("settings"))
        {
            ExportedWindow configDialog = GuiActivator.getUIService()
                .getExportedWindow(ExportedWindow.CONFIGURATION_WINDOW);

            configDialog.setVisible(true);
        }
    }

    public void mouseEntered(MouseEvent e)
    {
        ToolBarButton button = (ToolBarButton) e.getSource();
        button.setMouseOver(true);
    }

    public void mouseExited(MouseEvent e)
    {
        ToolBarButton button = (ToolBarButton) e.getSource();
        button.setMouseOver(false);
    }

    public void mouseReleased(MouseEvent e)
    {
        ToolBarButton button = (ToolBarButton) e.getSource();
        button.setMousePressed(false);
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        if (backgroundImage != null)
        {
            Graphics2D g2 = (Graphics2D) g;

            g2.setPaint(texture);

            g2.fillRect(0, 2, this.getWidth(), this.getHeight() - 2);

            g2.setColor(new Color(
                GuiActivator.getResources()
                .getColor("desktopBackgroundColor")));

            g2.drawRect(0, this.getHeight() - 2, this.getWidth(), 2);
        }
    }
}