package net.geant.nmaas.portal.service.impl.security;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.service.AclService.Permissions;
import net.geant.nmaas.portal.service.DomainService;

@Component
public class AppInstancePermissionCheck extends BasePermissionCheck {

	public final static String APPINSTANCE = "appInstance";
	
	
	@Autowired
	AppInstanceRepository appInstances;
	
	@Autowired
	DomainService domains;

	
	final protected Map<Role, Permissions[]> globalPermMatrix = new HashMap<Role, Permissions[]>();
	final protected Map<Role, Permissions[]> permMatrix = new HashMap<Role, Permissions[]>(); 
	final protected static Permissions[] OWNER_DEFAULT_PERMS = new Permissions[] {Permissions.CREATE, Permissions.DELETE, Permissions.READ, Permissions.WRITE, Permissions.OWNER};
	
	public AppInstancePermissionCheck() {
		
		globalPermMatrix.put(Role.ROLE_SUPERADMIN, new Permissions[] {Permissions.CREATE, Permissions.DELETE, Permissions.OWNER, Permissions.READ, Permissions.WRITE});
		globalPermMatrix.put(Role.ROLE_TOOL_MANAGER, new Permissions[] {Permissions.READ});
		globalPermMatrix.put(Role.ROLE_DOMAIN_ADMIN, new Permissions[] {});
		globalPermMatrix.put(Role.ROLE_USER, new Permissions[] {});
		globalPermMatrix.put(Role.ROLE_GUEST, new Permissions[] {});	
		
		permMatrix.put(Role.ROLE_SUPERADMIN, new Permissions[] {Permissions.CREATE, Permissions.DELETE, Permissions.READ, Permissions.WRITE, Permissions.OWNER});
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
		if(targetId != null && !(targetId instanceof Long)) 
			throw new IllegalArgumentException("targetId is not a valid type of " + Long.class.getSimpleName());
		if(!APPINSTANCE.equalsIgnoreCase(targetType))
			throw new IllegalArgumentException("targetType not supported");
		if(user == null)
			throw new IllegalArgumentException("user is missing");

		
		Set<Permissions> resultPerms = new HashSet<Permissions>();
		
		AppInstance appInstance = appInstances.findOne((Long)targetId);
		if(appInstance == null)
			return resultPerms;
		
		Domain domain = (appInstance.getDomain() != null ? appInstance.getDomain() : domains.getGlobalDomain().get());
		
		if(appInstance != null && appInstance.getOwner() != null && appInstance.getOwner().equals(user))
			resultPerms.addAll(Arrays.asList(OWNER_DEFAULT_PERMS));
		else
			for(UserRole role : user.getRoles())
				if(role.getDomain().equals(domain))
					resultPerms.addAll(Arrays.asList(permMatrix.get(role.getRole())));
				else if(domains.getGlobalDomain().get().equals(role.getDomain()))
					resultPerms.addAll(Arrays.asList(globalPermMatrix.get(role.getRole())));
		
		return resultPerms;
	}

}
