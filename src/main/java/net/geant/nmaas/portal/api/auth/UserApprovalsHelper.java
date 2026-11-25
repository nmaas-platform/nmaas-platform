package net.geant.nmaas.portal.api.auth;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.exceptions.SignupException;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserRole;
import net.geant.nmaas.portal.service.DomainService;

import java.util.Set;

/**
 * Shared logic of the basic and OIDC login flows for users with pending
 * AUP / Privacy Notice approvals.
 */
@Slf4j
final class UserApprovalsHelper {

    private UserApprovalsHelper() {
    }

    /**
     * Adds the {@link Role#ROLE_NOT_ACCEPTED} role, in memory only, to the user
     * that is about to receive tokens, so that the issued JWT makes the portal
     * present the AUP/Privacy Notice approval screens; acceptance is then recorded
     * through {@code POST /users/terms/{username}}.
     *
     * <p>Idempotent: the role is assigned only when the user does not hold it
     * yet. Assigning a second in-memory representation of an already held role
     * made the subsequent entity merge fail with
     * {@code Multiple representations of the same entity} on repeated logins.</p>
     */
    static void assignNotAcceptedRoleIfMissing(User user, DomainService domains) {
        if (!user.isTermsOfUseAccepted() || !user.isPrivacyPolicyAccepted()) {
            log.info("Check during login: Terms of Use or Privacy Policy were not accepted by user [{}]", user.getUsername());
            if (user.getRoles().stream().noneMatch(role -> role.getRole() == Role.ROLE_NOT_ACCEPTED)) {
                user.setNewRoles(Set.of(new UserRole(user, domains.getGlobalDomain().orElseThrow(SignupException::new), Role.ROLE_NOT_ACCEPTED)));
            }
        }
    }
}
