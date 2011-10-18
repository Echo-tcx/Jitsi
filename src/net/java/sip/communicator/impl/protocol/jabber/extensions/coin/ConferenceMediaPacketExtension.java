/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import net.java.sip.communicator.impl.protocol.jabber.extensions.*;

/**
 * Conference media packet extension.
 *
 * @author Sebastien Vincent
 */
public class ConferenceMediaPacketExtension
    extends AbstractPacketExtension
{
    /**
     * The namespace that conference media belongs to.
     */
    public static final String NAMESPACE = "";

    /**
     * The name of the element that contains the conference media.
     */
    public static final String ELEMENT_NAME = "available-media";

    /**
     * Constructor.
     */
    public ConferenceMediaPacketExtension()
    {
        super(NAMESPACE, ELEMENT_NAME);
    }
}
