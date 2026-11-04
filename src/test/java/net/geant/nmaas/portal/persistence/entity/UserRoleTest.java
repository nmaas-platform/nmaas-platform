package net.geant.nmaas.portal.persistence.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserRoleTest {

    private static final User USER = new User("username");
    private static final Domain DOMAIN = new Domain(1L, "name", "codename");
    private static final Role ROLE = Role.ROLE_DOMAIN_ADMIN;

    @Test
    void shouldBeEqual() {
        assertEquals(new UserRole(USER, DOMAIN, ROLE).getId(),
                new UserRole(new User("username"), new Domain(1L, "name", "codename"), Role.ROLE_DOMAIN_ADMIN).getId());
    }

    @Test
    void shouldGetCorrectAuthorityString() {
        UserRole userRole = new UserRole(USER, DOMAIN, ROLE);
        assertEquals("1:ROLE_DOMAIN_ADMIN", userRole.getAuthority());
    }

}
