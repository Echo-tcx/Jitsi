/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.util.swing;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import net.java.sip.communicator.service.configuration.*;
import net.java.sip.communicator.service.keybindings.*;
import net.java.sip.communicator.service.resources.*;
import net.java.sip.communicator.util.*;

/**
 * A custom frame that remembers its size and location and could have a
 * semi-transparent background.
 *
 * @author Yana Stamcheva
 * @author Lyubomir Marinov
 * @author Adam Netocny
 */
public class SIPCommFrame
    extends JFrame
    implements Observer
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 0L;

    /**
     * Property that disables the automatic resizing and positioning when a
     * window's top edge is outside the visible area of the screen.
     * <p>
     * <tt>true</tt> use automatic repositioning (default)<br/>
     * <tt>false</tt> rely on the window manager to place the window
     */
    static final String PNAME_CALCULATED_POSITIONING
        = "net.sip.communicator.util.swing.USE_CALCULATED_POSITIONING";

    /**
     * The <tt>Logger</tt> used by the <tt>SIPCommFrame</tt> class and its
     * instances for logging output.
     */
    private static final Logger logger = Logger.getLogger(SIPCommFrame.class);

    /**
     * The action map of this dialog.
     */
    private ActionMap amap;

    /**
     * The input map of this dialog.
     */
    private InputMap imap;

    /**
     * The key bindings set.
     */
    private KeybindingSet bindings = null;

    /**
     * Indicates if the size and location of this dialog are stored after
     * closing. By default we store window size and location.
     */
    private boolean isSaveSizeAndLocation = true;

    /**
     * Creates a <tt>SIPCommFrame</tt>.
     */
    public SIPCommFrame()
    {
        // If on MacOS we would use the native background.
        if (!OSUtils.IS_MAC)
            this.setContentPane(new MainContentPane());

        init();

        this.addWindowListener(new FrameWindowAdapter());

        JRootPane rootPane = getRootPane();
        amap = rootPane.getActionMap();
        amap.put("close", new CloseAction());
        amap.put("closeEsc", new CloseEscAction());

        imap = rootPane.getInputMap(
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeEsc");

        // put the defaults for macosx
        if(OSUtils.IS_MAC)
        {
            imap.put(
                KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.META_DOWN_MASK),
                "closeEsc");
            imap.put(
                KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK),
                "closeEsc");
        }

        GuiUtils.addWindow(this);
    }

    /**
     * Validates this container and all of its subcomponents.
     * <p>
     * The <code>validate</code> method is used to cause a container
     * to lay out its subcomponents again. It should be invoked when
     * this container's subcomponents are modified (added to or
     * removed from the container, or layout-related information
     * changed) after the container has been displayed.
     *
     * <p>If this {@code Container} is not valid, this method invokes
     * the {@code validateTree} method and marks this {@code Container}
     * as valid. Otherwise, no action is performed.
     *
     * @see #add(java.awt.Component)
     * @see Component#invalidate
     * @see javax.swing.JComponent#revalidate()
     * @see #validateTree
     */
    @Override
    public void validate()
    {
        init();
        super.validate();
    }

    /**
     * Initialize default values.
     */
    private void init()
    {
        try
        {
            Method m = Window.class.getMethod("setIconImages", List.class);
            List<Image> logos = new ArrayList<Image>(6)
            {
                private static final long serialVersionUID = 0L;
                {
                add(UtilActivator
                    .getImage("service.gui.SIP_COMMUNICATOR_LOGO"));
                add(UtilActivator
                    .getImage("service.gui.SIP_COMMUNICATOR_LOGO_20x20"));
                add(UtilActivator
                    .getImage("service.gui.SIP_COMMUNICATOR_LOGO_32x32"));
                add(UtilActivator
                    .getImage("service.gui.SIP_COMMUNICATOR_LOGO_45x45"));
                add(UtilActivator
                    .getImage("service.gui.SIP_COMMUNICATOR_LOGO_64x64"));
                add(UtilActivator
                    .getImage("service.gui.SIP_COMMUNICATOR_LOGO_128x128"));
                }
            };
            m.invoke(this, logos);
            // In order to have the same icon when using option panes
            m.invoke(JOptionPane.getRootFrame(), logos);
        }
        catch (Exception e)
        {
            Image scLogo =
                UtilActivator.getImage("service.gui.SIP_COMMUNICATOR_LOGO");
            this.setIconImage(scLogo);

            // In order to have the same icon when using option panes
            JOptionPane.getRootFrame().setIconImage(scLogo);
        }
    }

    /**
     * Creates an instance of <tt>SIPCommFrame</tt> by specifying explicitly
     * if the size and location properties are saved. By default size and
     * location are stored.
     * @param isSaveSizeAndLocation indicates whether to save the size and
     * location of this dialog
     */
    public SIPCommFrame(boolean isSaveSizeAndLocation)
    {
        this();

        this.isSaveSizeAndLocation = isSaveSizeAndLocation;
    }

    /**
     * The action invoked when user presses Ctrl-W and Cmd-W key combination.
     */
    private class CloseAction
        extends UIAction
    {
        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 0L;

        public void actionPerformed(ActionEvent e)
        {
            if (isSaveSizeAndLocation)
                saveSizeAndLocation();
            close(false);
        }
    }

    /**
     * The action invoked when user presses Escape key.
     */
    private class CloseEscAction
        extends UIAction
    {
        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 0L;

        public void actionPerformed(ActionEvent e)
        {
            if (isSaveSizeAndLocation)
                saveSizeAndLocation();
            close(true);
        }
    }

    /**
     * Sets the input map to utilize a given category of keybindings. The frame
     * is updated to reflect the new bindings when they change. This replaces
     * any previous bindings that have been added.
     *
     * @param category set of keybindings to be utilized
     */
    protected void setKeybindingInput(KeybindingSet.Category category)
    {
        // Removes old binding set
        if (bindings != null)
        {
            bindings.deleteObserver(this);
            resetInputMap();
        }

        // Adds new bindings to input map
        bindings
            = UtilActivator.getKeybindingsService().getBindings(category);

        if (bindings != null)
        {
            for (Map.Entry<KeyStroke, String> key2action
                    : bindings.getBindings().entrySet())
                imap.put(key2action.getKey(), key2action.getValue());

            bindings.addObserver(this);
        }
    }

    /**
     * Bindings the string representation for a keybinding to the action that
     * will be executed.
     *
     * @param binding string representation of action used by input map
     * @param action the action which will be executed when user presses the
     *            given key combination
     */
    protected void addKeybindingAction(String binding, Action action)
    {
        amap.put(binding, action);
    }

    private static class FrameWindowAdapter
        extends WindowAdapter
    {
        @Override
        public void windowClosing(WindowEvent e)
        {
            ((SIPCommFrame) e.getWindow()).windowClosing(e);
        }
    }

    /**
     * Invoked when this window is in the process of being closed. The close
     * operation can be overridden at this point.
     * @param e the <tt>WindowEvent</tt> that notified us
     */
    protected void windowClosing(WindowEvent e)
    {
        /*
         * Before closing the application window save the current size and
         * position through the ConfigurationService.
         */
        if(isSaveSizeAndLocation)
            saveSizeAndLocation();

        close(false);
    }

    /**
     * Saves the size and the location of this frame through the
     * <tt>ConfigurationService</tt>.
     */
    private void saveSizeAndLocation()
    {
        try
        {
            saveSizeAndLocation(this);
        }
        catch (ConfigPropertyVetoException e)
        {
            logger
                .error(
                    "Saving the size and the location properties failed",
                    e);
        }
    }

    /**
     * Saves the size and the location of a specific <tt>Component</tt> through
     * the <tt>ConfigurationService</tt>.
     *
     * @param component the <tt>Component</tt> which is to have its size and
     * location saved through the <tt>ConfigurationService</tt>
     * @throws ConfigPropertyVetoException if the <tt>ConfigurationService</tt>
     * does not accept the saving because of objections from its
     * <tt>PropertyVetoListener</tt>s.
     */
    static void saveSizeAndLocation(Component component)
        throws ConfigPropertyVetoException
    {
        Map<String, Object> props = new HashMap<String, Object>();
        String className
            = component.getClass().getName().replaceAll("\\$", "_");

        props.put(className + ".width", component.getWidth());
        props.put(className + ".height", component.getHeight());
        props.put(className + ".x", component.getX());
        props.put(className + ".y", component.getY());
        UtilActivator.getConfigurationService().setProperties(props);
    }

    /**
     * Sets window size and position.
     */
    public void setSizeAndLocation()
    {
        ConfigurationService configService =
            UtilActivator.getConfigurationService();
        String className = this.getClass().getName();
        String widthString = configService.getString(className + ".width");
        String heightString = configService.getString(className + ".height");
        String xString = configService.getString(className + ".x");
        String yString = configService.getString(className + ".y");

        int width = 0;
        int height = 0;

        if (widthString != null && heightString != null)
        {
            width = Integer.parseInt(widthString);
            height = Integer.parseInt(heightString);

            if (width > 0 && height > 0)
            {
                Dimension screenSize =
                    Toolkit.getDefaultToolkit().getScreenSize();
                if (width <= screenSize.width && height <= screenSize.height)
                    this.setSize(width, height);
            }
        }

        int x = 0;
        int y = 0;

        if (xString != null && yString != null)
        {
            x = Integer.parseInt(xString);
            y = Integer.parseInt(yString);

            if(ScreenInformation.
                isTitleOnScreen(new Rectangle(x, y, width, height))
                || configService.getBoolean(
                    SIPCommFrame.PNAME_CALCULATED_POSITIONING, true))
            {
                this.setLocation(x, y);
            }
        }
        else
        {
            this.setCenterLocation();
        }
    }

    /**
     * Positions this window in the center of the screen.
     */
    private void setCenterLocation()
    {
        setLocationRelativeTo(null);
    }

    /**
     * Checks whether the current component will exceeds the screen size and if
     * it do will set a default size
     */
    private void ensureOnScreenLocationAndSize()
    {
        ConfigurationService config = UtilActivator.getConfigurationService();
        if(!config.getBoolean(SIPCommFrame.PNAME_CALCULATED_POSITIONING, true))
            return;

        int x = this.getX();
        int y = this.getY();

        int width = this.getWidth();
        int height = this.getHeight();

        Rectangle virtualBounds = ScreenInformation.getScreenBounds();

        // the default distance to the screen border
        final int borderDistance = 10;

        // in case any of the sizes exceeds the screen size
        // we set default one
        // get the left upper point of the window
        if (!(virtualBounds.contains(x, y)))
        {
            // top left exceeds screen bounds
            if (x < virtualBounds.x)
            {
                // window is too far to the left
                // move it to the right
                x = virtualBounds.x + borderDistance;
            }
            else if (x > virtualBounds.x)
            {
                // window is too far to the right
                // can only occour, when screen resolution is
                // changed or displayed are disconnected

                // move the window in the bounds to the very right
                x =
                    virtualBounds.x + virtualBounds.width - width
                        - borderDistance;
                if (x < virtualBounds.x + borderDistance)
                {
                    x = virtualBounds.x + borderDistance;
                }
            }

            // top left exceeds screen bounds
            if (y < virtualBounds.y)
            {
                // window is too far to the top
                // move it to the bottom
                y = virtualBounds.y + borderDistance;
            }
            else if (y > virtualBounds.y)
            {
                // window is too far to the bottom
                // can only occour, when screen resolution is
                // changed or displayed are disconnected

                // move the window in the bounds to the very bottom
                y =
                    virtualBounds.y + virtualBounds.height - height
                        - borderDistance;
                if (y < virtualBounds.y + borderDistance)
                {
                    y = virtualBounds.y + borderDistance;
                }
            }
            this.setLocation(x, y);
        }

        // check the lower right corder
        if (!(virtualBounds.contains(x + width, y + height)))
        {

            if (x + width > virtualBounds.x + virtualBounds.width)
            {
                // location of window is too far to the right, its right
                // border is out of bounds

                // calculate a new horizontal position
                // move the whole window to the left
                x =
                    virtualBounds.x + virtualBounds.width - width
                        - borderDistance;
                if (x < virtualBounds.x + borderDistance)
                {
                    // window is already on left side, it is too wide.
                    x = virtualBounds.x + borderDistance;
                    // reduce the width, so it surely fits
                    width = virtualBounds.width - 2 * borderDistance;
                }
            }
            if (y + height > virtualBounds.y + virtualBounds.height)
            {
                // location of window is too far to the bottom, its bottom
                // border is out of bounds

                // calculate a new vertical position
                // move the whole window to the top
                y =
                    virtualBounds.y + virtualBounds.height - height
                        - borderDistance;
                if (y < virtualBounds.y + borderDistance)
                {
                    // window is already on top, it is too high.
                    y = virtualBounds.y + borderDistance;
                    // reduce the width, so it surely fits
                    height = virtualBounds.height - 2 * borderDistance;
                }
            }
            this.setPreferredSize(new Dimension(width, height));
            this.setSize(width, height);
            this.setLocation(x, y);
        }
    }

    /**
     * Overwrites the setVisible method in order to set the size and the
     * position of this window before showing it.
     * @param isVisible indicates if this frame should be visible
     */
    @Override
    public void setVisible(boolean isVisible)
    {
        if (isVisible)
        {
            this.setSizeAndLocation();

            this.ensureOnScreenLocationAndSize();
        }

        super.setVisible(isVisible);
    }

    /**
     * Overwrites the setVisible method in order to set the size and the
     * position of this window before showing it.
     * @param isVisible indicates if this window will be made visible or will
     * be hidden
     * @param isPackEnabled indicates if the pack() method should be invoked
     * before showing this window
     */
    public void setVisible(boolean isVisible, boolean isPackEnabled)
    {
        if (isVisible)
        {
            /*
             * Since setSizeAndLocation() will use the width and the height,
             * pack() should be called prior to it. Otherwise, the width and the
             * height may be zero or may just change after setSizeAndLocation()
             * during pack().
             */
            this.pack();
            this.setSizeAndLocation();
            this.ensureOnScreenLocationAndSize();
        }

        super.setVisible(isVisible);
    }

    /**
     * Overwrites the dispose method in order to save the size and the position
     * of this window before closing it.
     */
    @Override
    public void dispose()
    {
        if (isSaveSizeAndLocation)
            this.saveSizeAndLocation();

        /*
         * The keybinding service will outlive us so don't let us retain our
         * memory.
         */
        if (bindings != null)
            bindings.deleteObserver(this);

        super.dispose();
    }

    private void resetInputMap()
    {
        imap.clear();
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
    }

    /**
     * Listens for changes in binding sets so they can be reflected in the input
     * map.
     * @param obs the <tt>KeybindingSet</tt> from which to update
     */
    public void update(Observable obs, Object arg)
    {
        if (obs instanceof KeybindingSet)
        {
            KeybindingSet changedBindings = (KeybindingSet) obs;

            resetInputMap();
            for (Map.Entry<KeyStroke, String> key2action : changedBindings
                .getBindings().entrySet())
            {
                imap.put(key2action.getKey(), key2action.getValue());
            }
        }
    }

    /**
     * The main content pane.
     */
    public static class MainContentPane
        extends JPanel
    {
        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 0L;

        private boolean isColorBgEnabled;

        private boolean isImageBgEnabled;

        private Color bgStartColor;

        private Color bgEndColor;

        private BufferedImage bgImage = null;

        private TexturePaint texture = null;

        /**
         * Creates an instance of <tt>MainContentPane</tt>.
         */
        public MainContentPane()
        {
            super(new BorderLayout());

            initColors();
            initStyles();
        }

        /**
         * Validates this container and all of its subcomponents.
         * <p>
         * The <code>validate</code> method is used to cause a container
         * to lay out its subcomponents again. It should be invoked when
         * this container's subcomponents are modified (added to or
         * removed from the container, or layout-related information
         * changed) after the container has been displayed.
         *
         * <p>If this {@code Container} is not valid, this method invokes
         * the {@code validateTree} method and marks this {@code Container}
         * as valid. Otherwise, no action is performed.
         *
         * @see #add(java.awt.Component)
         * @see Component#invalidate
         * @see javax.swing.JComponent#revalidate()
         * @see #validateTree
         */
        @Override
        public void validate()
        {
            initStyles();
            super.validate();
        }

        /**
         * Repaints this component.
         */
        @Override
        public void repaint()
        {
            initColors();
            super.repaint();
        }

        /**
         * Initialize color values.
         */
        private void initColors()
        {
            ResourceManagementService resources =
                UtilActivator.getResources();

            isColorBgEnabled =
                new Boolean(resources.getSettingsString(
                    "impl.gui.IS_WINDOW_COLOR_BACKGROUND_ENABLED"))
                    .booleanValue();

            if (isColorBgEnabled)
            {
                bgStartColor =
                    new Color(resources.getColor("service.gui.MAIN_BACKGROUND"));
                bgEndColor =
                    new Color(resources
                        .getColor("service.gui.MAIN_BACKGROUND_GRADIENT"));
            }
            else
            {
                bgStartColor = null;
                bgEndColor = null;
            }

            isImageBgEnabled =
                new Boolean(resources.getSettingsString(
                    "impl.gui.IS_WINDOW_IMAGE_BACKGROUND_ENABLED"))
                    .booleanValue();

            if (isImageBgEnabled)
            {
                final URL bgImagePath
                    = resources.getImageURL("service.gui.WINDOW_TITLE_BAR_BG");

                bgImage = ImageUtils.getBufferedImage(bgImagePath);

                final Rectangle rect =
                    new Rectangle(0, 0, bgImage.getWidth(),
                                    bgImage.getHeight());

                texture = new TexturePaint(bgImage, rect);
            }
        }

        /**
         * Initialize style values.
         */
        private void initStyles()
        {
            ResourceManagementService resources =
                UtilActivator.getResources();

            int borderSize =
                resources
                    .getSettingsInt("impl.gui.MAIN_WINDOW_BORDER_SIZE");
            this.setBorder(BorderFactory.createEmptyBorder(borderSize,
                borderSize, borderSize, borderSize));
        }

        /**
         * Paints this content pane.
         * @param g the <tt>Graphics</tt> object used for painting
         */
        @Override
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            // If the custom color or image window background is not enabled we
            // have nothing to do here.
            if (isColorBgEnabled || isImageBgEnabled)
            {
                g = g.create();
                try
                {
                    internalPaintComponent(g);
                }
                finally
                {
                    g.dispose();
                }
            }
        }

        /**
         * Provides a custom paint if the color or image background properties
         * are enabled.
         * @param g the <tt>Graphics</tt> object used for painting
         */
        private void internalPaintComponent(Graphics g)
        {
            AntialiasingManager.activateAntialiasing(g);

            Graphics2D g2 = (Graphics2D) g;
            int width = getWidth();
            int height = getHeight();

            if (isColorBgEnabled)
            {
                GradientPaint bgGradientColor =
                    new GradientPaint(width / 2, 0, bgStartColor, width / 2, 80,
                        bgEndColor);

                g2.setPaint(bgGradientColor);
                g2.fillRect(0, 0, width, 80);

                g2.setColor(bgEndColor);
                g2.fillRect(0, 78, width, height);
            }

            if (isImageBgEnabled)
            {
                if (bgImage != null && texture != null)
                {
                    g2.setPaint(texture);

                    g2.fillRect(0, 0, this.getWidth(), bgImage.getHeight());
                }
            }
        }
    }

    /**
     * All functions implemented in this method will be invoked when user
     * presses the Escape key.
     * @param isEscaped indicates if this frame has been closed by pressing the
     * Esc key
     */
    protected void close(boolean isEscaped)
    {

    }
}
