package net.geant.nmaas.portal.service.impl.security;

import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import net.geant.nmaas.portal.persistent.repositories.CommentRepository;
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

public class CommentPermissionCheckTest {

	private CommentPermissionCheck cpch;

	private final CommentRepository comments = mock(CommentRepository.class);
	
	private final DomainService domains = mock(DomainService.class);
	
	@BeforeEach
	public void setUp() throws Exception {
		this.cpch = new CommentPermissionCheck(comments);

		when(comments.findById(UsersHelper.COMMENT1.getId())).thenReturn(Optional.of(UsersHelper.COMMENT1));
		when(comments.findById(UsersHelper.COMMENT2.getId())).thenReturn(Optional.of(UsersHelper.COMMENT2));
		when(comments.findById(UsersHelper.COMMENT3.getId())).thenReturn(Optional.of(UsersHelper.COMMENT3));
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
	public void testSystemAdminEvaluatePermissions() {
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
		assertEquals(4, perms.size());
		assertThat(perms, hasItems(Permissions.READ, Permissions.WRITE, Permissions.CREATE, Permissions.OWNER));
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
