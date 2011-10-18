/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.jabber.extensions.notification;

import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.provider.*;
import org.xmlpull.v1.*;

/**
 * The NotificationEventIQProvider parses Notification Event packets.
 *
 * @author Damian Minkov
 */
public class NotificationEventIQProvider
    implements IQProvider
{
    /**
     * Parse the IQ sub-document and create an IQ instance. Each IQ must have a
     * single child element. At the beginning of the method call, the xml parser
     * will be positioned at the opening tag of the IQ child element. At the end
     * of the method call, the parser <b>must</b> be positioned on the closing
     * tag of the child element.
     *
     * @param parser an XML parser.
     * @return a new IQ instance.
     * @throws Exception if an error occurs parsing the XML.
     */
    public IQ parseIQ(XmlPullParser parser)
        throws Exception
    {
        NotificationEventIQ result = new NotificationEventIQ();

        boolean done = false;
        while (!done)
        {
            int eventType = parser.next();
            if(eventType == XmlPullParser.START_TAG)
            {
                if(parser.getName().equals(NotificationEventIQ.EVENT_NAME))
                {
                    result.setEventName(parser.nextText());
                }
                else if(parser.getName().equals(NotificationEventIQ.EVENT_VALUE))
                {
                    result.setEventValue(parser.nextText());
                }
                else if(parser.getName().equals(NotificationEventIQ.EVENT_SOURCE))
                {
                    result.setEventSource(parser.nextText());
                }
            }
            else if(eventType == XmlPullParser.END_TAG)
            {
                if(parser.getName().equals(NotificationEventIQ.ELEMENT_NAME))
                {
                    done = true;
                }
            }
        }

        return result;
    }
}
