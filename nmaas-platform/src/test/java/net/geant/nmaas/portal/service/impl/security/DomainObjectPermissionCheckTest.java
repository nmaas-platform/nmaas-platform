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
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRoleRepository;
import net.geant.nmaas.portal.service.AclService.Permissions;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableAutoConfiguration
//@ContextConfiguration(classes = {SecurityConfig.class, PortalConfig.class /*, PersistentConfig.class */})
public class DomainObjectPermissionCheckTest {
	
	@Mock
	UserRepository userRepository;
	
	@Mock
	UserRoleRepository userRoleRepository;
	
	@Mock
	UserService userService;
	
	@Mock
	DomainService domains;

	@InjectMocks
	DomainObjectPermissionCheck dopch = new DomainObjectPermissionCheck();

	@Before
	public void setUp() throws Exception {
		when(domains.getGlobalDomain()).thenReturn(Optional.of(UsersHelper.GLOBAL));
		when(domains.findDomain(UsersHelper.GLOBAL.getId())).thenReturn(Optional.of(UsersHelper.GLOBAL));
		when(domains.findDomain(UsersHelper.DOMAIN1.getId())).thenReturn(Optional.of(UsersHelper.DOMAIN1));
		when(domains.findDomain(UsersHelper.DOMAIN2.getId())).thenReturn(Optional.of(UsersHelper.DOMAIN2));
		
	}

	@After
	public void tearDown() throws Exception {
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
		assertEquals(5, perms.size());
		assertThat(perms, hasItems(Permissions.READ, Permissions.WRITE, Permissions.CREATE, Permissions.DELETE, Permissions.OWNER));
		
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
		assertEquals(4, perms.size());
		assertThat(perms, hasItems(Permissions.READ, Permissions.WRITE, Permissions.CREATE, Permissions.DELETE));
		
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
