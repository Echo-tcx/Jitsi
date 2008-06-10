/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 * SSHContactInfo.java
 *
 * SSH Suport in SIP Communicator - GSoC' 07 Project
 *
 */

package net.java.sip.communicator.impl.protocol.ssh;

import java.text.ParseException;
import net.java.sip.communicator.service.gui.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 *
 * @author Shobhit Jindal
 */
class SSHContactInfo extends JDialog
        implements ConfigurationForm {
    private ContactSSH sshContact;
    
    private JPanel mainPanel = new JPanel();
    private JPanel machinePanel = new JPanel();
    private JPanel detailNamesPanel = new JPanel();
    private JPanel detailFieldsPanel = new JPanel();
    private JPanel detailsPanel = new JPanel();
    
    private JCheckBox addDetailsCheckBox = new JCheckBox("Add Details");
    
    private JButton doneButton = new JButton("Done");
    private JLabel machineID = new JLabel("Hostname / IP: ");
    private JTextField machineIDField = new JTextField();
    private JLabel userName = new JLabel("User Name: ");
    private JTextField userNameField = new JTextField();
    private JLabel password = new JLabel("Password: ");
    private JTextField passwordField = new JPasswordField();
    private JLabel port = new JLabel("Port: ");
    
    private JFormattedTextField portField;
    private JLabel secs = new JLabel("secs");
    private JLabel statusUpdate = new JLabel("Update Interval: ");
    private JLabel terminalType = new JLabel("Terminal Type: ");
    private JTextField terminalTypeField = new JTextField("SIP Communicator");
    private JSpinner updateTimer = new JSpinner();
    
    private JPanel emptyPanel1 = new JPanel();
    
    private JPanel emptyPanel2 = new JPanel();
    
    private JPanel emptyPanel3 = new JPanel();
    
    private JPanel emptyPanel4 = new JPanel();
    
    private JPanel emptyPanel5 = new JPanel();
    
    private JPanel emptyPanel6 = new JPanel();
    
    private JPanel emptyPanel7 = new JPanel();
    
    private JPanel emptyPanel8 = new JPanel();
    
    private JPanel emptyPanel9 = new JPanel();
    
    private JPanel emptyPanel10 = new JPanel();
    
    private JPanel emptyPanel11 = new JPanel();
    
//    private ContactGroup contactGroup = null;
    
    /**
     * Creates a new instance of SSHContactInfo
     * 
     * @param sshContact the concerned contact
     */
    public SSHContactInfo(ContactSSH sshContact) {
        super(new JFrame(), true);
        this.sshContact = sshContact;
        initForm();
        
        this.getContentPane().add(mainPanel);
        
        this.setSize(370, 325);
        
        this.setResizable(false);
        
        this.setTitle("SSH: Account Details of " + sshContact.getDisplayName());
        
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        
        int x = (screenSize.width - this.getWidth()) / 2;
        int y = (screenSize.height - this.getHeight()) / 2;
        
        this.setLocation(x,y);
        
//        ProtocolProviderServiceSSHImpl.getUIService().getConfigurationWindow().
//                addConfigurationForm(this);
    }
    
    /**
     * initialize the form.
     */
    public void initForm() {
        updateTimer.setValue(new Integer(30));
        MaskFormatter maskFormatter = new MaskFormatter();
        try {
            maskFormatter.setMask("#####");
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        maskFormatter.setAllowsInvalid(false);
        portField = new JFormattedTextField(maskFormatter);
        portField.setValue(new Integer(22));
        
        userNameField.setEnabled(false);
        passwordField.setEditable(false);
        portField.setEnabled(false);
        terminalTypeField.setEnabled(false);
        updateTimer.setEnabled(false);
        
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        machinePanel.setLayout(new BoxLayout(machinePanel, BoxLayout.X_AXIS));
        detailNamesPanel.setLayout(new BoxLayout(detailNamesPanel,
                BoxLayout.Y_AXIS));
        detailFieldsPanel.setLayout(new BoxLayout(detailFieldsPanel,
                BoxLayout.Y_AXIS));
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.X_AXIS));
        
        machinePanel.add(machineID);
        machinePanel.add(machineIDField);
        
        detailNamesPanel.add(userName);
        detailNamesPanel.add(emptyPanel1);
        detailNamesPanel.add(password);
        detailNamesPanel.add(emptyPanel2);
        detailNamesPanel.add(port);
        detailNamesPanel.add(emptyPanel3);
        detailNamesPanel.add(statusUpdate);
        detailNamesPanel.add(emptyPanel4);
        detailNamesPanel.add(terminalType);
        
        detailFieldsPanel.add(userNameField);
        detailFieldsPanel.add(emptyPanel5);
        detailFieldsPanel.add(passwordField);
        detailFieldsPanel.add(emptyPanel6);
        detailFieldsPanel.add(portField);
        detailFieldsPanel.add(emptyPanel7);
        detailFieldsPanel.add(updateTimer);
        detailFieldsPanel.add(emptyPanel8);
        detailFieldsPanel.add(terminalTypeField);
        
        detailsPanel.add(detailNamesPanel);
        detailsPanel.add(detailFieldsPanel);
        
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Details"));
        
        mainPanel.add(emptyPanel9);
        mainPanel.add(machinePanel);
        mainPanel.add(addDetailsCheckBox);
        mainPanel.add(detailsPanel);
        mainPanel.add(emptyPanel10);
        mainPanel.add(doneButton);
        mainPanel.add(emptyPanel11);
        
        addDetailsCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                addDetailsCheckBox.setEnabled(false);
                userNameField.setEnabled(true);
                passwordField.setEditable(true);
                portField.setEnabled(true);
                terminalTypeField.setEnabled(true);
                updateTimer.setEnabled(true);
                
                userNameField.grabFocus();
            }
        });
        
        doneButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(machineIDField.getText().equals("")) {
                    machineIDField.setText("Field needed");
                    return;
                }
                
                sshContact.savePersistentDetails();
//                ((OperationSetPersistentPresenceSSHImpl)sshContact
//                    .getParentPresenceOperationSet())
//                    .addContactToList(contactGroup, sshContact);
                setVisible(false);
            }        
        });
    }
    
    /**
     * Return the ssh icon
     *
     * @return the ssh icon
     */
    public byte[] getIcon() {
        return Resources.getImage(Resources.SSH_LOGO);
    }
    
    /**
     * Return the main panel
     *
     * @return the main panel
     */
    public Object getForm() {
        return mainPanel;
    }
//
//    public void setContactGroup(ContactGroup contactGroup)
//    {
//        this.contactGroup = contactGroup;
//    }
//
//    public ContactGroup getContactGroup()
//    {
//        return this.contactGroup;
//    }
    
    /**
     * Sets the UserName of the dialog
     *
     * @param userName to be associated
     */
    public void setUserNameField(String userName) {
        this.userNameField.setText(userName);
    }
    
    /**
     * Sets the Password of the dialog
     *
     * @param password to be associated
     */
    public void setPasswordField(String password) {
        this.passwordField.setText(password);
    }
    
    /**
     * Return the hostname
     *
     * @return the hostname
     */
    public String getHostName() {
        return this.machineIDField.getText();
    }
    
    /**
     * Return the username
     *
     * @return the username
     */
    public String getUserName() {
        return this.userNameField.getText();
    }
    
    /**
     * Return the password
     *
     * @return the password in a clear form
     */
    public String getPassword() {
        return this.passwordField.getText();
    }
    
    /**
     * Return the terminal type
     *
     * @return the terminal type
     */
    public String getTerminalType() {
        return this.terminalTypeField.getText();
    }
    
    /**
     * Return the port
     *
     * @return the port value
     */
    public int getPort() {
        return Integer.parseInt(this.portField.getText().trim());
    }
    
    /**
     * Return the update interval
     *
     * @return the update interval
     */
    public int getUpdateInterval() {
        return Integer.parseInt(String.valueOf(this.updateTimer.getValue()));
    }
    
    /**
     * Sets the HostName of the dialog
     *
     * @param hostName to be associated
     */
    public void setHostNameField(String hostName) {
        this.machineIDField.setText(hostName);
    }
    
    /**
     * Sets the Terminal Type of the dialog
     *
     * @param termType to be associated
     */
    public void setTerminalType(String termType) {
        this.terminalTypeField.setText(termType);
    }
    
    /**
     * Sets the Update Interval of the dialog
     *
     * @param interval to be associated
     */
    public void setUpdateInterval(Integer interval) {
        this.updateTimer.setValue(interval);
    }
    
    /**
     * Sets the Port of the dialog
     *
     * @param port to be associated
     */
    public void setPort(String port) {
        this.portField.setText(port);
    }

    public int getIndex()
    {
        return -1;
    }
}
