/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.call.conference;

import java.awt.*;
import java.beans.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.main.call.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.skin.*;

/**
 * The <tt>ConferenceMemberPanel</tt> renders <tt>ConferenceMember</tt> details.
 *
 * @author Yana Stamcheva
 * @author Adam Netocny
 */
public class ConferenceMemberPanel
    extends BasicConferenceParticipantPanel
    implements  PropertyChangeListener,
                Skinnable
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 0L;

    /**
     * The underlying conference member.
     */
    private final ConferenceMember member;

    /**
     * Creates a <tt><ConferenceMemberPanel</tt> by specifying the corresponding
     * <tt>member</tt> that it represents.
     *
     * @param callRenderer the parent call renderer
     * @param member the <tt>ConferenceMember</tt> shown in this panel
     */
    public ConferenceMemberPanel(   CallRenderer callRenderer,
                                    ConferenceMember member)
    {
        super(callRenderer, false);

        this.member = member;

        this.setParticipantName(member.getDisplayName());
        this.setParticipantState(member.getState().toString());

        this.setTitleBackground(
            new Color(GuiActivator.getResources().getColor(
            "service.gui.CALL_MEMBER_NAME_BACKGROUND")));
    }

    /**
     * Returns the underlying <tt>ConferenceMember</tt>.
     *
     * @return the underlying <tt>ConferenceMember</tt>.
     */
    public ConferenceMember getConferenceMember()
    {
        return member;
    }

    /**
     * Updates the name and the state of the conference member whenever notified
     * of a change.
     *
     * @param evt the <tt>PropertyChangeEvent</tt> that notified us of the
     * change
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        String propertyName = evt.getPropertyName();

        if (propertyName.equals(ConferenceMember.DISPLAY_NAME_PROPERTY_NAME))
        {
            String displayName = (String) evt.getNewValue();

            this.setParticipantName(displayName);

            this.revalidate();
            this.repaint();
        }
        else if (propertyName.equals(ConferenceMember.STATE_PROPERTY_NAME))
        {
            ConferenceMemberState state
                = (ConferenceMemberState) evt.getNewValue();

            this.setParticipantState(state.toString());

            this.revalidate();
            this.repaint();
        }
    }

    /**
     * Reloads title background color.
     */
    public void loadSkin()
    {
        this.setTitleBackground(
            new Color(GuiActivator.getResources().getColor(
            "service.gui.CALL_MEMBER_NAME_BACKGROUND")));
    }
}
