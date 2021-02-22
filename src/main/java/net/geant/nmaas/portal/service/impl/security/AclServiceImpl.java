package net.geant.nmaas.portal.service.impl.security;

import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.AclService;
import net.geant.nmaas.portal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class AclServiceImpl implements AclService {

	private UserService users;

	@Autowired
	public AclServiceImpl(DomainObjectPermissionCheck domainObjectPermissionCheck,
						  CommentPermissionCheck commentPermissionCheck,
						  AppInstancePermissionCheck appInstancePermissionCheck,
						  AppTemplatePermissionCheck appTemplatePermissionCheck,
						  UserService userService) {
		add(domainObjectPermissionCheck);
		add(commentPermissionCheck);
		add(appInstancePermissionCheck);
		add(appTemplatePermissionCheck);
		this.users = userService;

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
		Permissions[] perms = new Permissions[1];
		perms[0] = perm;
		return this.isAuthorized(userId, targetId, targetType, perms);
	}

	@Override
	public boolean isAuthorized(Long userId, Serializable targetId, String targetType, Permissions[] perms) {
		Optional<User> userOptional = users.findById(userId);
		// if user is not present then do not authorize
		if(!userOptional.isPresent()) {
			return false;
		}

		// check all available permission checks if user is authorized
		for (PermissionCheck permCheck : permissionChecks) {
			if (permCheck.supports(targetType) && permCheck.check(userOptional.get(), targetId, targetType, perms)) {
				return true;
			}
		}

		// finally check default permission checker if available
		return (defaultPermissionCheck != null && defaultPermissionCheck.check(userOptional.get(), targetId, targetType, perms));
	}

	private void setDefaultPermissionCheck(PermissionCheck defaultPermissionCheck) {
		this.defaultPermissionCheck = defaultPermissionCheck;
	}
	
}
