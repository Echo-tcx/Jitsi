/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.service.contactlist;

import java.util.*;

import net.java.sip.communicator.service.protocol.*;

/**
 * A MetaContact is an abstraction used for merging mutltiple Contacts (most
 * often) belonging to different <tt>ProtocolProvider</tt>s.
 * <p>
 * Instances of a MetaContact are readonly objects that cannot be modified
 * directly but only through the corresponding MetaContactListService.
 * <p>
 * @author Emil Ivov
 */
public interface MetaContact extends Comparable
{
    /**
     * Returns the default protocol specific <tt>Contact</tt> to use when
     * communicating with this <tt>MetaContact</tt>.
     * @return the default <tt>Contact</tt> to use when communicating with
     * this <tt>MetaContact</tt>
     */
    public Contact getDefaultContact();

    /**
     * Returns the default protocol specific <tt>Contact</tt> to use with this
     * <tt>MetaContact</tt> for a precise operation (IM, call, ...).
     * 
     * @param operationSet the operation for which the default contact is needed
     * @return the default contact for the specified operation.
     */
    public Contact getDefaultContact(Class<? extends OperationSet> operationSet);

    /**
     * Returns a <tt>java.util.Iterator</tt> with all protocol specific
     * <tt>Contacts</tt> encapsulated by this <tt>MetaContact</tt>.
     * <p>
     * Note to implementors:  In order to prevent problems with concurrency, the
     * <tt>Iterator</tt> returned by this method should not be over the actual
     * list of contacts but rather over a copy of that list.
     * <p>
     * @return a <tt>java.util.Iterator</tt> containing all protocol specific
     * <tt>Contact</tt>s that were registered as subcontacts for this
     * <tt>MetaContact</tt>
     */
    public Iterator<Contact> getContacts();

    /**
     * Returns a contact encapsulated by this meta contact, having the specified
     * contactAddress and coming from the indicated ownerProvider.
     * @param contactAddress the address of the contact who we're looking for.
     * @param ownerProvider a reference to the ProtocolProviderService that
     * the contact we're looking for belongs to.
     * @return a reference to a <tt>Contact</tt>, encapsulated by this
     * MetaContact, carrying the specified address and originating from the
     * specified ownerProvider or null if no such contact exists..
     */
    public Contact getContact( String                  contactAddress,
                               ProtocolProviderService ownerProvider);

    /**
     * Returns the number of protocol speciic <tt>Contact</tt>s that this
     * <tt>MetaContact</tt> contains.
     * @return an int indicating the number of protocol specific contacts merged
     * in this <tt>MetaContact</tt>
     */
    public int getContactCount();

    /**
     * Returns all protocol specific Contacts, encapsulated by this MetaContact
     * and coming from the indicated ProtocolProviderService. If none of the
     * contacts encapsulated by this MetaContact is originating from the
     * specified provider then an empty iterator is returned.
     * <p>
     * Note to implementors:  In order to prevent problems with concurrency, the
     * <tt>Iterator</tt> returned by this method should not be over the actual
     * list of contacts but rather over a copy of that list.
     * <p>
     * @param provider a reference to the <tt>ProtocolProviderService</tt>
     * whose contacts we'd like to get.
     * @return an <tt>Iterator</tt> over all contacts encapsulated in this
     * <tt>MetaContact</tt> and originating from the specified provider.
     */
    public Iterator<Contact> getContactsForProvider(
                                              ProtocolProviderService provider);

    /**
     * Returns the MetaContactGroup currently containin this meta contact
     * @return a reference to the MetaContactGroup currently containing this
     * meta contact.
     */
    public MetaContactGroup getParentMetaContactGroup();

    /**
     * Returns a String identifier (the actual contents is left to
     * implementations) that uniquely represents this <tt>MetaContact</tt>
     * in the containing <tt>MetaContactList</tt>
     * @return String
     */
    public String getMetaUID();

    /**
     * Returns a characteristic display name that can be used when including
     * this <tt>MetaContact</tt> in user interface.
     * @return a human readable String that represents this meta contact.
     */
    public String getDisplayName();

    /**
     * Returns the avatar of this contact, that can be used when including this
     * <tt>MetaContact</tt> in user interface.
     *
     * @return an avatar (e.g. user photo) of this contact.
     */
    public byte[] getAvatar();

    /**
     * Returns the avatar of this contact, that can be used when including this
     * <tt>MetaContact</tt> in user interface. The isLazy
     * parameter would tell the implementation if it could return the locally
     * stored avatar or it should obtain the avatar right from the server.
     *
     * @param isLazy Indicates if this method should return the locally stored
     * avatar or it should obtain the avatar right from the server.
     * @return an avatar (e.g. user photo) of this contact.
     */
    public byte[] getAvatar(boolean isLazy);

    /**
     * Returns a String representation of this <tt>MetaContact</tt>.
     * @return a String representation of this <tt>MetaContact</tt>.
     */
    public String toString();

    /**
     * Adds a custom detail to this contact.
     * @param name name of the detail.
     * @param value the value of the detail.
     */
    public void addDetail(String name, String value);

    /**
     * Remove the given detail.
     * @param name of the detail to be removed.
     * @param value value of the detail to be removed.
     */
    public void removeDetail(String name, String value);

    /**
     * Remove all details with given name.
     * @param name of the details to be removed.
     */
    public void removeDetails(String name);

    /**
     * Change the detail.
     * @param name of the detail to be changed.
     * @param oldValue the old value of the detail.
     * @param newValue the new value of the detail.
     */
    public void changeDetail(String name, String oldValue, String newValue);

    /**
     * Get all details with given name.
     * @param name the name of the details we are searching.
     * @return list of string values for the details with the given name.
     */
    public List<String> getDetails(String name);
}
