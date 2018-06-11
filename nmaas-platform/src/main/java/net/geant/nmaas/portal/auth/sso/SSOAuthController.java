package net.geant.nmaas.portal.auth.sso;

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
import net.geant.nmaas.portal.api.auth.UserSSOLogin;
import net.geant.nmaas.portal.api.auth.UserToken;
import net.geant.nmaas.portal.api.domain.Pong;
import net.geant.nmaas.portal.api.exception.AuthenticationException;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.SignupException;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.api.security.SSOSettings;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;

@RestController
@RequestMapping("/portal/api/auth/sso")
public class SSOAuthController {

//	@Autowired
//	UserRepository users;

    @Autowired
    UserService users;

    @Autowired
    DomainService domains;

    @Autowired
    SSOSettings ssoSettings;

    @Autowired
    JWTTokenService jwtTokenService;

    @RequestMapping(value="/login", method=RequestMethod.POST)
    public UserToken login(@RequestBody final UserSSOLogin userID) throws AuthenticationException,SignupException {
        if(userID == null)
            throw new AuthenticationException("No userID");

        userID.validate(ssoSettings.getKey(), ssoSettings.getTimeout());

        if(StringUtils.isEmpty(userID.getUsername()))
            throw new AuthenticationException("Missing username");

        Optional<User> maybeUser = users.findByUsername(userID.getUsername());
        User user = maybeUser.isPresent() ? maybeUser.get() : null;

        if(user == null) {
            // Autocreate as we trust sso
            try {
                user = users.register(userID.getUsername());
                if(user == null)
                    throw new SignupException("Unable to register new user");

            } catch (ObjectAlreadyExistsException e) {
                throw new SignupException("User already exists");
            } catch (MissingElementException e) {
                throw new SignupException("Domain not found");
            }
        }

        return new UserToken(jwtTokenService.getToken(user), jwtTokenService.getRefreshToken(user));
    }
}