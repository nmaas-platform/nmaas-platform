package net.geant.nmaas.portal.api.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.Arrays;
import java.util.List;

@Configuration
@ConditionalOnProperty(
        value = "portal.config.ssoLoginAllowed",
        havingValue = "true"
)
@PropertySource("classpath:application-oidc.properties")
public class SsoConfiguration {

    @Value("${spring.security.oauth2.client.registration.my-oidc.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.my-oidc.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.my-oidc.scope:openid,profile,email}")
    private String scope;

    @Value("${spring.security.oauth2.client.registration.my-oidc.redirect-uri:{baseUrl}/api/login/oauth2/code/{registrationId}}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.my-oidc.issuer-uri}")
    private String issuerUri;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration registration = ClientRegistrations
                .fromIssuerLocation(issuerUri)
                .registrationId("my-oidc")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .scope(parseScope(scope))
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(redirectUri)
                .build();
        return new InMemoryClientRegistrationRepository(registration);
    }

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
            ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    private List<String> parseScope(String configuredScope) {
        return Arrays.stream(configuredScope.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
