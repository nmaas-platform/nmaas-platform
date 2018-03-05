package net.geant.nmaas.portal.service.impl.security;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.UserService;


@Service
public class AclService implements net.geant.nmaas.portal.service.AclService {

	@Autowired
	UserService users;	
	
	public static interface PermissionCheck {
		boolean supports(String targetType);
		boolean check(User user, Serializable targetId, String targetType, Permissions perm);
		boolean check(User user, Serializable targetId, String targetType, Permissions[] perm);
	}
	
	Set<PermissionCheck> permissionChecks = new HashSet<PermissionCheck>();
	
	PermissionCheck defaultPermissionCheck = null;
	
	public void add(PermissionCheck permissionCheck) {
		permissionChecks.add(permissionCheck);
	}
	
	@Override
	public boolean isAuthorized(Long userId, Serializable targetId, String targetType, Permissions perm) {
		
		User user = users.findById(userId).get();
		if(user == null)
			return false;
		
		
		for(PermissionCheck permCheck : permissionChecks) {
			if(permCheck.supports(targetType))
				if(permCheck.check(user, targetId, targetType, perm))
					return true;
		}
		
		return (defaultPermissionCheck != null ? defaultPermissionCheck.check(user, targetId, targetType, perm) : false);
	}

	@Override
	public boolean isAuthorized(Long userId, Serializable targetId, String targetType, Permissions[] perms) {
		User user = users.findById(userId).get();
		if(user == null)
			return false;
		
		
		for(PermissionCheck permCheck : permissionChecks) {
			if(permCheck.supports(targetType))
				if(permCheck.check(user, targetId, targetType, perms))
					return true;
		}
		
		return (defaultPermissionCheck != null ? defaultPermissionCheck.check(user, targetId, targetType, perms) : false);
	}

	public PermissionCheck getDefaultPermissionCheck() {
		return defaultPermissionCheck;
	}

	public void setDefaultPermissionCheck(PermissionCheck defaultPermissionCheck) {
		this.defaultPermissionCheck = defaultPermissionCheck;
	}
	
}
