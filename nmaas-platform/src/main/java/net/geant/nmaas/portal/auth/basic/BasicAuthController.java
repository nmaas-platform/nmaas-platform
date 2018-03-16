package net.geant.nmaas.portal.auth.basic;

import java.security.Principal;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Claims;
import net.geant.nmaas.portal.api.auth.UserLogin;
import net.geant.nmaas.portal.api.auth.UserRefreshToken;
import net.geant.nmaas.portal.api.auth.UserSignup;
import net.geant.nmaas.portal.api.auth.UserToken;
import net.geant.nmaas.portal.api.domain.Pong;
import net.geant.nmaas.portal.api.exception.AuthenticationException;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.SignupException;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;

@RestController
@RequestMapping("/portal/api/auth/basic")
public class BasicAuthController {

//	@Autowired
//	UserRepository users;
	
	@Autowired
	UserService users;
	
	@Autowired
	DomainService domains;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	JWTTokenService jwtTokenService;
	
	
	final long validFor = 60 * 60 * 1000; // 1h
	
	@RequestMapping(value="/login", method=RequestMethod.POST)
	public UserToken login(@RequestBody final UserLogin userLogin) throws AuthenticationException {
		if(userLogin == null)
			throw new AuthenticationException("No credentials.");
		
		if(StringUtils.isEmpty(userLogin.getUsername()) || StringUtils.isEmpty(userLogin.getPassword()))
			throw new AuthenticationException("Missing credentials.");
		
		User user = users.findByUsername(userLogin.getUsername()).orElseThrow(() -> new AuthenticationException("User not found."));
		
		if(!passwordEncoder.matches(userLogin.getPassword(), user.getPassword()))
			throw new AuthenticationException("Invalid password.");
						
		return new UserToken(jwtTokenService.getToken(user), jwtTokenService.getRefreshToken(user));
	}
	
	@RequestMapping(value="/token", method=RequestMethod.POST)
	public UserToken token(@RequestBody final UserRefreshToken userRefreshToken) throws AuthenticationException {
		if(userRefreshToken == null || StringUtils.isEmpty(userRefreshToken.getRefreshToken()))
			throw new AuthenticationException("Missing token.");
		
		if(jwtTokenService.validateRefreshToken(userRefreshToken.getRefreshToken())) {
			Claims claims = jwtTokenService.getClaims(userRefreshToken.getRefreshToken());
			User user = users.findByUsername(claims.getSubject()).orElseThrow(() -> new AuthenticationException("User in token not found."));
			if(user != null) {
				return new UserToken(jwtTokenService.getToken(user), jwtTokenService.getRefreshToken(user));
			}
		}
				
		throw new AuthenticationException("Unable to generate new tokens");
	}
	
	
	@RequestMapping(value="/signup", method=RequestMethod.POST)
	@Transactional
	public void signup(@RequestBody final UserSignup userSignup) throws SignupException {
		if(userSignup == null || StringUtils.isEmpty(userSignup.getUsername()) || StringUtils.isEmpty(userSignup.getPassword()) )
			throw new SignupException("Invalid credentials.");
							
		User newUser = null;
		try {
			newUser = users.register(userSignup.getUsername());
			if(newUser == null)
				throw new SignupException("Unable to register new user");
		} catch (ObjectAlreadyExistsException e) {
			throw new SignupException("User already exists.");
		} catch (MissingElementException e) {
			throw new SignupException("Domain not found.");
		}
		
		newUser.setPassword(passwordEncoder.encode(userSignup.getPassword()));		
		
		try {
			users.update(newUser);
			if(userSignup.getDomainId() != null)
				domains.addMemberRole(userSignup.getDomainId(), newUser.getId(), Role.ROLE_GUEST);
		} catch (ObjectNotFoundException e) {
			throw new SignupException("Domain not found."); 
		} catch (ProcessingException e) {
			throw new SignupException("Unable to update newly registered user.");
		} 
	}
	
	@RequestMapping(value="/ping", method=RequestMethod.GET)
	public Pong ping(Principal principal) {
		return new Pong(new Date(System.currentTimeMillis()), (principal != null ? principal.getName() : null));
	}
	
}
