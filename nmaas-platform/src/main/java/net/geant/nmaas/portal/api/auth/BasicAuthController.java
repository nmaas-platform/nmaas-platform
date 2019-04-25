package net.geant.nmaas.portal.api.auth;

import com.google.common.collect.ImmutableSet;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.api.domain.Pong;
import net.geant.nmaas.portal.api.exception.AuthenticationException;
import net.geant.nmaas.portal.api.exception.SignupException;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.exceptions.UndergoingMaintenanceException;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Date;
import java.util.stream.Collectors;

import static java.lang.String.format;

@RestController
@AllArgsConstructor
@Log4j2
@RequestMapping("/api/auth/basic")
public class BasicAuthController {

	private UserService users;
	
    private DomainService domains;
	
    private PasswordEncoder passwordEncoder;
	
    private JWTTokenService jwtTokenService;

    private ConfigurationManager configurationManager;

	@PostMapping(value="/login")
	public UserToken login(@RequestBody final UserLogin userLogin) {
        User user = users.findByUsername(userLogin.getUsername()).orElseThrow(() -> new AuthenticationException("User not found"));
        validate(userLogin.getUsername(), userLogin.getPassword(), user.getPassword(), user.isEnabled());
        checkUserApprovals(user);

        if(configurationManager.getConfiguration().isMaintenance() && user.getRoles().stream().noneMatch(value -> value.getRole().equals(Role.ROLE_SYSTEM_ADMIN))) {
            throw new UndergoingMaintenanceException("Application is undergoing maintenance right now");
        }

        log.info(format("User [%s] logged in with role [%s]", userLogin.getUsername(),
                user.getRoles().stream().map(role -> role.getRole().name()).collect(Collectors.toList())));
        return new UserToken(jwtTokenService.getToken(user), jwtTokenService.getRefreshToken(user));
	}
	
	@PostMapping(value="/token")
	public UserToken token(@RequestBody final UserRefreshToken userRefreshToken) {
        if(userRefreshToken == null || StringUtils.isEmpty(userRefreshToken.getRefreshToken())) {
            throw new AuthenticationException("Token is missing");
        }

        if(jwtTokenService.validateRefreshToken(userRefreshToken.getRefreshToken())) {
            final Claims claims = jwtTokenService.getClaims(userRefreshToken.getRefreshToken());
            final User user = users.findByUsername(claims.getSubject()).orElseThrow(() -> new AuthenticationException("User in token not found."));
            return new UserToken(jwtTokenService.getToken(user), jwtTokenService.getRefreshToken(user));
        } else {
            throw new AuthenticationException("Unable to generate new tokens");
        }
	}
	
	@GetMapping(value="/ping")
	public Pong ping(Principal principal) {
		return new Pong(new Date(System.currentTimeMillis()), (principal != null ? principal.getName() : null));
	}

    void validate(String userName, String providedPassword, String actualPassword, boolean isEnabled) {
        validateConditionAndLogMessage(userName == null || providedPassword == null,
                format("Login failed: missing credentials%s", userName != null ? (format(" (username: %s)", userName)) : ""));
        validateConditionAndLogMessage(!isEnabled, format("Login failed: user [%s] is not active", userName));
        validateConditionAndLogMessage(!passwordEncoder.matches(providedPassword, actualPassword), format("Login failed: user [%s] entered incorrect password", userName));
    }

    void checkUserApprovals(User user) {
        if (!user.isTermsOfUseAccepted() || !user.isPrivacyPolicyAccepted()){
            log.info(format("Check during login: Terms of Use or Privacy Policy were not accepted by user [%s]", user.getUsername()));
            user.setNewRoles(ImmutableSet.of(new UserRole(user, domains.getGlobalDomain().orElseThrow(SignupException::new), Role.ROLE_NOT_ACCEPTED)));
        }
    }

    private void validateConditionAndLogMessage(boolean loginCondition, String errorMessage){
	    if (loginCondition) {
            log.info(errorMessage);
            throw new AuthenticationException("Invalid Credentials");
        }
    }
	
}
