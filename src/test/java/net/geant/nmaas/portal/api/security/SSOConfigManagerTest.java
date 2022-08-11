package net.geant.nmaas.portal.api.security;

import net.geant.nmaas.portal.api.auth.SSOView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SSOConfigManagerTest {

    private static final String LOGIN_URL = "https://shibbolethsp.pllab.internal/cgi-bin/nmaas.php";
    private static final String LOGOUT_URL = "https://shibbolethsp.pllab.internal/Shibboleth.sso/Logout";
    private static final Integer TIMEOUT = 15;
    private static final String KEY = "sso_shared_secret_key";

    private SSOConfigManager configManager;

    @BeforeEach
    void setup(){
        configManager = new SSOConfigManager();
        ReflectionTestUtils.setField(configManager, "loginUrl", LOGIN_URL);
        ReflectionTestUtils.setField(configManager, "logoutUrl", LOGOUT_URL);
        ReflectionTestUtils.setField(configManager, "timeout", TIMEOUT);
        ReflectionTestUtils.setField(configManager, "key", KEY);
    }

    @Test
    void shouldValidateConfigAndNotThrowAnException(){
        configManager.validateConfig();
    }

    @Test
    void shouldConfirmThatConfigIsValid(){
        assertTrue(configManager.isConfigValid());
    }

    @Test
    void shouldThrowExceptionWhenLoginUrlIsEmpty(){
        assertThrows(IllegalStateException.class, () -> {
            ReflectionTestUtils.setField(configManager, "loginUrl", "");
            configManager.validateConfig();
        });
    }

    @Test
    void shouldConfirmThatConfigIsNotValid() {
        ReflectionTestUtils.setField(configManager, "loginUrl", "");
        assertFalse(configManager.isConfigValid());
    }

    @Test
    void shouldThrowExceptionWhenLoginUrlIsNull() {
        assertThrows(IllegalStateException.class, () -> {
            ReflectionTestUtils.setField(configManager, "loginUrl", null);
            configManager.validateConfig();
        });
    }

    @Test
    void shouldThrowExceptionWhenLogoutUrlIsNull() {
        assertThrows(IllegalStateException.class, () -> {
            ReflectionTestUtils.setField(configManager, "logoutUrl", null);
            configManager.validateConfig();
        });
    }

    @Test
    void shouldThrowExceptionWhenLogoutUrlIsEmpty() {
        assertThrows(IllegalStateException.class, () -> {
            ReflectionTestUtils.setField(configManager, "logoutUrl", "");
            configManager.validateConfig();
        });
    }

    @Test
    void shouldThrowExceptionWhenKeyIsNull() {
        assertThrows(IllegalStateException.class, () -> {
            ReflectionTestUtils.setField(configManager, "key", null);
            configManager.validateConfig();
        });
    }

    @Test
    void shouldThrowExceptionWhenKeyIsEmpty() {
        assertThrows(IllegalStateException.class, () -> {
            ReflectionTestUtils.setField(configManager, "key", "");
            configManager.validateConfig();
        });
    }

    @Test
    void shouldThrowExceptionWhenTimeoutIsLessThanZero() {
        assertThrows(IllegalStateException.class, () -> {
            ReflectionTestUtils.setField(configManager, "timeout", -123);
            configManager.validateConfig();
        });
    }

    @Test
    void shouldReturnSSOViewInstance() {
        SSOView result = configManager.getSSOView();
        assertThat("Login url mismatch", result.getLoginUrl().equals(configManager.getLoginUrl()));
        assertThat("Logout url mismatch", result.getLogoutUrl().equals(configManager.getLogoutUrl()));
    }
}
