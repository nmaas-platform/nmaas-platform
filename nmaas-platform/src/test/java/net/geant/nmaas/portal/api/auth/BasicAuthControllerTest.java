package net.geant.nmaas.portal.api.auth;

import net.geant.nmaas.portal.api.exception.AuthenticationException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.service.DomainService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BasicAuthControllerTest {

    private BasicAuthController basicAuthController;

    private DomainService domains = mock(DomainService.class);

    private PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    @Before
    public void setup(){
        basicAuthController = new BasicAuthController(null, domains, passwordEncoder, null, null);
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
    }

    @Test
    public void testValidateWithValidUserNameAndPassword() throws AuthenticationException {
        basicAuthController.validate("TEST", "TEST", "TEST", true);
    }

    @Test(expected = AuthenticationException.class)
    public void testValidateWithInvalidUserNameAndValidPassword() throws AuthenticationException {
        basicAuthController.validate(null,"TEST", "TEST",true);
    }

    @Test(expected = AuthenticationException.class)
    public void testValidateWithValidUserNameAndInvalidPassword() throws AuthenticationException {
        basicAuthController.validate("TEST", null, "TEST",true);
    }

    @Test(expected = AuthenticationException.class)
    public void testValidateWithInvalidUserNameAndInvalidPassword() throws AuthenticationException {
        basicAuthController.validate(null, null, "TEST",true);
    }

    @Test(expected = AuthenticationException.class)
    public void testValidateWithValidUserNameAndValidPasswordAndUserNotEnabled() throws AuthenticationException {
        basicAuthController.validate("TEST", "TEST", "TEST",false);
    }

    @Test(expected = AuthenticationException.class)
    public void testValidateWithValidUserNameAndWrongPassword() throws AuthenticationException {
        when(passwordEncoder.matches(any(), any())).thenReturn(false);
        basicAuthController.validate("TEST", "TEST", "TEST",true);
    }

    @Test
    public void shouldNotChangeUserRoles() {
        User user = User.builder().roles(new ArrayList<>()).privacyPolicyAccepted(true).termsOfUseAccepted(true).build();
        basicAuthController.checkUserApprovals(user);
        assertTrue(user.getRoles().stream().map(UserRole::getRole).noneMatch(r -> r.equals(Role.ROLE_NOT_ACCEPTED)));
    }

    @Test
    public void shouldAddIncompleteRoleToUser() {
        when(domains.getGlobalDomain()).thenReturn(Optional.of(new Domain("name", "codename")));
        User user = User.builder().roles(new ArrayList<>()).username("testUser").privacyPolicyAccepted(false).termsOfUseAccepted(true).build();
        basicAuthController.checkUserApprovals(user);
        assertTrue(user.getRoles().stream().map(UserRole::getRole).anyMatch(r -> r.equals(Role.ROLE_NOT_ACCEPTED)));
    }

}
