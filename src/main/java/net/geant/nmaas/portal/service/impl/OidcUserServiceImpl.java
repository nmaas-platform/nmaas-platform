package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.exceptions.ExternalUserMatchException;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.api.exceptions.SignupException;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.OidcUserService;
import net.geant.nmaas.portal.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class OidcUserServiceImpl implements OidcUserService {

    private final UserService userService;
    private final DomainService domains;
    private final UserRepository userRepository;
    private final ConfigurationManager configurationManager;

    @Value("${oidc.allowedLinkingUsersByEmail:false}")
    private boolean allowedLinkingUsersByEmail;

    @Override
    public User checkUser(OidcUser oidcUser) {

        String oidcUserSub = oidcUser.getAttribute("sub");
        String oidcUserEmail = oidcUser.getAttribute("email");
        String oidcUserPreferredUsername = oidcUser.getAttribute("preferred_username");

        boolean existUserBySamlToken = userService
                .existsBySamlToken(oidcUserSub);

        if (existUserBySamlToken) { //exist by saml_token and everything is correct
            return userService
                    .findBySamlToken(oidcUserSub)
                    .orElseThrow();
        }

        if (userService.existsByEmail(oidcUserEmail)) {//exist by email needs work with this account
            User user = userService.findByEmail(oidcUserEmail);
            //check if user with given email have older SamlToken as Email or Username
            if (user.getSamlToken().equals(oidcUserEmail)
                    || user.getSamlToken().equals(oidcUserPreferredUsername)) {
                user.setSamlToken(oidcUserSub);
                userService.update(user);
                return user;
            }else{
                throw new ExternalUserMatchException("External user "
                        + oidcUserSub
                        + " does not match internal user with SamlToken " +
                        user.getSamlToken());
            }
        }
        return registerNewUser(oidcUser);

    }

    @Override
    public User registerNewUser(OidcUser oidcUser) {
        try {
            return register(oidcUser, domains.getGlobalDomain().orElseThrow(MissingElementException::new));
        } catch (ObjectAlreadyExistsException e) {
            throw new SignupException("User already exists");
        } catch (MissingElementException e) {
            throw new SignupException("Domain not found");
        }
    }

    @Override
    public User register(OidcUser oidcUser, Domain globalDomain) {

        byte[] array = new byte[16];
        new SecureRandom().nextBytes(array);
        String generatedString = Base64.getEncoder().encodeToString(array);

        User newUser = new User(
                oidcUser.getAttribute("preferred_username"),
                true,
                generatedString,
                globalDomain,
                Role.ROLE_GUEST);
        newUser.setEmail(oidcUser.getAttribute("email"));
        newUser.setLastname(oidcUser.getAttribute("family_name"));
        newUser.setFirstname(oidcUser.getAttribute("given_name"));
        newUser.setSamlToken(oidcUser.getAttribute("sub"));
        newUser.setSelectedLanguage(configurationManager.getConfiguration().getDefaultLanguage());

        userRepository.save(newUser);
        return newUser;
    }

    @Override
    public boolean externalUserRequiresLinking(OidcUser oidcUser) {

        String oidcUserEmail = oidcUser.getAttribute("email");

        if (userService.existsByEmail(oidcUserEmail)) {
            final User user = userService.findByEmail(oidcUserEmail);
            return user.getSamlToken() == null || user.getSamlToken().isEmpty();
        }

        return false;
    }

    @Override
    public User linkUser(String email, String samlToken, String firstName, String lastName) {

        User user = userService.findByEmail(email);
        user.setSamlToken(samlToken);
        user.setFirstname(firstName);
        user.setLastname(lastName);

        userService.update(user);
        return user;
    }

}
