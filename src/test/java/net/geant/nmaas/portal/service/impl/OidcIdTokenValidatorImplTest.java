package net.geant.nmaas.portal.service.impl;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;
import net.geant.nmaas.portal.api.exceptions.AuthenticationException;
import net.geant.nmaas.portal.api.security.config.SsoConfiguration;
import net.geant.nmaas.portal.service.OidcUserClaims;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies that the OIDC ID token validator actually rejects tokens that are
 * not trustworthy (wrong signature, wrong issuer, wrong audience, expired)
 * and extracts claims only from validly signed tokens.
 */
class OidcIdTokenValidatorImplTest {

    private static final String ISSUER = "http://localhost:8080/realms/test";

    private static final String CLIENT_ID = "nmaas-oidc";

    private static HttpServer jwksServer;

    private static RSAKey signingKey;

    private static RSAKey otherKey;

    private static OidcIdTokenValidatorImpl validator;

    @BeforeAll
    static void setUp() throws Exception {
        signingKey = new RSAKeyGenerator(2048).keyID("signing-key").generate();
        otherKey = new RSAKeyGenerator(2048).keyID("other-key").generate();
        String jwks = new JWKSet(signingKey.toPublicJWK()).toString();

        jwksServer = HttpServer.create(new InetSocketAddress(0), 0);
        jwksServer.createContext("/jwks", exchange -> {
            byte[] body = jwks.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        jwksServer.start();

        validator = new OidcIdTokenValidatorImpl(
                clientRegistrationRepository("http://localhost:" + jwksServer.getAddress().getPort() + "/jwks"));
    }

    @AfterAll
    static void tearDown() {
        jwksServer.stop(0);
    }

    @Test
    void shouldValidateTokenAndExtractClaims() {
        OidcUserClaims claims = validator.validate(validToken());

        assertEquals("test-sub", claims.sub());
        assertEquals("test@example.com", claims.email());
        assertEquals("testuser", claims.preferredUsername());
        assertEquals("John", claims.givenName());
        assertEquals("Doe", claims.familyName());
    }

    @Test
    void shouldRejectExpiredToken() {
        String token = token(claims -> {}, Date.from(Instant.now().minusSeconds(3600)), ISSUER, List.of(CLIENT_ID));

        assertInvalid(token);
    }

    @Test
    void shouldRejectTokenIssuedByDifferentIssuer() {
        String token = token(claims -> {}, Date.from(Instant.now().plusSeconds(300)), "http://evil.example.com", List.of(CLIENT_ID));

        assertInvalid(token);
    }

    @Test
    void shouldRejectTokenIssuedForDifferentAudience() {
        String token = token(claims -> {}, Date.from(Instant.now().plusSeconds(300)), ISSUER, List.of("other-client"));

        assertInvalid(token);
    }

    @Test
    void shouldRejectTokenSignedWithUnknownKey() throws Exception {
        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(otherKey.getKeyID()).build(),
                defaultClaims().build());
        jwt.sign(new RSASSASigner(otherKey));

        assertInvalid(jwt.serialize());
    }

    @Test
    void shouldRejectTamperedToken() {
        String valid = validToken();
        String[] parts = valid.split("\\.");
        String tampered = parts[0] + "." + parts[1] + "." + parts[2].substring(0, parts[2].length() - 2) + "aa";

        assertInvalid(tampered);
    }

    @Test
    void shouldRejectTokenWithoutEmailClaim() {
        String token = token(claims -> claims.claim("email", null),
                Date.from(Instant.now().plusSeconds(300)), ISSUER, List.of(CLIENT_ID));

        assertInvalid(token);
    }

    @Test
    void shouldRejectMalformedToken() {
        assertInvalid("not-a-jwt");
    }

    @Test
    void shouldRejectMissingToken() {
        assertThrows(AuthenticationException.class, () -> validator.validate(null));
        assertThrows(AuthenticationException.class, () -> validator.validate(""));
        assertThrows(AuthenticationException.class, () -> validator.validate("   "));
    }

    private void assertInvalid(String token) {
        AuthenticationException ex = assertThrows(AuthenticationException.class, () -> validator.validate(token));
        assertEquals("Invalid OIDC token", ex.getMessage());
    }

    private String validToken() {
        return token(claims -> {}, Date.from(Instant.now().plusSeconds(300)), ISSUER, List.of(CLIENT_ID));
    }

    private String token(java.util.function.Consumer<JWTClaimsSet.Builder> claimsCustomizer,
                         Date expiration, String issuer, List<String> audience) {
        try {
            JWTClaimsSet.Builder builder = defaultClaims();
            claimsCustomizer.accept(builder);
            SignedJWT jwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(signingKey.getKeyID()).build(),
                    builder
                            .expirationTime(expiration)
                            .issuer(issuer)
                            .audience(audience)
                            .build());
            jwt.sign(new RSASSASigner(signingKey));
            return jwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private JWTClaimsSet.Builder defaultClaims() {
        return new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .issueTime(new Date())
                .claim("sub", "test-sub")
                .claim("email", "test@example.com")
                .claim("preferred_username", "testuser")
                .claim("given_name", "John")
                .claim("family_name", "Doe");
    }

    private static ClientRegistrationRepository clientRegistrationRepository(String jwkSetUri) {
        ClientRegistration registration = ClientRegistration.withRegistrationId(SsoConfiguration.OIDC_REGISTRATION_ID)
                .clientId(CLIENT_ID)
                .clientSecret("secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/api/v1/login/oauth2/code/my-oidc")
                .scope("openid", "profile", "email")
                .authorizationUri(ISSUER + "/protocol/openid-connect/auth")
                .tokenUri(ISSUER + "/protocol/openid-connect/token")
                .userInfoUri(ISSUER + "/protocol/openid-connect/userinfo")
                .jwkSetUri(jwkSetUri)
                .issuerUri(ISSUER)
                .build();
        return new InMemoryClientRegistrationRepository(registration);
    }

}
