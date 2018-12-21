package net.geant.nmaas.portal.service.impl.security;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import net.geant.nmaas.portal.service.AclService.Permissions;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableAutoConfiguration
public class AppTemplatePermissionCheckTest {

	@InjectMocks
	AppTemplatePermissionCheck atpch = new AppTemplatePermissionCheck();
	
	@Mock
	ApplicationRepository applications;
	
	
	@Before
	public void setUp() throws Exception {
		when(applications.findById(UsersHelper.APP1.getId())).thenReturn(Optional.of(UsersHelper.APP1));
		when(applications.findById(UsersHelper.APP2.getId())).thenReturn(Optional.of(UsersHelper.APP2));
		when(applications.findById(UsersHelper.APP3.getId())).thenReturn(Optional.of(UsersHelper.APP3));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testSupports() {
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
