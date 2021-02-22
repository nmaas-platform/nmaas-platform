package net.geant.nmaas.portal.api.security;

import net.geant.nmaas.portal.api.auth.SSOView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SSOConfigManagerTest {

    private SSOConfigManager configManager;

    private static final String LOGIN_URL = "https://shibbolethsp.pllab.internal/cgi-bin/nmaas.php";

    private static final String LOGOUT_URL = "https://shibbolethsp.pllab.internal/Shibboleth.sso/Logout";

    private static final Integer TIMEOUT = 15;

    private static final String KEY = "sso_shared_secret_key";

    @BeforeEach
    public void setup(){
        configManager = new SSOConfigManager();
        ReflectionTestUtils.setField(configManager, "loginUrl", LOGIN_URL);
        ReflectionTestUtils.setField(configManager, "logoutUrl", LOGOUT_URL);
        ReflectionTestUtils.setField(configManager, "timeout", TIMEOUT);
        ReflectionTestUtils.setField(configManager, "key", KEY);
    }

    @Test
    public void shouldCheckParamAndNotThrowAnException(){
        configManager.checkParam();
    }

    @Test
    public void shouldThrowExceptionWhenLoginUrlIsEmpty(){
        assertThrows(IllegalStateException.class, () -> {
            ReflectionTestUtils.setField(configManager, "loginUrl", "");
            configManager.checkParam();
        });
    }

    @Test
    public void shouldThrowExceptionWhenLoginUrlIsNull(){
        assertThrows(IllegalStateException.class, () -> {
            ReflectionTestUtils.setField(configManager, "loginUrl", null);
            configManager.checkParam();
        });
    }

    @Test
    public void shouldThrowExceptionWhenLogoutUrlIsNull(){
        assertThrows(IllegalStateException.class, () -> {
            ReflectionTestUtils.setField(configManager, "logoutUrl", null);
            configManager.checkParam();
        });
    }

    @Test
    public void shouldThrowExceptionWhenLogoutUrlIsEmpty(){
        assertThrows(IllegalStateException.class, () -> {
            ReflectionTestUtils.setField(configManager, "logoutUrl", "");
            configManager.checkParam();
        });
    }

    @Test
    public void shouldThrowExceptionWhenKeyIsNull(){
        assertThrows(IllegalStateException.class, () -> {
            ReflectionTestUtils.setField(configManager, "key", null);
            configManager.checkParam();
        });
    }

    @Test
    public void shouldThrowExceptionWhenKeyIsEmpty(){
        assertThrows(IllegalStateException.class, () -> {
            ReflectionTestUtils.setField(configManager, "key", "");
            configManager.checkParam();
        });
    }

    @Test
    public void shouldThrowExceptionWhenTimeoutIsLessThanZero(){
        assertThrows(IllegalStateException.class, () -> {
            ReflectionTestUtils.setField(configManager, "timeout", -123);
            configManager.checkParam();
        });
    }

    @Test
    public void shouldReturnSSOViewInstance(){
        SSOView result = configManager.getSSOView();
        assertThat("Login url mismatch", result.getLoginUrl().equals(configManager.getLoginUrl()));
        assertThat("Logout url mismatch", result.getLogoutUrl().equals(configManager.getLogoutUrl()));
    }
}
