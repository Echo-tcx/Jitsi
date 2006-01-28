/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.service.configuration.event;

import net.java.sip.communicator.service.configuration.*;
import net.java.sip.communicator.util.xml.*;
import net.java.sip.communicator.impl.configuration.xml.*;

/**
 * A "ConfigurationChange" event gets fired whenever a configuration property
 * changes. Depending on whether the property was constrained or not, the
 * propertyChange or vetoableChange methods get called.
 *
 * @author Emil Ivov
 */
public interface PropertyChangeListener extends java.util.EventListener
{
    /**
     * This method gets called when a bound property is changed.
     * @param evt A PropertyChangeEvent object describing the event source
     *   	and the property that has changed.
     */
    void propertyChange(PropertyChangeEvent evt);

}
