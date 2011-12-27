/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.contactlist.contactsource;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.main.contactlist.*;
import net.java.sip.communicator.impl.gui.utils.*;
import net.java.sip.communicator.service.contactsource.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;

/**
 * The <tt>SourceUIContact</tt> is the implementation of the UIContact for the
 * <tt>ExternalContactSource</tt>.
 *
 * @author Yana Stamcheva
 */
public class SourceUIContact
    implements UIContact
{
    /**
     * The corresponding <tt>SourceContact</tt>, on which this abstraction is
     * based.
     */
    private final SourceContact sourceContact;

    /**
     * The corresponding <tt>ContactNode</tt> in the contact list component.
     */
    private ContactNode contactNode;

    /**
     * The parent <tt>UIGroup</tt>.
     */
    private UIGroup uiGroup;

    /**
     * The search strings for this <tt>UIContact</tt>.
     */
    private final List<String> searchStrings = new LinkedList<String>();

    /**
     * Creates an instance of <tt>SourceUIContact</tt> by specifying the
     * <tt>SourceContact</tt>, on which this abstraction is based and the
     * parent <tt>UIGroup</tt>.
     *
     * @param contact the <tt>SourceContact</tt>, on which this abstraction
     * is based
     * @param parentGroup the parent <tt>UIGroup</tt>
     */
    public SourceUIContact( SourceContact contact,
                            UIGroup parentGroup)
    {
        this.sourceContact = contact;
        this.uiGroup = parentGroup;

        if(contact.getContactDetails() != null)
            for(ContactDetail detail : contact.getContactDetails())
            {
                if(detail.getContactAddress() != null)
                    searchStrings.add(detail.getContactAddress());
            }

        searchStrings.add(contact.getDisplayName());

    }

    /**
     * Returns the display name of the underlying <tt>SourceContact</tt>.
     * @return the display name
     */
    public String getDisplayName()
    {
        return sourceContact.getDisplayName();
    }

    /**
     * Returns the parent <tt>UIGroup</tt>.
     * @return the parent <tt>UIGroup</tt>
     */
    public UIGroup getParentGroup()
    {
        return uiGroup;
    }

    /**
     * The parent group of source contacts could not be changed.
     * @param parentGroup the parent group to set
     */
    public void setParentGroup(UIGroup parentGroup)
    {}

    /**
     * Returns -1 to indicate that the source index of the underlying
     * <tt>SourceContact</tt> is unknown.
     * @return -1
     */
    public int getSourceIndex()
    {
        return -1;
    }

    /**
     * Returns null to indicate unknown status of the underlying
     * <tt>SourceContact</tt>.
     * @return null
     */
    public ImageIcon getStatusIcon()
    {
        return new ImageIcon(Constants.getStatusIcon(Constants.OFFLINE_STATUS));
    }

    /**
     * Returns the image corresponding to the underlying <tt>SourceContact</tt>.
     * @param isSelected indicates if the contact is currently selected in the
     * contact list component
     * @param width the desired image width
     * @param height the desired image height
     * @return the image
     */
    public ImageIcon getAvatar(boolean isSelected, int width, int height)
    {
        byte[] image = sourceContact.getImage();

        if ((image != null) && (image.length > 0))
        {
            ImageIcon icon = new ImageIcon(image);

            if (icon.getIconWidth() > width || icon.getIconHeight() > height)
            {
                icon
                    = ImageUtils.getScaledRoundedIcon(
                            icon.getImage(),
                            width, height);
            }
            return icon;
        }
        else
            return null;
    }

    /**
     * Returns the default <tt>ContactDetail</tt> to use for any operations
     * depending to the given <tt>OperationSet</tt> class.
     * @param opSetClass the <tt>OperationSet</tt> class we're interested in
     * @return the default <tt>ContactDetail</tt> to use for any operations
     * depending to the given <tt>OperationSet</tt> class
     */
    public UIContactDetail getDefaultContactDetail(
        Class<? extends OperationSet> opSetClass)
    {
        List<UIContactDetail> details
            = getContactDetailsForOperationSet(opSetClass);

        if (details != null && !details.isEmpty())
            return details.get(0);
        return null;
    }

    /**
     * Returns the underlying <tt>SourceContact</tt> this abstraction is about.
     * @return the underlying <tt>SourceContact</tt>
     */
    public Object getDescriptor()
    {
        return sourceContact;
    }

    /**
     * Returns the display details for the underlying <tt>SourceContact</tt>.
     * @return the display details for the underlying <tt>SourceContact</tt>
     */
    public String getDisplayDetails()
    {
        return sourceContact.getDisplayDetails();
    }

    /**
     * Returns a list of <tt>UIContactDetail</tt>s supporting the given
     * <tt>OperationSet</tt> class.
     * @param opSetClass the <tt>OperationSet</tt> class we're interested in
     * @return a list of <tt>UIContactDetail</tt>s supporting the given
     * <tt>OperationSet</tt> class
     */
    public List<UIContactDetail> getContactDetailsForOperationSet(
        Class<? extends OperationSet> opSetClass)
    {
        List<UIContactDetail> resultList
            = new LinkedList<UIContactDetail>();

        Iterator<ContactDetail> details
            = sourceContact.getContactDetails().iterator();

        while (details.hasNext())
        {
            ContactDetail detail = details.next();

            List<Class<? extends OperationSet>> supportedOperationSets
                = detail.getSupportedOperationSets();

            if ((supportedOperationSets != null)
                    && supportedOperationSets.contains(opSetClass))
            {
                resultList.add(new SourceContactDetail(detail, opSetClass));
            }
        }
        return resultList;
    }

    /**
     * Returns an <tt>Iterator</tt> over a list of strings, which can be used
     * to find this contact.
     * @return an <tt>Iterator</tt> over a list of search strings
     */
    public Iterator<String> getSearchStrings()
    {
        return searchStrings.iterator();
    }

    /**
     * Returns the corresponding <tt>ContactNode</tt> from the contact list
     * component.
     * @return the corresponding <tt>ContactNode</tt>
     */
    public ContactNode getContactNode()
    {
        return contactNode;
    }

    /**
     * Sets the corresponding <tt>ContactNode</tt>.
     * @param contactNode the corresponding <tt>ContactNode</tt>
     */
    public void setContactNode(ContactNode contactNode)
    {
        this.contactNode = contactNode;
    }

    /**
     * The implementation of the <tt>UIContactDetail</tt> interface for the
     * external source <tt>ContactDetail</tt>s.
     */
    private class SourceContactDetail extends UIContactDetail
    {
        /**
         * Creates an instance of <tt>SourceContactDetail</tt> by specifying
         * the underlying <tt>detail</tt> and the <tt>OperationSet</tt> class
         * for it.
         * @param detail the underlying <tt>ContactDetail</tt>
         * @param opSetClass the <tt>OperationSet</tt> class for the
         * preferred protocol provider
         */
        public SourceContactDetail( ContactDetail detail,
                                    Class<? extends OperationSet> opSetClass)
        {
            super(  detail.getContactAddress(),
                    detail.getContactAddress(),
                    detail.getCategory(),
                    detail.getLabels(),
                    null,
                    detail.getPreferredProtocolProvider(opSetClass),
                    detail.getPreferredProtocol(opSetClass));

            ContactSourceService contactSource
                = sourceContact.getContactSource();

            if (contactSource instanceof ExtendedContactSourceService)
            {
                String prefix = ((ExtendedContactSourceService) contactSource)
                    .getPhoneNumberPrefix();

                if (prefix != null)
                    setPrefix(prefix);
            }
        }

        /**
         * Returns null to indicate that this detail doesn't support presence.
         * @return null
         */
        public PresenceStatus getPresenceStatus()
        {
            return null;
        }
    }

    /**
     * Returns the <tt>JPopupMenu</tt> opened on a right button click over this
     * <tt>SourceUIContact</tt>.
     * @return the <tt>JPopupMenu</tt> opened on a right button click over this
     * <tt>SourceUIContact</tt>
     */
    public JPopupMenu getRightButtonMenu()
    {
        return new SourceContactRightButtonMenu(sourceContact);
    }

    /**
     * Returns the tool tip opened on mouse over.
     * @return the tool tip opened on mouse over
     */
    public ExtendedTooltip getToolTip()
    {
        ExtendedTooltip tip = new ExtendedTooltip(
            GuiActivator.getUIService().getMainFrame(), true);

        byte[] avatarImage = sourceContact.getImage();

        if (avatarImage != null && avatarImage.length > 0)
            tip.setImage(new ImageIcon(avatarImage));

        tip.setTitle(sourceContact.getDisplayName());

        String displayDetails = getDisplayDetails();

        if (displayDetails != null)
            tip.addLine(new JLabel[]{new JLabel(getDisplayDetails())});

        try
        {
            List<ContactDetail> details = sourceContact.getContactDetails(
                            ContactDetail.CATEGORY_PHONE);

            if (details != null && details.size() > 0)
                addDetailsToToolTip(details,
                        ContactDetail.CATEGORY_PHONE + "s",
                        tip);

            details = sourceContact.getContactDetails(
                ContactDetail.CATEGORY_EMAIL);

            if (details != null && details.size() > 0)
                addDetailsToToolTip(details,
                        ContactDetail.CATEGORY_EMAIL + "s",
                        tip);

            details = sourceContact.getContactDetails(
                ContactDetail.CATEGORY_INSTANT_MESSAGING);

            if (details != null && details.size() > 0)
                addDetailsToToolTip(details,
                        ContactDetail.CATEGORY_INSTANT_MESSAGING + "s",
                        tip);
        }
        catch (OperationNotSupportedException e)
        {
            // Categories aren't supported. This is the case for history
            // records.
            List<ContactDetail> allDetails = sourceContact.getContactDetails();

            addDetailsToToolTip(allDetails,
                GuiActivator.getResources()
                    .getI18NString("service.gui.CALL_WITH"), tip);
        }

        return tip;
    }

    private void addDetailsToToolTip(   List<ContactDetail> details,
                                        String category,
                                        ExtendedTooltip toolTip)
    {
        ContactDetail contactDetail;

        JLabel categoryLabel = new JLabel(category, null, JLabel.LEFT);
        categoryLabel.setFont(categoryLabel.getFont().deriveFont(Font.BOLD));
        categoryLabel.setForeground(Color.DARK_GRAY);

        toolTip.addLine(null, " ");
        toolTip.addLine(new JLabel[]{categoryLabel});

        Iterator<ContactDetail> detailsIter = details.iterator();
        while (detailsIter.hasNext())
        {
            contactDetail = detailsIter.next();
            Collection<String> labels = contactDetail.getLabels();

            JLabel[] jLabels = new JLabel[labels.size() + 1];
            int i = 0;
            if (labels != null && labels.size() > 0)
            {
                Iterator<String> labelsIter = labels.iterator();
                while(labelsIter.hasNext())
                {
                    JLabel label = new JLabel(labelsIter.next().toLowerCase());
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                    label.setForeground(Color.GRAY);

                    jLabels[i] = label;
                    i++;
                }
            }

            jLabels[i] = new JLabel(contactDetail.getContactAddress());

            toolTip.addLine(jLabels);
        }
    }
}
