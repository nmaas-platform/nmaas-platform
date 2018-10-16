package net.geant.nmaas.portal.api.security;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.AclService;
import net.geant.nmaas.portal.service.AclService.Permissions;
import net.geant.nmaas.portal.service.UserService;

@Component
@NoArgsConstructor
public class ApiPermissionEvaluator implements PermissionEvaluator {
	
	private UserService users;
	
	private AclService aclService;

	@Autowired
	public ApiPermissionEvaluator(UserService userService, AclService aclService){
		this.users = userService;
		this.aclService = aclService;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		return false;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
			Object permission) {
				
		if(authentication == null || authentication.getName() == null || targetType == null || !(permission instanceof String))
			return false;
						
		String permissionStr = (String)permission;
		Optional<User> user = users.findByUsername(authentication.getName());
		if(!user.isPresent())
			return false;
		
		Permissions[] perms = convertToPermissions(permissionStr);
		
		return aclService.isAuthorized(user.get().getId(), targetId, targetType, perms);
	}

	private Permissions[] convertToPermissions(String permissionStr) {
		
		Set<Permissions> perms = new HashSet<Permissions>();
		
		String[] permArray = (permissionStr != null ? permissionStr.trim().split(",") : null); 
		if(permArray != null) {
			for(String perm : permArray)
				if(perm != null && perm.trim().length() > 0)
					perms.add(Permissions.valueOf(perm));
		}
		
		return perms.toArray(new Permissions[perms.size()]);
	}

}
