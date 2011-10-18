/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.slick.contactlist;

import junit.framework.*;
import net.java.sip.communicator.service.contactlist.*;
import net.java.sip.communicator.service.protocol.*;

/**
 * The tests in this suite add more mock provider instances to the osgi bus and
 * verify whether they are properly handled by the meta contact list.
 * @author Emil Ivov
 */
public class TestSupportForMultipleProviders
    extends TestCase
{
    /**
     * A reference to the SLICK fixture.
     */
    private MclSlickFixture fixture = new MclSlickFixture(getClass().getName());

    public TestSupportForMultipleProviders(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        fixture.setUp();

    }

    protected void tearDown() throws Exception
    {
        fixture.tearDown();
        fixture = null;

        super.tearDown();
    }

    /**
     * Returns tests in this class in the order that they are supposed to be
     * executed.
     * @return a Test suite containing tests in this class in the order they are
     * to be executed.
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(new TestSupportForMultipleProviders(
            "testAddProviders"));
        suite.addTest(new TestSupportForMultipleProviders(
            "testMergingContactsFromDifferentProviders"));

        return suite;
    }

    /**
     * Registers mock providers with the OSGI framework and verifies that they
     * have been properly configured.
     */
    public void testAddProviders()
    {
        //register the new providers with the osgi bus.
        fixture.mockP1ServiceRegistration
            = MetaContactListServiceLick.registerMockProviderService(fixture.mockP1);
        fixture.mockP2ServiceRegistration
            = MetaContactListServiceLick.registerMockProviderService(fixture.mockP2);

        //verify that the groups have been properly added.
        fixture.metaP1Grp1 = fixture.metaClService
                      .findMetaContactGroupByContactGroup(fixture.mockP1Grp1);

        assertNotNull("The MCL impl ignored a newly added proto provider."
                      , fixture.metaP1Grp1);

        MclSlickFixture.assertGroupEquals(
            fixture.mockP1Grp1
            , fixture.metaP1Grp1
            , false);//there's no reason to have empty meta groups here so
                     //they are to be considered a problem

        fixture.metaP2Grp1 = fixture.metaClService
                            .findMetaContactGroupByContactGroup(
                                fixture.mockP2Grp1);

        assertNotNull("The MCL impl ignored a newly added proto provider."
                      , fixture.metaP2Grp1);

        MclSlickFixture.assertGroupEquals(
              fixture.mockP2Grp1
            , fixture.metaP2Grp1
            , false);//there's no reason to have empty meta groups here so
                     //they are to be considered a problem
    }

    /**
     * Merges contacts from the protocol providers added in the previous test
     * and make sure the merge was successful. The metod aims to prepare the
     * contact list for the following tests on contact list persistence.
     */
    public void testMergingContactsFromDifferentProviders()
    {
        //add a contact from provider 2 to the meta contact encapsulator of a
        //provider 1 contact
        MetaContact metaEmilP1 = fixture.metaClService
                                      .findMetaContactByContact(fixture.emilP1);

        assertNotNull("No meta contact found for "
                      + fixture.emilP1.getDisplayName(), metaEmilP1);

        fixture.metaClService.moveContact(fixture.emilP2, metaEmilP1);

        //verify that the new meta contact contains both contacts (no point
        //in verifying events)
        assertEquals("Contact " + fixture.emilP2.getDisplayName()
                     + " was not added to metacontact "
                     + metaEmilP1.getDisplayName()
                     , 2, metaEmilP1.getContactCount());

        //verify that mock provider 2 contains a new group named the same as the
        //parent meta group of the contact we just moved
        ContactGroup newGrpP2 = fixture.mockPresOpSetP2
            .getServerStoredContactListRoot().getGroup(
                fixture.metaP1Grp1.getGroupName());

        assertNotNull("Contact " + fixture.emilP2.getDisplayName()
                      + " was not moved to the proper group inside provider 2"
                      , newGrpP2);

        //verify that the encapsulating meta group now contains both mock groups
        MetaContactGroup newGrpP2MetaWrapper = fixture.metaClService
            .findMetaContactGroupByContactGroup(newGrpP2);

        assertNotNull("Strange Error", newGrpP2MetaWrapper);

        assertSame("Contact group " + newGrpP2
                   + " was not added to the right meta contact group"
                   , fixture.metaP1Grp1
                   , newGrpP2MetaWrapper);

    }
}
