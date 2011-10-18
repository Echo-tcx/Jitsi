/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.irc;

import java.io.*;
import java.util.*;

import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;

/**
 * Represents the IRC protocol icon. Implements the <tt>ProtocolIcon</tt>
 * interface in order to provide an IRC icon image in two different sizes.
 * 
 * @author Stephane Remy
 * @author Loic Kempf
 * @author Lubomir Marinov
 */
public class ProtocolIconIrcImpl
    implements ProtocolIcon
{
    /**
     * The <tt>Logger</tt> used by the <tt>ProtocolIconIrcImpl</tt> class and
     * its instances for logging output.
     */
    private static final Logger logger
        = Logger.getLogger(ProtocolIconIrcImpl.class);
    
    /**
     * A hash table containing the protocol icon in different sizes.
     */
    private static final Map<String, byte[]> iconsTable
        = new Hashtable<String, byte[]>();
    static
    {
        iconsTable.put(ProtocolIcon.ICON_SIZE_16x16,
            getImageInBytes("service.protocol.irc.IRC_16x16"));

        iconsTable.put(ProtocolIcon.ICON_SIZE_32x32,
            getImageInBytes("service.protocol.irc.IRC_32x32"));

        iconsTable.put(ProtocolIcon.ICON_SIZE_48x48,
            getImageInBytes("service.protocol.irc.IRC_48x48"));

        iconsTable.put(ProtocolIcon.ICON_SIZE_64x64,
            getImageInBytes("service.protocol.irc.IRC_64x64"));
    }

    /**
     * A hash table containing the path to the protocol icon in different sizes.
     */
    private static final Map<String, String> iconPathsTable
        = new Hashtable<String, String>();
    static
    {
        iconPathsTable.put(ProtocolIcon.ICON_SIZE_16x16,
            IrcActivator.getResources().getImagePath(
                "service.protocol.irc.IRC_16x16"));

        iconPathsTable.put(ProtocolIcon.ICON_SIZE_32x32,
            IrcActivator.getResources().getImagePath(
                "service.protocol.irc.IRC_32x32"));

        iconPathsTable.put(ProtocolIcon.ICON_SIZE_48x48,
            IrcActivator.getResources().getImagePath(
                "service.protocol.irc.IRC_48x48"));

        iconPathsTable.put(ProtocolIcon.ICON_SIZE_64x64,
            IrcActivator.getResources().getImagePath(
                "service.protocol.irc.IRC_64x64"));
    }

    /**
     * Implements the <tt>ProtocolIcon.getSupportedSizes()</tt> method. Returns
     * an iterator to a set containing the supported icon sizes.
     * @return an iterator to a set containing the supported icon sizes
     */
    public Iterator<String> getSupportedSizes()
    {
        return iconsTable.keySet().iterator();
    }

    /**
     * Returns TRUE if a icon with the given size is supported, FALSE-otherwise.
     */
    public boolean isSizeSupported(String iconSize)
    {
        return iconsTable.containsKey(iconSize);
    }
    
    /**
     * Returns the icon image in the given size.
     * @param iconSize the icon size; one of ICON_SIZE_XXX constants
     */
    public byte[] getIcon(String iconSize)
    {
        return iconsTable.get(iconSize);
    }

    /**
     * Returns a path to the icon with the given size.
     * @param iconSize the size of the icon we're looking for
     * @return the path to the icon with the given size
     */
    public String getIconPath(String iconSize)
    {
        return iconPathsTable.get(iconSize);
    }

    /**
     * Returns the icon image used to represent the protocol connecting state.
     * @return the icon image used to represent the protocol connecting state
     */
    public byte[] getConnectingIcon()
    {
        return getImageInBytes("ircConnectingIcon");
    }
    
    /**
     * Returns the byte representation of the image corresponding to the given
     * identifier.
     * 
     * @param imageID the identifier of the image
     * @return the byte representation of the image corresponding to the given
     * identifier.
     */
    static byte[] getImageInBytes(String imageID) 
    {
        InputStream in
            = IrcActivator.getResources().getImageInputStream(imageID);
        byte[] image = null;

        if (in != null)
        {
            try 
            {
                image = new byte[in.available()];
    
                in.read(image);
            }
            catch (IOException e) 
            {
                logger.error("Failed to load image:" + imageID, e);
            }
        }
        return image;
    }
}
