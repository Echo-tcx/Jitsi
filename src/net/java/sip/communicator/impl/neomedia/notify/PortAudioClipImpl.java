/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.neomedia.notify;

import java.io.*;
import java.net.*;

import javax.media.*;
import javax.sound.sampled.*;

import net.java.sip.communicator.impl.neomedia.codec.audio.speex.*;
import net.java.sip.communicator.impl.neomedia.jmfext.media.renderer.audio.*;
import net.java.sip.communicator.util.*;

/**
 * Implementation of SCAudioClip using PortAudio.
 *
 * @author Damian Minkov
 * @author Lubomir Marinov
 */
public class PortAudioClipImpl
    extends SCAudioClipImpl
{
    /**
     * The <tt>Logger</tt> used by the <tt>PortAudioClipImpl</tt> class and its
     * instances for logging output.
     */
    private static final Logger logger
        = Logger.getLogger(PortAudioClipImpl.class);

    private final AudioNotifierServiceImpl audioNotifier;

    private boolean started = false;

    private final URL url;
    
    private final Object syncObject = new Object();

    /**
     * Creates the audio clip and initializes the listener used from the
     * loop timer.
     *
     * @param url the URL pointing to the audio file
     * @param audioNotifier the audio notify service
     * @throws IOException cannot audio clip with supplied URL.
     */
    public PortAudioClipImpl(URL url, AudioNotifierServiceImpl audioNotifier)
        throws IOException
    {
        this.audioNotifier = audioNotifier;
        this.url = url;
    }

    /**
     * Plays this audio.
     */
    public void play()
    {
        if ((url != null) && !audioNotifier.isMute())
        {
            started = true;
            new Thread()
                    {
                        @Override
                        public void run()
                        {
                            runInPlayThread();
                        }
                    }.start();
        }
    }

    /**
     * Plays this audio in loop.
     *
     * @param interval the loop interval
     */
    public void playInLoop(int interval)
    {
        setLoopInterval(interval);
        setIsLooping(true);

        play();
    }

    /**
     * Stops this audio.
     */
    public void stop()
    {
        internalStop();
        setIsLooping(false);
    }

    /**
     * Stops this audio without setting the isLooping property in the case of
     * a looping audio. The AudioNotifier uses this method to stop the audio
     * when setMute(true) is invoked. This allows us to restore all looping
     * audios when the sound is restored by calling setMute(false).
     */
    public void internalStop()
    {
        synchronized (syncObject) 
        {
            if (url != null && started) 
            {
                started = false;
                syncObject.notifyAll();
            }
        }
    }

    /**
     * Runs in a separate thread to perform the actual playback of the audio
     * stream pointed to by {@link #url} looping as necessary.
     */
    private void runInPlayThread()
    {
        Buffer buffer = new Buffer();
        byte[] bufferData = new byte[1024];
        // don't enable volume control for notifications
        PortAudioRenderer renderer = new PortAudioRenderer(false);

        buffer.setData(bufferData);
        while (started)
        {
            try
            {
                if (!runOnceInPlayThread(renderer, buffer, bufferData))
                    break;
            }
            finally
            {
                try
                {
                    renderer.stop();
                }
                finally
                {
                    renderer.close();
                }
            }

            if(isLooping())
            {
                synchronized(syncObject)
                {
                    if (started)
                    {
                        try
                        {
                            syncObject.wait(getLoopInterval());
                        }
                        catch (InterruptedException e)
                        {
                        }
                    }
                }
            }
            else
                break;
        }
    }

    /**
     * Runs in a separate thread to perform the actual playback of the audio
     * stream pointed to by {@link #url} once using a specific
     * <tt>PortAudioRenderer</tt> and giving it the audio data for processing
     * through a specific JMF <tt>Buffer</tt>.
     *
     * @param renderer the <tt>PortAudioRenderer</tt> which is to render the
     * audio data read from the audio stream pointed to by {@link #url}
     * @param buffer the JMF <tt>Buffer</tt> through which the audio data to be
     * rendered is to be given to <tt>renderer</tt>
     * @param bufferData the value of the <tt>data</tt> property of
     * <tt>buffer</tt> explicitly specified for performance reasons so that it
     * doesn't have to be read and cast during every iteration of the playback
     * loop
     * @return <tt>true</tt> if the playback was successful and it is to be
     * carried out again in accord with the <tt>looping</tt> property value of
     * this <tt>SCAudioClipImpl</tt>; otherwise, <tt>false</tt>
     */
    private boolean runOnceInPlayThread(
            PortAudioRenderer renderer,
            Buffer buffer,
            byte[] bufferData)
    {
        /*
         * If the user has configured PortAudio to use no notification device,
         * don't try to play this clip.
         */
        CaptureDeviceInfo audioNotifyDeviceInfo
            = audioNotifier
                .getDeviceConfiguration().getAudioNotifyDevice();
        if(audioNotifyDeviceInfo == null)
            return false;

        MediaLocator rendererLocator = audioNotifyDeviceInfo.getLocator();
        if (rendererLocator == null)
            return false;

        renderer.setLocator(rendererLocator);

        AudioInputStream audioStream = null;

        try
        {
            audioStream = AudioSystem.getAudioInputStream(url);
        }
        catch (IOException ioex)
        {
            logger.error("Failed to get audio stream " + url, ioex);
        }
        catch (UnsupportedAudioFileException uafex)
        {
            logger.error("Unsupported format of audio stream " + url, uafex);
        }
        if (audioStream == null)
            return false;

        Codec resampler = null;

        try
        {
            AudioFormat audioStreamFormat = audioStream.getFormat();
            Format rendererFormat
                = new javax.media.format.AudioFormat(
                        javax.media.format.AudioFormat.LINEAR,
                        audioStreamFormat.getSampleRate(),
                        audioStreamFormat.getSampleSizeInBits(),
                        audioStreamFormat.getChannels());
            Format resamplerFormat = null;

            if (renderer.setInputFormat(rendererFormat) == null)
            {
                /*
                 * Try to negotiate a resampling of the audioStreamFormat to one
                 * of the formats supported by the renderer.
                 */
                resampler = new SpeexResampler();
                resamplerFormat = rendererFormat;
                resampler.setInputFormat(resamplerFormat);

                Format[] supportedResamplerFormats
                    = resampler.getSupportedOutputFormats(resamplerFormat);

                for (Format supportedRendererFormat
                        : renderer.getSupportedInputFormats())
                {
                    for (Format supportedResamplerFormat
                            : supportedResamplerFormats)
                    {
                        if (supportedRendererFormat.matches(
                                supportedResamplerFormat))
                        {
                            rendererFormat = supportedRendererFormat;
                            resampler.setOutputFormat(rendererFormat);
                            renderer.setInputFormat(rendererFormat);
                            break;
                        }
                    }
                }
            }

            Buffer rendererBuffer = buffer;
            Buffer resamplerBuffer;

            rendererBuffer.setFormat(rendererFormat);
            if (resampler == null)
                resamplerBuffer = null;
            else
            {
                resamplerBuffer = new Buffer();
                bufferData = new byte[bufferData.length];
                resamplerBuffer.setData(bufferData);
                resamplerBuffer.setFormat(resamplerFormat);

                resampler.open();
            }

            try
            {
                renderer.open();
                renderer.start();

                int bufferLength;

                while(started
                        && ((bufferLength = audioStream.read(bufferData))
                                != -1))
                {
                    if (resampler == null)
                    {
                        rendererBuffer.setLength(bufferLength);
                        rendererBuffer.setOffset(0);
                    }
                    else
                    {
                        resamplerBuffer.setLength(bufferLength);
                        resamplerBuffer.setOffset(0);
                        rendererBuffer.setLength(0);
                        rendererBuffer.setOffset(0);
                        resampler.process(resamplerBuffer, rendererBuffer);
                    }
                    while ((renderer.process(rendererBuffer)
                                & Renderer.INPUT_BUFFER_NOT_CONSUMED)
                            == Renderer.INPUT_BUFFER_NOT_CONSUMED);
                }
            }
            catch (IOException ioex)
            {
                logger.error("Failed to read from audio stream " + url, ioex);
                return false;
            }
            catch (ResourceUnavailableException ruex)
            {
                logger.error("Failed to open PortAudioRenderer.", ruex);
                return false;
            }
        }
        catch (ResourceUnavailableException ruex)
        {
            if (resampler != null)
            {
                logger.error("Failed to open SpeexResampler.", ruex);
                return false;
            }
        }
        finally
        {
            try
            {
                audioStream.close();
            }
            catch (IOException ioex)
            {
                /*
                 * The audio stream failed to close but it doesn't mean the URL
                 * will fail to open again so ignore the exception.
                 */
            }

            if (resampler != null)
                resampler.close();
        }
        return true;
    }
}
