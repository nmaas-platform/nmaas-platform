package net.geant.nmaas.portal.api.auth;

import net.geant.nmaas.portal.exceptions.UndergoingMaintenanceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.geant.nmaas.externalservices.inventory.shibboleth.ShibbolethConfigManager;
import net.geant.nmaas.portal.api.configuration.ConfigurationView;
import net.geant.nmaas.portal.api.exception.AuthenticationException;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.SignupException;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;

@RestController
@RequestMapping("/api/auth/sso")
public class SSOAuthController {

	private UserService users;

	private DomainService domains;

	private JWTTokenService jwtTokenService;

	private ConfigurationManager configurationManager;

	private ShibbolethConfigManager shibbolethConfigManager;

	@Autowired
	public SSOAuthController(UserService users, DomainService domains, JWTTokenService jwtTokenService, ConfigurationManager configurationManager, ShibbolethConfigManager shibbolethConfigManager){
		this.users = users;
		this.domains = domains;
		this.jwtTokenService = jwtTokenService;
		this.configurationManager = configurationManager;
		this.shibbolethConfigManager = shibbolethConfigManager;
	}

	@PostMapping(value="/login")
	public UserToken login(@RequestBody final UserSSOLogin userSSOLoginData) {
		ConfigurationView configuration = this.configurationManager.getConfiguration();
		if(!configuration.isSsoLoginAllowed())
			throw new SignupException("SSO login method is not enabled");

		if(userSSOLoginData == null)
			throw new AuthenticationException("Received user SSO login data is incorrect");

		if(StringUtils.isEmpty(userSSOLoginData.getUsername()))
			throw new AuthenticationException("Missing username");

		shibbolethConfigManager.checkParam();
		userSSOLoginData.validate(shibbolethConfigManager.getKey(), shibbolethConfigManager.getTimeout());

		User user = users.findBySamlToken(userSSOLoginData.getUsername()).orElse(registerNewUser(userSSOLoginData));

		if(!user.isEnabled())
			throw new AuthenticationException("User is not active.");

		if(configuration.isMaintenance() && user.getRoles().stream().noneMatch(value -> value.getRole().authority().equals("ROLE_SYSTEM_ADMIN")))
			throw new UndergoingMaintenanceException("Application is undergoing maintenance right now");

		return new UserToken(jwtTokenService.getToken(user), jwtTokenService.getRefreshToken(user));
	}

	private User registerNewUser(UserSSOLogin userSSOLoginData){
		try{
			return users.register(userSSOLoginData, domains.getGlobalDomain().orElseThrow(MissingElementException::new));
		} catch (ObjectAlreadyExistsException e) {
			throw new SignupException("User already exists");
		} catch (MissingElementException e) {
			throw new SignupException("Domain not found");
		} catch (net.geant.nmaas.portal.exceptions.ProcessingException e) {
			throw new SignupException("Internal server error");
		}
	}
}