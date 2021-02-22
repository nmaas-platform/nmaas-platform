package net.geant.nmaas.portal.api.auth;

import net.geant.nmaas.portal.api.security.SSOConfigManager;
import net.geant.nmaas.portal.api.configuration.ConfigurationView;
import net.geant.nmaas.portal.api.exception.AuthenticationException;
import net.geant.nmaas.portal.api.exception.SignupException;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.exceptions.UndergoingMaintenanceException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserLoginRegisterService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SSOAuthControlleTest {

    private SSOAuthController ssoAuthController;

    private UserService users = mock(UserService.class);

    private DomainService domains = mock(DomainService.class);

    private JWTTokenService jwtTokenService = mock(JWTTokenService.class);

    private ConfigurationManager configurationManager = mock(ConfigurationManager.class);

    private SSOConfigManager SSOConfigManager = mock(SSOConfigManager.class);

    private ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

    private UserLoginRegisterService userLoginService = mock(UserLoginRegisterService.class);

    private HttpServletRequest request = mock(HttpServletRequest.class);

    @BeforeEach
    public void setup(){
        ssoAuthController = new SSOAuthController(users, domains, jwtTokenService, configurationManager, SSOConfigManager, userLoginService);
        when(request.getHeader(any())).thenReturn("empty");
    }

    @Test
    public void loginShouldThrowSignupExceptionWhenSSOLoginNotAllowed() {
        ConfigurationView configuration = new ConfigurationView();
        configuration.setSsoLoginAllowed(false);
        when(configurationManager.getConfiguration()).thenReturn(configuration);
        assertThrows(SignupException.class, () -> {
            ssoAuthController.login(null, request);
        });
    }

    @Test
    public void loginShouldThrowAuthenticationExceptionWhenLoginDataIsNull() {
        ConfigurationView configuration = new ConfigurationView();
        configuration.setSsoLoginAllowed(true);
        when(configurationManager.getConfiguration()).thenReturn(configuration);
        assertThrows(AuthenticationException.class, () -> {
            ssoAuthController.login(null, request);
        });
    }

    @Test
    public void loginShouldThrowAuthenticationExceptionWhenLoginDataIsEmpty() {
        ConfigurationView configuration = new ConfigurationView();
        configuration.setSsoLoginAllowed(true);
        when(configurationManager.getConfiguration()).thenReturn(configuration);
        UserSSOLogin userSSOLogin = mock(UserSSOLogin.class);
        when(userSSOLogin.getUsername()).thenReturn("");
        assertThrows(AuthenticationException.class, () -> {
            ssoAuthController.login(userSSOLogin, request);
        });
    }

    @Test
    public void loginShouldThrowAuthenticationExceptionWhenUserIsNotEnabled() {
        ConfigurationView configuration = new ConfigurationView();
        configuration.setSsoLoginAllowed(true);
        when(configurationManager.getConfiguration()).thenReturn(configuration);
        UserSSOLogin userSSOLogin = mock(UserSSOLogin.class);
        when(userSSOLogin.getUsername()).thenReturn("johny");
        User user = new User("johny", false);
        when(users.findBySamlToken(any())).thenReturn(Optional.of(user));
        assertThrows(AuthenticationException.class, () -> {
            ssoAuthController.login(userSSOLogin, request);
        });
    }

    @Test
    public void shouldRegisterNewUserIfUserIsNotFound() {
        ConfigurationView configuration = new ConfigurationView();
        configuration.setSsoLoginAllowed(true);
        when(configurationManager.getConfiguration()).thenReturn(configuration);
        UserSSOLogin userSSOLoginData = mock(UserSSOLogin.class);
        when(userSSOLoginData.getUsername()).thenReturn("johny");
        User user = new User("johny", true);
        when(users.findBySamlToken(any())).thenReturn(Optional.empty());
        when(users.register(any(), any())).thenReturn(user);
        Domain global = mock(Domain.class);
        when(domains.getGlobalDomain()).thenReturn(Optional.of(global));

        ssoAuthController.login(userSSOLoginData, request);

        verify(users).findBySamlToken(isA(String.class));
        verify(users).register(isA(UserSSOLogin.class), isA(Domain.class));
    }

    @Test
    public void shouldLoginUserWithoutRegistrationIfUserExistsAndIsEnabled() {
        ConfigurationView configuration = new ConfigurationView();
        configuration.setSsoLoginAllowed(true);
        configuration.setMaintenance(false);
        when(configurationManager.getConfiguration()).thenReturn(configuration);
        UserSSOLogin userSSOLoginData = mock(UserSSOLogin.class);
        when(userSSOLoginData.getUsername()).thenReturn("johny");
        User user = new User("johny", true);
        when(users.findBySamlToken(any())).thenReturn(Optional.of(user));
        when(users.register(any(), any())).thenReturn(user);
        Domain global = mock(Domain.class);
        when(domains.getGlobalDomain()).thenReturn(Optional.of(global));

        when(jwtTokenService.getToken(any())).thenReturn("sometoken");
        when(jwtTokenService.getRefreshToken(any())).thenReturn("somerefreshtoken");

        UserToken userToken = ssoAuthController.login(userSSOLoginData, request);

        assertEquals("sometoken", userToken.getToken());
        assertEquals("somerefreshtoken", userToken.getRefreshToken());
    }

    @Test
    public void shouldThrowSignupExceptionWhenUserAlreadyExists() {
        ConfigurationView configuration = new ConfigurationView();
        configuration.setSsoLoginAllowed(true);
        when(configurationManager.getConfiguration()).thenReturn(configuration);
        UserSSOLogin userSSOLoginData = mock(UserSSOLogin.class);
        when(userSSOLoginData.getUsername()).thenReturn("johny");
        User user = new User("johny", true);
        when(users.findBySamlToken(any())).thenReturn(Optional.empty());
        when(users.register(any(), any())).thenThrow(ObjectAlreadyExistsException.class);
        Domain global = mock(Domain.class);
        when(domains.getGlobalDomain()).thenReturn(Optional.of(global));

        assertThrows(SignupException.class, () -> {
            ssoAuthController.login(userSSOLoginData, request);
        });

    }

    @Test
    public void shouldThrowSignupExceptionWhenDomainDoesNotExist() {
        ConfigurationView configuration = new ConfigurationView();
        configuration.setSsoLoginAllowed(true);
        when(configurationManager.getConfiguration()).thenReturn(configuration);
        UserSSOLogin userSSOLoginData = mock(UserSSOLogin.class);
        when(userSSOLoginData.getUsername()).thenReturn("johny");
        User user = new User("johny", true);
        when(users.findBySamlToken(any())).thenReturn(Optional.empty());
        when(users.register(any(), any())).thenReturn(user);
        Domain global = mock(Domain.class);
        when(domains.getGlobalDomain()).thenReturn(Optional.empty());

        assertThrows(SignupException.class, () -> {
            ssoAuthController.login(userSSOLoginData, request);
        });
    }

    @Test
    public void shouldThrowUndergoingMaintenanceExceptionIfApplicationIsUndergoingMaintenance() {
        ConfigurationView configuration = new ConfigurationView();
        configuration.setSsoLoginAllowed(true);
        configuration.setMaintenance(true);
        when(configurationManager.getConfiguration()).thenReturn(configuration);
        UserSSOLogin userSSOLoginData = mock(UserSSOLogin.class);
        when(userSSOLoginData.getUsername()).thenReturn("johny");
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("johny");
        when(user.isEnabled()).thenReturn(true);
        when(user.getRoles()).thenReturn(new ArrayList<>()); // if no roles, then he is not an admin
        when(users.findBySamlToken(any())).thenReturn(Optional.of(user));
        when(users.register(any(), any())).thenReturn(user);
        Domain global = mock(Domain.class);
        when(domains.getGlobalDomain()).thenReturn(Optional.of(global));

        when(jwtTokenService.getToken(any())).thenReturn("sometoken");
        when(jwtTokenService.getRefreshToken(any())).thenReturn("somerefreshtoken");

        assertThrows(UndergoingMaintenanceException.class, () -> {
            ssoAuthController.login(userSSOLoginData, request);
        });
    }

    @Test
    public void whenUserIsAdminAndApplicationIsUnderMaintenanceThenShouldLogin(){
        ConfigurationView configuration = new ConfigurationView();
        configuration.setSsoLoginAllowed(true);
        configuration.setMaintenance(true);
        when(configurationManager.getConfiguration()).thenReturn(configuration);
        UserSSOLogin userSSOLoginData = mock(UserSSOLogin.class);
        when(userSSOLoginData.getUsername()).thenReturn("johny");
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("johny");
        when(user.isEnabled()).thenReturn(true);
        UserRole userRole = mock(UserRole.class);
        when(userRole.getRole()).thenReturn(Role.ROLE_SYSTEM_ADMIN);
        when(user.getRoles()).thenReturn(new ArrayList<UserRole>(){{add(userRole);}});
        when(users.findBySamlToken(any())).thenReturn(Optional.of(user));
        when(users.register(any(), any())).thenReturn(user);
        Domain global = mock(Domain.class);
        when(domains.getGlobalDomain()).thenReturn(Optional.of(global));

        when(jwtTokenService.getToken(any())).thenReturn("sometoken");
        when(jwtTokenService.getRefreshToken(any())).thenReturn("somerefreshtoken");

        UserToken userToken = ssoAuthController.login(userSSOLoginData, request);

        assertEquals("sometoken", userToken.getToken());
        assertEquals("somerefreshtoken", userToken.getRefreshToken());


    }



}
