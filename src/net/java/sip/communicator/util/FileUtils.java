/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.util;

import java.io.*;

import javax.swing.*;

/**
 * Utility class that allows to check if a given file is an image or to obtain
 * the file thumbnail icon.
 * 
 * @author Yana Stamcheva
 */
public class FileUtils
{
    private static final Logger logger = Logger.getLogger(FileUtils.class);

    /**
     * Returns <code>true</code> if the file given by <tt>fileName</tt> is an
     * image, <tt>false</tt> - otherwise.
     * 
     * @param fileName the name of the file to check
     * @return <code>true</code> if the file is an image, <tt>false</tt> -
     * otherwise.
     */
    public static boolean isImage(String fileName)
    {
        fileName = fileName.toLowerCase();

        String[] imageTypes = {"jpeg", "jpg", "png", "gif"};

        for (String imageType : imageTypes)
        {
            if (fileName.endsWith(imageType))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the native icon of the given file if one exists, otherwise
     * returns null.
     *
     * @param file the file to obtain icon for
     * @return the native icon of the given file if one exists, otherwise
     * returns null.
     * TODO: Use JNA to implement this under Linux.
     */
    public static Icon getIcon(File file)
    {
        Icon fileIcon = null;

        try
        {
            sun.awt.shell.ShellFolder shellFolder
                = sun.awt.shell.ShellFolder.getShellFolder(file);

            fileIcon = new ImageIcon(   shellFolder.getIcon(true),
                                        shellFolder.getFolderType());
        }
        catch (Exception e)
        {
            logger.debug("Failed to obtain file icon from ShellFolder.", e);
            try
            {
                fileIcon = new JFileChooser().getIcon(file);
            }
            catch (Exception e1)
            {
                logger.debug("Failed to obtain file icon from JFileChooser.", e);
            }
        }

        return fileIcon;
    }
}
