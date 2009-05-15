/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license. See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.account;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.service.configuration.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.swing.*;

/**
 * The <tt>AccountsConfigurationPanel</tt> is the panel containing the accounts
 * list and according buttons shown in the options form.
 * 
 * @author Yana Stamcheva
 */
public class AccountsConfigurationPanel
    extends TransparentPanel
    implements  ActionListener
{
    private final AccountList accountList;

    private final JButton newButton =
        new JButton(GuiActivator.getResources().getI18NString(
            "service.gui.ADD"));

    private final JButton editButton =
        new JButton(GuiActivator.getResources().getI18NString(
            "service.gui.EDIT"));

    private final JButton removeButton =
        new JButton(GuiActivator.getResources().getI18NString(
            "service.gui.DELETE"));

    public AccountsConfigurationPanel()
    {
        super(new BorderLayout());

        accountList = new AccountList(this);

        this.setPreferredSize(new Dimension(500, 400));

        JScrollPane accountListPane = new JScrollPane();

        accountListPane.getViewport().add(accountList);
        accountListPane.getVerticalScrollBar().setUnitIncrement(30);

        this.add(accountListPane, BorderLayout.CENTER);

        JPanel buttonsPanel =
            new TransparentPanel(new FlowLayout(FlowLayout.RIGHT));

        newButton.addActionListener(this);
        editButton.addActionListener(this);
        removeButton.addActionListener(this);

        this.newButton.setMnemonic(GuiActivator.getResources().getI18nMnemonic(
                "service.gui.ADD"));
        this.editButton
            .setMnemonic(GuiActivator.getResources().getI18nMnemonic(
                "service.gui.EDIT"));
        this.removeButton
            .setMnemonic(GuiActivator.getResources().getI18nMnemonic(
                "service.gui.DELETE"));

        buttonsPanel.add(newButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(removeButton);

        this.add(buttonsPanel, BorderLayout.SOUTH);
    }

    /**
     * Handles the <tt>ActionEvent</tt> triggered when user clicks on on the
     * buttons. Shows the account registration wizard when user clicks on "New".
     *
     * @param evt the action event that has just occurred.
     */
    public void actionPerformed(ActionEvent evt)
    {
        JButton sourceButton = (JButton) evt.getSource();

        if (sourceButton.equals(newButton))
        {
            NewAccountDialog.showNewAccountDialog();
        }
        else if (sourceButton.equals(removeButton))
        {
            ProtocolProviderService protocolProvider
                = (ProtocolProviderService) accountList.getSelectedValue();

            ProtocolProviderFactory providerFactory =
                GuiActivator.getProtocolProviderFactory(protocolProvider);

            if (providerFactory != null)
            {
                int result =
                    JOptionPane.showConfirmDialog(this, GuiActivator
                        .getResources().getI18NString(
                            "service.gui.REMOVE_ACCOUNT_MESSAGE"),
                        GuiActivator.getResources().getI18NString(
                            "service.gui.REMOVE_ACCOUNT"),
                        JOptionPane.YES_NO_OPTION);

                if (result == JOptionPane.YES_OPTION)
                {
                    ConfigurationService configService =
                        GuiActivator.getConfigurationService();

                    String prefix =
                        "net.java.sip.communicator.impl.gui.accounts";

                    List<String> accounts =
                        configService
                            .getPropertyNamesByPrefix(prefix, true);

                    for (String accountRootPropName : accounts)
                    {
                        String accountUID =
                            configService.getString(accountRootPropName);

                        if (accountUID.equals(protocolProvider
                            .getAccountID().getAccountUniqueID()))
                        {
                            configService.setProperty(accountRootPropName,
                                null);
                            break;
                        }
                    }
                    providerFactory.uninstallAccount(protocolProvider
                        .getAccountID());
                }
            }
        }
        else if (sourceButton.equals(editButton))
        {
            ProtocolProviderService protocolProvider
                = (ProtocolProviderService) accountList.getSelectedValue();

            AccountRegWizardContainerImpl wizard =
                (AccountRegWizardContainerImpl) GuiActivator.getUIService()
                    .getAccountRegWizardContainer();

            wizard.setTitle(GuiActivator.getResources().getI18NString(
                "service.gui.ACCOUNT_REGISTRATION_WIZARD"));

            wizard.modifyAccount(protocolProvider);
            wizard.showDialog(false);
        }
    }

    /**
     * Returns the edit button.
     * 
     * @return the edit button
     */
    public JButton getEditButton()
    {
        return editButton;
    }
}
