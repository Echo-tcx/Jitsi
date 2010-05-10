/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.call;

import java.util.*;

/**
 * The <tt>MissedCallsListener</tt> listens for changes in the missed calls
 * count. It is notified each time when a missed calls is registered by the
 * <tt>CallManager</tt>.
 *
 * @author Yana Stamcheva
 */
public interface MissedCallsListener
{
    /**
     * Indicates the missed calls count has changed.
     * @param missedCalls the new missed calls list
     */
    public void missedCallCountChanged(Collection<MissedCall> missedCalls);
}
