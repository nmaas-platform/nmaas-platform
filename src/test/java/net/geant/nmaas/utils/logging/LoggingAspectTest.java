package net.geant.nmaas.utils.logging;

import net.geant.nmaas.portal.api.auth.OidcApprovals;
import net.geant.nmaas.portal.api.auth.OidcLogin;
import net.geant.nmaas.portal.api.auth.UserOidcToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingAspectTest {

    private static final String JWT = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

    private LoggingAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new LoggingAspect();
    }

    @Test
    void shouldMaskPasswordLikeValues() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("email", "user@example.com");
        params.put("password", "s3cr3t-pass");

        String result = aspect.constructArgumentsString(params);

        assertThat(result)
                .doesNotContain("s3cr3t-pass")
                .contains("password=***")
                .contains("email=user@example.com");
    }

    @Test
    void shouldMaskTokenLikeValues() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("token", JWT);
        params.put("refreshToken", JWT);
        params.put("oidcToken", JWT);

        String result = aspect.constructArgumentsString(params);

        assertThat(result)
                .doesNotContain(JWT)
                .contains("token=***")
                .contains("refreshToken=***")
                .contains("oidcToken=***");
    }

    @Test
    void shouldMaskExternalIdLikeValues() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("externalId", "oidc-sub-123");
        params.put("external_id", "oidc-sub-456");
        params.put("userExternalId", "oidc-sub-789");
        params.put("sub", "oidc-sub-abc");
        params.put("email", "user@example.com");

        String result = aspect.constructArgumentsString(params);

        assertThat(result)
                .doesNotContain("oidc-sub")
                .contains("externalId=***")
                .contains("external_id=***")
                .contains("userExternalId=***")
                .contains("sub=***")
                .contains("email=user@example.com");
    }

    @Test
    void shouldKeepNonSensitiveValues() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("username", "admin");
        params.put("tokenCount", "5");
        params.put("maxTokens", "5");

        String result = aspect.constructArgumentsString(params);

        assertThat(result)
                .contains("username=admin")
                .contains("tokenCount=5")
                .contains("maxTokens=5");
    }

    @Test
    void shouldMaskRecordStyleRendering() {
        String rendering = "OidcLogin[email=user@example.com, password=hunter2, oidcToken=" + JWT + "]";

        String result = aspect.maskSensitiveValues(rendering);

        assertThat(result)
                .doesNotContain("hunter2")
                .doesNotContain(JWT)
                .contains("email=user@example.com")
                .contains("password=***")
                .contains("oidcToken=***");
    }

    @Test
    void shouldHandleMultipleAndNullArguments() {
        String result = aspect.constructArgumentsString("action=login", 42, null);

        assertThat(result)
                .contains("action=login")
                .contains("42")
                .contains("null");
    }

    @Test
    void authPayloadsShouldNotExposeSecretsInToString() {
        assertThat(new OidcLogin("user@example.com", "hunter2", JWT))
                .hasToString("OidcLogin[email=user@example.com, password=***, oidcToken=***]");
        assertThat(new OidcApprovals(JWT, "new-user", true, true).toString())
                .doesNotContain(JWT)
                .contains("new-user");
        assertThat(new UserOidcToken(JWT, JWT, JWT))
                .hasToString("UserOidcToken[token=***, refreshToken=***, oidcToken=***]");
    }
}
