package net.geant.nmaas.portal.service.impl.security;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.persistent.entity.*;
import net.geant.nmaas.portal.persistent.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.service.AclService.Permissions;
import net.geant.nmaas.portal.service.DomainService;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Component
@Log4j2
public class AppInstancePermissionCheck extends BasePermissionCheck {

	static final String APPINSTANCE = "appInstance";

	private final EnumMap<Role, Permissions[]> globalPermMatrix = new EnumMap<>(Role.class);
	private final EnumMap<Role, Permissions[]> permMatrix = new EnumMap<>(Role.class);

	private final AppInstanceRepository appInstanceRepository;

	private final DomainService domainService;

	public AppInstancePermissionCheck(AppInstanceRepository appInstanceRepository, DomainService domainService) {
		super();
		this.appInstanceRepository = appInstanceRepository;
		this.domainService = domainService;
		this.setupMatrix();
	}

	@Override
	protected void setupMatrix() {
		globalPermMatrix.put(Role.ROLE_SYSTEM_ADMIN, new Permissions[] {Permissions.CREATE, Permissions.DELETE, Permissions.OWNER, Permissions.READ, Permissions.WRITE});
		globalPermMatrix.put(Role.ROLE_OPERATOR, new Permissions[] {Permissions.READ});
		globalPermMatrix.put(Role.ROLE_TOOL_MANAGER, new Permissions[] {Permissions.READ});
		globalPermMatrix.put(Role.ROLE_DOMAIN_ADMIN, new Permissions[] {});
		globalPermMatrix.put(Role.ROLE_USER, new Permissions[] {});
		globalPermMatrix.put(Role.ROLE_GUEST, new Permissions[] {});
		globalPermMatrix.put(Role.ROLE_VL_DOMAIN_ADMIN, new Permissions[] {});
		globalPermMatrix.put(Role.ROLE_VL_MANAGER, new Permissions[] {});

		permMatrix.put(Role.ROLE_DOMAIN_ADMIN, new Permissions[] {Permissions.CREATE, Permissions.DELETE, Permissions.READ, Permissions.WRITE, Permissions.OWNER});
		permMatrix.put(Role.ROLE_VL_DOMAIN_ADMIN, new Permissions[] {Permissions.CREATE, Permissions.DELETE, Permissions.READ, Permissions.WRITE, Permissions.OWNER});
		permMatrix.put(Role.ROLE_USER, new Permissions[] {Permissions.READ});
		permMatrix.put(Role.ROLE_GUEST, new Permissions[] {});
		permMatrix.put(Role.ROLE_VL_MANAGER, new Permissions[] {Permissions.READ});
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

			for (UserRole role : user.getRoles()) {
				if(role.getDomain() == null) {
					continue;
				}
				if (role.getDomain().equals(domain)) {
					resultPerms.addAll(Arrays.asList(permMatrix.get(role.getRole())));
				} else if (role.getDomain().equals(domainService.getGlobalDomain().orElse(null))) {
					resultPerms.addAll(Arrays.asList(globalPermMatrix.get(role.getRole())));
				}
			}

			// explicitly add READ permission if user is member of the app instance
			if(appInstance.get().getMembers().contains(user)) {
				resultPerms.add(Permissions.READ);
			}
		}
		return resultPerms;
	}

}
