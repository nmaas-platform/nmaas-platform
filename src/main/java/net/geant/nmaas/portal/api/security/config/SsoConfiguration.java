package net.geant.nmaas.portal.api.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.Arrays;
import java.util.List;

/**
 * Builds the single OIDC client registration used by the platform.
 *
 * <p>All provider endpoints (authorization, token, JWKS, user info) are taken from
 * configuration explicitly instead of being resolved with OIDC issuer discovery.
 * Discovery requires the identity provider to be reachable while the application
 * context starts, which would turn a temporarily unavailable IdP into a failed
 * deployment. The issuer URI is still recorded on the registration because
 * {@code OidcIdTokenValidatorImpl} compares the {@code iss} claim of validated ID
 * tokens against it.</p>
 */
@Configuration
@ConditionalOnProperty(
        value = "portal.config.ssoLoginAllowed",
        havingValue = "true"
)
@PropertySource("classpath:application-oidc.properties")
public class SsoConfiguration {

    /**
     * Identifier of the single OIDC client registration used by the platform.
     */
    public static final String OIDC_REGISTRATION_ID = "my-oidc";

    @Value("${spring.security.oauth2.client.registration.my-oidc.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.my-oidc.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.my-oidc.scope:openid,profile,email}")
    private String scope;

    @Value("${spring.security.oauth2.client.registration.my-oidc.redirect-uri:{baseUrl}/api/v1/login/oauth2/code/{registrationId}}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.my-oidc.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.client.provider.my-oidc.authorization-uri}")
    private String authorizationUri;

    @Value("${spring.security.oauth2.client.provider.my-oidc.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.my-oidc.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${spring.security.oauth2.client.provider.my-oidc.user-info-uri}")
    private String userInfoUri;

    /**
     * Attribute used as the authenticated principal name. Previously resolved from
     * issuer discovery, where Spring Security defaults it to {@code sub}; the value
     * is mandatory in every OIDC user info response, so {@code sub} keeps the
     * behaviour identical.
     */
    @Value("${spring.security.oauth2.client.provider.my-oidc.user-name-attribute:sub}")
    private String userNameAttributeName;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration registration = ClientRegistration.withRegistrationId(OIDC_REGISTRATION_ID)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(redirectUri)
                .scope(parseScope(scope))
                .issuerUri(issuerUri)
                .authorizationUri(authorizationUri)
                .tokenUri(tokenUri)
                .jwkSetUri(jwkSetUri)
                .userInfoUri(userInfoUri)
                .userNameAttributeName(userNameAttributeName)
                .build();
        return new InMemoryClientRegistrationRepository(registration);
    }

    private List<String> parseScope(String configuredScope) {
        return Arrays.stream(configuredScope.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
