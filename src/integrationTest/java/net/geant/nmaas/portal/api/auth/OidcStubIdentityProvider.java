package net.geant.nmaas.portal.api.auth;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Minimal in-process OIDC identity provider used by the OIDC integration tests.
 *
 * <p>Serves the four endpoints of the Keycloak-style layout that the platform
 * registration is configured with:</p>
 * <ul>
 *     <li>{@code /realms/test/protocol/openid-connect/auth} - not actually
 *     invoked, because MockMvc does not follow the redirect to the provider,</li>
 *     <li>{@code .../token} - returns the ID token configured by the test and
 *     records every authorization code exchange (including the PKCE
 *     {@code code_verifier} and the client authentication header),</li>
 *     <li>{@code .../userinfo} - returns the claims configured by the test,</li>
 *     <li>{@code .../certs} - the JWKS of the stub signing key.</li>
 * </ul>
 *
 * <p>Two RSA keys are available: {@code signingKey} (published in the JWKS)
 * and {@code unknownKey} (never published), so tests can mint tokens with an
 * untrusted signature and verify they are rejected.</p>
 */
public final class OidcStubIdentityProvider {

    /**
     * Client credentials of the registration; the tests configure the same
     * values through dynamic properties.
     */
    public static final String CLIENT_ID = "nmaas-itest-oidc-client";

    public static final String CLIENT_SECRET = "nmaas-itest-oidc-secret";

    private static final String OIDC_BASE_PATH = "/realms/test/protocol/openid-connect";

    private static final String TOKEN_PATH = OIDC_BASE_PATH + "/token";

    private static final String USER_INFO_PATH = OIDC_BASE_PATH + "/userinfo";

    private static final String CERTS_PATH = OIDC_BASE_PATH + "/certs";

    private final HttpServer server;

    private final RSAKey signingKey;

    private final RSAKey unknownKey;

    private volatile String nextIdToken;

    private volatile Map<String, Object> userInfoClaims = Map.of();

    private final List<RecordedRequest> tokenRequests = new CopyOnWriteArrayList<>();

    private final List<RecordedRequest> userInfoRequests = new CopyOnWriteArrayList<>();

    private OidcStubIdentityProvider(HttpServer server, RSAKey signingKey, RSAKey unknownKey) {
        this.server = server;
        this.signingKey = signingKey;
        this.unknownKey = unknownKey;
    }

    /**
     * A single provider instance must be shared by all test classes that
     * reference it, so that every class resolves the same issuer URI and the
     * Spring test framework can cache one application context for all of them.
     */
    public static OidcStubIdentityProvider start() {
        try {
            RSAKey signingKey = new RSAKeyGenerator(2048).keyID("stub-signing-key").generate();
            RSAKey unknownKey = new RSAKeyGenerator(2048).keyID("stub-unknown-key").generate();
            String jwks = new JWKSet(signingKey.toPublicJWK()).toString();
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            OidcStubIdentityProvider provider = new OidcStubIdentityProvider(server, signingKey, unknownKey);
            server.createContext("/", exchange -> provider.handle(exchange, jwks));
            server.start();
            // Stops the embedded server when the test worker JVM exits, so the
            // stub can never keep the worker (or its jacoco flush) alive.
            Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0), "oidc-stub-shutdown"));
            return provider;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot start the stub OIDC identity provider", e);
        }
    }

    /**
     * An HTTP request recorded by the stub, for asserting how the platform
     * contacted the identity provider.
     *
     * @param body the raw request body (form parameters of the token request)
     * @param authorizationHeader the {@code Authorization} header value or {@code null}
     */
    public record RecordedRequest(String body, String authorizationHeader) {
    }

    public String issuer() {
        return "http://localhost:" + server.getAddress().getPort() + "/realms/test";
    }

    public List<RecordedRequest> tokenRequests() {
        return List.copyOf(tokenRequests);
    }

    public List<RecordedRequest> userInfoRequests() {
        return List.copyOf(userInfoRequests);
    }

    public RecordedRequest lastTokenRequest() {
        if (tokenRequests.isEmpty()) {
            throw new IllegalStateException("The stub identity provider has not been contacted at the token endpoint");
        }
        return tokenRequests.getLast();
    }

    public void setNextIdToken(String idToken) {
        this.nextIdToken = idToken;
    }

    public void setUserInfo(String sub, String email, String preferredUsername, String givenName, String familyName) {
        this.userInfoClaims = Map.of(
                "sub", sub,
                "email", email,
                "preferred_username", preferredUsername,
                "given_name", givenName,
                "family_name", familyName);
    }

    /**
     * Clears the recorded requests and the configured responses, so that tests
     * do not observe each other's state.
     */
    public void reset() {
        tokenRequests.clear();
        userInfoRequests.clear();
        nextIdToken = null;
        userInfoClaims = Map.of();
    }

    /**
     * Mints a valid ID token: signed with the published key, addressed to the
     * platform client, issued by the stub issuer.
     */
    public String mintValidIdToken(String sub, String email) {
        return mintValidIdToken(sub, email, null);
    }

    /**
     * As {@link #mintValidIdToken(String, String)}, with the nonce received in
     * the authorization request - Spring Security requires the ID token to echo
     * the nonce parameter that was sent to the provider.
     */
    public String mintValidIdToken(String sub, String email, String nonce) {
        return mint(sub, email, nonce, issuer(), List.of(CLIENT_ID), Date.from(Instant.now().plusSeconds(300)), signingKey);
    }

    public String mintExpiredIdToken(String sub, String email) {
        return mint(sub, email, null, issuer(), List.of(CLIENT_ID), Date.from(Instant.now().minusSeconds(3600)), signingKey);
    }

    /**
     * Mints a token signed with a key that is not published in the JWKS - the
     * signature verification must fail.
     */
    public String mintIdTokenWithUnknownSigningKey(String sub, String email, String nonce) {
        return mint(sub, email, nonce, issuer(), List.of(CLIENT_ID), Date.from(Instant.now().plusSeconds(300)), unknownKey);
    }

    public String mintIdTokenWithDifferentIssuer(String sub, String email) {
        return mint(sub, email, null, "http://evil.example.com/realms/other", List.of(CLIENT_ID), Date.from(Instant.now().plusSeconds(300)), signingKey);
    }

    public String mintIdTokenWithDifferentAudience(String sub, String email) {
        return mint(sub, email, null, issuer(), List.of("other-client"), Date.from(Instant.now().plusSeconds(300)), signingKey);
    }

    private String mint(String sub,
                        String email,
                        String nonce,
                        String issuer,
                        List<String> audience,
                        Date expiration,
                        RSAKey key) {
        try {
            JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                    .issuer(issuer)
                    .audience(audience)
                    .subject(sub)
                    .issueTime(new Date())
                    .expirationTime(expiration)
                    .claim("email", email)
                    .claim("preferred_username", sub + "-user")
                    .claim("given_name", "Given")
                    .claim("family_name", "Family");
            if (nonce != null) {
                claims.claim("nonce", nonce);
            }
            SignedJWT jwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build(),
                    claims.build());
            jwt.sign(new RSASSASigner(key));
            return jwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot mint a stub ID token", e);
        }
    }

    private void handle(HttpExchange exchange, String jwks) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (CERTS_PATH.equals(path)) {
            respond(exchange, 200, jwks);
        } else if (TOKEN_PATH.equals(path) && "POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            tokenRequests.add(new RecordedRequest(body, exchange.getRequestHeaders().getFirst("Authorization")));
            respond(exchange, 200, tokenResponse());
        } else if (USER_INFO_PATH.equals(path)) {
            userInfoRequests.add(new RecordedRequest(null, exchange.getRequestHeaders().getFirst("Authorization")));
            respond(exchange, 200, userInfoResponse());
        } else {
            respond(exchange, 404, "{\"error\":\"not found\"}");
        }
    }

    private String tokenResponse() {
        if (nextIdToken == null) {
            throw new IllegalStateException("No ID token configured for the stub identity provider - call setNextIdToken first");
        }
        return "{\"access_token\":\"stub-access-token\""
                + ",\"token_type\":\"Bearer\""
                + ",\"expires_in\":3600"
                + ",\"scope\":\"openid profile email\""
                + ",\"id_token\":\"" + nextIdToken + "\"}";
    }

    private String userInfoResponse() {
        StringBuilder json = new StringBuilder("{");
        userInfoClaims.forEach((name, value) -> {
            if (json.length() > 1) {
                json.append(',');
            }
            json.append('"').append(name).append("\":\"").append(value).append('"');
        });
        return json.append('}').toString();
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

}
