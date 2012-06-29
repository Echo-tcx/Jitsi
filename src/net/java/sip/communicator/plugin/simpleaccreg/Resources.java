/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.plugin.simpleaccreg;

import javax.swing.*;

import net.java.sip.communicator.service.resources.*;

import org.jitsi.service.resources.*;

/**
 * The <tt>Resources</tt> class manages the access to the internationalization
 * properties files and the image resources used in this plugin.
 *
 * @author Yana Stamcheva
 */
public class Resources
{
    private static ResourceManagementService resourcesService;

    /**
     * Returns an internationalized string corresponding to the given key.
     *
     * @param key The key of the string.
     * @return An internationalized string corresponding to the given key.
     */
    public static String getString(String key)
    {
        return getResources().getI18NString(key);
    }

    /**
     * Returns an int RGB color corresponding to the given key.
     *
     * @param key The key of the string.
     *
     * @return An internationalized string corresponding to the given key.
     */
    public static int getColor(String key)
    {
        return getResources().getColor(key);
    }

    /**
     * Returns the application property corresponding to the given key.
     *
     * @param key The key of the string.
     *
     * @return the application property corresponding to the given key
     */
    public static String getLoginProperty(String key)
    {
        return getResources().getSettingsString(key);
    }

    /**
     * Returns the application property corresponding to the given key.
     *
     * @param key The key of the string.
     *
     * @return the application property corresponding to the given key
     */
    public static String getApplicationProperty(String key)
    {
        return getResources().getSettingsString(key);
    }

    /**
     * Loads an image from a given image identifier.
     * @param imageID The identifier of the image.
     * @return The image for the given identifier.
     */
    public static ImageIcon getImage(String imageID)
    {
        return getResources().getImage(imageID);
    }

    /**
     * Returns the <tt>ResourceManagementService</tt>.
     *
     * @return the <tt>ResourceManagementService</tt>.
     */
    public static ResourceManagementService getResources()
    {
        if (resourcesService == null)
            resourcesService =
                ResourceManagementServiceUtils
                    .getService(SimpleAccountRegistrationActivator.bundleContext);
        return resourcesService;
    }
}
