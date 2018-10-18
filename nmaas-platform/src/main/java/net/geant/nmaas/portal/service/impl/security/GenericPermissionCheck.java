package net.geant.nmaas.portal.service.impl.security;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.service.AclService.Permissions;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class GenericPermissionCheck extends BasePermissionCheck {

	final protected Map<Role, Permissions[]> permMatrix = new HashMap<Role, Permissions[]>();
	
	public GenericPermissionCheck() {
		permMatrix.put(Role.ROLE_SYSTEM_ADMIN, new Permissions[] {Permissions.CREATE, Permissions.DELETE, Permissions.READ, Permissions.WRITE, Permissions.OWNER});
		permMatrix.put(Role.ROLE_OPERATOR, new Permissions[]{Permissions.CREATE, Permissions.DELETE, Permissions.READ, Permissions.WRITE});
		permMatrix.put(Role.ROLE_DOMAIN_ADMIN, new Permissions[] {Permissions.CREATE, Permissions.DELETE, Permissions.READ, Permissions.WRITE});
		permMatrix.put(Role.ROLE_USER, new Permissions[] {Permissions.CREATE, Permissions.READ, Permissions.WRITE});
		permMatrix.put(Role.ROLE_TOOL_MANAGER, new Permissions[] {Permissions.CREATE, Permissions.READ, Permissions.WRITE, Permissions.DELETE});
		permMatrix.put(Role.ROLE_GUEST, new Permissions[] {Permissions.READ, Permissions.WRITE});
	}
	
	@Override
	public boolean supports(String targetType) {
		return true;
	}

	@Override
	protected Set<Permissions> evaluatePermissions(User user, Serializable targetId, String targetType) {
		Set<Permissions> resultPerms = new HashSet<Permissions>();
		
		for(UserRole role : user.getRoles())
			resultPerms.addAll(Arrays.asList(permMatrix.get(role.getRole())));
		
		return resultPerms;
	}

	public Map<Role, Permissions[]> getPermMatrix() {
		return permMatrix;
	}
	
}
