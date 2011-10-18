/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 * SSHUserInfo.java
 *
 * SSH Suport in SIP Communicator - GSoC' 07 Project
 *
 */

package net.java.sip.communicator.impl.protocol.ssh;

import com.jcraft.jsch.*;
import javax.swing.*;

/**
 * SSHUserInfo passes authentication details to JSch SSH Stack
 *
 * @author Shobhit Jindal
 */
class SSHUserInfo 
        implements UserInfo, 
                   UIKeyboardInteractive
{
    /**
     * The Contact of the remote machine
     */
    private ContactSSH sshContact;
    
    /**
     * Identifier for failure of authentication
     * more explanation below in promptPassword function
     */
    private boolean failedOnce = false;
    
    /**
     * Password field for requesting auth details from user
     */
    JTextField passwordField=(JTextField)new JPasswordField(20);
    
    /**
     * Creates a UserInfo instance
     * 
     * @param sshContact the contact concerned
     */
    SSHUserInfo(ContactSSH sshContact)
    {
        this.sshContact = sshContact;
    }
    
    /**
     * Returns the password of account associated with this contact
     * 
     * @return the password of account associated with this contact
     */
    public String getPassword()
    {
        return sshContact.getPassword();
    }
    
    /**
     * Prompt for accepting the cipher information of the remote server
     * 
     * @param str the string to display
     * 
     * @return the user's answer
     */
    public boolean promptYesNo(String str)
    {
        Object[] options={ "yes", "no" };
        int foo=JOptionPane.showOptionDialog(null,
                str,
                "Warning",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        return foo==0;
    }
    
    /**
     * Passphrase authentication presently not implemented
     * 
     * @return null
     */
    public String getPassphrase()
    { return null; }
    
    /**
     * Passphrase authentication presently not implemented
     * 
     * @return true
     */
    public boolean promptPassphrase(String message)
    { return true; }
    
    /**
     * Asks user to re-enter password information in case of an auth failure
     * 
     * @param message the message to display
     * 
     * @return the user's answer
     */
    public boolean promptPassword(String message)
    {
        /**
         * Auth always fails for the first time for Redhat based machines.
         * Trying again with the same password
         */
        if(!failedOnce)
        {
            failedOnce = true;
            return true;
        }
        
        Object[] ob={passwordField};
        int result=JOptionPane.showConfirmDialog(null, ob, "Auth Failed: " 
                    + message,
                JOptionPane.OK_CANCEL_OPTION);
        
        if(result==JOptionPane.OK_OPTION)
        {
            sshContact.setPassword(passwordField.getText());
            return true;
        }
        
        return false;
    }
    
    /**
     * Shows a message from server
     * 
     * @param message The message to display
     */
    public void showMessage(String message)
    {
        JOptionPane.showMessageDialog(null, message);
    }
    
    /**
     * Keyboard Interactive Auth - not implemented
     */
    public String[] promptKeyboardInteractive(
            String destination,
            String name,
            String instruction,
            String[] prompt,
            boolean[] echo)
    {
        String response[] = new String[prompt.length];
        response[0] = sshContact.getPassword();
        return response;
    }
    
    
}
