package net.geant.nmaas.portal.api.auth;

import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the security rules of the OIDC login flow:
 * <ul>
 *     <li>the OAuth2 success endpoint must reject anonymous requests instead of
 *     failing with a server error,</li>
 *     <li>tokens are only issued for a valid OIDC session,</li>
 *     <li>the server-side session is terminated once tokens have been issued,</li>
 *     <li>pending AUP/Privacy Notice approvals route the user to the
 *     first-login approvals page instead of issuing tokens.</li>
 * </ul>
 *
 * The {@link ClientRegistrationRepository} is replaced with a repository backed
 * by a hand-built registration, so that the application context does not perform
 * OIDC issuer discovery at startup. Endpoints of the registration point at an
 * unreachable address - they are never contacted by these tests.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional(value = TxType.REQUIRES_NEW)
@TestPropertySource(properties = "portal.config.ssoLoginAllowed=true")
public class OidcAuthSecurityIntTest extends BaseControllerTestSetup {

    private static final String OIDC_SUCCESS_URL = "/api/v1/oidc/success";

    @Autowired
    private UserRepository userRepository;

    @TestBean
    ClientRegistrationRepository clientRegistrationRepository;

    static ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("my-oidc")
                .clientId("test-client-id")
                .clientSecret("test-client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .authorizationUri("http://localhost:9999/authorize")
                .tokenUri("http://localhost:9999/token")
                .userInfoUri("http://localhost:9999/userinfo")
                .jwkSetUri("http://localhost:9999/jwks")
                .issuerUri("http://localhost:9999")
                .build();
        return new InMemoryClientRegistrationRepository(registration);
    }

    @BeforeEach
    public void setup() {
        createMVC();
    }

    @AfterEach
    public void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldRejectOidcSuccessWithoutAuthentication() throws Exception {
        SecurityContextHolder.clearContext();

        mvc.perform(get(OIDC_SUCCESS_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldIssueTokensAndTerminateSessionForLinkedOidcUser() throws Exception {
        userRepository.save(oidcUser("oidc-it-linked", "oidc-it-linked@example.com", "oidc-it-linked-sub"));

        MockHttpSession session = oidcSession("oidc-it-linked-sub", "oidc-it-linked@example.com");
        MvcResult result = mvc.perform(get(OIDC_SUCCESS_URL).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login-success?token=**"))
                .andReturn();

        assertTrue(result.getResponse().getRedirectedUrl().contains("refresh-token="));
        assertTrue(result.getResponse().getRedirectedUrl().contains("oidc-token="));
        assertTrue(session.isInvalid(), "OIDC session should be invalidated after issuing tokens");
    }

    @Test
    public void shouldRedirectToLinkingAndTerminateSessionForUnlinkedAccount() throws Exception {
        userRepository.save(oidcUser("oidc-it-unlinked", "oidc-it-unlinked@example.com", null));

        MockHttpSession session = oidcSession("oidc-it-unlinked-sub", "oidc-it-unlinked@example.com");
        MvcResult result = mvc.perform(get(OIDC_SUCCESS_URL).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login-linking?oidc-token=**"))
                .andReturn();

        assertTrue(session.isInvalid(), "OIDC session should be invalidated also when linking is required");
    }

    @Test
    public void shouldRouteLinkedUserWithPendingApprovalsToFirstLoginPage() throws Exception {
        User user = new User("oidc-it-notaccepted", true, "password",
                domains.getGlobalDomain().get(), Role.ROLE_USER, false, false);
        user.setEmail("oidc-it-notaccepted@example.com");
        user.setExternalId("oidc-it-notaccepted-sub");
        userRepository.save(user);

        MockHttpSession session = oidcSession("oidc-it-notaccepted-sub", "oidc-it-notaccepted@example.com");
        mvc.perform(get(OIDC_SUCCESS_URL).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/sso-first-login?oidc-token=**"));

        assertTrue(session.isInvalid(), "OIDC session should be invalidated when routing to the approvals page");
    }

    private User oidcUser(String username, String email, String externalId) {
        User user = new User(username, true, "password", domains.getGlobalDomain().get(), Role.ROLE_USER, true, true);
        user.setEmail(email);
        user.setExternalId(externalId);
        return user;
    }

    /**
     * Builds a session carrying the authenticated OAuth2 user, mimicking the state
     * established by the OAuth2 login filters before the browser is redirected to
     * the success endpoint.
     */
    private MockHttpSession oidcSession(String sub, String email) {
        OidcIdToken idToken = OidcIdToken.withTokenValue("test-id-token-value")
                .claim("sub", sub)
                .claim("email", email)
                .claim("preferred_username", "preferred-username")
                .build();
        DefaultOidcUser principal = new DefaultOidcUser(AuthorityUtils.createAuthorityList("ROLE_USER"), idToken);
        OAuth2AuthenticationToken authentication =
                new OAuth2AuthenticationToken(principal, principal.getAuthorities(), "my-oidc");
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        return session;
    }

}
