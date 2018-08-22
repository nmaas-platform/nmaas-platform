package net.geant.nmaas.portal.auth.basic;

import java.security.Principal;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Claims;
import net.geant.nmaas.portal.api.auth.UserLogin;
import net.geant.nmaas.portal.api.auth.UserRefreshToken;
import net.geant.nmaas.portal.api.auth.UserToken;
import net.geant.nmaas.portal.api.domain.Pong;
import net.geant.nmaas.portal.api.exception.AuthenticationException;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
@RequestMapping("/api/auth/basic")
public class BasicAuthController {
    private static final Logger log = LogManager.getLogger(BasicAuthController.class);
	
	@Autowired
	private UserService users;
	
	@Autowired
    private DomainService domains;
	
	@Autowired
    private PasswordEncoder passwordEncoder;
	
	@Autowired
    private JWTTokenService jwtTokenService;

    @Autowired
    private ConfigurationManager configurationManager;
	
	@RequestMapping(value="/login", method=RequestMethod.POST)
	public UserToken login(@RequestBody final UserLogin userLogin) throws AuthenticationException {
        User user = users.findByUsername(userLogin.getUsername()).orElseThrow(() -> new AuthenticationException("Invalid Credentials."));
        validate(Optional.of(userLogin.getUsername()), Optional.of(userLogin.getPassword()), user.getPassword(), user.isEnabled(), user.isTouAccept());

        if(user.getRoles().stream().noneMatch(value -> value.getRole().authority().equals("ROLE_SUPERADMIN")) && configurationManager.getConfiguration().isMaintenance())
            throw new AuthenticationException("Application is undergoing maintenance right now. Please try again later.");

        log.info(String.format("The user who logged in is - %s, and the role is - %s", userLogin.getUsername(),
                user.getRoles().stream().map(role -> role.getRole().name()).collect(Collectors.toList())));
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

    protected void validate(final Optional<String> userName, final Optional<String> password,
                            String actualPassword, boolean isEnabled, boolean isTermsOfUseAccepted) throws AuthenticationException{
        boolean isValid = true;
        if(!userName.isPresent() || !password.isPresent()){
            isValid = validateAndLogMessage("Missing credentials", userName.orElse("ANONYMOUS"));
        }
        else{
            if (!isEnabled) {
                isValid = validateAndLogMessage("User is not active", userName.get());
            }
            if (!isTermsOfUseAccepted){
              isValid = validateAndLogMessage("Terms of Use were not accepted", userName.get());
            }
            if (!passwordEncoder.matches(password.get(), actualPassword)) {
                isValid = validateAndLogMessage("Invalid password", userName.get());
            }
        }
        if(!isValid){
            throw new AuthenticationException("Invalid Credentials");
        }
    }

    private boolean validateAndLogMessage(String message, String userName){
        log.info(String.format("%s for user name - %s.", message, userName));
        return false;
    }
	
}
