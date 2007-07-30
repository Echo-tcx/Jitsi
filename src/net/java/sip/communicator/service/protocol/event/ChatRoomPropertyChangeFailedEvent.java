/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.service.protocol.event;

import java.beans.*;

import net.java.sip.communicator.service.protocol.*;

/**
 * Dispatched to indicate that a change of a chat room property has failed.
 * The modification of a property could fail, because the implementation
 * doesn't support such a property.
 * 
 * @author Yana Stamcheva
 */
public class ChatRoomPropertyChangeFailedEvent
    extends PropertyChangeEvent
{
    /**
     * Indicates that the current implementation doesn't support the given
     * property.
     */
    public static int PROPERTY_NOT_SUPPORTED = 0;
    
    /**
     * The value of the property.
     */
    private Object propertyValue;
    
    /**
     * The value of the propertythat was expected after the change.
     */
    private Object expectedValue;
    
    /**
     * The reason of the failure.
     */
    private String reason;
    
    /**
     * Indicates why the failure occured.
     */
    private int reasonCode;
    
    /**
     * Creates a <tt>ChatRoomPropertyChangeEvent</tt> indicating that a change
     * has occurred for property <tt>propertyName</tt> in the <tt>source</tt>
     * chat room and that its value has changed from <tt>oldValue</tt> to
     * <tt>newValue</tt>.
     * <p>
     * @param source the <tt>ChatRoom</tt>, to which the propery belongs
     * @param propertyName the name of the property
     * @param propertyValue the value of the property
     * @param expectedValue the extected after the change value of the property
     * @param reasonCode the code indicating the reason for the failure
     * @param reason more detailed explanation of the failure
     */
    public ChatRoomPropertyChangeFailedEvent(   ChatRoom source, 
                                                String propertyName,
                                                Object propertyValue,
                                                Object expectedValue,
                                                int reasonCode,
                                                String reason)
    {
        super(source, propertyName, propertyValue, expectedValue);
        
        this.reasonCode = reasonCode;
        
        this.reason = reason;
    }

    /**
     * Returns the source chat room for this event.
     *
     * @return the <tt>ChatRoom</tt> associated with this
     * event.
     */
    public ChatRoom getSourceChatRoom()
    {
        return (ChatRoom)getSource();
    }
    
    /**
     * Returns the value of the property.
     * 
     * @return the value of the property.
     */
    public Object getPropertyValue()
    {
        return getOldValue(); 
    }
    
    /**
     * Return the expected after the change value of the property.
     * 
     * @return the expected after the change value of the property
     */
    public Object getExpectedValue()
    {
        return getNewValue();
    }
    
    /**
     * Returns the code of the failure. One of the static constants declared in
     * this class.
     * @return the code of the failure. One of the static constants declared in
     * this class
     */
    public int getReasonCode()
    {
        return reasonCode;
    }
    
    /**
     * Returns the reason of the failure.
     * @return the reason of the failure
     */
    public String getReason()
    {
        return reason;
    }
    
    /**
     * Returns a String representation of this event.
     */
    public String toString()
    {
        return "ChatRoomPropertyChangeEvent[type="
            + this.getPropertyName()
            + " sourceRoom="
            + this.getSource().toString()
            + "oldValue="
            + this.getOldValue().toString()
            + "newValue="
            + this.getNewValue().toString()
            + "]";
    }
}
