/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.contactlist;

import java.util.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.main.contactlist.contactsource.*;
import net.java.sip.communicator.service.contactsource.*;

/**
 * The <tt>FilterQuery</tt> gives information about a current filtering.
 *
 * @author Yana Stamcheva
 */
public class FilterQuery
    implements  ContactQueryListener,
                MetaContactQueryListener
{
    /**
     * The maximum result count for each contact source.
     */
    public static final int MAX_EXTERNAL_RESULT_COUNT = 10;

    /**
     * A listener, which is notified when this query finishes.
     */
    private FilterQueryListener filterQueryListener;

    /**
     * Indicates if the query succeeded, i.e. if any of the filters associated
     * with this query has returned any results.
     */
    private boolean isSucceeded = false;

    /**
     * Indicates if this query has been canceled.
     */
    private boolean isCanceled = false;

    /**
     * Indicates if this query is closed, means no more queries could be added
     * to it. A <tt>FilterQuery</tt>, which is closed knows that it has to wait
     * for a final number of queries to finish before notifying interested
     * parties of the result.
     */
    private boolean isClosed = false;

    /**
     * The list of filter queries.
     */
    private final Map<Object, List<SourceContact>> filterQueries
        = Collections.synchronizedMap(
                new Hashtable<Object, List<SourceContact>>());

    /**
     * Indicates the number of running queries.
     */
    private int runningQueries = 0;

    /**
     * Adds the given <tt>contactQuery</tt> to the list of filterQueries.
     * @param contactQuery the <tt>ContactQuery</tt> to add
     */
    public void addContactQuery(Object contactQuery)
    {
        synchronized (filterQueries)
        {
            // If this filter query has been already canceled and someone wants
            // to add something to it, we just cancel the incoming query and
            // return.
            if (isCanceled)
            {
                cancelQuery(contactQuery);
                return;
            }

            List<SourceContact> queryResults = new ArrayList<SourceContact>();

            if (contactQuery instanceof ContactQuery)
            {
                ContactQuery externalQuery = (ContactQuery) contactQuery;

                List<SourceContact> externalResults
                    = externalQuery.getQueryResults();

                if (externalResults != null && externalResults.size() > 0)
                    queryResults = new ArrayList<SourceContact>(externalResults);

                externalQuery.addContactQueryListener(this);
            }
            else if (contactQuery instanceof MetaContactQuery)
                ((MetaContactQuery) contactQuery).addContactQueryListener(this);

            filterQueries.put(contactQuery, queryResults);
            runningQueries++;
        }
    }

    /**
     * Sets the <tt>isSucceeded</tt> property.
     * @param isSucceeded indicates if this query has succeeded
     */
    public void setSucceeded(boolean isSucceeded)
    {
        this.isSucceeded = isSucceeded;
    }

    /**
     * Indicates if this query has succeeded.
     * @return <tt>true</tt> if this query has succeeded, <tt>false</tt> -
     * otherwise
     */
    public boolean isSucceeded()
    {
        return isSucceeded;
    }

    /**
     * Indicates if this query is canceled.
     * @return <tt>true</tt> if this query is canceled, <tt>false</tt> otherwise
     */
    public boolean isCanceled()
    {
        synchronized (filterQueries)
        {
            return isCanceled;
        }
    }

    /**
     * Cancels this filter query.
     */
    public void cancel()
    {
        synchronized(filterQueries)
        {
            isCanceled = true;

            Iterator<Object> queriesIter = filterQueries.keySet().iterator();

            while (queriesIter.hasNext())
                cancelQuery(queriesIter.next());
        }
    }

    /**
     * Closes this query to indicate that no more contact sub-queries would be
     * added to it.
     */
    public void close()
    {
        isClosed = true;

        if (runningQueries == 0)
            fireFilterQueryEvent();
    }

    /**
     * Sets the given <tt>FilterQueryListener</tt>.
     * @param l the <tt>FilterQueryListener</tt> to set
     */
    public void setQueryListener(FilterQueryListener l)
    {
        filterQueryListener = l;
    }

    /**
     * Notifies the <tt>FilterQueryListener</tt> of the result status of
     * this query.
     */
    private void fireFilterQueryEvent()
    {
        if (filterQueryListener == null)
            return;

        if (isSucceeded)
            filterQueryListener.filterQuerySucceeded(this);
        else
            filterQueryListener.filterQueryFailed(this);
    }

    /**
     * Indicates that a query has changed its status.
     * @param event the <tt>ContactQueryStatusEvent</tt> that notified us
     */
    public void queryStatusChanged(ContactQueryStatusEvent event)
    {
        ContactQuery query = event.getQuerySource();

        // Check if this query is in our filter queries list.
        if (!filterQueries.containsKey(query)
                || event.getEventType() == ContactQuery.QUERY_IN_PROGRESS)
            return;

        removeQuery(query);
    }

    /**
     * Removes the given query from this filter query, updates the related data
     * and notifies interested parties if this was the last query to process.
     * @param query the <tt>ContactQuery</tt> to remove.
     */
    public void removeQuery(ContactQuery query)
    {
        // First set the isSucceeded property.
        if (!isSucceeded() && !query.getQueryResults().isEmpty())
            setSucceeded(true);

        // Then remove the wait result from the filterQuery.
        runningQueries--;
        query.removeContactQueryListener(this);

        // If no queries have rest we notify interested listeners that query
        // has finished.
        if (runningQueries == 0 && isClosed)
            fireFilterQueryEvent();
    }

    /**
     * Indicates that a query has changed its status.
     * @param event the <tt>ContactQueryStatusEvent</tt> that notified us
     */
    public void metaContactQueryStatusChanged(MetaContactQueryStatusEvent event)
    {
        MetaContactQuery query = event.getQuerySource();

        // Check if this query is in our filter queries list.
        if (!filterQueries.containsKey(query))
            return;

        // First set the isSucceeded property.
        if (!isSucceeded() && query.getResultCount() > 0)
            setSucceeded(true);

        // We don't remove the query from our list, because even if the query
        // has finished its GUI part is scheduled in the Swing thread and we
        // don't know anything about these events, so if someone calls cancel()
        // we need to explicitly cancel all contained queries even they are
        // finished.
        runningQueries--;
        query.removeContactQueryListener(this);

        // If no queries have rest we notify interested listeners that query
        // has finished.
        if (runningQueries == 0 && isClosed)
            fireFilterQueryEvent();
    }

    /**
     * Cancels the given query.
     * @param query the query to cancel
     */
    private void cancelQuery(Object query)
    {
        if (query instanceof ContactQuery)
        {
            ContactQuery contactQuery = (ContactQuery) query;
            contactQuery.cancel();
            contactQuery.removeContactQueryListener(
                GuiActivator.getContactList());
            if (!isSucceeded && contactQuery.getQueryResults().size() > 0)
                isSucceeded = true;
        }
        else if (query instanceof MetaContactQuery)
        {
            MetaContactQuery metaContactQuery = (MetaContactQuery) query;
            metaContactQuery.cancel();
            metaContactQuery.removeContactQueryListener(
                GuiActivator.getContactList());
            if (!isSucceeded && metaContactQuery.getResultCount() > 0)
                isSucceeded = true;
        }
    }

    /**
     * Verifies if the given query is contained in this filter query.
     *
     * @param query the query we're looking for
     * @return <tt>true</tt> if the given <tt>query</tt> is contained in this
     * filter query, <tt>false</tt> - otherwise
     */
    public boolean containsQuery(Object query)
    {
        return filterQueries.containsKey(query);
    }

    /**
     * Cancels asynchronous queries after the maximum desired result count is
     * reached.
     *
     * @param query the query we're interested in
     * @param contact the source contact we just received as a result of the
     * given query
     */
    private void contactReceived(ContactQuery query, SourceContact contact)
    {
        List<SourceContact> queryResults = filterQueries.get(query);

        queryResults.add(contact);

        if (queryResults.size() == MAX_EXTERNAL_RESULT_COUNT)
        {
            query.removeContactQueryListener(GuiActivator.getContactList());

            ShowMoreContact moreInfoContact
                = new ShowMoreContact(query, queryResults);

            ContactSourceService contactSource = query.getContactSource();

            GuiActivator.getContactList().addContact(
                query,
                moreInfoContact,
                TreeContactList.getContactSource(contactSource).getUIGroup(),
                false);
        }
    }

    /**
     * Indicates that a contact has been received as a result of a query.
     *
     * @param event the <tt>ContactReceivedEvent</tt> that notified us
     */
    public void contactReceived(ContactReceivedEvent event)
    {
        contactReceived(event.getQuerySource(), event.getContact());
    }

    public void metaContactReceived(MetaContactQueryEvent event) {}

    public void metaGroupReceived(MetaGroupQueryEvent event) {}
}
