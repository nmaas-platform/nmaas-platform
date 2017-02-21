package net.geant.nmaas.portal.auth.basic;

import java.security.Principal;
import java.util.Date;
import java.util.Optional;

import javax.servlet.ServletException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import net.geant.nmaas.portal.api.auth.UserLogin;
import net.geant.nmaas.portal.api.auth.UserRefreshToken;
import net.geant.nmaas.portal.api.auth.UserSignup;
import net.geant.nmaas.portal.api.auth.UserToken;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.service.UserService;

@RestController
@RequestMapping("/portal/api/auth/basic")
public class BasicAuthController {

	@Autowired
	UserRepository users;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	JWTTokenService jwtTokenService;
	
	
	final long validFor = 60 * 60 * 1000; // 1h
	
	@RequestMapping(value="/login", method=RequestMethod.POST)
	public UserToken login(@RequestBody final UserLogin userLogin) throws ServletException {
		if(userLogin == null)
			throw new ServletException("No credentials.");
		
		if(StringUtils.isEmpty(userLogin.getUsername()) || StringUtils.isEmpty(userLogin.getPassword()))
			throw new ServletException("Missing credentials.");
		
		Optional<User> user = users.findByUsername(userLogin.getUsername());
		if(! user.isPresent())
			throw new ServletException("User not found.");
		
		if(!passwordEncoder.matches(userLogin.getPassword(), user.get().getPassword()))
			throw new ServletException("Invalid password.");
						
		return new UserToken(jwtTokenService.getToken(user.get()), jwtTokenService.getRefreshToken(user.get()));
	}
	
	@RequestMapping(value="/token", method=RequestMethod.POST)
	public UserToken token(@RequestBody final UserRefreshToken userRefreshToken) throws ServletException {
		if(userRefreshToken == null || StringUtils.isEmpty(userRefreshToken.getRefreshToken()))
			throw new ServletException("Missing token.");
		
		if(jwtTokenService.validateRefreshToken(userRefreshToken.getRefreshToken())) {
			Claims claims = jwtTokenService.getClaims(userRefreshToken.getRefreshToken());
			Optional<User> user = users.findByUsername(claims.getSubject());
			if(user.isPresent()) {
				return new UserToken(jwtTokenService.getToken(user.get()), jwtTokenService.getRefreshToken(user.get()));
			}
		}
				
		throw new ServletException("Unable to generate new tokens");
	}
	
	
	@RequestMapping(value="/signup", method=RequestMethod.POST)
	@Transactional
	public void signup(@RequestBody final UserSignup userSignup) throws ServletException {
		if(userSignup == null || StringUtils.isEmpty(userSignup.getUsername()) || StringUtils.isEmpty(userSignup.getPassword()) )
			throw new ServletException("Invalid credentials.");
		
		Optional<User> user = users.findByUsername(userSignup.getUsername());
		if(user.isPresent())
			throw new ServletException("User already exists.");
		
		User newUser = new User(userSignup.getUsername(), passwordEncoder.encode(userSignup.getPassword()), Role.USER);
		users.save(newUser);
	}
	
	@RequestMapping(value="/ping", method=RequestMethod.GET)
	public String ping(Principal principal) {
		return (principal != null ? principal.getName() : null ) + "," + System.currentTimeMillis();
	}
	
}
