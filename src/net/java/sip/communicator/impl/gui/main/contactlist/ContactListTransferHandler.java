/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.contactlist;

import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.main.chat.*;
import net.java.sip.communicator.service.contactlist.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.swing.*;

/**
 * A TransferHandler that we use to handle DnD operations of files in our
 * <tt>ContactList</tt>.
 *
 * @author Yana Stamcheva
 */
public class ContactListTransferHandler
    extends ExtendedTransferHandler
{
    private static final Logger logger
        = Logger.getLogger(ContactListTransferHandler.class);

    private final DefaultContactList contactList;

    public ContactListTransferHandler(DefaultContactList contactList)
    {
        this.contactList = contactList;
    }

    /**
     * Handles transfers to the contact list from the clip board or a
     * DND drop operation. The <tt>Transferable</tt> parameter contains the
     * data that needs to be imported.
     *
     * @param comp the component to receive the transfer
     * @param t the data to import
     * @return <tt>true</tt> if the data was inserted into the component;
     * <tt>false</tt>, otherwise
     * @see TransferHandler#importData(JComponent. Transferable)
     */
    @SuppressWarnings("unchecked") //taken care of
    public boolean importData(JComponent comp, Transferable t)
    {
        if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
        {
            Object o = null;

            try
            {
                o = t.getTransferData(DataFlavor.javaFileListFlavor);
            }
            catch (UnsupportedFlavorException e)
            {
                logger.debug("Failed to drop files.", e);
            }
            catch (IOException e)
            {
                logger.debug("Failed to drop files.", e);
            }

            if (o instanceof Collection)
            {
                ChatPanel chatPanel = getChatPanel();

                if (chatPanel != null)
                {
                    Collection<File> files = (Collection<File>) o;

                    for (File file : files)
                    {
                        chatPanel.sendFile(file);

                        GuiActivator.getUIService().getChatWindowManager()
                            .openChat(chatPanel, false);
                    }

                    // Otherwise fire files dropped event.
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the <tt>ChatPanel</tt> corresponding to the currently selected
     * contact.
     *
     * @return the <tt>ChatPanel</tt> corresponding to the currently selected
     * contact.
     */
    private ChatPanel getChatPanel()
    {
        ChatPanel chatPanel = null;
        Object selectedObject = contactList.getSelectedValue();

        if (selectedObject instanceof MetaContact)
        {
            MetaContact metaContact = (MetaContact) selectedObject;

            // Obtain the corresponding chat panel.
            chatPanel
                = GuiActivator.getUIService().getChatWindowManager()
                    .getContactChat(metaContact);
        }

        return chatPanel;
    }
}
