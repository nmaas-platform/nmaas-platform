package net.geant.nmaas.portal.service.impl.security;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.geant.nmaas.portal.persistent.entity.Comment;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.CommentRepository;
import net.geant.nmaas.portal.service.AclService.Permissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class CommentPermissionCheck extends BasePermissionCheck {
	public static final String COMMENT = "comment";
			
	@Autowired
	CommentRepository comments;
	
	protected final Map<Role, Permissions[]> permMatrix = new HashMap<>();
	
	protected final static Permissions[] OWNER_DEFAULT_PERMS = new Permissions[] {Permissions.CREATE, Permissions.DELETE, Permissions.READ, Permissions.WRITE, Permissions.OWNER};
	
	public CommentPermissionCheck() {
		permMatrix.put(Role.ROLE_SYSTEM_ADMIN, new Permissions[] {Permissions.CREATE, Permissions.DELETE, Permissions.READ, Permissions.WRITE, Permissions.OWNER});
		permMatrix.put(Role.ROLE_DOMAIN_ADMIN, new Permissions[] {Permissions.CREATE, Permissions.READ});
		permMatrix.put(Role.ROLE_USER, new Permissions[] {Permissions.CREATE, Permissions.READ});
		permMatrix.put(Role.ROLE_OPERATOR, new Permissions[]{Permissions.CREATE, Permissions.READ});
		permMatrix.put(Role.ROLE_TOOL_MANAGER, new Permissions[] {Permissions.CREATE, Permissions.READ, Permissions.WRITE, Permissions.OWNER});
		permMatrix.put(Role.ROLE_GUEST, new Permissions[] {Permissions.CREATE, Permissions.READ});
	}
	
	
	@Override
	public boolean supports(String targetType) {		
		return COMMENT.equalsIgnoreCase(targetType);
	}

	@Override
	protected Set<Permissions> evaluatePermissions(User user, Serializable targetId, String targetType) {
		if(!supports(targetType))
			throw new IllegalArgumentException("targetType not supported");
		if(targetId != null && !(targetId instanceof Long)) 
			throw new IllegalArgumentException("targetId is not a valid type of " + Long.class.getSimpleName());
		if(user == null)
			throw new IllegalArgumentException("user is missing");
		
		Set<Permissions> resultPerms = new HashSet<>();

		Comment comment = null;
		if(targetId != null)
			comment = comments.findById((Long)targetId).orElse(null);
		
		if(comment != null && comment.getOwner() != null && comment.getOwner().equals(user))
			resultPerms.addAll(Arrays.asList(OWNER_DEFAULT_PERMS));
		else
			for(UserRole role : user.getRoles())
				resultPerms.addAll(Arrays.asList(permMatrix.get(role.getRole())));

		return resultPerms;
	}



}
