/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.yahoo;

import java.io.*;
import java.util.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;
import ymsg.network.*;
import ymsg.network.event.*;

/**
 * This class encapsulates the Roster class. Once created, it will
 * register itself as a listener to the encapsulated Roster and modify it's
 * local copy of Contacts and ContactGroups every time an event is generated
 * by the underlying framework. The class would also generate
 * corresponding sip-communicator events to all events coming from smack.
 *
 * @author Damian Minkov
 */
public class ServerStoredContactListYahooImpl
{
    private static final Logger logger =
        Logger.getLogger(ServerStoredContactListYahooImpl.class);

    /**
     * The name of the Volatile group
     */
    private static final String VOLATILE_GROUP_NAME = "NotInContactList";
    
    /**
     * If there is no group and we add contact with no parent 
     * a default group is created with name : DEFAULT_GROUP_NAME
     */
    private static final String DEFAULT_GROUP_NAME = "General";

    /**
     * The root contagroup. The container for all yahoo buddies and groups.
     */
    private RootContactGroupYahooImpl rootGroup = new RootContactGroupYahooImpl();

    /**
     * The operation set that created us and that we could use when dispatching
     * subscription events.
     */
    private OperationSetPersistentPresenceYahooImpl parentOperationSet = null;

    /**
     * The provider that is on top of us.
     */
    private ProtocolProviderServiceYahooImpl yahooProvider = null;

    private YahooSession yahooSession = null;

    /**
     * Listeners that would receive event notifications for changes in group
     * names or other properties, removal or creation of groups.
     */
    private Vector serverStoredGroupListeners = new Vector();

    private ContactListModListenerImpl contactListModListenerImpl
        = new ContactListModListenerImpl();
    
    /**
     * Handler for incoming authorization requests.
     */
    private AuthorizationHandler handler = null;
    
    private Hashtable addedCustomYahooIds = new Hashtable();

    /**
     * Creates a ServerStoredContactList wrapper for the specified BuddyList.
     *
     * @param parentOperationSet the operation set that created us and that
     * we could use for dispatching subscription events
     * @param provider the provider that has instantiated us.
     */
    ServerStoredContactListYahooImpl(
        OperationSetPersistentPresenceYahooImpl parentOperationSet,
        ProtocolProviderServiceYahooImpl        provider)
    {
        //We need to init these as early as possible to ensure that the provider
        //and the operationsset would not be null in the incoming events.
        this.parentOperationSet = parentOperationSet;

        this.yahooProvider = provider;
        rootGroup.setOwnerProvider(provider);
    }
    
    /**
     * Handler for incoming authorization requests.
     *
     * @param handler an instance of an AuthorizationHandler for
     *   authorization requests coming from other users requesting
     *   permission add us to their contact list.
     */
    public void setAuthorizationHandler(AuthorizationHandler handler)
    {
        this.handler = handler;
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
     * @param listener the ServerStoredGroupListener to register for group events
     */
    void addGroupListener(ServerStoredGroupListener listener)
    {
        synchronized(serverStoredGroupListeners)
        {
            if(!serverStoredGroupListeners.contains(listener))
            this.serverStoredGroupListeners.add(listener);
        }
    }

    /**
     * Removes the specified group listener so that it won't receive further
     * events on group modification/creation/destruction.
     * @param listener the ServerStoredGroupListener to unregister
     */
    void removeGroupListener(ServerStoredGroupListener listener)
    {
        synchronized(serverStoredGroupListeners)
        {
            this.serverStoredGroupListeners.remove(listener);
        }
    }

    /**
     * Creates the corresponding event and notifies all
     * <tt>ServerStoredGroupListener</tt>s that the source group has been
     * removed, changed, renamed or whatever happened to it.
     * @param group the ContactGroup that has been created/modified/removed
     * @param eventID the id of the event to generate.
     */
    private void fireGroupEvent(ContactGroupYahooImpl group, int eventID)
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
                , yahooProvider
                , parentOperationSet);

        logger.trace("Will dispatch the following grp event: " + evt);

        Iterator listeners = null;
        synchronized (serverStoredGroupListeners)
        {
            listeners = new ArrayList(serverStoredGroupListeners).iterator();
        }

        while (listeners.hasNext())
        {
            ServerStoredGroupListener listener
                = (ServerStoredGroupListener) listeners.next();

            if (eventID == ServerStoredGroupEvent.GROUP_REMOVED_EVENT)
                listener.groupRemoved(evt);
            else if (eventID == ServerStoredGroupEvent.GROUP_RENAMED_EVENT)
                listener.groupNameChanged(evt);
            else if (eventID == ServerStoredGroupEvent.GROUP_CREATED_EVENT)
                listener.groupCreated(evt);
            else if (eventID == ServerStoredGroupEvent.GROUP_RESOLVED_EVENT)
                listener.groupResolved(evt);
        }
    }

    /**
     * Make the parent persistent presence operation set dispatch a contact
     * removed event.
     * @param parentGroup the group where that the removed contact belonged to.
     * @param contact the contact that was removed.
     */
    private void fireContactRemoved( ContactGroup parentGroup,
                                     ContactYahooImpl contact)
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
                                   ContactGroupYahooImpl newParentGroup,
                                   ContactYahooImpl contact)
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
    ProtocolProviderServiceYahooImpl getParentProvider()
    {
        return yahooProvider;
    }

    /**
     * Returns the ConntactGroup with the specified name or null if no such
     * group was found.
     * <p>
     * @param name the name of the group we're looking for.
     * @return a reference to the ContactGroupYahooImpl instance we're looking for
     * or null if no such group was found.
     */
    public ContactGroupYahooImpl findContactGroup(String name)
    {
        Iterator contactGroups = rootGroup.subgroups();

        while(contactGroups.hasNext())
        {
            ContactGroupYahooImpl contactGroup
                = (ContactGroupYahooImpl) contactGroups.next();

            if (contactGroup.getGroupName().equals(name))
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
    public ContactYahooImpl findContactById(String id)
    {
        Iterator contactGroups = rootGroup.subgroups();
        ContactYahooImpl result = null;

        while(contactGroups.hasNext())
        {
            ContactGroupYahooImpl contactGroup
                = (ContactGroupYahooImpl) contactGroups.next();

            result = contactGroup.findContact(id);

            if (result != null)
                return result;
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
    public ContactGroup findContactGroup(ContactYahooImpl child)
    {
        Iterator contactGroups = rootGroup.subgroups();

        while(contactGroups.hasNext())
        {
            ContactGroupYahooImpl contactGroup
                = (ContactGroupYahooImpl) contactGroups.next();

            if( contactGroup.findContact(child.getAddress())!= null)
                return contactGroup;
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
        ContactGroupYahooImpl parent = getFirstPersistentGroup();
        
        if(parent == null)
        {
            // if there is no group create it
            parent = createUnresolvedContactGroup(DEFAULT_GROUP_NAME);
        }
        
        addContact(parent, id);
    }

    /**
     * Adds a new contact with the specified screenname to the list under the
     * specified group.
     * @param id the id of the contact to add.
     * @param parent the group under which we want the new contact placed.
     * @throws OperationFailedException if the contact already exist
     */
    public void addContact(final ContactGroupYahooImpl parent, String id)
        throws OperationFailedException
    {
        logger.trace("Adding contact " + id + " to parent=" + parent);

        //if the contact is already in the contact list and is not volatile,
        //then only broadcast an event
        ContactYahooImpl existingContact = findContactById(id);

        if( existingContact != null
            && existingContact.isPersistent() )
        {
            logger.debug("Contact " + id + " already exists.");
            throw new OperationFailedException(
                "Contact " + id + " already exists.",
                OperationFailedException.SUBSCRIPTION_ALREADY_EXISTS);
        }
        
        if(id.indexOf("@") > -1 )
            addedCustomYahooIds.put(YahooSession.getYahooUserID(id), id);        
        
        try
        {
            yahooSession.addFriend(YahooSession.getYahooUserID(id), parent.getGroupName());
        }
        catch(IOException ex)
        {
            throw new OperationFailedException(
                "Contact cannot be added " + id,
                OperationFailedException.NETWORK_FAILURE);
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
    ContactYahooImpl createVolatileContact(String id)
    {
        ContactYahooImpl newVolatileContact = 
            new ContactYahooImpl(id, this, false, false, true);

        //Check whether a volatile group already exists and if not create one
        ContactGroupYahooImpl theVolatileGroup = getNonPersistentGroup();

        //if the parent group is null then create it
        if (theVolatileGroup == null)
        {
            theVolatileGroup = new VolatileContactGroupYahooImpl(
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
    ContactYahooImpl createUnresolvedContact(ContactGroup parentGroup, String id)
    {
        ContactYahooImpl newUnresolvedContact
            = new ContactYahooImpl(id, this, false, false, false);

        if(parentGroup instanceof ContactGroupYahooImpl)
            ((ContactGroupYahooImpl)parentGroup).
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
    ContactGroupYahooImpl createUnresolvedContactGroup(String groupName)
    {
        ContactGroupYahooImpl newUnresolvedGroup =
            new ContactGroupYahooImpl(groupName, this);

        this.rootGroup.addSubGroup(newUnresolvedGroup);

        fireGroupEvent(newUnresolvedGroup
                        , ServerStoredGroupEvent.GROUP_CREATED_EVENT);

        return newUnresolvedGroup;
    }

    /**
     * Creates the specified group on the server stored contact list.
     * @param groupName a String containing the name of the new group.
     * @throws OperationFailedException with code CONTACT_GROUP_ALREADY_EXISTS
     * if the group we're trying to create is already in our contact list.
     */
    public void createGroup(String groupName)
        throws OperationFailedException
    {
        logger.trace("Creating group: " + groupName);

        ContactGroupYahooImpl existingGroup = findContactGroup(groupName);

        if( existingGroup != null && existingGroup.isPersistent() )
        {
            logger.debug("ContactGroup " + groupName + " already exists.");
            throw new OperationFailedException(
                           "ContactGroup " + groupName + " already exists.",
                OperationFailedException.CONTACT_GROUP_ALREADY_EXISTS);
        }

        // create unresolved group if friend is added - group will be resolved
        createUnresolvedContactGroup(groupName);
    }

    /**
     * Removes the specified group from the buddy list.
     * @param groupToRemove the group that we'd like removed.
     */
    public void removeGroup(ContactGroupYahooImpl groupToRemove)
    {
        // to remove group just remove all the contacts in it
        
        logger.trace("removing group " + groupToRemove);
    
        // if its not persistent group just remove it
        if(!groupToRemove.isPersistent() || !groupToRemove.isResolved())
        {
            rootGroup.removeSubGroup(groupToRemove);
            fireGroupEvent(groupToRemove, 
                ServerStoredGroupEvent.GROUP_REMOVED_EVENT);
            return;
        }
        
        Vector contacts = groupToRemove.getSourceGroup().getMembers();
        
        if(contacts.size() == 0)
        {
            // the group is empty just remove it
            rootGroup.removeSubGroup(groupToRemove);
            fireGroupEvent(groupToRemove, 
                ServerStoredGroupEvent.GROUP_REMOVED_EVENT);
            return;
        }
        
        Iterator iter = contacts.iterator();
        while(iter.hasNext())
        {
            YahooUser item =  (YahooUser)iter.next();

            try
            {
                yahooSession.removeFriend(item.getId(), groupToRemove.getGroupName());
            }
            catch(IOException ex)
            {
                logger.info("Cannot Remove contact " + item.getId());
            }
        }
    }

    /**
     * Removes a contact from the serverside list
     * Event will come for successful operation
     * @param contactToRemove ContactYahooImpl
     */
    void removeContact(ContactYahooImpl contactToRemove)
    {
        logger.trace("Removing yahoo contact " + contactToRemove.getSourceContact());
        
        if(contactToRemove.isVolatile())
        {
            ContactGroupYahooImpl parent = 
                (ContactGroupYahooImpl)contactToRemove.getParentContactGroup();    
            
            parent.removeContact(contactToRemove);
            fireContactRemoved(parent, contactToRemove);
            return;
        }
        
        try
        {
            yahooSession.removeFriend(
                contactToRemove.getSourceContact().getId(), 
                contactToRemove.getParentContactGroup().getGroupName());
        }
        catch(IOException ex)
        {
            logger.info("Cannot Remove contact " + contactToRemove);
        }
    }

    /**
     * Renames the specified group according to the specified new name..
     * @param groupToRename the group that we'd like removed.
     * @param newName the new name of the group
     */
    public void renameGroup(ContactGroupYahooImpl groupToRename, String newName)
    {
        // not working
        /*
            try
            {
                yahooSession.renameGroup(groupToRename.getGroupName(), newName);
            }
            catch(IOException ex)
            {
                logger.info("Cannot rename group " + groupToRename);
            }

            fireGroupEvent(groupToRename, ServerStoredGroupEvent.GROUP_RENAMED_EVENT);
         */
    }

    /**
     * Moves the specified <tt>contact</tt> to the group indicated by
     * <tt>newParent</tt>.
     * @param contact the contact that we'd like moved under the new group.
     * @param newParent the group where we'd like the parent placed.
     */
    public void moveContact(ContactYahooImpl contact,
                            ContactGroupYahooImpl newParent)
    {
        String userID = contact.getID();
        try
        {
            contactListModListenerImpl.
                waitForMove(userID, 
                contact.getParentContactGroup().getGroupName());
            
            yahooSession.addFriend(
                userID, 
                newParent.getGroupName());
        }
        catch(IOException ex)
        {
            contactListModListenerImpl.removeWaitForMove(userID);
            logger.error("Contact cannot be added " + ex.getMessage());
        }
    }

    /**
     * Returns the volatile group
     *
     * @return ContactGroupYahooImpl
     */
    private ContactGroupYahooImpl getNonPersistentGroup()
    {
        for (int i = 0; i < getRootGroup().countSubgroups(); i++)
        {
            ContactGroupYahooImpl gr =
                (ContactGroupYahooImpl)getRootGroup().getGroup(i);

            if(!gr.isPersistent())
                return gr;
        }

        return null;
    }
    
    /**
     * Returns the first persistent group
     *
     * @return ContactGroupIcqImpl
     */
    private ContactGroupYahooImpl getFirstPersistentGroup()
    {
        for (int i = 0; i < getRootGroup().countSubgroups(); i++)
        {
            ContactGroupYahooImpl gr =
                (ContactGroupYahooImpl)getRootGroup().getGroup(i);

            if(gr.isPersistent())
                return gr;
        }

        return null;
    }

    /**
     * Finds Group by provided its yahoo ID
     * @param id String
     * @return ContactGroupYahooImpl
     */
    private ContactGroupYahooImpl findContactGroupByYahooId(String id)
    {
        Iterator contactGroups = rootGroup.subgroups();

        while(contactGroups.hasNext())
        {
            ContactGroupYahooImpl contactGroup
                = (ContactGroupYahooImpl) contactGroups.next();

            if (contactGroup.getSourceGroup().getName().equals(id))
                return contactGroup;
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
                                   ContactYahooImpl contact)
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
    void fireContactResolved( ContactGroup parentGroup,
                                      ContactYahooImpl contact)
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

    /**
     * When the protocol is online this method is used to fill or resolve
     * the current contact list
     */
    private void initList()
    {
        logger.trace("Start init list of " + yahooProvider.getAccountID().getUserID());
        
        YahooGroup[] groups = yahooSession.getGroups();
        
        for (int i = 0; i < groups.length; i++)
        {
            YahooGroup item = groups[i];
            
            ContactGroupYahooImpl group = findContactGroup(item.getName());
            
            if(group == null)
            {
                // create the group as it doesn't exist
                group =
                    new ContactGroupYahooImpl(item, item.getMembers(), this, true);

                rootGroup.addSubGroup(group);

                //tell listeners about the added group
                fireGroupEvent(group, ServerStoredGroupEvent.GROUP_CREATED_EVENT);
            }
            else
            {
                // the group exist so just resolved. The group will check and
                // create or resolve its entries
                group.setResolved(item);

                //fire an event saying that the group has been resolved
                fireGroupEvent(group
                               , ServerStoredGroupEvent.GROUP_RESOLVED_EVENT);

                /** @todo  if something to delete . delete it */
            }
            
            logger.trace("Init of group done! : " + group);
        }
    }
    
    /**
     * @param name Name of the group to search
     * @return The yahoo group with given name
     */
    private YahooGroup findGroup(String name)
    {
        YahooGroup[] groups = yahooSession.getGroups();
        for (int i = 0; i < groups.length; i++)
        {
            YahooGroup elem = groups[i];
            if(elem.getName().equals(name))
                return elem;
        }
        return null;
    }
    
    /**
     * Imulates firing adding contact in group and moving contact to group.
     * When moving contact it is first adding to the new group then
     * it is removed from the old one.
     */
    private class ContactListModListenerImpl
        extends SessionAdapter
    {
        private Hashtable waitMove = new Hashtable();
        
        public void waitForMove(String id, String oldParent)
        {
            waitMove.put(id, oldParent);
        }
        
        public void removeWaitForMove(String id)
        {
            waitMove.remove(id);
        }
        
        /**
         * Successfully added a friend
         * friend - YahooUser of friend
         * group - name of group added to
         */
        public void friendAddedReceived(SessionFriendEvent ev)
        {
            logger.trace("Receive event for adding a friend : " + ev);
            
            ContactGroupYahooImpl group = 
                findContactGroup(ev.getGroup());
            
            if(group == null){
                logger.trace("Group not found!" + ev.getGroup());
                return;
            }
            
            // if group is note resolved resolve it
            // this means newly created group
            if(!group.isResolved())
            {
                YahooGroup gr = findGroup(ev.getGroup());
                
                if(gr != null)
                    group.setResolved(gr);
                
                // contact will be added when resolving the group
                
                return;
            }
            
            String contactID = ev.getFriend().getId();
            ContactYahooImpl contactToAdd = findContactById(contactID);
            
            boolean isVolatile = false;

            if(contactToAdd == null)
            {
                if(addedCustomYahooIds.containsKey(contactID))
                {
                    String expectedContactID = 
                         (String)addedCustomYahooIds.remove(contactID);

                    contactToAdd =
                        new ContactYahooImpl(expectedContactID, ev.getFriend(), 
                            ServerStoredContactListYahooImpl.this, true, true);
                }
                else
                {
                    contactToAdd =
                        new ContactYahooImpl(ev.getFriend(), 
                            ServerStoredContactListYahooImpl.this, true, true);
                }
            }
            else
            {
                isVolatile = contactToAdd.isVolatile();
            }
            
            //first check is contact is moving from a group
            Object isWaitingForMove = waitMove.get(contactID);
            
            if(isWaitingForMove != null && isWaitingForMove instanceof String)
            {
                // waits for move into group
                // will remove it from old group and will wait for event remove 
                // from group, then will fire moved to group event
                String oldParent = (String)isWaitingForMove;
                
                group.addContact(contactToAdd);
                waitMove.put(contactID, group.getSourceGroup());
                try
                {
                    yahooSession.removeFriend(contactID, oldParent);
                }
                catch(IOException ex)
                {
                    logger.info("Cannot Remove(till moving) contact :" + 
                        contactToAdd + " from group " + oldParent);
                }
                return;
            }
            
            if(isVolatile)
            {
                // we must remove the volatile buddy as we will add
                // the persistent one. 
                // Volatile buddy is moving from the volatile group
                // to the new one
                ContactGroupYahooImpl parent = 
                    (ContactGroupYahooImpl)contactToAdd.getParentContactGroup();

                parent.removeContact(contactToAdd);
                fireContactRemoved(parent, contactToAdd);

                contactToAdd.setPersistent(true);
                contactToAdd.setResolved(ev.getFriend());

                group.addContact(contactToAdd);

                fireContactAdded(group, contactToAdd);
                waitMove.remove(contactID);

                return;
            }
            
            group.addContact(contactToAdd);
            fireContactAdded(group, contactToAdd);
        }

        /**
         * Successfully removed a friend
         * friend - YahooUser of friend
         * group - name of group removed from
         */
        public void friendRemovedReceived(SessionFriendEvent ev)
        {
            logger.trace("Receive event for removing a friend : " + ev);
            
            String contactID = ev.getFriend().getId();
            
            // first check is this part of move action
            Object waitForMoveObj = waitMove.get(contactID);
            if(waitForMoveObj != null && waitForMoveObj instanceof YahooGroup)
            {
                // first get the group - oldParent
                ContactGroupYahooImpl oldParent = findContactGroup(ev.getGroup());
                ContactYahooImpl contactToRemove = oldParent.findContact(contactID);
                
                oldParent.removeContact(contactToRemove);
                waitMove.remove(contactID);
                
                ContactGroupYahooImpl newParent = 
                    findContactGroup(((YahooGroup)waitForMoveObj).getName());
                
                fireContactMoved(oldParent, newParent, contactToRemove);
                return;
            }
            
            ContactYahooImpl contactToRemove = findContactById(contactID);
            
            // strange we cannot find the contact to be removed
            if(contactToRemove == null)
                return;
            
            ContactGroupYahooImpl parentGroup =
                    (ContactGroupYahooImpl)contactToRemove.getParentContactGroup();
            parentGroup.removeContact(contactToRemove);
            fireContactRemoved(parentGroup, contactToRemove);
            
            // check if the group is deleted. If the contact is the last one in
            // the group. The group is also deleted
            if(findGroup(ev.getGroup()) == null)
            {
                rootGroup.removeSubGroup(parentGroup);
                fireGroupEvent(parentGroup, ServerStoredGroupEvent.GROUP_REMOVED_EVENT);
            }
        }
        
        /**
         * Someone wants to add us to their friends list
         * to - the target (us!)
         * from - the user who wants to add us
         * message - the request message text
         */
        public void contactRequestReceived(SessionEvent ev)
        {
            logger.info("contactRequestReceived : " + ev);
            
            if(handler == null || ev.getFrom() == null)
                return;
            
            ContactYahooImpl contact = findContactById(ev.getFrom());
            
            if(contact == null)
                contact = createVolatileContact(ev.getFrom());
            
            AuthorizationRequest request = new AuthorizationRequest();
            request.setReason(ev.getMessage());
            
            AuthorizationResponse resp = 
                handler.processAuthorisationRequest(request, contact);
            
            if (resp.getResponseCode() == AuthorizationResponse.REJECT)
            {
                try{
                    yahooSession.rejectContact(ev, resp.getReason());
                }catch(IOException ex){
                    logger.error("Cannot send reject : " + ex.getMessage());
                }
            }
        }

        /**
         * Someone has rejected our attempts to add them to our friends list
         * from - the user who rejected us
         * message - rejection message text
         */
        public void contactRejectionReceived(SessionEvent ev)
        {
            logger.info("contactRejectionReceived : " + ev);

            if(handler == null)
                return;
            
            ContactYahooImpl contact = findContactById(ev.getFrom());
            
            AuthorizationResponse resp = 
                new AuthorizationResponse(AuthorizationResponse.REJECT, ev.getMessage());
            handler.processAuthorizationResponse(resp, contact);
        }
    }

    /**
     * Sets the yahoo session instance of the lib
     * which comunicates with the server
     * @param session YahooSession
     */
    void setYahooSession(YahooSession session)
    {
        this.yahooSession = session;
        session.addSessionListener(contactListModListenerImpl);
        initList();
    }
}