/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.media.conference;

import java.io.*;

import javax.media.*;
import javax.media.format.*;
import javax.media.protocol.*;

import net.java.sip.communicator.impl.media.*;

/**
 * Enables reading from a <code>PushBufferStream</code> a certain maximum number
 * of data units (e.g. bytes, shorts, ints) even if the
 * <code>PushBufferStream</code> itself pushes a larger number of data units.
 * <p>
 * An example use of this functionality is pacing a
 * <code>PushBufferStream</code> which pushes more data units in a single step
 * than a <code>CaptureDevice</code>. When these two undergo audio mixing, the
 * different numbers of per-push data units will cause the
 * <code>PushBufferStream</code> "play" itself faster than the
 * <code>CaptureDevice</code>.
 * </p>
 *
 * @author Lubomir Marinov
 */
public class CachingPushBufferStream
    implements PushBufferStream
{

    /**
     * The <code>Buffer</code> in which this instance stores the data it reads
     * from the wrapped <code>PushBufferStream</code> and from which it reads in
     * chunks later on when its <code>#read(Buffer)</code> method is called.
     */
    private Buffer cache;

    /**
     * The last <code>IOException</code> this stream has received from the
     * <code>#read(Buffer)</code> method of the wrapped stream and to be thrown
     * by this stream on the earliest call of its <code>#read(Buffer)</code>
     * method.
     */
    private IOException readException;

    /**
     * The <code>PushBufferStream</code> being paced by this instance with
     * respect to the maximum number of data units it provides in a single push. 
     */
    private final PushBufferStream stream;

    /**
     * Initializes a new <code>CachingPushBufferStream</code> instance which is
     * to pace the number of per-push data units a specific
     * <code>PushBufferStream</code> provides.
     * 
     * @param stream the <code>PushBufferStream</code> to be paced with respect
     *            to the number of per-push data units it provides
     */
    public CachingPushBufferStream(PushBufferStream stream)
    {
        this.stream = stream;
    }

    /*
     * Implements SourceStream#endOfStream(). Delegates to the wrapped
     * PushBufferStream when the cache of this instance is fully read;
     * otherwise, returns false.
     */
    public boolean endOfStream()
    {
        /*
         * TODO If the cache is still not exhausted, don't report the end of
         * this stream even if the wrapped stream has reached its end.
         */
        return stream.endOfStream();
    }

    /*
     * Implements SourceStream#getContentDescriptor(). Delegates to the wrapped
     * PushBufferStream.
     */
    public ContentDescriptor getContentDescriptor()
    {
        return stream.getContentDescriptor();
    }

    /*
     * Implements SourceStream#getContentLength(). Delegates to the wrapped
     * PushBufferStream.
     */
    public long getContentLength()
    {
        return stream.getContentLength();
    }

    /*
     * Implements Controls#getControl(String). Delegates to the wrapped
     * PushBufferStream.
     */
    public Object getControl(String controlType)
    {
        return stream.getControl(controlType);
    }

    /*
     * Implements Controls#getControls(). Delegates to the wrapped
     * PushBufferStream.
     */
    public Object[] getControls()
    {
        return stream.getControls();
    }

    /*
     * Implements PushBufferStream#getFormat(). Delegates to the wrapped
     * PushBufferStream.
     */
    public Format getFormat()
    {
        return stream.getFormat();
    }

    /**
     * Gets the object this instance uses for synchronization of the operations
     * (such as reading from the wrapped stream into the cache of this instance
     * and reading out of the cache into the <code>Buffer</code> provided to the
     * <code>#read(Buffer)</code> method of this instance) it performs in
     * various threads.
     *  
     * @return the object this instance uses for synchronization of the
     *         operations it performs in various threads
     */
    private Object getSyncRoot()
    {
        return this;
    }

    /*
     * Implements PushBufferStream#read(Buffer). If an IOException has been
     * thrown by the wrapped stream when data was last read from it, re-throws
     * it. If there is no such exception, reads from the cache of this instance.
     */
    public void read(Buffer buffer)
        throws IOException
    {
        Object syncRoot = getSyncRoot();

        synchronized (syncRoot)
        {
            if (readException != null)
            {
                IOException ex = readException;
                readException = null;
                throw ex;
            }

            if (cache != null)
            {
                try
                {
                    read(cache, buffer);
                }
                catch (UnsupportedFormatException ufex)
                {
                    IOException ioex = new IOException();
                    ioex.initCause(ufex);
                    throw ioex;
                }

                int cacheLength = cache.getLength();

                if ((cacheLength <= 0)
                        || (cacheLength <= cache.getOffset())
                        || (cache.getData() == null))
                {
                    cache = null;
                    syncRoot.notifyAll();
                }
            }
        }
    }

    /**
     * Reads data from a specific input <code>Buffer</code> (if such data is
     * available) and writes the read data into a specific output
     * <code>Buffer</code>. The input <code>Buffer</code> will be modified to
     * reflect the number of read data units. If the output <code>Buffer</code>
     * has allocated an array for storing the read data and the type of this
     * array matches that of the input <code>Buffer</code>, it will be used and
     * thus the output <code>Buffer</code> may control the maximum number of
     * data units to be read into it. 
     * 
     * @param input the <code>Buffer</code> to read data from
     * @param output the <code>Buffer</code> into which to write the data read
     *            from the specified <code>input</code>
     * @throws IOException
     * @throws UnsupportedFormatException
     */
    private void read(Buffer input, Buffer output)
        throws IOException,
               UnsupportedFormatException
    {
        Object outputData = output.getData();

        if (outputData != null)
        {
            Object inputData = input.getData();

            if (inputData == null)
            {
                output.setFormat(input.getFormat());
                output.setLength(0);
                return;
            }

            Class<?> dataType = outputData.getClass();

            if (inputData.getClass().equals(dataType)
                    && dataType.equals(byte[].class))
            {
                byte[] outputBytes = (byte[]) outputData;
                int outputLength
                    = Math.min(input.getLength(), outputBytes.length);

                System.arraycopy(
                    (byte[]) inputData,
                    input.getOffset(),
                    outputBytes,
                    output.getOffset(),
                    outputLength);

                output.setData(outputBytes);
                output.setFormat(input.getFormat());
                output.setLength(outputLength);

                input.setLength(input.getLength() - outputLength);
                input.setOffset(input.getOffset() + outputLength);
                return;
            }
        }

        output.copy(input);

        int outputLength = output.getLength();

        input.setLength(input.getLength() - outputLength);
        input.setOffset(input.getOffset() + outputLength);
    }

    /*
     * Implements PushBufferStream#setTransferHandler(BufferTransferHandler).
     * Delegates to the wrapped PushBufferStream but wraps the specified
     * BufferTransferHandler in order to intercept the calls to
     * BufferTransferHandler#transferData(PushBufferStream) and read data from
     * the wrapped PushBufferStream into the cache during the calls in question.
     */
    public void setTransferHandler(BufferTransferHandler transferHandler)
    {
        stream.setTransferHandler(
            (transferHandler == null)
                ? null
                : new StreamSubstituteBufferTransferHandler(
                            transferHandler,
                            stream,
                            this)
                        {
                            public void transferData(PushBufferStream stream)
                            {
                                if (CachingPushBufferStream.this.stream
                                        == stream)
                                    CachingPushBufferStream.this.transferData();

                                super.transferData(stream);
                            }
                        });
    }

    /**
     * Reads data from the wrapped/input PushBufferStream into the cache of this
     * stream if the cache is empty. If the cache is not empty, blocks the
     * calling thread until the cache is emptied and data is read from the
     * wrapped PushBufferStream into the cache.
     */
    protected void transferData()
    {
        Object syncRoot = getSyncRoot();

        synchronized (syncRoot)
        {
            boolean interrupted = false;

            try
            {
                while (cache != null)
                    try
                    {
                        syncRoot.wait();
                    }
                    catch (InterruptedException ex)
                    {
                        interrupted = true;
                    }
            }
            finally
            {
                if (interrupted)
                    Thread.currentThread().interrupt();
            }

            cache = new Buffer();

            try
            {
                stream.read(cache);
                readException = null;
            }
            catch (IOException ex)
            {
                readException = ex;
            }
        }
    }
}
