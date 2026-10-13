package net.geant.nmaas.portal.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.exceptions.AuthenticationException;
import net.geant.nmaas.portal.api.security.config.SsoConfiguration;
import net.geant.nmaas.portal.service.OidcIdTokenValidator;
import net.geant.nmaas.portal.service.OidcUserClaims;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Validates OIDC ID tokens submitted by the portal frontend.
 *
 * <p>Token payloads coming from the browser can never be trusted as-is, so before
 * any account linking or registration happens the token is verified against the
 * configured identity provider: the signature is checked using the provider's
 * JWKS endpoint, and the issuer, audience and expiry claims are validated.
 * Identity-related request fields (subject, email, names) are then taken from the
 * validated token claims instead of the request body.</p>
 */
@Service
@ConditionalOnProperty(value = "portal.config.ssoLoginAllowed", havingValue = "true")
@Slf4j
public class OidcIdTokenValidatorImpl implements OidcIdTokenValidator {

    private static final String EMAIL_CLAIM = "email";

    private static final String PREFERRED_USERNAME_CLAIM = "preferred_username";

    private static final String GIVEN_NAME_CLAIM = "given_name";

    private static final String FAMILY_NAME_CLAIM = "family_name";

    private final ClientRegistration clientRegistration;

    private final JwtDecoder jwtDecoder;

    public OidcIdTokenValidatorImpl(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistration = clientRegistrationRepository.findByRegistrationId(SsoConfiguration.OIDC_REGISTRATION_ID);
        if (this.clientRegistration == null) {
            throw new IllegalStateException("OIDC client registration [" + SsoConfiguration.OIDC_REGISTRATION_ID + "] is not configured");
        }
        if (!StringUtils.hasText(this.clientRegistration.getProviderDetails().getIssuerUri())) {
            throw new IllegalStateException("Issuer URI is not configured for OIDC client registration [" + SsoConfiguration.OIDC_REGISTRATION_ID + "]");
        }
        this.jwtDecoder = buildJwtDecoder();
    }

    @Override
    public OidcUserClaims validate(String idToken) {
        if (!StringUtils.hasText(idToken)) {
            log.warn("OIDC ID token rejected: token is missing");
            throw new AuthenticationException("Missing OIDC token");
        }
        Jwt jwt = decode(idToken);
        verifyIssuer(jwt);
        verifyAudience(jwt);
        return extractClaims(jwt);
    }

    private Jwt decode(String idToken) {
        try {
            // signature (RS256 against the provider JWKS endpoint) and expiry are verified by the decoder
            return jwtDecoder.decode(idToken);
        } catch (JwtException e) {
            log.warn("OIDC ID token rejected: {}", e.getMessage());
            throw new AuthenticationException("Invalid OIDC token");
        }
    }

    private void verifyIssuer(Jwt jwt) {
        String expectedIssuer = clientRegistration.getProviderDetails().getIssuerUri();
        // Jwt#getIssuer() parses the claim into a java.net.URL, so the raw claim string is compared instead
        String issuer = jwt.getClaimAsString(JwtClaimNames.ISS);
        if (!expectedIssuer.equals(issuer)) {
            log.warn("OIDC ID token rejected: issuer [{}] does not match expected [{}]", issuer, expectedIssuer);
            throw new AuthenticationException("Invalid OIDC token");
        }
    }

    private void verifyAudience(Jwt jwt) {
        String clientId = clientRegistration.getClientId();
        List<String> audience = jwt.getAudience();
        if (audience == null || !audience.contains(clientId)) {
            log.warn("OIDC ID token rejected: audience {} does not contain client id [{}]", audience, clientId);
            throw new AuthenticationException("Invalid OIDC token");
        }
    }

    private OidcUserClaims extractClaims(Jwt jwt) {
        String sub = jwt.getSubject();
        String email = jwt.getClaimAsString(EMAIL_CLAIM);
        if (!StringUtils.hasText(sub) || !StringUtils.hasText(email)) {
            log.warn("OIDC ID token rejected: missing [sub] or [email] claim");
            throw new AuthenticationException("Invalid OIDC token");
        }
        return new OidcUserClaims(
                sub,
                email,
                jwt.getClaimAsString(PREFERRED_USERNAME_CLAIM),
                jwt.getClaimAsString(GIVEN_NAME_CLAIM),
                jwt.getClaimAsString(FAMILY_NAME_CLAIM));
    }

    private JwtDecoder buildJwtDecoder() {
        String jwkSetUri = clientRegistration.getProviderDetails().getJwkSetUri();
        if (!StringUtils.hasText(jwkSetUri)) {
            throw new IllegalStateException("JWK set URI is not configured for OIDC client registration [" + SsoConfiguration.OIDC_REGISTRATION_ID + "]");
        }
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

}
