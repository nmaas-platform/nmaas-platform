package net.geant.nmaas.portal.service.impl.security;

import java.io.Serializable;
import java.util.Set;

import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.AclService.Permissions;
import net.geant.nmaas.portal.service.impl.security.AclServiceImpl.PermissionCheck;

public abstract class BasePermissionCheck implements PermissionCheck {

	public BasePermissionCheck() {
		super();
	}

	protected boolean hasPermission(Set<Permissions> userPerms, Permissions perm) {
		if(userPerms == null || userPerms.isEmpty())
			return false;
		if(perm == null)
			return true;
		
		if(perm == Permissions.ANY)
			return true;
		
		return (userPerms.contains(perm));
	}
	
	protected boolean hasPermission(Set<Permissions> userPerms, Permissions[] perms) {		
		for(Permissions perm : perms)
			if(hasPermission(userPerms, perm))
				return true;
			
		return false;
	}

	@Override
	public boolean check(User user, Serializable targetId, String targetType, Permissions perm) {				
		return check(user, targetId, targetType, new Permissions[] {perm});
	}

	@Override
	public boolean check(User user, Serializable targetId, String targetType, Permissions[] perms) {
		if(supports(targetType)) {
			
			Set<Permissions> userPerms = evaluatePermissions(user, targetId, targetType);
			
			return hasPermission(userPerms, perms);
			
		}
		return false;
	}

	protected abstract Set<Permissions> evaluatePermissions(User user, Serializable targetId, String targetType);
	
}