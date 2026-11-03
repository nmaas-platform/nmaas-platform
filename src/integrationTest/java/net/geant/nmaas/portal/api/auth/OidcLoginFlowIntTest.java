package net.geant.nmaas.portal.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import net.geant.nmaas.portal.persistence.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end integration tests of the OAuth2 authorization code login dance
 * with PKCE, driven through the real security filter chain:
 *
 * <pre>
 * GET /api/v1/oauth2/authorization/my-oidc
 *   -> 302 to the stub provider authorization endpoint (state, nonce, PKCE challenge)
 * GET /api/v1/login/oauth2/code/my-oidc?code&amp;state
 *   -> the platform exchanges the code at the stub token endpoint (code_verifier,
 *      client authentication) and fetches user info from the stub
 *   -> 302 to /api/v1/oidc/success
 * GET /api/v1/oidc/success
 *   -> the decision tree: linked user receives tokens, new user is routed to
 *      the first-login page, unlinked account is routed to linking, and an
 *      account linked to a different identity is routed to the provider
 *      logout returning to the portal login page with an error code for the
 *      SPA to present
 * </pre>
 *
 * <p>These tests cover the framework behaviour that the platform only
 * <em>configures</em>: the shape of the authorization request (including the
 * S256 code challenge), the state parameter handling, the token exchange with
 * PKCE and client authentication, and the validation of the ID token returned
 * by the provider.</p>
 *
 * <p>The provider logout redirect ({@code GET /api/v1/oidc/logout/{token}}),
 * the counterpart of the login flow, is covered here as well. A broken dance
 * (state mismatch, invalid ID token signature) is routed back to the portal
 * login page with the {@code error=oidc-failure} code.</p>
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional(value = TxType.REQUIRES_NEW)
class OidcLoginFlowIntTest extends AbstractOidcFlowIntTest {

    private static final String AUTHORIZATION_TRIGGER_URL = "/api/v1/oauth2/authorization/my-oidc";

    private static final String CALLBACK_URL = "/api/v1/login/oauth2/code/my-oidc";

    private static final String OIDC_SUCCESS_URL = "/api/v1/oidc/success";

    private static final String AUTHORIZATION_CODE = "stub-authorization-code";

    private static final String PORTAL_ADDRESS = "http://localhost:4200";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        createMVC();
        STUB_IDP.reset();
    }

    @Test
    void shouldBuildAuthorizationRequestWithPkceAndCorrectClient() throws Exception {
        MockHttpSession session = new MockHttpSession();

        String redirectUrl = beginAuthorizationRequest(session);
        Map<String, String> parameters = queryParameters(redirectUrl);

        assertEquals("code", parameters.get("response_type"));
        assertEquals(OidcStubIdentityProvider.CLIENT_ID, parameters.get("client_id"));
        assertEquals("http://localhost/api/v1/login/oauth2/code/my-oidc", parameters.get("redirect_uri"));
        assertTrue(parameters.get("scope").contains("openid"), "openid scope is required for the OIDC flow");
        assertNotNull(parameters.get("state"), "the authorization request must carry the state parameter");
        assertNotNull(parameters.get("nonce"), "the authorization request must carry the nonce parameter");
        assertNotNull(parameters.get("code_challenge"), "PKCE must be applied to the authorization request");
        assertEquals("S256", parameters.get("code_challenge_method"), "PKCE must use the S256 method");
    }

    @Test
    void shouldCompleteAuthorizationCodeLoginForLinkedUser() throws Exception {
        String sub = "oidc-flow-returning-sub";
        String email = "oidc-flow-returning@example.com";
        seedUser("oidc-flow-returning", email, "FlowS3cret!", sub, true, true);

        MockHttpSession session = new MockHttpSession();
        Map<String, String> authorizationParameters =
                queryParameters(beginAuthorizationRequest(session));
        prepareStubForCallback(authorizationParameters, sub, email);

        mvc.perform(get(CALLBACK_URL)
                        .queryParam("code", AUTHORIZATION_CODE)
                        .queryParam("state", authorizationParameters.get("state"))
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(OIDC_SUCCESS_URL));

        assertTokenExchangeUsedPkceAndClientAuthentication();
        assertUserInfoWasFetched();

        MvcResult result = mvc.perform(get(OIDC_SUCCESS_URL).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login-success?token=**"))
                .andReturn();

        String redirectUrl = result.getResponse().getRedirectedUrl();
        assertTrue(redirectUrl.contains("refresh-token="), "the redirect must carry the refresh token");
        assertTrue(redirectUrl.contains("oidc-token="), "the redirect must carry the ID token");
        String payload = jwtPayload(queryParameters(redirectUrl).get("token"));
        assertTrue(payload.contains("oidc-flow-returning"), "Issued token should address the local account, payload was: " + payload);
        assertFalse(payload.contains("ROLE_NOT_ACCEPTED"), "Approvals were accepted, so no pending-approval role is expected");
        assertTrue(session.isInvalid(), "the OAuth2 session must be terminated once tokens are issued");
    }

    @Test
    void shouldRouteNewUserToFirstLoginAndCompleteRegistration() throws Exception {
        String sub = "oidc-flow-new-sub";
        String email = "oidc-flow-new@example.com";

        MockHttpSession session = new MockHttpSession();
        Map<String, String> authorizationParameters =
                queryParameters(beginAuthorizationRequest(session));
        prepareStubForCallback(authorizationParameters, sub, email);

        mvc.perform(get(CALLBACK_URL)
                        .queryParam("code", AUTHORIZATION_CODE)
                        .queryParam("state", authorizationParameters.get("state"))
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(OIDC_SUCCESS_URL));

        MvcResult success = mvc.perform(get(OIDC_SUCCESS_URL).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/sso-first-login?oidc-token=**"))
                .andReturn();
        assertTrue(session.isInvalid(), "the OAuth2 session must be terminated after routing to the first-login page");

        // the SPA would now complete the registration with the ID token from the redirect
        String oidcToken = queryParameters(success.getResponse().getRedirectedUrl()).get("oidc-token");
        MvcResult approvals = mvc.perform(post("/api/v1/oidc/approvals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OidcApprovals(oidcToken, "oidc-flow-new", true, true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        User registered = userRepository.findByEmail(email).orElseThrow();
        assertEquals(sub, registered.getSamlToken(), "the account must be linked with the validated token subject");
        assertTrue(registered.isEnabled());
        String payload = jwtPayload(
                objectMapper.readTree(approvals.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("token").asText());
        assertTrue(payload.contains("oidc-flow-new"), "Issued token should address the new account, payload was: " + payload);
    }

    @Test
    void shouldRouteUnlinkedAccountToLinkingAndCompleteItWithPassword() throws Exception {
        String sub = "oidc-flow-link-sub";
        String email = "oidc-flow-link@example.com";
        seedUser("oidc-flow-link", email, "FlowS3cret!", null, true, true);

        MockHttpSession session = new MockHttpSession();
        Map<String, String> authorizationParameters =
                queryParameters(beginAuthorizationRequest(session));
        prepareStubForCallback(authorizationParameters, sub, email);

        mvc.perform(get(CALLBACK_URL)
                        .queryParam("code", AUTHORIZATION_CODE)
                        .queryParam("state", authorizationParameters.get("state"))
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(OIDC_SUCCESS_URL));

        MvcResult success = mvc.perform(get(OIDC_SUCCESS_URL).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login-linking?oidc-token=**"))
                .andReturn();
        assertTrue(session.isInvalid(), "the OAuth2 session must be terminated after routing to the linking page");

        // the SPA would now confirm ownership of the local account with its password
        String oidcToken = queryParameters(success.getResponse().getRedirectedUrl()).get("oidc-token");
        mvc.perform(post("/api/v1/oidc/link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OidcLogin(email, "FlowS3cret!", oidcToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        assertEquals(sub, userRepository.findByEmail(email).orElseThrow().getSamlToken(),
                "the account must be linked with the validated token subject after the password proof");
    }

    @Test
    void shouldNotAuthenticateWhenIdTokenSignatureIsInvalid() throws Exception {
        String sub = "oidc-flow-forged-sub";
        String email = "oidc-flow-forged@example.com";

        MockHttpSession session = new MockHttpSession();
        Map<String, String> authorizationParameters =
                queryParameters(beginAuthorizationRequest(session));
        STUB_IDP.setNextIdToken(STUB_IDP.mintIdTokenWithUnknownSigningKey(sub, email, authorizationParameters.get("nonce")));
        STUB_IDP.setUserInfo(sub, email, sub + "-user", "Given", "Family");

        mvc.perform(get(CALLBACK_URL)
                        .queryParam("code", AUTHORIZATION_CODE)
                        .queryParam("state", authorizationParameters.get("state"))
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(PORTAL_ADDRESS + "/login?error=oidc-failure"));

        // the code was exchanged, but the ID token failed signature verification:
        // no authenticated session may have been established
        assertEquals(1, STUB_IDP.tokenRequests().size(), "the code exchange must have happened before the failure");
        mvc.perform(get(OIDC_SUCCESS_URL).session(session))
                .andExpect(status().isUnauthorized());
    }

    /**
     * A callback with a mismatched state parameter must not authenticate the
     * request; the user is routed back to the portal login page with the
     * generic OIDC failure code.
     */
    @Test
    void shouldRouteBrokenDanceBackToThePortalLoginPage() throws Exception {
        MockHttpSession session = new MockHttpSession();
        queryParameters(beginAuthorizationRequest(session));

        mvc.perform(get(CALLBACK_URL)
                        .queryParam("code", AUTHORIZATION_CODE)
                        .queryParam("state", "tampered-state-value")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(PORTAL_ADDRESS + "/login?error=oidc-failure"));

        mvc.perform(get(OIDC_SUCCESS_URL).session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRouteConflictingAccountToProviderLogout() throws Exception {
        String sub = "oidc-flow-conflict-sub";
        String email = "oidc-flow-conflict@example.com";
        seedUser("oidc-flow-conflict", email, "FlowS3cret!", "some-other-identity", true, true);

        MockHttpSession session = new MockHttpSession();
        Map<String, String> authorizationParameters =
                queryParameters(beginAuthorizationRequest(session));
        String idToken = STUB_IDP.mintValidIdToken(sub, email, authorizationParameters.get("nonce"));
        STUB_IDP.setNextIdToken(idToken);
        STUB_IDP.setUserInfo(sub, email, sub + "-user", "Given", "Family");

        mvc.perform(get(CALLBACK_URL)
                        .queryParam("code", AUTHORIZATION_CODE)
                        .queryParam("state", authorizationParameters.get("state"))
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(OIDC_SUCCESS_URL));

        mvc.perform(get(OIDC_SUCCESS_URL).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(STUB_IDP.issuer() + "/protocol/openid-connect/logout"
                        + "?id_token_hint=" + idToken
                        + "&post_logout_redirect_uri=" + PORTAL_ADDRESS + "/login?error=oidc-account-conflict"));
        assertTrue(session.isInvalid(), "the OAuth2 session must be terminated after the rejected login");
    }

    @Test
    void shouldRedirectProviderLogoutBackToThePortalLoginPage() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/oidc/logout/stub-id-token-value"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(STUB_IDP.issuer() + "/protocol/openid-connect/logout"
                        + "?id_token_hint=stub-id-token-value"
                        + "&post_logout_redirect_uri=" + PORTAL_ADDRESS + "/login"))
                .andReturn();
        assertFalse(result.getResponse().getRedirectedUrl().contains("error="),
                "a plain logout must not carry an error signal");
    }

    /**
     * Signal for the SPA guard: a linked user with pending AUP/Privacy Notice
     * approvals receives a JWT whose {@code global_role} claim carries
     * {@code ROLE_NOT_ACCEPTED}; the portal must route such a user to the
     * terms-acceptance screen instead of the application.
     */
    @Test
    void shouldIssueNotAcceptedRoleWhenLinkedUserHasPendingApprovals() throws Exception {
        String sub = "oidc-flow-pending-sub";
        String email = "oidc-flow-pending@example.com";
        seedUser("oidc-flow-pending", email, "FlowS3cret!", sub, false, false);

        MockHttpSession session = new MockHttpSession();
        Map<String, String> authorizationParameters =
                queryParameters(beginAuthorizationRequest(session));
        prepareStubForCallback(authorizationParameters, sub, email);

        mvc.perform(get(CALLBACK_URL)
                        .queryParam("code", AUTHORIZATION_CODE)
                        .queryParam("state", authorizationParameters.get("state"))
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(OIDC_SUCCESS_URL));

        MvcResult result = mvc.perform(get(OIDC_SUCCESS_URL).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login-success?token=**"))
                .andReturn();

        String payload = jwtPayload(queryParameters(result.getResponse().getRedirectedUrl()).get("token"));
        assertTrue(payload.contains("ROLE_NOT_ACCEPTED"),
                "Token issued for a linked user with pending approvals must carry ROLE_NOT_ACCEPTED, payload was: " + payload);
        assertTrue(session.isInvalid(), "the OAuth2 session must be terminated once tokens are issued");
    }

    /**
     * Starts the authorization code flow and returns the redirect URL to the
     * provider authorization endpoint.
     */
    private String beginAuthorizationRequest(MockHttpSession session) throws Exception {
        MvcResult result = mvc.perform(get(AUTHORIZATION_TRIGGER_URL).session(session))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        String redirectUrl = result.getResponse().getRedirectedUrl();
        assertNotNull(redirectUrl, "the authorization trigger must redirect to the provider");
        assertTrue(redirectUrl.startsWith(STUB_IDP.issuer() + "/protocol/openid-connect/auth"),
                "the redirect must point at the configured provider authorization endpoint, was: " + redirectUrl);
        return redirectUrl;
    }

    /**
     * Configures the stub responses for the code exchange: the ID token echoes
     * the nonce of the authorization request, and user info carries the same
     * identity.
     */
    private void prepareStubForCallback(Map<String, String> authorizationParameters, String sub, String email) {
        STUB_IDP.setNextIdToken(STUB_IDP.mintValidIdToken(sub, email, authorizationParameters.get("nonce")));
        STUB_IDP.setUserInfo(sub, email, sub + "-user", "Given", "Family");
    }

    private void assertTokenExchangeUsedPkceAndClientAuthentication() {
        OidcStubIdentityProvider.RecordedRequest tokenRequest = STUB_IDP.lastTokenRequest();
        String expectedAuthorization = "Basic " + Base64.getEncoder().encodeToString(
                (OidcStubIdentityProvider.CLIENT_ID + ":" + OidcStubIdentityProvider.CLIENT_SECRET).getBytes(StandardCharsets.UTF_8));
        assertEquals(expectedAuthorization, tokenRequest.authorizationHeader(),
                "the confidential client must authenticate at the token endpoint");
        assertTrue(tokenRequest.body().contains("grant_type=authorization_code"));
        assertTrue(tokenRequest.body().contains("code=" + AUTHORIZATION_CODE));
        assertTrue(tokenRequest.body().contains("code_verifier="),
                "the PKCE code verifier must be sent to the token endpoint");
        assertTrue(tokenRequest.body().contains("redirect_uri="));
    }

    private void assertUserInfoWasFetched() {
        assertEquals(1, STUB_IDP.userInfoRequests().size(),
                "user info must be fetched during the OIDC login");
        assertEquals("Bearer stub-access-token", STUB_IDP.userInfoRequests().getFirst().authorizationHeader());
    }

}
