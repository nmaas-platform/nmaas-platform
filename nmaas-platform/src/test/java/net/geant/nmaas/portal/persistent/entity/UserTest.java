package net.geant.nmaas.portal.persistent.entity;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class UserTest {

    @Test
    public void shouldBeEqual() {
        final String commonUsername = "username";
        assertEquals(new User(commonUsername), new User(commonUsername));
        assertEquals(new User(1L, commonUsername, true, new Domain(), Role.ROLE_GUEST),
                     new User(2L, commonUsername, false, new Domain(), Role.ROLE_USER));
    }

}
