package net.geant.nmaas.portal.service.impl.security;

import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import net.geant.nmaas.portal.service.AclService.Permissions;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.theInstance;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AppTemplatePermissionCheckTest {

	private AppTemplatePermissionCheck atpch;
	
	private final ApplicationRepository applications = mock(ApplicationRepository.class);

	@BeforeEach
	public void setUp() throws Exception {
		this.atpch = new AppTemplatePermissionCheck(applications);

		when(applications.findById(UsersHelper.APP1.getId())).thenReturn(Optional.of(UsersHelper.APP1));
		when(applications.findById(UsersHelper.APP2.getId())).thenReturn(Optional.of(UsersHelper.APP2));
		when(applications.findById(UsersHelper.APP3.getId())).thenReturn(Optional.of(UsersHelper.APP3));
	}

	@Test
	public final void testSupports() {
		assertNotNull(theInstance(atpch));

		assertTrue(atpch.supports("appTemplate"));
		assertTrue(atpch.supports("apptemplate"));
		assertTrue(atpch.supports("APPTEMPLATE"));
		
		assertFalse(atpch.supports("template"));
		assertFalse(atpch.supports(null));
	}

	@Test
	public void testSystemAdminEvaluatePermissions() {
		Set<Permissions> perms = atpch.evaluatePermissions(UsersHelper.ADMIN, UsersHelper.APP1.getId(), AppTemplatePermissionCheck.APPTEMPLATE);
		assertEquals(5, perms.size());
		assertThat(perms, hasItems(Permissions.READ, Permissions.WRITE, Permissions.CREATE, Permissions.DELETE, Permissions.OWNER));
	}
	
	@Test
	public void testDomainAdminEvaluatePermissions() {
		Set<Permissions> perms = null;
		
		perms = atpch.evaluatePermissions(UsersHelper.DOMAIN1_ADMIN, UsersHelper.APP1.getId(), AppTemplatePermissionCheck.APPTEMPLATE);
		assertEquals(1, perms.size());
		assertThat(perms, hasItems(Permissions.READ));
	}
	
	@Test
	public void testToolManagerEvaluatePermissions() {
		Set<Permissions> perms = null;
		
		perms = atpch.evaluatePermissions(UsersHelper.TOOL_MANAGER, UsersHelper.APP1.getId(), AppTemplatePermissionCheck.APPTEMPLATE);
		assertEquals(5, perms.size());
		assertThat(perms, hasItems(Permissions.READ, Permissions.WRITE, Permissions.CREATE, Permissions.DELETE, Permissions.OWNER));
	}
	
	@Test
	public void testDomainUserEvaluatePermissions() {
		Set<Permissions> perms = null;

		perms = atpch.evaluatePermissions(UsersHelper.DOMAIN1_USER1, UsersHelper.APP1.getId(), AppTemplatePermissionCheck.APPTEMPLATE);
		assertEquals(1, perms.size());
		assertThat(perms, hasItems(Permissions.READ));
	}
	
	@Test
	public void testDomainGuestEvaluatePermissions() {
		Set<Permissions> perms = null;
		
		perms = atpch.evaluatePermissions(UsersHelper.DOMAIN1_GUEST, UsersHelper.APP1.getId(), AppTemplatePermissionCheck.APPTEMPLATE);
		assertEquals(1, perms.size());
		assertThat(perms, hasItems(Permissions.READ));

		perms = atpch.evaluatePermissions(UsersHelper.GLOBAL_GUEST, UsersHelper.APP1.getId(), AppTemplatePermissionCheck.APPTEMPLATE);
		assertEquals(1, perms.size());
		assertThat(perms, hasItems(Permissions.READ));
	}

}
