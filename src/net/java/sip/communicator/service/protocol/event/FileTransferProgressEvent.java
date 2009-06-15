/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.service.protocol.event;

import java.util.*;

import net.java.sip.communicator.service.protocol.*;

/**
 * The <tt>FileTransferProgressEvent</tt> indicates the progress of a file
 * transfer.
 * 
 * @author Yana Stamcheva
 */
public class FileTransferProgressEvent
    extends EventObject
{
    /**
     * Indicates the progress of a file transfer in bytes.
     */
    private int progress;

    /**
     * Creates a <tt>FileTransferProgressEvent</tt> by specifying the source
     * file transfer object, that triggered the event and the new progress
     * value.
     * 
     * @param fileTransfer the source file transfer object, that triggered the
     * event
     * @param progress the new progress value
     */
    public FileTransferProgressEvent(   FileTransfer fileTransfer,
                                        int progress)
    {
        super(fileTransfer);

        this.progress = progress;
    }

    /**
     * Returns the source <tt>FileTransfer</tt> that triggered this event.
     * 
     * @return the source <tt>FileTransfer</tt> that triggered this event
     */
    public FileTransfer getFileTransfer()
    {
        return (FileTransfer) source;
    }

    /**
     * Returns the progress of the file transfer.
     * 
     * @return the progress of the file transfer
     */
    public int getProgress()
    {
        return progress;
    }
}
