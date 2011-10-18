/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.chat.history;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.utils.*;
import net.java.sip.communicator.util.skin.*;
import net.java.sip.communicator.util.swing.*;

/**
 * The <tt>SearchPanel</tt> is the panel, where user could make a search in
 * the message history. The search could be made by specifying a date
 * or an hour, or searching by a keyword.
 *
 * @author Yana Stamcheva
 * @author Lubomir Marinov
 * @author Adam Netocny
 */
public class SearchPanel
    extends TransparentPanel
    implements  ActionListener,
                DocumentListener,
                Skinnable
{
    private JTextField searchTextField = new JTextField();

    private final HistoryWindow historyWindow;

    /**
     * Search button.
     */
    private JButton searchButton;

    /**
     * Creates an instance of the <tt>SearchPanel</tt>.
     *
     * @param historyWindow the parent history window
     */
    public SearchPanel(HistoryWindow historyWindow)
    {
        super(new BorderLayout(5, 5));

        this.historyWindow = historyWindow;

        this.init();
    }

    /**
     * Constructs the <tt>SearchPanel</tt>.
     */
    private void init()
    {
        String searchString
            = GuiActivator.getResources().getI18NString("service.gui.SEARCH");
        JLabel searchLabel = new JLabel(searchString + ": ");

        searchButton
            = new JButton(
                    searchString,
                    new ImageIcon(
                            ImageLoader.getImage(ImageLoader.SEARCH_ICON)));

        this.searchTextField.getDocument().addDocumentListener(this);

        this.add(searchLabel, BorderLayout.WEST);
        this.add(searchTextField, BorderLayout.CENTER);

        searchButton.setName("search");
        searchButton.setMnemonic(
            GuiActivator.getResources().getI18nMnemonic("service.gui.SEARCH"));

        searchButton.addActionListener(this);

        this.historyWindow.getRootPane().setDefaultButton(searchButton);

        this.add(searchButton, BorderLayout.EAST);
    }

    /**
     * Handles the <tt>ActionEvent</tt> which occurred when user clicks
     * the Search button.
     *
     * @param e the <tt>ActionEvent</tt> that notified us of the button click
     */
    public void actionPerformed(ActionEvent e)
    {
        JButton button = (JButton) e.getSource();
        String buttonName = button.getName();

        if (buttonName.equalsIgnoreCase("search"))
        {
            historyWindow.showHistoryByKeyword(searchTextField.getText());
        }
    }

    public void insertUpdate(DocumentEvent e) {}

    /**
     * When all text is removed from the search field shows the whole history.
     *
     * @param e the <tt>DocumentEvent</tt> that notified us of the text remove
     */
    public void removeUpdate(DocumentEvent e)
    {
        if (searchTextField.getText() == null
                || searchTextField.getText().equals(""))
        {
            historyWindow.showHistoryByKeyword("");
        }
    }

    public void changedUpdate(DocumentEvent e) {}

    /**
     * Reloads search button icon.
     */
    public void loadSkin()
    {
        searchButton.setIcon(new ImageIcon(
                ImageLoader.getImage(ImageLoader.SEARCH_ICON)));
    }
}
