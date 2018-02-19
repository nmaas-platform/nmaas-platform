package net.geant.nmaas.portal.service.impl.security;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

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
import net.geant.nmaas.portal.persistent.repositories.CommentRepository;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.AclService.Permissions;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableAutoConfiguration
public class CommentPermissionCheckTest {

	@InjectMocks
	CommentPermissionCheck cpch = new CommentPermissionCheck();
	
	@Mock
	CommentRepository comments;
	
	@Mock
	DomainService domains;
	
	@Before
	public void setUp() throws Exception {
		when(comments.findOne(UsersHelper.COMMENT1.getId())).thenReturn(UsersHelper.COMMENT1);
		when(comments.findOne(UsersHelper.COMMENT2.getId())).thenReturn(UsersHelper.COMMENT2);
		when(comments.findOne(UsersHelper.COMMENT3.getId())).thenReturn(UsersHelper.COMMENT3);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testSupport() {
		assertTrue(cpch.supports("comment"));
		assertTrue(cpch.supports("COMMENT"));
		assertTrue(cpch.supports("Comment"));
		
		assertFalse(cpch.supports("comments"));
		assertFalse(cpch.supports(null));
	}

	@Test
	public void testSuperAdminEvaluatePermissions() {
	
		Set<Permissions> perms = cpch.evaluatePermissions(UsersHelper.ADMIN, UsersHelper.COMMENT1.getId(), CommentPermissionCheck.COMMENT);
		assertEquals(5, perms.size());
		assertThat(perms, hasItems(Permissions.READ, Permissions.WRITE, Permissions.CREATE, Permissions.DELETE, Permissions.OWNER));
			
	}
	
	@Test
	public void testDomainAdminEvaluatePermissions() {
		Set<Permissions> perms = null;
		
		perms = cpch.evaluatePermissions(UsersHelper.DOMAIN1_ADMIN, UsersHelper.COMMENT1.getId(), CommentPermissionCheck.COMMENT);
		assertEquals(2, perms.size());
		assertThat(perms, hasItems(Permissions.READ, Permissions.CREATE));
				
	}
	
	@Test
	public void testToolManagerEvaluatePermissions() {
		Set<Permissions> perms = null;
		
		perms = cpch.evaluatePermissions(UsersHelper.TOOL_MANAGER, UsersHelper.COMMENT1.getId(), CommentPermissionCheck.COMMENT);
		assertEquals(5, perms.size());
		assertThat(perms, hasItems(Permissions.READ, Permissions.WRITE, Permissions.CREATE, Permissions.DELETE, Permissions.OWNER));
		
	}
	
	@Test
	public void testDomainUserEvaluatePermissions() {
		Set<Permissions> perms = null;

		perms = cpch.evaluatePermissions(UsersHelper.DOMAIN1_USER1, UsersHelper.COMMENT1.getId(), CommentPermissionCheck.COMMENT);
		assertEquals(5, perms.size());
		assertThat(perms, hasItems(Permissions.CREATE, Permissions.READ, Permissions.WRITE, Permissions.DELETE, Permissions.OWNER));

		perms = cpch.evaluatePermissions(UsersHelper.DOMAIN1_USER2, UsersHelper.COMMENT1.getId(), CommentPermissionCheck.COMMENT);
		assertEquals(2, perms.size());
		assertThat(perms, hasItems(Permissions.CREATE, Permissions.READ));
				
	}
	
	@Test
	public void testDomainGuestEvaluatePermissions() {
		Set<Permissions> perms = null;
		
		perms = cpch.evaluatePermissions(UsersHelper.DOMAIN1_GUEST, UsersHelper.COMMENT1.getId(), CommentPermissionCheck.COMMENT);
		assertEquals(2, perms.size());
		assertThat(perms, hasItems(Permissions.CREATE, Permissions.READ));
				
		
	}
	
}