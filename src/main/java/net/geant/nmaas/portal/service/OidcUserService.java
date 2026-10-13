package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.persistence.entity.User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public interface OidcUserService {

    User checkUser(OidcUser oidcUser);

    User registerNewUser(OidcUser oidcUser);

    /**
     * Registers a new user based on identity claims extracted from a validated OIDC ID token.
     *
     * @param claims            validated identity claims (subject, email, names)
     * @param requestedUsername username requested by the user; falls back to the preferred_username claim when blank
     * @param isAupAccepted     whether the user accepted the Acceptable Use Policy
     * @param isPnAccepted      whether the user accepted the Privacy Notice
     * @return newly created user
     */
    User registerNewUser(OidcUserClaims claims, String requestedUsername, boolean isAupAccepted, boolean isPnAccepted);

    boolean externalUserRequiresLinking(OidcUser oidcUser);

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
