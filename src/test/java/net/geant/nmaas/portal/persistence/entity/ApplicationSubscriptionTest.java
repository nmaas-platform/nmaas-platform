package net.geant.nmaas.portal.persistence.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationSubscriptionTest {

    @Test
    void shouldBeEqual() {
        assertEquals(
                new ApplicationSubscription(
                        new Domain("name", "codename"),
                        new ApplicationBase("name")).getId(),
                new ApplicationSubscription(
                        new Domain("name", "codename"),
                        new ApplicationBase("name")).getId());
    }

}
