/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.chat.filetransfer;

import java.awt.event.*;
import java.io.*;
import java.util.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.main.chat.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;
// Disambiguates SwingWorker on Java 6 in the presence of javax.swing.*
import net.java.sip.communicator.util.swing.SwingWorker;

/**
 * The <tt>ReceiveFileConversationComponent</tt> is the component shown in the
 * conversation area of the chat window to display a incoming file transfer.
 * 
 * @author Yana Stamcheva
 */
public class ReceiveFileConversationComponent
    extends FileTransferConversationComponent
    implements  ActionListener,
                FileTransferStatusListener
{
    private final Logger logger
        = Logger.getLogger(ReceiveFileConversationComponent.class);

    private final IncomingFileTransferRequest fileTransferRequest;

    private final ChatPanel chatPanel;

    private final Date date;

    private final String dateString;

    /**
     * Creates a <tt>ReceiveFileConversationComponent</tt>.
     * 
     * @param fileTransferRequest the <tt>IncomingFileTransferRequest</tt>
     * associated with this component
     */
    public ReceiveFileConversationComponent(
        ChatPanel chatPanel,
        final IncomingFileTransferRequest request,
        final Date date)
    {
        this.chatPanel = chatPanel;
        this.fileTransferRequest = request;
        this.date = date;
        this.dateString = getDateString(date);

        titleLabel.setText(
            dateString
            + resources.getI18NString(
            "service.gui.FILE_TRANSFER_REQUEST_RECIEVED",
            new String[]{fileTransferRequest.getSender().getDisplayName()}));

        fileLabel.setText(fileTransferRequest.getFileName());

        acceptButton.setVisible(true);
        acceptButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                titleLabel.setText(
                    dateString
                    + resources
                    .getI18NString("service.gui.FILE_TRANSFER_PREPARING",
                                    new String[]{fileTransferRequest.getSender()
                                                .getDisplayName()}));
                acceptButton.setVisible(false);
                rejectButton.setVisible(false);
                cancelButton.setVisible(true);
                progressBar.setVisible(true);

                File downloadFile = createFile(fileTransferRequest);

                new AcceptFile(downloadFile).start();
            }
        });

        rejectButton.setVisible(true);
        rejectButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                fileTransferRequest.rejectFile();

                acceptButton.setVisible(false);
                rejectButton.setVisible(false);
                fileLabel.setText("");
                titleLabel.setText(
                    dateString
                    + resources.getI18NString(
                        "service.gui.FILE_TRANSFER_CANCELED"));
            }
        });

        progressBar.setMaximum((int)fileTransferRequest.getFileSize());
    }

    /**
     * Creates the file to download.
     * 
     * @return the file to download.
     */
    private File createFile(IncomingFileTransferRequest fileTransferRequest)
    {
        File downloadFile = null;
        File downloadDir = null;

        String incomingFileName = fileTransferRequest.getFileName();
        try
        {
            downloadDir = GuiActivator.getFileAccessService()
                .getDefaultDownloadDirectory();

            if (!downloadDir.exists())
            {
                if (!downloadDir.mkdirs())
                {
                    logger.error("Could not create the download directory : "
                        + downloadDir.getAbsolutePath());
                }
                logger.debug("Download directory created : "
                        + downloadDir.getAbsolutePath());
            }
        }
        catch (IOException e)
        {
            logger.debug("Unable to find download directory.", e);
        }

        downloadFile = new File(downloadDir, incomingFileName);

        // If a file with the given name already exists, add an index to the
        // file name.
        int index = 0;
        while (downloadFile.exists())
        {
            String newFileName
             = incomingFileName.substring(0, incomingFileName.lastIndexOf("."))
                 + "-" + ++index
                 + incomingFileName.substring(incomingFileName.lastIndexOf("."));

            downloadFile = new File(downloadDir, newFileName);
        }

        fileLabel.setText(downloadFile.getName());

        return downloadFile;
    }

    /**
     * Handles status changes in file transfer.
     */
    public void statusChanged(FileTransferStatusChangeEvent event)
    {
        int status = event.getNewStatus();
        FileTransfer fileTransfer = event.getFileTransfer();

        String fromContactName
            = fileTransferRequest.getSender().getDisplayName();

        if (status == FileTransferStatusChangeEvent.PREPARING)
        {
            progressBar.setVisible(false);
            titleLabel.setText(
                dateString
                + resources.getI18NString(
                "service.gui.FILE_TRANSFER_PREPARING",
                new String[]{fromContactName}));
        }
        else if (status == FileTransferStatusChangeEvent.FAILED)
        {
            progressBar.setVisible(false);
            titleLabel.setText(
                dateString
                + resources.getI18NString(
                "service.gui.FILE_RECEIVE_FAILED",
                new String[]{fromContactName}));

            setWarningStyle(true);
        }
        else if (status == FileTransferStatusChangeEvent.IN_PROGRESS)
        {
            titleLabel.setText(
                dateString
                + resources.getI18NString(
                "service.gui.FILE_RECEIVING_FROM",
                new String[]{fromContactName}));
            setWarningStyle(false);

            if (!progressBar.isVisible())
            {
                progressBar.setVisible(true);
            }
        }
        else if (status == FileTransferStatusChangeEvent.COMPLETED)
        {
            this.setCompletedDownloadFile(fileTransfer.getFile());

            progressBar.setVisible(false);
            cancelButton.setVisible(false);
            openFileButton.setVisible(true);
            openFolderButton.setVisible(true);

            titleLabel.setText(
                dateString
                + resources.getI18NString(
                "service.gui.FILE_RECEIVE_COMPLETED",
                new String[]{fromContactName}));
        }
        else if (status == FileTransferStatusChangeEvent.CANCELED)
        {
            progressBar.setVisible(false);
            cancelButton.setVisible(false);

            titleLabel.setText(
                dateString
                + resources.getI18NString(
                "service.gui.FILE_TRANSFER_CANCELED"));
            setWarningStyle(true);
        }
        else if (status == FileTransferStatusChangeEvent.REFUSED)
        {
            progressBar.setVisible(false);
            titleLabel.setText(
                dateString
                + resources.getI18NString(
                "service.gui.FILE_TRANSFER_REFUSED",
                new String[]{fromContactName}));
            cancelButton.setVisible(false);
            setWarningStyle(true);
        }
    }

    /**
     * Returns the date of the component event.
     * 
     * @return the date of the component event
     */
    public Date getDate()
    {
        return date;
    }

    /**
     * Accepts the file in a new thread.
     */
    private class AcceptFile extends SwingWorker
    {
        private FileTransfer fileTransfer;

        private final File downloadFile;

        public AcceptFile(File downloadFile)
        {
            this.downloadFile = downloadFile;
        }

        public Object construct()
        {
            fileTransfer = fileTransferRequest.acceptFile(downloadFile);

            chatPanel.addActiveFileTransfer(fileTransfer.getID(), fileTransfer);

            // Add the status listener that would notify us when the file
            // transfer has been completed and should be removed from
            // active components.
            fileTransfer.addStatusListener(chatPanel);

            fileTransfer.addStatusListener(
                ReceiveFileConversationComponent.this);

            return "";
        }

        public void finished()
        {
            if (fileTransfer != null)
            {
                setFileTransfer(fileTransfer);
            }
        }
    }
}
