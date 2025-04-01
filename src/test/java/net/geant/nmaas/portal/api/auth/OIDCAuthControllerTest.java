package net.geant.nmaas.portal.api.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;
import net.geant.nmaas.portal.api.configuration.ConfigurationView;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.OidcUserService;
import net.geant.nmaas.portal.service.UserLoginRegisterService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.view.RedirectView;

import java.lang.reflect.Constructor;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OIDCAuthControllerTest {

    @InjectMocks
    private OIDCAuthController oidcAuthController;

    @Mock
    private OidcUserService oidcUserService;
    @Mock
    private JWTTokenService jwtTokenService;
    @Mock
    private UserLoginRegisterService loginRegisterService;
    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private DomainService domainService;
    @Mock
    private ConfigurationManager configurationManager;
    @Mock
    private HttpServletRequest request;

    @Test
    void shouldReturnOidcTokenOnSuccessfulLinking() throws Exception {
        // Given
        OidcLogin oidcLogin = new OidcLogin("user@example.com", "pass123", "uuid-123", "John", "Doe", "oidc-token");

        Constructor<User> userConstructor = User.class.getDeclaredConstructor();
        userConstructor.setAccessible(true);
        User user = userConstructor.newInstance();

        ReflectionTestUtils.setField(user, "password", "hashed-password");
        ReflectionTestUtils.setField(user, "enabled", true);
        ReflectionTestUtils.setField(user, "termsOfUseAccepted", true);
        ReflectionTestUtils.setField(user, "privacyPolicyAccepted", true);

        Constructor<Domain> domainConstructor = Domain.class.getDeclaredConstructor();
        domainConstructor.setAccessible(true);
        Domain domain = domainConstructor.newInstance();
        ReflectionTestUtils.setField(domain, "name", "Global");

        UserRole role = new UserRole(user, domain, Role.ROLE_USER);
        ReflectionTestUtils.setField(user, "roles", List.of(role));

        when(userService.findByEmail(any())).thenReturn(user);
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        ConfigurationView config = mock(ConfigurationView.class);
        when(config.isMaintenance()).thenReturn(false);
        when(configurationManager.getConfiguration()).thenReturn(config);

        User linkedUser = userConstructor.newInstance();
        when(oidcUserService.linkUser(any(), any(), any(), any())).thenReturn(linkedUser);
        when(jwtTokenService.getToken(any())).thenReturn("jwt-token");
        when(jwtTokenService.getRefreshToken(any())).thenReturn("refresh-token");

        when(request.getHeader(HttpHeaders.HOST)).thenReturn("localhost");
        when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn("JUnit");

        // When
        UserOidcToken result = oidcAuthController.oidcLinkedSuccess(oidcLogin, request);

        // Then
        assertEquals("jwt-token", result.token());
        assertEquals("refresh-token", result.refreshToken());
        assertEquals("uuid-123", result.oidcToken());

        verify(loginRegisterService).registerNewSuccessfulLogin(eq(user), any(), any(), any());
    }

    @Test
    void shouldRedirectToLoginSuccessIfOidcUserIsValid() throws Exception {
        // given
        OidcUser oidcUser = mock(OidcUser.class);
        OidcIdToken idToken = mock(OidcIdToken.class);
        when(idToken.getTokenValue()).thenReturn("oidc-token");
        when(oidcUser.getIdToken()).thenReturn(idToken);

        when(oidcUserService.externalUserRequiredLinking(any())).thenReturn(false);

        Constructor<User> userConstructor = User.class.getDeclaredConstructor();
        userConstructor.setAccessible(true);
        User user = userConstructor.newInstance();

        when(oidcUserService.checkUser(any())).thenReturn(user);
        when(jwtTokenService.getToken(any())).thenReturn("jwt-token");
        when(jwtTokenService.getRefreshToken(any())).thenReturn("refresh-token");

        when(request.getHeader(HttpHeaders.HOST)).thenReturn("localhost");
        when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn("JUnit");

        // when
        RedirectView result = oidcAuthController.oidcLoginSuccess(oidcUser, request);

        // then
        assertTrue(result.getUrl().contains("login-success"));
        assertTrue(result.getUrl().contains("token=jwt-token"));
        assertTrue(result.getUrl().contains("refresh_token=refresh-token"));
        assertTrue(result.getUrl().contains("oidc_token=oidc-token"));

        verify(loginRegisterService).registerNewSuccessfulLogin(eq(user), any(), any(), any());
    }

    @Test
    void shouldRedirectToLinkingIfExternalUserRequiresLinking() {
        // given
        OidcUser oidcUser = mock(OidcUser.class);
        OidcIdToken idToken = mock(OidcIdToken.class);
        when(idToken.getTokenValue()).thenReturn("oidc-token");
        when(oidcUser.getIdToken()).thenReturn(idToken);

        when(oidcUserService.externalUserRequiredLinking(any())).thenReturn(true);

        // when
        RedirectView result = oidcAuthController.oidcLoginSuccess(oidcUser, request);

        // then
        assertTrue(result.getUrl().contains("/login-linking?oidc_token=oidc-token"));
    }
}
