/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import net.java.sip.communicator.impl.protocol.jabber.extensions.*;

/**
 * The element containing details about an encryption algorithm that could be
 * used during a jingle session.
 *
 * @author Emil Ivov
 */
public class CryptoPacketExtension
    extends AbstractPacketExtension
{
    /**
     * The name of the 'crypto-suite' argument.
     */
    public static final String CRYPTO_SUITE_ATTR_NAME = "crypto-suite";

    /**
     * The name of the 'key-params' argument.
     */
    public static final String KEY_PARAMS_ATTR_NAME = "key-params";

    /**
     * The name of the 'session-params' argument.
     */
    public static final String SESSION_PARAMS_ATTR_NAME = "session-params";

    /**
     * The name of the 'tag' argument.
     */
    public static final String TAG_ATTR_NAME = "tag";

    /**
     * Creates a new {@link CryptoPacketExtension} instance with the proper
     * element name and namespace.
     */
    public CryptoPacketExtension()
    {
        super(null, "crypto");
    }

    /**
     * Sets the value of the <tt>crypto-suite</tt> attribute: an identifier that
     * describes the encryption and authentication algorithms.
     *
     * @param cryptoSuite a <tt>String</tt> that describes the encryption and
     * authentication algorithms.
     */
    public void setCryptoSuite(String cryptoSuite)
    {
        super.setAttribute(CRYPTO_SUITE_ATTR_NAME, cryptoSuite);
    }

    /**
     * Returns the value of the <tt>crypto-suite</tt> attribute.
     *
     * @return a <tt>String</tt> that describes the encryption and
     * authentication algorithms.
     */
    public String getCryptoSuite()
    {
        return getAttributeAsString(CRYPTO_SUITE_ATTR_NAME);
    }

    /**
     * Sets the value of the <tt>key-params</tt> attribute that provides one or
     * more sets of keying material for the crypto-suite in question).
     *
     * @param keyParams a <tt>String</tt> that provides one or more sets of
     * keying material for the crypto-suite in question.
     */
    public void setKeyParams(String keyParams)
    {
        super.setAttribute(KEY_PARAMS_ATTR_NAME, keyParams);
    }

    /**
     * Returns the value of the <tt>key-params</tt> attribute.
     *
     * @return a <tt>String</tt> that provides one or more sets of keying
     * material for the crypto-suite in question).
     */
    public String getKeyPaams()
    {
        return getAttributeAsString(KEY_PARAMS_ATTR_NAME);
    }

    /**
     * Sets the value of the <tt>session-params</tt> attribute that provides
     * transport-specific parameters for SRTP negotiation.
     *
     * @param sessionParams a <tt>String</tt> that provides transport-specific
     * parameters for SRTP negotiation.
     */
    public void setSessionParams(String sessionParams)
    {
        super.setAttribute(SESSION_PARAMS_ATTR_NAME, sessionParams);
    }

    /**
     * Returns the value of the <tt>session-params</tt> attribute.
     *
     * @return a <tt>String</tt> that provides transport-specific parameters
     * for SRTP negotiation.
     */
    public String getSessionPaams()
    {
        return getAttributeAsString(SESSION_PARAMS_ATTR_NAME);
    }

    /**
     * Sets the value of the <tt>tag</tt> attribute: a decimal number used as
     * an identifier for a particular crypto element.
     *
     * @param tag a <tt>String</tt> containing a decimal number used as an
     * identifier for a particular crypto element.
     */
    public void setTag(String tag)
    {
        super.setAttribute(TAG_ATTR_NAME, tag);
    }

    /**
     * Returns the value of the <tt>tag</tt> attribute.
     *
     * @return a <tt>String</tt> containing a decimal number used as an
     * identifier for a particular crypto element.
     */
    public String getTag()
    {
        return getAttributeAsString(TAG_ATTR_NAME);
    }
}
