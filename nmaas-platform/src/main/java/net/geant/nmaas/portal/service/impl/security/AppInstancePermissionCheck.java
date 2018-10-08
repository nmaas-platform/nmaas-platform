package net.geant.nmaas.portal.service.impl.security;

import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.service.AclService.Permissions;
import net.geant.nmaas.portal.service.DomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Component
public class AppInstancePermissionCheck extends BasePermissionCheck {

	static final String APPINSTANCE = "appInstance";

	private static final Permissions[] OWNER_DEFAULT_PERMS = new Permissions[] {Permissions.CREATE, Permissions.DELETE, Permissions.READ, Permissions.WRITE, Permissions.OWNER};
	private final Map<Role, Permissions[]> globalPermMatrix = new HashMap<>();
	private final Map<Role, Permissions[]> permMatrix = new HashMap<>();

	@Autowired
	private AppInstanceRepository appInstanceRepository;

	@Autowired
	private DomainService domainService;

	public AppInstancePermissionCheck() {
		globalPermMatrix.put(Role.ROLE_SUPERADMIN, new Permissions[] {Permissions.CREATE, Permissions.DELETE, Permissions.OWNER, Permissions.READ, Permissions.WRITE});
		globalPermMatrix.put(Role.ROLE_OPERATOR, new Permissions[]{Permissions.READ});
		globalPermMatrix.put(Role.ROLE_TOOL_MANAGER, new Permissions[] {Permissions.READ});
		globalPermMatrix.put(Role.ROLE_DOMAIN_ADMIN, new Permissions[] {});
		globalPermMatrix.put(Role.ROLE_USER, new Permissions[] {});
		globalPermMatrix.put(Role.ROLE_GUEST, new Permissions[] {});	
		
		permMatrix.put(Role.ROLE_SUPERADMIN, new Permissions[] {Permissions.CREATE, Permissions.DELETE, Permissions.READ, Permissions.WRITE, Permissions.OWNER});
		permMatrix.put(Role.ROLE_OPERATOR, new Permissions[]{Permissions.READ});
		permMatrix.put(Role.ROLE_DOMAIN_ADMIN, new Permissions[] {Permissions.CREATE, Permissions.DELETE, Permissions.READ, Permissions.WRITE, Permissions.OWNER});
		permMatrix.put(Role.ROLE_USER, new Permissions[] {Permissions.CREATE, Permissions.READ});
		permMatrix.put(Role.ROLE_TOOL_MANAGER, new Permissions[] {Permissions.READ});
		permMatrix.put(Role.ROLE_GUEST, new Permissions[] {Permissions.READ});	
	}
	
	@Override
	public boolean supports(String targetType) {		
		return APPINSTANCE.equalsIgnoreCase(targetType);
	}

	@Override
	protected Set<Permissions> evaluatePermissions(User user, Serializable targetId, String targetType) {
		checkNotNull(targetId, "targetId is missing");
		checkArgument(targetId instanceof Long, "targetId is not a valid type of " + Long.class.getSimpleName());
		checkArgument(APPINSTANCE.equalsIgnoreCase(targetType), "targetType not supported");
		checkArgument(user != null, "user is missing");

		Set<Permissions> resultPerms = new HashSet<>();

		Optional<AppInstance> appInstance = appInstanceRepository.findById((Long)targetId);

		if(appInstance.isPresent()) {
			Domain domain = (appInstance.get().getDomain() != null
					? appInstance.get().getDomain()
					: domainService.getGlobalDomain().orElse(null));

			if (appInstance.get().getOwner() != null && appInstance.get().getOwner().equals(user))
				resultPerms.addAll(Arrays.asList(OWNER_DEFAULT_PERMS));
			else
				for (UserRole role : user.getRoles())
					if (role.getDomain().equals(domain))
						resultPerms.addAll(Arrays.asList(permMatrix.get(role.getRole())));
					else if (role.getDomain().equals(domainService.getGlobalDomain().orElse(null)))
						resultPerms.addAll(Arrays.asList(globalPermMatrix.get(role.getRole())));
		}
		return resultPerms;
	}

}
