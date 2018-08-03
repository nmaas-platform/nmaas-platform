package net.geant.nmaas.portal.auth.sso;

import net.geant.nmaas.portal.api.auth.UserSSOLogin;
import net.geant.nmaas.portal.api.auth.UserToken;
import net.geant.nmaas.portal.api.exception.AuthenticationException;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.SignupException;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.api.security.SSOSettings;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth/sso")
public class SSOAuthController {

	@Autowired
	UserService users;

	@Autowired
	DomainService domains;

	@Autowired
	SSOSettings ssoSettings;

	@Autowired
	JWTTokenService jwtTokenService;

	@RequestMapping(value="/login", method=RequestMethod.POST)
	public UserToken login(@RequestBody final UserSSOLogin userSSOLoginData) throws AuthenticationException,SignupException {
		if(userSSOLoginData == null)
			throw new AuthenticationException("Received user SSO login data is empty");

		if(StringUtils.isEmpty(userSSOLoginData.getUsername()))
			throw new AuthenticationException("Missing username");

		userSSOLoginData.validate(ssoSettings.getKey(), ssoSettings.getTimeout());

		Optional<User> maybeUser = users.findByUsername(userSSOLoginData.getUsername());
		User user = maybeUser.orElse(null);

		if(user == null) {
			// Autocreate as we trust sso
			try {
				user = users.register(userSSOLoginData.getUsername());
				if(user == null)
					throw new SignupException("Unable to register new user");

			} catch (ObjectAlreadyExistsException e) {
				throw new SignupException("User already exists");
			} catch (MissingElementException e) {
				throw new SignupException("Domain not found");
			}
		}
		
		if(!user.isEnabled())
			throw new AuthenticationException("User is not active.");

		return new UserToken(jwtTokenService.getToken(user), jwtTokenService.getRefreshToken(user));
	}
}