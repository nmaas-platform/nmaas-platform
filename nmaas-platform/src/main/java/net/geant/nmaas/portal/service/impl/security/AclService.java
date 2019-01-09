package net.geant.nmaas.portal.service.impl.security;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.UserService;

@Service
public class AclService implements net.geant.nmaas.portal.service.AclService {

	@Autowired
	UserService users;

	@Autowired
	public AclService(DomainObjectPermissionCheck domainObjectPermissionCheck,
					  CommentPermissionCheck commentPermissionCheck,
					  AppInstancePermissionCheck appInstancePermissionCheck,
					  AppTemplatePermissionCheck appTemplatePermissionCheck) {

		add(domainObjectPermissionCheck);
		add(commentPermissionCheck);
		add(appInstancePermissionCheck);
		add(appTemplatePermissionCheck);

		setDefaultPermissionCheck(new GenericPermissionCheck());
	}


	public interface PermissionCheck {
		boolean supports(String targetType);
		boolean check(User user, Serializable targetId, String targetType, Permissions perm);
		boolean check(User user, Serializable targetId, String targetType, Permissions[] perm);
	}
	
	private Set<PermissionCheck> permissionChecks = new HashSet<>();
	
	private PermissionCheck defaultPermissionCheck = null;
	
	public void add(PermissionCheck permissionCheck) {
		permissionChecks.add(permissionCheck);
	}
	
	@Override
	public boolean isAuthorized(Long userId, Serializable targetId, String targetType, Permissions perm) {

		Optional<User> userOptional = users.findById(userId);
		if(userOptional.isPresent()) {
			for (PermissionCheck permCheck : permissionChecks) {
				if (permCheck.supports(targetType) && permCheck.check(userOptional.get(), targetId, targetType, perm))
					return true;
			}

			return (defaultPermissionCheck != null && defaultPermissionCheck.check(userOptional.get(), targetId, targetType, perm));
		}else{
			return false;
		}
	}

	@Override
	public boolean isAuthorized(Long userId, Serializable targetId, String targetType, Permissions[] perms) {
		Optional<User> userOptional = users.findById(userId);
		if(userOptional.isPresent()) {
			for (PermissionCheck permCheck : permissionChecks) {
				if (permCheck.supports(targetType))
					if (permCheck.check(userOptional.get(), targetId, targetType, perms))
						return true;
			}

			return (defaultPermissionCheck != null && defaultPermissionCheck.check(userOptional.get(), targetId, targetType, perms));
		}else{
			return false;
		}
	}

	public PermissionCheck getDefaultPermissionCheck() {
		return defaultPermissionCheck;
	}

	public void setDefaultPermissionCheck(PermissionCheck defaultPermissionCheck) {
		this.defaultPermissionCheck = defaultPermissionCheck;
	}
	
}
