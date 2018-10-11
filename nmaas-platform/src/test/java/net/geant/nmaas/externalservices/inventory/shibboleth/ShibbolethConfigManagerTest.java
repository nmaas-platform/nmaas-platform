package net.geant.nmaas.externalservices.inventory.shibboleth;

import net.geant.nmaas.externalservices.inventory.shibboleth.model.ShibbolethView;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class ShibbolethConfigManagerTest {

    private ShibbolethConfigManager configManager;

    private static final String LOGIN_URL = "https://shibbolethsp.pllab.internal/cgi-bin/nmaas.php";

    private static final String LOGOUT_URL = "https://shibbolethsp.pllab.internal/Shibboleth.sso/Logout";

    private static final Integer TIMEOUT = 15;

    private static final String KEY = "jovana_shared_secret_key";

    @Before
    public void setup(){
        configManager = new ShibbolethConfigManager();
        ReflectionTestUtils.setField(configManager, "loginUrl", LOGIN_URL);
        ReflectionTestUtils.setField(configManager, "logoutUrl", LOGOUT_URL);
        ReflectionTestUtils.setField(configManager, "timeout", TIMEOUT);
        ReflectionTestUtils.setField(configManager, "key", KEY);
    }

    @Test
    public void shouldCheckParamAndNotThrowAnException(){
        configManager.checkParam();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenLoginUrlIsEmpty(){
        ReflectionTestUtils.setField(configManager, "loginUrl", "");
        configManager.checkParam();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenLoginUrlIsNull(){
        ReflectionTestUtils.setField(configManager, "loginUrl", null);
        configManager.checkParam();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenLogoutUrlIsNull(){
        ReflectionTestUtils.setField(configManager, "logoutUrl", null);
        configManager.checkParam();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenLogoutUrlIsEmpty(){
        ReflectionTestUtils.setField(configManager, "logoutUrl", "");
        configManager.checkParam();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenKeyIsNull(){
        ReflectionTestUtils.setField(configManager, "key", null);
        configManager.checkParam();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenKeyIsEmpty(){
        ReflectionTestUtils.setField(configManager, "key", "");
        configManager.checkParam();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenTimeoutIsLessThanZero(){
        ReflectionTestUtils.setField(configManager, "timeout", -123);
        configManager.checkParam();
    }

    @Test
    public void shouldReturnShibbolethViewInstance(){
        ShibbolethView result = configManager.getShibbolethView();
        assertThat("Login url mismatch", result.getLoginUrl().equals(configManager.getLoginUrl()));
        assertThat("Logout url mismatch", result.getLogoutUrl().equals(configManager.getLogoutUrl()));
    }
}
