/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.neomedia.codec;

import javax.media.*;

import net.sf.fmj.media.*;

/**
 * Extends FMJ's <tt>AbstractCodec</tt> to make it even easier to implement a
 * <tt>Codec</tt>.
 *
 * @author Lubomir Marinov
 */
public abstract class AbstractCodecExt
    extends AbstractCodec
{
    private final Class<? extends Format> formatClass;

    private final String name;

    private final Format[] supportedOutputFormats;

    protected AbstractCodecExt(
        String name,
        Class<? extends Format> formatClass,
        Format[] supportedOutputFormats)
    {
        this.formatClass = formatClass;
        this.name = name;
        this.supportedOutputFormats = supportedOutputFormats;
    }

    public void close()
    {
        if (!opened)
            return;

        doClose();

        opened = false;
        super.close();
    }

    protected void discardOutputBuffer(Buffer outputBuffer)
    {
        outputBuffer.setDiscard(true);
    }

    protected abstract void doClose();

    /**
     * Opens this <tt>Codec</tt> and acquires the resources that it needs to
     * operate. A call to {@link PlugIn#open()} on this instance will result in
     * a call to <tt>doOpen</tt> only if {@link AbstractCodec#opened} is
     * <tt>false</tt>. All required input and/or output formats are assumed to
     * have been set on this <tt>Codec</tt> before <tt>doOpen</tt> is called.
     *
     * @throws ResourceUnavailableException if any of the resources that this
     * <tt>Codec</tt> needs to operate cannot be acquired
     */
    protected abstract void doOpen()
        throws ResourceUnavailableException;

    protected abstract int doProcess(Buffer inputBuffer, Buffer outputBuffer);

    protected Format[] getMatchingOutputFormats(Format inputFormat)
    {
        if (supportedOutputFormats != null)
            return supportedOutputFormats.clone();
        return new Format[0];
    }

    public String getName()
    {
        return (name == null) ? super.getName() : name;
    }

    /**
     * Implements {AbstractCodec#getSupportedOutputFormats(Format)}.
     *
     * @param inputFormat
     * @return
     * @see AbstractCodec#getSupportedOutputFormats(Format)
     */
    public Format[] getSupportedOutputFormats(Format inputFormat)
    {
        if (inputFormat == null)
            return supportedOutputFormats;

        if (!formatClass.isInstance(inputFormat)
                || (matches(inputFormat, inputFormats) == null))
            return new Format[0];

        return getMatchingOutputFormats(inputFormat);
    }

    /**
     * Utility to perform format matching.
     */
    public static Format matches(Format in, Format outs[])
    {
        for (Format out : outs)
            if (in.matches(out))
                return out;
        return null;
    }

    /**
     * Opens this <tt>PlugIn</tt> software or hardware component and acquires
     * the resources that it needs to operate. All required input and/or output
     * formats have to be set on this <tt>PlugIn</tt> before <tt>open</tt> is
     * called. Buffers should not be passed into this <tt>PlugIn</tt> without
     * first calling <tt>open</tt>.
     *
     * @throws ResourceUnavailableException if any of the resources that this
     * <tt>PlugIn</tt> needs to operate cannot be acquired
     * @see AbstractPlugIn#open()
     */
    public void open()
        throws ResourceUnavailableException
    {
        if (opened)
            return;

        doOpen();

        opened = true;
        super.open();
    }

    /**
     * Implements AbstractCodec#process(Buffer, Buffer).
     *
     * @param inputBuffer
     * @param outputBuffer
     * @return
     * @see AbstractCodec#process(Buffer, Buffer)
     */
    public int process(Buffer inputBuffer, Buffer outputBuffer)
    {
        if (!checkInputBuffer(inputBuffer))
            return BUFFER_PROCESSED_FAILED;
        if (isEOM(inputBuffer))
        {
            propagateEOM(outputBuffer);
            return BUFFER_PROCESSED_OK;
        }
        if (inputBuffer.isDiscard())
        {
            discardOutputBuffer(outputBuffer);
            return BUFFER_PROCESSED_OK;
        }

        return doProcess(inputBuffer, outputBuffer);
    }

    public Format setInputFormat(Format format)
    {
        if (!formatClass.isInstance(format)
                || (matches(format, inputFormats) == null))
            return null;

        return super.setInputFormat(format);
    }

    public Format setOutputFormat(Format format)
    {
        if (!formatClass.isInstance(format)
                || (matches(format, getMatchingOutputFormats(inputFormat))
                        == null))
            return null;

        return super.setOutputFormat(format);
    }

    protected byte[] validateByteArraySize(Buffer buffer, int newSize)
    {
        Object data = buffer.getData();
        byte[] newBytes;

        if (data instanceof byte[])
        {
            byte[] bytes = (byte[]) data;

            if (bytes.length >= newSize)
                return bytes;

            newBytes = new byte[newSize];
            System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
        }
        else
        {
            newBytes = new byte[newSize];
            buffer.setLength(0);
            buffer.setOffset(0);
        }

        buffer.setData(newBytes);
        return newBytes;
    }
}
