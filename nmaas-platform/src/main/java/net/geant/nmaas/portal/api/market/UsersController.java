package net.geant.nmaas.portal.api.market;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.geant.nmaas.portal.api.domain.Id;
import net.geant.nmaas.portal.api.domain.NewUserRequest;
import net.geant.nmaas.portal.api.domain.PasswordChange;
import net.geant.nmaas.portal.api.domain.User;
import net.geant.nmaas.portal.api.domain.UserRequest;
import net.geant.nmaas.portal.api.domain.UserRole;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.api.exception.SignupException;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;

@RestController
@RequestMapping("/api")
public class UsersController {

//	@Autowired
//	UserRepository userRepo;

	@Autowired
	UserService users;
	
	@Autowired
	DomainService domains;
	
	@Autowired
	ModelMapper modelMapper;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	
	
	@GetMapping("/users")
	@PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasRole('ROLE_DOMAIN_ADMIN')")
	public List<User> getUsers(Pageable pageable) {
		return users.findAll(pageable).getContent().stream().map(user -> modelMapper.map(user, User.class)).collect(Collectors.toList());
	}
	
	@GetMapping(value="/users/roles")	
	public List<Role> getRoles() {
		return Arrays.asList(Role.values());
	}
	
	@PostMapping(value="/users")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasPermission(#newUserRequest.domainId, 'domain', 'OWNER')")
	@Transactional
	public Id addUser(@RequestBody NewUserRequest newUserRequest) throws SignupException {
		net.geant.nmaas.portal.persistent.entity.User user = null;
		try {
			user = users.register(newUserRequest.getUsername());
		} catch(ObjectAlreadyExistsException ex) {
			throw new SignupException("User already exists.");
		} catch (MissingElementException e) {			
			throw new SignupException("Domain not found.");
		}

		if(user == null)
			throw new SignupException("Unable to register new user");

		user.setPassword(null);
		user.setEnabled(true);
		
		try {
			users.update(user);
		} catch (net.geant.nmaas.portal.exceptions.ProcessingException ex) {
			throw new SignupException("Unable to update newly registered user.");
		}
		
		return new Id(user.getId());
	}
	
		
	@GetMapping(value="/users/{userId}")
	@PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasRole('ROLE_DOMAIN_ADMIN')")
	public User retrieveUser(@PathVariable("userId") Long userId) throws MissingElementException {
		net.geant.nmaas.portal.persistent.entity.User user = getUser(userId);		
		return modelMapper.map(user, User.class);
	}
	
	@PutMapping(value="/users/{userId}")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@PreAuthorize("hasRole('ROLE_SUPERADMIN')")
	@Transactional
	public void updateUser(@PathVariable("userId") Long userId, @RequestBody UserRequest userRequest) throws ProcessingException, MissingElementException {
		net.geant.nmaas.portal.persistent.entity.User userMod = users.findById(userId).orElseThrow(() -> new MissingElementException("User not found."));
		
		if(userRequest.getUsername() != null && !userMod.getUsername().equals(userRequest.getUsername())) {
			if(users.existsByUsername(userRequest.getUsername()))
				throw new ProcessingException("Unable to change username.");
			
			userMod.setUsername(userRequest.getUsername());
		}
		
		if(userRequest.getPassword() != null)
			userMod.setPassword(passwordEncoder.encode(userRequest.getPassword()));

		if(userRequest.getFirstname() != null)
			userMod.setFirstname(userRequest.getFirstname());
		if(userRequest.getLastname() != null)
			userMod.setLastname(userRequest.getLastname());
		if(userRequest.getEmail() != null)
			userMod.setEmail(userRequest.getEmail());		
		userMod.setEnabled(userRequest.isEnabled());
		if(userRequest.getRoles() != null && !userRequest.getRoles().isEmpty())
			userMod.clearRoles(); //we have to update it in two transactions, otherwise hibernate won't remove orphans
		try {
			users.update(userMod);
		} catch (net.geant.nmaas.portal.exceptions.ProcessingException e) {
			throw new ProcessingException("Unable to modify user");
		}
		

		if(userRequest.getRoles() != null && !userRequest.getRoles().isEmpty()) {
			Set<net.geant.nmaas.portal.persistent.entity.UserRole> roles = userRequest.getRoles().stream()
					.map(ur -> new net.geant.nmaas.portal.persistent.entity.UserRole(
							userMod,
							domains.findDomain(ur.getDomainId()).get(), 
							ur.getRole()))
					.collect(Collectors.toSet());

			if(userMod.getEmail() == null)
				roles.add(new net.geant.nmaas.portal.persistent.entity.UserRole(userMod,
						domains.findDomain("GLOBAL").get(), //TODO: will it work?
						Role.ROLE_INCOMPLETE));
			
			userMod.setNewRoles(roles);
		}
		try {
			users.update(userMod);
		} catch (net.geant.nmaas.portal.exceptions.ProcessingException e) {
			throw new ProcessingException("Unable to modify roles");
		}
	}
	
	@DeleteMapping(value="/users/{userId}")
	@PreAuthorize("hasRole('ROLE_SUPERADMIN')")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void deleteUser(@PathVariable("userId") Long userId) throws ProcessingException {
		throw new ProcessingException("User removing not supported.");
	}
	
	@GetMapping("/users/{userId}/roles")
	@PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasRole('ROLE_DOMAIN_ADMIN')")
	public Set<UserRole> getUserRoles(@PathVariable Long userId) throws MissingElementException {		
		net.geant.nmaas.portal.persistent.entity.User user = getUser(userId);
				
		return user.getRoles().stream().map(ur -> modelMapper.map(ur, UserRole.class)).collect(Collectors.toSet());
	}
		
	@DeleteMapping("/users/{userId}/roles")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@PreAuthorize("hasRole('ROLE_SUPERADMIN')")
	@Transactional
	public void removeUserRole(@PathVariable Long userId, @RequestBody UserRole userRole) throws ProcessingException, MissingElementException {
		if(userRole.getRole() == null)
			throw new MissingElementException("Missing role");

		Domain domain = null;
		if (userRole.getDomainId() == null) 
			domain = domains.getGlobalDomain().orElseThrow(() -> new MissingElementException("Global domain not found"));
		else
			domain = domains.findDomain(userRole.getDomainId()).orElseThrow(() -> new MissingElementException("Domain not found"));		

		net.geant.nmaas.portal.persistent.entity.User user = getUser(userId);

		try {
			domains.removeMemberRole(domain.getId(), user.getId(), userRole.getRole());
			addGlobalGuestUserRoleIfMissing(userId);
		} catch (ObjectNotFoundException e) {
			throw new MissingElementException(e.getMessage());
		}		
	}

	@PostMapping("/users/{userId}/auth/basic/password")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@PreAuthorize("hasRole('ROLE_SUPERADMIN')")
	@Transactional
	public void changePassword(@PathVariable Long userId, @RequestBody PasswordChange passwordChange) throws ProcessingException, MissingElementException {
		net.geant.nmaas.portal.persistent.entity.User user = getUser(userId);
		try {
			changePassword(user, passwordChange.getPassword());
		} catch (net.geant.nmaas.portal.exceptions.ProcessingException e) {
			throw new ProcessingException("Unable to change password");
		}
	}
	
	@PostMapping("/users/my/auth/basic/password")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@Transactional
	public void changePassword(Principal principal, @RequestBody PasswordChange passwordChange) throws ProcessingException {
		net.geant.nmaas.portal.persistent.entity.User user = users.findByUsername(principal.getName()).orElseThrow(() -> new ProcessingException("Internal error. User not found."));
		try {
			changePassword(user, passwordChange.getPassword());
		} catch (net.geant.nmaas.portal.exceptions.ProcessingException e) {
			throw new ProcessingException("Unable to change password");
		}
	}
	
	private void changePassword(net.geant.nmaas.portal.persistent.entity.User user, String password) throws net.geant.nmaas.portal.exceptions.ProcessingException {
		user.setPassword(passwordEncoder.encode(password));
		users.update(user);
	}

	@GetMapping("/domains/{domainId}/users")
	@PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
	public List<User> getDomainUsers(@PathVariable Long domainId) {
		return domains.getMembers(domainId).stream().map(domain -> modelMapper.map(domain, User.class)).collect(Collectors.toList());		
	}
	
	@GetMapping("/domains/{domainId}/users/{userId}")
	@PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
	public User getDomainUser(@PathVariable Long domainId, @PathVariable Long userId) throws MissingElementException, ProcessingException {
		try {
			return modelMapper.map(domains.getMember(domainId, userId), User.class);
		} catch (ObjectNotFoundException e) {
			throw new MissingElementException(e.getMessage());
		} catch (net.geant.nmaas.portal.exceptions.ProcessingException e) {
			throw new ProcessingException(e.getMessage());
		}
	}
	
	@DeleteMapping("/domains/{domainId}/users/{userId}")
	@PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void removeDomainUser(@PathVariable Long domainId, @PathVariable Long userId) throws MissingElementException {
		Domain domain = getDomain(domainId);
		
		net.geant.nmaas.portal.persistent.entity.User user = getUser(userId);
		
		try {
			domains.removeMember(domain.getId(), user.getId());
		} catch (ObjectNotFoundException e) {
			throw new MissingElementException(e.getMessage());
		}
	}
	
	@GetMapping("/domains/{domainId}/users/{userId}/roles")
	@PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
	public Set<Role> getUserRoles(@PathVariable Long domainId, @PathVariable Long userId) throws MissingElementException {
		Domain domain = getDomain(domainId);
		
		net.geant.nmaas.portal.persistent.entity.User user = getUser(userId);

		try {
			return domains.getMemberRoles(domain.getId(), user.getId());
		} catch (ObjectNotFoundException e) {
			throw new MissingElementException(e.getMessage());
		}
	}
	
	@PostMapping("/domains/{domainId}/users/{userId}/roles")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
	@Transactional
	public void addUserRole(@PathVariable Long domainId, @PathVariable Long userId, @RequestBody UserRole userRole) throws ProcessingException, MissingElementException {

		if(userRole == null)
			throw new MissingElementException("Empty request");

		if(userRole.getRole() == null)
			throw new MissingElementException("Missing role");
		Role role = userRole.getRole();
		
		if(!domainId.equals(userRole.getDomainId()))
			throw new ProcessingException("Invalid request domain");
								
		Domain domain = getDomain(domainId);
		Domain globalDomain = domains.getGlobalDomain().orElseThrow(() -> new MissingElementException("Global domain not found"));
		
		if(domain.equals(globalDomain)) {
			if(!(role == Role.ROLE_SUPERADMIN || role == Role.ROLE_TOOL_MANAGER || role == Role.ROLE_OPERATOR || role == Role.ROLE_GUEST))
				throw new ProcessingException("Role cannot be assigned.");			
		} else {
			if(!(role == Role.ROLE_GUEST || role == Role.ROLE_USER || role == Role.ROLE_DOMAIN_ADMIN))
				throw new ProcessingException("Role cannot be assigned.");
		}
			
		net.geant.nmaas.portal.persistent.entity.User user = getUser(userId);;

		try {
			domains.addMemberRole(domain.getId(), user.getId(), role);
		} catch (ObjectNotFoundException e) {
			throw new MissingElementException(e.getMessage());
		}		
	}
	
	@DeleteMapping("/domains/{domainId}/users/{userId}/roles/{userRole}")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
	@Transactional
	public void removeUserRole(@PathVariable Long domainId, @PathVariable Long userId, @PathVariable String userRole) throws ProcessingException, MissingElementException {
		Role role = convertRole(userRole);

		Domain domain = getDomain(domainId);
		net.geant.nmaas.portal.persistent.entity.User user = getUser(userId);;
				
		try {
			domains.removeMemberRole(domain.getId(), user.getId(), role);
			addGlobalGuestUserRoleIfMissing(userId);
		} catch (ObjectNotFoundException e) {
			throw new MissingElementException(e.getMessage());
		}
	}

	private void addGlobalGuestUserRoleIfMissing(Long userId) throws MissingElementException{
		if(domains.getGlobalDomain().isPresent()){
			Long globalId = domains.getGlobalDomain().get().getId();
			try{
				if(domains.getMemberRoles(globalId, userId).isEmpty()){
					domains.addMemberRole(globalId, userId, Role.ROLE_GUEST);
				}
			} catch(ObjectNotFoundException e){
				throw new MissingElementException(e.getMessage());
			}
		}
	}

	protected Role convertRole(String userRole) throws MissingElementException {
		Role role = null;
		try {
			role = Role.valueOf(userRole);
		} catch(IllegalArgumentException ex) {
			throw new MissingElementException("Missing or invalid role");
		}
		return role;
	}
	
	protected Domain getDomain(Long domainId) throws MissingElementException {
		return domains.findDomain(domainId).orElseThrow(() -> new MissingElementException("Domain not found"));
	}
	
	protected net.geant.nmaas.portal.persistent.entity.User getUser(Long userId) throws MissingElementException {
		return users.findById(userId).orElseThrow(() -> new MissingElementException("User not found"));
	}
}

