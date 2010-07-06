/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.neomedia.codec.video.h264;

import java.awt.Dimension; // disambiguation
import java.util.*;

import javax.media.*;
import javax.media.format.*;

import net.java.sip.communicator.impl.neomedia.codec.*;
import net.sf.fmj.media.*;

/**
 * Packetizes H.264 encoded data/NAL units into RTP packets in accord with RFC
 * 3984 "RTP Payload Format for H.264 Video".
 *
 * @author Damian Minkov
 * @author Lubomir Marinov
 */
public class Packetizer
    extends AbstractPacketizer
{
    /**
     * Array of default output formats.
     */
    private static final Format[] DEFAULT_OUTPUT_FORMATS
        = { new VideoFormat(Constants.H264_RTP) };

    /**
     * Maximum payload size without the headers.
     */
    public static final int MAX_PAYLOAD_SIZE = 1024;

    /**
     * Name of the plugin.
     */
    private static final String PLUGIN_NAME = "H264 Packetizer";

    /**
     * The list of NAL units to be sent as payload in RTP packets.
     */
    private final List<byte[]> nals = new LinkedList<byte[]>();

    /**
     * The timeStamp of the RTP packets in which <tt>nals</tt> are to be sent.
     */
    private long nalsTimeStamp;

    /**
     * The sequence number of the next RTP packet to be output by this
     * <tt>Packetizer</tt>.
     */
    private int sequenceNumber;

    /**
     * Initializes a new <tt>Packetizer</tt> instance which is to packetize
     * H.264 encoded data/NAL units into RTP packets in accord with RFC 3984
     * "RTP Payload Format for H.264 Video".
     */
    public Packetizer()
    {
        inputFormats = new Format[] { new VideoFormat(Constants.H264) };

        inputFormat = null;
        outputFormat = null;
    }

    /**
     * Close this <tt>Packetizer</tt>.
     */
    @Override
    public synchronized void close()
    {
        if (opened)
        {
            opened = false;
            super.close();
        }
    }

    /**
     * Finds the index in <tt>byteStream</tt> at which the
     * start_code_prefix_one_3bytes of a NAL unit begins.
     *
     * @param byteStream the H.264 encoded byte stream composed of NAL units in
     * which the index of the beginning of the start_code_prefix_one_3bytes of a
     * NAL unit is to be found
     * @param beginIndex the inclusive index in <tt>byteStream</tt> at which the
     * search is to begin
     * @param endIndex the exclusive index in <tt>byteStream</tt> at which the
     * search is to end
     * @return the index in <tt>byteStream</tt> at which the
     * start_code_prefix_one_3bytes of a NAL unit begins if it is found;
     * otherwise, <tt>endIndex</tt>
     */
    private static int ff_avc_find_startcode(
        byte[] byteStream,
        int beginIndex,
        int endIndex)
    {
        for (; beginIndex < (endIndex - 3); beginIndex++)
            if((byteStream[beginIndex] == 0)
                    && (byteStream[beginIndex + 1] == 0)
                    && (byteStream[beginIndex + 2] == 1))
                return beginIndex;
        return endIndex;
    }

    /**
     * Get the matching output formats for a specific format.
     *
     * @param in input format
     * @return array for formats matching input format
     */
    private Format[] getMatchingOutputFormats(Format in)
    {
        VideoFormat videoIn = (VideoFormat) in;
        Dimension inSize = videoIn.getSize();

        return
            new VideoFormat[]
            {
                new VideoFormat(
                        Constants.H264_RTP,
                        inSize,
                        Format.NOT_SPECIFIED,
                        Format.byteArray,
                        videoIn.getFrameRate())
            };
    }
    /**
     * Get codec name.
     *
     * @return codec name
     */
    @Override
    public String getName()
    {
        return PLUGIN_NAME;
    }

    /**
     * Return the list of formats supported at the output.
     * @param in input <tt>Format</tt> to determine corresponding output
     * <tt>Format/tt>s
     * @return array of formats supported at output
     */
    public Format[] getSupportedOutputFormats(Format in)
    {
        // null input format
        if (in == null)
            return DEFAULT_OUTPUT_FORMATS;

        // mismatch input format
        if (!(in instanceof VideoFormat)
                || (null == AbstractCodecExt.matches(in, inputFormats)))
            return new Format[0];

        return getMatchingOutputFormats(in);
    }

    /**
     * Open this <tt>Packetizer</tt>.
     */
    @Override
    public synchronized void open()
        throws ResourceUnavailableException
    {
        if (!opened)
        {
            nals.clear();
            sequenceNumber = 0;

            super.open();
            opened = true;
        }
    }

    /**
     * Packetizes a specific NAL unit of H.264 encoded data so that it becomes
     * ready to be sent as the payload of RTP packets. If the specified NAL unit
     * does not fit into a single RTP packet i.e. will not become a "Single NAL
     * Unit Packet", splits it into "Fragmentation Units (FUs)" of type FU-A.
     *
     * @param nal the bytes which contain the NAL unit of H.264 encoded data to
     * be packetized
     * @param nalOffset the offset in <tt>nal</tt> at which the NAL unit of
     * H.264 encoded data to be packetized begins
     * @param nalLength the length in <tt>nal</tt> beginning at
     * <tt>nalOffset</tt> of the NAL unit of H.264 encoded data to be packetized
     * @return <tt>true</tt> if at least one RTP packet payload has been
     * packetized i.e. prepared for sending; otherwise, <tt>false</tt>
     */
    private boolean packetizeNAL(byte[] nal, int nalOffset, int nalLength)
    {
        /*
         * If the NAL fits into a "Single NAL Unit Packet", it's already
         * packetized.
         */
        if (nalLength <= MAX_PAYLOAD_SIZE)
        {
            byte[] singleNALUnitPacket = new byte[nalLength];

            System.arraycopy(nal, nalOffset, singleNALUnitPacket, 0, nalLength);
            return nals.add(singleNALUnitPacket);
        }

        // Otherwise, split it into "Fragmentation Units (FUs)".
        byte octet = nal[nalOffset];
        int forbidden_zero_bit = octet & 0x80;
        int nri = octet & 0x60;
        int nal_unit_type = octet & 0x1F;

        byte fuIndicator
            = (byte)
                (0xFF
                    & (forbidden_zero_bit
                        | nri
                        | 28 /* nal_unit_type FU-A */));
        byte fuHeader
            = (byte)
                (0xFF
                    & (0x80 /* Start bit */
                        | 0 /* End bit */
                        | 0 /* Reserved bit */
                        | nal_unit_type));

        nalOffset++;
        nalLength--;

        int maxFUPayloadLength
            = MAX_PAYLOAD_SIZE - 2 /* FU indicator & FU header */;
        boolean nalsAdded = false;

        while (nalLength > 0)
        {
            int fuPayloadLength;

            if (nalLength > maxFUPayloadLength)
                fuPayloadLength = maxFUPayloadLength;
            else
            {
                fuPayloadLength = nalLength;
                fuHeader |= 0x40; // Turn on the End bit.
            }

            /*
             * Tests with Asterisk suggest that the fragments of a fragmented
             * NAL unit must be with one and the same size. There is also a
             * similar question on the x264-devel mailing list but,
             * unfortunately, it is unanswered.
             */
            byte[] fua
                = new byte[
                        2 /* FU indicator & FU header */ + maxFUPayloadLength];

            fua[0] = fuIndicator;
            fua[1] = fuHeader;
            System.arraycopy(nal, nalOffset, fua, 2, fuPayloadLength);
            nalOffset += fuPayloadLength;
            nalLength -= fuPayloadLength;

            nalsAdded = nals.add(fua) || nalsAdded;

            fuHeader &= ~0x80; // Turn off the Start bit.
        }
        return nalsAdded;
    }

    /**
     * Processes (packetize) a buffer.
     *
     * @param inBuffer input buffer
     * @param outBuffer output buffer
     * @return <tt>BUFFER_PROCESSED_OK</tt> if buffer has been successfully
     * processed
     */
    @Override
    public int process(Buffer inBuffer, Buffer outBuffer)
    {
        // if there are some nals we check and send them
        if (nals.size() > 0)
        {
            byte[] nal = nals.remove(0);

            // Send the NAL.
            outBuffer.setData(nal);
            outBuffer.setLength(nal.length);
            outBuffer.setOffset(0);
            outBuffer.setTimeStamp(nalsTimeStamp);
            outBuffer.setSequenceNumber(sequenceNumber++);

            // flags
//            int inFlags = inBuffer.getFlags();
//            int outFlags = outBuffer.getFlags();

//            if ((inFlags & Buffer.FLAG_LIVE_DATA) != 0)
//                outFlags |= Buffer.FLAG_LIVE_DATA;
//            if ((inFlags & Buffer.FLAG_RELATIVE_TIME) != 0)
//                outFlags |= Buffer.FLAG_RELATIVE_TIME;
//            if ((inFlags & Buffer.FLAG_RTP_TIME) != 0)
//                outFlags |= Buffer.FLAG_RTP_TIME;
//            if ((inFlags & Buffer.FLAG_SYSTEM_TIME) != 0)
//                outFlags |= Buffer.FLAG_SYSTEM_TIME;
//            outBuffer.setFlags(outFlags);

            // If there are other NALs, send them as well.
            if(nals.size() > 0)
                return (BUFFER_PROCESSED_OK | INPUT_BUFFER_NOT_CONSUMED);
            else
            {
                // It's the last NAL of the current frame so mark it.
                outBuffer.setFlags(
                    outBuffer.getFlags() | Buffer.FLAG_RTP_MARKER);
                return BUFFER_PROCESSED_OK;
            }
        }

        if (isEOM(inBuffer))
        {
            propagateEOM(outBuffer);
            reset();
            return BUFFER_PROCESSED_OK;
        }
        if (inBuffer.isDiscard())
        {
            outBuffer.setDiscard(true);
            reset();
            return BUFFER_PROCESSED_OK;
        }

        Format inFormat = inBuffer.getFormat();

        if ((inFormat != inputFormat) && !inFormat.matches(inputFormat))
            setInputFormat(inFormat);

        int inLength = inBuffer.getLength();

        /*
         * We need 3 bytes for start_code_prefix_one_3bytes and at least 1 byte
         * for the NAL unit i.e. its octet serving as the payload header.
         */
        if (inLength < 4)
        {
            outBuffer.setDiscard(true);
            reset();
            return BUFFER_PROCESSED_OK;
        }

        byte[] inData = (byte[]) inBuffer.getData();
        int inOffset = inBuffer.getOffset();
        boolean nalsAdded = false;

        /*
         * Split the H.264 encoded data into NAL units. Each NAL unit begins
         * with start_code_prefix_one_3bytes. Refer to "B.1 Byte stream NAL unit
         * syntax and semantics" of "ITU-T Rec. H.264 Advanced video coding for
         * generic audiovisual services" for further details.
         */
        int endIndex = inOffset + inLength;
        int beginIndex = ff_avc_find_startcode(inData, inOffset, endIndex);

        if (beginIndex < endIndex)
        {
            beginIndex += 3;

            for (int nextBeginIndex;
                    (beginIndex < endIndex)
                        && ((nextBeginIndex
                                = ff_avc_find_startcode(
                                    inData,
                                    beginIndex,
                                    endIndex))
                            <= endIndex);
                    beginIndex = nextBeginIndex + 3)
            {
                int nalLength = nextBeginIndex - beginIndex;

                // Discard any trailing_zero_8bits.
                while ((nalLength > 0)
                        && (inData[beginIndex + nalLength - 1] == 0))
                    nalLength--;

                if (nalLength > 0)
                    nalsAdded
                        = packetizeNAL(inData, beginIndex, nalLength)
                            || nalsAdded;
            }
        }

        nalsTimeStamp = inBuffer.getTimeStamp();

        return
            nalsAdded ? process(inBuffer, outBuffer) : OUTPUT_BUFFER_NOT_FILLED;
    }

    /**
     * Sets the input format.
     *
     * @param in format to set
     * @return format
     */
    @Override
    public Format setInputFormat(Format in)
    {
        /*
         * Return null if the specified input Format is incompatible with this
         * Packetizer.
         */
        if (!(in instanceof VideoFormat)
                || null == AbstractCodecExt.matches(in, inputFormats))
            return null;

        inputFormat = in;
        return in;
    }

    /**
     * Sets the <tt>Format</tt> in which this <tt>Codec</tt> is to output media
     * data.
     *
     * @param out the <tt>Format</tt> in which this <tt>Codec</tt> is to
     * output media data
     * @return the <tt>Format</tt> in which this <tt>Codec</tt> is currently
     * configured to output media data or <tt>null</tt> if <tt>format</tt> was
     * found to be incompatible with this <tt>Codec</tt>
     */
    @Override
    public Format setOutputFormat(Format out)
    {
        /*
         * Return null if the specified output Format is incompatible with this
         * Packetizer.
         */
        if (!(out instanceof VideoFormat)
                || (null
                        == AbstractCodecExt.matches(
                                out,
                                getMatchingOutputFormats(inputFormat))))
            return null;

        VideoFormat videoOut = (VideoFormat) out;
        Dimension outSize = videoOut.getSize();

        if (outSize == null)
        {
            Dimension inSize = ((VideoFormat) inputFormat).getSize();

            outSize
                = (inSize == null)
                    ? new Dimension(
                            Constants.VIDEO_WIDTH,
                            Constants.VIDEO_HEIGHT)
                    : inSize;
        }

        outputFormat
            = new VideoFormat(
                    videoOut.getEncoding(),
                    outSize,
                    outSize.width * outSize.height,
                    Format.byteArray,
                    videoOut.getFrameRate());

        // Return the outputFormat which is actually set.
        return outputFormat;
    }
}
