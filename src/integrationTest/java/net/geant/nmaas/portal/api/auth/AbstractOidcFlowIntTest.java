package net.geant.nmaas.portal.api.auth;

import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base class of the OIDC integration tests. Unlike {@link OidcAuthSecurityIntTest},
 * which replaces the client registration with a hand-built bean, these tests run
 * against the real {@code SsoConfiguration} registration and point every provider
 * endpoint at {@link OidcStubIdentityProvider} through dynamic properties.
 *
 * <p>The stub provider is a single JVM-wide instance shared by all subclasses, so
 * that every subclass resolves the same property values and the Spring test
 * framework can cache a single application context for all of them.</p>
 */
public abstract class AbstractOidcFlowIntTest extends BaseControllerTestSetup {

    protected static final OidcStubIdentityProvider STUB_IDP = OidcStubIdentityProvider.start();

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @DynamicPropertySource
    static void stubIdentityProviderProperties(DynamicPropertyRegistry registry) {
        String issuer = STUB_IDP.issuer();
        registry.add("portal.config.ssoLoginAllowed", () -> "true");
        registry.add("spring.security.oauth2.client.registration.my-oidc.client-id", () -> OidcStubIdentityProvider.CLIENT_ID);
        registry.add("spring.security.oauth2.client.registration.my-oidc.client-secret", () -> OidcStubIdentityProvider.CLIENT_SECRET);
        registry.add("spring.security.oauth2.client.provider.my-oidc.issuer-uri", STUB_IDP::issuer);
        registry.add("spring.security.oauth2.client.provider.my-oidc.authorization-uri", () -> issuer + "/protocol/openid-connect/auth");
        registry.add("spring.security.oauth2.client.provider.my-oidc.token-uri", () -> issuer + "/protocol/openid-connect/token");
        registry.add("spring.security.oauth2.client.provider.my-oidc.jwk-set-uri", () -> issuer + "/protocol/openid-connect/certs");
        registry.add("spring.security.oauth2.client.provider.my-oidc.user-info-uri", () -> issuer + "/protocol/openid-connect/userinfo");
    }

    /**
     * Seeds a local account the way the linking and login flows see it. The
     * password is stored encoded, exactly like in production.
     */
    protected User seedUser(String username, String email, String rawPassword, String externalId,
                            boolean termsOfUseAccepted, boolean privacyPolicyAccepted) {
        User user = new User(username, true, passwordEncoder.encode(rawPassword),
                domains.getGlobalDomain().get(), Role.ROLE_USER, termsOfUseAccepted, privacyPolicyAccepted);
        user.setEmail(email);
        user.setExternalId(externalId);
        return userRepository.save(user);
    }

    /**
     * Decodes the payload of a JWT issued by the portal. The tests only inspect
     * claim values, so no signature verification is needed.
     */
    protected static String jwtPayload(String token) {
        return new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
    }

    /**
     * Parses the (URL-decoded) query parameters of a URL.
     */
    protected static Map<String, String> queryParameters(String url) {
        String query = url.substring(url.indexOf('?') + 1);
        Map<String, String> parameters = new LinkedHashMap<>();
        for (String parameter : query.split("&")) {
            int separator = parameter.indexOf('=');
            if (separator > 0) {
                parameters.put(
                        URLDecoder.decode(parameter.substring(0, separator), StandardCharsets.UTF_8),
                        URLDecoder.decode(parameter.substring(separator + 1), StandardCharsets.UTF_8));
            }
        }
        return parameters;
    }

}
