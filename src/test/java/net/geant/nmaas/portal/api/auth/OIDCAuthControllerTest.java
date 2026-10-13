package net.geant.nmaas.portal.api.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;
import net.geant.nmaas.api.dto.users.UserDto;
import net.geant.nmaas.portal.api.configuration.model.ConfigurationDto;
import net.geant.nmaas.portal.api.exceptions.AuthenticationException;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserRole;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.OidcIdTokenValidator;
import net.geant.nmaas.portal.service.OidcUserClaims;
import net.geant.nmaas.portal.service.OidcUserService;
import net.geant.nmaas.portal.service.UserLoginRegisterService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.view.RedirectView;

import java.lang.reflect.Constructor;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OIDCAuthControllerTest {

    private static final String OIDC_TOKEN = "oidc-token";

    private static final OidcUserClaims CLAIMS = new OidcUserClaims(
            "uuid-123", "user@example.com", "user", "John", "Doe");

    @InjectMocks
    private OIDCAuthController oidcAuthController;

    @Mock
    private OidcIdTokenValidator oidcIdTokenValidator;
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
    private ModelMapper modelMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private HttpServletRequest request;

    @Test
    void shouldReturnOidcTokenOnSuccessfulLinking() throws Exception {
        // Given
        OidcLogin oidcLogin = new OidcLogin("user@example.com", "pass123", OIDC_TOKEN);
        when(oidcIdTokenValidator.validate(OIDC_TOKEN)).thenReturn(CLAIMS);

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

        when(userService.findByEmail("user@example.com")).thenReturn(user);
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        ConfigurationDto config = mock(ConfigurationDto.class);
        when(config.isMaintenance()).thenReturn(false);
        when(configurationManager.getConfiguration()).thenReturn(config);

        User linkedUser = userConstructor.newInstance();
        when(oidcUserService.linkUser(CLAIMS)).thenReturn(linkedUser);
        when(jwtTokenService.getToken(any())).thenReturn("jwt-token");
        when(jwtTokenService.getRefreshToken(any())).thenReturn("refresh-token");

        when(request.getHeader(HttpHeaders.HOST)).thenReturn("localhost");
        when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn("JUnit");

        // When
        UserOidcToken result = oidcAuthController.oidcLinkedSuccess(oidcLogin, request);

        // Then
        assertEquals("jwt-token", result.token());
        assertEquals("refresh-token", result.refreshToken());
        assertEquals(OIDC_TOKEN, result.oidcToken());

        verify(oidcUserService).linkUser(CLAIMS);
        verify(loginRegisterService).registerNewSuccessfulLogin(eq(user), any(), any(), any());
    }

    @Test
    void shouldNotLinkUserWhenOidcTokenIsInvalid() {
        // Given
        OidcLogin oidcLogin = new OidcLogin("user@example.com", "pass123", "forged-token");
        when(oidcIdTokenValidator.validate("forged-token")).thenThrow(new AuthenticationException("Invalid OIDC token"));

        // When + Then
        assertThrows(AuthenticationException.class, () -> oidcAuthController.oidcLinkedSuccess(oidcLogin, request));
        verifyNoInteractions(oidcUserService, userService, loginRegisterService);
    }

    @Test
    void shouldNotLinkUserWhenEmailDoesNotMatchValidatedToken() {
        // Given
        OidcLogin oidcLogin = new OidcLogin("other@example.com", "pass123", OIDC_TOKEN);
        when(oidcIdTokenValidator.validate(OIDC_TOKEN)).thenReturn(CLAIMS);

        // When + Then
        assertThrows(AuthenticationException.class, () -> oidcAuthController.oidcLinkedSuccess(oidcLogin, request));
        verifyNoInteractions(oidcUserService, userService, loginRegisterService);
    }

    @Test
    void shouldRegisterNewUserOnApprovals() throws Exception {
        // Given
        OidcApprovals approvals = new OidcApprovals(OIDC_TOKEN, "newuser", true, true);
        when(oidcIdTokenValidator.validate(OIDC_TOKEN)).thenReturn(CLAIMS);

        Constructor<User> userConstructor = User.class.getDeclaredConstructor();
        userConstructor.setAccessible(true);
        User user = userConstructor.newInstance();
        user.setUsername("newuser");

        when(oidcUserService.registerNewUser(CLAIMS, "newuser", true, true)).thenReturn(user);

        UserDto admin = mock(UserDto.class);
        when(userService.findAllUsersWithAdminRole()).thenReturn(List.of(admin));
        when(modelMapper.map(any(UserDto.class), eq(UserDto.class))).thenReturn(admin);

        ConfigurationDto config = mock(ConfigurationDto.class);
        when(config.isMaintenance()).thenReturn(false);
        when(config.getDefaultDomainForSsoUsers()).thenReturn(null);
        when(configurationManager.getConfiguration()).thenReturn(config);

        when(jwtTokenService.getToken(any())).thenReturn("jwt-token");
        when(jwtTokenService.getRefreshToken(any())).thenReturn("refresh-token");

        when(request.getHeader(HttpHeaders.HOST)).thenReturn("localhost");
        when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn("JUnit");

        // When
        UserOidcToken result = oidcAuthController.oidcApprovalsSuccess(approvals, request);

        // Then
        assertEquals("jwt-token", result.token());
        assertEquals("refresh-token", result.refreshToken());
        assertEquals(OIDC_TOKEN, result.oidcToken());

        verify(oidcUserService).registerNewUser(eq(CLAIMS), eq("newuser"), anyBoolean(), anyBoolean());
        verify(loginRegisterService).registerNewSuccessfulLogin(eq(user), any(), any(), any());
    }

    @Test
    void shouldNotRegisterNewUserWhenOidcTokenIsInvalid() {
        // Given
        OidcApprovals approvals = new OidcApprovals("forged-token", "attacker", true, true);
        when(oidcIdTokenValidator.validate("forged-token")).thenThrow(new AuthenticationException("Invalid OIDC token"));

        // When + Then
        assertThrows(AuthenticationException.class, () -> oidcAuthController.oidcApprovalsSuccess(approvals, request));
        verifyNoInteractions(oidcUserService, userService, loginRegisterService);
    }

    @Test
    void shouldRedirectToLoginSuccessIfOidcUserIsValid() throws Exception {
        // given
        OidcUser oidcUser = mock(OidcUser.class);
        OidcIdToken idToken = mock(OidcIdToken.class);
        when(idToken.getTokenValue()).thenReturn(OIDC_TOKEN);
        when(oidcUser.getIdToken()).thenReturn(idToken);

        when(oidcUserService.externalUserRequiresLinking(any())).thenReturn(false);
        when(oidcUserService.externalUserRequiresAupAndPn(any())).thenReturn(false);

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
        assertTrue(result.getUrl().contains("refresh-token=refresh-token"));
        assertTrue(result.getUrl().contains("oidc-token=oidc-token"));

        verify(loginRegisterService).registerNewSuccessfulLogin(eq(user), any(), any(), any());
    }

    @Test
    void shouldRedirectToLinkingIfExternalUserRequiresLinking() {
        // given
        OidcUser oidcUser = mock(OidcUser.class);
        OidcIdToken idToken = mock(OidcIdToken.class);
        when(idToken.getTokenValue()).thenReturn(OIDC_TOKEN);
        when(oidcUser.getIdToken()).thenReturn(idToken);

        when(oidcUserService.externalUserRequiresLinking(any())).thenReturn(true);

        // when
        RedirectView result = oidcAuthController.oidcLoginSuccess(oidcUser, request);

        // then
        assertTrue(result.getUrl().contains("/login-linking?oidc-token=oidc-token"));
    }
}
