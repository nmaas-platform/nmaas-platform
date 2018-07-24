package net.geant.nmaas.portal.auth.basic;

import io.jsonwebtoken.Claims;
import net.geant.nmaas.portal.api.auth.UserLogin;
import net.geant.nmaas.portal.api.auth.UserRefreshToken;
import net.geant.nmaas.portal.api.auth.UserToken;
import net.geant.nmaas.portal.api.domain.Pong;
import net.geant.nmaas.portal.api.exception.AuthenticationException;
import net.geant.nmaas.portal.api.maintenance.MaintenanceManager;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Date;

@RestController
@RequestMapping("/api/auth/basic")
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

	@Autowired
	MaintenanceManager maintenanceManager;
	
	
	final long validFor = 60 * 60 * 1000; // 1h
	
	@RequestMapping(value="/login", method=RequestMethod.POST)
	public UserToken login(@RequestBody final UserLogin userLogin) throws AuthenticationException {
		if(userLogin == null)
			throw new AuthenticationException("No credentials.");
		
		if(StringUtils.isEmpty(userLogin.getUsername()) || StringUtils.isEmpty(userLogin.getPassword()))
			throw new AuthenticationException("Missing credentials.");
		
		User user = users.findByUsername(userLogin.getUsername()).orElseThrow(() -> new AuthenticationException("User not found."));
		
		if(!user.isEnabled())
			throw new AuthenticationException("User is not active.");
		
		if(!passwordEncoder.matches(userLogin.getPassword(), user.getPassword()))
			throw new AuthenticationException("Invalid password.");

		if(maintenanceManager.isMaintenance() && user.getRoles().stream().noneMatch(value -> value.getRole().authority().equals("ROLE_SUPERADMIN")))
			throw new AuthenticationException("Login is not possible due to active maintenance period");

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
	
	@RequestMapping(value="/ping", method=RequestMethod.GET)
	public Pong ping(Principal principal) {
		return new Pong(new Date(System.currentTimeMillis()), (principal != null ? principal.getName() : null));
	}
	
}
