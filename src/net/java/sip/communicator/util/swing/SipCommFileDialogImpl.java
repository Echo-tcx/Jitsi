/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.util.swing;

import java.awt.*;
import java.io.*;

/**
 * Implements <tt>SipCommFileChooser</tt> for AWT's <tt>FileDialog</tt>.
 *
 * @author Valentin Martinet
 */
public class SipCommFileDialogImpl 
    extends FileDialog 
    implements SipCommFileChooser
{
    /**
     * The serialization-related version of the <tt>SipCommFileDialogImpl</tt>
     * class explicitly defined to silence a related warning (e.g. in Eclipse
     * IDE) since the <tt>SipCommFileDialogImpl</tt> class does not add instance
     * fields.
     */
    private static final long serialVersionUID = 0L;

    /**
     * Constructor
     * 
     * @param parent the parent frame of this dialog
     * @param title the title for this dialog
     */
    public SipCommFileDialogImpl(Frame parent, String title)
    {
        super(parent, title);
    }

    /**
     * Constructor
     * 
     * @param parent the parent frame of this dialog
     * @param title the title for this dialog
     * @param fileOperation request a 'load file' or 'save file' dialog
     */
    public SipCommFileDialogImpl(Frame parent, String title, int fileOperation)
    {
        super(parent, title, fileOperation);
    }

    /**
     * Returns the selected file by the user from the dialog.
     * 
     * @return File the selected file from the dialog
     */
    public File getApprovedFile() 
    {
        String file = getFile();

        return (file != null) ? new File(getDirectory(), file) : null;
    }

    /**
     * Sets the default path to be considered for browsing among files.
     * 
     * @param path the default start path for this dialog
     */
    public void setStartPath(String path) 
    {
        File file = (path == null) ? null : new File(path);

        if ((file != null) && !file.isDirectory())
        {
            setDirectory(file.getParent());
            setFile(file.getName());
        }
        else
            setDirectory(path);
    }

    /**
     * Shows the dialog and returns the selected file.
     * 
     * @return File the selected file in this dialog
     */
    public File getFileFromDialog()
    {
        this.setVisible(true);

        return this.getApprovedFile();
    }

    /**
     * Adds a file filter to this dialog.
     * 
     * @param filter the filter to add
     */
    public void addFilter(SipCommFileFilter filter) 
    {
        this.setFilenameFilter(filter);
    }

    /**
     * Returns the filter the user has chosen for saving a file.
     *
     * @return SipCommFileFilter the used filter when saving a file
     */
    public SipCommFileFilter getUsedFilter()
    {
        return (SipCommFileFilter)this.getFilenameFilter();
    }
}
