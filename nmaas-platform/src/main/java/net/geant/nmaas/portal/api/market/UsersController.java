package net.geant.nmaas.portal.api.market;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.api.domain.Id;
import net.geant.nmaas.portal.api.domain.NewUserRequest;
import net.geant.nmaas.portal.api.domain.PasswordChange;
import net.geant.nmaas.portal.api.domain.User;
import net.geant.nmaas.portal.api.domain.UserRequest;
import net.geant.nmaas.portal.api.domain.UserRole;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.api.exception.SignupException;
import net.geant.nmaas.portal.api.model.EmailConfirmation;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.NotificationService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Log4j2
public class UsersController {

	private UserService userService;

    private DomainService domainService;

    private NotificationService notificationService;

    private ModelMapper modelMapper;

    private PasswordEncoder passwordEncoder;

	@Autowired
	public UsersController(UserService userService, DomainService domainService, NotificationService notificationService, ModelMapper modelMapper, PasswordEncoder passwordEncoder){
		this.userService = userService;
		this.domainService = domainService;
        this.notificationService = notificationService;
		this.modelMapper = modelMapper;
		this.passwordEncoder = passwordEncoder;
	}

	@GetMapping("/users")
	@PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasRole('ROLE_DOMAIN_ADMIN')")
	public List<User> getUsers(Pageable pageable) {
		return userService.findAll(pageable).getContent().stream().map(user -> modelMapper.map(user, User.class)).collect(Collectors.toList());
	}
	
	@GetMapping(value="/users/roles")	
	public List<Role> getRoles() {
		return Arrays.asList(Role.values());
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

		if(userRequest == null)
			throw new MissingElementException("User request is null");

        String message = getMessageWhenUserUpdated(userDetails, userRequest);
        final net.geant.nmaas.portal.persistent.entity.User adminUser =
                userService.findByUsername(principal.getName()).get();
        final String adminRoles = getRoleAsString(adminUser.getRoles());
        final String userRoles = getRoleAsString(userDetails.getRoles());

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
							domainService.findDomain(ur.getDomainId()).get(),
							ur.getRole()))
					.collect(Collectors.toSet());

			userDetails.setNewRoles(roles);
		}
		try {
            userService.update(userDetails);
            if(!StringUtils.isEmpty(message)) {
	            log.info(String.format("Admin user name - %s with role - %s, has updated the user - %s with role - %s. The following changes are - ",
	                    principal.getName(),
	                    adminRoles,
	                    userDetails.getUsername(),
	                    userRoles));
	            log.info(message);
            }
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
		if(userRole == null)
			throw new MissingElementException("userRole is null");

		if(userRole.getRole() == null)
			throw new MissingElementException("Missing role");

		Domain domain = null;
		if (userRole.getDomainId() == null) 
			domain = domainService.getGlobalDomain().orElseThrow(() -> new MissingElementException("Global domain not found"));
		else
			domain = domainService.findDomain(userRole.getDomainId()).orElseThrow(() -> new MissingElementException("Domain not found"));

		net.geant.nmaas.portal.persistent.entity.User user = getUser(userId);

		try {
			domainService.removeMemberRole(domain.getId(), user.getId(), userRole.getRole());

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

	@PostMapping(value="/users/my/complete")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@PreAuthorize("hasRole('ROLE_INCOMPLETE')")
	@Transactional
	public void completeRegistration(Principal principal, @RequestBody UserRequest userRequest) throws MissingElementException, ProcessingException {
		net.geant.nmaas.portal.persistent.entity.User user = userService.findByUsername(principal.getName()).orElseThrow(() -> new MissingElementException("Internal error. User not found."));
		try {
			Long domainId = domainService.getGlobalDomain().orElseThrow(() -> new ProcessingException()).getId();
			completeRegistration(userRequest, user, domainId);
		} catch (net.geant.nmaas.portal.exceptions.ProcessingException e) { //TODO: Refactor exceptions not to have same names
			throw new ProcessingException("Unable to complete your registration");
		}
	}

	private void completeRegistration(UserRequest userRequest, net.geant.nmaas.portal.persistent.entity.User user, Long domainId) throws net.geant.nmaas.portal.exceptions.ProcessingException {
		if(userService.existsByUsername(userRequest.getUsername())) {
			throw new net.geant.nmaas.portal.exceptions.ProcessingException("User with same username already exists");
		} else {
			user.setUsername(userRequest.getUsername());
		}
		if(userRequest.getFirstname() != null)
			user.setFirstname(userRequest.getFirstname());
		if(userRequest.getLastname() != null)
			user.setLastname(userRequest.getLastname());
		if(userRequest.getEmail() != null) {
			user.setEmail(userRequest.getEmail());
			domainService.removeMemberRole(domainId, user.getId(), Role.ROLE_INCOMPLETE);
		}

		userService.update(user);
	}

	@PostMapping("/users/my/auth/basic/password")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@Transactional
	public void changePassword(Principal principal, @RequestBody PasswordChange passwordChange) throws ProcessingException {
		net.geant.nmaas.portal.persistent.entity.User user = userService.findByUsername(principal.getName()).orElseThrow(() -> new ProcessingException("Internal error. User not found."));
		try {
			checkPassword(user, passwordChange.getPassword());
			changePassword(user, passwordChange.getNewPassword());
		} catch (net.geant.nmaas.portal.exceptions.ProcessingException e) {
			throw new ProcessingException("Unable to change password");
		}
	}

	private void checkPassword(net.geant.nmaas.portal.persistent.entity.User user, String password) throws net.geant.nmaas.portal.exceptions.ProcessingException{
		if(!passwordEncoder.matches(password, user.getPassword()))
			throw new net.geant.nmaas.portal.exceptions.ProcessingException("Password mismatch");
	}
	
	private void changePassword(net.geant.nmaas.portal.persistent.entity.User user, String password) throws net.geant.nmaas.portal.exceptions.ProcessingException {
		user.setPassword(passwordEncoder.encode(password));
		userService.update(user);
	}

	@GetMapping("/domains/{domainId}/users")
	@PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
	public List<User> getDomainUsers(@PathVariable Long domainId) {
		return domainService.getMembers(domainId).stream().map(domain -> modelMapper.map(domain, User.class)).collect(Collectors.toList());
	}
	
	@GetMapping("/domains/{domainId}/users/{userId}")
	@PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
	public User getDomainUser(@PathVariable Long domainId, @PathVariable Long userId) throws MissingElementException, ProcessingException {
		try {
			return modelMapper.map(domainService.getMember(domainId, userId), User.class);
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
			domainService.removeMember(domain.getId(), user.getId());
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
			return domainService.getMemberRoles(domain.getId(), user.getId());
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
		final Domain globalDomain = domainService.getGlobalDomain().orElseThrow(() -> new MissingElementException("Global domain not found"));
		
		if(domain.equals(globalDomain)) {
			if(!(role == Role.ROLE_SUPERADMIN || role == Role.ROLE_TOOL_MANAGER || role == Role.ROLE_OPERATOR || role == Role.ROLE_GUEST))
				throw new ProcessingException("Role cannot be assigned.");			
		} else {
			if(!(role == Role.ROLE_GUEST || role == Role.ROLE_USER || role == Role.ROLE_DOMAIN_ADMIN))
				throw new ProcessingException("Role cannot be assigned.");
		}
			
		final net.geant.nmaas.portal.persistent.entity.User user = getUser(userId);;

		try {
			domainService.addMemberRole(domain.getId(), user.getId(), role);

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

		final Domain domain = getDomain(domainId);
		final net.geant.nmaas.portal.persistent.entity.User user = getUser(userId);
				
		try {
			domainService.removeMemberRole(domain.getId(), user.getId(), role);

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
	@ResponseStatus(HttpStatus.ACCEPTED)
    public void setEnabledFlag(@PathVariable Long userId,
                                                 @RequestParam("enabled") final boolean isEnabledFlag,
                                                 @RequestHeader("Authorization") final String token,
                                                 final Principal principal) throws MissingElementException{
        userService.setEnabledFlag(userId, isEnabledFlag);
        net.geant.nmaas.portal.persistent.entity.User user = userService.findById(userId).orElseThrow(() -> new MissingElementException("User not found."));
        net.geant.nmaas.portal.persistent.entity.User adminUser =
                userService.findByUsername(principal.getName()).get();
        List<Role> rolesList = adminUser.getRoles().stream().map(x-> x.getRole()).collect(Collectors.toList());
        List<String> rolesAsStringList = rolesList.stream().map(x-> x.authority()).collect(Collectors.toList());
        String roleAsString = rolesAsStringList.stream().collect(Collectors.joining(","));
        String message = String.format("User %s account has been %s by user %s with role %s.",
                getUser(userId).getUsername(),
                isEnabledFlag ? "activated" : "deactivated",
                principal.getName(),
                roleAsString);
        EmailConfirmation emailConfirmation = EmailConfirmation
                .builder()
                .firstName(user.getFirstname())
                .lastName(user.getLastname())
                .toEmail(user.getEmail())
                .adminFirstName(principal.getName())
                .userName(user.getUsername())
                .build();
        if(isEnabledFlag) {
            emailConfirmation.setSubject("NMaaS: Account created");
            emailConfirmation.setTemplateName("user-activate-notification");
        } else{
            emailConfirmation.setSubject("NMaaS: Account blocked");
            emailConfirmation.setTemplateName("user-deactivate-notification");
        }

        notificationService.sendEmail(emailConfirmation, token);
        log.info(message);
    }

    @GetMapping("/users/isAdmin")
    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void isAdmin(final Principal principal){
        log.info("User with name " + principal.getName() + " is an admin user, has validated the token");
    }

	private void addGlobalGuestUserRoleIfMissing(Long userId) throws MissingElementException{
		if(domainService.getGlobalDomain().isPresent()){
			Long globalId = domainService.getGlobalDomain().get().getId();
			try{
				if(domainService.getMemberRoles(globalId, userId).isEmpty()){
					domainService.addMemberRole(globalId, userId, Role.ROLE_GUEST);
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
		return domainService.findDomain(domainId).orElseThrow(() -> new MissingElementException("Domain not found"));
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

    protected String getRoleWithDomainIdAsString(Set<UserRole> userRoles){
        return userRoles.stream().map(x-> x.getRole().authority() + "@domain" + x.getDomainId())
                .collect(Collectors.toList())
                .stream().collect(Collectors.joining(", "));
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
            message = message + System.lineSeparator() + "||| Role changed from - " + getRoleAsString(user.getRoles()) + " to - " + getRoleWithDomainIdAsString(userRequest.getRoles()) + "|||";
        }
        return message;
    }
}

