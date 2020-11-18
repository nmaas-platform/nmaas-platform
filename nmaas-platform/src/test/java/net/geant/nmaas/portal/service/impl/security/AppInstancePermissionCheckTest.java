package net.geant.nmaas.portal.service.impl.security;

import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import net.geant.nmaas.portal.persistent.repositories.AppInstanceRepository;
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

public class AppInstancePermissionCheckTest {

	private AppInstancePermissionCheck aipch;

	private final AppInstanceRepository appInstances = mock(AppInstanceRepository.class);

	private final DomainService domains = mock(DomainService.class);
	
	@BeforeEach
	public void setUp(){
		this.aipch = new AppInstancePermissionCheck(appInstances, domains);

		when(domains.getGlobalDomain()).thenReturn(Optional.of(UsersHelper.GLOBAL));
		when(domains.findDomain(UsersHelper.GLOBAL.getId())).thenReturn(Optional.of(UsersHelper.GLOBAL));
		when(domains.findDomain(UsersHelper.DOMAIN1.getId())).thenReturn(Optional.of(UsersHelper.DOMAIN1));
		when(domains.findDomain(UsersHelper.DOMAIN2.getId())).thenReturn(Optional.of(UsersHelper.DOMAIN2));
		
		when(appInstances.findById(UsersHelper.DOMAIN1_APP1.getId())).thenReturn(Optional.of(UsersHelper.DOMAIN1_APP1));
		when(appInstances.findById(UsersHelper.DOMAIN1_APP2.getId())).thenReturn(Optional.of(UsersHelper.DOMAIN1_APP2));
		when(appInstances.findById(UsersHelper.DOMAIN2_APP1.getId())).thenReturn(Optional.of(UsersHelper.DOMAIN2_APP1));
		when(appInstances.findById(UsersHelper.DOMAIN2_APP2.getId())).thenReturn(Optional.of(UsersHelper.DOMAIN2_APP1));
	}

	@Test
	public final void testSupports() {
		assertTrue(aipch.supports("appinstance"));
		assertTrue(aipch.supports("appInstance"));
		assertTrue(aipch.supports("APPINSTANCE"));
		
		assertFalse(aipch.supports("application"));
		assertFalse(aipch.supports(null));
	}

	@Test
	public void testSystemAdminEvaluatePermissions() {
		Set<Permissions> perms = aipch.evaluatePermissions(UsersHelper.ADMIN, UsersHelper.DOMAIN1_APP1.getId(), AppInstancePermissionCheck.APPINSTANCE);
		assertEquals(5, perms.size());
		assertThat(perms, hasItems(Permissions.READ, Permissions.WRITE, Permissions.CREATE, Permissions.DELETE, Permissions.OWNER));
	}
	
	@Test
	public void testDomainAdminEvaluatePermissions() {
		Set<Permissions> perms = null;
		
		perms = aipch.evaluatePermissions(UsersHelper.DOMAIN1_ADMIN, UsersHelper.DOMAIN1_APP1.getId(), AppInstancePermissionCheck.APPINSTANCE);
		assertEquals(5, perms.size());
		assertThat(perms, hasItems(Permissions.READ, Permissions.WRITE, Permissions.CREATE, Permissions.DELETE, Permissions.OWNER));
		
		perms = aipch.evaluatePermissions(UsersHelper.DOMAIN1_ADMIN, UsersHelper.DOMAIN2_APP1.getId(), AppInstancePermissionCheck.APPINSTANCE);
		assertEquals(0, perms.size());
	}
	
	@Test
	public void testToolManagerEvaluatePermissions() {
		Set<Permissions> perms = null;
		
		perms = aipch.evaluatePermissions(UsersHelper.TOOL_MANAGER, UsersHelper.DOMAIN1_APP1.getId(), AppInstancePermissionCheck.APPINSTANCE);
		assertEquals(1, perms.size());
		assertThat(perms, hasItems(Permissions.READ));
	}
	
	@Test
	public void testDomainUserEvaluatePermissions() {
		Set<Permissions> perms = null;

		perms = aipch.evaluatePermissions(UsersHelper.DOMAIN1_USER1, UsersHelper.DOMAIN1_APP1.getId(), AppInstancePermissionCheck.APPINSTANCE);
		assertEquals(1, perms.size());
		assertThat(perms, hasItems(Permissions.READ));

		perms = aipch.evaluatePermissions(UsersHelper.DOMAIN1_USER2, UsersHelper.DOMAIN1_APP1.getId(), AppInstancePermissionCheck.APPINSTANCE);
		assertEquals(1, perms.size());
		assertThat(perms, hasItems(Permissions.READ));
		
		perms = aipch.evaluatePermissions(UsersHelper.DOMAIN1_USER1, UsersHelper.DOMAIN2_APP1.getId(), AppInstancePermissionCheck.APPINSTANCE);
		assertEquals(0, perms.size());
	}
	
	@Test
	public void testDomainGuestEvaluatePermissions() {
		Set<Permissions> perms = null;
		
		perms = aipch.evaluatePermissions(UsersHelper.DOMAIN1_GUEST, UsersHelper.DOMAIN1_APP1.getId(), AppInstancePermissionCheck.APPINSTANCE);
		assertEquals(0, perms.size());

		perms = aipch.evaluatePermissions(UsersHelper.DOMAIN1_GUEST, UsersHelper.DOMAIN2_APP2.getId(), AppInstancePermissionCheck.APPINSTANCE);
		assertEquals(0, perms.size());

		perms = aipch.evaluatePermissions(UsersHelper.GLOBAL_GUEST, UsersHelper.DOMAIN1_APP1.getId(), AppInstancePermissionCheck.APPINSTANCE);
		assertEquals(0, perms.size());
	}

}
