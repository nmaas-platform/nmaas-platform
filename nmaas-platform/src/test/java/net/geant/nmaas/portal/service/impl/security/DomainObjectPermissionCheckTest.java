package net.geant.nmaas.portal.service.impl.security;

import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import net.geant.nmaas.portal.service.AclService.Permissions;
import net.geant.nmaas.portal.service.DomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DomainObjectPermissionCheckTest {
	
	private final DomainService domains = mock(DomainService.class);

	DomainObjectPermissionCheck dopch;

	@BeforeEach
	public void setUp() {
		this.dopch = new DomainObjectPermissionCheck(domains);

		when(domains.getGlobalDomain()).thenReturn(Optional.of(UsersHelper.GLOBAL));
		when(domains.findDomain(UsersHelper.GLOBAL.getId())).thenReturn(Optional.of(UsersHelper.GLOBAL));
		when(domains.findDomain(UsersHelper.DOMAIN1.getId())).thenReturn(Optional.of(UsersHelper.DOMAIN1));
		when(domains.findDomain(UsersHelper.DOMAIN2.getId())).thenReturn(Optional.of(UsersHelper.DOMAIN2));
	}

	@Test
	public void testSupports() {
		assertTrue(dopch.supports("domain"));
		assertTrue(dopch.supports("DOMAIN"));
		
		assertFalse(dopch.supports("other"));
		assertFalse(dopch.supports(null));
	}

	@Test
	public void testSystemAdminEvaluatePermissions() {
		Set<Permissions> perms = dopch.evaluatePermissions(UsersHelper.ADMIN, UsersHelper.DOMAIN1.getId(), DomainObjectPermissionCheck.DOMAIN);
		assertEquals(5, perms.size());
		assertThat(perms, hasItems(Permissions.READ, Permissions.WRITE, Permissions.CREATE, Permissions.DELETE, Permissions.OWNER));
	}
	
	@Test
	public void testDomainAdminEvaluatePermissions() {
		Set<Permissions> perms = null;
		
		perms = dopch.evaluatePermissions(UsersHelper.DOMAIN1_ADMIN, UsersHelper.DOMAIN1.getId(), DomainObjectPermissionCheck.DOMAIN);
		assertEquals(2, perms.size());
		assertThat(perms, hasItems(Permissions.READ, Permissions.OWNER));

		perms = dopch.evaluatePermissions(UsersHelper.DOMAIN1_ADMIN, UsersHelper.DOMAIN2.getId(), DomainObjectPermissionCheck.DOMAIN);
		assertEquals(0, perms.size());
	}
	
	@Test
	public void testToolManagerEvaluatePermissions() {
		Set<Permissions> perms = null;
		
		perms = dopch.evaluatePermissions(UsersHelper.TOOL_MANAGER, UsersHelper.DOMAIN1.getId(), DomainObjectPermissionCheck.DOMAIN);
		assertEquals(1, perms.size());
		assertThat(perms, hasItems(Permissions.READ));
	}
	
	@Test
	public void testDomainUserEvaluatePermissions() {
		Set<Permissions> perms = null;
		
		perms = dopch.evaluatePermissions(UsersHelper.DOMAIN1_USER1, UsersHelper.DOMAIN1.getId(), DomainObjectPermissionCheck.DOMAIN);
		assertEquals(1, perms.size());
		assertThat(perms, hasItems(Permissions.READ));
		
		perms = dopch.evaluatePermissions(UsersHelper.DOMAIN1_USER1, UsersHelper.DOMAIN2.getId(), DomainObjectPermissionCheck.DOMAIN);
		assertEquals(0, perms.size());
		
	}
	
	@Test
	public void testDomainGuestEvaluatePermissions() {
		Set<Permissions> perms = null;
		
		perms = dopch.evaluatePermissions(UsersHelper.DOMAIN1_GUEST, UsersHelper.DOMAIN1.getId(), DomainObjectPermissionCheck.DOMAIN);
		assertEquals(1, perms.size());
		assertThat(perms, hasItems(Permissions.READ));
		
		perms = dopch.evaluatePermissions(UsersHelper.DOMAIN1_GUEST, UsersHelper.DOMAIN2.getId(), DomainObjectPermissionCheck.DOMAIN);
		assertEquals(0, perms.size());

		perms = dopch.evaluatePermissions(UsersHelper.GLOBAL_GUEST, UsersHelper.GLOBAL.getId(), DomainObjectPermissionCheck.DOMAIN);
		assertEquals(0, perms.size());

		perms = dopch.evaluatePermissions(UsersHelper.GLOBAL_GUEST, UsersHelper.DOMAIN1.getId(), DomainObjectPermissionCheck.DOMAIN);
		assertEquals(0, perms.size());
	}

}
