/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.service.configuration;

import java.io.*;
import java.util.*;

import net.java.sip.communicator.service.configuration.event.*;
import net.java.sip.communicator.util.xml.*;

/**
 * The configuration services provides a centralized approach of storing
 * persistent configuration data.
 *
 * @author Emil Ivov
 */
public interface ConfigurationService
{
    /**
     * The name of the property that indicates the name of the directory where
     * SIP Communicator is to store user specific data such as configuration
     * files, message and call history as well as is bundle repository.
     */
    public static final String PNAME_SC_HOME_DIR_NAME
        = "net.java.sip.communicator.SC_HOME_DIR_NAME";

    /**
     * The name of the property that indicates the location of the directory
     * where SIP Communicator is to store user specific data such as
     * configuration files, message and call history as well as is bundle
     * repository.
     */
    public static final String PNAME_SC_HOME_DIR_LOCATION
        = "net.java.sip.communicator.SC_HOME_DIR_LOCATION";


    /**
     * Sets the property with the specified name to the specified value. Calling
     * this method would first trigger a PropertyChangeEvent that will
     * be dispatched to all VetoableChangeListeners. In case no complaints
     * (PropertyVetoException) have been received, the property will be actually
     * changed and a PropertyChangeEvent will be dispatched.
     * <p>
     * @param propertyName the name of the property to change.
     * @param property the new value of the specified property.
     * @throws PropertyVetoException in case the changed has been refused by
     * at least one propertychange listener.
     */
    public void setProperty(String propertyName, Object property)
        throws PropertyVetoException;

    /**
     * Sets the property with the specified name to the specified. Calling
     * this method would first trigger a PropertyChangeEvent that will
     * be dispatched to all VetoableChangeListeners. In case no complaints
     * (PropertyVetoException) have been received, the property will be actually
     * changed and a PropertyChangeEvent will be dispatched. This method also
     * allows the caller to specify whether or not the specified property is a
     * system one.
     * <p>
     * @param propertyName the name of the property to change.
     * @param property the new value of the specified property.
     * @param isSystem specifies whether or not the property being is a System
     *                 property and should be resolved against the system
     *                 property set
     * @throws PropertyVetoException in case the changed has been refused by
     * at least one propertychange listener.
     */
    public void setProperty(String propertyName,
                            Object property,
                            boolean isSystem)
        throws PropertyVetoException;

    /**
     * Returns the value of the property with the specified name or null if no
     * such property exists.
     * @param propertyName the name of the property that is being queried.
     * @return the value of the property with the specified name.
     */
    public Object getProperty(String propertyName);
    
    /**
     * Removes the property with the specified name. Calling
     * this method would first trigger a PropertyChangeEvent that will
     * be dispatched to all VetoableChangeListeners. In case no complaints
     * (PropertyVetoException) have been received, the property will be actually
     * changed and a PropertyChangeEvent will be dispatched.
     * All properties with prefix propertyName will also be removed.
     * <p>
     * @param propertyName the name of the property to change.
     * @param property the new value of the specified property.
     * @throws PropertyVetoException in case the changed has been refused by
     * at least one propertychange listener.
     */
    public void removeProperty(String propertyName)
        throws PropertyVetoException;

    /**
     * Returns a <tt>java.util.List</tt> of <tt>String</tt>s containing the
     * all property names that have the specified prefix. Depending on the value
     * of the <tt>exactPrefixMatch</tt> parameter the method will (when false)
     * or will not (when exactPrefixMatch is true) include property names that
     * have prefixes longer than the specified <tt>prefix</tt> param.
     * <p>
     * Example:
     * <p>
     * Imagine a configuration service instance containing 2 properties only:<br>
     * <code>
     * net.java.sip.communicator.PROP1=value1<br>
     * net.java.sip.communicator.service.protocol.PROP1=value2
     * </code>
     * <p>
     * A call to this method with a prefix="net.java.sip.communicator" and
     * exactPrefixMatch=true would only return the first property -
     * net.java.sip.communicator.PROP1, whereas the same call with
     * exactPrefixMatch=false would return both properties as the second prefix
     * includes the requested prefix string.
     * <p>
     * @param prefix a String containing the prefix (the non dotted non-caps
     * part of a property name) that we're looking for.
     * @param exactPrefixMatch a boolean indicating whether the returned
     * property names should all have a prefix that is an exact match of the
     * the <tt>prefix</tt> param or whether properties with prefixes that
     * contain it but are longer than it are also accepted.
     * @return a <tt>java.util.List</tt>containing all property name String-s
     * matching the specified conditions.
     */
    public List getPropertyNamesByPrefix(String  prefix,
                                         boolean exactPrefixMatch);

    /**
     * Returns the String value of the specified property and null in case no
     * property value was mapped against the specified propertyName, or in
     * case the returned property string had zero length or contained
     * whitespaces only.
     *
     * @param propertyName the name of the property that is being queried.
     * @return the result of calling the property's toString method and null in
     * case there was no vlaue mapped against the specified
     * <tt>propertyName</tt>, or the returned string had zero length or
     * contained whitespaces only.
     */
    public String getString(String propertyName);

    /**
     * Adds a PropertyChangeListener to the listener list. The listener is
     * registered for all properties in the current configuration.
     * <p>
     * @param listener the PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes a PropertyChangeListener from the listener list.
     * <p>
     * @param listener the PropertyChangeListener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Adds a PropertyChangeListener to the listener list for a specific
     * property. In case a property with the specified name does not exist the
     * listener is still added and would only be taken into account from the
     * moment such a property is set by someone.
     * <p>
     * @param propertyName one of the property names listed above
     * @param listener the PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(String propertyName,
                                          PropertyChangeListener listener);

    /**
     * Removes a PropertyChangeListener from the listener list for a specific
     * property. This method should be used to remove PropertyChangeListeners
     * that were registered for a specific property. The method has no effect
     * when called for a listener that was not registered for that specifiec
     * property.
     * <p>
     *
     * @param propertyName a valid property name
     * @param listener the PropertyChangeListener to be removed
     */
    public void removePropertyChangeListener(String propertyName,
                                             PropertyChangeListener listener);

    /**
     * Adds a VetoableChangeListener to the listener list. The listener is
     * registered for all properties in the configuration.
     * <p>
     * @param listener the VetoableChangeListener to be added
     */
    public void addVetoableChangeListener(VetoableChangeListener listener);

    /**
     * Removes a VetoableChangeListener from the listener list.
     * <p>
     *
     * @param listener the VetoableChangeListener to be removed
     */
    public void removeVetoableChangeListener(VetoableChangeListener listener);

    /**
     * Adds a VetoableChangeListener to the listener list for a specific
     * property.
     * <p>
     *
     * @param propertyName one of the property names listed above
     * @param listener the VetoableChangeListener to be added
     */
    public void addVetoableChangeListener(String propertyName,
                                          VetoableChangeListener listener);

    /**
     * Removes a VetoableChangeListener from the listener list for a specific
     * property.
     * <p>
     *
     * @param propertyName a valid property name
     * @param listener the VetoableChangeListener to be removed
     */
    public void removeVetoableChangeListener(String propertyName,
                         VetoableChangeListener listener);

    /**
     * Store the current set of properties back to the configuration file. The
     * name of the configuration file is queried from the system property
     * net.java.sip.communicator.PROPERTIES_FILE_NAME, and is set to
     * sip-communicator.xml in case the property does not contain a valid file
     * name. The location might be one of three possibile, checked in the
     * following order: <br>
     * 1. The current directory. <br>
     * 2. The sip-communicator directory in the user.home
     *    ($HOME/.sip-communicator)
     * 3. A location in the classpath (such as the sip-communicator jar file).
     * <p>
     * In the last case the file is copied to the sip-communicator configuration
     * directory right after being extracted from the classpath location.
     *
     * @throws IOException in case storing the configuration failed.
     */
    public void storeConfiguration()
        throws IOException;

    /**
     * Deletes the current configuration and reloads it from the configuration
     * file.  The
     * name of the configuration file is queried from the system property
     * net.java.sip.communicator.PROPERTIES_FILE_NAME, and is set to
     * sip-communicator.xml in case the property does not contain a valid file
     * name. The location might be one of three possibile, checked in the
     * following order: <br>
     * 1. The current directory. <br>
     * 2. The sip-communicator directory in the user.home
     *    ($HOME/.sip-communicator)
     * 3. A location in the classpath (such as the sip-communicator jar file).
     * <p>
     * In the last case the file is copied to the sip-communicator configuration
     * directory right after being extracted from the classpath location.
     * @throws IOException in case reading the configuration failes
     * @throws XMLException in case parsing the configuration file has failed
     */
    public void reloadConfiguration()
        throws IOException, XMLException;

    /**
     * Removes all locally stored properties leaving an empty configuration.
     * Implementations that use a file for storing properties may simply delete
     * it when this method is called.
     */
    public void purgeStoredConfiguration();

    /**
     * Returns the name of the directory where SIP Communicator is to store user
     * specific data such as configuration files, message and call history
     * as well as is bundle repository.
     *
     * @return the name of the directory where SIP Communicator is to store
     * user specific data such as configuration files, message and call history
     * as well as is bundle repository.
     */
    public String getScHomeDirName();

    /**
     * Returns the location of the directory where SIP Communicator is to store
     * user specific data such as configuration files, message and call history
     * as well as is bundle repository.
     *
     * @return the location of the directory where SIP Communicator is to store
     * user specific data such as configuration files, message and call history
     * as well as is bundle repository.
     */
    public String getScHomeDirLocation();

}
