package net.geant.nmaas.portal.service.impl.security;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import net.geant.nmaas.portal.service.AclService.Permissions;

@Component
public class AppTemplatePermissionCheck extends BasePermissionCheck {

	public final static String APPTEMPLATE = "appTemplate";
	
	@Autowired
	ApplicationRepository applications;
	
	final protected Map<Role, Permissions[]> permMatrix = new HashMap<Role, Permissions[]>(); 
	
	public AppTemplatePermissionCheck() {
		permMatrix.put(Role.SUPERADMIN, new Permissions[] {Permissions.CREATE, Permissions.DELETE, Permissions.READ, Permissions.WRITE, Permissions.OWNER});
		permMatrix.put(Role.DOMAIN_ADMIN, new Permissions[] {Permissions.READ});
		permMatrix.put(Role.USER, new Permissions[] { Permissions.READ});
		permMatrix.put(Role.TOOL_MANAGER, new Permissions[] { Permissions.CREATE, Permissions.DELETE, Permissions.READ, Permissions.WRITE, Permissions.OWNER });
		permMatrix.put(Role.GUEST, new Permissions[] {Permissions.READ});	
	}
	
	@Override
	public boolean supports(String targetType) {
		return APPTEMPLATE.equalsIgnoreCase(targetType);
	}

	@Override
	protected Set<Permissions> evaluatePermissions(User user, Serializable targetId, String targetType) {
		if(targetId != null && !(targetId instanceof Long)) 
			throw new IllegalArgumentException("targetId is not a valid type of " + Long.class.getSimpleName());
		if(!APPTEMPLATE.equalsIgnoreCase(targetType))
			throw new IllegalArgumentException("targetType not supported");
		if(user == null)
			throw new IllegalArgumentException("user is missing");

		
		Set<Permissions> resultPerms = new HashSet<Permissions>();
		
		for(UserRole role : user.getRoles())
			resultPerms.addAll(Arrays.asList(permMatrix.get(role.getRole())));
		
		return resultPerms;
	}

}
