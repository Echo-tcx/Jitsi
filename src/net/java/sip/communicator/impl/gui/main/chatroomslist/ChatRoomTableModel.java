/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.chatroomslist;

import java.beans.*;
import java.util.*;
import java.util.concurrent.*;

import javax.swing.*;
import javax.swing.table.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.main.chat.conference.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;

/**
 * The model for the table with saved rooms.
 *
 * @author Damian Minkov
 */
public class ChatRoomTableModel
    extends AbstractTableModel
    implements ChatRoomListChangeListener,
               ProviderPresenceStatusListener,
               ChatRoomList.ChatRoomProviderWrapperListener
{
    /**
     * The <tt>ChatRoomsList</tt> is the list containing all chat rooms.
     */
    private final ChatRoomList chatRoomList;

    /**
     * The column names for the table.
     */
    private final static String[] COLUMN_NAMES =
        new String[]{
        GuiActivator.getResources().getI18NString("service.gui.ROOM_NAME"),
        GuiActivator.getResources().getI18NString("service.gui.ACCOUNT"),
        GuiActivator.getResources().getI18NString("service.gui.AUTOJOIN")};

    /**
     * The current list of rooms.
     */
    private List<ChatRoomWrapper> rooms =
        new CopyOnWriteArrayList<ChatRoomWrapper>();

    /**
     * The parent table.
     */
    private JTable parentTable = null;

    /**
     * Creates new model for the supplied table.
     * 
     * @param parentTable the parent table.
     */
    public ChatRoomTableModel(JTable parentTable)
    {
        this.parentTable = parentTable;

        chatRoomList = GuiActivator.getUIService()
            .getConferenceChatManager().getChatRoomList();

        chatRoomList.addChatRoomProviderWrapperListener(this);

        Iterator<ChatRoomProviderWrapper> iter =
            chatRoomList.getChatRoomProviders();
        while (iter.hasNext())
        {
            ChatRoomProviderWrapper provider = iter.next();
            if(!provider.getProtocolProvider().getAccountID().isEnabled())
            {
                continue;
            }

            handleProviderAdded(provider);
        }
    }

    /**
     * Performs all actions on chat room provider added. Add listeners and
     * add its saved rooms to the list of rooms.
     *
     * @param chatProviderWrapper the provider.
     */
    private void handleProviderAdded(
            ChatRoomProviderWrapper chatProviderWrapper)
    {
        for (int i = 0; i < chatProviderWrapper.countChatRooms(); i++)
        {
            addChatRoom(chatProviderWrapper.getChatRoom(i), false);
        }

        OperationSetPresence presence =
                chatProviderWrapper.getProtocolProvider()
                    .getOperationSet(OperationSetPresence.class);

        if(presence != null)
            presence.addProviderPresenceStatusListener(this);
    }

    /**
     * Performs all actions on chat room provider removed. Remove listeners and
     * remove its saved rooms of the list of rooms.
     *
     * @param chatProviderWrapper the provider.
     */
    private void handleProviderRemoved(
            ChatRoomProviderWrapper chatProviderWrapper)
    {
        OperationSetPresence presence =
                chatProviderWrapper.getProtocolProvider()
                    .getOperationSet(OperationSetPresence.class);

        if(presence != null)
            presence.removeProviderPresenceStatusListener(this);

        for (int i = 0; i < chatProviderWrapper.countChatRooms(); i++)
        {
            ChatRoomWrapper room = chatProviderWrapper.getChatRoom(i);
            removeChatRoom(room);
        }
    }

    /**
     * Returns true if the cell at <code>rowIndex</code> and
     * <code>columnIndex</code>
     * is editable.
     *
     * @param	row	the row whose value to be queried
     * @param	column	the column whose value to be queried
     * @return	true if the cell is editable
     */
    @Override
    public boolean isCellEditable(int row, int column)
    {
        if(column == 2)
            return true;
        else
            return super.isCellEditable(row, column);
    }

    /**
     * Returns the number of rows in the model.
     *
     * @return the number of rows in the model
     */
    public int getRowCount()
    {
        return rooms.size();
    }

    /**
     * Returns the number of columns in the model. 
     *
     * @return the number of columns in the model
     */
    public int getColumnCount()
    {
        return COLUMN_NAMES.length;
    }

    /**
     * The room wrapper for the supplied row.
     * @param rowIndex the row.
     * @return the chat room wrapper.
     */
    ChatRoomWrapper getValueAt(int rowIndex)
    {
        if(rowIndex >= 0 && rowIndex < rooms.size())
            return rooms.get(rowIndex);
        return null;
    }

    /**
     * Returns the value for the cell at <code>columnIndex</code> and
     * <code>rowIndex</code>.
     *
     * @param	rowIndex	the row whose value is to be queried
     * @param	columnIndex 	the column whose value is to be queried
     * @return	the value Object at the specified cell
     */
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        ChatRoomWrapper room = rooms.get(rowIndex);

        switch(columnIndex)
        {
            case 0: return room;
            case 1: return room.getParentProvider().getProtocolProvider();
            case 2: return room.isAutojoin();
            default: return room;
        }
    }

    /**
     * Sets the value in the cell at <code>columnIndex</code> and
     * <code>rowIndex</code> to <code>aValue</code>.
     *
     * @param	aValue		 the new value
     * @param	rowIndex	 the row whose value is to be changed
     * @param	columnIndex 	 the column whose value is to be changed
     */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        if(columnIndex == 2)
        {
            ChatRoomWrapper room = rooms.get(rowIndex);
            room.setAutoJoin((Boolean)aValue);
        }
        else
            super.setValueAt(aValue, rowIndex, columnIndex);
    }

    /**
     * Returns the name of the column at <code>columnIndex</code>.
     *
     * @param	column	the index of the column
     * @return  the name of the column
     */
    @Override
    public String getColumnName(int column)
    {
        if(column < COLUMN_NAMES.length)
            return COLUMN_NAMES[column];
        else
            return super.getColumnName(column);
    }

    /**
     * Returns the most specific superclass for all the cell values
     * in the column.
     *
     * @param columnIndex  the index of the column
     * @return the common ancestor class of the object values in the model.
     */
    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        switch(columnIndex)
        {
            case 0: return ChatRoomWrapper.class;
            case 1: return ProtocolProviderService.class;
            case 2: return Boolean.class;
            default: return super.getColumnClass(columnIndex);
        }
    }

    /**
     * Refreshes the chat room's list when a modification in the model has
     * occurred.
     * @param evt
     */
    public void contentChanged(ChatRoomListChangeEvent evt)
    {
        ChatRoomWrapper chatRoomWrapper = evt.getSourceChatRoom();

        if (evt.getEventID() == ChatRoomListChangeEvent.CHAT_ROOM_ADDED)
        {
            addChatRoom(chatRoomWrapper, true);
        }
        else if (evt.getEventID() == ChatRoomListChangeEvent.CHAT_ROOM_REMOVED)
        {
            removeChatRoom(chatRoomWrapper);
        }
        else if (evt.getEventID() == ChatRoomListChangeEvent.CHAT_ROOM_CHANGED)
        {
            int index = rooms.indexOf(chatRoomWrapper);

            if(index != -1)
            {
                fireTableRowsUpdated(index, index);
            }
        }
    }

    /**
     * Remove chat room from the ui.
     * @param chatRoomWrapper the room wrapper.
     */
    private void removeChatRoom(ChatRoomWrapper chatRoomWrapper)
    {
        int ix = rooms.indexOf(chatRoomWrapper);
        rooms.remove(chatRoomWrapper);

        if (ix != -1)
        {
            fireTableRowsDeleted(ix, ix);
        }
    }

    /**
     * Adds a chat room to the ui, updates the ui and if pointed selects
     * that chat room.
     * @param chatRoomWrapper the room to add.
     * @param select whether we should select the room.
     */
    private void addChatRoom(ChatRoomWrapper chatRoomWrapper, boolean select)
    {
        rooms.add(chatRoomWrapper);
        int index = rooms.indexOf(chatRoomWrapper);

        if (index != -1)
        {
            fireTableRowsInserted(index, index);

            if(select)
                parentTable.setRowSelectionInterval(index, index);
        }
    }

    /**
     * Listens for provider status change to change protocol icon.
     * @param evt the event
     */
    public void providerStatusChanged(ProviderPresenceStatusChangeEvent evt)
    {
        ProtocolProviderService pps = evt.getProvider();

        Iterator<ChatRoomWrapper> iter = rooms.iterator();
        int row = 0;
        while (iter.hasNext())
        {
            ChatRoomWrapper chatRoomWrapper = iter.next();

            if(chatRoomWrapper.getParentProvider()
                .getProtocolProvider().equals(pps))
            {
                fireTableCellUpdated(row, 1);
            }

            row++;
        }
    }

    /**
     * Not used.
     * @param evt the event.
     */
    public void providerStatusMessageChanged(PropertyChangeEvent evt)
    {}

    /**
     * When a provider wrapper is added this method is called to inform
     * listeners.
     *
     * @param provider which was added.
     */
    public void chatRoomProviderWrapperAdded(ChatRoomProviderWrapper provider)
    {
        handleProviderAdded(provider);
    }

    /**
     * When a provider wrapper is removed this method is called to inform
     * listeners.
     *
     * @param provider which was removed.
     */
    public void chatRoomProviderWrapperRemoved(ChatRoomProviderWrapper provider)
    {
        handleProviderRemoved(provider);
    }
}
