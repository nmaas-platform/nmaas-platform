package net.geant.nmaas.portal.persistent.entity;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UserRoleTest {

    final private User user = new User("username");
    final private Domain domain = new Domain(1L, "name", "codename");
    final private Role role = Role.ROLE_DOMAIN_ADMIN;

    @Test
    public void shouldBeEqual() {
        assertEquals(new UserRole(user, domain, role).getId(),
                     new UserRole(new User("username"), new Domain(1L, "name", "codename"), Role.ROLE_DOMAIN_ADMIN).getId());
    }

    @Test
    public void shouldGetCorrectAuthorityString() {
        UserRole userRole = new UserRole(user, domain, role);
        assertEquals("1:ROLE_DOMAIN_ADMIN", userRole.getAuthority());
    }

}
