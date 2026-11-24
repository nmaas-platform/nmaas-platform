package net.geant.nmaas.portal.service;

/**
 * Identity claims extracted from an OIDC ID token that has been validated
 * server-side (signature, issuer, audience and expiry).
 *
 * <p>Values in this record can be trusted, in contrast to raw request payload
 * fields provided by the client.</p>
 *
 * @param sub               subject identifier of the user at the identity provider (never null)
 * @param email             email claim (never null)
 * @param preferredUsername preferred_username claim, may be null
 * @param givenName         given_name claim, may be null
 * @param familyName        family_name claim, may be null
 */
public record OidcUserClaims(String sub,
                             String email,
                             String preferredUsername,
                             String givenName,
                             String familyName) {
}
