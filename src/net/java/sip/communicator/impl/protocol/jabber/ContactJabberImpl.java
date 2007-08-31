/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.jabber;

import org.jivesoftware.smack.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.service.protocol.jabberconstants.*;

/**
 * The Jabber implementation of the service.protocol.Contact interface.
 * @author Damian Minkov
 */
public class ContactJabberImpl
    implements Contact
{
    private static final Logger logger = Logger.getLogger(ContactJabberImpl.class);


    private RosterEntry rosterEntry = null;
    private boolean isLocal = false;
    private byte[] image = null;
    private PresenceStatus status = JabberStatusEnum.OFFLINE;
    private ServerStoredContactListJabberImpl ssclCallback = null;
    private boolean isPersistent = false;
    private boolean isResolved = false;

    private String tempId = null;

    /**
     * Creates an JabberContactImpl
     * @param rosterEntry the RosterEntry object that we will be encapsulating.
     * @param ssclCallback a reference to the ServerStoredContactListImpl
     * instance that created us.
     * @param isPersistent determines whether this contact is persistent or not.
     * @param isResolved specifies whether the contact has been resolved against
     * the server contact list
     */
    ContactJabberImpl(RosterEntry rosterEntry,
                   ServerStoredContactListJabberImpl ssclCallback,
                   boolean isPersistent,
                   boolean isResolved)
    {
        this.rosterEntry = rosterEntry;
        this.isLocal = isLocal;
        this.ssclCallback = ssclCallback;
        this.isPersistent = isPersistent;
        this.isResolved = isResolved;
    }

    ContactJabberImpl(String id,
               ServerStoredContactListJabberImpl ssclCallback,
               boolean isPersistent)
    {
        this.tempId = id;
        this.isLocal = isLocal;
        this.ssclCallback = ssclCallback;
        this.isPersistent = isPersistent;
        this.isResolved = false;
    }


    /**
     * Returns the Jabber Userid of this contact
     * @return the Jabber Userid of this contact
     */
    public String getAddress()
    {
        if(isResolved)
            return rosterEntry.getUser();
        else
            return tempId;
    }

    /**
     * Determines whether or not this Contact instance represents the user used
     * by this protocol provider to connect to the service.
     *
     * @return true if this Contact represents us (the local user) and false
     * otherwise.
     */
    public boolean isLocal()
    {
        return isLocal;
    }

    /**
     *  Returns the image of the contact or null if absent
     */
    public byte[] getImage()
    {
        if(image == null)
            ssclCallback.addContactForImageUpdate(this);

        return image;
    }
    
    /**
     *  Set the image of the contact
     */
    void setImage(byte[] imgBytes)
    {
        this.image = imgBytes;
    }

    /**
     * Returns a hashCode for this contact. The returned hashcode is actually
     * that of the Contact's Address
     * @return the hashcode of this Contact
     */
    public int hashCode()
    {
        return getAddress().hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     *
     * @param   obj   the reference object with which to compare.
     * @return  <tt>true</tt> if this object is the same as the obj
     *          argument; <tt>false</tt> otherwise.
     */
    public boolean equals(Object obj)
    {
        if (obj == null
            || !(obj instanceof ContactJabberImpl)
            || !(((ContactJabberImpl)obj).getAddress().equals(getAddress())
                && ((ContactJabberImpl)obj).getProtocolProvider()
                        == getProtocolProvider()))

            return false;

        return true;
    }

    /**
     * Returns a string representation of this contact, containing most of its
     * representative details.
     *
     * @return  a string representation of this contact.
     */
    public String toString()
    {
        StringBuffer buff =  new StringBuffer("JabberContact[ id=");
        buff.append(getAddress()).
            append(", isPersistent=").append(isPersistent).
            append(", isResolved=").append(isResolved).append("]");

        return buff.toString();
    }

    /**
     * Sets the status that this contact is currently in. The method is to
     * only be called as a result of a status update received from the server.
     *
     * @param status the JabberStatusEnum that this contact is currently in.
     */
    void updatePresenceStatus(PresenceStatus status)
    {
        this.status = status;
    }

    /**
     * Returns the status of the contact as per the last status update we've
     * received for it. Note that this method is not to perform any network
     * operations and will simply return the status received in the last
     * status update message. If you want a reliable way of retrieving someone's
     * status, you should use the <tt>queryContactStatus()</tt> method in
     * <tt>OperationSetPresence</tt>.
     * @return the PresenceStatus that we've received in the last status update
     * pertaining to this contact.
     */
    public PresenceStatus getPresenceStatus()
    {
        return status;
    }

    /**
     * Returns a String that could be used by any user interacting modules for
     * referring to this contact. An alias is not necessarily unique but is
     * often more human readable than an address (or id).
     * @return a String that can be used for referring to this contact when
     * interacting with the user.
     */
    public String getDisplayName()
    {
        if(isResolved)
        {
            String name = rosterEntry.getName();

            if (name == null)
                name = getAddress();

            return name;
        }
        else
            return tempId;
    }

    /**
     * Modify the display name of this contact.
     *
     * @param displayName the new display name for this contact.
     */
    public void setDisplayName(String displayName)
    {
        if (isResolved)
        {
            rosterEntry.setName(displayName);
        }
    }

    /**
     * Returns a reference to the contact group that this contact is currently
     * a child of or null if the underlying protocol does not suppord persistent
     * presence.
     * @return a reference to the contact group that this contact is currently
     * a child of or null if the underlying protocol does not suppord persistent
     * presence.
     */
    public ContactGroup getParentContactGroup()
    {
        return ssclCallback.findContactGroup(this);
    }


    /**
     * Returns a reference to the protocol provider that created the contact.
     * @return a refererence to an instance of the ProtocolProviderService
     */
    public ProtocolProviderService getProtocolProvider()
    {
        return ssclCallback.getParentProvider();
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
     * @return true if the contact is persistent and false otherwise.
     */
    public boolean isPersistent()
    {
        return isPersistent;
    }

    /**
     * Specifies whether this contact is to be considered persistent or not. The
     * method is to be used _only_ when a non-persistent contact has been added
     * to the contact list and its encapsulated VolatileBuddy has been repalced
     * with a standard buddy.
     * @param persistent true if the buddy is to be considered persistent and
     * false for volatile.
     */
    void setPersistent(boolean persistent)
    {
        this.isPersistent = persistent;
    }

    /**
     * Resolve this contact against the given entry
     * @param entry the server stored entry
     */
    void setResolved(RosterEntry entry)
    {
        if(isResolved)
            return;

        this.isResolved = true;
        rosterEntry = entry;
    }

    /**
     * Returns the persistent data
     * @return the persistent data
     */
    public String getPersistentData()
    {
        return null;
    }

    /**
     * Determines whether or not this contact has been resolved against the
     * server. Unresolved contacts are used when initially loading a contact
     * list that has been stored in a local file until the presence operation
     * set has managed to retrieve all the contact list from the server and has
     * properly mapped contacts to their on-line buddies.
     * @return true if the contact has been resolved (mapped against a buddy)
     * and false otherwise.
     */
    public boolean isResolved()
    {
        return isResolved;
    }

    public void setPersistentData(String persistentData)
    {
    }

    /**
     * Get source entry
     * @return RosterEntry
     */
    RosterEntry getSourceEntry()
    {
        return rosterEntry;
    }
}
