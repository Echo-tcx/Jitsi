/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license. See terms of license at gnu.org.
 */
package net.java.sip.communicator.plugin.updatechecker;

import java.io.*;
import java.util.*;

import net.java.sip.communicator.util.*;

/**
 *
 * @author Yana Stamcheva
 */
public class Resources
{
    private static Logger logger = Logger.getLogger(Resources.class);

    private static final String CONFIG_PROP_FILE_NAME
        = "versionupdate.properties";

    private static Properties configProps = null;

    /**
     * Returns an internationalized string corresponding to the given key.
     *
     * @param key The key of the string.
     * @return An internationalized string corresponding to the given key.
     */
    public static String getConfigString(String key)
    {
        try
        {
            if(configProps == null)
            {
                configProps = new Properties();

                File configPropsFile = new File(CONFIG_PROP_FILE_NAME);
                if(!configPropsFile.exists())
                {
                    logger.info("No config file specified for update checker");
                    logger.info("Disabling update checks");
                    return null;
                }

                configProps.load(new FileInputStream(configPropsFile));
            }

            return configProps.getProperty(key);
        }
        catch (IOException exc)
        {
            logger.error("Could not open config file.");
            logger.debug("Error was: " + exc);
            return null;
        }
    }
}
