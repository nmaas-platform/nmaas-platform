package net.geant.nmaas.portal.api.security.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the OIDC client registration is built purely from configuration,
 * without contacting the identity provider for issuer discovery: constructing the
 * repository must not perform any network access.
 */
class SsoConfigurationTest {

    private static final String ISSUER = "http://idp.example.org/realms/test";

    private SsoConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = new SsoConfiguration();
        ReflectionTestUtils.setField(configuration, "clientId", "test-client-id");
        ReflectionTestUtils.setField(configuration, "clientSecret", "test-client-secret");
        ReflectionTestUtils.setField(configuration, "scope", "openid, profile, email");
        ReflectionTestUtils.setField(configuration, "redirectUri",
                "{baseUrl}/api/v1/login/oauth2/code/{registrationId}");
        ReflectionTestUtils.setField(configuration, "issuerUri", ISSUER);
        ReflectionTestUtils.setField(configuration, "authorizationUri",
                ISSUER + "/protocol/openid-connect/auth");
        ReflectionTestUtils.setField(configuration, "tokenUri",
                ISSUER + "/protocol/openid-connect/token");
        ReflectionTestUtils.setField(configuration, "jwkSetUri",
                ISSUER + "/protocol/openid-connect/certs");
        ReflectionTestUtils.setField(configuration, "userInfoUri",
                ISSUER + "/protocol/openid-connect/userinfo");
        ReflectionTestUtils.setField(configuration, "userNameAttributeName", "sub");
    }

    @Test
    void shouldBuildRegistrationFromExplicitlyConfiguredEndpoints() {
        ClientRegistrationRepository repository = configuration.clientRegistrationRepository();

        ClientRegistration registration = repository.findByRegistrationId(SsoConfiguration.OIDC_REGISTRATION_ID);
        assertThat(registration).isNotNull();
        assertThat(registration.getClientId()).isEqualTo("test-client-id");
        assertThat(registration.getAuthorizationGrantType()).isEqualTo(AuthorizationGrantType.AUTHORIZATION_CODE);
        assertThat(registration.getRedirectUri()).isEqualTo("{baseUrl}/api/v1/login/oauth2/code/{registrationId}");
        assertThat(registration.getScopes()).containsExactly("openid", "profile", "email");

        ClientRegistration.ProviderDetails provider = registration.getProviderDetails();
        assertThat(provider.getAuthorizationUri()).isEqualTo(ISSUER + "/protocol/openid-connect/auth");
        assertThat(provider.getTokenUri()).isEqualTo(ISSUER + "/protocol/openid-connect/token");
        assertThat(provider.getUserInfoEndpoint().getUri()).isEqualTo(ISSUER + "/protocol/openid-connect/userinfo");
        assertThat(provider.getUserInfoEndpoint().getUserNameAttributeName()).isEqualTo("sub");
    }

    @Test
    void shouldExposeIssuerAndJwkSetUriRequiredForIdTokenValidation() {
        ClientRegistrationRepository repository = configuration.clientRegistrationRepository();

        ClientRegistration registration = repository.findByRegistrationId(SsoConfiguration.OIDC_REGISTRATION_ID);
        // OidcIdTokenValidatorImpl requires both to verify ID token signatures and the iss claim.
        assertThat(registration.getProviderDetails().getIssuerUri()).isEqualTo(ISSUER);
        assertThat(registration.getProviderDetails().getJwkSetUri()).isEqualTo(ISSUER + "/protocol/openid-connect/certs");
    }

    @Test
    void shouldIgnoreBlankScopeEntries() {
        ReflectionTestUtils.setField(configuration, "scope", "openid,, profile , email,");

        ClientRegistration registration = configuration.clientRegistrationRepository()
                .findByRegistrationId(SsoConfiguration.OIDC_REGISTRATION_ID);

        assertThat(registration.getScopes()).containsExactly("openid", "profile", "email");
    }
}
