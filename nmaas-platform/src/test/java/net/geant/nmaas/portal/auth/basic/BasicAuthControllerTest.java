package net.geant.nmaas.portal.auth.basic;

import net.geant.nmaas.portal.api.exception.AuthenticationException;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class BasicAuthControllerTest {
    @InjectMocks
    private BasicAuthController basicAuthController;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Before
    public void setup(){
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
    }

    @Test
    public void testValidateWithValidUserNameAndPassword() throws AuthenticationException {
        basicAuthController.validate(Optional.of("TEST"), Optional.of("TEST"), "TEST", true, true, true);
    }

    @Test(expected = AuthenticationException.class)
    public void testValidateWithInvalidUserNameAndValidPassword() throws AuthenticationException {
        basicAuthController.validate(Optional.empty(), Optional.of("TEST"), "TEST",true, true, true);
    }

    @Test(expected = AuthenticationException.class)
    public void testValidateWithValidUserNameAndInvalidPassword() throws AuthenticationException {
        basicAuthController.validate(Optional.of("TEST"), Optional.empty(), "TEST",true, true, true);
    }

    @Test(expected = AuthenticationException.class)
    public void testValidateWithInvalidUserNameAndInvalidPassword() throws AuthenticationException {
        basicAuthController.validate(Optional.empty(), Optional.empty(), "TEST",true, true, true);
    }

    @Test(expected = AuthenticationException.class)
    public void testValidateWithValidUserNameAndValidPasswordAndUserNotEnabled() throws AuthenticationException {
        basicAuthController.validate(Optional.of("TEST"), Optional.of("TEST"), "TEST",false, true, true);
    }
}
