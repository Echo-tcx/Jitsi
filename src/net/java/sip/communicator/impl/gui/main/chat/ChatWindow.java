/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.chat;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;

import javax.swing.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.customcontrols.*;
import net.java.sip.communicator.impl.gui.event.*;
import net.java.sip.communicator.impl.gui.main.chat.menus.*;
import net.java.sip.communicator.impl.gui.main.chat.toolBars.*;
import net.java.sip.communicator.impl.gui.main.contactlist.addcontact.*;
import net.java.sip.communicator.impl.gui.utils.*;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.gui.Container;
import net.java.sip.communicator.service.keybindings.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.swing.*;
import net.java.sip.communicator.util.swing.event.*;

import org.osgi.framework.*;

/**
 * The chat window is the place, where users can write and send messages, view
 * received messages. The ChatWindow supports two modes of use: "Group all
 * messages in one window" and "Open messages in new window". In the first case
 * a <tt>JTabbedPane</tt> is added in the window, where each tab contains a
 * <tt>ChatPanel</tt>. In the second case the <tt>ChatPanel</tt> is added
 * like a "write message area", "send" button, etc. It corresponds to a
 * <tt>MetaContact</tt> or to a conference.
 * <p>
 * Note that the conference case is not yet implemented.
 * 
 * @author Yana Stamcheva
 */
public class ChatWindow
    extends SIPCommFrame
    implements  ExportedWindow,
                PluginComponentListener
{
    private final Logger logger = Logger.getLogger(ChatWindow.class);

    private SIPCommTabbedPane chatTabbedPane = null;

    private int chatCount = 0;

    private final java.util.List<ChatChangeListener> chatChangeListeners =
        new Vector<ChatChangeListener>();

    private final JPanel mainPanel = new JPanel(new BorderLayout());

    private final JPanel northPanel = new JPanel(new BorderLayout());

    private final JPanel statusBarPanel = new JPanel(new BorderLayout());

    private final JPanel pluginPanelNorth = new JPanel();
    private final JPanel pluginPanelSouth = new JPanel();
    private final JPanel pluginPanelWest = new JPanel();
    private final JPanel pluginPanelEast = new JPanel();

    private final ContactPhotoPanel contactPhotoPanel = new ContactPhotoPanel();

    private MessageWindowMenuBar menuBar;

    private MainToolBar mainToolBar;

    /**
     * Creates an instance of <tt>ChatWindow</tt> by passing to it an instance
     * of the main application window.
     * 
     * @param mainFrame the main application window
     */
    public ChatWindow()
    {
        if (!ConfigurationManager.isWindowDecorated())
        {
            this.setUndecorated(true);
        }

        this.setHierarchicallyOpaque(false);

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        //If in mode TABBED_CHAT_WINDOW initialize the tabbed pane
        if(ConfigurationManager.isMultiChatWindowEnabled())
        {
            chatTabbedPane = new SIPCommTabbedPane(true, false);

            chatTabbedPane.addCloseListener(new CloseListener() {
                public void closeOperation(MouseEvent e)
                {
                    int tabIndex = chatTabbedPane.getOverTabIndex();

                    ChatPanel chatPanel
                        = (ChatPanel) chatTabbedPane.getComponentAt(tabIndex);

                    GuiActivator.getUIService()
                        .getChatWindowManager().closeChat(chatPanel);
                }
            });
        }

        menuBar = new MessageWindowMenuBar(this);

        this.setJMenuBar(menuBar);

        boolean isToolBarExtended
            = new Boolean(GuiActivator.getResources().
                getSettingsString("impl.gui.IS_TOOLBAR_EXTENDED"))
                    .booleanValue();

        if (isToolBarExtended)
            mainToolBar = new ExtendedMainToolBar(this);
        else
            mainToolBar = new MainToolBar(this);

        northPanel.add(new LogoBar(), BorderLayout.NORTH);
        northPanel.add(mainToolBar, BorderLayout.CENTER);
        northPanel.add(contactPhotoPanel, BorderLayout.EAST);

        this.mainPanel.add(northPanel, BorderLayout.NORTH);

        this.getContentPane().add(mainPanel, BorderLayout.CENTER);
        this.getContentPane().add(statusBarPanel, BorderLayout.SOUTH);

        this.initPluginComponents();

        this.setKeybindingInput(KeybindingSet.Category.CHAT);
        this.addKeybindingAction("plugin.keybindings.CHAT_NEXT_TAB", new ForwordTabAction());
        this.addKeybindingAction("plugin.keybindings.CHAT_PREVIOUS_TAB", new BackwordTabAction());
        this.addKeybindingAction("plugin.keybindings.CHAT_COPY", new CopyAction());
        this.addKeybindingAction("plugin.keybindings.CHAT_PASTE", new PasteAction());
        this.addKeybindingAction("chat-openSmilies", new OpenSmileyAction());
        this.addKeybindingAction("chat-openHistory", new OpenHistoryAction());
        this.addKeybindingAction("chat-close", new CloseAction());

        this.addWindowListener(new ChatWindowAdapter());

        int width = GuiActivator.getResources()
            .getSettingsInt("impl.gui.CHAT_WINDOW_WIDTH");
        int height = GuiActivator.getResources()
            .getSettingsInt("impl.gui.CHAT_WINDOW_HEIGHT");

        this.setSize(width, height);
    }

    /**
     * Sets the given isOpaque property to this panel and all its contained
     * components.
     * 
     * @param isOpaque <code>true</code> to set this panel paque and
     * <code>false</code> - otherwise.
     */
    public void setHierarchicallyOpaque(boolean isOpaque)
    {
        northPanel.setOpaque(isOpaque);
        mainPanel.setOpaque(isOpaque);
        statusBarPanel.setOpaque(isOpaque);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.java.sip.communicator.impl.gui.customcontrols.SIPCommFrame#dispose()
     */
    public void dispose()
    {
        try
        {
            GuiActivator.getUIService().removePluginComponentListener(this);
            mainToolBar.dispose();
            menuBar.dispose();
        }
        finally
        {
            super.dispose();
        }
    }

    /**
     * Returns the main toolbar in this chat window.
     * @return the main toolbar in this chat window
     */
    public MainToolBar getMainToolBar()
    {
        return mainToolBar;
    }
    
    /**
     * Adds a given <tt>ChatPanel</tt> to this chat window.
     * 
     * @param chatPanel The <tt>ChatPanel</tt> to add.
     */
    public void addChat(ChatPanel chatPanel)
    {
        if (ConfigurationManager.isMultiChatWindowEnabled())
            addChatTab(chatPanel);
        else
            addSimpleChat(chatPanel);

        chatCount ++;

        chatPanel.setShown(true);
    }

    /**
     * Adds a given <tt>ChatPanel</tt> to this chat window.
     * 
     * @param chatPanel The <tt>ChatPanel</tt> to add.
     */
    private void addSimpleChat(ChatPanel chatPanel)
    {
        this.mainPanel.add(chatPanel, BorderLayout.CENTER);
    }

    /**
     * Adds a given <tt>ChatPanel</tt> to the <tt>JTabbedPane</tt> of this
     * chat window.
     * 
     * @param chatPanel The <tt>ChatPanel</tt> to add.
     */
    private void addChatTab(ChatPanel chatPanel)
    {
        ChatSession chatSession = chatPanel.getChatSession();
        String chatName = chatSession.getChatName();

        ChatPanel currentChatPanel = getCurrentChatPanel();

        if (currentChatPanel == null)
        {
            this.mainPanel.add(chatPanel, BorderLayout.CENTER);
        }
        else
        {
            if (getChatTabCount() == 0)
            {
                ChatPanel firstChatPanel = currentChatPanel;
                ChatSession firstChatSession = firstChatPanel.getChatSession();

                // Add first two tabs to the tabbed pane.
                chatTabbedPane.addTab(  firstChatSession.getChatName(),
                                        firstChatSession.getChatStatusIcon(),
                                        firstChatPanel);

                chatTabbedPane.addTab(  chatName,
                                        chatSession.getChatStatusIcon(),
                                        chatPanel);

                // When added to the tabbed pane, the first chat panel should
                // rest the selected component.
                chatTabbedPane.setSelectedComponent(firstChatPanel);

                // Workaround for the following problem:
                // The scrollbar in the conversation area moves up when the
                // scrollpane is resized. This happens when ChatWindow is in
                // mode "Group messages in one window" and the first chat panel
                // is added to the tabbed pane. Then the scrollpane in the
                // conversation area is slightly resized and is made smaller,
                // which moves the scrollbar up.
                firstChatPanel.setCaretToEnd();

                //add the chatTabbedPane to the window
                this.mainPanel.add(chatTabbedPane, BorderLayout.CENTER);
                this.mainPanel.validate();
            }
            else
            {
                // The tabbed pane contains already tabs.

                chatTabbedPane.addTab(
                    chatName,
                    chatSession.getChatStatusIcon(),
                    chatPanel);

                chatTabbedPane.getParent().validate();
            }
        }
    }

    /**
     * Removes a given <tt>ChatPanel</tt> from this chat window.
     * 
     * @param chatPanel The <tt>ChatPanel</tt> to remove.
     */
    public void removeChat(ChatPanel chatPanel)
    {
        logger.debug("Removes chat for contact: "
                + chatPanel.getChatSession().getChatName());

        //if there's no tabs remove the chat panel directly from the content
        //pane.
        if(getChatTabCount() == 0)
        {
            this.mainPanel.remove(chatPanel);

            chatCount --;

            return;
        }

        //in the case of a tabbed chat window
        int index = chatTabbedPane.indexOfComponent(chatPanel);

        if (index != -1)
        {
            if (chatTabbedPane.getTabCount() > 1)
                chatTabbedPane.removeTabAt(index);

            if (chatTabbedPane.getTabCount() == 1)
            {
                ChatPanel currentChatPanel = getCurrentChatPanel();

                this.chatTabbedPane.removeAll();

                this.mainPanel.remove(chatTabbedPane);

                this.mainPanel.add(currentChatPanel, BorderLayout.CENTER);

                this.setCurrentChatPanel(currentChatPanel);
            }

            chatCount --;
        }
    }

    /**
     * Removes all tabs in the chat tabbed pane. If not in mode
     * TABBED_CHAT_WINDOW doesn nothing.
     */
    public void removeAllChats()
    {
        logger.debug("Remove all tabs from the chat window.");
        
        if(getChatTabCount() > 0)
        {
            this.chatTabbedPane.removeAll();

            this.mainPanel.remove(chatTabbedPane);

            chatCount = 0;
        }
        else
        {
            this.removeChat(getCurrentChatPanel());
        }
    }

    /**
     * Selects the chat tab which corresponds to the given <tt>MetaContact</tt>.
     * 
     * @param chatPanel The <tt>ChatPanel</tt> to select.
     */
    public void setCurrentChatPanel(ChatPanel chatPanel)
    {
        logger.debug("Set current chat panel to: "
            + chatPanel.getChatSession().getChatName());

        if(getChatTabCount() > 0)
            this.chatTabbedPane.setSelectedComponent(chatPanel);

        this.setTitle(chatPanel.getChatSession().getChatName());

        this.setChatContactPhoto(chatPanel.getChatSession());

        ChatTransport inviteChatTransport = chatPanel.findInviteChatTransport();

        if (inviteChatTransport != null)
            mainToolBar.enableInviteButton(true);
        else
            mainToolBar.enableInviteButton(false);

        mainToolBar.changeHistoryButtonsState(chatPanel);

        chatPanel.requestFocusInWriteArea();
    }

    /**
     * Selects the tab given by the index. If there's no tabbed pane does nothing.
     * @param index the index to select
     */
    public void setCurrentChatTab(int index)
    {
        ChatPanel chatPanel = null;
        if(getChatTabCount() > 0)
        {
            chatPanel = (ChatPanel) this.chatTabbedPane
                .getComponentAt(index);

            setCurrentChatPanel(chatPanel);
        }
    }

    /**
     * Returns the currently selected chat panel.
     * 
     * @return the currently selected chat panel.
     */
    public ChatPanel getCurrentChatPanel()
    {   
        if(getChatTabCount() > 0)
            return (ChatPanel)chatTabbedPane.getSelectedComponent();
        else
        {
            int componentCount = mainPanel.getComponentCount();
            
            for (int i = 0; i < componentCount; i ++)
            {
                Component c = mainPanel.getComponent(i);

                if(c instanceof ChatPanel)
                {
                    return (ChatPanel)c;
                }
            }
        }
        return null;
    }

    /**
     * Returns the tab count of the chat tabbed pane. Meant to be used when in
     * "Group chat windows" mode.
     * 
     * @return int The number of opened tabs.
     */
    public int getChatTabCount()
    {
        return (chatTabbedPane == null) ? 0 : chatTabbedPane.getTabCount();
    }

    /**
     * Highlights the corresponding tab for the given chat panel.
     * 
     * @param chatPanel the chat panel which corresponds to the tab to highlight
     */
    public void highlightTab(ChatPanel chatPanel)
    {
        this.chatTabbedPane.highlightTab(
            chatTabbedPane.indexOfComponent(chatPanel));
    }
    
    /**
     * Sets the given icon to the tab opened for the given chat panel.
     * 
     * @param chatPanel the chat panel, which corresponds the tab
     * @param icon the icon to be set
     */
    public void setTabIcon(ChatPanel chatPanel, Icon icon)
    {
        int index = this.chatTabbedPane.indexOfComponent(chatPanel);
        this.chatTabbedPane.setIconAt(index, icon);
    }

    /**
     * Sets the given title to the tab opened for the given chat panel.
     * @param chatPanel the chat panel
     * @param title the new title of the tab
     */
    public void setTabTitle(ChatPanel chatPanel, String title)
    {
        int index = this.chatTabbedPane.indexOfComponent(chatPanel);
        
        if(index > -1)
            this.chatTabbedPane.setTitleAt(index, title);
    }
    
    /**
     * The <tt>ForwordTabAction</tt> is an <tt>AbstractAction</tt> that
     * changes the currently selected tab with the next one. Each time when the
     * last tab index is reached the first one is selected.
     */
    private class ForwordTabAction
        extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (getChatTabCount() > 0) {
                int selectedIndex = chatTabbedPane.getSelectedIndex();
                if (selectedIndex < chatTabbedPane.getTabCount() - 1) {
                    setCurrentChatTab(selectedIndex + 1);
                }
                else {
                    setCurrentChatTab(0);
                }
            }
        }
    };

    /**
     * The <tt>BackwordTabAction</tt> is an <tt>AbstractAction</tt> that
     * changes the currently selected tab with the previous one. Each time when
     * the first tab index is reached the last one is selected.
     */
    private class BackwordTabAction
        extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (getChatTabCount() > 0) {
                int selectedIndex = chatTabbedPane.getSelectedIndex();
                if (selectedIndex != 0) {
                    setCurrentChatTab(selectedIndex - 1);
                }
                else {
                    setCurrentChatTab(chatTabbedPane.getTabCount() - 1);
                }
            }
        }
    };

    /**
     * The <tt>CopyAction</tt> is an <tt>AbstractAction</tt> that copies the
     * text currently selected.
     */
    private class CopyAction
        extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            getCurrentChatPanel().copy();
        }
    };

    /**
     * The <tt>PasteAction</tt> is an <tt>AbstractAction</tt> that pastes
     * the text contained in the clipboard in the current <tt>ChatPanel</tt>.
     */
    private class PasteAction
        extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            getCurrentChatPanel().paste();
        }
    };

    /**
     * The <tt>SendMessageAction</tt> is an <tt>AbstractAction</tt> that
     * sends the text that is currently in the write message area.
     */
    private class SendMessageAction
        extends AbstractAction
    {
		public void actionPerformed(ActionEvent e)
        {
            // chatPanel.stopTypingNotifications();
            getCurrentChatPanel().sendButtonDoClick();
        }
    }

    /**
     * The <tt>OpenSmileyAction</tt> is an <tt>AbstractAction</tt> that
     * opens the menu, containing all available smilies' icons.
     */
    private class OpenSmileyAction
        extends AbstractAction
    {
		public void actionPerformed(ActionEvent e)
        {
            getCurrentChatPanel().getChatWritePanel()
                .getEditTextToolBar().getSmiliesSelectorBox().open();
        }
    }

    /**
     * The <tt>OpenHistoryAction</tt> is an <tt>AbstractAction</tt> that
     * opens the history window for the currently selected contact.
     */
    private class OpenHistoryAction
        extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            mainToolBar.getHistoryButton().doClick();
        }
    }

    
    /**
     * The <tt>CloseAction</tt> is an <tt>AbstractAction</tt> that
     * closes a tab in the chat window.
     */
    private class CloseAction
        extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            close(true);
        }
    }
    
        
    /**
     * Returns the time of the last received message.
     *
     * @return The time of the last received message.
     */
    public long getLastIncomingMsgTimestamp(ChatPanel chatPanel)
    {
        return chatPanel.getChatConversationPanel()
            .getLastIncomingMsgTimestamp();
    }
      
    /**
     * Before closing the chat window saves the current size and position
     * through the <tt>ConfigurationService</tt>.
     */
    public class ChatWindowAdapter
        extends WindowAdapter
    {
        public void windowDeiconified(WindowEvent e)
        {
            String title = getTitle();

            if (title.startsWith("*")) {
                setTitle(title.substring(1, title.length()));
            }
        }
    }

    /**
     * Implements the <tt>SIPCommFrame</tt> close method. We check for an open
     * menu and if there's one we close it, otherwise we close the current chat.
     */
    protected void close(boolean isEscaped)
    {
        ChatPanel chatPanel = getCurrentChatPanel();

        if(isEscaped)
        {
            ChatRightButtonMenu chatRightMenu =
                chatPanel.getChatConversationPanel().getRightButtonMenu();
            ChatWritePanel chatWritePanel = chatPanel.getChatWritePanel();
            WritePanelRightButtonMenu writePanelRightMenu =
                chatWritePanel.getRightButtonMenu();

            SIPCommMenu selectedMenu = menuBar.getSelectedMenu();
            // SIPCommMenu contactMenu = getCurrentChatPanel()
            // .getProtoContactSelectorBox().getMenu();

            MenuSelectionManager menuSelectionManager =
                MenuSelectionManager.defaultManager();

            if (chatRightMenu.isVisible())
            {
                chatRightMenu.setVisible(false);
            }
            else if (writePanelRightMenu.isVisible())
            {
                writePanelRightMenu.setVisible(false);
            }
            else if (selectedMenu != null
                || chatWritePanel.getEditTextToolBar().hasSelectedMenus())
            {
                menuSelectionManager.clearSelectedPath();
            }
            else
            {
                GuiActivator.getUIService().getChatWindowManager().closeChat(
                    chatPanel);
            }
        }
        else 
        {
            GuiActivator.getUIService().getChatWindowManager()
                .closeWindow(this);
        }
    }

    /**
     * Implements the <tt>ExportedWindow.getIdentifier()</tt> method.
     * Returns the identifier of this window, which will 
     */
    public WindowID getIdentifier()
    {   
        return ExportedWindow.CHAT_WINDOW;
    }

    /**
     * Implements the <tt>ExportedWindow.minimize()</tt> method. Minimizes this
     * window.
     */
    public void minimize()
    {
        this.setExtendedState(JFrame.ICONIFIED);
    }

    /**
     * Implements the <tt>ExportedWindow.maximize()</tt> method. Maximizes this
     * window.
     */
    public void maximize()
    {   
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    /**
     * Implements the <tt>ExportedWindow.bringToFront()</tt> method. Brings
     * this window to front.
     */
    public void bringToFront()
    {
        if(getExtendedState() == JFrame.ICONIFIED)
            setExtendedState(JFrame.NORMAL);
        
        this.toFront();
    }

    /**
     * Initialize plugin components already registered for this container.
     */
    private void initPluginComponents()
    {
        pluginPanelNorth.setLayout(
            new BoxLayout(pluginPanelNorth, BoxLayout.Y_AXIS));
        pluginPanelEast.setLayout(
            new BoxLayout(pluginPanelEast, BoxLayout.Y_AXIS));
        pluginPanelSouth.setLayout(
            new BoxLayout(pluginPanelSouth, BoxLayout.Y_AXIS));
        pluginPanelWest.setLayout(
            new BoxLayout(pluginPanelWest, BoxLayout.Y_AXIS));

        this.getContentPane().add(pluginPanelNorth, BorderLayout.NORTH);
        this.getContentPane().add(pluginPanelEast, BorderLayout.EAST);
        this.getContentPane().add(pluginPanelWest, BorderLayout.WEST);
        this.mainPanel.add(pluginPanelSouth, BorderLayout.SOUTH);

        // Search for plugin components registered through the OSGI bundle
        // context.
        ServiceReference[] serRefs = null;

        String osgiFilter = "(|("
            + Container.CONTAINER_ID
            + "="+Container.CONTAINER_CHAT_WINDOW.getID()+")"
            + "(" + Container.CONTAINER_ID
            + "="+Container.CONTAINER_CHAT_STATUS_BAR.getID()+"))";

        try
        {
            serRefs = GuiActivator.bundleContext.getServiceReferences(
                PluginComponent.class.getName(),
                osgiFilter);
        }
        catch (InvalidSyntaxException exc)
        {
            logger.error("Could not obtain plugin component reference.", exc);
        }

        if (serRefs != null)
        {

            for (int i = 0; i < serRefs.length; i ++)
            {
                PluginComponent c = (PluginComponent) GuiActivator
                    .bundleContext.getService(serRefs[i]);

                Object borderLayoutConstraint = UIServiceImpl
                    .getBorderLayoutConstraintsFromContainer(c.getConstraints());

                this.addPluginComponent((Component)c.getComponent(),
                                        c.getContainer(),
                                        borderLayoutConstraint);
            }
        }

        GuiActivator.getUIService().addPluginComponentListener(this);
    }

    public void pluginComponentAdded(PluginComponentEvent event)
    {
        PluginComponent c = event.getPluginComponent();

        if (c.getContainer().equals(Container.CONTAINER_CHAT_WINDOW)
            || c.getContainer().equals(Container.CONTAINER_CHAT_STATUS_BAR))
        {
            Object borderLayoutConstraints = UIServiceImpl
                .getBorderLayoutConstraintsFromContainer(c.getConstraints());

            this.addPluginComponent((Component) c.getComponent(),
                                    c.getContainer(),
                                    borderLayoutConstraints);
        }
    }

    public void pluginComponentRemoved(PluginComponentEvent event)
    {
        PluginComponent c = event.getPluginComponent();

        if (c.getContainer().equals(Container.CONTAINER_CHAT_WINDOW)
            || c.getContainer().equals(Container.CONTAINER_CHAT_STATUS_BAR))
        {
            Object borderLayoutConstraint = UIServiceImpl
                .getBorderLayoutConstraintsFromContainer(c.getConstraints());

            this.removePluginComponent( (Component) c.getComponent(),
                                        c.getContainer(),
                                        borderLayoutConstraint);

            this.pack();
        }
    }

    /**
     * The source of the window
     * @return the source of the window
     */
    public Object getSource()
    {
        return this;
    }

    /**
     * Returns the number of all open chats.
     * 
     * @return the number of all open chats
     */
    public int getChatCount()
    {
        return chatCount;
    }
    
    public void addChatChangeListener(ChatChangeListener listener)
    {
        synchronized (chatChangeListeners)
        {
            if (!chatChangeListeners.contains(listener))
                chatChangeListeners.add(listener);
        }
    }
    
    public void removeChatChangeListener(ChatChangeListener listener)
    {
        synchronized (chatChangeListeners)
        {
            chatChangeListeners.remove(listener);
        }
    }

    /**
     * The logo bar is positioned on the top of the window and is meant to
     * contain the application logo.
     */
    private class LogoBar
        extends JPanel
    {
        private TexturePaint texture;
        
    /**
         * Creates the logo bar and specify the size.
         */
        public LogoBar()
        {
            int width = GuiActivator.getResources()
                .getSettingsInt("impl.gui.LOGO_BAR_WIDTH");

            int height = GuiActivator.getResources()
                .getSettingsInt("impl.gui.LOGO_BAR_HEIGHT");

            this.setMinimumSize(new Dimension(width, height));
            this.setPreferredSize(new Dimension(width, height));

            BufferedImage bgImage
                = ImageLoader.getImage(ImageLoader.WINDOW_TITLE_BAR_BG);

            Rectangle rect
                = new Rectangle(0, 0,
                            bgImage.getWidth(null),
                            bgImage.getHeight(null));

            texture = new TexturePaint(bgImage, rect);
        }

        /**
         * Paints the logo bar.
         * 
         * @param g the <tt>Graphics</tt> object used to paint the background
         * image of this logo bar.
         */
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            Image logoImage
                 = ImageLoader.getImage(ImageLoader.WINDOW_TITLE_BAR);

            g.drawImage(logoImage, 0, 0, null);
            g.setColor(new Color(
                GuiActivator.getResources().getColor("logoBarBackground")));

            Graphics2D g2 = (Graphics2D) g;

            g2.setPaint(texture);
            g2.fillRect(logoImage.getWidth(null), 0,
                this.getWidth(), this.getHeight());
        }
    }

    /**
     * Adds the given component with to the container corresponding to the
     * given constraints.
     * 
     * @param c the component to add
     * @param constraints the constraints determining the container
     */
    private void addPluginComponent(Component c,
                                    Container container,
                                    Object constraints)
    {
        if (container.equals(Container.CONTAINER_CHAT_WINDOW))
        {
            if (constraints.equals(BorderLayout.NORTH))
            {
                pluginPanelNorth.add(c);
                pluginPanelNorth.repaint();
            }
            else if (constraints.equals(BorderLayout.SOUTH))
            {
                pluginPanelSouth.add(c);
                pluginPanelSouth.repaint();
            }
            else if (constraints.equals(BorderLayout.WEST))
            {
                pluginPanelWest.add(c);
                pluginPanelSouth.repaint();
            }
            else if (constraints.equals(BorderLayout.EAST))
            {
                pluginPanelEast.add(c);
                pluginPanelSouth.repaint();
            }
        }
        else if (container.equals(Container.CONTAINER_CHAT_STATUS_BAR))
        {
            statusBarPanel.add(c);
        }

        this.getContentPane().repaint();
    }

    /**
     * Removes the given component from the container corresponding to the given
     * constraints.
     * 
     * @param c the component to remove
     * @param constraints the constraints determining the container
     */
    private void removePluginComponent( Component c,
                                        Container container,
                                        Object constraints)
    {
        if (container.equals(Container.CONTAINER_CHAT_WINDOW))
        {
            if (constraints.equals(BorderLayout.NORTH))
                pluginPanelNorth.remove(c);
            else if (constraints.equals(BorderLayout.SOUTH))
                pluginPanelSouth.remove(c);
            else if (constraints.equals(BorderLayout.WEST))
                pluginPanelWest.remove(c);
            else if (constraints.equals(BorderLayout.EAST))
                pluginPanelEast.remove(c);
        }
        else if (container.equals(Container.CONTAINER_CHAT_STATUS_BAR))
        {
            this.statusBarPanel.remove(c);
        }
    }

    /**
     * Sets the chat panel contact photo to this window.
     * 
     * @param chatPanel The chat panel which contact photo to set.
     */
    private void setChatContactPhoto(ChatSession chatSession)
    {
        this.contactPhotoPanel.setChatSession(chatSession);

        byte[] chatAvatar = chatSession.getChatAvatar();

        ImageIcon contactPhotoIcon;
        if (chatAvatar != null && chatAvatar.length > 0)
        {
            contactPhotoIcon = ImageUtils.getScaledRoundedImage(chatAvatar,
                                                                10,
                                                                10);

            if (contactPhotoIcon != null)
                this.setIconImage(contactPhotoIcon.getImage());
        }
        else
        {
            this.setIconImage(ImageLoader
                .getImage(ImageLoader.SIP_COMMUNICATOR_LOGO));
        }
    }

    /**
     * The photo label corresponding to the current chat.
     */
    private class ContactPhotoPanel extends JLayeredPane
    {
        private final FramedImage photoLabel =
            new FramedImage(ChatContact.AVATAR_ICON_WIDTH,
                ChatContact.AVATAR_ICON_HEIGHT);

        private JLabel addContactButton = new JLabel(
            new ImageIcon(ImageLoader.getImage(
                ImageLoader.ADD_CONTACT_CHAT_ICON)));

        private ImageIcon tooltipIcon;

        private ChatSession chatSession;

        public ContactPhotoPanel()
        {
            this.setLayout(null);

            this.setPreferredSize(
                new Dimension(  ChatContact.AVATAR_ICON_WIDTH + 10,
                                ChatContact.AVATAR_ICON_HEIGHT));

            this.add(photoLabel, 1);

            this.photoLabel.setBounds(5, 0,
                ChatContact.AVATAR_ICON_WIDTH,
                ChatContact.AVATAR_ICON_HEIGHT);

            addContactButton.setBounds(
                ChatContact.AVATAR_ICON_WIDTH - 6,
                ChatContact.AVATAR_ICON_HEIGHT - 16,
                16, 16);

            this.addContactButton.addMouseListener(new MouseAdapter()
            {
                public void mousePressed(MouseEvent e)
                {
                    if(chatSession != null)
                    {
                        AddContactWizard addCWizz = 
                                new AddContactWizard(
                                    GuiActivator.getUIService().getMainFrame(),
                                    chatSession.getCurrentChatTransport()
                                        .getName(),
                                    chatSession.getCurrentChatTransport()
                                            .getProtocolProvider()
                                );

                        addCWizz.setVisible(true);
                    }
                }
            });
        }

        /**
         * Sets the given <tt>chatSession</tt> parameters to this contact
         * photo label.
         * 
         * @param chatSession The <tt>ChatSession</tt> to set.
         */
        public void setChatSession(ChatSession chatSession)
        {
            this.chatSession = chatSession;

            byte[] chatAvatar = chatSession.getChatAvatar();

            ImageIcon contactPhotoIcon;
            if (chatAvatar != null && chatAvatar.length > 0)
            {
                contactPhotoIcon = new ImageIcon(chatAvatar);

                this.tooltipIcon = new ImageIcon(chatAvatar);
            }
            else
            {
                contactPhotoIcon = new ImageIcon(
                    ImageLoader.getImage(ImageLoader.DEFAULT_USER_PHOTO));

                this.tooltipIcon = null;
            }

            this.photoLabel.setImageIcon(contactPhotoIcon);

            // Need to set the tooltip in order to have createToolTip called
            // from the TooltipManager.
            this.setToolTipText("");

            if (!chatSession.isDescriptorPersistent())
                this.add(addContactButton, 0);
            else
                this.remove(addContactButton);

            this.revalidate();
            this.repaint();
        }

        /**
         * Create tooltip.
         */
        public JToolTip createToolTip()
        {
            ExtendedTooltip tip = new ExtendedTooltip();

            if (tooltipIcon != null)
                tip.setImage(tooltipIcon);

            tip.setTitle(chatSession.getChatName());

            Iterator<ChatTransport> transports = chatSession.getChatTransports();

            while (transports.hasNext())
            {
                ChatTransport transport = (ChatTransport) transports.next();

                ImageIcon protocolStatusIcon;
                if (transport.getStatus() != null)
                {
                    protocolStatusIcon
                        = new ImageIcon(transport.getStatus().getStatusIcon());
                }
                else
                    protocolStatusIcon = new ImageIcon();

                String transportAddress = transport.getName();

                tip.addLine( protocolStatusIcon,
                                        transportAddress);
            }

            tip.setComponent(this);

            return tip;
        }

        /**
         * Returns the string to be used as the tooltip for <i>event</i>. We
         * don't really use this string, but we need to return different string
         * each time in order to make the TooltipManager change the tooltip over
         * the different cells in the JList.
         * 
         * @return the string to be used as the tooltip for <i>event</i>.
         */
        public String getToolTipText(MouseEvent event)
        {
            return chatSession.getChatName();
        }
    }

    /**
     * Implementation of {@link ExportedWindow#setParams(Object[])}.
     */
    public void setParams(Object[] windowParams) {}
}