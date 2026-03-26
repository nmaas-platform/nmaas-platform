package net.geant.nmaas.portal.service;

import net.geant.nmaas.api.dto.users.RoleDto;
import net.geant.nmaas.api.dto.users.UserListEntryDto;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserRole;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserListEntryTest {

    @Test
    void constructorShouldMapNamesRolesDomainsAndDates() {
        User user = new User("john", true);
        user.setId(10L);
        user.setFirstname("John");
        user.setLastname("Doe");
        user.setEmail("john@example.com");

        Domain globalDomain = new Domain(1L, "GLOBAL", "GLOBAL", true);
        Domain domainOne = new Domain(2L, "Research", "research", true);
        Domain domainTwo = new Domain(3L, "Operations", "ops", true);
        user.setRoles(List.of(
                new UserRole(user, globalDomain, Role.ROLE_SYSTEM_ADMIN),
                new UserRole(user, domainOne, Role.ROLE_DOMAIN_ADMIN),
                new UserRole(user, domainTwo, Role.ROLE_USER)
        ));

        OffsetDateTime lastLogin = OffsetDateTime.now().minusDays(1);
        OffsetDateTime firstLogin = OffsetDateTime.now().minusDays(30);

        UserListEntry entry = new UserListEntry(user, 2L, lastLogin, firstLogin);
        UserListEntryDto dto = entry.toDto();

        assertEquals(10L, entry.getId());
        assertEquals("john", entry.getUsername());
        assertEquals("John Doe", entry.getName());
        assertEquals("john@example.com", entry.getEmail());
        assertEquals("ROLE_SYSTEM_ADMIN", entry.getGlobalRole());
        assertEquals(Role.ROLE_DOMAIN_ADMIN, entry.getDomainRole());
        assertEquals(lastLogin, entry.getLastSuccessfulLoginDate());
        assertEquals(firstLogin, entry.getFirstLoginDate());
        assertEquals(2, entry.getDomainsName().size());
        assertTrue(entry.getDomainsName().contains("Research"));
        assertTrue(entry.getDomainsName().contains("Operations"));

        assertEquals(10L, dto.getId());
        assertEquals("john", dto.getUsername());
        assertEquals("John Doe", dto.getName());
        assertEquals("john@example.com", dto.getEmail());
        assertEquals("ROLE_SYSTEM_ADMIN", dto.getGlobalRole());
        assertEquals(RoleDto.ROLE_DOMAIN_ADMIN, dto.getDomainRole());
        assertEquals(lastLogin, dto.getLastSuccessfulLoginDate());
        assertEquals(firstLogin, dto.getFirstLoginDate());
        assertTrue(dto.isEnabled());
    }

    @Test
    void constructorShouldUseLastNameWhenFirstNameIsMissing() {
        User user = new User("anna", false);
        user.setId(20L);
        user.setFirstname(null);
        user.setLastname("Nowak");
        user.setRoles(List.of());

        UserListEntry entry = new UserListEntry(user);

        assertEquals("Nowak", entry.getName());
        assertEquals("", entry.getGlobalRole());
        assertTrue(entry.getDomainsName().isEmpty());
    }

    @Test
    void toDtoShouldHaveNullDomainRoleWhenNotProvided() {
        User user = new User("tom", true);
        user.setId(30L);
        user.setFirstname("Tom");
        user.setLastname("");
        user.setRoles(List.of());

        UserListEntry entry = new UserListEntry(user);
        UserListEntryDto dto = entry.toDto();

        assertNull(entry.getDomainRole());
        assertNull(dto.getDomainRole());
        assertEquals("Tom", dto.getName());
    }
}
