/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.slick.protocol.jabber;

import java.util.*;
import junit.framework.*;

import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;

/**
 * Tests multi user chat functionalities
 *
 * @author Symphorien Wanko
 */
public class TestOperationSetMultiUserChat2
    extends TestCase
{

    /**
     * logger for <tt>TestOperationSetMultiUserChat2</tt> class
     */
    private static final Logger logger =
        Logger.getLogger(TestOperationSetMultiUserChat2.class);

    //room name for each test will be testRoomBaseName + roomID
    private static String testRoomBaseName = "muctestroom";

    private static int roomID = 0;

    /**
     * Provides constants and some utilities method.
     */
    private JabberSlickFixture fixture = new JabberSlickFixture();

    private OperationSetMultiUserChat opSetMUC1;

    private OperationSetMultiUserChat opSetMUC2;

    private OperationSetPresence opSetPresence1;

    private OperationSetPresence opSetPresence2;

    /**
     * Initializes the test with the specified <tt>name</tt>.
     *
     * @param name the name of the test to initialize.
     */
    public TestOperationSetMultiUserChat2(String name)
    {
        super(name);
    }

    /**
     * JUnit setup method.
     * @throws Exception in case anything goes wrong.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        fixture.setUp();

        Map<String, OperationSet> supportedOperationSets1 =
            fixture.provider1.getSupportedOperationSets();

        if (supportedOperationSets1 == null
            || (supportedOperationSets1.size() < 1))
        {
            throw new NullPointerException(
                "No OperationSet implementations are supported by " +
                "this implementation. ");
        }

        //get the operation set presence here.
        opSetMUC1 = (OperationSetMultiUserChat)
            supportedOperationSets1.get(
            OperationSetMultiUserChat.class.getName());

        if (opSetMUC1 == null)
        {
            throw new NullPointerException(
                "No implementation for MUC was found");
        }

        //we also need the presence op set in order to retrieve contacts.
        opSetPresence1 = (OperationSetPresence)
            supportedOperationSets1.get(OperationSetPresence.class.getName());

        //if the op set is null show that we're not happy.
        if (opSetPresence1 == null)
        {
            throw new NullPointerException(
                "An implementation of the service must provide an " +
                "implementation of at least one of the PresenceOperationSets");
        }

        Map<String, OperationSet> supportedOperationSets2 =
            fixture.provider2.getSupportedOperationSets();

        if (supportedOperationSets2 == null
            || (supportedOperationSets2.size() < 1))
        {
            throw new NullPointerException(
                "No OperationSet implementations are supported by " +
                "this implementation. ");
        }

        opSetMUC2 = (OperationSetMultiUserChat)
            supportedOperationSets2.get(
            OperationSetMultiUserChat.class.getName());

        if (opSetMUC2 == null)
        {
            throw new NullPointerException(
                "No implementation for MUC was found");
        }

        opSetPresence2 = (OperationSetPresence)
            supportedOperationSets2.get(OperationSetPresence.class.getName());

        //if the op set is null show that we're not happy.
        if (opSetPresence2 == null)
        {
            throw new NullPointerException(
                "An implementation of the service must provide an " +
                "implementation of at least one of the PresenceOperationSets");
        }
    }

    /**
     * JUnit teardown method.
     * @throws Exception in case anything goes wrong.
     */
    @Override
    protected void tearDown() throws Exception
    {
        fixture.tearDown();
        super.tearDown();
    }

    /**
     * Create the list to be sure that contacts exchanging messages
     * exists in each other lists
     * @throws Exception
     */
    public void prepareContactList()
        throws Exception
    {
        fixture.clearProvidersLists();

        Object o = new Object();
        synchronized (o)
        {
            o.wait(2000);
        }

        try
        {
            opSetPresence1.subscribe(fixture.userID2);
        }
        catch (OperationFailedException ex)
        {
            // the contact already exist its OK
        }

        try
        {
            opSetPresence2.subscribe(fixture.userID1);
        }
        catch (OperationFailedException ex1)
        {
            // the contact already exist its OK
        }

        logger.info("will wait till the list prepare is completed");
        synchronized (o)
        {
            o.wait(4000);
        }
    }

    /**
     * utiliy method to know if a given nickname is on a
     * <tt>ChatRoomMember</tt> list
     *
     * @param name the name we are looking for
     * @param memberList the list where we search
     *
     * @return true if the name is found, false otherwise
     */
    private boolean nameIsOnMemberList(
        String name, List<ChatRoomMember> memberList)
    {
        for (ChatRoomMember member : memberList)
        {
            if (member.getName().equals(name))
            {
                return true;
            }
        }

        return false;
    }

    /**
     *
     * @return Test a testsuite containing all tests to execute.
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(
            new TestOperationSetMultiUserChat2("testCreateChatRoom"));

        suite.addTest(
            new TestOperationSetMultiUserChat2("testJoinRoom"));

        suite.addTest(
            new TestOperationSetMultiUserChat2("testGetJoinedChatRoom"));

        suite.addTest(
            new TestOperationSetMultiUserChat2("testFindRoom"));

        suite.addTest(
            new TestOperationSetMultiUserChat2("testInviteReject"));

        suite.addTest(
            new TestOperationSetMultiUserChat2("testInviteJoin"));

        suite.addTest(
            new TestOperationSetMultiUserChat2("testLeave"));

        suite.addTest(
            new TestOperationSetMultiUserChat2("testNickName"));


// following test are not yet commplete
//
//        suite.addTest(
//            new TestOperationSetMultiUserChat2("testRoomSubject"));
//
//
//        suite.addTest(
//            new TestOperationSetMultiUserChat2("testConferenceChat"));
//
//
//        suite.addTest(
//            new TestOperationSetMultiUserChat2("testMemberBan"));
//
//
//        suite.addTest(
//            new TestOperationSetMultiUserChat2("testMemberKick"));
        return suite;
    }

    /**
     * <tt>testCreateChatRoom</tt> creates a room using
     * <tt>OperationSetMultiUserChat#createRoom()</tt>.
     * We then look if the room is on the list returned by
     * <tt>OperationSetMultiUserChat#getExistingChatRooms()</tt>
     *
     * @throws OperationNotSupportedException
     * @throws OperationFailedException
     */
    public void testCreateChatRoom()
        throws OperationNotSupportedException,
               OperationFailedException
    {
        String testRoomName = testRoomBaseName + roomID++;

        // we create the test room
        ChatRoom opSet1Room =
            opSetMUC1.createChatRoom(testRoomName, null);

        assertNotNull("createChatRoom returned null", opSet1Room);

        // and check if it exists on the server
        List<String> existingRooms = opSetMUC1.getExistingChatRooms();

        for (String roomName : existingRooms)
        {
            if (roomName.equals(opSet1Room.getName()))
            {
                return; // ok the created room is listed on server
            }
        }
        fail("the new created room is not liste on server");
    }

    /**
     * <tt>testJoinRoom</tt> looks if <tt>someRoom.isJoined</tt> will returns
     * true for an user after he joins a room with <tt>someRoom.join()</tt>
     *
     * @throws OperationFailedException
     */
    public void testJoinRoom()
        throws OperationFailedException,
               OperationNotSupportedException
    {
        String testRoomName = testRoomBaseName + roomID++;

        // we create the test room
        ChatRoom opSet1Room =
            opSetMUC1.createChatRoom(testRoomName, null);

        opSet1Room.join();

        assertTrue(
            "we are not in the room we just joined", opSet1Room.isJoined());

        assertTrue("user is not listed in the room with his nickname",
            nameIsOnMemberList(
            opSet1Room.getUserNickname(), opSet1Room.getMembers()));
    }

    /**
     * After a user joins a room, we look if the room is present on the list
     * returned by
     * <tt>OperationSetMultiUserChat#getCurrentlyJoinedChatRooms</tt>.
     *
     */
    public void testGetJoinedChatRoom()
        throws OperationFailedException,
               OperationNotSupportedException
    {
        String testRoomName = testRoomBaseName + roomID++;

        // we create the test room
        ChatRoom opSet1Room =
            opSetMUC1.createChatRoom(testRoomName, null);

        opSet1Room.join();

        assertTrue(
            "we are not in the room we just joined", opSet1Room.isJoined());

        // is the joined room list up to date ?
        List<ChatRoom> joinedRooms = opSetMUC1.getCurrentlyJoinedChatRooms();

        for (ChatRoom room : joinedRooms)
        {
            if (room.getName().equals(opSet1Room.getName()))
            {
                return; // ok opSet1Room is on the joined rooms list
            }
        }
        fail("the joined room list does not contains a joined room");
    }

    /**
     * <tt>testFindRoom</tt> will looks for an unexisting room and an existing
     * one to see if results are what we expect.
     */
    public void testFindRoom()
        throws OperationFailedException,
               OperationNotSupportedException
    {
        String testRoomName = testRoomBaseName + roomID++;

        // we create the test room
        ChatRoom opSet1Room =
            opSetMUC1.createChatRoom(testRoomName, null);
        opSet1Room.join();

        ChatRoom foundRoom = null;
        try
        {
            foundRoom = opSetMUC1.findRoom("WhoCreatedThatRoom");
        }
        catch (Exception ex)
        {
            //ok, no one created a room named "WhoCreatedThatRoom"
        }
        assertNull("wasnt expecting to find the room named " +
            "'WhoCreatedThatRoom' on server", foundRoom);

        // to find the existing room created with opSetMUC1,
        // we will use opSetMUC2 to be sure the room will not be retrieved from
        // opSetMUC1 cache
        try
        {
            foundRoom = opSetMUC2.findRoom(testRoomName);
        }
        catch (Exception ex)
        {
            logger.warn(ex);
        }
        assertNotNull("failed to find an existing room on server", foundRoom);

        assertEquals(
            "the room found is not exactly the one we were looking for"
            , opSet1Room.getName(), foundRoom.getName());
    }

    /**
     * user1 send to user2 an invitation which will be rejected.
     */
    public void testInviteReject()
        throws OperationFailedException,
               OperationNotSupportedException
    {
        MUCEventCollector opSet1Collector =
            new MUCEventCollector(opSetMUC1, MUCEventCollector.EVENT_INVITE);
        MUCEventCollector opSet2Collector =
            new MUCEventCollector(opSetMUC2, MUCEventCollector.EVENT_INVITE);

        String testRoomName = testRoomBaseName + roomID++;

        // we create the test room
        ChatRoom opSet1Room =
            opSetMUC1.createChatRoom(testRoomName, null);
        opSet1Room.join();

        // an invitation user2 must reject
        opSet1Room.invite(fixture.userID2, "testInviteReject");

        opSet2Collector.waitForEvent(10000);

        //now we look if user2 received an event
        assertEquals(
            "The invitation sent from user1 to user2 has not been received "
            , 1, opSet2Collector.collectedEvents.size());

        // does the invitation comes from user1 with the correct reason
        ChatRoomInvitationReceivedEvent invitationReceivedEvent =
            (ChatRoomInvitationReceivedEvent)
            opSet2Collector.collectedEvents.get(0);

        ChatRoomInvitation invitation = invitationReceivedEvent.getInvitation();

        assertEquals("The inviter is not the expected user"
            , fixture.userID1, invitation.getInviter());

        assertEquals("The invitation reason received differs from the one sent"
            , "testInviteReject", invitation.getReason());

        //user2 reject the invitation as planned
        opSetMUC2.rejectInvitation(invitation, invitation.getReason());

        opSet1Collector.waitForEvent(10000);

        //is user1 aware his invitation has been declined
        assertEquals(
            "no response received from the invitation sent to user 2"
            , 1, opSet1Collector.collectedEvents.size());

        //who declined the invitation and why
        ChatRoomInvitationRejectedEvent invitationRejectedEvent =
            (ChatRoomInvitationRejectedEvent)
            opSet1Collector.collectedEvents.get(0);

        assertEquals("the invitation has been declined  by an unexpected user",
            fixture.userID2, invitationRejectedEvent.getInvitee());

        assertEquals("the invitation is not declined for the expected reason",
            "testInviteReject", invitationRejectedEvent.getReason());
    }

    /**
     * <tt>testInviteFindJoin</tt> reproduces the following scenario : user1
     * invite user2. user2 retrieves the room where he is invited then, he joins it.
     */
    public void testInviteJoin()
        throws OperationFailedException,
               OperationNotSupportedException
    {
        String testRoomName = testRoomBaseName + roomID++;

        // we create the test room
        ChatRoom opSet1Room =
            opSetMUC1.createChatRoom(testRoomName, null);
        opSet1Room.join();

        MUCEventCollector opSet1RoomCollector =
            new MUCEventCollector(
            opSet1Room, MUCEventCollector.EVENT_PRESENCE);

        MUCEventCollector opSet2Collector =
            new MUCEventCollector(opSetMUC2, MUCEventCollector.EVENT_INVITE);

        opSet1Room.invite(fixture.userID2, "testInviteAccept");

        opSet2Collector.waitForEvent(10000);

        //make sure the invitation has been received
        assertEquals("the invitation has not been received"
            , 1, opSet2Collector.collectedEvents.size());

        // does the invitation comes from user1 with the correct reason
        ChatRoomInvitationReceivedEvent invitationReceivedEvent =
            (ChatRoomInvitationReceivedEvent)
            opSet2Collector.collectedEvents.get(0);

        ChatRoomInvitation invitation = invitationReceivedEvent.getInvitation();

        assertEquals("The inviter is not the expected user",
            fixture.userID1, invitation.getInviter());

        assertEquals("The invitation reason received differs from the one sent"
            , "testInviteAccept", invitation.getReason());

        //user accept the invitation
        ChatRoom opSet2Room = invitation.getTargetChatRoom();

        MUCEventCollector opSet2RoomCollector =
            new MUCEventCollector(opSet2Room, MUCEventCollector.EVENT_PRESENCE);

        opSet2Room.join();

        opSet2RoomCollector.waitForEvent(10000);
        opSet1RoomCollector.waitForEvent(10000);

        // we know check if both member received events
        assertEquals("a room member has not been notified that someone " +
            "joined the room"
            , 1, opSet1RoomCollector.collectedEvents.size());

        assertEquals("a room member has not been notified that someone " +
            "joined the room"
            , 1, opSet2RoomCollector.collectedEvents.size());

        ChatRoomMemberPresenceChangeEvent memberEvent =
            (ChatRoomMemberPresenceChangeEvent)
            opSet1RoomCollector.collectedEvents.get(0);

        assertEquals("user received an event of the wrong type "
            , ChatRoomMemberPresenceChangeEvent.MEMBER_JOINED
            , memberEvent.getEventType());

        assertEquals("the user who joined is not the one who was invited"
            , fixture.userID2
            , memberEvent.getChatRoomMember().getContactAddress());


        assertEquals("there is not exactly two members in the room"
            , 2 , opSet1Room.getMembersCount());

        //both sides should report the same members number
        assertEquals("the same room reports different members count " +
            "from one side to other"
            , opSet1Room.getMembersCount(), opSet2Room.getMembersCount());

        //user2 must be on room members list now
        assertTrue("user is not listed in the room with his nickname",
            nameIsOnMemberList(
            opSet2Room.getUserNickname(), opSet2Room.getMembers()));

        //and he must be listed on both sides
        assertTrue("user is not listed in the room with his nickname ,from " +
            "peer side",
            nameIsOnMemberList(
            opSet2Room.getUserNickname(), opSet1Room.getMembers()));
    }

    /**
     * In <tt>testJoinLeave</tt>, we join a room then leave it and see if the
     * reported <tt>ChatRoom</tt> members are reflecting our actions.
     */
    public void testLeave()
        throws OperationFailedException,
               OperationNotSupportedException
    {
        String testRoomName = testRoomBaseName + roomID++;

        // we create the test room
        ChatRoom opSet1Room =
            opSetMUC1.createChatRoom(testRoomName, null);
        opSet1Room.join();

        ChatRoom opSet2Room = opSetMUC2.findRoom(testRoomName);

        // first be sure to be outside the room
        if (opSet2Room.isJoined())
            opSet2Room.leave();

        assertFalse("user is in a room he left or didnt joined"
            , opSet2Room.isJoined());

        MUCEventCollector opSet1RoomCollector =
            new MUCEventCollector(opSet1Room, MUCEventCollector.EVENT_PRESENCE);

        MUCEventCollector opSet2RoomCollector =
            new MUCEventCollector(opSet2Room, MUCEventCollector.EVENT_PRESENCE);

        opSet2Room.join();

        opSet2RoomCollector.waitForEvent(10000);
        opSet1RoomCollector.waitForEvent(10000);

        // does users received events since a member joined
        assertEquals("a room member has not been notified that someone else " +
            "joined the room"
            , 1, opSet1RoomCollector.collectedEvents.size());

        assertTrue("user failed to join a room", opSet2Room.isJoined());

        // checking if the user is listed with the right nickname
        assertTrue("user nickname not found in the room after join "
            , nameIsOnMemberList(
            opSet2Room.getUserNickname(), opSet2Room.getMembers()));

        //and we check on the other side
        assertTrue("user nickname not found in the room after join, " +
            "from peer side"
            , nameIsOnMemberList(
            opSet2Room.getUserNickname(), opSet1Room.getMembers()));

        opSet1RoomCollector.collectedEvents.clear();

        // leaving
        opSet2Room.leave();

        opSet1RoomCollector.waitForEvent(10000);

        assertFalse("an user is reported present in a room he left"
            , opSet2Room.isJoined());

        // the nickname shouldnt be listed anymore
        assertFalse("user nickname is still on the room list after he left"
            , nameIsOnMemberList(opSet2Room.getUserNickname()
            , opSet2Room.getMembers()));

        // and the other side

        // does user1 received notifcation since user2 left
        assertEquals("an user joined and left and a room member missed events "
            , 1, opSet1RoomCollector.collectedEvents.size());

        ChatRoomMemberPresenceChangeEvent memberEvent =
            (ChatRoomMemberPresenceChangeEvent)
            opSet1RoomCollector.collectedEvents.get(0);

        assertEquals("user received an event of the wrong type "
            , ChatRoomMemberPresenceChangeEvent.MEMBER_LEFT
            , memberEvent.getEventType());

        assertEquals("the user who left is not the expected one"
            , fixture.userID2
            , memberEvent.getChatRoomMember().getContactAddress());

        assertFalse("user nickname is still on the room list after he left, " +
            "from the peer side"
            , nameIsOnMemberList(opSet2Room.getUserNickname()
            , opSet1Room.getMembers()));

    }

    /**
     * In <tt>testNickName</tt>, user nicknames will be changed
     * and we will check if changes are well reflected on both user
     * sides.
     */
    public void testNickName()
        throws OperationFailedException,
               OperationNotSupportedException
    {
        String testRoomName = testRoomBaseName + roomID++;

        // we create the test room
        ChatRoom opSet1Room =
            opSetMUC1.createChatRoom(testRoomName, null);
        opSet1Room.join();

        String user1FirstNick = opSet1Room.getUserNickname();
        String user1SecondNick = "user1nickchange";

        ChatRoom opSet2Room = opSetMUC2.findRoom(testRoomName);
        opSet2Room.join();

        // checking the nickname from the peer side
        assertTrue("user nickname not found by peer "
            , nameIsOnMemberList(user1FirstNick, opSet2Room.getMembers()));

        // change the nickname
        opSet1Room.setUserNickname(user1SecondNick);

        assertEquals("failed to change user nickname"
            , user1SecondNick, opSet1Room.getUserNickname());

        //user should be listed with his new nick name
        assertTrue(
            "user nickname not found on member list after modification"
            , nameIsOnMemberList(user1SecondNick, opSet1Room.getMembers()));

        // checking the nickname modification on the other side
        assertTrue("user nickname not found on member list after modification " +
            "from, peer side",
            nameIsOnMemberList(user1SecondNick, opSet2Room.getMembers()));

        //previous nickname should not rest on the list after change
        assertFalse(
            "both old and new nick are listed in a room after nick change"
            , nameIsOnMemberList(user1FirstNick, opSet1Room.getMembers()));

        assertFalse(
            "both old and new nick are listed in a room after nick change, " +
            "from the peer side",
            nameIsOnMemberList(user1FirstNick, opSet2Room.getMembers()));

        // trying to steal a nickname
        try
        {
            opSet2Room.setUserNickname(user1SecondNick);
        }
        catch (OperationFailedException ex)
        {
            // stealing nickname is not allowed by the room
        }
        if (opSet1Room.getUserNickname().equals(opSet2Room.getUserNickname()))
        {
            fail(
                "different users are holding the same nick in the same room");
        }
    }

    /**
     * <tt>testRoomSubject</tt> test changing the room subject.
     */
    public void testRoomSubject()
        throws OperationFailedException,
        OperationNotSupportedException
    {
        if (true) // just to avoid the not reacheable statement when compiling
            throw new OperationNotSupportedException("not yet implemented");
        String testRoomName = testRoomBaseName + roomID++;

        // we create the test room
        ChatRoom opSet1Room =
            opSetMUC1.createChatRoom(testRoomName, null);
        opSet1Room.join();

        String subject1 = "one";
        String subject2 = "two";

        ChatRoom opSet2Room = opSetMUC2.findRoom(testRoomName);

        opSet1Room.setSubject(subject1);
        assertEquals("failed to change room subject"
            , subject1, opSet1Room.getSubject());

        assertEquals("subject change not seen on peer side"
            , subject1, opSet2Room.getSubject());

        opSet1Room.setSubject(subject2);
        assertEquals("failed to change room subject"
            , subject2, opSet1Room.getSubject());

        assertEquals("subject change not seem on peer side"
            , subject2, opSet2Room.getSubject());
    }

    /**
     * Here we test conference messaging : sending, receiving messages and
     * corresponding events.
     */
    public void testConferenceChat()
        throws OperationFailedException,
               OperationNotSupportedException
    {
        if (true) // just to avoid the not reacheable statement when compiling
            throw new OperationNotSupportedException("not yet implemented");
        String testRoomName = testRoomBaseName + roomID++;

        // we create the test room
        ChatRoom opSet1Room =
            opSetMUC1.createChatRoom(testRoomName, null);
        opSet1Room.join();

        ChatRoom opSet2Room = opSetMUC2.findRoom(testRoomName);
        opSet2Room.join();

        // building and checking the message
        String message1 = "lorem ipsum first";
        Message opSet1Message = opSet1Room.createMessage(message1);

        assertEquals("created message content differ from the one provided "
            ,message1, opSet1Message.getContent());

        MUCEventCollector opSet1RoomCollector =
            new MUCEventCollector(opSet1Room, MUCEventCollector.EVENT_MESSAGE);

        MUCEventCollector opSet2RoomCollector =
            new MUCEventCollector(opSet2Room, MUCEventCollector.EVENT_MESSAGE);

        // ship it
        opSet1Room.sendMessage(opSet1Message);

        opSet1RoomCollector.waitForEvent(10000);
        opSet2RoomCollector.waitForEvent(10000);

        // does every member received the message
        assertEquals("user1 didn't received the message"
            , 1, opSet1RoomCollector.collectedEvents.size());
        assertEquals("user1 didn't received the message"
            , 1, opSet2RoomCollector.collectedEvents.size());

        ChatRoomMessageReceivedEvent messageEvent =
            (ChatRoomMessageReceivedEvent)
            opSet2RoomCollector.collectedEvents.get(0);

        assertSame("the message comes from another room instance"
            , opSet2Room, messageEvent.getSourceChatRoom());

        assertEquals("message type is not CONVERSATION_MESSAGE_RECEIVED"
            , ChatRoomMessageReceivedEvent.CONVERSATION_MESSAGE_RECEIVED
            , messageEvent.getEventType());

        ChatRoomMember member = messageEvent.getSourceChatRoomMember();

        assertEquals("message comes from an unexpected user"
            , opSet1Room.getUserNickname(), member.getName());

        assertEquals(
            "received message differ from sent message"
            , message1, messageEvent.getMessage().getContent());

        // for the sender side, thereis two events, the message delivery one
        // and the message reception itself
        if (opSet1RoomCollector.collectedEvents.size() != 2)
        {
            opSet1RoomCollector.waitForEvent(10000);
        }

        assertEquals(
            "message sender missed either the message delivery " +
            "or the reception event"
            , 2, opSet1RoomCollector.collectedEvents.size());

        ChatRoomMessageDeliveredEvent deliveryEvent;
        ChatRoomMessageReceivedEvent receptionEvent;

        // since we dont know which message has been received first, we have
        // to check
        if (opSet1RoomCollector.collectedEvents.get(0)
            instanceof ChatRoomMessageDeliveredEvent)
        {
            deliveryEvent = (ChatRoomMessageDeliveredEvent)
                opSet1RoomCollector.collectedEvents.get(0);
            receptionEvent = (ChatRoomMessageReceivedEvent)
                opSet1RoomCollector.collectedEvents.get(1);
        }
        else
        {
            receptionEvent = (ChatRoomMessageReceivedEvent)
                opSet1RoomCollector.collectedEvents.get(0);
            deliveryEvent = (ChatRoomMessageDeliveredEvent)
                opSet1RoomCollector.collectedEvents.get(1);
        }

        // looking the receptionEvent
        assertSame("the message comes from another room instance"
            , opSet1Room, receptionEvent.getSourceChatRoom());

        assertEquals("message type is not CONVERSATION_MESSAGE_RECEIVED"
            , ChatRoomMessageReceivedEvent.CONVERSATION_MESSAGE_RECEIVED
            , messageEvent.getEventType());

        member = receptionEvent.getSourceChatRoomMember();

        assertEquals("message comes from an unexpected user"
            , opSet1Room.getUserNickname(), member.getName());

        assertEquals("received message differ from sent message"
            , message1, receptionEvent.getMessage().getContent());

        // and now check consistency between the delivery event and the
        // reception eveynt
        assertSame("delivery event and reception event source rooms differ"
            , deliveryEvent.getSourceChatRoom()
            , receptionEvent.getSourceChatRoom());

        assertSame("delivery event and reception event message content"
            , deliveryEvent.getMessage().getContent()
            , receptionEvent.getMessage().getContent());
    }

    /**
     * Here we test conference messagin : sending, receiving messages and
     * corresponding events.
     */
    public void testMemberBan()
        throws OperationFailedException,
        OperationNotSupportedException
    {
        if (true) // just to avoid the not reacheable statement when compiling
            throw new OperationNotSupportedException("not yet implemented");

        String testRoomName = testRoomBaseName + roomID++;

        // we create the test room
        ChatRoom opSet1Room =
            opSetMUC1.createChatRoom(testRoomName, null);
        opSet1Room.join();

        ChatRoom opSet2Room = opSetMUC2.findRoom(testRoomName);
        opSet2Room.join();
    }

    /**
     * Here we test conference messagin : sending, receiving messages and
     * corresponding events.
     */
    public void testMemberKick()
        throws OperationFailedException,
        OperationNotSupportedException
    {
        if (true) // just to avoid the not reacheable statement when compiling
            throw new OperationNotSupportedException("not yet implemented");
        String testRoomName = testRoomBaseName + roomID++;

        // we create the test room
        ChatRoom opSet1Room =
            opSetMUC1.createChatRoom(testRoomName, null);
        opSet1Room.join();

        ChatRoom opSet2Room = opSetMUC2.findRoom(testRoomName);
        opSet2Room.join();
    }

    /**
     * Utility class used to collect events received during tests.
     */
    class MUCEventCollector
        implements ChatRoomInvitationRejectionListener,
                   ChatRoomInvitationListener,
                   LocalUserChatRoomPresenceListener,
                   ChatRoomMemberPresenceListener,
                   ChatRoomMessageListener
    {

        private final ArrayList collectedEvents = new ArrayList();

        private int waitCount = 0;

        private final OperationSetMultiUserChat opSet;

        private final ChatRoom room;

        private static final int EVENT_INVITE = 1;

        private static final int EVENT_PRESENCE = 2;

        private static final int EVENT_MESSAGE = 3;

        /**
         * Creates an event collector to listen for specific events from
         * the given opSet
         * 
         * @param opSet the <tt>OperationSetMultiUserChat> from which we will
         * receive events.
         * @param  eventType indicades the kind of events we are looking for
         */
        public MUCEventCollector(
            OperationSetMultiUserChat opSet, int eventType)
        {
            this.opSet = opSet;
            room = null;
            switch (eventType)
            {
                case EVENT_INVITE:
                    opSet.addInvitationListener(this);
                    opSet.addInvitationRejectionListener(this);
                    break;
                case EVENT_PRESENCE:
                    opSet.addPresenceListener(this);
                    break;
                default:
                    throw new IllegalArgumentException(
                        "invalid event category " + eventType);
            }
        }

        public MUCEventCollector(ChatRoom room, int eventType)
        {
            this.room = room;
            opSet = null;
            switch (eventType)
            {
                case EVENT_PRESENCE:
                    room.addMemberPresenceListener(this);
                    break;
                case EVENT_MESSAGE:
                    room.addMessageListener(this);
                    break;
                default:
                    throw new IllegalArgumentException(
                        "invalid event category " + eventType);
            }
        }

        /**
         * Blocks until at least one event is received or until waitFor
         * miliseconds pass (whichever happens first).
         *
         * @param waitFor the number of miliseconds that we should be waiting
         * for an event before simply bailing out.
         */
        public void waitForEvent(long waitFor)
        {
            logger.trace("Waiting for a CallEvent");

            synchronized (this)
            {
                if (collectedEvents.size() > waitCount)
                {
                    waitCount++;
                    logger.trace("Event already received. " + collectedEvents);
                    return;
                }
                try
                {
                    wait(waitFor);
                    if (collectedEvents.size() > waitCount)
                    {
                        logger.trace("Received a MUC event.");
                    }
                    else
                    {
                        logger.trace(
                            "MUC event missed after " + waitFor + "ms.");
                    }
                    waitCount++;
                }
                catch (InterruptedException ex)
                {
                    logger.debug(
                        "Interrupted while waiting for a MUC event", ex);
                }
            }
        }

        /**
         * utility method to collect an event
         * @param evt the event to collect
         */
        private void collectEvent(EventObject evt)
        {
            synchronized (this)
            {
                logger.debug(
                    "Collected evt(" + collectedEvents.size() + ")= " + evt);
                collectedEvents.add(evt);
                notifyAll();
            }
        }

        public void invitationRejected(
            ChatRoomInvitationRejectedEvent evt)
        {
            collectEvent(evt);
        }

        public void invitationReceived(
            ChatRoomInvitationReceivedEvent evt)
        {
            collectEvent(evt);
        }

        public void localUserPresenceChanged(
            LocalUserChatRoomPresenceChangeEvent evt)
        {
            collectEvent(evt);
        }

        public void memberPresenceChanged(
            ChatRoomMemberPresenceChangeEvent evt)
        {
            collectEvent(evt);
        }

        public void messageReceived(
            ChatRoomMessageReceivedEvent evt)
        {
            collectEvent(evt);
        }

        public void messageDelivered(
            ChatRoomMessageDeliveredEvent evt)
        {
            collectEvent(evt);
        }

        public void messageDeliveryFailed(
            ChatRoomMessageDeliveryFailedEvent evt)
        {
            collectEvent(evt);
        }
    }
}
