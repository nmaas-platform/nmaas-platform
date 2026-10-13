package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.exceptions.AuthenticationException;

/**
 * Validates raw OIDC ID tokens issued by the configured identity provider
 * and extracts identity claims that can be trusted by the application.
 */
public interface OidcIdTokenValidator {

    /**
     * Validates the given ID token (signature against the provider's JWKS endpoint,
     * issuer, audience and expiry) and returns the identity claims carried by it.
     *
     * @param idToken raw ID token value
     * @return validated identity claims
     * @throws AuthenticationException when the token is missing, malformed, signed
     *                                 by an unknown key, expired or issued for another party
     */
    OidcUserClaims validate(String idToken);

}
