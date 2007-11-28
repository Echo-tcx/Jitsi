/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package net.java.sip.communicator.plugin.whiteboard.gui.whiteboardshapes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.*;
import java.util.*;
import net.java.sip.communicator.service.protocol.WhiteboardPoint;
import net.java.sip.communicator.service.protocol.whiteboardobjects.*;

/**
 * A WhiteboardShapePath, in XML :
 * <path d="M250 150 L150 350 L350 350 Z" />
 *
 * @author Julien Waechter
 */
public class WhiteboardShapePath
  extends WhiteboardShape implements WhiteboardObjectPath
{

    /**
     * List of WhiteboardPoint
     */
    private ArrayList points;

    /**
     * WhiteboardShapePath constructor.
     *
     * @param id String that uniquely identifies this WhiteboardObject
     * @param t number of pixels that this object (its border) 
     * should be thick.
     * @param c WhiteboardShapePath's color (or rather it's border)
     * @param points list of WhiteboardPoint.
     */
    public WhiteboardShapePath (String id, int t, Color c, List points)
    {
        super (id);
        this.setThickness (t);
        setColor (c.getRGB ());
        this.points = new ArrayList (points);
    }
    
    /**
     * WhiteboardShapePath constructor.
     *
     * @param id String that uniquely identifies this WhiteboardObject
     * @param t number of pixels that this object (its border) 
     * @param c WhiteboardShapePath's color (it's border)
     * @param points list of points
     * @param v2w 2D affine transform
     */
    public WhiteboardShapePath (String id, int t, Color c,
      List points, AffineTransform v2w)
    {
        super (id);
        this.setThickness (t);
        setColor (c.getRGB ());
        
        this.points = new ArrayList ();
        WhiteboardPoint p = null;
        for (int i = 0; i < points.size ();i++)
        {
            p = (WhiteboardPoint) points.get (i);
            Point2D w = v2w.transform (
              new Point2D.Double (p.getX (), p.getY ()), null);
            this.points.add (new WhiteboardPoint (w.getX (), w.getY ()));
        }
    }
    
    /**
     * Returns a list of all the <tt>WhiteboardPoint</tt> instances that this
     * <tt>WhiteboardObject</tt> is composed of.
     *
     * @return the list of <tt>WhiteboardPoint</tt>s composing this object.
     */
    public List getPoints ()
    {
        return points;
    }
    
    /**
     * Returns the list of selected points.
     *
     * @return list of selected points
     */
    public List getSelectionPoints ()
    {
        List list = new ArrayList ();
        WhiteboardPoint p ;
        for(int i =0; i< points.size (); i++)
        {
            p = (WhiteboardPoint) points.get (i);
            list.add (new WhiteboardPoint (p.getX (), p.getY ()));
        }
        return list;
    }
    
    /**
     * Code to paint the specific shape
     * @param g graphics context
     * @param t 2D affine transform
     */
    public void paintShape (Graphics2D g, AffineTransform t)
    {
        g.setStroke (new BasicStroke (this.getThickness (),
          BasicStroke.CAP_ROUND,BasicStroke.CAP_ROUND));
        double startX = -1;
        double startY = -1;
        int size = points.size ();
        for (int i = 0; i < size; i++)
        {
            WhiteboardPoint point = (WhiteboardPoint) points.get (i);
            Point2D p0 = t.transform (
              new Point2D.Double (startX, startY), null);
            Point2D p1 = t.transform (
              new Point2D.Double (point.getX (), point.getY ()), null);
            
            int x0 = (int) p0.getX ();
            int y0 = (int) p0.getY ();
            int x1 = (int) p1.getX ();
            int y1 = (int) p1.getY ();
            
            if (i > 0)
            {
                g.drawLine (x0, y0, x1, y1);
                startX = point.getX ();
                startY = point.getY ();
            }
            startX = point.getX ();
            startY = point.getY ();
        }
    }
    
    /**
     * Tests if the shape contains a point.
     *
     * @param p coord point
     * @return true if shape contains p
     */
    public boolean contains (Point2D p)
    {
        double startX = -1;
        double startY = -1;
        int size = points.size ();
        for (int i = 0; i < size; i++)
        {
            WhiteboardPoint point = (WhiteboardPoint) points.get (i);
            
            if (i > 0)
            {
                Line2D line = new Line2D.Double (
                  startX, startY, point.getX (), point.getY ());
                if (line.intersects (p.getX (), p.getY (), 1, 1))
                {
                    return true;
                }
                startX = point.getX ();
                startY = point.getY ();
            }
            startX = point.getX ();
            startY = point.getY ();
        }
        return false;
    }
    
    /**
     * Sets the list of <tt>WhiteboardPoint</tt> instances that this
     * <tt>WhiteboardObject</tt> is composed of.
     *
     * @param points the list of <tt>WhiteboardPoint</tt> instances that this
     * <tt>WhiteboardObject</tt> is composed of.
     */
    public void setPoints (List points)
    {
        this.points = new ArrayList (points);
    }
    
    /**
     * Translates the shape.
     *
     * @param deltaX x coordinate
     * @param deltaY y coordinate
     */
    public void translate (double deltaX, double deltaY)
    {
        WhiteboardPoint point;
        for (int i = 0; i< points.size ();i++)
        {
            point = (WhiteboardPoint) points.get (i);

            points.set (i, new WhiteboardPoint (
              point.getX () + deltaX, point.getY () + deltaY));
        }
    }
    
    /**
     * Translates a point from the shape.
     *
     * @param p point position
     * @param deltaX x coordinate
     * @param deltaY y coordinate
     */
    public void translateSelectedPoint (double deltaX, double deltaY)
    {
        if (getModifyPoint() == null)
            return;

        WhiteboardPoint point;

        for (int i = 0; i < points.size (); i++)
        {
            point = (WhiteboardPoint) points.get (i);

            if(getModifyPoint().equals(point))
            {
                WhiteboardPoint newPoint
                    = new WhiteboardPoint (
                        point.getX () + deltaX, point.getY () + deltaY);

                points.set (i, newPoint);

                this.setModifyPoint(newPoint);
            }
        }
    }
    
    /**
     * Tests if a point p is over a selection point.
     * 
     * @param p point
     * @return nearest selection point
     */
    public WhiteboardPoint getSelectionPoint (Point2D p)
    {
        WhiteboardPoint point;
        for (int i = 0; i < points.size (); i++)
        {
            point = (WhiteboardPoint) points.get (i);

            if((new Point2D.Double (
              point.getX (),  point.getY ())).distance (p) < 18)
            {
                return point;
            }
        }
        return null;
    }
}