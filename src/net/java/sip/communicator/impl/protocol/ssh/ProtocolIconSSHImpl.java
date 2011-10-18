/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 * ProtocolIconSSHImpl.java
 *
 * SSH Suport in SIP Communicator - GSoC' 07 Project
 *
 */

package net.java.sip.communicator.impl.protocol.ssh;

import java.io.*;
import java.util.*;

import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;

/**
 * Represents the SSH protocol icon. Implements the <tt>ProtocolIcon</tt>
 * interface in order to provide a SSH logo image in two different sizes.
 * 
 * @author Shobhit Jindal
 */
public class ProtocolIconSSHImpl
    implements ProtocolIcon
{
    private static Logger logger
        = Logger.getLogger(ProtocolIconSSHImpl.class); 

    /**
     * A hash table containing the protocol icon in different sizes.
     */
    private static Hashtable<String, byte[]> iconsTable
        = new Hashtable<String, byte[]>();
    static {
        iconsTable.put(ProtocolIcon.ICON_SIZE_16x16,
            getImageInBytes("service.protocol.ssh.SSH_16x16"));

        iconsTable.put(ProtocolIcon.ICON_SIZE_32x32,
            getImageInBytes("service.protocol.ssh.SSH_32x32"));

        iconsTable.put(ProtocolIcon.ICON_SIZE_48x48,
            getImageInBytes("service.protocol.ssh.SSH_48x48"));

        iconsTable.put(ProtocolIcon.ICON_SIZE_64x64,
            getImageInBytes("service.protocol.ssh.SSH_64x64"));
    }

    /**
     * A hash table containing the protocol icon in different sizes.
     */
    private static Hashtable<String, String> iconPathsTable
        = new Hashtable<String, String>();
    static {
        iconPathsTable.put(ProtocolIcon.ICON_SIZE_16x16,
            SSHActivator.getResources().getImagePath(
                "service.protocol.ssh.SSH_16x16"));

        iconPathsTable.put(ProtocolIcon.ICON_SIZE_32x32,
            SSHActivator.getResources().getImagePath(
                "service.protocol.ssh.SSH_32x32"));

        iconPathsTable.put(ProtocolIcon.ICON_SIZE_48x48,
            SSHActivator.getResources().getImagePath(
                "service.protocol.ssh.SSH_48x48"));

        iconPathsTable.put(ProtocolIcon.ICON_SIZE_64x64,
            SSHActivator.getResources().getImagePath(
                "service.protocol.ssh.SSH_64x64"));
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
     * 
     * @return TRUE if a icon with the given size is supported, FALSE otherwise
     */
    public boolean isSizeSupported(String iconSize)
    {
        return iconsTable.containsKey(iconSize);
    }
    
    /**
     * Returns the icon image in the given size.
     * @param iconSize the icon size; one of ICON_SIZE_XXX constants
     * @return the icon
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
        return getImageInBytes("protocolIconSsh");
    }
    
    /**
     * Returns the byte representation of the image corresponding to the given
     * identifier.
     * 
     * @param imageID the identifier of the image
     * @return the byte representation of the image corresponding to the given
     * identifier.
     */
    public static byte[] getImageInBytes(String imageID) 
    {
        InputStream in = SSHActivator.getResources().
            getImageInputStream(imageID);

        if (in == null)
            return null;
        byte[] image = null;
        try 
        {
            image = new byte[in.available()];

            in.read(image);
        }
        catch (IOException e) 
        {
            logger.error("Failed to load image:" + imageID, e);
        }

        return image;
    }
}
