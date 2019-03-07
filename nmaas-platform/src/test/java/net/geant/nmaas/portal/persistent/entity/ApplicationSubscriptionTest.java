package net.geant.nmaas.portal.persistent.entity;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class ApplicationSubscriptionTest {

    @Test
    public void shouldBeEqual() {
        assertEquals(
                new ApplicationSubscription(
                        new Domain("name", "codename"),
                        new Application("name")).getId(),
                new ApplicationSubscription(
                        new Domain("name", "codename"),
                        new Application("name")).getId());
    }

}
