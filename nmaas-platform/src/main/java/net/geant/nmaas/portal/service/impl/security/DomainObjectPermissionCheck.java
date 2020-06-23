package net.geant.nmaas.portal.service.impl.security;

import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.service.AclService.Permissions;
import net.geant.nmaas.portal.service.DomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

@Component
public class DomainObjectPermissionCheck extends BasePermissionCheck {

	static final String DOMAIN = "domain";

	@Autowired
	private DomainService domains;

	private final EnumMap<Role, Permissions[]> globalPermMatrix = new EnumMap<>(Role.class);
	private final EnumMap<Role, Permissions[]> permMatrix = new EnumMap<>(Role.class);

	public DomainObjectPermissionCheck() {
		globalPermMatrix.put(Role.ROLE_SYSTEM_ADMIN, new Permissions[] {Permissions.CREATE, Permissions.DELETE, Permissions.OWNER, Permissions.READ, Permissions.WRITE});
		globalPermMatrix.put(Role.ROLE_OPERATOR, new Permissions[]{Permissions.READ, Permissions.WRITE});
		globalPermMatrix.put(Role.ROLE_TOOL_MANAGER, new Permissions[] {Permissions.READ });
		globalPermMatrix.put(Role.ROLE_DOMAIN_ADMIN, new Permissions[] {Permissions.READ});
		globalPermMatrix.put(Role.ROLE_USER, new Permissions[] {Permissions.READ});
		globalPermMatrix.put(Role.ROLE_GUEST, new Permissions[] {});		

		permMatrix.put(Role.ROLE_SYSTEM_ADMIN, new Permissions[] {Permissions.CREATE, Permissions.DELETE, Permissions.OWNER, Permissions.READ, Permissions.WRITE});
		permMatrix.put(Role.ROLE_OPERATOR, new Permissions[] {Permissions.READ, Permissions.WRITE});
		permMatrix.put(Role.ROLE_DOMAIN_ADMIN, new Permissions[] {Permissions.READ});
		permMatrix.put(Role.ROLE_USER, new Permissions[] {Permissions.READ});
		permMatrix.put(Role.ROLE_TOOL_MANAGER, new Permissions[] {Permissions.READ});
		permMatrix.put(Role.ROLE_GUEST, new Permissions[] {Permissions.READ});
	}

	@Override
	public boolean supports(String targetType) {		
		return DOMAIN.equalsIgnoreCase(targetType);
	}

	protected Set<Permissions> evaluatePermissions(User user, Serializable targetId, String targetType) {
		if (targetId != null && !(targetId instanceof Long)) {
			throw new IllegalArgumentException("targetId is not a valid type of " + Long.class.getSimpleName());
		}
		if (!supports(targetType)) {
			throw new IllegalArgumentException("targetType not supported");
		}
		if (user == null) {
			throw new IllegalArgumentException("user is missing");
		}
		
		Set<Permissions> resultPerms = new HashSet<>();

		Domain domain = (targetId != null
				? domains.findDomain((Long)targetId).orElseThrow(() -> new IllegalStateException("Domain not found."))
				: domains.getGlobalDomain().orElseThrow(() -> new IllegalStateException("Global domain not found.")));

		for(UserRole role : user.getRoles()) {
			if (domains.getGlobalDomain().orElseThrow(() -> new IllegalArgumentException("Global domain not found")).equals(role.getDomain())) {
				resultPerms.addAll(Arrays.asList(globalPermMatrix.get(role.getRole())));
			} else if (role.getDomain().equals(domain)) {
				resultPerms.addAll(Arrays.asList(permMatrix.get(role.getRole())));
			}
		}
		
		return resultPerms;
	}

}
