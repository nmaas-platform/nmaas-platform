package net.geant.nmaas.portal.service;

import java.io.Serializable;

import org.springframework.security.access.PermissionEvaluator;

public interface AclService {
	
	public enum Permissions {
		ANY,
		CREATE,
		READ,
		WRITE,
		DELETE,
		OWNER		
	};
	
	
	boolean isAuthorized(Long userId, Serializable targetId, String targetType, Permissions perm);
	boolean isAuthorized(Long userId, Serializable targetId, String targetType, Permissions[] perm);
}
