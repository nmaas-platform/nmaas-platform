package net.geant.nmaas.portal.service.impl.security;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.geant.nmaas.portal.persistent.entity.Comment;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.CommentRepository;
import net.geant.nmaas.portal.service.AclService.Permissions;

@Component
public class CommentPermissionCheck extends BasePermissionCheck {
	public final static String COMMENT = "comment";
			
	@Autowired
	CommentRepository comments;
	
	final protected Map<Role, Permissions[]> permMatrix = new HashMap<Role, Permissions[]>(); 
	
	final protected static Permissions[] OWNER_DEFAULT_PERMS = new Permissions[] {Permissions.CREATE, Permissions.DELETE, Permissions.READ, Permissions.WRITE, Permissions.OWNER};
	
	public CommentPermissionCheck() {
		permMatrix.put(Role.SUPERADMIN, new Permissions[] {Permissions.CREATE, Permissions.DELETE, Permissions.READ, Permissions.WRITE, Permissions.OWNER});
		permMatrix.put(Role.DOMAIN_ADMIN, new Permissions[] {Permissions.CREATE, Permissions.READ});
		permMatrix.put(Role.USER, new Permissions[] {Permissions.CREATE, Permissions.READ});
		permMatrix.put(Role.TOOL_MANAGER, new Permissions[] {Permissions.CREATE, Permissions.DELETE, Permissions.READ, Permissions.WRITE, Permissions.OWNER});
		permMatrix.put(Role.GUEST, new Permissions[] {Permissions.CREATE, Permissions.READ});	
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
		
		Set<Permissions> resultPerms = new HashSet<Permissions>();
		
		Comment comment = (targetId != null ? comments.findOne((Long)targetId) : null); 
		
		if(comment != null && comment.getOwner() != null && comment.getOwner().equals(user))
			resultPerms.addAll(Arrays.asList(OWNER_DEFAULT_PERMS));
		else
			for(UserRole role : user.getRoles())
				resultPerms.addAll(Arrays.asList(permMatrix.get(role.getRole())));

		return resultPerms;
	}



}
