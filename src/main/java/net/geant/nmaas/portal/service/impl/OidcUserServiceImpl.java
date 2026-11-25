package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.exceptions.ExternalUserCanNotBeLinked;
import net.geant.nmaas.portal.api.exceptions.ExternalUserMatchException;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.api.exceptions.SignupException;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class OidcUserServiceImpl implements OidcUserService {

    /**
     * Matches the size of the username column in the database.
     */
    private static final int USERNAME_MAX_LENGTH = 255;

    private final UserService userService;
    private final DomainService domains;
    private final UserRepository userRepository;
    private final ConfigurationManager configurationManager;
    private final PasswordEncoder passwordEncoder;

    /**
     * Resolves the local account matching the external identity: either already
     * linked by subject, or linked by email under a legacy token equal to the
     * email or preferred username (migrated to the subject on the fly).
     *
     * <p>Never creates accounts: registration is only possible through the
     * approvals endpoint with explicit AUP and Privacy Notice consent.</p>
     *
     * @throws ExternalUserMatchException when no local account matches, the
     *         identity is incomplete, or the account is linked to a different
     *         external identity
     */
    @Override
    public User checkUser(OidcUser oidcUser) {

        String oidcUserSub = oidcUser.getAttribute("sub");
        String oidcUserEmail = oidcUser.getAttribute("email");
        String oidcUserPreferredUsername = oidcUser.getAttribute("preferred_username");

        if (StringUtils.isBlank(oidcUserSub) || StringUtils.isBlank(oidcUserEmail)) {
            throw new ExternalUserMatchException("External user identity is incomplete: subject or email claim is missing");
        }

        boolean externalIdentityLinked = userService
                .existsByExternalId(oidcUserSub);

        if (externalIdentityLinked) { //already linked by external identifier
            return userService
                    .findByExternalId(oidcUserSub)
                    .orElseThrow();
        }

        if (userService.existsByEmail(oidcUserEmail)) { //exist by email needs work with this account
            User user = userService.findByEmail(oidcUserEmail);
            String linkedExternalId = user.getExternalId();
            if (StringUtils.isBlank(linkedExternalId)) {
                throw new ExternalUserMatchException("User with email " + oidcUserEmail + " has no external identity linked yet");
            }
            if (linkedExternalId.equals(oidcUserEmail)
                    || linkedExternalId.equals(oidcUserPreferredUsername)) {
                user.setExternalId(oidcUserSub);
                try {
                    userService.update(user);
                } catch (DataIntegrityViolationException _) {
                    // another request linked the same external identity in the meantime
                    throw new ExternalUserMatchException("External user "
                            + oidcUserSub
                            + " is already linked to another user");
                }
                return user;
            }
            throw new ExternalUserMatchException("External user "
                    + oidcUserSub
                    + " does not match internal user with external identity "
                    + linkedExternalId);
        }
        // Registration is only possible through the approvals endpoint, which
        // requires explicit AUP and Privacy Notice consent. Reaching this point
        // means no local account matches the external identity (e.g. it was
        // removed concurrently with the login request).
        throw new ExternalUserMatchException("No local account matches external user " + oidcUserSub);
    }

    @Override
    public User registerNewUser(OidcUserClaims claims, String requestedUsername, boolean isAupAccepted, boolean isPnAccepted) {
        if (userService.existsByEmail(claims.email())) {
            return acceptApprovalsForExistingUser(claims, isAupAccepted, isPnAccepted);
        }
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
        } catch (MissingElementException _) {
            throw new SignupException("Domain not found");
        } catch (DataIntegrityViolationException _) {
            // a concurrent request registered the same user between the pre-checks and the insert
            throw new SignupException("User already exists");
        }
    }
    private User acceptApprovalsForExistingUser(OidcUserClaims claims, boolean isAupAccepted, boolean isPnAccepted) {
        User user = userService.findByEmail(claims.email());
        if (!identityMatches(user, claims.sub(), claims.email(), claims.preferredUsername())) {
            log.warn("Rejected approvals attempt with external identity [{}] for account [{}] linked with a different identity",
                    claims.sub(), user.getUsername());
            throw new SignupException("User already exists");
        }
        user.setTermsOfUseAccepted(isAupAccepted);
        user.setPrivacyPolicyAccepted(isPnAccepted);
        user.setExternalId(claims.sub());
        try {
            userService.update(user);
        } catch (DataIntegrityViolationException _) {
            throw new ExternalUserCanNotBeLinked("External identity is already linked to another user");
        }
        return user;
    }

    private String resolveUsername(OidcUserClaims claims, String requestedUsername) {
        return resolveUsername(requestedUsername, claims.preferredUsername());
    }

    /**
     * Returns the first non-blank candidate. Fails with a signup error when no
     * candidate is available, and with a validation error when the value exceeds
     * the database column size so that oversized usernames do not surface as
     * persistence failures.
     */
    private String resolveUsername(String... candidates) {
        String username = Arrays.stream(candidates)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElseThrow(() -> new SignupException("Username is required"));
        if (username.length() > USERNAME_MAX_LENGTH) {
            throw new IllegalArgumentException("Username exceeds the maximum length of " + USERNAME_MAX_LENGTH + " characters");
        }
        return username;
    }

    /**
     * Ensures the external identity is not linked to any other account yet.
     * The database-level unique constraint remains the last line of defense
     * against concurrent requests.
     */
    private void assertExternalIdentityAvailable(String externalId) {
        if (externalId != null && userRepository.existsByExternalId(externalId)) {
            log.warn("Rejected registration attempt for external identity [{}] already linked to another user", externalId);
            throw new SignupException("External identity is already linked to another user");
        }
    }

    private User register(String username,
                          String email,
                          String lastName,
                          String firstName,
                          String externalId,
                          boolean isAupAccepted,
                          boolean isPnAccepted,
                          Domain globalDomain) {
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            throw new SignupException("User already exists");
        }
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
        newUser.setExternalId(externalId);
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
            return user.getExternalId() == null || user.getExternalId().isEmpty();
        }

        return false;
    }

    /**
     * Returns true only for external users without a local account, who must be
     * directed to the first-login registration page where AUP and Privacy Notice
     * consent is collected. Existing accounts are never routed there: their
     * pending approvals are enforced at token issuance time with the
     * {@code ROLE_NOT_ACCEPTED} role, exactly like in the basic login flow.
     */
    @Override
    public boolean externalUserRequiresAupAndPn(OidcUser oidcUser) {

        String oidcUserSub = oidcUser.getAttribute("sub");
        String oidcUserEmail = oidcUser.getAttribute("email");
        String oidcUserPreferredUsername = oidcUser.getAttribute("preferred_username");

        if (!userService.existsByEmail(oidcUserEmail)) {
            return true;
        }

        User user = userService.findByEmail(oidcUserEmail);
        return identityMatches(user, oidcUserSub, oidcUserEmail, oidcUserPreferredUsername)
                && (!user.isTermsOfUseAccepted() || !user.isPrivacyPolicyAccepted());
    }
    private boolean identityMatches(User user, String sub, String email, String preferredUsername) {
        String linkedExternalId = user.getExternalId();
        return StringUtils.isNotBlank(linkedExternalId)
                && (linkedExternalId.equals(sub)
                || linkedExternalId.equals(email)
                || linkedExternalId.equals(preferredUsername));
    }

    @Override
    public User linkUser(OidcUserClaims claims) {
        User user = userService.findByEmail(claims.email());
        if (StringUtils.isNotBlank(user.getExternalId()) && !user.getExternalId().equals(claims.sub())) {
            log.warn("Rejected attempt to link external identity [{}] to user [{}] already linked to another external identity",
                    claims.sub(), user.getUsername());
            throw new ExternalUserCanNotBeLinked("User " + user.getUsername() + " is already linked to a different external identity");
        }
        userRepository.findByExternalId(claims.sub()).ifPresent(existing -> {
            if (!existing.getId().equals(user.getId())) {
                log.warn("Rejected attempt to link external identity [{}] to user [{}] while it is already linked to user [{}]",
                        claims.sub(), user.getUsername(), existing.getUsername());
                throw new ExternalUserCanNotBeLinked("This external identity is already linked to another account");
            }
        });
        user.setExternalId(claims.sub());
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
