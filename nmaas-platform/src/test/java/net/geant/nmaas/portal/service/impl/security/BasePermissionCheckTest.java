package net.geant.nmaas.portal.service.impl.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.AclService.Permissions;

public class BasePermissionCheckTest {

	User user = mock(User.class);
	
	BasePermissionCheck bpch = null;

	Set<Permissions> perms = null;
	Set<Permissions> emptyPerms = null;
	
	@Before
	public void setUp() throws Exception {
		perms = new HashSet<Permissions>();
		perms.addAll(Arrays.asList(new Permissions[] { Permissions.CREATE, Permissions.OWNER }));
		perms = Collections.unmodifiableSet(perms);
		
		emptyPerms = Collections.unmodifiableSet(new HashSet<Permissions>());		
		
		bpch = new BasePermissionCheck() {
			@Override
			public boolean supports(String targetType) {
				return "supported".equalsIgnoreCase(targetType);
			}
			@Override
			protected Set<Permissions> evaluatePermissions(User user, Serializable targetId, String targetType) {
				return perms;
			}
		};
		
	}

	@After
	public void tearDown() throws Exception {
		bpch = null;
	}

	@Test
	public final void testHasPermissionSetOfPermissionsPermissions() {
		
		assertTrue(bpch.hasPermission(perms, Permissions.ANY));
		assertFalse(bpch.hasPermission(emptyPerms, Permissions.ANY));
		assertFalse(bpch.hasPermission(emptyPerms, Permissions.OWNER));
		
		assertTrue(bpch.hasPermission(perms, Permissions.CREATE));
		assertFalse(bpch.hasPermission(perms,Permissions.WRITE));
		
	}

	@Test
	public final void testHasPermissionSetOfPermissionsPermissionsArray() {		
		
		assertTrue(bpch.hasPermission(perms, new Permissions[] { Permissions.WRITE, Permissions.CREATE }));
		assertFalse(bpch.hasPermission(perms, new Permissions[] { Permissions.DELETE, Permissions.READ }));
		
		assertTrue(bpch.hasPermission(perms, new Permissions[] { Permissions.ANY }));
		assertFalse(bpch.hasPermission(emptyPerms, new Permissions[] { Permissions.ANY }));
		assertFalse(bpch.hasPermission(emptyPerms, new Permissions[] { Permissions.WRITE, Permissions.CREATE }));
	}

	@Test
	public final void testCheckUserSerializableStringPermissions() {
		assertTrue(bpch.check(user, 1, "supported", Permissions.OWNER));
		assertFalse(bpch.check(user, 1, "notSupported", Permissions.OWNER));
		
		assertFalse(bpch.check(user, 1, "supported", Permissions.WRITE));
		assertFalse(bpch.check(user, 1, "notSupported", Permissions.WRITE));
		
		assertTrue(bpch.check(user, 1, "supported", Permissions.ANY));
		assertFalse(bpch.check(user, 1, "notSupported", Permissions.ANY));

	}

	@Test
	public final void testCheckUserSerializableStringPermissionsArray() {
		assertTrue(bpch.check(user, 1, "supported", new Permissions[] { Permissions.OWNER, Permissions.READ }));
		assertFalse(bpch.check(user, 1, "notSupported", new Permissions [] { Permissions.OWNER, Permissions.ANY} ));
		
		assertFalse(bpch.check(user, 1, "supported", new Permissions[] { Permissions.WRITE, Permissions.READ }));
		assertFalse(bpch.check(user, 1, "notSupported", new Permissions[] { Permissions.WRITE, Permissions.READ }));
		
		assertTrue(bpch.check(user, 1, "supported", new Permissions[] { Permissions.ANY }));
		assertFalse(bpch.check(user, 1, "notSupported", new Permissions[] {Permissions.ANY}));

	}

}
