/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.sip;

import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;

import javax.sip.*;
import java.util.*;

/**
 * A simple, straightforward implementation of a SIP Contact. Since
 * the SIP protocol is not a real one, we simply store all contact details
 * in class fields. You should know that when implementing a real protocol,
 * the contact implementation would rather encapsulate contact objects from
 * the protocol stack and group property values should be returned after
 * consulting the encapsulated object.
 *
 * @author Emil Ivov
 * @author Benoit Pradelle
 */
public class ContactSipImpl
    implements Contact
{
    private static final Logger logger
        = Logger.getLogger(ContactSipImpl.class);

    /**
     * The id of the contact.
     */
    private String contactID = null;

    /**
     * The provider that created us.
     */
    private ProtocolProviderServiceSipImpl parentProvider = null;

    /**
     * The group that belong to.
     */
    private ContactGroupSipImpl parentGroup = null;

    /**
     * The presence status of the contact.
     */
    private PresenceStatus presenceStatus;

    /**
     * Determines whether this contact is persistent, i.e. member of the contact
     * list or whether it is here only temporarily.
     */
    private boolean isPersistent = true;

    /**
     * Determines whether the contact has been resolved (i.e. we have a
     * confirmation that it is still on the server contact list).
     */
    private boolean isResolved = false;
    
    /**
     * Determines whether this contact can be resolved or if he will be
     * never resolved (for example if he doesn't support SIMPLE)
     */
    private boolean isResolvable = true;
    
    /**
     * Stores the dialog used for receiving the contact status
     */
    private Dialog clientDialog = null;
    
    /**
     * Stores the dialog used for communicate our status to this contact
     */
    private Dialog serverDialog = null;
    
    /**
     * The task which will send a final NOTIFY when a timeout occur in the
     * subscription of this contact. If the contact isn't subscribed at us,
     * this will remain null.
     */
    private TimerTask timeoutTask = null;
    
    /**
     * The task which will refresh the subscription we've made with this
     * contact. If we didn't subscribe to this contact, it will remain null.
     */
    private TimerTask resfreshTask = null;
    
    /**
     * Creates an instance of a meta contact with the specified string used
     * as a name and identifier.
     *
     * @param id the identifier of this contact (also used as a name).
     * @param parentProvider the provider that created us.
     */
    public ContactSipImpl(
                String id,
                ProtocolProviderServiceSipImpl parentProvider)
    {
        this.contactID = id;
        this.parentProvider = parentProvider;
        
        this.presenceStatus = parentProvider.getSipStatusEnum()
            .getStatus(SipStatusEnum.UNKNOWN);
    }

    /**
     * This method is only called when the contact is added to a new
     * <tt>ContactGroupSipImpl</tt> by the
     * <tt>ContactGroupSipImpl</tt> itself.
     *
     * @param newParentGroup the <tt>ContactGroupSipImpl</tt> that is now
     * parent of this <tt>ContactSipImpl</tt>
     */
    void setParentGroup(ContactGroupSipImpl newParentGroup)
    {
        this.parentGroup = newParentGroup;
    }

    /**
     * Returns a String that can be used for identifying the contact.
     *
     * @return a String id representing and uniquely identifying the contact.
     */
    public String getAddress()
    {
        return contactID;
    }

    /**
     * Returns a String that could be used by any user interacting modules
     * for referring to this contact.
     *
     * @return a String that can be used for referring to this contact when
     *   interacting with the user.
     */
    public String getDisplayName()
    {
        return contactID;
    }

    /**
     * Returns a byte array containing an image (most often a photo or an
     * avatar) that the contact uses as a representation.
     *
     * @return byte[] an image representing the contact.
     */
    public byte[] getImage()
    {
        return null;
    }

    /**
     * Returns the status of the contact.
     *
     * @return always IcqStatusEnum.ONLINE.
     */
    public PresenceStatus getPresenceStatus()
    {
        return this.presenceStatus;
    }

    /**
     * Sets <tt>sipPresenceStatus</tt> as the PresenceStatus that this
     * contact is currently in.
     * @param sipPresenceStatus the <tt>SipPresenceStatus</tt>
     * currently valid for this contact.
     */
    public void setPresenceStatus(PresenceStatus sipPresenceStatus)
    {
        this.presenceStatus = sipPresenceStatus;
    }

    /**
     * Returns a reference to the protocol provider that created the contact.
     *
     * @return a refererence to an instance of the ProtocolProviderService
     */
    public ProtocolProviderService getProtocolProvider()
    {
        return parentProvider;
    }

    /**
     * Determines whether or not this contact represents our own identity.
     *
     * @return true in case this is a contact that represents ourselves and
     *   false otherwise.
     */
    public boolean isLocal()
    {
        return false;
    }

    /**
     * Returns the group that contains this contact.
     * @return a reference to the <tt>ContactGroupSipImpl</tt> that
     * contains this contact.
     */
    public ContactGroup getParentContactGroup()
    {
        return this.parentGroup;
    }

    /**
     * Returns a string representation of this contact, containing most of its
     * representative details.
     *
     * @return  a string representation of this contact.
     */
    public String toString()
    {
        StringBuffer buff
            = new StringBuffer("ContactSipImpl[ DisplayName=")
                .append(getDisplayName()).append("]");

        return buff.toString();
    }

    /**
     * Determines whether or not this contact is being stored by the server.
     * Non persistent contacts are common in the case of simple, non-persistent
     * presence operation sets. They could however also be seen in persistent
     * presence operation sets when for example we have received an event
     * from someone not on our contact list. Non persistent contacts are
     * volatile even when coming from a persistent presence op. set. They would
     * only exist until the application is closed and will not be there next
     * time it is loaded.
     *
     * @return true if the contact is persistent and false otherwise.
     */
    public boolean isPersistent()
    {
        return isPersistent;
    }

    /**
     * Specifies whether or not this contact is being stored by the server.
     * Non persistent contacts are common in the case of simple, non-persistent
     * presence operation sets. They could however also be seen in persistent
     * presence operation sets when for example we have received an event
     * from someone not on our contact list. Non persistent contacts are
     * volatile even when coming from a persistent presence op. set. They would
     * only exist until the application is closed and will not be there next
     * time it is loaded.
     *
     * @param isPersistent true if the contact is persistent and false
     * otherwise.
     */
    public void setPersistent(boolean isPersistent)
    {
        this.isPersistent = isPersistent;
    }


    /**
     * Returns null as no persistent data is required and the contact address is
     * sufficient for restoring the contact.
     * <p>
     * @return null as no such data is needed.
     */
    public String getPersistentData()
    {
        return null;
    }

    /**
     * Returns the current timeout task associated with this contact.
     * 
     * @return the current timeout task of this contact
     */
    public TimerTask getTimeoutTask() {
        return this.timeoutTask;
    }
    
    /**
     * Sets the timeout task associated with this contact
     * 
     * @param timeoutTask The timeout task to set
     */
    public void setTimeoutTask(TimerTask timeoutTask) {
        this.timeoutTask = timeoutTask;
    }

    /**
     * Returns the current refresh task associated with this contact.
     * 
     * @return The resfresh task
     */
    public TimerTask getResfreshTask() {
        return this.resfreshTask;
    }
    
    /**
     * Changes the current refresh task of this contact.
     * 
     * @param resfreshTask The resfresh task to set
     */
    public void setResfreshTask(TimerTask resfreshTask) {
        this.resfreshTask = resfreshTask;
    }

    /**
     * Sets the client dialog associated with this contact.
     * The client dialog is the dialog we use to retrieve the presence
     * state of this contact.
     * 
     * @param clientDialog the new clientDialog to use
     */
    public void setClientDialog(Dialog clientDialog) {
        this.clientDialog = clientDialog;
    }
    
    /**
     * Returns the client dialog associated with this contact.
     * The client dialog is the dialog we use to retrieve the presence
     * state of this contact.
     * 
     * @return the clientDialog associated with the contact
     */
    public Dialog getClientDialog() {
        return this.clientDialog;
    }
    
    /**
     * Sets the server dialog associated with this contact.
     * The server dialog is the dialog we use to send our presence status
     * to this contact.
     * 
     * @param serverDialog the new clientDialog to use
     */
    public void setServerDialog(Dialog serverDialog) {
        this.serverDialog = serverDialog;
    }
    
    /**
     * Returns the server dialog associated with this contact.
     * The server dialog is the dialog we use to send our presence status
     * to this contact.
     * 
     * @return the clientDialog associated with the contact
     */
    public Dialog getServerDialog() {
        return this.serverDialog;
    }

    /**
     * Determines whether or not this contact has been resolved against the
     * server. Unresolved contacts are used when initially loading a contact
     * list that has been stored in a local file until the presence operation
     * set has managed to retrieve all the contact list from the server and has
     * properly mapped contacts to their on-line buddies.
     *
     * @return true if the contact has been resolved (mapped against a buddy)
     * and false otherwise.
     */
    public boolean isResolved()
    {
        return this.isResolved;
    }

    /**
     * Makes the contact resolved or unresolved.
     *
     * @param resolved  true to make the contact resolved; false to
     *                  make it unresolved
     */
    public void setResolved(boolean resolved)
    {
        this.isResolved = resolved;
    }
    
    /**
     * Determines whether or not this contact can be resolved against the
     * server.
     *
     * @return true if the contact can be resolved (mapped against a buddy)
     * and false otherwise.
     */
    public boolean isResolvable()
    {
        return this.isResolvable;
    }

    /**
     * Makes the contact resolvable or unresolvable.
     *
     * @param resolvable  true to make the contact resolvable; false to
     * make it unresolvable
     */
    public void setResolvable(boolean resolvable)
    {
        this.isResolvable = resolvable;
    }

    /**
     * Indicates whether some other object is "equal to" this one which in terms
     * of contacts translates to having equal ids. The resolved status of the
     * contacts deliberately ignored so that contacts would be declared equal
     * even if it differs.
     * <p>
     * @param   obj   the reference object with which to compare.
     * @return  <code>true</code> if this contact has the same id as that of the
     * <code>obj</code> argument.
     */
    public boolean equals(Object obj)
    {
        if (obj == null
            || ! (obj instanceof ContactSipImpl))
            return false;

        ContactSipImpl sipContact = (ContactSipImpl) obj;

        return this.getAddress().equals(sipContact.getAddress());
    }


    /**
     * Returns the presence operation set that this contact belongs
     * to.
     *
     * @return the <tt>OperationSetPresenceSipImpl</tt> that
     * this contact belongs to.
     */
    public OperationSetPresenceSipImpl getParentPresenceOperationSet()
    {
        return (OperationSetPresenceSipImpl) parentProvider
            .getOperationSet(OperationSetPresence.class);
    }

    /**
     * Return the current status message of this contact.
     * 
     * @return null as the protocol has currently no support of status messages
     */
    public String getStatusMessage()
    {
        return null;
    }
}
