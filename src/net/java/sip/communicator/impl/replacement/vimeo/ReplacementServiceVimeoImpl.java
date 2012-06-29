/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.replacement.vimeo;

import java.io.*;
import java.net.*;
import java.util.regex.*;

import net.java.sip.communicator.service.replacement.*;
import net.java.sip.communicator.util.*;

import org.json.*;

/**
 * Implements the {@link ReplacementService} to provide previews for Vimeo
 * links.
 * 
 * @author Purvesh Sahoo
 */
public class ReplacementServiceVimeoImpl
    implements ReplacementService
{
    /**
     * The logger for this class.
     */
    private static final Logger logger =
        Logger.getLogger(ReplacementServiceVimeoImpl.class);

    /**
     * The regex used to match the link in the message.
     */
    public static final String VIMEO_PATTERN =
        "(?<=>)(https?\\:\\/\\/(www\\.)*?vimeo\\.com"
        + "\\/([a-zA-Z0-9_\\-]+))(?=</A>)";

    /**
     * Configuration label shown in the config form. 
     */
    public static final String VIMEO_CONFIG_LABEL = "Vimeo";
    
    /**
     * Source name; also used as property label.
     */
    public static final String SOURCE_NAME = "VIMEO";

    /**
     * Constructor for <tt>ReplacementServiceVimeoImpl</tt>. 
     */
    public ReplacementServiceVimeoImpl()
    {
        logger.trace("Creating a Vimeo Source.");
    }

    /**
     * Returns the thumbnail URL of the video link provided.
     * 
     * @param sourceString the original video link.
     * @return the thumbnail image link; the original link in case of no match.
     */
    public String getReplacement(String sourceString)
    {
        final Pattern p =
            Pattern.compile(".+\\.com\\/([a-zA-Z0-9_\\-]+)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(sourceString);

        String thumbUrl = sourceString;

        while (m.find())
        {
            try
            {
                String url =
                    "http://vimeo.com/api/v2/video/" + m.group(1) + ".json";
                URL vimeoURL = new URL(url);
                URLConnection conn = vimeoURL.openConnection();

                BufferedReader in =
                    new BufferedReader(new InputStreamReader(conn
                        .getInputStream()));

                String inputLine, holder = "";

                while ((inputLine = in.readLine()) != null)
                    holder = inputLine;
                in.close();

                JSONArray result = new JSONArray(holder);

                if (!(result.length() == 0))
                {
                    thumbUrl
                        = result.getJSONObject(0).getString("thumbnail_medium");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return thumbUrl;
    }

    /**
     * Returns the source name
     * 
     * @return the source name
     */
    public String getSourceName()
    {
        return SOURCE_NAME;
    }

    /**
     * Returns the pattern of the source
     * 
     * @return the source pattern
     */
    public String getPattern()
    {
        return VIMEO_PATTERN;
    }
}
