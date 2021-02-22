package net.geant.nmaas.portal.persistent.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApplicationSubscriptionTest {

    @Test
    public void shouldBeEqual() {
        assertEquals(
                new ApplicationSubscription(
                        new Domain("name", "codename"),
                        new ApplicationBase("name")).getId(),
                new ApplicationSubscription(
                        new Domain("name", "codename"),
                        new ApplicationBase("name")).getId());
    }

}
