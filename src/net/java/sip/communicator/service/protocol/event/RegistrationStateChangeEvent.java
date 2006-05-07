/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.service.protocol.event;

import java.beans.PropertyChangeEvent;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;

/**
 * Instances of this class represent a  change in the status of the provider
 * that triggerred them. A status change may have occurred because the user
 * requested it or because an error or a failure have occurred, in which case
 * the reason and reason code would be set accordingly.
 * <p>
 * Keep in mind that reasons are not localized and services such as the user
 * interface should only show them in a "details box". In the rest of the time,
 * such services should consult the error code and provide corresponding,
 * localized, reason phrases.
 * <p>
 * Note, that we have tried to provide a maximum number of error codes in order
 * to enumerate all possible reason codes that may be returned from servers
 * in the various protocols. Each protocol would only return a subset of these.
 * <p>
 * @author Emil Ivov
 */
public class RegistrationStateChangeEvent extends PropertyChangeEvent
{
    /**
     * Indicates that no reason is specified for this event transition.
     */
    public static final int REASON_NOT_SPECIFIED = -1;

    /**
     * Indicates that the server has refused registration du to a problem with
     * the authentication (most probably a wrong password).
     */
    public static final int REASON_AUTHENTICATION_FAILED = 1;

    /**
     * Indicates that the same user identifier has logged somewhere else. This
     * code is often returned when transitting into disconnected state. Some
     * protocols, however, support multiple logins and servers would only return
     * this code for purely informational reasons.
     */
    public static final int REASON_MULTIPLE_LOGINS = 2;

    /**
     * Indicates that the server does not recognize the used idenfitier that
     * we tried to register with.
     */
    public static final int REASON_NON_EXISTING_USER_ID = 3;

    /**
     * Indicates that we have too many existing registrations from the local
     * IP address and the server won't allow us to open any more of them.
     */
    public static final int REASON_CLIENT_LIMIT_REACHED_FOR_IP = 4;

    /**
     * Indicates that we have been disconnecting and reconnecting to the server
     * at a rate that ha become too fast. We're temporarily banned and would
     * have to wait a bit before trying again. It is often a good idea for the
     * user interface to prevent the user from actually trying again for a
     * certain amount of time.
     */
    public static final int REASON_RECONNECTION_RATE_LIMIT_EXCEEDED = 5;

    /**
     * The reason code returned by the server in order to explain the state
     * transition.
     */
    private int reasonCode = REASON_NOT_SPECIFIED;

    /**
     * A (non localized) String containing information further explaining the
     * reason code.
     */
    private String reason = null;

    /**
     * Creates an event instance indicating a change of the provider state
     * from <tt>oldValue</tt> to <tt>newValue</tt>.
     *
     * @param source the provider that generated the event
     * @param oldValue the status the source provider was int before enetering
     * the new state.
     * @param newValue the status the source provider is currently in.
     * @param reasonCode a value corresponding to one of the REASON_XXX fields
     * of this class, indicating the reason for this state transition.
     * @param reason a String further explaining the reason code or null if
     * no such explanation is necessary.
     */
    public RegistrationStateChangeEvent( ProtocolProviderService source,
                                         RegistrationState oldValue,
                                         RegistrationState newValue,
                                         int               reasonCode,
                                         String            reason)
    {
        super(source,
              RegistrationStateChangeEvent.class.getName(),
              oldValue,
              newValue);
    }

    /**
     * Returns the provider that has genereted this event
     * @return the provider that generated the event.
     */
    public ProtocolProviderService getProvider()
    {
        return (ProtocolProviderService)getSource();
    }

    /**
     * Returns the status of the provider before this event took place.
     * @return a RegistrationState instance indicating the event the source
     * provider was in before it entered its new state.
     */
    public RegistrationState getOldState()
    {
        return (RegistrationState)super.getOldValue();
    }

    /**
     * Returns the status of the provider after this event took place.
     * (i.e. at the time the event is being dispatched).
     * @return a RegistrationState instance indicating the event the source
     * provider is in after the status change occurred.
     */
    public RegistrationState getNewState()
    {
        return (RegistrationState)super.getNewValue();
    }

    /**
     * Returns a string representation of this event.
     * @return a String containing the name of the event as well as the names
     * of the old and new <tt>RegistrationState</tt>s
     */
    public String toString()
    {
        return "RegistrationStateChangeEvent[ oldState="
            + getOldState().getStateName()
            + "; newState="+ getNewState()+"]";
    }

    /**
     * One of the REASON_XXX fields, indicating the reason code returned by the
     * server in order to explain the state transition.
     *
     * @return a value corresponding toone of the REASON_XXX fields of this
     * class.
     */
    public int getReasonCode()
    {
        return reasonCode;
    }

    /**
     * Returns a (non localized) String containing information further
     * explaining the reason code, or null if no particular reason has been
     * specified.
     *
     * Keep in mind that reason String-s returned by this method are not
     * localized and services such as the user interface should only show them
     * in a "details box". In the rest of the time, such services should consult
     * the error code and provide corresponding, localized, reason phrases.
     *
     * @return a non localized String explaining the reason fo the state
     * transition.
     */
    public String getReason()
    {
        return reason;
    }
}
