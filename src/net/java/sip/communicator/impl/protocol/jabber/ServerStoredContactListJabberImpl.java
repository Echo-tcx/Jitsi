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
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;

/**
 * This class encapsulates the Roster class. Once created, it will
 * register itself as a listener to the encapsulated Roster and modify it's
 * local copy of Contacts and ContactGroups every time an event is generated
 * by the underlying framework. The class would also generate
 * corresponding sip-communicator events to all events coming from smack.
 *
 * @author Damian Minkov
 */
public class ServerStoredContactListJabberImpl
{
    private static final Logger logger =
        Logger.getLogger(ServerStoredContactListJabberImpl.class);

    private static final String VOLATILE_GROUP_NAME = "NotInContactList";
    /**
     * The jabber list that we encapsulate
     */
    private Roster roster = null;

    /**
     * The root contagroup. The container for all jabber buddies and groups.
     */
    private RootContactGroupJabberImpl rootGroup = new RootContactGroupJabberImpl();

    /**
     * The operation set that created us and that we could use when dispatching
     * subscription events.
     */
    private OperationSetPersistentPresenceJabberImpl parentOperationSet = null;

    /**
     * The provider that is on top of us.
     */
    private ProtocolProviderServiceJabberImpl jabberProvider = null;

    /**
     * Listeners that would receive event notifications for changes in group
     * names or other properties, removal or creation of groups.
     */
    private Vector serverStoredGroupListeners = new Vector();

    /**
     * Creates a ServerStoredContactList wrapper for the specified BuddyList.
     *
     * @param parentOperationSet the operation set that created us and that
     * we could use for dispatching subscription events
     * @param provider the provider that has instantiated us.
     */
    ServerStoredContactListJabberImpl(
        OperationSetPersistentPresenceJabberImpl parentOperationSet,
        ProtocolProviderServiceJabberImpl        provider)
    {
        //We need to init these as early as possible to ensure that the provider
        //and the operationsset would not be null in the incoming events.
        this.parentOperationSet = parentOperationSet;

        this.jabberProvider = provider;
    }

    /**
     * Returns the root group of the contact list.
     *
     * @return the root ContactGroup for the ContactList
     */
    public ContactGroup getRootGroup()
    {
        return rootGroup;
    }

    /**
     * Registers the specified group listener so that it would receive events
     * on group modification/creation/destruction.
     * @param l the ServerStoredGroupListener to register for group events
     */
    void addGroupListener(ServerStoredGroupListener l)
    {
        synchronized(serverStoredGroupListeners){
            this.serverStoredGroupListeners.add(l);
        }
    }

    /**
     * Removes the specified group listener so that it won't receive further
     * events on group modification/creation/destruction.
     * @param l the ServerStoredGroupListener to unregister
     */
    void removeGroupListener(ServerStoredGroupListener l)
    {
        synchronized(serverStoredGroupListeners){
            this.serverStoredGroupListeners.remove(l);
        }
    }

    /**
     * Creates the corresponding event and notifies all
     * <tt>ServerStoredGroupListener</tt>s that the source group has been
     * removed, changed, renamed or whatever happened to it.
     * @param group the ContactGroup that has been created/modified/removed
     * @param eventID the id of the event to generate.
     */
    private void fireGroupEvent(ContactGroupJabberImpl group, int eventID)
    {
        //bail out if no one's listening
        if(parentOperationSet == null){
            logger.debug("No presence op. set available. Bailing out.");
            return;
        }

        ServerStoredGroupEvent evt = new ServerStoredGroupEvent(
                  group
                , eventID
                , parentOperationSet.getServerStoredContactListRoot()
                , jabberProvider
                , parentOperationSet);

        logger.trace("Will dispatch the following grp event: " + evt);

        synchronized (serverStoredGroupListeners){
            Iterator listeners = this.serverStoredGroupListeners.iterator();

            while (listeners.hasNext())
            {
                ServerStoredGroupListener l
                    = (ServerStoredGroupListener) listeners.next();
                if (eventID == ServerStoredGroupEvent.GROUP_REMOVED_EVENT)
                    l.groupRemoved(evt);
                else if (eventID == ServerStoredGroupEvent.GROUP_RENAMED_EVENT)
                    l.groupNameChanged(evt);
                else if (eventID == ServerStoredGroupEvent.GROUP_CREATED_EVENT)
                    l.groupCreated(evt);
                else if (eventID == ServerStoredGroupEvent.GROUP_RESOLVED_EVENT)
                    l.groupResolved(evt);
            }
        }
    }

    /**
     * Make the parent persistent presence operation set dispatch a contact
     * removed event.
     * @param parentGroup the group where that the removed contact belonged to.
     * @param contact the contact that was removed.
     */
    private void fireContactRemoved( ContactGroup parentGroup,
                                     ContactJabberImpl contact)
    {
        //bail out if no one's listening
        if(parentOperationSet == null){
            logger.debug("No presence op. set available. Bailing out.");
            return;
        }

        //dispatch
        parentOperationSet.fireSubscriptionEvent(
            SubscriptionEvent.SUBSCRIPTION_REMOVED, contact, parentGroup);
    }

    /**
     * Make the parent persistent presence operation set dispatch a subscription
     * moved event.
     * @param oldParentGroup the group where the source contact was located
     * before being moved
     * @param newParentGroup the group that the source contact is currently in.
     * @param contact the contact that was added
     */
    private void fireContactMoved( ContactGroup oldParentGroup,
                                   ContactGroupJabberImpl newParentGroup,
                                   ContactJabberImpl contact)
    {
        //bail out if no one's listening
        if(parentOperationSet == null){
            logger.debug("No presence op. set available. Bailing out.");
            return;
        }

        //dispatch
        parentOperationSet.fireSubscriptionMovedEvent(
            contact, oldParentGroup, newParentGroup);
    }

    /**
     * Retrns a reference to the provider that created us.
     * @return a reference to a ProtocolProviderServiceImpl instance.
     */
    ProtocolProviderServiceJabberImpl getParentProvider()
    {
        return jabberProvider;
    }

    /**
     * Returns the ConntactGroup with the specified name or null if no such
     * group was found.
     * <p>
     * @param name the name of the group we're looking for.
     * @return a reference to the ContactGroupJabberImpl instance we're looking for
     * or null if no such group was found.
     */
    public ContactGroupJabberImpl findContactGroup(String name)
    {
        Iterator contactGroups = rootGroup.subgroups();

        while(contactGroups.hasNext())
        {
            ContactGroupJabberImpl contactGroup
                = (ContactGroupJabberImpl) contactGroups.next();

            if (contactGroup.getGroupName().equals(name))
                return contactGroup;

        }

        return null;
    }

    private ContactGroupJabberImpl findContactGroupByNameCopy(String name)
    {
        Iterator contactGroups = rootGroup.subgroups();

        while(contactGroups.hasNext())
        {
            ContactGroupJabberImpl contactGroup
                = (ContactGroupJabberImpl) contactGroups.next();

            if (contactGroup.getNameCopy().equals(name))
                return contactGroup;

        }
        return null;
    }

    /**
     * Returns the Contact with the specified id or null if
     * no such id was found.
     *
     * @param id the id of the contact to find.
     * @return the <tt>Contact</tt> carrying the specified
     * <tt>screenName</tt> or <tt>null</tt> if no such contact exits.
     */
    public ContactJabberImpl findContactById(String id)
    {
        Iterator contactGroups = rootGroup.subgroups();
        ContactJabberImpl result = null;

        while(contactGroups.hasNext())
        {
            ContactGroupJabberImpl contactGroup
                = (ContactGroupJabberImpl) contactGroups.next();

            result = contactGroup.findContact(id);

            if (result != null)
                return result;

        }

        Iterator rootContacts = rootGroup.contacts();
        while (rootContacts.hasNext())
        {
            ContactJabberImpl item = (ContactJabberImpl) rootContacts.next();

            if(item.getAddress().equals(id))
                return item;
        }

        return null;
    }

    /**
     * Returns the ContactGroup containing the specified contact or null
     * if no such group or contact exist.
     *
     * @param child the contact whose parent group we're looking for.
     * @return the <tt>ContactGroup</tt> containing the specified
     * <tt>contact</tt> or <tt>null</tt> if no such groupo or contact
     * exist.
     */
    public ContactGroup findContactGroup(ContactJabberImpl child)
    {
        Iterator contactGroups = rootGroup.subgroups();

        while(contactGroups.hasNext())
        {
            ContactGroupJabberImpl contactGroup
                = (ContactGroupJabberImpl) contactGroups.next();

            if( contactGroup.findContact(child.getAddress())!= null)
                return contactGroup;
        }

        Iterator contacts = rootGroup.contacts();

        while(contacts.hasNext())
        {
            ContactJabberImpl contact = (ContactJabberImpl) contacts.next();

            if( contact.equals(child))
                return rootGroup;
        }

        return null;
    }

    /**
     * Adds a new contact with the specified screenname to the list under a
     * default location.
     * @param id the id of the contact to add.
     * @throws OperationFailedException
     */
    public void addContact(String id)
        throws OperationFailedException
    {
        addContact(null, id);
    }

    /**
     * Adds a new contact with the specified screenname to the list under the
     * specified group.
     * @param id the id of the contact to add.
     * @param parent the group under which we want the new contact placed.
     * @throws OperationFailedException if the contact already exist
     */
    public void addContact(ContactGroup parent, String id)
        throws OperationFailedException
    {
        logger.trace("Adding contact " + id + " to parent=" + parent);

        //if the contact is already in the contact list and is not volatile,
        //then only broadcast an event
        final ContactJabberImpl existingContact = findContactById(id);

        if( existingContact != null
            && existingContact.isPersistent() )
        {
            logger.debug("Contact " + id + " already exists.");
            throw new OperationFailedException(
                "Contact " + id + " already exists.",
                OperationFailedException.SUBSCRIPTION_ALREADY_EXISTS);
        }

        try
        {
            String[] parentNames = null;

            if(parent != null)
                parentNames = new String[]{parent.getGroupName()};

            this.roster.createEntry(id, id, parentNames);
        }
        catch (XMPPException ex)
        {
            logger.error("Error adding new jabber entry", ex);
        }
    }

    /**
     * Creates a non persistent contact for the specified address. This would
     * also create (if necessary) a group for volatile contacts that would not
     * be added to the server stored contact list. This method would have no
     * effect on the server stored contact list.
     * @param id the address of the contact to create.
     * @return the newly created volatile <tt>ContactImpl</tt>
     */
    ContactJabberImpl createVolatileContact(String id)
    {
        VolatileContactJabberImpl newVolatileContact
            = new VolatileContactJabberImpl(id, this);

        //Check whether a volatile group already exists and if not create
        //one
        ContactGroupJabberImpl theVolatileGroup = getNonPersistentGroup();

        //if the parent group is null then add necessary create the group
        if (theVolatileGroup == null)
        {
            theVolatileGroup = new VolatileContactGroupJabberImpl(
                VOLATILE_GROUP_NAME, this);

            theVolatileGroup.addContact(newVolatileContact);

            this.rootGroup.addSubGroup(theVolatileGroup);

            fireGroupEvent(theVolatileGroup
                           , ServerStoredGroupEvent.GROUP_CREATED_EVENT);
        }
        else
        {
            theVolatileGroup.addContact(newVolatileContact);

            fireContactAdded(theVolatileGroup, newVolatileContact);
        }

        return newVolatileContact;
    }


    /**
     * Creates a non resolved contact for the specified address and inside the
     * specified group. The newly created contact would be added to the local
     * contact list as a standard contact but when an event is received from the
     * server concerning this contact, then it will be reused and only its
     * isResolved field would be updated instead of creating the whole contact
     * again.
     *
     * @param parentGroup the group where the unersolved contact is to be
     * created
     * @param id the Address of the contact to create.
     * @return the newly created unresolved <tt>ContactImpl</tt>
     */
    ContactJabberImpl createUnresolvedContact(ContactGroup parentGroup,
                                              String  id)
    {
        ContactJabberImpl newUnresolvedContact
            = new ContactJabberImpl(id, this, false);

        if(parentGroup instanceof ContactGroupJabberImpl)
            ((ContactGroupJabberImpl)parentGroup).
                addContact(newUnresolvedContact);
        else if(parentGroup instanceof RootContactGroupJabberImpl)
            ((RootContactGroupJabberImpl)parentGroup).
                addContact(newUnresolvedContact);

        fireContactAdded(parentGroup, newUnresolvedContact);

        return newUnresolvedContact;
    }

    /**
     * Creates a non resolved contact group for the specified name. The newly
     * created group would be added to the local contact list as any other group
     * but when an event is received from the server concerning this group, then
     * it will be reused and only its isResolved field would be updated instead
     * of creating the whole group again.
     * <p>
     * @param groupName the name of the group to create.
     * @return the newly created unresolved <tt>ContactGroupImpl</tt>
     */
    ContactGroupJabberImpl createUnresolvedContactGroup(String groupName)
    {
        ContactGroupJabberImpl newUnresolvedGroup =
            new ContactGroupJabberImpl(groupName, this);

        this.rootGroup.addSubGroup(newUnresolvedGroup);

        fireGroupEvent(newUnresolvedGroup
                        , ServerStoredGroupEvent.GROUP_CREATED_EVENT);

        return newUnresolvedGroup;
    }

    /**
     * Creates the specified group on the server stored contact list.
     * @param groupName a String containing the name of the new group.
     */
    public void createGroup(String groupName)
    {
        logger.trace("Creating group: " + groupName);

        RosterGroup newRosterGroup = roster.createGroup(groupName);

        ContactGroupJabberImpl newGroup =
            new ContactGroupJabberImpl(newRosterGroup,
                                       new Vector().iterator(),
                                       this,
                                       true);
        rootGroup.addSubGroup(newGroup);

        fireGroupEvent(newGroup, ServerStoredGroupEvent.GROUP_CREATED_EVENT);

        logger.trace("Group " +groupName+ " created.");
    }

    /**
     * Removes the specified group from the buddy list.
     * @param groupToRemove the group that we'd like removed.
     */
    public void removeGroup(ContactGroupJabberImpl groupToRemove)
    {
        try
        {
            // first copy the item that will be removed
            // when iterating over group contacts and removing them
            // concurrent exception occures
            Vector localCopy = new Vector();
            Iterator iter = groupToRemove.contacts();

            while (iter.hasNext())
            {
                localCopy.add(iter.next());
            }

            iter = localCopy.iterator();
            while (iter.hasNext())
            {
                ContactJabberImpl item = (ContactJabberImpl) iter.next();
                roster.removeEntry(item.getSourceEntry());
            }
        }
        catch (XMPPException ex)
        {
            logger.error("Error removing group", ex);
        }
    }

    void removeContact(ContactJabberImpl contactToRemove)
    {
        try
        {
            this.roster.removeEntry(contactToRemove.getSourceEntry());
        }
        catch (XMPPException ex)
        {
            logger.error("Error removing contact", ex);
        }
    }


    /**
     * Renames the specified group according to the specified new name..
     * @param groupToRename the group that we'd like removed.
     * @param newName the new name of the group
     */
    public void renameGroup(ContactGroupJabberImpl groupToRename, String newName)
    {
        groupToRename.getSourceGroup().setName(newName);
        groupToRename.setNameCopy(newName);
    }

    /**
     * Moves the specified <tt>contact</tt> to the group indicated by
     * <tt>newParent</tt>.
     * @param contact the contact that we'd like moved under the new group.
     * @param newParent the group where we'd like the parent placed.
     */
    public void moveContact(ContactJabberImpl contact,
                            ContactGroupJabberImpl newParent)
    {
        List contactsToMove = new ArrayList();
        contactsToMove.add(contact);

        newParent.addContact(contact);

        try
        {
            // will create the entry with the new group so it can be removed
            // from other groups if any
            roster.createEntry(contact.getSourceEntry().getUser(),
                               contact.getDisplayName(),
                               new String[]{newParent.getGroupName()});
        }
        catch (XMPPException ex)
        {
            logger.error("Cannot move contact! ", ex);
        }
    }

    /**
     * Sets a reference to the currently active and valid instance of
     * roster that this list is to use for retrieving
     * server stored information
     */
    void init()
    {
        this.rootGroup.setOwnerProvider(jabberProvider);

        this.roster = jabberProvider.getConnection().getRoster();

        initRoster();

        this.roster.addRosterListener(new ChangeListener());
    }

    private void initRoster()
    {
        // first if unfiled exntries will move them in a group
        if(roster.getUnfiledEntryCount() > 0)
        {
            Iterator iter = roster.getUnfiledEntries();
            while (iter.hasNext())
            {
                RosterEntry item = (RosterEntry) iter.next();
                ContactJabberImpl contact =
                    findContactById(item.getUser());

                if(contact == null)
                {
                    contact = new ContactJabberImpl(item, this, true, true);
                    rootGroup.addContact(contact);

                    fireContactAdded(rootGroup, contact);
                }
                else
                    contact.setResolved(item);
            }
        }


        // fill in root group
        Iterator iter = roster.getGroups();
        while (iter.hasNext())
        {
            RosterGroup item = (RosterGroup) iter.next();

            ContactGroupJabberImpl group =
                findContactGroup(item.getName());

            if(group == null)
            {
                ContactGroupJabberImpl newGroup =
                new ContactGroupJabberImpl(item, item.getEntries(), this, true);

                rootGroup.addSubGroup(newGroup);

                //tell listeners about the added group
                fireGroupEvent(newGroup
                               , ServerStoredGroupEvent.GROUP_CREATED_EVENT);
            }
            else
            {
                group.setResolved(item);

                //fire an event saying that the group has been resolved
                fireGroupEvent(group
                               , ServerStoredGroupEvent.GROUP_RESOLVED_EVENT);

                /** @todo  if something to delete . delete it */
            }
        }
    }

    /**
     * Returns the volatile group
     *
     * @return ContactGroupJabberImpl
     */
    private ContactGroupJabberImpl getNonPersistentGroup()
    {
        for (int i = 0; i < getRootGroup().countSubgroups(); i++)
        {
            ContactGroupJabberImpl gr =
                (ContactGroupJabberImpl)getRootGroup().getGroup(i);

            if(!gr.isPersistent())
                return gr;
        }

        return null;
    }

    /**
     * Make the parent persistent presence operation set dispatch a contact
     * added event.
     * @param parentGroup the group where the new contact was added
     * @param contact the contact that was added
     */
    void fireContactAdded( ContactGroup parentGroup,
                                   ContactJabberImpl contact)
    {
        //bail out if no one's listening
        if(parentOperationSet == null){
            logger.debug("No presence op. set available. Bailing out.");
            return;
        }

        //dispatch
        parentOperationSet.fireSubscriptionEvent(
            SubscriptionEvent.SUBSCRIPTION_CREATED, contact, parentGroup);
    }

    /**
     * Make the parent persistent presence operation set dispatch a contact
     * resolved event.
     * @param parentGroup the group that the resolved contact belongs to.
     * @param contact the contact that was resolved
     */
    void fireContactResolved( ContactGroupJabberImpl parentGroup,
                                      ContactJabberImpl contact)
    {
        //bail out if no one's listening
        if(parentOperationSet == null){
            logger.debug("No presence op. set available. Bailing out.");
            return;
        }

        //dispatch
        parentOperationSet.fireSubscriptionEvent(
            SubscriptionEvent.SUBSCRIPTION_RESOLVED, contact, parentGroup);
    }

    private class ChangeListener
        implements RosterListener
    {
        public void entriesAdded(Collection addresses)
        {
            logger.trace("entriesAdded " + addresses);

            Iterator iter = addresses.iterator();
            while (iter.hasNext())
            {
                String id = (String) iter.next();

                RosterEntry entry = roster.getEntry(id);

                ContactJabberImpl contact =
                    new ContactJabberImpl(roster.getEntry(id),
                                          ServerStoredContactListJabberImpl.this,
                                          true,
                                          true);

                boolean isUnfiledEntry = true;
                Iterator groupIter = entry.getGroups();
                while (groupIter.hasNext())
                {
                    RosterGroup group = (RosterGroup) groupIter.next();

                    ContactGroupJabberImpl parentGroup =
                        findContactGroup(group.getName());
                    if(parentGroup != null)
                        parentGroup.addContact(contact);

                    isUnfiledEntry = false;
                }

                ContactGroup parentGroup = findContactGroup(contact);

                // fire the event if and only we have parent group
                if(parentGroup != null && !isUnfiledEntry)
                    fireContactAdded(findContactGroup(contact), contact);

                if(parentGroup == null && isUnfiledEntry)
                {
                    rootGroup.addContact(contact);
                    fireContactAdded(rootGroup, contact);
                }
            }
        }

        public void entriesUpdated(Collection addresses)
        {
            logger.trace("entriesUpdated  " + addresses);

            // will search for group renamed

            Iterator iter = addresses.iterator();
            while (iter.hasNext())
            {
                String contactID = (String) iter.next();
                RosterEntry entry = roster.getEntry(contactID);

                Iterator iter1 = entry.getGroups();
                while (iter1.hasNext())
                {
                    RosterGroup gr = (RosterGroup) iter1.next();

                    if(findContactGroup(gr.getName()) == null)
                    {
                        // such group does not exist. so it must be
                        // renamed one
                        ContactGroupJabberImpl group =
                            findContactGroupByNameCopy(gr.getName());
                        if(group != null)
                        {
                            // just change the source entry
                            group.setSourceGroup(gr);

                            fireGroupEvent(group,
                                           ServerStoredGroupEvent.GROUP_RENAMED_EVENT);
                        }
                        else
                        {
                            // strange ???
                        }
                    }
                    else
                    {
                        // the group is found the contact may be moved from one group
                        // to another
                        ContactJabberImpl contact = findContactById(contactID);
                        ContactGroup contactGroup =
                            contact.getParentContactGroup();

                        if(!gr.getName().equals(contactGroup.getGroupName()))
                        {
                            // the contact is moved to onether group
                            // first remove it from the original one
                            if(contactGroup instanceof ContactGroupJabberImpl)
                                ((ContactGroupJabberImpl)contactGroup).
                                    removeContact(contact);
                            else if(contactGroup instanceof RootContactGroupJabberImpl)
                                ((RootContactGroupJabberImpl)contactGroup).
                                    removeContact(contact);

                            // the add it to the new one
                            ContactGroupJabberImpl newParentGroup =
                                findContactGroup(gr.getName());

                            newParentGroup.addContact(contact);

                            fireContactMoved(contactGroup,
                                             newParentGroup,
                                             contact);
                        }
                    }
                }
            }
        }

        public void entriesDeleted(Collection addresses)
        {
            Iterator iter = addresses.iterator();
            while (iter.hasNext())
            {
                String address = (String) iter.next();
                logger.trace("entry deleted " + address);

                ContactJabberImpl contact =
                    findContactById(address);

                if(contact == null)
                    continue;

                ContactGroup group = findContactGroup(contact);

                if(group == null)
                    continue;

                if(group instanceof ContactGroupJabberImpl)
                {
                    ContactGroupJabberImpl groupImpl = (ContactGroupJabberImpl)group;

                    // remove the contact from parrent group
                    groupImpl.removeContact(contact);

                    // if the group is empty remove it from
                    // root group . This group will be removed from server if empty
                    if (groupImpl.countContacts() == 0)
                    {
                        rootGroup.removeSubGroup(groupImpl);
                        fireGroupEvent(groupImpl,
                                       ServerStoredGroupEvent.GROUP_REMOVED_EVENT);
                    }

                    fireContactRemoved(groupImpl, contact);
                }
                else if(group instanceof RootContactGroupJabberImpl)
                {
                    rootGroup.removeContact(contact);

                    fireContactRemoved(rootGroup, contact);
                }

            }
        }

        public void presenceChanged(String XMPPAddress)
        {}
    }
}
