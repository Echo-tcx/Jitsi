/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.googlecontacts.configform;

import java.util.*;

import javax.swing.table.*;

import net.java.sip.communicator.service.googlecontacts.*;
import net.java.sip.communicator.impl.googlecontacts.*;


/**
 * A table model suitable for the directories list in
 * the configuration form. Takes its data in an LdapDirectorySet.
 *
 * @author Sebastien Mazy
 * @author Sebastien Vincent
 */
public class GoogleContactsTableModel
    extends AbstractTableModel
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 0L;

    /**
     * Google Contacts service reference.
     */
    private final GoogleContactsServiceImpl googleService =
        GoogleContactsActivator.getGoogleContactsService();

    /**
     * Add account from table.
     *
     * @param cnx account
     * @param enabled if the account should be enabled
     */
    public void addAccount(GoogleContactsConnection cnx, boolean enabled)
    {
        if(cnx != null)
        {
            ((GoogleContactsConnectionImpl)cnx).setEnabled(enabled);
            googleService.getAccounts().add((GoogleContactsConnectionImpl)cnx);
        }
    }

    /**
     * Remove account from table.
     *
     * @param login account login to remove
     */
    public void removeAccount(String login)
    {
        Iterator<GoogleContactsConnectionImpl> it =
            googleService.getAccounts().iterator();

        while(it.hasNext())
        {
            GoogleContactsConnection cnx = it.next();
            if(cnx.getLogin().equals(login))
            {
                it.remove();
                return;
            }
        }
    }

    /**
     * Returns the title for this column
     *
     * @param column the column
     *
     * @return the title for this column
     *
     * @see javax.swing.table.AbstractTableModel#getColumnName
     */
    public String getColumnName(int column)
    {
        switch(column)
        {
            case 0:
                return Resources.getString("impl.googlecontacts.ENABLED");
            case 1:
                return Resources.getString("impl.googlecontacts.ACCOUNT_NAME");
            default:
                throw new IllegalArgumentException("column not found");
        }
    }

    /**
     * Returns the number of rows in the table
     *
     * @return the number of rows in the table
     * @see javax.swing.table.AbstractTableModel#getRowCount
     */
    public int getRowCount()
    {
        return googleService.getAccounts().size();
    }

    /**
     * Returns the number of column in the table
     *
     * @return the number of columns in the table
     *
     * @see javax.swing.table.AbstractTableModel#getColumnCount
     */
    public int getColumnCount()
    {
        // 2 columns: "enable" and "account name"
        return 2;
    }

    /**
     * Returns the text for the given cell of the table
     *
     * @param row cell row
     * @param column cell column
     *
     * @see javax.swing.table.AbstractTableModel#getValueAt
     */
    public Object getValueAt(int row, int column)
    {
        switch(column)
        {
            case 0:
                return new Boolean(getAccountAt(row).isEnabled());
            case 1:
                return getAccountAt(row).getLogin();
            default:
                throw new IllegalArgumentException("column not found");
        }
    }

    /**
     * Returns the account credentials at the row 'row'
     *
     * @param row the row
     *
     * @return the login/password for the account
     */
    public GoogleContactsConnectionImpl getAccountAt(int row)
    {
        if(row < 0 || row >= googleService.getAccounts().size())
        {
            throw new IllegalArgumentException("row not found");
        }
        else
        {
            return googleService.getAccounts().get(row);
        }
    }

    /**
     * Returns whether a cell is editable. Only "enable" column (checkboxes)
     * is editable
     *
     * @param row row of the cell
     * @param col column of the cell
     *
     * @return whether the cell is editable
     */
    public boolean isCellEditable(int row, int col)
    {
        if(col == 0)
            return true;
        else
            return false;
    }

    /**
     * Overrides a method that always returned Object.class
     * Now it will return Boolean.class for the first method,
     * letting the DefaultTableCellRenderer create checkboxes.
     *
     * @param columnIndex index of the column
     * @return Column class
     */
    public Class<?> getColumnClass(int columnIndex)
    {
        return getValueAt(0, columnIndex).getClass();
    }

    /**
     * Sets a value in an editable cell, that is to say
     * an enable/disable chekbox in colum 0
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        if(columnIndex != 0)
            throw new IllegalArgumentException("non editable column!");

        GoogleContactsConfigForm.RefreshContactSourceThread th = null;
        GoogleContactsConnectionImpl cnx = getAccountAt(rowIndex);

        if(cnx.isEnabled())
        {
            th = new GoogleContactsConfigForm.RefreshContactSourceThread(cnx,
                    null);
        }
        else
        {
            th = new GoogleContactsConfigForm.RefreshContactSourceThread(null,
                    cnx);
        }

        cnx.setEnabled(!cnx.isEnabled());

        th.start();
    }
}
