package net.geant.nmaas.portal.api.auth;

import net.geant.nmaas.portal.api.exception.AuthenticationException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserLoginRegisterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BasicAuthControllerTest {

    private BasicAuthController basicAuthController;

    private DomainService domains = mock(DomainService.class);

    private PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    private UserLoginRegisterService userLoginService = mock(UserLoginRegisterService.class);

    @BeforeEach
    public void setup(){
        basicAuthController = new BasicAuthController(null, domains, passwordEncoder, null, null, userLoginService);
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
    }

    @Test
    public void testValidateWithValidUserNameAndPassword() throws AuthenticationException {
        basicAuthController.validate("TEST", "TEST", "TEST", true);
    }

    @Test
    public void testValidateWithInvalidUserNameAndValidPassword() throws AuthenticationException {
        assertThrows(AuthenticationException.class, () -> {
            basicAuthController.validate(null, "TEST", "TEST", true);
        });
    }

    @Test
    public void testValidateWithValidUserNameAndInvalidPassword() throws AuthenticationException {
        assertThrows(AuthenticationException.class, () -> {
            basicAuthController.validate("TEST", null, "TEST", true);
        });
    }

    @Test
    public void testValidateWithInvalidUserNameAndInvalidPassword() throws AuthenticationException {
        assertThrows(AuthenticationException.class, () -> {
            basicAuthController.validate(null, null, "TEST", true);
        });
    }

    @Test
    public void testValidateWithValidUserNameAndValidPasswordAndUserNotEnabled() throws AuthenticationException {
        assertThrows(AuthenticationException.class, () -> {
            basicAuthController.validate("TEST", "TEST", "TEST", false);
        });
    }

    @Test
    public void testValidateWithValidUserNameAndWrongPassword() throws AuthenticationException {
        assertThrows(AuthenticationException.class, () -> {
            when(passwordEncoder.matches(any(), any())).thenReturn(false);
            basicAuthController.validate("TEST", "TEST", "TEST", true);
        });
    }

    @Test
    public void shouldNotChangeUserRoles() {
        User user = new User("testUser");
        user.setPrivacyPolicyAccepted(true);
        user.setTermsOfUseAccepted(true);
        basicAuthController.checkUserApprovals(user);
        assertTrue(user.getRoles().stream().map(UserRole::getRole).noneMatch(r -> r.equals(Role.ROLE_NOT_ACCEPTED)));
    }

    @Test
    public void shouldAddIncompleteRoleToUser() {
        when(domains.getGlobalDomain()).thenReturn(Optional.of(new Domain("name", "codename")));
        User user = new User("testUser");
        user.setPrivacyPolicyAccepted(false);
        user.setTermsOfUseAccepted(true);
        basicAuthController.checkUserApprovals(user);
        assertTrue(user.getRoles().stream().map(UserRole::getRole).anyMatch(r -> r.equals(Role.ROLE_NOT_ACCEPTED)));
    }

}
