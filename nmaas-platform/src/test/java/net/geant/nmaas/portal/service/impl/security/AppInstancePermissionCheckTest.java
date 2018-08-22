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
import net.geant.nmaas.portal.persistent.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.service.AclService.Permissions;
import net.geant.nmaas.portal.service.DomainService;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableAutoConfiguration
public class AppInstancePermissionCheckTest {

	@InjectMocks
	AppInstancePermissionCheck aipch = new AppInstancePermissionCheck();
	
	@Mock
	AppInstanceRepository appInstances;
	
	@Mock
	DomainService domains;
	
	
	@Before
	public void setUp() throws Exception {
		when(domains.getGlobalDomain()).thenReturn(Optional.of(UsersHelper.GLOBAL));
		when(domains.findDomain(UsersHelper.GLOBAL.getId())).thenReturn(Optional.of(UsersHelper.GLOBAL));
		when(domains.findDomain(UsersHelper.DOMAIN1.getId())).thenReturn(Optional.of(UsersHelper.DOMAIN1));
		when(domains.findDomain(UsersHelper.DOMAIN2.getId())).thenReturn(Optional.of(UsersHelper.DOMAIN2));
		
		when(appInstances.findById(UsersHelper.DOMAIN1_APP1.getId()).get()).thenReturn(UsersHelper.DOMAIN1_APP1);
		when(appInstances.findById(UsersHelper.DOMAIN1_APP2.getId()).get()).thenReturn(UsersHelper.DOMAIN1_APP2);
		when(appInstances.findById(UsersHelper.DOMAIN2_APP1.getId()).get()).thenReturn(UsersHelper.DOMAIN2_APP1);
		when(appInstances.findById(UsersHelper.DOMAIN2_APP2.getId()).get()).thenReturn(UsersHelper.DOMAIN2_APP1);
	}

	@After
	public void tearDown() throws Exception {
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
	public void testSuperAdminEvaluatePermissions() {
	
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
		assertEquals(5, perms.size());
		assertThat(perms, hasItems(Permissions.CREATE, Permissions.READ, Permissions.WRITE, Permissions.DELETE, Permissions.OWNER));

		perms = aipch.evaluatePermissions(UsersHelper.DOMAIN1_USER2, UsersHelper.DOMAIN1_APP1.getId(), AppInstancePermissionCheck.APPINSTANCE);
		assertEquals(2, perms.size());
		assertThat(perms, hasItems(Permissions.CREATE, Permissions.READ));
		
		perms = aipch.evaluatePermissions(UsersHelper.DOMAIN1_USER1, UsersHelper.DOMAIN2_APP1.getId(), AppInstancePermissionCheck.APPINSTANCE);
		assertEquals(0, perms.size());
		
	}
	
	@Test
	public void testDomainGuestEvaluatePermissions() {
		Set<Permissions> perms = null;
		
		perms = aipch.evaluatePermissions(UsersHelper.DOMAIN1_GUEST, UsersHelper.DOMAIN1_APP1.getId(), AppInstancePermissionCheck.APPINSTANCE);
		assertEquals(1, perms.size());
		assertThat(perms, hasItems(Permissions.READ));
		
		perms = aipch.evaluatePermissions(UsersHelper.DOMAIN1_GUEST, UsersHelper.DOMAIN2_APP2.getId(), AppInstancePermissionCheck.APPINSTANCE);
		assertEquals(0, perms.size());
		
		
		perms = aipch.evaluatePermissions(UsersHelper.GLOBAL_GUEST, UsersHelper.DOMAIN1_APP1.getId(), AppInstancePermissionCheck.APPINSTANCE);
		assertEquals(0, perms.size());
		
		
		
	}

}
