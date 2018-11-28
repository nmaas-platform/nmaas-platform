package net.geant.nmaas.portal.auth.basic;

import java.security.Principal;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Claims;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.api.auth.UserLogin;
import net.geant.nmaas.portal.api.auth.UserRefreshToken;
import net.geant.nmaas.portal.api.auth.UserToken;
import net.geant.nmaas.portal.api.domain.Pong;
import net.geant.nmaas.portal.api.exception.AuthenticationException;
import net.geant.nmaas.portal.api.exception.SignupException;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;

@RestController
@RequestMapping("/api/auth/basic")
@Log4j2
public class BasicAuthController {

	private UserService users;
	
    private DomainService domains;
	
    private PasswordEncoder passwordEncoder;
	
    private JWTTokenService jwtTokenService;

    private ConfigurationManager configurationManager;

    @Autowired
    public BasicAuthController(UserService users, DomainService domains,
                               PasswordEncoder passwordEncoder, JWTTokenService jwtTokenService,
                               ConfigurationManager configurationManager){
        this.users = users;
        this.domains = domains;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.configurationManager = configurationManager;
    }
	
	@PostMapping(value="/login")
	public UserToken login(@RequestBody final UserLogin userLogin) {
        User user = users.findByUsername(userLogin.getUsername()).orElseThrow(() -> new AuthenticationException("Invalid Credentials."));
        validate(Optional.of(userLogin.getUsername()), Optional.of(userLogin.getPassword()), user.getPassword(), user.isEnabled(), user.isTermsOfUseAccepted(), user.isPrivacyPolicyAccepted());

        if(configurationManager.getConfiguration().isMaintenance() && user.getRoles().stream().noneMatch(value -> value.getRole().equals(Role.ROLE_SYSTEM_ADMIN)))
            throw new AuthenticationException("Application is undergoing maintenance right now. Please try again later.");

        if(user.getRoles().stream().anyMatch(role -> role.getRole().equals(Role.ROLE_SYSTEM_COMPONENT)))
            throw new AuthenticationException("System component login cannot be performed");

        log.info(String.format("User [%s] logged in with role [%s]", userLogin.getUsername(),
                user.getRoles().stream().map(role -> role.getRole().name()).collect(Collectors.toList())));
        return new UserToken(jwtTokenService.getToken(user), jwtTokenService.getRefreshToken(user));
	}
	
	@PostMapping(value="/token")
	public UserToken token(@RequestBody final UserRefreshToken userRefreshToken) {
	    UserToken userToken = null;
        if(userRefreshToken == null || StringUtils.isEmpty(userRefreshToken.getRefreshToken()))
        throw new AuthenticationException("Missing token.");

        if(jwtTokenService.validateRefreshToken(userRefreshToken.getRefreshToken())) {
            final Claims claims = jwtTokenService.getClaims(userRefreshToken.getRefreshToken());
            final User user = users.findByUsername(claims.getSubject()).orElseThrow(() -> new AuthenticationException("User in token not found."));
            if(user != null) {
                userToken = new UserToken(jwtTokenService.getToken(user), jwtTokenService.getRefreshToken(user));
            }
        } else {
            throw new AuthenticationException("Unable to generate new tokens");
        }
        return userToken;
	}
	
	@GetMapping(value="/ping")
	public Pong ping(Principal principal) {
		return new Pong(new Date(System.currentTimeMillis()), (principal != null ? principal.getName() : null));
	}

    protected void validate(final Optional<String> userName, final Optional<String> password,
                            String actualPassword, boolean isEnabled, boolean isTermsOfUseAccepted, boolean isPrivacyPolicyAccepted) {
        boolean isValid = true;
        if(!userName.isPresent() || !password.isPresent()){
            isValid = validateAndLogMessage("Missing credentials", userName.orElse("ANONYMOUS"));
        }
        else{
            if (!isEnabled) {
                isValid = validateAndLogMessage("User is not active", userName.get());
            }
            if (!isTermsOfUseAccepted || !isPrivacyPolicyAccepted){
              User user = users.findByUsername(userName.get()).orElseThrow(SignupException::new);
              log.error("Terms of Use or Privacy Policy were not accepted by %s", userName.get());
              user.setNewRoles(ImmutableSet.of(new UserRole(user, domains.getGlobalDomain().orElseThrow(SignupException::new), Role.ROLE_NOT_ACCEPTED)));
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
