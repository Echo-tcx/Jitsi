/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.slick.protocol.icq;

import java.beans.*;
import java.io.*;
import java.util.*;

import net.java.sip.communicator.service.protocol.icqconstants.*;
import net.java.sip.communicator.util.*;
import net.kano.joscar.*;
import net.kano.joscar.flapcmd.*;
import net.kano.joscar.snac.*;
import net.kano.joscar.snaccmd.*;
import net.kano.joscar.snaccmd.conn.*;
import net.kano.joscar.snaccmd.error.*;
import net.kano.joscar.snaccmd.icbm.*;
import net.kano.joscar.snaccmd.loc.*;
import net.kano.joscar.snaccmd.ssi.*;
import net.kano.joscar.tlv.*;
import net.kano.joustsim.*;
import net.kano.joustsim.oscar.*;
import net.kano.joustsim.oscar.State;
import net.kano.joustsim.oscar.oscar.service.bos.*;
import net.kano.joustsim.oscar.oscar.service.buddy.*;
import net.kano.joustsim.oscar.oscar.service.icbm.*;
import net.kano.joustsim.oscar.oscar.service.info.*;
import net.kano.joustsim.oscar.oscar.service.ssi.*;
import net.kano.joustsim.trust.*;

/**
 * An utility that we use to test AIM/ICQ implementations of the
 * ProtocolProviderService. This class implements functionality such as
 * verifying whether a particular user is currently on-line, single message
 * reception and single message sending, and other features that help us verify
 * that icq implementations behave properly.
 *
 * @author Emil Ivov
 * @author Damian Minkov
 */
public class IcqTesterAgent
{
    private static final Logger logger =
        Logger.getLogger(IcqTesterAgent.class);
    /**
     * We use this field to determine whether registration has gone ok.
     */
    private IcbmService icbmService = null;

    /**
     * The AimConnection that the IcqEchoTest user has established with the icq
     * service.
     */
    private AimConnection conn = null;

    /**
     * We use it to wait for registration completion.
     */
    private Object connectionLock = new Object();

    /**
     * This one should actually be in joscar. But since it isn't we might as
     * well define it here.
     */
    public static final long ICQ_ONLINE_MASK = 0x01000000L;

    /**
     * Indicates whether the tester agent is registered (signed on) the icq
     * service.
     */
    private boolean registered = false;

    /**
     * The icqUIN (or AIM screenname) that the tester agent should use to log
     * onto the aim service.
     */
    private Screenname icqUIN = null;

    private AuthCmdFactory authCmdFactory = new AuthCmdFactory();

    /**
     * The IcqTesterAgent constructor that would create a tester agent instance,
     * prepared to sign on line with the specified icq uin.
     *
     * @param icqUinString the icq uin that the tester agent should use when
     * signing online.
     */
    IcqTesterAgent(String icqUinString)
    {
        this.icqUIN = new Screenname(icqUinString);
    }

    /**
     * Registers the echo test user on icq so that it could receive test
     * messages from icq/aim tested stacks.
     *
     * @param password the password corresponding to the icq uin specified in
     * the constructor.
     * @return true if registration was successful and false otherwise.
     */
    public boolean register(String password)
    {
        if(registered)
            return true;

        DefaultAppSession session = new DefaultAppSession();

        AimSession aimSession =
            session.openAimSession(icqUIN);
        aimSession.openConnection(
            new AimConnectionProperties(
                icqUIN, password));

        conn = aimSession.getConnection();

        conn.addStateListener(new AimConnStateListener());
        conn.getBuddyInfoManager().addGlobalBuddyInfoListener(new GlobalBuddyListener());

        conn.connect();

        synchronized(connectionLock){
            try{connectionLock.wait(10000);}catch(InterruptedException ex){}
        }

        if (icbmService == null){
            //maybe throw an exception here
            return (registered = false);
        }

        //conn.getSsiService()
        //    .getBuddyList().addRetroactiveLayoutListener(new RetroListener());
        conn.getBuddyService().addBuddyListener(new BuddyListener());

        return (registered = true );
    }

    /**
     * The method would delete all existing buddies on its current server stored
     * contact list and fill it in again as specified by the listContents
     * hashtable. IMPORTANT - Note that this method would completely ERASE
     * any existing contacts in the account currently used by the tester agent
     * to log in.
     *
     * @param listContents a Hashtable that must contain a description of the
     * contact list such as the caller would like it to be (groupnNawhere copy
     * of the initialized contact list would be stored, mapping group names to
     * lists of contact identifiers
     * (screennames).
     */
    public void initializeBuddyList(Hashtable listContents)
    {
        logger.debug("Will Create the following contact list:\n"+ listContents);
        MutableBuddyList joustSimBuddyList
            = (MutableBuddyList)conn.getSsiService().getBuddyList();

        //First empty the existing contact list.
        List groups = joustSimBuddyList.getGroups();

        Iterator groupsIter = groups.iterator();
        while (groupsIter.hasNext())
        {
            Group group = (Group)groupsIter.next();
            joustSimBuddyList.deleteGroupAndBuddies(group);
        }

        //Now insert all items from the listContents hashtable if they're not
        //already there.
        Enumeration newGroupsEnum = listContents.keys();

        LayoutEventCollector evtCollector = new LayoutEventCollector();

        //go over all groups in the contactsToAdd table
        while (newGroupsEnum.hasMoreElements())
        {
            String groupName = (String) newGroupsEnum.nextElement();
            logger.debug("Will add group " + groupName);

            //first clear any previously registered groups and then add the
            //layout listenet to the buddy list.
            evtCollector.addedGroups.removeAllElements();
            joustSimBuddyList.addLayoutListener(evtCollector);

            joustSimBuddyList.addGroup(groupName);

            //wait for a notification from the aim server that the group has
            //been added
            evtCollector.waitForANewGroup(10000);
            joustSimBuddyList.removeLayoutListener(evtCollector);

            //now see if it all worked ok and if yes get a ref to the newly
            //added group.
            MutableGroup newlyCreatedGroup = null;

            if (evtCollector.addedGroups.size() == 0
                || (newlyCreatedGroup
                        = (MutableGroup)evtCollector.addedGroups.get(0))
                            == null)
                throw new NullPointerException(
                    "Couldn't create group " + groupName);

            Iterator contactsToAddToThisGroup
                = ( (List) listContents.get(groupName)).iterator();
            while (contactsToAddToThisGroup.hasNext()){
                String screenname = (String) contactsToAddToThisGroup.next();

                //remove all buddies captured by the event collector so far
                //then register it as a listener
                evtCollector.addedBuddies.removeAllElements();
                joustSimBuddyList.addLayoutListener(evtCollector);


                logger.debug("Will add buddy " + screenname);
                newlyCreatedGroup.addBuddy( screenname);

                //wait for a notification from the aim server that the buddy has
                //been added
                evtCollector.waitForANewBuddy(10000);
                joustSimBuddyList.removeLayoutListener(evtCollector);

                //now see if it all worked ok and if yes get a ref to the newly
                //added group.
                if (evtCollector.addedBuddies.size() == 0
                    || evtCollector.addedBuddies.get(0) == null)
                {
                    //We didn't get an event ... let's see that the new buddy
                    //is really not there and if that is the case throw an exs
                    if(findBuddyInBuddyList(joustSimBuddyList, screenname) == null)
                        throw new NullPointerException(
                            "Couldn't add buddy " + screenname);
                }
            }
        }
    }

    /**
     * Sends to <tt>buddy</tt> a notification that our typing state has now
     * changed to indicated by <tt>notif</tt>.
     * @param buddy the screenname of the budy that we'd like to notify.
     * @param state the typing state that we'd like to send to the specified
     * buddy.
     */
    public void sendTypingNotification(String buddy, TypingState state)
    {
        conn.getIcbmService().getImConversation(new Screenname(buddy))
            .setTypingState(state);
    }

    /**
     * Adds a typing listener that would receive joust sim based on typing
     * notifications received from <tt>buddy</tt>
     * @param buddy the screenname of the buddy that we'd like to receive
     * notifications from.
     * @param l the <tt>ConversationListener</tt> (which also needs to be a
     * TypingListener) that would be registered for typing notifications
     * @throws ClassCastException if <tt>l</tt> is only an instance of
     * <tt>ConversationListener</tt> without implementing
     * <tt>TypingListener</tt>
     */
    public void addTypingStateInfoListenerForBuddy( String buddy,
                                                    ConversationListener l)
        throws ClassCastException
    {
        if (! (l instanceof TypingListener))
            throw new ClassCastException(
                "In order to receive typing notifications a typing listener "
                +"needs to also implement " + TypingListener.class.getName());

        conn.getIcbmService().getImConversation(new Screenname(buddy))
            .addConversationListener(l);
    }

    /**
     * Removes <tt>l</tt> so that it won't receive further typing events for
     * <tt>buddy</tt>.
     * @param buddy the screenname of the buddy that we'd like to stop receiving
     * notifications from.
     * @param l the <tt>ConversationListener</tt> to remove
     */
    public void removeTypingStateInfoListenerForBuddy( String buddy,
                                                       ConversationListener l)
    {
        conn.getIcbmService().getImConversation(new Screenname(buddy))
            .removeConversationListener(l);
    }

    /**
     * Sends <tt>body</tt> to <tt>buddy</tt>  as an instant message
     * @param buddy the screenname of the budy that we'd like to send our msg to.
     * @param body the content of the message to send.
     */
    public void sendMessage(String buddy, String body)
    {
        conn.getIcbmService().getImConversation(new Screenname(buddy))
            .sendMessage(new SimpleMessage(body));

        //the aim server doesn't like fast consecutice messages
        try{Thread.sleep(600);}catch (InterruptedException ex){}
    }

    /**
     * Registers <tt>listener</tt> as an ImConversationListener so that it would
     * receive messages coming from <tt>buddy</tt>.
     * @param buddy the screenname of the buddy that we'd like to listen to.
     * @param listener the <tt>ImConversationListener</tt> to register.
     */
    public void addConversationListener(String               buddy,
                                        ConversationListener listener)
    {
        conn.getIcbmService().getImConversation(new Screenname(buddy))
            .addConversationListener(listener);
    }

    /**
     * Removes <tt>listener</tt> as an ImConversationListener so that it won't
     * receive further messages coming from <tt>buddy</tt>.
     * @param buddy the screenname of the buddy that we'd like to unregister
     * from.
     * @param listener the <tt>ImConversationListener</tt> to remove.
     */
    public void removeConversationListener(String               buddy,
                                           ConversationListener listener)
    {
        conn.getIcbmService().getImConversation(new Screenname(buddy))
            .removeConversationListener(listener);
    }

    /**
     * Tries to find the buddy with screenname screenname in the given buddy
     * list
     * @param list the BuddyList where to look for the buddy
     * @param screenname  the screen name of the buddy we're looking for
     * @return a ref to the Buddy we're looking for or null if no such buddy
     * was found.
     */
    private Buddy findBuddyInBuddyList(BuddyList list, String screenname)
    {
        Iterator groups = list.getGroups().iterator();

        while (groups.hasNext())
        {
            Group group = (Group) groups.next();
            List buddies = group.getBuddiesCopy();
            for (int i = 0; i < buddies.size(); i++)
            {
                Buddy buddy = (Buddy)buddies.get(i);
                if(buddy.getScreenname().getFormatted().equals(screenname))
                    return buddy;
            }
        }
        return null;
    }
    /**
     * Unregisters from (signs off) the ICQ service.
     */
    public void unregister()
    {
        if(!registered)
            return;
        conn.disconnect(true);
        registered = false;
    }

    /**
     * All this listener does is wait for an event coming from oscar.jar and
     * indicating that registration has been successful.
     */
    private class AimConnStateListener implements StateListener
    {
        public synchronized void handleStateChange(StateEvent event)
        {
            synchronized( connectionLock ) {
                AimConnection conn = event.getAimConnection();
                logger.debug("EchoUser change state from:"
                             + event.getOldState() + " to " + event.getNewState());
                if (event.getNewState() == State.ONLINE)
                {
                    icbmService = conn.getIcbmService();
                    connectionLock.notifyAll();

                    icbmService.getOscarConnection().getSnacProcessor().
                        getCmdFactoryMgr().getDefaultFactoryList().
                        registerAll(authCmdFactory);

                    icbmService.getOscarConnection().getSnacProcessor().
                        addGlobalResponseListener(authCmdFactory);
                }
                else if (event.getNewState() == State.FAILED
                         || event.getNewState() == State.DISCONNECTED)
                {
                    logger.error("AIM Connection DISCONNECTED for "
                                 + getIcqUIN() + "!");
                    connectionLock.notifyAll();
                }
            }
        }
    }

    /**
     * Returns the on-line status of the user with the specified screenname.
     * @param screenname the screenname of the user whose status we're
     *        interested in.
     * @return a PresenceStatus (one of the IcqStatusEnum static fields)
     * indicating the the status of the specified buddy.
     *
     * @throws java.lang.IllegalStateException if the method is called before
     *         the IcqTesterAgent has been registered with (signed on) the
     *         IcqService.
     */
    public IcqStatusEnum getBuddyStatus(String screenname)
        throws IllegalStateException
    {
        if ( !registered )
            throw new IllegalStateException(
                "You need to register before querying a buddy's status");

        StatusResponseRetriever responseRetriever =
            new StatusResponseRetriever();

        GetInfoCmd getInfoCmd =
            new GetInfoCmd(GetInfoCmd.CMD_NEW_GET_INFO | GetInfoCmd.FLAG_AWAYMSG | GetInfoCmd.FLAG_INFO,
                           new Screenname(screenname).getFormatted());


        conn.getInfoService()
            .sendSnacRequest(getInfoCmd, responseRetriever);

        synchronized(responseRetriever.waitingForResponseLock)
        {
            try{
                logger.debug("waiting to receive status for " + screenname);
                responseRetriever.waitingForResponseLock.wait(100000);
            }
            catch (InterruptedException ex){
                logger.debug("Couldn't wait upon a response retriver", ex);
            }
        }

        logger.debug("Done. we'll return status "  + responseRetriever.status);

        return responseRetriever.status == null
                    ? IcqStatusEnum. OFFLINE
                    : responseRetriever.status;
    }

    /**
     * Converts the specified icqstatus to on of the ICQ_STATUS string fields
     * of this class
     * @param icqStatus the icqStatus as retured in FullUserInfo by the joscar
     *        stack
     * @param returnOnMinus1 specifies the value that should be returned if
     * icqStatus is on its default joust sim value "-1".
     * @return the IcqStatusEnum instance that best corresponds to the "long"
     * icqStatus parameter.
     */
    private static IcqStatusEnum icqStatusLongToString(long icqStatus,
                                                IcqStatusEnum returnOnMinus1)
    {
        if (icqStatus == -1 )
        {
            return returnOnMinus1;
        }
        else if ( (icqStatus & FullUserInfo.ICQSTATUS_AWAY ) != 0)
        {
            return IcqStatusEnum.AWAY;
        }
        else if ( (icqStatus & FullUserInfo.ICQSTATUS_DND ) != 0)
        {
            return IcqStatusEnum.DO_NOT_DISTURB;
        }
        else if ( (icqStatus & FullUserInfo.ICQSTATUS_FFC ) != 0)
        {
            return IcqStatusEnum.FREE_FOR_CHAT;
        }
        else if ( (icqStatus & FullUserInfo.ICQSTATUS_INVISIBLE ) != 0)
        {
            return IcqStatusEnum.INVISIBLE;
        }
        else if ( (icqStatus & FullUserInfo.ICQSTATUS_NA ) != 0)
        {
            return IcqStatusEnum.NOT_AVAILABLE;
        }
        else if ( (icqStatus & FullUserInfo.ICQSTATUS_OCCUPIED ) != 0)
        {
            return IcqStatusEnum.OCCUPIED;
        }
        else if ((icqStatus & ICQ_ONLINE_MASK) == 0 )
        {
            return IcqStatusEnum.OFFLINE;
        }

        return IcqStatusEnum.ONLINE;
    }

    /**
     * The StatusResponseRetriever is used as a one time handler for responses
     * to requests sent through the sendSnacRequest method of one of joustsim's
     * Services. The StatusResponseRetriever would ignore everything apart from
     * the first response, which will be stored in the status field. In the
     * case of a timeout, the status would remain null. Both a response and
     * a timeout would make the StatusResponseRetriever call its notifyAll
     * method so that those that are waiting on it be notified.
     */
    private static class StatusResponseRetriever extends SnacRequestAdapter
    {
            private boolean ran = false;
            private IcqStatusEnum status = null;
            public Object waitingForResponseLock = new Object();


            public void handleResponse(SnacResponseEvent e) {
                SnacCommand snac = e.getSnacCommand();
                logger.debug("Received a response to our status request: " + snac);

                synchronized(this) {
                    if (ran) return;
                    ran = true;
                }

                Object value = null;
                if (snac instanceof UserInfoCmd)
                {
                    UserInfoCmd uic = (UserInfoCmd) snac;

                    FullUserInfo userInfo = uic.getUserInfo();
                    if (userInfo != null)
                    {
                        //if we got a UserInfoCmd and not a SnacError then we
                        //are certain the user is not offline and we specify
                        //the second (defaultReturn) param accordingly.
                        this.status =
                            icqStatusLongToString(userInfo.getIcqStatus(),
                                                  IcqStatusEnum.ONLINE);

                        logger.debug("status is " + status +"="
                                     + userInfo.getIcqStatus());

                        List eInfoBlocks = userInfo.getExtraInfoBlocks();
                        if(eInfoBlocks != null){
                            System.out.println("printing extra info blocks ("
                                               + eInfoBlocks.size() + ")");

                            for (int i = 0; i < eInfoBlocks.size(); i++)
                            {
                                ExtraInfoBlock block
                                    = (ExtraInfoBlock) eInfoBlocks.get(i);
                                System.out.println("block.toString()="
                                    + block.toString()); ;

                            }
                        }
                        else
                            System.out.println("no extra info.");
                        synchronized(waitingForResponseLock){
                            waitingForResponseLock.notifyAll();
                        }
                    }
                }
                else if( snac instanceof SnacError)
                {
                    //this is most probably a CODE_USER_UNAVAILABLE, but
                    //whatever it is it means that to us the buddy in question
                    //is as good as offline so leave status at -1 and notify.
                    this.status = IcqStatusEnum.OFFLINE;
                    logger.debug("status is" + status);
                    synchronized(waitingForResponseLock){
                        waitingForResponseLock.notifyAll();
                    }
                }

            }

            public void handleTimeout(SnacRequestTimeoutEvent event) {
                synchronized(this) {
                    if (ran) return;
                    ran = true;
                }

                synchronized(waitingForResponseLock)
                {
                    waitingForResponseLock.notifyAll();
                }
            }
    }

    /**
     * Returns a string representation of the UIN used by the tester agent to
     * signon on the icq network.
     * @return a String containing the icq UIN used by the tester agent to
     * signon on the AIM network.
     */
    public String getIcqUIN()
    {
        return icqUIN.getFormatted();
    }

    /**
     * Causes the tester agent to enter the specified icq status and returns
     * only after receiving a notification from tha AIM server that the new
     * status has been successfully published.
     * @param icqStatus the icq status to enter
     * @return true if the status change has succeeded and the corresponding bos
     * event was received and false otherwise.
     */
    public boolean enterStatus(long icqStatus)
    {
        //first init the guy that'll tell us that it's ok.
        BosEventNotifier bosEventNotifier = new BosEventNotifier();
        conn.getBosService().addMainBosServiceListener(bosEventNotifier);

        //do the state switch
        synchronized(bosEventNotifier.infoLock ){
            conn.getBosService().sendSnac(new SetExtraInfoCmd(icqStatus));

            try{bosEventNotifier.infoLock.wait(10000);}
                catch (InterruptedException ex){logger.debug("Strange!");}

            conn.getBosService().removeMainBosServiceListener(bosEventNotifier);

            if(bosEventNotifier.lastUserInfo == null){
                logger.debug("Status change was not confirmed by AIM server.");
                return false;
            }

            return true;
        }
    }

    /**
     * Queries the AIM server for our own status and returns accordingly.
     * @return the IcqStatusEnum instance corresponding to our own status.
     */
    public IcqStatusEnum getPresneceStatus()
    {
        return getBuddyStatus(getIcqUIN());
    }

    public net.java.sip.communicator.slick.protocol.icq.IcqTesterAgent.
        AuthCmdFactory getAuthCmdFactory()
    {
        return authCmdFactory;
    }

    /**
     * A bos listener is the way of receiving notifications of changes in our
     * own state or info. This class allows others to wait() for Bos events.
     * The class defines an extraInfoLock and an infoLock both of which are
     * notifyAll()ed whenever a corresponding event is received
     */
    private static class BosEventNotifier implements MainBosServiceListener
    {
        public Object extraInfoLock = new Object();
        public Object infoLock = new Object();

        public List lastExtraInfos = null;
        public FullUserInfo lastUserInfo = null;

        /**
         * Saves the extraInfos list and calls a notifyAll on the extraInfoLock
         * @param extraInfos the list of extraInfos that the AIM server sent
         */
        public void handleYourExtraInfo(List extraInfos)
        {
            logger.debug("Bosiat.extrainfo=" + extraInfos);
            synchronized(extraInfoLock){
                lastExtraInfos = extraInfos;
                extraInfoLock.notifyAll();
            }
        }

        /**
         * Saves the full user info and calls a notifyAll on the infoLock
         * @param service the source bos service
         * @param userInfo the FullUserInfo as received from the aim server.
         */
        public void handleYourInfo(MainBosService service,
                                   FullUserInfo userInfo)
        {
            logger.debug("Bosiat.yourinfo=" + userInfo);
            synchronized(infoLock){
                lastUserInfo = userInfo;
                infoLock.notifyAll();
            }
        }


    }

    /**
     * We use this class to collect and/or wait for events generated upon
     * modification of a server stored contact list.
     */
    private class LayoutEventCollector
        implements BuddyListLayoutListener
    {
        public Vector addedGroups  = new Vector();
        public Vector addedBuddies = new Vector();
        public Vector removedBuddies = new Vector();

        /**
         * The method would wait until at least one new buddy is collected by
         * this collector or the specified number of milliseconds have passed.
         * @param milliseconds the maximum number of millisseconds to wait for
         * a new buddy before simply bailing out.
         */
        public void waitForANewBuddy(int milliseconds)
        {
            synchronized (this.addedBuddies){
                if ( !addedBuddies.isEmpty()){
                    return;
                }
                try{
                    this.addedBuddies.wait(milliseconds);
                }
                catch (InterruptedException ex){
                    logger.warn("A strange thing happened while waiting", ex);
                }
            }
        }

        /**
         * The method would wait until at least one new group is collected by
         * this collector or the specified number of milliseconds have passed.
         * @param milliseconds the maximum number of millisseconds to wait for
         * a new group before simply bailing out.
         */
        public void waitForANewGroup(int milliseconds)
        {
            synchronized (this.addedGroups){
                if ( !addedGroups.isEmpty()){
                    return;
                }
                try{
                    this.addedGroups.wait(milliseconds);
                }
                catch (InterruptedException ex){
                    logger.warn("A strange thing happened while waiting", ex);
                }
            }
        }

        public void waitForRemovedBuddy(int milliseconds)
        {
            synchronized (this.removedBuddies)
            {
                if (!removedBuddies.isEmpty())
                {
                    return;
                }
                try
                {
                    this.removedBuddies.wait(milliseconds);
                }
                catch (InterruptedException ex)
                {
                    logger.warn("A strange thing happened while waiting", ex);
                }
            }
        }


        /**
         * Registers a reference to the group that has just been created and
         * call a notifyAll() on this.
         * @param list the BuddyList where this is happening (unused).
         * @param oldItems List (unused)
         * @param newItems List (unused)
         * @param group a reference to the Group that has just been created.
         * @param buddies a List of the buddies created by this group (unused).
         */
        public void groupAdded(BuddyList list, List oldItems,
                               List newItems,
                               Group group, List buddies)
        {
            logger.debug("A group was added gname is=" + group.getName());
            synchronized(this.addedGroups){
                this.addedGroups.add(group);
                this.addedGroups.notifyAll();
            }
        }

        /**
         * Registers a reference to the newly added buddy and calls a
         * notifyAll() on this.
         * @param list the BuddyList where this is happening (unused).
         * @param group the Group where the buddy was added (unused).
         * @param oldItems List (unused)
         * @param newItems List (unused)
         * @param buddy a reference to the newly added Buddy.
         */
        public void buddyAdded(BuddyList list, Group group, List oldItems,
            List newItems, Buddy buddy)
        {
            logger.debug("A buddy ("+buddy.getScreenname()
                         +")was added to group " + group.getName());
            synchronized(this.addedBuddies){
                this.addedBuddies.add(buddy);
                this.addedBuddies.notifyAll();
            }
        }

        //we don't use this one so far.
        public void groupsReordered(BuddyList list, List oldOrder, List newOrder)
        {
            logger.debug("groupsReordered");
        }

        //we don't use this one so far.
        public void groupRemoved(BuddyList list,
            List oldItems, List newItems, Group group)
        {
            logger.debug("removedGroup="+group.getName());
        }

        // we don't use this one so far.
        public void buddyRemoved(BuddyList list, Group group,
                                 List oldItems,
                                 List newItems,
                                 Buddy buddy)
        {
            logger.debug("removed buddy=" + buddy);
        }

        //we don't use this one
        public void buddiesReordered(BuddyList list, Group group,
                                     List oldBuddies,
                                     List newBuddies)
        {
            logger.debug("buddiesReordered in group " + group.getName());
        }
    }

//------------- other utility stuff that is not really very used ---------------
    private class BuddyListener implements BuddyServiceListener{
        public void gotBuddyStatus(BuddyService service, Screenname buddy,
                            FullUserInfo info)
        {
            System.out.println("BuddyListener.gotBuddyStatus " + buddy.toString()
                               +" and status is : " + info.getIcqStatus());
            List eInfoBlocks = info.getExtraInfoBlocks();
            if (eInfoBlocks != null)
            {
                System.out.println("printing extra info blocks ("
                                   + eInfoBlocks.size() + ")");

                for (int i = 0; i < eInfoBlocks.size(); i++)
                {
                    ExtraInfoBlock block
                        = (ExtraInfoBlock) eInfoBlocks.get(i);
                    System.out.println("block.toString()="
                                       + block.toString()); ;

                }
            }
            else
                logger.trace("no extra info.");
        }

        public void buddyOffline(BuddyService service, Screenname buddy)
        {
            System.out.println("BuddyListener.buddyOffline " + buddy.toString());
        }
    }

    private class GlobalBuddyListener implements GlobalBuddyInfoListener
    {
        public void buddyInfoChanged(BuddyInfoManager manager, Screenname buddy,
                                     BuddyInfo info, PropertyChangeEvent event)
        {
            System.out.println("GlobalBuddyListener.buddyInfoChanged: "
                               + "propN= " + event.getPropertyName()
                               + " for buddy: " + buddy.toString()
                               + " info.isOnline()= " + info.isOnline()
                               + " info.statusMessage=" + info.getStatusMessage()
                                + " info.statusMessage=" + info.getAwayMessage() );
            System.out.println("info=" + info);
        }

        public void newBuddyInfo(BuddyInfoManager manager, Screenname buddy,
                                 BuddyInfo info)
        {
            System.out.println( "GlobalBuddyListener.newBuddyInfo: "
                               + buddy.toString()
                               + " info.isOnline()= " + info.isOnline()
                               + " info.statusMessage=" + info.getStatusMessage()
                                + " info.statusMessage=" + info.getAwayMessage() );
            System.out.println("info=" + info);
        }

        public void receivedStatusUpdate(BuddyInfoManager manager,
                                         Screenname buddy, BuddyInfo info)
        {
            System.out.println("GlobalBuddyListener.receivedStatusUpdate "
                               + buddy.toString()
                               + " info.isOnline()= " + info.isOnline()
                               + " info.statusMessage=" + info.getStatusMessage()
                                + " info.statusMessage=" + info.getAwayMessage() );
            System.out.println("info=" + info);
        }

    }

    private class ServiceListener implements OpenedServiceListener
    {

        public void closedServices(AimConnection conn, Collection services)
        {

        }

        public void openedServices(AimConnection conn, Collection services)
        {
            conn.getBuddyInfoManager().addGlobalBuddyInfoListener(new GlobalBuddyListener());
            conn.getBuddyService().addBuddyListener(new BuddyListener());
        }

    }

    private static class InfoRespListener  implements InfoResponseListener{

            public void handleAwayMessage(InfoService service, Screenname buddy,
                                          String awayMessage)
            {
                System.out.println("InfoResponseListener.handleAwayMessage " + awayMessage);
            }

            public void handleCertificateInfo(InfoService service,
                                              Screenname buddy,
                                              BuddyCertificateInfo certInfo)
            {
                System.out.println("InfoResponseListener.handleCertificateInfo " + certInfo);
            }

            public void handleDirectoryInfo(InfoService service,
                                            Screenname buddy, DirInfo dirInfo)
            {
                System.out.println("InfoResponseListener.handleDirectoryInfo " + dirInfo);
            }

            public void handleUserProfile(InfoService service, Screenname buddy,
                                          String userInfo)
            {
                System.out.println("InfoResponseListener.handleUserProfile " + userInfo);
            }

            public void handleInvalidCertificates(InfoService service,
                                                  Screenname buddy,
                                                  CertificateInfo origCertInfo,
                                                  Throwable ex)
            {
                System.out.println("InfoResponseListener.handleInvalidCertificates " + origCertInfo);
            }

        }

    private class RetroListener
        implements BuddyListLayoutListener, GroupListener
    {
        public void groupsReordered(BuddyList list, List oldOrder,
                             List newOrder)
        {
            System.out.println("        RetroListener.groupReordered");
        }

        public void groupAdded(BuddyList list, List oldItems,
                        List newItems,
                        Group group, List buddies)
        {
            System.out.println("RetroListener.groupAdded");
            System.out.println("    group.name is="+group.getName());
            System.out.println("index="+newItems.indexOf(group));
            for (int i = 0; i < buddies.size(); i++){

                System.out.println("        buddy is="
                             +((Buddy)buddies.get(i))
                                .getScreenname().getFormatted());
                Buddy b = ((Buddy)buddies.get(i));
                conn.getBuddyInfoTracker().addTracker(b.getScreenname(),
                    new BuddyInfoTrackerListener(){});
            }

            group.addGroupListener(this);
        }

        public void groupRemoved(BuddyList list, List oldItems,
                          List newItems,
                          Group group)
        {
            System.out.println("        RetroListener.groupRemoved");
        }

        public void buddyAdded(BuddyList list, Group group, List oldItems,
                        List newItems,
                        Buddy buddy)
        {
            System.out.println("        RetroListener.buddyAdded="+buddy);
        }

        public void buddyRemoved(BuddyList list, Group group,
                          List oldItems,
                          List newItems,
                          Buddy buddy)
        {
            System.out.println("        RetroListener.buddyRemoved"+buddy);
        }

        public void buddiesReordered(BuddyList list, Group group,
                              List oldBuddies,
                              List newBuddies)
        {
            System.out.println("        RetroListener.buddiesReordered");
        }

        public void groupNameChanged(Group group, String oldName,
                                     String newName)
        {
            System.out.println(
                "        RetroListener.GroupListener.groupNameChanged. old is="
                + oldName
                + " new is="
                + newName
                + " group is="+group.getName());
            System.out.println("GroupContains="+group.getBuddiesCopy());

        }

    }

////////////////////////// ugly unused testing code //////////////////////////
    private RetroListener rl = new RetroListener();
    public static void main(String[] args) throws Throwable
    {
java.util.logging.Logger.getLogger("net.kano").setLevel(java.util.logging.Level.FINEST);
        IcqTesterAgent icqtests = new IcqTesterAgent("319305099");
        if (!icqtests.register("6pC0mmtt"))
        {
            System.out.println("registration failed"); ;
            return;
        }
        MainBosService bos =  icqtests.conn.getBosService();
        Thread.sleep(1000);
        icqtests.conn.getSsiService()
            .getBuddyList().addRetroactiveLayoutListener(icqtests.rl);
        Thread.sleep(1000);
        System.out.println("\n\nr u ready?");
        Thread.sleep(3000);

        java.util.logging.Logger.getLogger("net.kano").setLevel(java.util.logging.Level.FINEST);

        MutableBuddyList list = icqtests.conn.getSsiService().getBuddyList();

        MutableGroup dupeGroup = null;
        MutableGroup grpGroup = null;
        Buddy buddyToMove = null;

        List groups = list.getGroups();
        Iterator groupsIter = groups.iterator();
        while(groupsIter.hasNext())
        {
            MutableGroup group = (MutableGroup)groupsIter.next();
            if (group.getName().equals("dupe"))
                dupeGroup = group;
            if (group.getName().equals("grp"))
                grpGroup = group;
            List buddies = group.getBuddiesCopy();
            System.out.println("Printing buddies for group " + group.getName());
            Thread.sleep(1000);
            for (int i = 0; i < buddies.size(); i++)
            {
                Buddy buddy = (Buddy) buddies.get(i);
                System.out.println(buddy.getScreenname());
                if (buddy.getScreenname().getFormatted().equals("201345337"))
                    buddyToMove = buddy;
            }
        }

        System.out.println();System.out.println();System.out.println();System.out.println();System.out.println();System.out.println();System.out.println();System.out.println();System.out.println();
        System.out.println("will move buddyyyyyyyyyy");
        Thread.sleep(5000);
        List listToMove = new ArrayList();
        listToMove.add(buddyToMove);
        list.moveBuddies(listToMove, grpGroup);
        System.out.println("MOved i sega triabva da doidat eventi ");
        Thread.sleep(50000);

        //find the buddy again.
        Buddy movedBuddy = null;
        groupsIter = list.getGroups().iterator();
        while(groupsIter.hasNext())
        {
            MutableGroup group = (MutableGroup)groupsIter.next();
            List buddies = group.getBuddiesCopy();
            for (int i = 0; i < buddies.size(); i++)
            {
                Buddy buddy = (Buddy) buddies.get(i);
                if (buddy.getScreenname().getFormatted().equals("201345337"))
                    movedBuddy = buddy;
            }
        }

        if (buddyToMove == movedBuddy)
            System.out.println("hahaha");

    }

    private class TestSnacCmd  extends SnacCommand
    {
        /** The SNAC family code for the location family. */
        public static final int FAMILY_ICQ = 0x0015;

        protected TestSnacCmd(int command) {
            super(FAMILY_ICQ, command);
        }

        /**
         * Writes this command's SNAC data block to the given stream. The SNAC data
         * block is the data after the first ten bytes of a SNAC packet.
         *
         * @param out the stream to which to write the SNAC data
         * @throws IOException if an I/O error occurs
         */
        public void writeData(OutputStream out) throws IOException
        {

        }
    }

    public void deleteBuddy(String screenname)
    {
        logger.debug("Will delete buddy : " + screenname);
        MutableBuddyList joustSimBuddyList
            = (MutableBuddyList)conn.getSsiService().getBuddyList();

        LayoutEventCollector evtCollector = new LayoutEventCollector();
        joustSimBuddyList.addLayoutListener(evtCollector);

        List grList = joustSimBuddyList.getGroups();
        boolean isDeleted = false;
        Iterator iter = grList.iterator();
        while (iter.hasNext())
        {
            MutableGroup item = (MutableGroup) iter.next();

            List bs = item.getBuddiesCopy();
            Iterator iter1 = bs.iterator();
            while (iter1.hasNext())
            {
                Buddy b = (Buddy) iter1.next();
                if(b.getScreenname().getFormatted().equals(screenname))
                {
                    item.deleteBuddy(b);
                    isDeleted = true;
                }
            }

            if(isDeleted)
                break;
        }

        evtCollector.waitForRemovedBuddy(10000);
        joustSimBuddyList.removeLayoutListener(evtCollector);
    }

    public void addBuddy(String screenname)
    {
        logger.debug("Will add buddy : " + screenname);
        MutableBuddyList joustSimBuddyList
            = (MutableBuddyList)conn.getSsiService().getBuddyList();

        LayoutEventCollector evtCollector = new LayoutEventCollector();
        joustSimBuddyList.addLayoutListener(evtCollector);

        List grList = joustSimBuddyList.getGroups();

        Iterator iter = grList.iterator();
        while (iter.hasNext())
        {
            MutableGroup item = (MutableGroup) iter.next();
            logger.debug("group : " + item);
            List bs = item.getBuddiesCopy();
            Iterator iter1 = bs.iterator();
            while (iter1.hasNext())
            {
                Object b = (Object) iter1.next();
                logger.debug("buddy : " + b);
            }
        }

        if(grList.size() < 1)
        {
            logger.debug("No groups! Will stop now");
            return;
        }

        ((MutableGroup)grList.get(0)).addBuddy(screenname);

        evtCollector.waitForANewBuddy(10000);
        joustSimBuddyList.removeLayoutListener(evtCollector);
    }

    /**
     * Sends <tt>body</tt> to <tt>buddy</tt>  as an offline instant message
     * @param buddy the screenname of the budy that we'd like to send our msg to.
     * @param body the content of the message to send.
     */
    public void sendOfflineMessage(String buddy, String body)
    {
        conn.sendSnac(new OfflineSnacCmd(buddy, body));
    }

    private class OfflineSnacCmd  extends SendImIcbm
    {
        private static final int TYPE_OFFLINE = 0x0006;

        protected OfflineSnacCmd(String sn, String message)
        {
            super(sn, message);
        }

        protected void writeChannelData(OutputStream out)
                throws IOException
        {
                super.writeChannelData(out);
                new Tlv(TYPE_OFFLINE).write(out);
        }
    }

    private class AuthReplyCmd
        extends SsiCommand
    {
        private int FLAG_AUTH_ACCEPTED = 1;
        private int FLAG_AUTH_DECLINED = 0;

        private String uin = null;
        private String reason = null;
        private boolean accepted = false;

        public AuthReplyCmd(SnacPacket packet)
        {
            super(0x001b);

            ByteBlock messageData = packet.getData();
            // parse data
            int offset = 0;
            short uinLen = BinaryTools.getUByte(messageData, offset++);
            uin = OscarTools.getString(messageData.subBlock(offset, uinLen), "US-ASCII");
            offset += uinLen;

            accepted = BinaryTools.getUByte(messageData, offset++) == 1;

            int reasonLen = BinaryTools.getUShort(messageData, offset);
            offset += 2;
            reason = OscarTools.getString(messageData.subBlock(offset, reasonLen), "US-ASCII");
        }


        public AuthReplyCmd(String uin, String reason, boolean accepted)
        {
            super(0x001a);

            this.uin = uin;
            this.reason = reason;
            this.accepted = accepted;
        }

        public void writeData(OutputStream out)
            throws IOException
        {
            byte[] uinBytes = BinaryTools.getAsciiBytes(uin);
            BinaryTools.writeUByte(out, uinBytes.length);
            out.write(uinBytes);

            if(accepted)
            {
                BinaryTools.writeUByte(out, FLAG_AUTH_ACCEPTED);
            }
            else
            {
                BinaryTools.writeUByte(out, FLAG_AUTH_DECLINED);
            }

            if(reason == null)
                reason = "";

            byte[] reasonBytes = BinaryTools.getAsciiBytes(reason);
            BinaryTools.writeUShort(out, reasonBytes.length);
            out.write(reasonBytes);
        }
    }

    public class AuthCmdFactory
        extends ServerSsiCmdFactory
        implements SnacResponseListener
    {
        List SUPPORTED_TYPES = null;

        public String responseReasonStr = null;
        public String requestReasonStr = null;
        public boolean ACCEPT = false;

        public boolean isErrorAddingReceived = false;
        public boolean isRequestAccepted = false;

        public AuthCmdFactory()
        {
            List types = super.getSupportedTypes();
            ArrayList tempTypes = new ArrayList(types);
            tempTypes.add(new CmdType(SsiCommand.FAMILY_SSI, 0x001b)); // 1b auth request reply
            tempTypes.add(new CmdType(SsiCommand.FAMILY_SSI, 0x0019)); // 19 auth request

            this.SUPPORTED_TYPES = DefensiveTools.getUnmodifiable(tempTypes);
        }

        public List getSupportedTypes()
        {return SUPPORTED_TYPES;}

        public SnacCommand genSnacCommand(SnacPacket packet)
        {
            int command = packet.getCommand();

            // auth reply
//            if (command == 0x001b)
            // auth request
            if (command == 25)
            {
                RequestAuthCmd cmd = new RequestAuthCmd(packet);
                requestReasonStr = cmd.reason;

                // will wait as a normal user
                Object lock = new Object();
                synchronized(lock){
                    try{
                        lock.wait(2000);
                    }
                    catch (Exception ex){}
                }

                logger.trace("sending authorization " + ACCEPT);
                conn.sendSnac(
                    new AuthReplyCmd(
                        String.valueOf(cmd.uin),
                        responseReasonStr,
                        ACCEPT));
                return cmd;
            }
            else if (command == 27) // auth reply
            {
                AuthReplyCmd cmd = new AuthReplyCmd(packet);

                isRequestAccepted = cmd.accepted;
                responseReasonStr = cmd.reason;

                return cmd;
            }

            return super.genSnacCommand(packet);
        }

        public void handleResponse(SnacResponseEvent e)
        {
            if (e.getSnacCommand() instanceof SsiDataModResponse)
            {
                SsiDataModResponse dataModResponse =
                    (SsiDataModResponse) e.getSnacCommand();

                int[] results = dataModResponse.getResults();
                List items = ( (ItemsCmd) e.getRequest().getCommand()).
                    getItems();
                items = new LinkedList(items);

                for (int i = 0; i < results.length; i++)
                {
                    int result = results[i];
                    if (result ==
                        SsiDataModResponse.RESULT_ICQ_AUTH_REQUIRED)
                    {
                        isErrorAddingReceived = true;

                        // authorisation required for user
                        SsiItem buddyItem = (SsiItem) items.get(i);

                        String uinToAskForAuth = buddyItem.getName();

                        logger.trace("finding buddy : " + uinToAskForAuth);

                        Vector buddiesToBeAdded = new Vector();

                        BuddyAwaitingAuth newBuddy = new BuddyAwaitingAuth(
                            buddyItem);
                        buddiesToBeAdded.add(newBuddy);

                        CreateItemsCmd addCMD = new CreateItemsCmd(buddiesToBeAdded);

                        logger.trace("Adding buddy as awaiting authorization");

                        MutableBuddyList joustSimBuddyList
                            = (MutableBuddyList)conn.getSsiService().getBuddyList();

                        LayoutEventCollector evtCollector = new LayoutEventCollector();
                        joustSimBuddyList.addLayoutListener(evtCollector);

                        conn.getSsiService().sendSnac(addCMD);

                        evtCollector.waitForANewBuddy(15000);
                        joustSimBuddyList.removeLayoutListener(evtCollector);

                        logger.trace("Finished - Adding buddy as awaiting authorization");

                        //SNAC(13,18)     send authorization request
                        conn.getSsiService().sendSnac(
                            new RequestAuthCmd(
                                uinToAskForAuth,
                                requestReasonStr));
                    }
                }
            }

        }
    }

    private class RequestAuthCmd
        extends SsiCommand
    {
        String uin;
        String reason;

        public RequestAuthCmd(String uin, String reason)
        {
            super(0x0018);
            this.uin = uin;
            this.reason = reason;
        }

        public RequestAuthCmd(SnacPacket packet)
        {
            super(0x0019);

            ByteBlock messageData = packet.getData();
            // parse data
            int offset = 0;
            short uinLen = BinaryTools.getUByte(messageData, offset);
            offset++;

            uin = OscarTools.getString(messageData.subBlock(offset, uinLen),"US-ASCII");

            offset += uinLen;

            int reasonLen = BinaryTools.getUShort(messageData, offset);
            offset+=2;

            reason =
                OscarTools.getString(messageData.subBlock(offset, reasonLen), "US-ASCII");
        }

        public void writeData(OutputStream out) throws IOException
        {
            byte[] uinBytes = BinaryTools.getAsciiBytes(uin);
            BinaryTools.writeUByte(out, uinBytes.length);
            out.write(uinBytes);

            if (reason == null)
            {
                reason = "";
            }

            byte[] reasonBytes = BinaryTools.getAsciiBytes(reason);
            BinaryTools.writeUShort(out, reasonBytes.length);
            out.write(reasonBytes);
        }
    }

    public void setAuthorizationRequired()
    {
        logger.debug("sending auth required");
        conn.getSsiService().sendSnac(new SaveInfoRequest());
    }

    private class SaveInfoRequest
        extends SnacCommand
    {
        public SaveInfoRequest()
        {
            super(21,2);
        }

        public void writeData(OutputStream out)
            throws IOException
        {
            ByteArrayOutputStream icqout = new ByteArrayOutputStream();

            ByteArrayOutputStream icqDataOut = new ByteArrayOutputStream();
            byte b[] = new byte[]{
                (byte)0xf8, (byte)0x02, // 0x02F8  User 'show web status' permissions
                (byte)0x01, (byte)0x00, (byte)0x00,
                (byte)0x0c, (byte)0x03, // 0x030C User authorization permissions
                (byte)0x01, (byte)0x00, (byte)0x00
            };
            icqDataOut.write(b);

//            new Tlv(0x030C, ByteBlock.wrap(b)).write(icqDataOut);

            int hdrlen = 10; // The expected header length, not counting the length field itself.
            int primary = 0x07D0;
            int secondary = 0x0c3a;

            long icqUINlong = Long.parseLong(icqUIN.getFormatted());

            int length = hdrlen + icqDataOut.size();

            writeUShort(icqout, length);
            writeUInt(icqout, icqUINlong);
            writeUShort(icqout, primary);
            writeUShort(icqout, 0); // the sequence

            writeUShort(icqout, secondary);

            icqDataOut.writeTo(icqout);

            logger.debug("now we will write data!");
            System.out.println("now we will write data!");
            new Tlv(0x0001, ByteBlock.wrap(icqout.toByteArray())).write(out);
        }

        public void writeUInt(final OutputStream out, final long number)
            throws IOException
        {
            out.write(new byte[] {
                (byte)((number) & 0xff),
                (byte)((number >> 8) & 0xff),
                (byte)((number >> 16) & 0xff),
                (byte)((number >> 24) & 0xff)
            });
        }

        public void writeUShort(OutputStream out, int number)
            throws IOException
        {
            out.write(new byte[]
                {
                (byte)(number & 0xff),
                (byte)((number >> 8) & 0xff)
            });
        }
    }

    private static class BuddyAwaitingAuth
        extends SsiItem
    {
        private SsiItem originalItem = null;
        public BuddyAwaitingAuth(SsiItem originalItem)
        {
            super(
                originalItem.getName(),
                originalItem.getParentId(),
                originalItem.getId(),
                originalItem.getItemType(),
                getSpecTlvData());

            this.originalItem = originalItem;
        }

        public void write(OutputStream out) throws IOException
        {
            byte[] namebytes = BinaryTools.getAsciiBytes(originalItem.getName());
            BinaryTools.writeUShort(out, namebytes.length);
            out.write(namebytes);

            BinaryTools.writeUShort(out, originalItem.getParentId());
            BinaryTools.writeUShort(out, originalItem.getId());
            BinaryTools.writeUShort(out, originalItem.getItemType());

            ByteBlock data = getData();
            // here we are nice and let data be null
            int len = data == null ? 0 : data.getLength();
            BinaryTools.writeUShort(out, len);
            if (data != null)
            {
                data.write(out);
            }
        }

        private static ByteBlock getSpecTlvData()
        {
            try
            {
                ByteArrayOutputStream o = new ByteArrayOutputStream();
                new Tlv(0x0066).write(o);

                ByteBlock block = ByteBlock.wrap(o.toByteArray());
                return block;
            }
            catch (IOException ex)
            {
                logger.error("Error creating buddy awaiting auth tlv", ex);
                return null;
            }
        }
    }
}
