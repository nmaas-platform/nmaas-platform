package net.geant.nmaas.portal.api.security;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.AclService;
import net.geant.nmaas.portal.service.AclService.Permissions;
import net.geant.nmaas.portal.service.UserService;

@Component
public class ApiPermissionEvaluator implements PermissionEvaluator {
	
	@Autowired
	private UserService users;
	
	@Autowired
	AclService aclService;
	
	public ApiPermissionEvaluator() {
		
	}

	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		return false;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
			Object permission) {
				
		if(authentication == null || authentication.getName() == null || targetType == null || permission == null || !(permission instanceof String))
			return false;
						
		String targetName;
		String permissionStr = (String)permission;
		
		if(targetType instanceof String)
			targetName = targetType;
		else
			targetName = targetType.getClass().getSimpleName();
		targetName = targetName.toLowerCase();
								
		User user = users.findByUsername(authentication.getName()).get();
		if(user == null)
			return false;
		
		Permissions[] perms = convertToPermissions(permissionStr);
		
		return aclService.isAuthorized(user.getId(), targetId, targetType, perms);		
	}

	private Permissions[] convertToPermissions(String permissionStr) {
		
		Set<Permissions> perms = new HashSet<Permissions>();
		
		String[] permArray = (permissionStr != null ? permissionStr.trim().split(",") : null); 
		if(permArray != null) {
			for(String perm : permArray)
				if(perm != null || perm.trim().length() > 0)
					perms.add(Permissions.valueOf(perm));
		}
		
		return perms.toArray(new Permissions[perms.size()]);
	}

}
