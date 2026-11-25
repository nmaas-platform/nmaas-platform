package net.geant.nmaas.portal.persistence.entity;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserTest {

    @Test
    void shouldBeEqual() {
        final String commonUsername = "username";
        assertEquals(new User(commonUsername), new User(commonUsername));
        assertEquals(new User(1L, commonUsername, true, new Domain(), Role.ROLE_GUEST),
                     new User(2L, commonUsername, false, new Domain(), Role.ROLE_USER));
    }

    @Test
    void shouldNotDuplicateAlreadyHeldRolesInSetNewRoles() {
        User user = new User("testUser");
        user.setNewRoles(Set.of(new UserRole(user, new Domain("name", "codename"), Role.ROLE_USER)));
        user.setNewRoles(Set.of(new UserRole(user, new Domain("name", "codename"), Role.ROLE_USER)));
        assertEquals(1, user.getRoles().size());
    }

    @Test
    void shouldAppendDistinctRolesInSetNewRoles() {
        User user = new User("testUser");
        user.setNewRoles(Set.of(new UserRole(user, new Domain("name", "codename1"), Role.ROLE_USER)));
        user.setNewRoles(Set.of(new UserRole(user, new Domain("name", "codename2"), Role.ROLE_USER)));
        assertEquals(2, user.getRoles().size());
    }

}
