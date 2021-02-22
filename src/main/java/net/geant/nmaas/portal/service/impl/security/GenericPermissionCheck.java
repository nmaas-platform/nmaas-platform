package net.geant.nmaas.portal.service.impl.security;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.NoArgsConstructor;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.service.AclService.Permissions;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class GenericPermissionCheck extends BasePermissionCheck {

	private final EnumMap<Role, Permissions[]> permMatrix = new EnumMap<>(Role.class);

	public GenericPermissionCheck() {
		super();
		this.setupMatrix();
	}

	@Override
	protected void setupMatrix() {
		permMatrix.put(Role.ROLE_SYSTEM_ADMIN, new Permissions[] {Permissions.CREATE, Permissions.DELETE, Permissions.READ, Permissions.WRITE, Permissions.OWNER});
		permMatrix.put(Role.ROLE_OPERATOR, new Permissions[] {});
		permMatrix.put(Role.ROLE_DOMAIN_ADMIN, new Permissions[] {});
		permMatrix.put(Role.ROLE_USER, new Permissions[] {});
		permMatrix.put(Role.ROLE_TOOL_MANAGER, new Permissions[] {});
		permMatrix.put(Role.ROLE_GUEST, new Permissions[] {});
	}

	@Override
	public boolean supports(String targetType) {
		return true;
	}

	@Override
	protected Set<Permissions> evaluatePermissions(User user, Serializable targetId, String targetType) {
		Set<Permissions> resultPerms = new HashSet<>();
		
		for(UserRole role : user.getRoles())
			resultPerms.addAll(Arrays.asList(permMatrix.get(role.getRole())));
		
		return resultPerms;
	}

	public Map<Role, Permissions[]> getPermMatrix() {
		return permMatrix;
	}
	
}
