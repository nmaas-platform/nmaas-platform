package net.geant.nmaas.portal.service;

import java.io.Serializable;

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
