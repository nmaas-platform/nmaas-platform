package net.geant.nmaas.portal.persistent.entity;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ApplicationSubscriptionTest {

    @Test
    public void shouldBeEqual() {
        assertEquals(
                new ApplicationSubscription(
                        new Domain("name", "codename"),
                        new Application("name","testversion")).getId(),
                new ApplicationSubscription(
                        new Domain("name", "codename"),
                        new Application("name","testversion")).getId());
    }

}
