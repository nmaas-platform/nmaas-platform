package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.exceptions.ExternalUserCanNotBeLinked;
import net.geant.nmaas.portal.api.exceptions.ExternalUserMatchException;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.api.exceptions.SignupException;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.repositories.UserRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.OidcUserClaims;
import net.geant.nmaas.portal.service.OidcUserService;
import net.geant.nmaas.portal.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OidcUserServiceImpl implements OidcUserService {

    private final UserService userService;
    private final DomainService domains;
    private final UserRepository userRepository;
    private final ConfigurationManager configurationManager;
    private final PasswordEncoder passwordEncoder;

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

        if (userService.existsByEmail(oidcUserEmail)) { //exist by email needs work with this account
            User user = userService.findByEmail(oidcUserEmail);
            //check if user with given email have older SamlToken as Email or Username
            if (user.getSamlToken().equals(oidcUserEmail)
                    || user.getSamlToken().equals(oidcUserPreferredUsername)) {
                user.setSamlToken(oidcUserSub);
                userService.update(user);
                return user;
            } else {
                throw new ExternalUserMatchException("External user "
                        + oidcUserSub
                        + " does not match internal user with SamlToken " +
                        user.getSamlToken());
            }
        }
        return registerNewUser(oidcUser);
    }

    @Override
    public User registerNewUser(OidcUserClaims claims, String requestedUsername, boolean isAupAccepted, boolean isPnAccepted) {
        String username = resolveUsername(claims, requestedUsername);
        assertExternalIdentityAvailable(claims.sub());
        try {
            return register(username,
                    claims.email(),
                    claims.familyName(),
                    claims.givenName(),
                    claims.sub(),
                    isAupAccepted,
                    isPnAccepted,
                    domains.getGlobalDomain().orElseThrow(MissingElementException::new));
        } catch (ObjectAlreadyExistsException _) {
            throw new SignupException("User already exists");
        } catch (MissingElementException _) {
            throw new SignupException("Domain not found");
        }
    }

    private String resolveUsername(OidcUserClaims claims, String requestedUsername) {
        if (StringUtils.isNotBlank(requestedUsername)) {
            return requestedUsername;
        }
        if (StringUtils.isNotBlank(claims.preferredUsername())) {
            return claims.preferredUsername();
        }
        throw new SignupException("Username is required");
    }

    /**
     * Ensures the external identity is not linked to any other account yet.
     * The database-level unique constraint remains the last line of defense
     * against concurrent requests.
     */
    private void assertExternalIdentityAvailable(String samlToken) {
        if (samlToken != null && userRepository.existsBySamlToken(samlToken)) {
            log.warn("Rejected registration attempt for external identity [{}] already linked to another user", samlToken);
            throw new SignupException("External identity is already linked to another user");
        }
    }

    @Override
    public User registerNewUser(OidcUser oidcUser) {
        assertExternalIdentityAvailable(oidcUser.getAttribute("sub"));
        try {
            return register(oidcUser, domains.getGlobalDomain().orElseThrow(MissingElementException::new));
        } catch (ObjectAlreadyExistsException _) {
            throw new SignupException("User already exists");
        } catch (MissingElementException _) {
            throw new SignupException("Domain not found");
        }
    }

    private User register(OidcUser oidcUser, Domain globalDomain) {
        String preferredUsername = Objects.requireNonNull(Optional.ofNullable(oidcUser.getAttribute("preferred_username"))
                .orElseGet(() -> Optional.ofNullable(oidcUser.getAttribute("username"))
                        .orElseGet(() -> oidcUser.getAttribute("name")))).toString();

        return this.register(preferredUsername,
                oidcUser.getAttribute("email"),
                oidcUser.getAttribute("family_name"),
                oidcUser.getAttribute("given_name"),
                oidcUser.getAttribute("sub"),
                false,
                false,
                globalDomain
        );
    }

    private User register(String username,
                          String email,
                          String lastName,
                          String firstName,
                          String samlToken,
                          boolean isAupAccepted,
                          boolean isPnAccepted,
                          Domain globalDomain) {
        byte[] array = new byte[16];
        new SecureRandom().nextBytes(array);
        String generatedString = Base64.getEncoder().encodeToString(array);

        User newUser = new User(
                username,
                true,
                passwordEncoder.encode(generatedString),
                globalDomain,
                Role.ROLE_GUEST);
        newUser.setEmail(email);
        newUser.setLastname(lastName);
        newUser.setFirstname(firstName);
        newUser.setSamlToken(samlToken);
        newUser.setTermsOfUseAccepted(isAupAccepted);
        newUser.setPrivacyPolicyAccepted(isPnAccepted);
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
    public boolean externalUserRequiresAupAndPn(OidcUser oidcUser) {

        String oidcUserEmail = oidcUser.getAttribute("email");

        return !userService.existsByEmail(oidcUserEmail);

//        if (userService.existsByEmail(oidcUserEmail)) {
//            final User user = userService.findByEmail(oidcUserEmail);
//            return !user.isTermsOfUseAccepted() || !user.isPrivacyPolicyAccepted();
//        }
//
//        return true;
    }

    @Override
    public User linkUser(OidcUserClaims claims) {
        User user = userService.findByEmail(claims.email());
        if (StringUtils.isNotBlank(user.getSamlToken()) && !user.getSamlToken().equals(claims.sub())) {
            log.warn("Rejected attempt to link external identity [{}] to user [{}] already linked to another external identity",
                    claims.sub(), user.getUsername());
            throw new ExternalUserCanNotBeLinked("User " + user.getUsername() + " is already linked to a different external identity");
        }
        userRepository.findBySamlToken(claims.sub()).ifPresent(existing -> {
            if (!existing.getId().equals(user.getId())) {
                log.warn("Rejected attempt to link external identity [{}] to user [{}] while it is already linked to user [{}]",
                        claims.sub(), user.getUsername(), existing.getUsername());
                throw new ExternalUserCanNotBeLinked("This external identity is already linked to another account");
            }
        });
        user.setSamlToken(claims.sub());
        if (claims.givenName() != null) {
            user.setFirstname(claims.givenName());
        }
        if (claims.familyName() != null) {
            user.setLastname(claims.familyName());
        }

        userService.update(user);
        return user;
    }

}
