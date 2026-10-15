package net.geant.nmaas.portal.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import net.geant.nmaas.portal.api.configuration.model.ConfigurationDto;
import net.geant.nmaas.portal.api.i18n.api.I18nDto;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.repositories.InternationalizationSimpleRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests of the OIDC account linking ({@code POST /oidc/link}) and
 * first-login registration ({@code POST /oidc/approvals}) endpoints.
 *
 * <p>Unlike the unit tests, every request here goes through the complete
 * server-side chain: the Spring Security filter chain, the real
 * {@code OidcIdTokenValidatorImpl} (RS256 signature against the stub provider
 * JWKS, issuer, audience and expiry checks), the user services with a real
 * database, and the portal JWT issued in the response. The identity provider
 * is {@link OidcStubIdentityProvider}, so tokens can be minted for arbitrary
 * users and also deliberately broken (unknown signing key, wrong issuer,
 * wrong audience, expired) to verify the critical paths are really blocked.</p>
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional(value = TxType.REQUIRES_NEW)
class OidcLinkAndApprovalsIntTest extends AbstractOidcFlowIntTest {

    private static final String LINK_URL = "/api/v1/oidc/link";

    private static final String APPROVALS_URL = "/api/v1/oidc/approvals";

    @Autowired
    private ConfigurationManager configurationManager;

    @Autowired
    private InternationalizationSimpleRepository internationalizationRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        createMVC();
        STUB_IDP.reset();
        if (internationalizationRepository.findAll().stream().noneMatch(lang -> lang.getLanguage().equalsIgnoreCase("en"))) {
            internationalizationRepository.save(new I18nDto("en", true, "{\"content\":\"content\"}").getAsInternationalizationSimple());
        }
    }

    @AfterEach
    void restoreConfiguration() {
        ConfigurationDto configuration = configurationManager.getConfiguration();
        configuration.setMaintenance(false);
        configurationManager.updateConfiguration(configuration.getId(), configuration);
    }

    @Test
    void shouldLinkExistingAccountWithValidTokenAndPassword() throws Exception {
        seedUser("oidc-link-happy", "oidc-link-happy@example.com", "LinkS3cret!", null, true, true);
        String idToken = STUB_IDP.mintValidIdToken("oidc-link-happy-sub", "oidc-link-happy@example.com");

        MvcResult result = mvc.perform(post(LINK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OidcLogin("oidc-link-happy@example.com", "LinkS3cret!", idToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.oidcToken").value(idToken))
                .andReturn();

        User linked = userRepository.findByEmail("oidc-link-happy@example.com").orElseThrow();
        assertEquals("oidc-link-happy-sub", linked.getSamlToken(), "Account should be linked with the token subject");
        String payload = jwtPayload(tokenFrom(result));
        assertTrue(payload.contains("oidc-link-happy"), "Issued token should address the linked account, payload was: " + payload);
        assertFalse(payload.contains("ROLE_NOT_ACCEPTED"), "Token of a user with accepted approvals must not carry ROLE_NOT_ACCEPTED");
    }

    @Test
    void shouldRejectLinkingWithWrongPassword() throws Exception {
        seedUser("oidc-link-badpass", "oidc-link-badpass@example.com", "LinkS3cret!", null, true, true);
        String idToken = STUB_IDP.mintValidIdToken("oidc-link-badpass-sub", "oidc-link-badpass@example.com");

        mvc.perform(post(LINK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OidcLogin("oidc-link-badpass@example.com", "WrongPassword!", idToken))))
                .andExpect(status().isUnauthorized());

        assertNull(userRepository.findByEmail("oidc-link-badpass@example.com").orElseThrow().getSamlToken(),
                "Account must stay unlinked when the password proof fails");
    }

    @Test
    void shouldRejectLinkingWhenPayloadEmailDoesNotMatchTokenClaim() throws Exception {
        seedUser("oidc-link-email", "oidc-link-email@example.com", "LinkS3cret!", null, true, true);
        String idToken = STUB_IDP.mintValidIdToken("oidc-link-email-sub", "oidc-link-email@example.com");

        mvc.perform(post(LINK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OidcLogin("somebody-else@example.com", "LinkS3cret!", idToken))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectLinkingWithForgedIdToken() throws Exception {
        seedUser("oidc-link-forged", "oidc-link-forged@example.com", "LinkS3cret!", null, true, true);
        String idToken = STUB_IDP.mintIdTokenWithUnknownSigningKey("oidc-link-forged-sub", "oidc-link-forged@example.com", null);

        mvc.perform(post(LINK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OidcLogin("oidc-link-forged@example.com", "LinkS3cret!", idToken))))
                .andExpect(status().isUnauthorized());

        assertNull(userRepository.findByEmail("oidc-link-forged@example.com").orElseThrow().getSamlToken());
    }

    @Test
    void shouldRejectLinkingWithExpiredIdToken() throws Exception {
        seedUser("oidc-link-expired", "oidc-link-expired@example.com", "LinkS3cret!", null, true, true);
        String idToken = STUB_IDP.mintExpiredIdToken("oidc-link-expired-sub", "oidc-link-expired@example.com");

        mvc.perform(post(LINK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OidcLogin("oidc-link-expired@example.com", "LinkS3cret!", idToken))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectLinkingWithIdTokenFromDifferentIssuer() throws Exception {
        seedUser("oidc-link-issuer", "oidc-link-issuer@example.com", "LinkS3cret!", null, true, true);
        String idToken = STUB_IDP.mintIdTokenWithDifferentIssuer("oidc-link-issuer-sub", "oidc-link-issuer@example.com");

        mvc.perform(post(LINK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OidcLogin("oidc-link-issuer@example.com", "LinkS3cret!", idToken))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectLinkingWithIdTokenForDifferentAudience() throws Exception {
        seedUser("oidc-link-audience", "oidc-link-audience@example.com", "LinkS3cret!", null, true, true);
        String idToken = STUB_IDP.mintIdTokenWithDifferentAudience("oidc-link-audience-sub", "oidc-link-audience@example.com");

        mvc.perform(post(LINK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OidcLogin("oidc-link-audience@example.com", "LinkS3cret!", idToken))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectLinkingWhenSubIsAlreadyLinkedToAnotherAccount() throws Exception {
        seedUser("oidc-link-target", "oidc-link-target@example.com", "LinkS3cret!", null, true, true);
        seedUser("oidc-link-owner", "oidc-link-owner@example.com", "LinkS3cret!", "oidc-shared-sub", true, true);
        String idToken = STUB_IDP.mintValidIdToken("oidc-shared-sub", "oidc-link-target@example.com");

        mvc.perform(post(LINK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OidcLogin("oidc-link-target@example.com", "LinkS3cret!", idToken))))
                .andExpect(status().isConflict());

        assertNull(userRepository.findByEmail("oidc-link-target@example.com").orElseThrow().getSamlToken(),
                "One external identity must stay linked to a single account");
    }

    @Test
    void shouldRejectLinkingWhenAccountAlreadyLinkedToDifferentIdentity() throws Exception {
        seedUser("oidc-link-occupied", "oidc-link-occupied@example.com", "LinkS3cret!", "oidc-other-sub", true, true);
        String idToken = STUB_IDP.mintValidIdToken("oidc-new-sub", "oidc-link-occupied@example.com");

        mvc.perform(post(LINK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OidcLogin("oidc-link-occupied@example.com", "LinkS3cret!", idToken))))
                .andExpect(status().isConflict());

        assertEquals("oidc-other-sub", userRepository.findByEmail("oidc-link-occupied@example.com").orElseThrow().getSamlToken());
    }

    @Test
    void shouldAddNotAcceptedRoleToIssuedTokenWhenApprovalsPending() throws Exception {
        seedUser("oidc-link-pending", "oidc-link-pending@example.com", "LinkS3cret!", null, false, false);
        String idToken = STUB_IDP.mintValidIdToken("oidc-link-pending-sub", "oidc-link-pending@example.com");

        MvcResult result = mvc.perform(post(LINK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OidcLogin("oidc-link-pending@example.com", "LinkS3cret!", idToken))))
                .andExpect(status().isOk())
                .andReturn();

        String payload = jwtPayload(tokenFrom(result));
        assertTrue(payload.contains("ROLE_NOT_ACCEPTED"),
                "Token issued for a user with pending approvals must carry ROLE_NOT_ACCEPTED, payload was: " + payload);
    }

    @Test
    void shouldRejectLinkingDuringMaintenance() throws Exception {
        seedUser("oidc-link-maintenance", "oidc-link-maintenance@example.com", "LinkS3cret!", null, true, true);
        setMaintenance(true);
        String idToken = STUB_IDP.mintValidIdToken("oidc-link-maintenance-sub", "oidc-link-maintenance@example.com");

        mvc.perform(post(LINK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OidcLogin("oidc-link-maintenance@example.com", "LinkS3cret!", idToken))))
                .andExpect(status().isNotAcceptable());

        assertNull(userRepository.findByEmail("oidc-link-maintenance@example.com").orElseThrow().getSamlToken(),
                "Account must stay unlinked when the platform is in maintenance mode");
    }

    @Test
    void shouldRegisterNewUserAfterExplicitConsent() throws Exception {
        String email = "oidc-reg-happy@example.com";
        String idToken = STUB_IDP.mintValidIdToken("oidc-reg-happy-sub", email);

        MvcResult result = mvc.perform(post(APPROVALS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OidcApprovals(idToken, "oidc-reg-happy", true, true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.oidcToken").value(idToken))
                .andReturn();

        User registered = userRepository.findByEmail(email).orElseThrow();
        assertTrue(registered.isEnabled(), "OIDC accounts are created enabled");
        assertEquals("oidc-reg-happy-sub", registered.getSamlToken());
        assertTrue(registered.isTermsOfUseAccepted());
        assertTrue(registered.isPrivacyPolicyAccepted());
        assertTrue(registered.getRoles().stream().anyMatch(role -> role.getRole().equals(Role.ROLE_GUEST)),
                "New OIDC accounts start as guests in the global domain");
        assertEquals("Given", registered.getFirstname(), "First name should come from the ID token claims");
        assertEquals("Family", registered.getLastname(), "Last name should come from the ID token claims");

        String payload = jwtPayload(tokenFrom(result));
        assertTrue(payload.contains("oidc-reg-happy"), "Issued token should address the new account, payload was: " + payload);
        assertFalse(payload.contains("ROLE_NOT_ACCEPTED"));
    }

    @Test
    void shouldRejectRegistrationWithoutAupConsent() throws Exception {
        String idToken = STUB_IDP.mintValidIdToken("oidc-reg-noaup-sub", "oidc-reg-noaup@example.com");

        mvc.perform(post(APPROVALS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OidcApprovals(idToken, "oidc-reg-noaup", false, true))))
                .andExpect(status().isConflict());

        assertFalse(userRepository.existsByEmail("oidc-reg-noaup@example.com"),
                "No account may be created without AUP consent");
    }

    @Test
    void shouldRejectRegistrationWithoutPnConsent() throws Exception {
        String idToken = STUB_IDP.mintValidIdToken("oidc-reg-nopn-sub", "oidc-reg-nopn@example.com");

        mvc.perform(post(APPROVALS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OidcApprovals(idToken, "oidc-reg-nopn", true, false))))
                .andExpect(status().isConflict());

        assertFalse(userRepository.existsByEmail("oidc-reg-nopn@example.com"),
                "No account may be created without Privacy Notice consent");
    }

    @Test
    void shouldRejectRegistrationWhenConsentFlagsAreMissing() throws Exception {
        String idToken = STUB_IDP.mintValidIdToken("oidc-reg-noflags-sub", "oidc-reg-noflags@example.com");

        // the payload is incomplete, so it is rejected during deserialization -
        // consent can never be implied by a missing flag
        mvc.perform(post(APPROVALS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oidcToken\":\"" + idToken + "\",\"username\":\"oidc-reg-noflags\"}"))
                .andExpect(status().isBadRequest());

        assertFalse(userRepository.existsByEmail("oidc-reg-noflags@example.com"),
                "Missing consent flags mean no consent");
    }

    @Test
    void shouldRejectRegistrationWhenEmailAlreadyExists() throws Exception {
        seedUser("oidc-reg-dupemail", "oidc-reg-dupemail@example.com", "LinkS3cret!", null, true, true);
        String idToken = STUB_IDP.mintValidIdToken("oidc-reg-dupemail-sub", "oidc-reg-dupemail@example.com");

        mvc.perform(post(APPROVALS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OidcApprovals(idToken, "oidc-reg-dupemail-new", true, true))))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectRegistrationWhenUsernameAlreadyExists() throws Exception {
        seedUser("oidc-reg-dupuser", "oidc-reg-dupuser@example.com", "LinkS3cret!", null, true, true);
        String idToken = STUB_IDP.mintValidIdToken("oidc-reg-dupuser-sub", "oidc-reg-dupuser-other@example.com");

        mvc.perform(post(APPROVALS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OidcApprovals(idToken, "oidc-reg-dupuser", true, true))))
                .andExpect(status().isConflict());

        assertFalse(userRepository.existsByEmail("oidc-reg-dupuser-other@example.com"));
    }

    @Test
    void shouldRejectRegistrationWhenSubAlreadyLinked() throws Exception {
        seedUser("oidc-reg-subowner", "oidc-reg-subowner@example.com", "LinkS3cret!", "oidc-taken-sub", true, true);
        String idToken = STUB_IDP.mintValidIdToken("oidc-taken-sub", "oidc-reg-subowner-new@example.com");

        mvc.perform(post(APPROVALS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OidcApprovals(idToken, "oidc-reg-subowner-new", true, true))))
                .andExpect(status().isConflict());

        assertFalse(userRepository.existsByEmail("oidc-reg-subowner-new@example.com"),
                "An external identity must never be linked to two accounts");
    }

    @Test
    void shouldRejectRegistrationWithForgedIdToken() throws Exception {
        String idToken = STUB_IDP.mintIdTokenWithUnknownSigningKey("oidc-reg-forged-sub", "oidc-reg-forged@example.com", null);

        mvc.perform(post(APPROVALS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OidcApprovals(idToken, "oidc-reg-forged", true, true))))
                .andExpect(status().isUnauthorized());

        assertFalse(userRepository.existsByEmail("oidc-reg-forged@example.com"),
                "No account may be created from a token that failed validation");
    }

    @Test
    void shouldRejectRegistrationDuringMaintenance() throws Exception {
        setMaintenance(true);
        String idToken = STUB_IDP.mintValidIdToken("oidc-reg-maintenance-sub", "oidc-reg-maintenance@example.com");

        // note: the registration itself happens before the maintenance check today,
        // so this asserts only that non-admin logins are rejected during maintenance
        mvc.perform(post(APPROVALS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OidcApprovals(idToken, "oidc-reg-maintenance", true, true))))
                .andExpect(status().isNotAcceptable());
    }

    private String tokenFrom(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("token").asText();
    }

    private void setMaintenance(boolean maintenance) {
        ConfigurationDto configuration = configurationManager.getConfiguration();
        configuration.setMaintenance(maintenance);
        configurationManager.updateConfiguration(configuration.getId(), configuration);
    }

}
