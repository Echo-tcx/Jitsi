/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.gibberish;

import java.util.*;

import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;

/**
 * A Gibberish implementation of the ProtocolProviderService.
 *
 * @author Emil Ivov
 */
public class ProtocolProviderServiceGibberishImpl
    extends AbstractProtocolProviderService
{
    private static final Logger logger
        = Logger.getLogger(ProtocolProviderServiceGibberishImpl.class);

    /**
     * The name of this protocol.
     */
    public static final String GIBBERISH_PROTOCOL_NAME = "Gibberish";

    /**
     * The id of the account that this protocol provider represents.
     */
    private AccountID accountID = null;

    /**
     * We use this to lock access to initialization.
     */
    private Object initializationLock = new Object();

    /**
     * The hashtable with the operation sets that we support locally.
     */
    private Hashtable supportedOperationSets = new Hashtable();

    /**
     * Indicates whether or not the provider is initialized and ready for use.
     */
    private boolean isInitialized = false;

    /**
     * The logo corresponding to the gibberish protocol.
     */
    private ProtocolIconGibberishImpl gibberishIcon
        = new ProtocolIconGibberishImpl();

    /**
     * The registration state that we are currently in. Note that in a real
     * world protocol implementation this field won't exist and the registration
     * state would be retrieved from the protocol stack.
     */
    private RegistrationState currentRegistrationState
        = RegistrationState.UNREGISTERED;

    /**
     * The default constructor for the Gibberish protocol provider.
     */
    public ProtocolProviderServiceGibberishImpl()
    {
        logger.trace("Creating a gibberish provider.");
    }

    /**
     * Initializes the service implementation, and puts it in a sate where it
     * could interoperate with other services. It is strongly recomended that
     * properties in this Map be mapped to property names as specified by
     * <tt>AccountProperties</tt>.
     *
     * @param userID the user id of the gibberish account we're currently
     * initializing
     * @param accountID the identifier of the account that this protocol
     * provider represents.
     *
     * @see net.java.sip.communicator.service.protocol.AccountID
     */
    protected void initialize(String userID,
                              AccountID accountID)
    {
        synchronized(initializationLock)
        {
            this.accountID = accountID;

            //initialize the presence operationset
            OperationSetPersistentPresenceGibberishImpl persistentPresence =
                new OperationSetPersistentPresenceGibberishImpl(this);

            supportedOperationSets.put(
                OperationSetPersistentPresence.class.getName(),
                persistentPresence);


            //register it once again for those that simply need presence and
            //won't be smart enough to check for a persistent presence
            //alternative
            supportedOperationSets.put( OperationSetPresence.class.getName(),
                                        persistentPresence);

            //initialize the IM operation set
            OperationSetBasicInstantMessagingGibberishImpl basicInstantMessaging
                = new OperationSetBasicInstantMessagingGibberishImpl(
                    this
                    , (OperationSetPersistentPresenceGibberishImpl)
                            persistentPresence);

            supportedOperationSets.put(
                OperationSetBasicInstantMessaging.class.getName(),
                basicInstantMessaging);

            //initialize the typing notifications operation set
            OperationSetTypingNotifications typingNotifications =
                new OperationSetTypingNotificationsGibberishImpl(
                        this, persistentPresence);

            supportedOperationSets.put(
                OperationSetTypingNotifications.class.getName(),
                typingNotifications);

            isInitialized = true;
        }
    }

    /**
     * Returns the AccountID that uniquely identifies the account represented
     * by this instance of the ProtocolProviderService.
     *
     * @return the id of the account represented by this provider.
     */
    public AccountID getAccountID()
    {
        return accountID;
    }

    /**
     * Returns the operation set corresponding to the specified class or null
     * if this operation set is not supported by the provider implementation.
     *
     * @param opsetClass the <tt>Class</tt> of the operation set that we're
     *   looking for.
     * @return returns an OperationSet of the specified <tt>Class</tt> if
     *   the undelying implementation supports it or null otherwise.
     */
    public OperationSet getOperationSet(Class opsetClass)
    {
        return (OperationSet) getSupportedOperationSets()
            .get(opsetClass.getName());
    }

    /**
     * Returns the short name of the protocol that the implementation of this
     * provider is based upon (like SIP, Jabber, ICQ/AIM, or others for
     * example).
     *
     * @return a String containing the short name of the protocol this
     *   service is implementing (most often that would be a name in
     *   ProtocolNames).
     */
    public String getProtocolName()
    {
        return GIBBERISH_PROTOCOL_NAME;
    }

    /**
     * Returns the state of the registration of this protocol provider with
     * the corresponding registration service.
     *
     * @return ProviderRegistrationState
     */
    public RegistrationState getRegistrationState()
    {
        return currentRegistrationState;
    }

    /**
     * Returns an array containing all operation sets supported by the
     * current implementation.
     *
     * @return a java.util.Map containing instance of all supported
     *   operation sets mapped against their class names (e.g.
     *   OperationSetPresence.class.getName()) .
     */
    public Map getSupportedOperationSets()
    {
        //Copy the map so that the caller is not able to modify it.
        return (Map)supportedOperationSets.clone();
    }

    /**
     * Starts the registration process.
     *
     * @param authority the security authority that will be used for
     *   resolving any security challenges that may be returned during the
     *   registration or at any moment while wer're registered.
     * @throws OperationFailedException with the corresponding code it the
     *   registration fails for some reason (e.g. a networking error or an
     *   implementation problem).
     */
    public void register(SecurityAuthority authority)
        throws OperationFailedException
    {
        //we don't really need a password here since there's no server in
        //Gibberish but nevertheless we'll behave as if we did.

        //verify whether a password has already been stored for this account
        String password = GibberishActivator.
            getProtocolProviderFactory().loadPassword(getAccountID());

        //if we don't - retrieve it from the user through the security authority
        if (password == null)
        {
            //create a default credentials object
            UserCredentials credentials = new UserCredentials();
            credentials.setUserName(getAccountID().getUserID());

            //request a password from the user
            credentials = authority.obtainCredentials(
                    "Gibberish",
                    credentials);

            //extract the password the user passed us.
            char[] pass = credentials.getPassword();

            // the user didn't provide us a password (canceled the operation)
            if (pass == null)
            {
                fireRegistrationStateChanged(
                    getRegistrationState(),
                    RegistrationState.UNREGISTERED,
                    RegistrationStateChangeEvent.REASON_USER_REQUEST, "");
                return;
            }
            password = new String(pass);

            //if the user indicated that the password should be saved, we'll ask
            //the proto provider factory to store it for us.
            if (credentials.isPasswordPersistent())
            {
                GibberishActivator.getProtocolProviderFactory()
                    .storePassword(getAccountID(), password);
            }
        }


        RegistrationState oldState = currentRegistrationState;
        currentRegistrationState = RegistrationState.REGISTERED;

        fireRegistrationStateChanged(
            oldState
            , currentRegistrationState
            , RegistrationStateChangeEvent.REASON_USER_REQUEST
            , null);
    }

    /**
     * Makes the service implementation close all open sockets and release
     * any resources that it might have taken and prepare for
     * shutdown/garbage collection.
     */
    public void shutdown()
    {
        if(!isInitialized)
        {
            return;
        }
        logger.trace("Killing the Gibberish Protocol Provider.");

        if(isRegistered())
        {
            try
            {
                //do the unregistration
                unregister();
            }
            catch (OperationFailedException ex)
            {
                //we're shutting down so we need to silence the exception here
                logger.error(
                    "Failed to properly unregister before shutting down. "
                    + getAccountID()
                    , ex);
            }
        }

        isInitialized = false;
    }

    /**
     * Ends the registration of this protocol provider with the current
     * registration service.
     *
     * @throws OperationFailedException with the corresponding code it the
     *   registration fails for some reason (e.g. a networking error or an
     *   implementation problem).
     */
    public void unregister()
        throws OperationFailedException
    {
        RegistrationState oldState = currentRegistrationState;
        currentRegistrationState = RegistrationState.UNREGISTERED;

        fireRegistrationStateChanged(
            oldState
            , currentRegistrationState
            , RegistrationStateChangeEvent.REASON_USER_REQUEST
            , null);
    }

    /**
     * Returns the gibberish protocol icon.
     * @return the gibberish protocol icon
     */
    public ProtocolIcon getProtocolIcon()
    {
        return gibberishIcon;
    }
}
