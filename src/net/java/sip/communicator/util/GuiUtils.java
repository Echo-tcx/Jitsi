/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.util;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.*;

import javax.swing.*;

/**
 * The <tt>StringUtils</tt> class is used through this ui implementation for
 * some special operations with strings.
 * 
 * @author Yana Stamcheva
 * @author Lubomir Marinov
 * @author Adam Netocny
 */
public class GuiUtils
{
    /**
     * List of all windows owned by the app.
     */
    private static final ArrayList<Window> WINDOW_LIST
        = new ArrayList<Window>();

    private static Calendar c1 = Calendar.getInstance();

    private static Calendar c2 = Calendar.getInstance();

    /**
     * Number of milliseconds in a second.
     */
    public static final long MILLIS_PER_SECOND = 1000;

    /**
     * Number of milliseconds in a standard minute.
     */
    public static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;

    /**
     * Number of milliseconds in a standard hour.
     */
    public static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;

    /**
     * Number of milliseconds in a standard day.
     */
    public static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;

    /**
     * Replaces some chars that are special in a regular expression.
     * @param text The initial text.
     * @return the formatted text
     */
    public static String replaceSpecialRegExpChars(String text)
    {
        return text.replaceAll("([.()^&$*|])", "\\\\$1");
    }

    /**
     * Returns the width in pixels of a text.
     * @param c the component where the text is contained
     * @param text the text to measure
     * @return the width in pixels of a text.
     */
    public static int getStringWidth(Component c, String text)
    {
        return SwingUtilities.computeStringWidth(c
                .getFontMetrics(c.getFont()), text);
    }

    /**
     * Returns the size of the given text computed towards to the given
     * component.
     * @param c the component where the text is contained
     * @param text the text to measure
     * @return the dimensions of the text
     */
    public static Dimension getStringSize(Component c, String text)
    {
        // get metrics from the graphics
        FontMetrics metrics = c.getFontMetrics(c.getFont());
        // get the height of a line of text in this font and render context
        int hgt = metrics.getHeight();
        // get the advance of my text in this font and render context
        int adv = metrics.stringWidth(text);
        // calculate the size of a box to hold the text with some padding.
        return new Dimension(adv+2, hgt+2);
    }

    /**
     * Returns the bounds of the given string.
     * @param text the string to measure
     * @return the bounds of the given string
     */
    public static Rectangle2D getDefaultStringSize(String text)
    {
        Font font = UIManager.getFont("Label.font");

        FontRenderContext frc = new FontRenderContext(null, true, false);

        TextLayout layout = new TextLayout(text, font, frc);

        return layout.getBounds();
    }

    /**
     * Counts occurrences of the <tt>needle</tt> character in the given
     * <tt>text</tt>.
     * @param text the text in which we search
     * @param needle the character we're looking for
     * @return the count of occurrences of the <tt>needle</tt> chat in the
     * given <tt>text</tt>
     */
    public static int countOccurrences(String text, char needle)
    {
        int count = 0;
        for (char c : text.toCharArray())
        {
            if (c == needle)
            {
               ++count;
            }
        }
        return count;
    }

    /**
     * Compares the two dates. The comparison is based only on the day, month
     * and year values. Returns 0 if the two dates are equals, a value < 0 if
     * the first date is before the second one and > 0 if the first date is after
     * the second one.
     * @param date1 the first date to compare
     * @param date2 the second date to compare with 
     * @return Returns 0 if the two dates are equals, a value < 0 if
     * the first date is before the second one and > 0 if the first date is after
     * the second one
     */
    public static int compareDates(Date date1, Date date2)
    {
        return date1.compareTo(date2);
    }

    /**
     * Compares the two dates. The comparison is based only on the day, month
     * and year values. Returns 0 if the two dates are equals, a value < 0 if
     * the first date is before the second one and > 0 if the first date is after
     * the second one.
     * @param date1 the first date to compare
     * @param date2 the second date to compare with 
     * @return Returns 0 if the two dates are equals, a value < 0 if
     * the first date is before the second one and > 0 if the first date is after
     * the second one
     */
    public static int compareDates(long date1, long date2)
    {
        return (date1 < date2 ? -1 : (date1 == date2 ? 0 : 1));
    }

    /**
     * Compares the two dates. The comparison is based only on the day, month
     * and year values. Returns 0 if the two dates are equals, a value < 0 if
     * the first date is before the second one and > 0 if the first date is
     * after the second one.
     * @param date1 the first date to compare
     * @param date2 the second date to compare with 
     * @return Returns 0 if the two dates are equals, a value < 0 if
     * the first date is before the second one and > 0 if the first date is
     * after the second one
     */
    public static int compareDatesOnly(long date1, long date2)
    {
        c1.setTimeInMillis(date1);
        c2.setTimeInMillis(date2);

        int day1 = c1.get(Calendar.DAY_OF_MONTH);
        int month1 = c1.get(Calendar.MONTH);
        int year1 = c1.get(Calendar.YEAR);

        int day2 = c2.get(Calendar.DAY_OF_MONTH);
        int month2 = c2.get(Calendar.MONTH);
        int year2 = c2.get(Calendar.YEAR);

        if (year1 < year2)
        {
            return -1;
        }
        else if (year1 == year2)
        {
            if (month1 < month2)
                return -1;
            else if (month1 == month2)
            {
                if (day1 < day2)
                    return -1;
                else if (day1 == day2)
                    return 0;
                else
                    return 1;
            }
            else
                return 1;
        }
        else
        {
            return 1;
        }
    }

    /**
     * Formats the given date. The result format is the following:
     * [Month] [Day], [Year]. For example: Dec 24, 2000.
     * @param date the date to format
     * @return the formatted date string
     */
    public static String formatDate(Date date)
    {
        return formatDate(date.getTime());
    }

    /**
     * Formats the given date. The result format is the following:
     * [Month] [Day], [Year]. For example: Dec 24, 2000.
     * @param date the date to format
     * @return the formatted date string
     */
    public static String formatDate(final long date)
    {
        StringBuffer strBuf = new StringBuffer();

        formatDate(date, strBuf);
        return strBuf.toString();
    }

    /**
     * Formats the given date as: Month DD, YYYY and appends it to the given
     * <tt>dateStrBuf</tt> string buffer.
     * @param date the date to format
     * @param dateStrBuf the <tt>StringBuffer</tt>, where to append the
     * formatted date
     */
    public static void formatDate(long date, StringBuffer dateStrBuf)
    {
        c1.setTimeInMillis(date);

        dateStrBuf.append(GuiUtils.processMonth(c1.get(Calendar.MONTH)));
        dateStrBuf.append(' ');
        GuiUtils.formatTime(c1.get(Calendar.DAY_OF_MONTH), dateStrBuf);
        dateStrBuf.append(", ");
        GuiUtils.formatTime(c1.get(Calendar.YEAR), dateStrBuf);
    }

    /**
     * Formats the time for the given date. The result format is the following:
     * [Hour]:[Minute]:[Second]. For example: 12:25:30. 
     * @param date the date to format
     * @return the formatted hour string
     */
    public static String formatTime(Date date)
    {
        return formatTime(date.getTime());
    }

    /**
     * Formats the time for the given date. The result format is the following:
     * [Hour]:[Minute]:[Second]. For example: 12:25:30. 
     * @param time the date to format
     * @return the formatted hour string
     */
    public static String formatTime(long time)
    {
        c1.setTimeInMillis(time);

        StringBuffer timeStrBuf = new StringBuffer();

        GuiUtils.formatTime(c1.get(Calendar.HOUR_OF_DAY), timeStrBuf);
        timeStrBuf.append(':');
        GuiUtils.formatTime(c1.get(Calendar.MINUTE), timeStrBuf);
        timeStrBuf.append(':');
        GuiUtils.formatTime(c1.get(Calendar.SECOND), timeStrBuf);
        return timeStrBuf.toString();
    }

    /**
     * Subtracts the two dates.
     * @param date1 the first date argument
     * @param date2 the second date argument
     * @return the date resulted from the substracting
     */
    public static Date substractDates(Date date1, Date date2)
    {
        long d1 = date1.getTime();
        long d2 = date2.getTime();
        long difMil = d1-d2;
        long milPerDay = 1000*60*60*24;
        long milPerHour = 1000*60*60;
        long milPerMin = 1000*60;
        long milPerSec = 1000;

        long days = difMil / milPerDay;
        int hour = (int)(( difMil - days*milPerDay ) / milPerHour);
        int min
            = (int)(( difMil - days*milPerDay - hour*milPerHour ) / milPerMin);
        int sec
            = (int)(( difMil - days*milPerDay - hour*milPerHour - min*milPerMin)
                    / milPerSec);

        c1.clear();
        c1.set(Calendar.HOUR, hour);
        c1.set(Calendar.MINUTE, min);
        c1.set(Calendar.SECOND, sec);

        return c1.getTime();
    }

    /**
     * Gets the display/human-readable string representation of the month with
     * the specified zero-based month number.
     *
     * @param month the zero-based month number
     * @return the corresponding month abbreviation
     */
    private static String processMonth(int month)
    {
        String monthStringKey;

        switch (month)
        {
        case 0: monthStringKey = "service.gui.JANUARY"; break;
        case 1: monthStringKey = "service.gui.FEBRUARY"; break;
        case 2: monthStringKey = "service.gui.MARCH"; break;
        case 3: monthStringKey = "service.gui.APRIL"; break;
        case 4: monthStringKey = "service.gui.MAY"; break;
        case 5: monthStringKey = "service.gui.JUNE"; break;
        case 6: monthStringKey = "service.gui.JULY"; break;
        case 7: monthStringKey = "service.gui.AUGUST"; break;
        case 8: monthStringKey = "service.gui.SEPTEMBER"; break;
        case 9: monthStringKey = "service.gui.OCTOBER"; break;
        case 10: monthStringKey = "service.gui.NOVEMBER"; break;
        case 11: monthStringKey = "service.gui.DECEMBER"; break;
        default: return "";
        }

        return UtilActivator.getResources().getI18NString(monthStringKey);
    }

    /**
     * Adds a 0 in the beginning of one digit numbers.
     *
     * @param time The time parameter could be hours, minutes or seconds.
     * @param timeStrBuf the <tt>StringBuffer</tt> to which the formatted
     * minutes string is to be appended
     */
    private static void formatTime(int time, StringBuffer timeStrBuf)
    {
        String timeString = Integer.toString(time);

        if (timeString.length() < 2)
            timeStrBuf.append('0');
        timeStrBuf.append(timeString);
    }

    /**
     * Formats the given long to X hour, Y min, Z sec.
     * @param millis the time in milliseconds to format
     * @return the formatted seconds
     */
    public static String formatSeconds(long millis)
    {
        long[] values = new long[4];
        values[0] = millis / MILLIS_PER_DAY;
        values[1] = (millis / MILLIS_PER_HOUR) % 24;
        values[2] = (millis / MILLIS_PER_MINUTE) % 60;
        values[3] = (millis / MILLIS_PER_SECOND) % 60;

        String[] fields = { " d ", " h ", " min ", " sec" };

        StringBuffer buf = new StringBuffer(64);
        boolean valueOutput = false;

        for (int i = 0; i < 4; i++)
        {
            long value = values[i];

            if (value == 0)
            {
                if (valueOutput)
                    buf.append('0').append(fields[i]);
            }
            else
            {
                valueOutput = true;
                buf.append(value).append(fields[i]);
            }
        }

        return buf.toString().trim();
    }

    /**
     * Returns an array of all {@code Window}s, both owned and ownerless,
     * created by this application.
     * If called from an applet, the array includes only the {@code Window}s
     * accessible by that applet.
     * <p>
     * <b>Warning:</b> this method may return system created windows, such
     * as a print dialog. Applications should not assume the existence of
     * these dialogs, nor should an application assume anything about these
     * dialogs such as component positions, <code>LayoutManager</code>s
     * or serialization.
     *
     * @return Returns an array of all {@code Window}s.
     */
    public static Window[] getWindows()
    {
        Window[] retVal = new Window[WINDOW_LIST.size()];

        int c = 0;
        for(Window w : WINDOW_LIST)
        {
            retVal[c++] = w;
        }

        return retVal;
    }

    /**
     * Adds a {@link Window} into window list
     * @param w {@link Window} to be added.
     */
    public static void addWindow(Window w)
    {
        WINDOW_LIST.add(w);
    }

    /**
     * Removes a {@link Window} into window list
     * @param w {@link Window} to be removed.
     */
    public static void removeWindow(Window w)
    {
        WINDOW_LIST.remove(w);
    }

    /**
     * A simple minded look and feel change: ask each node in the tree
     * to <code>updateUI()</code> -- that is, to initialize its UI property
     * with the current look and feel.
     *
     * @param c UI component.
     */
    public static void updateComponentTreeUI(Component c)
    {
        updateComponentTreeUI0(c);
        c.invalidate();
        c.validate();
        c.repaint();
    }

    /**
     * Repaints UI tree recursively.
     * @param c UI component.
     */
    private static void updateComponentTreeUI0(Component c)
    {
        if (c instanceof JComponent)
        {
            JComponent jc = (JComponent) c;
            jc.invalidate();
            jc.validate();
            jc.repaint();
            JPopupMenu jpm =jc.getComponentPopupMenu();
            if(jpm != null && jpm.isVisible() && jpm.getInvoker() == jc)
            {
                updateComponentTreeUI(jpm);
            }
        }
        Component[] children = null;
        if (c instanceof JMenu)
        {
            children = ((JMenu)c).getMenuComponents();
        }
        else if (c instanceof java.awt.Container)
        {
            children = ((java.awt.Container)c).getComponents();
        }
        if (children != null)
        {
            for(int i = 0; i < children.length; i++)
            {
                updateComponentTreeUI0(children[i]);
            }
        }
    }
}
