package net.geant.nmaas.portal.api.security;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import net.geant.nmaas.portal.persistent.entity.Comment;
import net.geant.nmaas.portal.persistent.repositories.CommentRepository;

public class ApiPermissionEvaluator implements PermissionEvaluator {

	@Autowired
	private CommentRepository commentRepository;
	
	public ApiPermissionEvaluator() {
		
	}

	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		return false;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
			Object permission) {
		
		if(authentication == null || targetId == null || targetType == null || permission == null || !(permission instanceof String))
			return false;
		
		String targetName;
		String permissionName = (String)permission;
		
		if(targetType instanceof String)
			targetName = targetType;
		else
			targetName = targetType.getClass().getSimpleName();
		targetName = targetName.toLowerCase();
		
		permissionName = permissionName.toLowerCase();
		
		switch(targetType) {
			case "comment": if(!(targetId instanceof Long))
								return false;
							else
								return hasPermissionComment(authentication, (Long) targetId, permissionName);
		}
		
		return false;
	}

	private boolean hasPermissionComment(Authentication authentication, Long commentId, String permission) {
		Comment comment = commentRepository.findOne(commentId);
		switch(permission) {
			case "owner":
				return (comment != null && comment.getOwner() != null && comment.getOwner().getUsername() == authentication.getName());
		}
		
		return false;
	}
	
}
