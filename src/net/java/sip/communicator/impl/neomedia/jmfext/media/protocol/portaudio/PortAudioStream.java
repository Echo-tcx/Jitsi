/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.neomedia.jmfext.media.protocol.portaudio;

import java.io.*;

import javax.media.*;
import javax.media.control.*;
import javax.media.format.*;

import net.java.sip.communicator.impl.neomedia.*;
import net.java.sip.communicator.impl.neomedia.device.*;
import net.java.sip.communicator.impl.neomedia.jmfext.media.protocol.*;
import net.java.sip.communicator.impl.neomedia.portaudio.*;
import net.java.sip.communicator.util.*;

/**
 * Implements <tt>PullBufferStream</tt> for PortAudio.
 *
 * @author Damian Minkov
 * @author Lyubomir Marinov
 */
public class PortAudioStream
    extends AbstractPullBufferStream
{
    /**
     * The <tt>Logger</tt> used by the <tt>PortAudioStream</tt> class and its
     * instances for logging output.
     */
    private static final Logger logger
        = Logger.getLogger(PortAudioStream.class);

    /**
     * The indicator which determines whether audio quality improvement is
     * enabled for this <tt>PortAudioStream</tt> in accord with the preferences
     * of the user.
     */
    private final boolean audioQualityImprovement;

    /**
     * The number of bytes to read from a native PortAudio stream in a single
     * invocation. Based on {@link #framesPerBuffer}.
     */
    private int bytesPerBuffer;

    /**
     * The device index of the PortAudio device read through this
     * <tt>PullBufferStream</tt>.
     */
    private int deviceIndex = PortAudio.paNoDevice;

    /**
     * The last-known <tt>Format</tt> of the media data made available by this
     * <tt>PullBufferStream</tt>.
     */
    private AudioFormat format;

    /**
     * The number of frames to read from a native PortAudio stream in a single
     * invocation.
     */
    private int framesPerBuffer;

    /**
     * The <tt>GainControl</tt> through which the volume/gain of captured media
     * is controlled.
     */
    private final GainControl gainControl;

    /**
     * Native pointer to a PaStreamParameters object.
     */
    private long inputParameters = 0;

    /**
     * Current sequence number.
     */
    private int sequenceNumber = 0;

    /**
     * The input PortAudio stream represented by this instance.
     */
    private long stream = 0;

    /**
     * The indicator which determines whether {@link #stream} is busy and should
     * not, for example, be closed.
     */
    private boolean streamIsBusy = false;

    /**
     * Initializes a new <tt>PortAudioStream</tt> instance which is to have its
     * <tt>Format</tt>-related information abstracted by a specific
     * <tt>FormatControl</tt>.
     *
     * @param dataSource the <tt>DataSource</tt> which is creating the new
     * instance so that it becomes one of its <tt>streams</tt>
     * @param formatControl the <tt>FormatControl</tt> which is to abstract the
     * <tt>Format</tt>-related information of the new instance
     * @param audioQualityImprovement <tt>true</tt> to enable audio quality
     * improvement for the new instance in accord with the preferences of the
     * user or <tt>false</tt> to completely disable audio quality improvement
     */
    public PortAudioStream(
            DataSource dataSource,
            FormatControl formatControl,
            boolean audioQualityImprovement)
    {
        super(dataSource, formatControl);

        this.audioQualityImprovement = audioQualityImprovement;

        gainControl
            = (GainControl)
                NeomediaActivator.getMediaServiceImpl().getInputVolumeControl();
    }

    /**
     * Gets the <tt>Format</tt> of this <tt>PullBufferStream</tt> as directly
     * known by it.
     *
     * @return the <tt>Format</tt> of this <tt>PullBufferStream</tt> as directly
     * known by it or <tt>null</tt> if this <tt>PullBufferStream</tt> does not
     * directly know its <tt>Format</tt> and it relies on the
     * <tt>PullBufferDataSource</tt> which created it to report its
     * <tt>Format</tt>
     * @see AbstractPullBufferStream#doGetFormat()
     */
    @Override
    protected Format doGetFormat()
    {
        return (format == null) ? super.doGetFormat() : format;
    }

    /**
     * Reads media data from this <tt>PullBufferStream</tt> into a specific
     * <tt>Buffer</tt> with blocking.
     *
     * @param buffer the <tt>Buffer</tt> in which media data is to be read from
     * this <tt>PullBufferStream</tt>
     * @throws IOException if anything goes wrong while reading media data from
     * this <tt>PullBufferStream</tt> into the specified <tt>buffer</tt>
     */
    public void read(Buffer buffer)
        throws IOException
    {
        synchronized (this)
        {
            if (stream == 0)
            {
                buffer.setLength(0);
                return;
            }
            else
                streamIsBusy = true;
        }
        try
        {
            /*
             * Reuse the data of buffer in order to not perform unnecessary
             * allocations.
             */
            Object data = buffer.getData();
            byte[] bufferData = null;

            if (data instanceof byte[])
            {
                bufferData = (byte[]) data;
                if (bufferData.length < bytesPerBuffer)
                    bufferData = null;
            }
            if (bufferData == null)
            {
                bufferData = new byte[bytesPerBuffer];
                buffer.setData(bufferData);
            }

            try
            {
                PortAudio.Pa_ReadStream(stream, bufferData, framesPerBuffer);
            }
            catch (PortAudioException paex)
            {
                IOException ioex = new IOException(paex.getLocalizedMessage());

                ioex.initCause(paex);
                throw ioex;
            }

            // if we have some volume setting apply them
            if (gainControl != null)
            {
                AbstractVolumeControl.applyGain(
                        gainControl,
                        bufferData, 0, bytesPerBuffer);
            }

            long bufferTimeStamp = System.nanoTime();

            buffer.setFlags(Buffer.FLAG_SYSTEM_TIME);
            if (format != null)
                buffer.setFormat(format);
            buffer.setHeader(null);
            buffer.setLength(bytesPerBuffer);
            buffer.setOffset(0);
            buffer.setSequenceNumber(sequenceNumber++);
            buffer.setTimeStamp(bufferTimeStamp);
        }
        finally
        {
            synchronized (this)
            {
               streamIsBusy = false;
               notifyAll();
            }
        }
    }

    /**
     * Sets the device index of the PortAudio device to be read through this
     * <tt>PullBufferStream</tt>.
     *
     * @param deviceIndex the device index of the PortAudio device to be read
     * through this <tt>PullBufferStream</tt>
     * @throws IOException if input/output error occurred
     */
    synchronized void setDeviceIndex(int deviceIndex)
        throws IOException
    {
        if (this.deviceIndex == deviceIndex)
            return;

        // DataSource#disconnect
        if (this.deviceIndex != PortAudio.paNoDevice)
        {
            if (stream != 0)
            {
                try
                {
                    PortAudio.Pa_CloseStream(stream);
                }
                catch (PortAudioException paex)
                {
                    logger.error(
                            "Failed to close " + getClass().getSimpleName(),
                            paex);

                    IOException ioex
                        = new IOException(paex.getLocalizedMessage());

                    ioex.initCause(paex);
                    throw ioex;
                }
                stream = 0;
                if (inputParameters != 0)
                {
                    PortAudio.PaStreamParameters_free(inputParameters);
                    inputParameters = 0;
                }

                /*
                 * Make sure this AbstractPullBufferStream asks its DataSource
                 * for the Format in which it is supposed to output audio data
                 * next time it's opened instead of using its Format from a
                 * previous open.
                 */
                this.format = null;
            }
        }
        this.deviceIndex = deviceIndex;
        // DataSource#connect
        if (this.deviceIndex != PortAudio.paNoDevice)
        {
            AudioFormat format = (AudioFormat) getFormat();
            int channels = format.getChannels();
            int sampleSizeInBits = format.getSampleSizeInBits();
            long sampleFormat = PortAudio.getPaSampleFormat(sampleSizeInBits);
            double sampleRate = format.getSampleRate();
            int framesPerBuffer
                = (int)
                    ((sampleRate * PortAudio.DEFAULT_MILLIS_PER_BUFFER)
                        / (channels * 1000));

            try
            {
                inputParameters
                    = PortAudio.PaStreamParameters_new(
                            this.deviceIndex,
                            channels,
                            sampleFormat,
                            PortAudio.getSuggestedLatency());

                stream
                    = PortAudio.Pa_OpenStream(
                            inputParameters,
                            0 /* outputParameters */,
                            sampleRate,
                            framesPerBuffer,
                            PortAudio.STREAM_FLAGS_CLIP_OFF
                                | PortAudio.STREAM_FLAGS_DITHER_OFF,
                            null /* streamCallback */);
            }
            catch (PortAudioException paex)
            {
                logger.error(
                        "Failed to open " + getClass().getSimpleName(),
                        paex);

                IOException ioex = new IOException(paex.getLocalizedMessage());

                ioex.initCause(paex);
                throw ioex;
            }
            finally
            {
                if ((stream == 0) && (inputParameters != 0))
                {
                    PortAudio.PaStreamParameters_free(inputParameters);
                    inputParameters = 0;
                }
            }
            if (stream == 0)
                throw new IOException("Pa_OpenStream");

            this.framesPerBuffer = framesPerBuffer;
            bytesPerBuffer
                = PortAudio.Pa_GetSampleSize(sampleFormat)
                    * channels
                    * framesPerBuffer;

            /*
             * Know the Format in which this PortAudioStream will output audio
             * data so that it can report it without going through its
             * DataSource.
             */
            this.format
                    = new AudioFormat(
                            AudioFormat.LINEAR,
                            sampleRate,
                            sampleSizeInBits,
                            channels,
                            AudioFormat.LITTLE_ENDIAN,
                            AudioFormat.SIGNED,
                            Format.NOT_SPECIFIED /* frameSizeInBits */,
                            Format.NOT_SPECIFIED /* frameRate */,
                            Format.byteArray);

            DeviceConfiguration deviceConfig
                = NeomediaActivator
                    .getMediaServiceImpl()
                        .getDeviceConfiguration();

            PortAudio.setDenoise(
                    stream,
                    audioQualityImprovement && deviceConfig.isDenoise());
            PortAudio.setEchoFilterLengthInMillis(
                    stream,
                    (audioQualityImprovement && deviceConfig.isEchoCancel())
                        ? deviceConfig.getEchoCancelFilterLengthInMillis()
                        : 0);
        }
    }

    /**
     * Starts the transfer of media data from this <tt>PullBufferStream</tt>.
     *
     * @throws IOException if anything goes wrong while starting the transfer of
     * media data from this <tt>PullBufferStream</tt>
     */
    @Override
    public synchronized void start()
        throws IOException
    {
        try
        {
            PortAudio.Pa_StartStream(stream);
        }
        catch (PortAudioException paex)
        {
            logger.error("Failed to start " + getClass().getSimpleName(), paex);

            IOException ioex = new IOException(paex.getLocalizedMessage());

            ioex.initCause(paex);
            throw ioex;
        }
    }

    /**
     * Stops the transfer of media data from this <tt>PullBufferStream</tt>.
     *
     * @throws IOException if anything goes wrong while stopping the transfer of
     * media data from this <tt>PullBufferStream</tt>
     */
    @Override
    public synchronized void stop()
        throws IOException
    {
        boolean interrupted = false;

        while (streamIsBusy)
        {
            try
            {
                wait();
            }
            catch (InterruptedException iex)
            {
                interrupted = true;
            }
        }
        if (interrupted)
            Thread.currentThread().interrupt();

        try
        {
            PortAudio.Pa_StopStream(stream);
        }
        catch (PortAudioException paex)
        {
            logger.error("Failed to stop " + getClass().getSimpleName(), paex);

            IOException ioex = new IOException(paex.getLocalizedMessage());

            ioex.initCause(paex);
            throw ioex;
        }
    }
}
