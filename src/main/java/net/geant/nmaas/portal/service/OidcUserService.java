package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.persistence.entity.User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public interface OidcUserService {

    /**
     * Resolves the local account matching the external identity. Never creates
     * accounts: registration is only possible through the approvals endpoint
     * with explicit AUP and Privacy Notice consent.
     *
     * @throws net.geant.nmaas.portal.api.exceptions.ExternalUserMatchException when no
     *         local account matches, the identity is incomplete, or the account
     *         is linked to a different external identity
     */
    User checkUser(OidcUser oidcUser);

    /**
     * Registers a new user based on identity claims extracted from a validated OIDC ID token.
     * This is the only supported way of creating an account for an external user.
     *
     * @param claims            validated identity claims (subject, email, names)
     * @param requestedUsername username requested by the user; falls back to the preferred_username claim when blank
     * @param isAupAccepted     whether the user accepted the Acceptable Use Policy
     * @param isPnAccepted      whether the user accepted the Privacy Notice
     * @return newly created user
     */
    User registerNewUser(OidcUserClaims claims, String requestedUsername, boolean isAupAccepted, boolean isPnAccepted);

    boolean externalUserRequiresLinking(OidcUser oidcUser);

    /**
     * Returns true only for external users without a local account, who must be
     * directed to the first-login registration page where AUP and Privacy Notice
     * consent is collected. Pending approvals of existing accounts are enforced
     * at token issuance time with the ROLE_NOT_ACCEPTED role instead.
     */
    boolean externalUserRequiresAupAndPn(OidcUser oidcUser);

    /**
     * Links the local account identified by the email claim of the validated OIDC token
     * with the external identity represented by the token subject.
     *
     * @param claims validated identity claims
     * @return linked user
     * @throws net.geant.nmaas.portal.api.exceptions.ExternalUserCanNotBeLinked when the account is already linked to a different external identity
     */
    User linkUser(OidcUserClaims claims);

}
