package net.geant.nmaas.portal.api.security;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Comment;
import net.geant.nmaas.portal.persistent.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.persistent.repositories.CommentRepository;

@Component
public class ApiPermissionEvaluator implements PermissionEvaluator {

	@Autowired
	private CommentRepository commentRepository;
	
	@Autowired
	private AppInstanceRepository appInstanceRepository;
	
	public ApiPermissionEvaluator() {
		
	}

	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		return false;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
			Object permission) {
		
		if(authentication == null || authentication.getName() == null || targetId == null || targetType == null || permission == null || !(permission instanceof String))
			return false;
		
		String targetName;
		String permissionName = (String)permission;
		
		if(targetType instanceof String)
			targetName = targetType;
		else
			targetName = targetType.getClass().getSimpleName();
		targetName = targetName.toLowerCase();
		
		permissionName = permissionName.toLowerCase();
		
		switch(targetName) {
			case "comment": if(!(targetId instanceof Long))
								return false;
							else
								return hasPermissionComment(authentication, (Long) targetId, permissionName);
			case "appinstance": if(!(targetId instanceof Long))
									return false;
								else
									return hasPermissionAppInstance(authentication, (Long) targetId, permissionName);
		}
		
		return false;
	}

	private boolean hasPermissionComment(Authentication authentication, Long commentId, String permission) {
		Comment comment = commentRepository.findOne(commentId);
		switch(permission) {
			case "owner":
				return (comment != null && comment.getOwner() != null && authentication.getName().equals(comment.getOwner().getUsername()));
		}
		
		return false;
	}

	private boolean hasPermissionAppInstance(Authentication authentication, Long appInstanceId, String permission) {
		AppInstance appInstance = appInstanceRepository.findOne(appInstanceId);
		switch(permission) {
			case "owner":
				return (appInstance != null && appInstance.getOwner() != null && authentication.getName().equals(appInstance.getOwner().getUsername()));
		}
		
		return false;
	}

}
