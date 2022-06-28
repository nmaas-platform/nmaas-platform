package net.geant.nmaas.portal.api.auth;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.api.configuration.ConfigurationView;
import net.geant.nmaas.portal.api.exception.AuthenticationException;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.SignupException;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.api.security.SSOConfigManager;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.exceptions.UndergoingMaintenanceException;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserLoginRegisterService;
import net.geant.nmaas.portal.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

import static java.lang.String.format;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/auth/sso")
public class SSOAuthController {

	private final UserService users;

	private final DomainService domains;

	private final JWTTokenService jwtTokenService;

	private final ConfigurationManager configurationManager;

	private final SSOConfigManager SSOConfigManager;

	private final UserLoginRegisterService userLoginService;

	@PostMapping(value="/login")
	public UserToken login(@RequestBody final UserSSOLogin userSSOLoginData, HttpServletRequest request) {
		ConfigurationView configuration = this.configurationManager.getConfiguration();
		if(!configuration.isSsoLoginAllowed()) {
			throw new SignupException("SSO login method is not enabled");
		}

		if(userSSOLoginData == null) {
			throw new AuthenticationException("Received user SSO login data is incorrect");
		}

		if(Strings.isNullOrEmpty(userSSOLoginData.getUsername())){
			throw new AuthenticationException("Missing username");
		}

		SSOConfigManager.validateConfig();
		userSSOLoginData.validate(SSOConfigManager.getKey(), SSOConfigManager.getTimeout());

		User user = users.findBySamlToken(userSSOLoginData.getUsername()).orElseGet(() -> registerNewUser(userSSOLoginData));

		if(!user.isEnabled()) {
			userLoginService.registerNewFailedLogin(user, request.getHeader(HttpHeaders.HOST), request.getHeader(HttpHeaders.USER_AGENT), BasicAuthController.getClientIpAddr(request));
			throw new AuthenticationException("User is not active.");
		}

		if(configuration.isMaintenance() && user.getRoles().stream().noneMatch(value -> value.getRole().authority().equals("ROLE_SYSTEM_ADMIN"))) {
			userLoginService.registerNewFailedLogin(user, request.getHeader(HttpHeaders.HOST), request.getHeader(HttpHeaders.USER_AGENT), BasicAuthController.getClientIpAddr(request));
			throw new UndergoingMaintenanceException("Application is undergoing maintenance right now");
		}

		userLoginService.registerNewFailedLogin(user, request.getHeader(HttpHeaders.HOST), request.getHeader(HttpHeaders.USER_AGENT), BasicAuthController.getClientIpAddr(request));

		log.info(format("User [%s] logged in with:%n \t- roles [%s]%n \t- host: [%s]%n \t- user-agent: [%s]%n \t- ip: [%s]",
				user.getUsername(),
				user.getRoles().stream().map(UserRole::getRoleAsString).collect(Collectors.toList()),
				request.getHeader(HttpHeaders.HOST),
				request.getHeader(HttpHeaders.USER_AGENT),
				BasicAuthController.getClientIpAddr(request)));

		userLoginService.registerNewSuccessfulLogin(user, request.getHeader(HttpHeaders.HOST), request.getHeader(HttpHeaders.USER_AGENT), BasicAuthController.getClientIpAddr(request));

		return new UserToken(jwtTokenService.getToken(user), jwtTokenService.getRefreshToken(user));
	}

	@GetMapping
	public SSOView getSSO() {
		SSOConfigManager.validateConfig();
		return SSOConfigManager.getSSOView();
	}

	private User registerNewUser(UserSSOLogin userSSOLoginData) {
		try {
			return users.register(userSSOLoginData, domains.getGlobalDomain().orElseThrow(MissingElementException::new));
		} catch (ObjectAlreadyExistsException e) {
			throw new SignupException("User already exists");
		} catch (MissingElementException e) {
			throw new SignupException("Domain not found");
		}
	}
}
