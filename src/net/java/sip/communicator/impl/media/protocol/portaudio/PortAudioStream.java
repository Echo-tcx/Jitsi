/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.media.protocol.portaudio;

import java.io.*;
import java.nio.*;

import javax.media.*;
import javax.media.Buffer;
import javax.media.format.*;
import javax.media.protocol.*;

/**
 * @author Damian Minkov
 */
public class PortAudioStream
    implements PushBufferStream,
               PortAudioStreamCallback
{
//    private static final Logger logger
//        = Logger.getLogger(PortAudioStream.class);

    private final static ContentDescriptor cd =
        new ContentDescriptor(ContentDescriptor.RAW);
    private BufferTransferHandler transferHandler;
    private Control[] controls = new Control[0];

    public static AudioFormat audioFormat = new AudioFormat(
                    AudioFormat.LINEAR,
                      44100,
                      16,
                      1,
                      AudioFormat.LITTLE_ENDIAN,
                      AudioFormat.SIGNED,
                      16,
                      Format.NOT_SPECIFIED,
                      Format.byteArray);

    private long stream = 0;

    private ByteBuffer bufferToProcess = null;

    private int seqNo = 0;

    /**
     * Returns the supported formats by this stream.
     * @return supported formats
     */
    public Format getFormat()
    {
        return audioFormat;
    }

    /**
     * 
     * @param buffer
     * @throws IOException
     */
    public void read(Buffer buffer)
        throws IOException
    {
        byte[] barr = new byte[bufferToProcess.remaining()];

        bufferToProcess.get(barr);

        buffer.setTimeStamp(System.nanoTime());
        buffer.setData(barr);
        buffer.setSequenceNumber(seqNo);
        buffer.setLength(barr.length);
        buffer.setFlags(0);
        buffer.setHeader(null);
        seqNo++;
    }

    /**
     * 
     * @param transferHandler
     */
    public void setTransferHandler(BufferTransferHandler transferHandler)
    {
        this.transferHandler = transferHandler;
    }

    /**
     * We are providing access to raw data
     */
    public ContentDescriptor getContentDescriptor()
    {
        return cd;
    }

    /**
     * We are streaming.
     */
    public long getContentLength()
    {
        return LENGTH_UNKNOWN;
    }

    /**
     * The stream never ends.
     *
     */
    public boolean endOfStream()
    {
        return false;
    }

    /**
     * Gives control information to the caller
     *
     */
    public Object[] getControls()
    {
        return controls;
    }

    /**
     * Return required control from the Control[] array
     * if exists, that is
     */
    public Object getControl(String controlType)
    {
        try
        {
            Class<?> cls = Class.forName(controlType);
            Object cs[] = getControls();
            for(int i = 0; i < cs.length; i++)
            {
                if(cls.isInstance(cs[i]))
                {
                    return cs[i];
                }
            }
            return null;

        }
        catch (Exception e)
        {
            return null;
        }
    }

    public void finishedCallback()
    {
    }

    /**
     * Starts the stream operation
     */
    void start()
        throws PortAudioException
    {
        if (this.stream != 0)
            throw new IllegalStateException("stream");

        long stream = createStream();

        try
        {
            PortAudio.Pa_StartStream(stream);
            this.stream = stream;
        }
        catch (PortAudioException startException)
        {
            try
            {
                PortAudio.Pa_CloseStream(stream);
            }
            catch (PortAudioException closeException)
            {
                /*
                 * We couldn't start the stream so we're closing it just to free
                 * the native resources but if that fails as well, we cannot do
                 * anything about it. Besides, we have to rethrow the exception
                 * which was thrown on start.
                 */
            }

            throw startException;
        }
    }

    void stop()
        throws PortAudioException
    {
        if (stream != 0)
        {
            PortAudio.Pa_CloseStream(stream);
            stream = 0;
        }
    }

    private long createStream()
        throws PortAudioException
    {
        int deviceCount = PortAudio.Pa_GetDeviceCount();
        int deviceIndex = 0;

        for (; deviceIndex < deviceCount; deviceIndex++)
        {
            long deviceInfo = PortAudio.Pa_GetDeviceInfo(deviceIndex);

            if ((PortAudio.PaDeviceInfo_getMaxInputChannels(deviceInfo) == 2)
                && (PortAudio.PaDeviceInfo_getMaxOutputChannels(deviceInfo) == 0)
                && PortAudio.PaDeviceInfo_getName(deviceInfo)
                    .contains("Analog"))
                break;
        }

        long streamParameters
            = PortAudio.PaStreamParameters_new(
                    deviceIndex,
                    1,
                    PortAudio.SAMPLE_FORMAT_INT16);

        return
            PortAudio.Pa_OpenStream(
                    streamParameters,
                    0,
                    44100,
                    PortAudio.FRAMES_PER_BUFFER_UNSPECIFIED,
                    PortAudio.STREAM_FLAGS_NO_FLAG,
                    this);
    }

    public int callback(ByteBuffer input, ByteBuffer output)
    {
        bufferToProcess = input;

        if (transferHandler != null)
            transferHandler.transferData(this);

        return RESULT_CONTINUE;
    }
}