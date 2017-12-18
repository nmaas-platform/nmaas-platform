package net.geant.nmaas.portal.api.market;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.apache.el.stream.Stream;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.geant.nmaas.portal.api.auth.UserSignup;
import net.geant.nmaas.portal.api.domain.Id;
import net.geant.nmaas.portal.api.domain.User;
import net.geant.nmaas.portal.api.domain.UserRequest;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.api.exception.SignupException;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;

@RestController
@RequestMapping("/portal/api")
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
	@PreAuthorize("hasRole('ROLE_SUPERADMIN')")
	public List<User> getUsers(Pageable pageable) {
		return users.findAll(pageable).getContent().stream().map(user -> modelMapper.map(user, User.class)).collect(Collectors.toList());
	}
	
	@GetMapping(value="/users/roles")	
	public List<Role> getRoles() {
		return Arrays.asList(Role.values());
	}
	
	@PostMapping(value="/users")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasPermission(#userSignup.domainId, 'domain', 'OWNER')")
	@Transactional
	public Id addUser(@RequestBody @NotNull UserSignup userSignup) throws SignupException {
		net.geant.nmaas.portal.persistent.entity.User user = null;
		try {
			user = users.register(userSignup.getUsername());
		} catch(ObjectAlreadyExistsException ex) {
			throw new SignupException("User already exists.");
		} catch (MissingElementException e) {			
			throw new SignupException("Domain not found.");
		}

		if(user == null)
			throw new SignupException("Unable to register new user");

		user.setPassword(userSignup.getPassword() != null ? passwordEncoder.encode(userSignup.getPassword()): null);
		
		try {
			users.update(user);
		} catch (net.geant.nmaas.portal.exceptions.ProcessingException ex) {
			throw new SignupException("Unable to update newly registered user.");
		}
		
		return new Id(user.getId());
	}
	
		
	@GetMapping(value="/users/{userId}")
	public User getUser(@PathVariable("userId") Long userId) {
		net.geant.nmaas.portal.persistent.entity.User user = users.findById(userId);		
		return modelMapper.map(user, User.class);
	}
	
	@PutMapping(value="/users/{userId}")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@Transactional
	public void updateUser(@PathVariable("userId") Long userId, @NotNull UserRequest userRequest) throws ProcessingException {
		net.geant.nmaas.portal.persistent.entity.User userMod = users.findById(userId);
		
		if(userRequest.getUsername() != null && !userMod.getUsername().equals(userRequest.getUsername())) {
			net.geant.nmaas.portal.persistent.entity.User userByUsername = users.findByUsername(userRequest.getUsername());
			if(userByUsername != null)
				throw new ProcessingException("Unable to change username.");
			userMod.setUsername(userRequest.getUsername());
		}
		
		if(userRequest.getPassword() != null)
			userMod.setPassword(passwordEncoder.encode(userRequest.getPassword()));
		
		if(userRequest.getRoles() != null && !userRequest.getRoles().isEmpty()) {
			Set<UserRole> roles = userRequest.getRoles().stream().map(ur -> new UserRole(userMod, domains.findDomain(ur.getDomainId()), ur.getRole())).collect(Collectors.toSet());
			userMod.setNewRoles(roles);
		}
		try {
			users.update(userMod);
		} catch (net.geant.nmaas.portal.exceptions.ProcessingException e) {
			throw new ProcessingException("Unable to modify user");
		}
	}
	
	@DeleteMapping(value="/users/{userId}")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void deleteUser(@PathVariable("userId") Long userId) throws ProcessingException {
		throw new ProcessingException("User removing not supported.");
	}
	
	@GetMapping("/domains/{domainId}/users")
	@PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
	public List<User> getDomainUsers(@PathVariable Long domainId) {
		return domains.getMembers(domainId).stream().map(domain -> modelMapper.map(domain, User.class)).collect(Collectors.toList());		
	}
	
	@GetMapping("/domains/{domainId}/users/{userId}")
	@PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
	public User getDomainUser(@PathVariable Long domainId, @PathVariable Long userId) throws MissingElementException {
		Domain domain = domains.findDomain(domainId);
		if(domain == null)
			throw new MissingElementException("Domain not found");
		
		net.geant.nmaas.portal.persistent.entity.User user = users.findById(userId);
		if(user == null)
			throw new MissingElementException("User not found");

		return modelMapper.map(domains.getMember(domainId, userId), User.class);
	}
	
	@DeleteMapping("/domains/{domainId}/users/{userId}")
	@PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void removeDomainUser(@PathVariable Long domainId, @PathVariable Long userId) throws MissingElementException {
		Domain domain = domains.findDomain(domainId);
		if(domain == null)
			throw new MissingElementException("Domain not found");
		
		net.geant.nmaas.portal.persistent.entity.User user = users.findById(userId);
		if(user == null)
			throw new MissingElementException("User not found");
		
		domains.removeMember(user.getId(), domain.getId());
	}
	
	@GetMapping("/domains/{domainId}/users/{userId}/roles")
	@PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
	public Set<Role> getUserRoles(@PathVariable Long domainId, @PathVariable Long userId) throws MissingElementException {
		Domain domain = domains.findDomain(domainId);
		if(domain == null)
			throw new MissingElementException("Domain not found");
		
		net.geant.nmaas.portal.persistent.entity.User user = users.findById(userId);
		if(user == null)
			throw new MissingElementException("User not found");

		return domains.getMemberRoles(domain.getId(), user.getId());
	}
}

