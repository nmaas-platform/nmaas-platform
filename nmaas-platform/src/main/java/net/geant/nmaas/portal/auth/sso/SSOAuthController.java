package net.geant.nmaas.portal.auth.sso;

import java.io.IOException;
import net.geant.nmaas.externalservices.inventory.shibboleth.ShibbolethConfigManager;
import net.geant.nmaas.portal.api.auth.UserSSOLogin;
import net.geant.nmaas.portal.api.auth.UserToken;
import net.geant.nmaas.portal.api.exception.AuthenticationException;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.SignupException;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.persistent.entity.Configuration;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;

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

	@RequestMapping(value="/login", method=RequestMethod.POST)
	public UserToken login(@RequestBody final UserSSOLogin userSSOLoginData) throws AuthenticationException,SignupException, IOException {
		Configuration configuration = this.configurationManager.getConfiguration();
		if(!configuration.isSsoLoginAllowed())
			throw new AuthenticationException("SSO login method is not enabled");

		if(userSSOLoginData == null)
			throw new AuthenticationException("Received user SSO login data is empty");

		if(StringUtils.isEmpty(userSSOLoginData.getUsername()))
			throw new AuthenticationException("Missing username");

		shibbolethConfigManager.checkParam();
		userSSOLoginData.validate(shibbolethConfigManager.getKey(), shibbolethConfigManager.getTimeout());

		Optional<User> maybeUser = users.findBySamlToken(userSSOLoginData.getUsername());
		User user = maybeUser.orElse(null);

		if(user == null) {
			// Autocreate as we trust sso
			try {
				byte[] array = new byte[16]; // random password
				new Random().nextBytes(array);
				String generatedString = new String(array, Charset.forName("UTF-8"));
				user = users.register("thirdparty-"+String.valueOf(System.currentTimeMillis()), true, generatedString, domains.getGlobalDomain().orElseThrow(MissingElementException::new));
				user.setSamlToken(userSSOLoginData.getUsername()); //Check user ID TODO: check if it's truly unique!
				user.setNewRoles(Collections.singleton(new UserRole(user, domains.getGlobalDomain().orElseThrow(() -> new SignupException()), Role.ROLE_INCOMPLETE)));
				users.update(user);
			} catch (ObjectAlreadyExistsException e) {
				throw new SignupException("User already exists");
			} catch (MissingElementException e) {
				throw new SignupException("Domain not found");
			} catch (net.geant.nmaas.portal.exceptions.ProcessingException e) {
				throw new SignupException("Internal server error");
			}
		}

		if(!user.isEnabled())
			throw new AuthenticationException("User is not active.");

		if(user.getRoles().stream().noneMatch(value -> value.getRole().authority().equals("ROLE_SUPERADMIN")) && configuration.isMaintenance())
			throw new AuthenticationException("Application is undergoing maintenance right now. Please try again later.");

		return new UserToken(jwtTokenService.getToken(user), jwtTokenService.getRefreshToken(user));
	}
}