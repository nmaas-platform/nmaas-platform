package net.geant.nmaas.portal.api.market;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UsersController {
    private static final Logger log = LogManager.getLogger(UsersController.class);

	@Autowired
	UserService userService;
	
	@Autowired
	DomainService domains;

	@Autowired
	ModelMapper modelMapper;
	
	@Autowired
	PasswordEncoder passwordEncoder;

	@GetMapping("/users")
	@PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasRole('ROLE_DOMAIN_ADMIN')")
	public List<User> getUsers(Pageable pageable) {
		return userService.findAll(pageable).getContent().stream().map(user -> modelMapper.map(user, User.class)).collect(Collectors.toList());
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
			user = userService.register(newUserRequest.getUsername());
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
			userService.update(user);
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
	public void updateUser(@PathVariable("userId") final Long userId, @RequestBody final UserRequest userRequest, final Principal principal) throws ProcessingException, MissingElementException {
		net.geant.nmaas.portal.persistent.entity.User userDetails = userService.findById(userId).orElseThrow(() -> new MissingElementException("User not found."));

        String message = getMessageWhenUserUpdated(userDetails, userRequest);
        final net.geant.nmaas.portal.persistent.entity.User adminUser =
                userService.findByUsername(principal.getName()).get();
        final String adminRoles = getRoleAsString(adminUser.getRoles());
        final String userRoles = getRoleAsString(userDetails.getRoles());

		if(userRequest.getUsername() != null && !userDetails.getUsername().equals(userRequest.getUsername())) {
			if(userService.existsByUsername(userRequest.getUsername()))
				throw new ProcessingException("Unable to change username.");

			userDetails.setUsername(userRequest.getUsername());
		}

		if(userRequest.getPassword() != null)
			userDetails.setPassword(passwordEncoder.encode(userRequest.getPassword()));

		if(userRequest.getFirstname() != null)
			userDetails.setFirstname(userRequest.getFirstname());
		if(userRequest.getLastname() != null)
			userDetails.setLastname(userRequest.getLastname());
		if(userRequest.getEmail() != null)
			userDetails.setEmail(userRequest.getEmail());
		userDetails.setEnabled(userRequest.isEnabled());
		if(userRequest.getRoles() != null && !userRequest.getRoles().isEmpty())
			userDetails.clearRoles(); //we have to update it in two transactions, otherwise hibernate won't remove orphans
		try {
            userService.update(userDetails);
		} catch (net.geant.nmaas.portal.exceptions.ProcessingException e) {
			throw new ProcessingException("Unable to modify user");
		}


		if(userRequest.getRoles() != null && !userRequest.getRoles().isEmpty()) {
			Set<net.geant.nmaas.portal.persistent.entity.UserRole> roles = userRequest.getRoles().stream()
					.map(ur -> new net.geant.nmaas.portal.persistent.entity.UserRole(
							userDetails,
							domains.findDomain(ur.getDomainId()).get(),
							ur.getRole()))
					.collect(Collectors.toSet());

			userDetails.setNewRoles(roles);
		}
		try {
            userService.update(userDetails);
            log.info(String.format("Admin user name - %s with role - %s, has updated the user - %s with role - %s. The following changes are - ",
                    principal.getName(),
                    adminRoles,
                    userDetails.getUsername(),
                    userRoles));
            log.info(message);
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
	public void removeUserRole(@PathVariable final Long userId,
                               @RequestBody final UserRole userRole,
                               final Principal principal) throws ProcessingException, MissingElementException {
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

            final net.geant.nmaas.portal.persistent.entity.User adminUser =
                    userService.findByUsername(principal.getName()).get();

            final String adminRoles = getRoleAsString(adminUser.getRoles());

            log.info(String.format("Admin user name - %s with role - %s, has removed role - %s of user name - %s. The domain id is  - %d",
                    principal.getName(),
                    adminRoles,
					userRole.getRole().authority(),
                    user.getUsername(),
                    userRole.getDomainId()));

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


	@PostMapping(value="/users/my/complete")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@PreAuthorize("hasRole('ROLE_INCOMPLETE')")
	@Transactional
	public void completeRegistration(Principal principal, @RequestBody UserRequest userRequest) throws MissingElementException, ProcessingException {
		net.geant.nmaas.portal.persistent.entity.User user = userService.findByUsername(principal.getName()).orElseThrow(() -> new MissingElementException("Internal error. User not found."));
		try {
			Long domainId = domains.getGlobalDomain().orElseThrow(() -> new ProcessingException()).getId();
			completeRegistration(userRequest, user, domainId);
		} catch (net.geant.nmaas.portal.exceptions.ProcessingException e) { //TODO: Refactor exceptions not to have same names
			throw new ProcessingException("Unable to complete your registration");
		}
	}

	public void completeRegistration(UserRequest userRequest, net.geant.nmaas.portal.persistent.entity.User user, Long domainId) throws net.geant.nmaas.portal.exceptions.ProcessingException {
		if(userRequest.getUsername() != null)
			user.setUsername(userRequest.getUsername());
		if(userRequest.getFirstname() != null)
			user.setFirstname(userRequest.getFirstname());
		if(userRequest.getLastname() != null)
			user.setLastname(userRequest.getLastname());
		if(userRequest.getEmail() != null) {
			user.setEmail(userRequest.getEmail());
			domains.removeMemberRole(domainId, user.getId(), Role.ROLE_INCOMPLETE);
		}

		userService.update(user);
	}

	@PostMapping("/users/my/auth/basic/password")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@Transactional
	public void changePassword(Principal principal, @RequestBody PasswordChange passwordChange) throws ProcessingException {
		net.geant.nmaas.portal.persistent.entity.User user = userService.findByUsername(principal.getName()).orElseThrow(() -> new ProcessingException("Internal error. User not found."));
		try {
			changePassword(user, passwordChange.getPassword());
		} catch (net.geant.nmaas.portal.exceptions.ProcessingException e) {
			throw new ProcessingException("Unable to change password");
		}
	}
	
	private void changePassword(net.geant.nmaas.portal.persistent.entity.User user, String password) throws net.geant.nmaas.portal.exceptions.ProcessingException {
		user.setPassword(passwordEncoder.encode(password));
		userService.update(user);
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
	public void addUserRole(@PathVariable final Long domainId,
							@PathVariable final Long userId,
							@RequestBody final UserRole userRole,
							final Principal principal) throws ProcessingException, MissingElementException {

		if(userRole == null)
			throw new MissingElementException("Empty request");

		if(userRole.getRole() == null)
			throw new MissingElementException("Missing role");
		Role role = userRole.getRole();
		
		if(!domainId.equals(userRole.getDomainId()))
			throw new ProcessingException("Invalid request domain");
								
		final Domain domain = getDomain(domainId);
		final Domain globalDomain = domains.getGlobalDomain().orElseThrow(() -> new MissingElementException("Global domain not found"));
		
		if(domain.equals(globalDomain)) {
			if(!(role == Role.ROLE_SUPERADMIN || role == Role.ROLE_TOOL_MANAGER || role == Role.ROLE_OPERATOR || role == Role.ROLE_GUEST))
				throw new ProcessingException("Role cannot be assigned.");			
		} else {
			if(!(role == Role.ROLE_GUEST || role == Role.ROLE_USER || role == Role.ROLE_DOMAIN_ADMIN))
				throw new ProcessingException("Role cannot be assigned.");
		}
			
		final net.geant.nmaas.portal.persistent.entity.User user = getUser(userId);;

		try {
			domains.addMemberRole(domain.getId(), user.getId(), role);

            final net.geant.nmaas.portal.persistent.entity.User adminUser =
                    userService.findByUsername(principal.getName()).get();
            final String adminRoles = getRoleAsString(adminUser.getRoles());

            log.info(String.format("Admin user name - %s with role - %s, has added a role - %s to user name - %s. The domain id is - %d.",
                    principal.getName(),
                    adminRoles,
                    userRole.getRole().authority(),
                    user.getUsername(),
                    domain.getId()
            ));
		} catch (ObjectNotFoundException e) {
			throw new MissingElementException(e.getMessage());
		}		
	}
	
	@DeleteMapping("/domains/{domainId}/users/{userId}/roles/{userRole}")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
	@Transactional
	public void removeUserRole(@PathVariable final long domainId,
                                                  @PathVariable final Long userId,
                                                  @PathVariable final String userRole,
                                                  final Principal principal) throws ProcessingException, MissingElementException {
		final Role role = convertRole(userRole);

		final Domain domain = Optional.of(getDomain(domainId)).orElseThrow(() -> new MissingElementException("Domain not found"));
		final net.geant.nmaas.portal.persistent.entity.User user = getUser(userId);
				
		try {
			domains.removeMemberRole(domain.getId(), user.getId(), role);

            final net.geant.nmaas.portal.persistent.entity.User adminUser =
                    userService.findByUsername(principal.getName()).get();

            final String adminRoles = getRoleAsString(adminUser.getRoles());

            log.info(String.format("Admin user name - %s with role - %s, has removed role - %s of user name - %s. The domain id is  - %d",
                    principal.getName(),
                    adminRoles,
					role.authority(),
                    user.getUsername(),
                    domainId));
			addGlobalGuestUserRoleIfMissing(userId);
		} catch (ObjectNotFoundException e) {
			throw new MissingElementException(e.getMessage());
		}
	}

    @PutMapping("/users/status/{userId}")
    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<String> setEnabledFlag(@PathVariable Long userId,
                                                 @RequestParam("enabled") boolean isEnabledFlag,
                                                 Principal principal) throws MissingElementException{
        userService.setEnabledFlag(userId, isEnabledFlag);

        net.geant.nmaas.portal.persistent.entity.User user =
                userService.findByUsername(principal.getName()).get();
        List<Role> rolesList = user.getRoles().stream().map(x-> x.getRole()).collect(Collectors.toList());
        List<String> rolesAsStringList = rolesList.stream().map(x-> x.authority()).collect(Collectors.toList());
        String roleAsString = rolesAsStringList.stream().collect(Collectors.joining(","));
        String message = String.format("User %s account has been %s by user %s with role %s.",
                getUser(userId).getUsername(),
                isEnabledFlag ? "activated" : "deactivated",
                principal.getName(),
                roleAsString);
        log.info(message);
        return new ResponseEntity<>(message, HttpStatus.ACCEPTED);
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
		return userService.findById(userId).orElseThrow(() -> new MissingElementException("User not found"));
	}

	protected String getRoleAsString(List<net.geant.nmaas.portal.persistent.entity.UserRole> userRoles){
        return getRoleAsList(userRoles).stream().collect(Collectors.joining(", "));
    }
	
	protected List<String> getRoleAsList(List<net.geant.nmaas.portal.persistent.entity.UserRole> userRoles){
        final List<Role> rolesList = userRoles.stream().map(x-> x.getRole()).collect(Collectors.toList());
        return rolesList.stream().map(x-> x.authority()).collect(Collectors.toList());
    }

    protected String getRequestedRoleAsString(Set<UserRole> userRoles){
        return getRequestedRoleAsList(userRoles).stream().collect(Collectors.joining(","));
    }
    
    protected List<String> getRequestedRoleAsList(Set<UserRole> userRoles){
        final List<Role> rolesList = userRoles.stream().map(x-> x.getRole()).collect(Collectors.toList());
        return rolesList.stream().map(x-> x.authority()).collect(Collectors.toList());
    }

    private boolean isSame(String newDetail, String oldDetail){
        newDetail  = StringUtils.isEmpty(newDetail) ? "" : newDetail;
        oldDetail  =  StringUtils.isEmpty(oldDetail) ? "" : oldDetail;

        return newDetail.equalsIgnoreCase(oldDetail) ? true : false;
    }

	private boolean isSame(List<String> requestRoleList, List<String> userRoleList) {
		return requestRoleList.containsAll(userRoleList) && userRoleList.containsAll(requestRoleList);
	}

    protected String getMessageWhenUserUpdated(final net.geant.nmaas.portal.persistent.entity.User user, final UserRequest userRequest){
        String message = "";
        if(!isSame(userRequest.getUsername(), user.getUsername())){
            message = message + System.lineSeparator() + "||| Username changed from - " + user.getUsername() + " to - " + userRequest.getUsername() + "|||" ;
        }
        if(!isSame(userRequest.getEmail(), user.getEmail())){
            message =  message + System.lineSeparator() + "||| Email changed from - " + user.getEmail() + " to - " + userRequest.getEmail() + "|||";
        }
        if(!isSame(userRequest.getFirstname(), user.getFirstname())){
            message =  message + System.lineSeparator() + "||| First name changed from - " + user.getFirstname() + " to - " + userRequest.getFirstname() + "|||";
        }
        if(!isSame(userRequest.getLastname(), user.getLastname())){
            message =  message + System.lineSeparator() + "||| Last name changed from - " + user.getLastname() + " to - " + userRequest.getLastname() + "|||";
        }
        if(!userRequest.isEnabled() == user.isEnabled()){
            message =  message + System.lineSeparator() + "||| Enabled flag changed from - " + user.isEnabled() + " to - " + userRequest.isEnabled() + "|||";
        }
        if(!isSame(getRequestedRoleAsList(userRequest.getRoles()), getRoleAsList(user.getRoles()))){
            message = message + System.lineSeparator() + "||| Role changed from - " + getRoleAsString(user.getRoles()) + " to - " + getRequestedRoleAsString(userRequest.getRoles()) + "|||";
        }
        return message;
    }
}

