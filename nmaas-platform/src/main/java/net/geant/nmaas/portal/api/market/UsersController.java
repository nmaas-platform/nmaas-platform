package net.geant.nmaas.portal.api.market;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.geant.nmaas.portal.api.auth.UserSignup;
import net.geant.nmaas.portal.api.domain.Id;
import net.geant.nmaas.portal.api.domain.User;
import net.geant.nmaas.portal.api.domain.UserRequest;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.api.exception.SignupException;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;

@RestController
@RequestMapping("/portal/api/users")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UsersController {

	@Autowired
	UserRepository userRepo;

	@Autowired
	ModelMapper modelMapper;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@GetMapping
	public List<User> getUsers(Pageable pageable) {
		return userRepo.findAll(pageable).getContent().stream().map(user -> modelMapper.map(user, User.class)).collect(Collectors.toList());
	}
	
	@GetMapping(value="/roles")
	public List<Role> getRoles() {
		return Arrays.asList(Role.values());
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public Id addUser(@NotNull UserSignup userSignup) throws SignupException {
		Optional<net.geant.nmaas.portal.persistent.entity.User> userOptional = userRepo.findByUsername(userSignup.getUsername());
		if(userOptional.isPresent())
			throw new SignupException("User already exists.");
		
		net.geant.nmaas.portal.persistent.entity.User user = new net.geant.nmaas.portal.persistent.entity.User(userSignup.getUsername(), 
																												passwordEncoder.encode(userSignup.getPassword()), 
																												Role.USER);
		userRepo.save(user);
		return new Id(user.getId());
	}
	
	
	
	@GetMapping(value="/{userId}")
	public User getUser(@PathVariable("userId") Long userId) {
		net.geant.nmaas.portal.persistent.entity.User user = userRepo.findOne(userId);
		
		return modelMapper.map(user, User.class);
	}
	
	@PutMapping(value="/{userId}")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@Transactional
	public void updateUser(@PathVariable("userId") Long userId, @NotNull UserRequest userRequest) throws ProcessingException {
		net.geant.nmaas.portal.persistent.entity.User userMod = userRepo.findOne(userId);
		
		if(userRequest.getUsername() != null && !userMod.getUsername().equals(userRequest.getUsername())) {
			Optional<net.geant.nmaas.portal.persistent.entity.User> userByUsername = userRepo.findByUsername(userRequest.getUsername());
			if(userByUsername.isPresent())
				throw new ProcessingException("Unable to change username.");
			userMod.setUsername(userRequest.getUsername());
		}
		
		if(userRequest.getPassword() != null)
			userMod.setPassword(passwordEncoder.encode(userRequest.getPassword()));
		
		if(userRequest.getRoles() != null && !userRequest.getRoles().isEmpty())
			userMod.setNewRoles(userRequest.getRoles());
		
		userRepo.save(userMod);
	}
	
	@DeleteMapping(value="/{userId}")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void deleteUser(@PathVariable("userId") Long userId) throws ProcessingException {
		throw new ProcessingException("User removing not supported.");
	}
	
}

