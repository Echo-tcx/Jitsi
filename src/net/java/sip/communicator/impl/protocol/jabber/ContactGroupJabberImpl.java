/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.jabber;

import java.util.*;

import org.jivesoftware.smack.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;

/**
 * The Jabber implementation of the ContactGroup interface. Intances of this class
 * (contrary to <tt>RootContactGroupJabberImpl</tt>) may only contain buddies
 * and cannot have sub groups. Note that instances of this class only use the
 * corresponding smack source group for reading their names and only
 * initially fill their <tt>buddies</tt> <tt>java.util.List</tt> with
 * the ContactJabberImpl objects corresponding to those contained in the source
 * group at the moment it is being created. They would, however, never try to
 * sync or update their contents ulteriorly. This would have to be done through
 * the addContact()/removeContact() methods.
 * The content of buddies is created on creating of the group and when the smack
 * source group is changed.
 *
 * @author Damian Minkov
 */
public class ContactGroupJabberImpl
    extends AbstractContactGroupJabberImpl
{
    private static final Logger logger =
        Logger.getLogger(ContactGroupJabberImpl.class);

    private List buddies = new LinkedList();
    private boolean isResolved = false;

    /**
     * The Jabber Group corresponding to this contact group.
     */
    private RosterGroup rosterGroup = null;

    /**
     * a list that would always remain empty. We only use it so that we're able
     * to extract empty iterators
     */
    private List dummyGroupsList = new LinkedList();

    /**
     * A variable that we use as a means of detecting changes in the name
     * of this group.
     */
    private String nameCopy = null;

    private String tempId = null;

    private ServerStoredContactListJabberImpl ssclCallback = null;

    /**
     * Creates an Jabber group using the specified <tt>RosterGroup</tt> as
     * a source. The newly created group will always return the name of the
     * underlying RosterGroup and would thus automatically adapt to changes.
     * It would, however, not receive or try to poll for modifications of the
     * buddies it contains and would therefore have to be updated manually by
     * ServerStoredContactListImpl update will only be done if source group
     * is changed.

     * @param rosterGroup the Jabber Group correspoinding to the group
     * @param groupMembers the group members that we should add to the group.
     * @param ssclCallback a callback to the server stored contact list
     * we're creating.
     * @param isResolved a boolean indicating whether or not the group has been
     * resolved against the server.
     */
    ContactGroupJabberImpl(
                        RosterGroup rosterGroup,
                        Iterator groupMembers,
                        ServerStoredContactListJabberImpl ssclCallback,
                        boolean isResolved)
    {
        this.rosterGroup = rosterGroup;
        this.isResolved = isResolved;
        this.ssclCallback = ssclCallback;

        // init the name copy if its not volatile
        if(rosterGroup != null)
            this.nameCopy = rosterGroup.getName();

        Iterator iter = groupMembers;
        while (iter.hasNext())
        {
            RosterEntry item = (RosterEntry) iter.next();

            addContact( new ContactJabberImpl(item, ssclCallback, true, true) );
        }
    }

    ContactGroupJabberImpl(
                        String id,
                        ServerStoredContactListJabberImpl ssclCallback)
    {
        this.tempId = id;
        this.isResolved = false;
        this.ssclCallback = ssclCallback;
    }


    /**
     * Returns the number of <tt>Contact</tt> members of this
     * <tt>ContactGroup</tt>
     *
     * @return an int indicating the number of <tt>Contact</tt>s,
     *   members of this <tt>ContactGroup</tt>.
     */
    public int countContacts()
    {
        return buddies.size();
    }

    /**
     * Returns a reference to the root group which in Jabber is the parent of
     * any other group since the protocol does not support subgroups.
     * @return a reference to the root group.
     */
    public ContactGroup getParentContactGroup()
    {
        return ssclCallback.getRootGroup();
    }

    /**
     * Adds the specified contact at the specified position.
     * @param contact the new contact to add to this group
     * @param index the position where the new contact should be added.
     */
    void addContact(int index, ContactJabberImpl contact)
    {
        buddies.add(index, contact);
    }

    /**
     * Adds the specified contact to the end of this group.
     * @param contact the new contact to add to this group
     */
    void addContact(ContactJabberImpl contact)
    {
        addContact(countContacts(), contact);
    }


    /**
     * Removes the specified contact from this contact group
     * @param contact the contact to remove.
     */
    void removeContact(ContactJabberImpl contact)
    {
        removeContact(buddies.indexOf(contact));
    }

    /**
     * Removes the contact with the specified index.
     * @param index the index of the cntact to remove
     */
    void removeContact(int index)
    {
        buddies.remove(index);
    }

    /**
     * Removes all buddies in this group and reinsterts them as specified
     * by the <tt>newOrder</tt> param. Contacts not contained in the
     * newOrder list are left at the end of this group.
     *
     * @param newOrder a list containing all contacts in the order that is
     * to be applied.
     *
     */
    void reorderContacts(List newOrder)
    {
        buddies.removeAll(newOrder);
        buddies.addAll(0, newOrder);
    }

    /**
     * Returns an Iterator over all contacts, member of this
     * <tt>ContactGroup</tt>.
     *
     * @return a java.util.Iterator over all contacts inside this
     *   <tt>ContactGroup</tt>. In case the group doesn't contain any
     * memebers it will return an empty iterator.
     */
    public Iterator contacts()
    {
        return buddies.iterator();
    }

    /**
     * Returns the <tt>Contact</tt> with the specified index.
     *
     * @param index the index of the <tt>Contact</tt> to return.
     * @return the <tt>Contact</tt> with the specified index.
     */
    public Contact getContact(int index)
    {
        return (ContactJabberImpl) buddies.get(index);
    }

    /**
     * Returns the <tt>Contact</tt> with the specified address or
     * identifier.
     * @param id the addres or identifier of the <tt>Contact</tt> we are
     * looking for.
     * @return the <tt>Contact</tt> with the specified id or address.
     */
    public Contact getContact(String id)
    {
        return this.findContact(id);
    }

    /**
     * Returns the name of this group.
     * @return a String containing the name of this group.
     */
    public String getGroupName()
    {
        if(isResolved)
            return rosterGroup.getName();
        else
            return tempId;
    }

    /**
     * Determines whether the group may contain subgroups or not.
     *
     * @return always false since only the root group may contain subgroups.
     */
    public boolean canContainSubgroups()
    {
        return false;
    }

    /**
     * Returns the subgroup with the specified index (i.e. always null since
     * this group may not contain subgroups).
     *
     * @param index the index of the <tt>ContactGroup</tt> to retrieve.
     * @return always null
     */
    public ContactGroup getGroup(int index)
    {
        return null;
    }

    /**
     * Returns the subgroup with the specified name.
     * @param groupName the name of the <tt>ContactGroup</tt> to retrieve.
     * @return the <tt>ContactGroup</tt> with the specified index.
     */
    public ContactGroup getGroup(String groupName)
    {
        return null;
    }

    /**
     * Returns an empty iterator. Subgroups may only be present in the root
     * group.
     *
     * @return an empty iterator
     */
    public Iterator subgroups()
    {
        return dummyGroupsList.iterator();
    }

    /**
     * Returns the number of subgroups contained by this group, which is
     * always 0 since sub groups in the protocol may only be contained
     * by the root group - <tt>RootContactGroupImpl</tt>.
     * @return a 0 int.
     */
    public int countSubgroups()
    {
        return 0;
    }

    /**
     * Returns a hash code value for the object, which is actually the hashcode
     * value of the groupname.
     *
     * @return  a hash code value for this ContactGroup.
     */
    public int hashCode()
    {
        return getGroupName().hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this group.
     *
     * @param   obj   the reference object with which to compare.
     * @return  <tt>true</tt> if this object is the same as the obj
     *          argument; <tt>false</tt> otherwise.
     */
    public boolean equals(Object obj)
    {
        if(    obj == this )
            return true;

        if (obj == null
            || !(obj instanceof ContactGroupJabberImpl) )
               return false;

        if(!((ContactGroup)obj).getGroupName().equals(getGroupName()))
            return false;

        //since Jabber does not support having two groups with the same name
        // at this point we could bravely state that the groups are the same
        // and not bother to compare buddies. (gotta check that though)
        return true;
    }

    /**
     * Returns the protocol provider that this group belongs to.
     * @return a regerence to the ProtocolProviderService instance that this
     * ContactGroup belongs to.
     */
    public ProtocolProviderService getProtocolProvider()
    {
        return this.ssclCallback.getParentProvider();
    }

    /**
     * Returns a string representation of this group, in the form
     * JabberGroup.GroupName[size]{ buddy1.toString(), buddy2.toString(), ...}.
     * @return  a String representation of the object.
     */
    public String toString()
    {
        StringBuffer buff = new StringBuffer("JabberGroup.");
        buff.append(getGroupName());
        buff.append(", childContacts="+countContacts()+":[");

        Iterator contacts = contacts();
        while (contacts.hasNext())
        {
            ContactJabberImpl contact = (ContactJabberImpl) contacts.next();
            buff.append(contact.toString());
            if(contacts.hasNext())
                buff.append(", ");
        }
        return buff.append("]").toString();
    }

    /**
     * Returns the contact encapsulating with the spcieified name or
     * null if no such contact was found.
     *
     * @param id the id for the contact we're looking for.
     * @return the <tt>ContactJabberImpl</tt> corresponding to the specified
     * screnname or null if no such contact existed.
     */
    ContactJabberImpl findContact(String id)
    {
        Iterator contacts = contacts();
        while (contacts.hasNext())
        {
            ContactJabberImpl item = (ContactJabberImpl) contacts.next();
            if(item.getAddress().equals(id))
                return item;
        }
        return null;
    }

    /**
     * Sets the name copy field that we use as a means of detecing changes in
     * the group name.
     * @param newName String
     */
    void setNameCopy(String newName)
    {
        this.nameCopy = newName;
    }

    /**
     * Returns the name of the group as it was at the last call of initNameCopy.
     * @return a String containing a copy of the name of this group as it was
     * last time when we called <tt>initNameCopy</tt>.
     */
    String getNameCopy()
    {
        return this.nameCopy;
    }

    /**
     * Determines whether or not this contact group is being stored by the
     * server. Non persistent contact groups exist for the sole purpose of
     * containing non persistent contacts.
     * @return true if the contact group is persistent and false otherwise.
     */
    public boolean isPersistent()
    {
        return true;
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
     * Determines whether or not this contact group has been resolved against
     * the server. Unresolved group are used when initially loading a contact
     * list that has been stored in a local file until the presence operation
     * set has managed to retrieve all the contact list from the server and has
     * properly mapped contacts and groups to their corresponding on-line
     * buddies.
     * @return true if the contact has been resolved (mapped against a buddy)
     * and false otherwise.
     */
    public boolean isResolved()
    {
        return isResolved;
    }

    /**
     * Resolve this contact group against the specified group
     * @param source the server stored group
     */
    void setResolved(RosterGroup source)
    {
        if(isResolved)
            return;

        this.isResolved = true;

        this.rosterGroup = source;

        Iterator iter = source.getEntries().iterator();
        while (iter.hasNext())
        {
            RosterEntry item = (RosterEntry) iter.next();

            ContactJabberImpl contact =
                ssclCallback.findContactById(item.getUser());
            if(contact != null)
            {
                contact.setResolved(item);

                ssclCallback.fireContactResolved(this, contact);
            }
            else
            {
                ContactJabberImpl newContact =
                    new ContactJabberImpl(item, ssclCallback, true, true);
                addContact(newContact);

                ssclCallback.fireContactAdded(this, newContact);
            }
        }
    }

    /**
     * Returns a <tt>String</tt> that uniquely represnets the group. In this we
     * use the name of the group as an identifier. This may cause problems
     * though, in clase the name is changed by some other application between
     * consecutive runs of the sip-communicator.
     *
     * @return a String representing this group in a unique and persistent
     * way.
     */
    public String getUID()
    {
        return getGroupName();
    }

    /**
     * The source group we are encapsulating
     * @return RosterGroup
     */
    RosterGroup getSourceGroup()
    {
        return rosterGroup;
    }

    /**
     * Change the source group
     * change the buddies
     *
     * @param newGroup RosterGroup
     */
    void setSourceGroup(RosterGroup newGroup)
    {
        this.rosterGroup = newGroup;

        // clear current buddies . new entries will be added
        buddies.clear();

        Iterator iter = newGroup.getEntries().iterator();
        while (iter.hasNext())
        {
            RosterEntry item = (RosterEntry) iter.next();

            addContact( new ContactJabberImpl(item, ssclCallback, true, true) );
        }
    }

}
