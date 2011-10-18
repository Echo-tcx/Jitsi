/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.plugin.aimaccregwizz;

/**
 * The <tt>AimAccountRegistration</tt> is used to store all user input data
 * through the <tt>AimAccountRegistrationWizard</tt>.
 *
 * @author Yana Stamcheva
 * @author Lubomir Marinov
 */
public class AimAccountRegistration
{
    private String uin;

    private String password;

    /**
     * The indicator which determines whether the password of this account
     * registration is remembered for later automatic use or it is to be
     * acquired from the user whenever necessary.
     * <p>
     * The default value is <tt>true</tt> because it reflects the default
     * behavior of the application for other protocols. Otherwise, creating an
     * AIM account through the simple account registration form (the
     * multi-protocol entry form which appears at startup when there are no
     * accounts) will result in immediately asking for the password again as
     * soon as the simple account registration form is closed while it will not
     * happen for other types of accounts.
     * </p>
     */
    private boolean rememberPassword = true;

    /**
     * Returns the password of the aim registration account.
     * @return the password of the aim registration account.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Sets the password of the aim registration account.
     * @param password the password of the aim registration account.
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * Returns <tt>true</tt> if password has to remembered, <tt>false</tt>
     * otherwise.
     * 
     * @return <tt>true</tt> if password has to remembered, <tt>false</tt>
     *         otherwise
     */
    public boolean isRememberPassword()
    {
        return rememberPassword;
    }

    /**
     * Sets the rememberPassword value of this aim account registration.
     * 
     * @param rememberPassword
     *            <tt>true</tt> if password has to remembered, <tt>false</tt>
     *            otherwise
     */
    public void setRememberPassword(boolean rememberPassword)
    {
        this.rememberPassword = rememberPassword;
    }

    /**
     * Returns the UIN of the aim registration account.
     * @return the UIN of the aim registration account.
     */
    public String getUin()
    {
        return uin;
    }

    /**
     * Sets the UIN of the aim registration account.
     * @param uin the UIN of the aim registration account.
     */
    public void setUin(String uin)
    {
        this.uin = uin;
    }
}
