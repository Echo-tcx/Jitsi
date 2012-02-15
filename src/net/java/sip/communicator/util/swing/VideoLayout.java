/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.util.swing;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

/**
 * Implements the <tt>LayoutManager</tt> which lays out the local and remote
 * videos in a video <tt>Call</tt>.
 *
 * @author Lyubomir Marinov
 * @author Yana Stamcheva
 */
public class VideoLayout extends FitLayout
{
    /**
     * The video canvas constraint.
     */
    public static final String CANVAS = "CANVAS";

    /**
     * The center remote video constraint.
     */
    public static final String CENTER_REMOTE = "CENTER_REMOTE";

    /**
     * The close local video constraint.
     */
    public static final String CLOSE_LOCAL_BUTTON = "CLOSE_LOCAL_BUTTON";

    /**
     * The east remote video constraint.
     */
    public static final String EAST_REMOTE = "EAST_REMOTE";

    /**
     * The local video constraint.
     */
    public static final String LOCAL = "LOCAL";

    /**
     * The ration between the local and the remote video.
     */
    private static final float LOCAL_TO_REMOTE_RATIO = 0.30f;

    /**
     * The video canvas.
     */
    private Component canvas;

    /**
     * The close local video button component.
     */
    private Component closeButton;

    /**
     * The map of component constraints.
     */
    private final HashMap<Component, Object> constraints
        = new HashMap<Component, Object>();

    /**
     * The component containing the local video.
     */
    private Component local;

    /**
     * The list of <tt>Component</tt>s depicting remote videos.
     */
    private final List<Component> remotes = new LinkedList<Component>();

    /**
     * The x coordinate alignment of the remote video.
     */
    private float remoteAlignmentX = Component.CENTER_ALIGNMENT;

    /**
     * Adds the given component in this layout on the specified by name
     * position.
     *
     * @param name the constraint giving the position of the component in this
     * layout
     * @param comp the component to add
     */
    @Override
    public void addLayoutComponent(String name, Component comp)
    {
        super.addLayoutComponent(name, comp);

        synchronized (constraints)
        {
            this.constraints.put(comp, name);
        }

        if ((name == null) || name.equals(CENTER_REMOTE))
        {
            remotes.add(comp);
            remoteAlignmentX = Component.CENTER_ALIGNMENT;
        }
        else if (name.equals(EAST_REMOTE))
        {
            remotes.add(comp);
            remoteAlignmentX = Component.RIGHT_ALIGNMENT;
        }
        else if (name.equals(LOCAL))
            local = comp;
        else if (name.equals(CLOSE_LOCAL_BUTTON))
            closeButton = comp;
        else if (name.equals(CANVAS))
            canvas = comp;
    }

    /**
     * Determines how may columns to use for the grid display of specific remote
     * visual/video <tt>Component</tt>s.
     *
     * @param remotes the remote visual/video <tt>Component</tt>s to be
     * displayed in a grid
     * @return the number of columns to use for the grid display of the
     * specified remote visual/video <tt>Component</tt>s
     */
    private int calculateColumnCount(List<Component> remotes)
    {
        return (remotes.size() > 4) ? 3 : 2;
    }

    /**
     * Returns the remote video component.
     *
     * @return the remote video component
     */
    @Override
    protected Component getComponent(Container parent)
    {
        return (remotes.size() == 1) ? remotes.get(0) : null;
    }

    /**
     * Returns the constraints for the given component.
     * 
     * @param c the component for which constraints we're looking for
     * @return the constraints for the given component
     */
    public Object getComponentConstraints(Component c)
    {
        synchronized (constraints)
        {
            return constraints.get(c);
        }
    }

    /**
     * Returns the local video component.
     *
     * @return the local video component
     */
    public Component getLocal()
    {
        return local;
    }

    /**
     * Returns the local video close button.
     *
     * @return the local video close button
     */
    public Component getLocalCloseButton()
    {
        return closeButton;
    }

    /**
     * Lays out this given container.
     *
     * @param parent the container to lay out
     */
    @Override
    public void layoutContainer(Container parent)
    {
        List<Component> remotes;
        Component local = getLocal();

        /*
         * When there are multiple remote visual/video Components, the local one
         * will be displayed as if it is a remote one i.e. in the same grid, not
         * on top of a remote one.
         */
        if ((this.remotes.size() > 1) && (local != null))
        {
            remotes = new ArrayList<Component>();
            remotes.addAll(this.remotes);
            remotes.add(local);
        }
        else
            remotes = this.remotes;

        int remoteCount = remotes.size();
        Dimension parentSize = parent.getSize();

        if (remoteCount == 1)
        {
            super.layoutContainer(parent,
                    (local == null)
                        ? Component.CENTER_ALIGNMENT
                        : remoteAlignmentX);
        }
        else if (remoteCount > 0)
        {
            int columns = calculateColumnCount(remotes);
            int rows = (remoteCount + columns - 1) / columns;
            Rectangle bounds
                = new Rectangle(
                        0,
                        0,
                        parentSize.width / columns,
                        parentSize.height / rows);
            int columnsMinus1 = columns - 1;
            int i = 0;

            for (Component remote : remotes)
            {
                /*
                 * We want the remote videos ordered from right to left so that
                 * the local video does not cover a remote video when possible.
                 */
                bounds.x = (columnsMinus1 - (i % columns)) * bounds.width;
                bounds.y = (i / columns) * bounds.height;
                super.layoutComponent(
                        remote,
                        bounds,
                        Component.CENTER_ALIGNMENT,
                        Component.CENTER_ALIGNMENT);

                i++;
                if (i >= remoteCount)
                    break;
            }
        }

        if (local != null)
        {
            /*
             * If the local visual/video Component is not displayed as if it is
             * a remote one, it will be placed on top of a remote one.
             */
            if (!remotes.contains(local))
            {
                Component remote0 = remotes.isEmpty() ? null : remotes.get(0);
                int localX;
                int localY;
                int height
                    = Math.round(parentSize.height * LOCAL_TO_REMOTE_RATIO);
                int width
                    = Math.round(parentSize.width * LOCAL_TO_REMOTE_RATIO);

                /*
                 * XXX The remote Component being a JLabel is meant to signal
                 * that there is no remote video and the remote is the
                 * photoLabel.
                 */
                if ((remotes.size() == 1) && (remote0 instanceof JLabel))
                {
                    localX = parentSize.width/2 - width/2;
                    localY = parentSize.height - height;
                    super.layoutComponent(
                            local,
                            new Rectangle(localX, localY, width, height),
                            Component.CENTER_ALIGNMENT,
                            Component.BOTTOM_ALIGNMENT);
                }
                else
                {
                    localX = ((remote0 == null) ? 0 : remote0.getX()) + 5;
                    localY = parentSize.height - height - 5;
                    super.layoutComponent(
                            local,
                            new Rectangle(localX, localY, width, height),
                            Component.LEFT_ALIGNMENT,
                            Component.BOTTOM_ALIGNMENT);
                }
            }

            if (closeButton != null)
            {
                super.layoutComponent(
                        closeButton,
                        new Rectangle(
                            local.getX() + local.getWidth()
                                - closeButton.getWidth(),
                            local.getY(),
                            closeButton.getWidth(),
                            closeButton.getHeight()),
                            Component.CENTER_ALIGNMENT,
                            Component.CENTER_ALIGNMENT);
            }
        }

        if (canvas != null)
        {
            /*
             * The video canvas will get the locations of the other components
             * to paint so it has to cover the parent completely.
             */
            canvas.setBounds(0, 0, parentSize.width, parentSize.height);
        }
    }

    /**
     * Returns the minimum layout size for the given container.
     *
     * @param parent the container which minimum layout size we're looking for
     * @return a Dimension containing, the minimum layout size for the given
     * container
     */
    @Override
    public Dimension minimumLayoutSize(Container parent)
    {
        // TODO Auto-generated method stub
        return super.minimumLayoutSize(parent);
    }

    /**
     * Returns the preferred layout size for the given container.
     *
     * @param parent the container which preferred layout size we're looking for
     * @return a Dimension containing, the preferred layout size for the given
     * container
     */
    @Override
    public Dimension preferredLayoutSize(Container parent)
    {
        List<Component> remotes;
        Component local = getLocal();

        /*
         * When there are multiple remote visual/video Components, the local one
         * will be displayed as if it is a remote one i.e. in the same grid, not
         * on top of a remote one.
         */
        if ((this.remotes.size() > 1) && (local != null))
        {
            remotes = new ArrayList<Component>();
            remotes.addAll(this.remotes);
            remotes.add(local);
        }
        else
            remotes = this.remotes;

        int remoteCount = remotes.size();

        if (remoteCount == 0)
        {
            /*
             * If there is no remote visual/video Component, the local one will
             * serve the preferredSize of the Container.
             */
            if (local != null)
            {
                Dimension preferredSize = local.getPreferredSize();

                if (preferredSize != null)
                    return preferredSize;
            }
        }
        else if (remoteCount == 1)
        {
            /*
             * If there is a single remote visual/video Component, the local one
             * will be on top of it so the remote one will serve the
             * preferredSize of the Container.
             */
            Dimension preferredSize = remotes.get(0).getPreferredSize();

            if (preferredSize != null)
                return preferredSize;
        }
        else if (remoteCount > 1)
        {
            int maxWidth = 0;
            int maxHeight = 0;

            for (Component remote : remotes)
            {
                Dimension preferredSize = remote.getPreferredSize();

                if (preferredSize != null)
                {
                    if (maxWidth < preferredSize.width)
                        maxWidth = preferredSize.width;
                    if (maxHeight < preferredSize.height)
                        maxHeight = preferredSize.height;
                }
            }

            if ((maxWidth > 0) && (maxHeight > 0))
            {
                int columns = calculateColumnCount(remotes);
                int rows = (remoteCount + columns - 1) / columns;

                return new Dimension(maxWidth * columns, maxHeight * rows);
            }
        }

        return super.preferredLayoutSize(parent);
    }

    /**
     * Removes the given component from this layout.
     *
     * @param comp the component to remove from the layout
     */
    @Override
    public void removeLayoutComponent(Component comp)
    {
        super.removeLayoutComponent(comp);

        if (local == comp)
            local = null;
        else if (closeButton == comp)
            closeButton = null;
        else if (canvas == comp)
            canvas = null;
        else
            remotes.remove(comp);
    }
}
