package net.geant.nmaas.portal.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.api.security.exceptions.AuthenticationMethodNotSupportedException;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserApiToken;
import net.geant.nmaas.portal.persistence.entity.UserRole;
import net.geant.nmaas.portal.persistence.repositories.UserApiTokenRepository;
import net.geant.nmaas.portal.service.impl.security.SecretPasswordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenAuthenticationServiceTest {

    private static final String AUTH_HEADER = "Authorization";
    private static final String AUTH_METHOD = "Bearer";

    @Mock
    private JWTTokenService jwtTokenService;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private UserApiTokenRepository userApiTokenRepository;

    @Mock
    private SecretPasswordService secretPasswordService;

    @InjectMocks
    private TokenAuthenticationService tokenAuthenticationService;

    @Test
    void shouldThrowWhenAuthHeaderIsNull() {
        // given
        when(httpRequest.getHeader(AUTH_HEADER)).thenReturn(null);

        // when & then
        assertThrows(
                AuthenticationMethodNotSupportedException.class,
                () -> tokenAuthenticationService.getAuthentication(httpRequest)
        );
    }

    @Test
    void shouldThrowWhenAuthHeaderIsEmpty() {
        // given
        when(httpRequest.getHeader(AUTH_HEADER)).thenReturn("");

        // when & then
        assertThrows(
                AuthenticationMethodNotSupportedException.class,
                () -> tokenAuthenticationService.getAuthentication(httpRequest)
        );
    }

    @Test
    void shouldThrowWhenAuthHeaderDoesNotStartWithBearer() {
        // given
        when(httpRequest.getHeader(AUTH_HEADER)).thenReturn("SomeOtherMethod token123");

        // when & then
        assertThrows(
                AuthenticationMethodNotSupportedException.class,
                () -> tokenAuthenticationService.getAuthentication(httpRequest)
        );
    }

    @Test
    void shouldReturnAuthenticationWhenAuthHeaderIsValid() {
        // given
        String token = "example.jwt.token";
        String validHeader = AUTH_METHOD + " " + token;
        String username = "testUser";

        List<String> roles = List.of("ROLE_USER");
        List<String> globalRole = List.of("ROLE_ADMIN");

        Claims mockClaims = mock(Claims.class);
        when(mockClaims.getSubject()).thenReturn(username);
        when(mockClaims.get("roles")).thenReturn(roles);
        when(mockClaims.get("global_role")).thenReturn(globalRole);

        when(jwtTokenService.getClaims(token)).thenReturn(mockClaims);

        when(httpRequest.getHeader(AUTH_HEADER)).thenReturn(validHeader);

        // when
        Authentication authentication = tokenAuthenticationService.getAuthentication(httpRequest);

        // then
        assertNotNull(authentication);
        assertEquals(username, authentication.getName());

        assertEquals(authentication.getAuthorities().stream().map(Object::toString).toList(), List.of("ROLE_USER", "ROLE_ADMIN"));
    }

    @Test
    void shouldReturnAuthenticationWithNoAuthoritiesWhenRolesAreNotList() {
        // given
        String token = "example.jwt.token";
        String validHeader = AUTH_METHOD + " " + token;
        String username = "testUser";

        Claims mockClaims = mock(Claims.class);
        when(mockClaims.getSubject()).thenReturn(username);
        when(mockClaims.get("roles")).thenReturn("ROLE_USER");

        when(jwtTokenService.getClaims(token)).thenReturn(mockClaims);
        when(httpRequest.getHeader(AUTH_HEADER)).thenReturn(validHeader);

        // when
        Authentication authentication = tokenAuthenticationService.getAuthentication(httpRequest);

        // then
        assertNotNull(authentication);
        assertEquals(username, authentication.getName());

        assertTrue(authentication.getAuthorities().isEmpty());
    }

    @Test
    void shouldReturnAuthenticationForUuidTokenWithOnlyActiveDomainAuthorities() {
        String token = "550e8400-e29b-41d4-a716-446655440000";
        when(httpRequest.getHeader(AUTH_HEADER)).thenReturn(AUTH_METHOD + " " + token);

        User user = new User("uuid-user", true);
        Domain activeDomain = new Domain(1L, "active", "active", true);
        Domain inactiveDomain = new Domain(2L, "inactive", "inactive", false);
        user.setRoles(List.of(
                new UserRole(user, activeDomain, Role.ROLE_USER),
                new UserRole(user, inactiveDomain, Role.ROLE_OPERATOR)
        ));
        when(userApiTokenRepository.findAllByValid(true)).thenReturn(List.of(mock(UserApiToken.class)));
        when(secretPasswordService.findUserBasedOnToken(anyString(), anyList())).thenReturn(user);

        Authentication authentication = tokenAuthenticationService.getAuthentication(httpRequest);

        assertEquals("uuid-user", authentication.getName());
        assertEquals(1, authentication.getAuthorities().size());
        assertTrue(authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void shouldThrowWhenTokenTypeIsNotSupported() {
        when(httpRequest.getHeader(AUTH_HEADER)).thenReturn(AUTH_METHOD + " " + "bad-token");

        assertThrows(AuthenticationMethodNotSupportedException.class,
                () -> tokenAuthenticationService.getAuthentication(httpRequest));
    }

    @Test
    void shouldDetectUuidAuthorization() {
        when(httpRequest.getHeader(AUTH_HEADER)).thenReturn(AUTH_METHOD + " " + "550e8400-e29b-41d4-a716-446655440000");

        assertTrue(tokenAuthenticationService.isUUIDAuthorization(httpRequest));
    }

    @Test
    void shouldDetectJwtAuthorization() {
        when(httpRequest.getHeader(AUTH_HEADER)).thenReturn(AUTH_METHOD + " " + "a.b.c");

        assertTrue(tokenAuthenticationService.isJWTAuthorization(httpRequest));
    }

    @Test
    void getAuthenticationForJwtShouldThrowForInvalidToken() {
        when(httpRequest.getHeader(AUTH_HEADER)).thenReturn(AUTH_METHOD + " " + "550e8400-e29b-41d4-a716-446655440000");

        assertThrows(AuthenticationMethodNotSupportedException.class,
                () -> tokenAuthenticationService.getAuthenticationForJWT(httpRequest));
    }

    @Test
    void getAuthenticationForJwtShouldReturnAuthorities() {
        String token = "jwt.token.value";
        when(httpRequest.getHeader(AUTH_HEADER)).thenReturn(AUTH_METHOD + " " + token);

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("jwt-user");
        when(claims.get("roles")).thenReturn(List.of("ROLE_USER"));
        when(claims.get("global_role")).thenReturn(List.of("ROLE_SYSTEM_ADMIN"));
        when(jwtTokenService.getClaims(token)).thenReturn(claims);

        Authentication authentication = tokenAuthenticationService.getAuthenticationForJWT(httpRequest);

        assertEquals("jwt-user", authentication.getName());
        assertEquals(2, authentication.getAuthorities().size());
    }

    @Test
    void getAuthenticationForUuidShouldThrowForInvalidToken() {
        when(httpRequest.getHeader(AUTH_HEADER)).thenReturn(AUTH_METHOD + " " + "not-uuid");

        assertThrows(AuthenticationMethodNotSupportedException.class,
                () -> tokenAuthenticationService.getAuthenticationForUUID(httpRequest));
    }

    @Test
    void getAuthenticationForUuidShouldReturnAuthorities() {
        String token = "550e8400-e29b-41d4-a716-446655440000";
        when(httpRequest.getHeader(AUTH_HEADER)).thenReturn(AUTH_METHOD + " " + token);

        User user = new User("api-user", true);
        Domain activeDomain = new Domain(10L, "domain", "domain", true);
        user.setRoles(List.of(new UserRole(user, activeDomain, Role.ROLE_OPERATOR)));

        when(userApiTokenRepository.findAllByValid(true)).thenReturn(List.of(mock(UserApiToken.class)));
        when(secretPasswordService.findUserBasedOnToken(anyString(), anyList())).thenReturn(user);

        Authentication authentication = tokenAuthenticationService.getAuthenticationForUUID(httpRequest);

        assertEquals("api-user", authentication.getName());
        assertTrue(authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_OPERATOR")));
    }
}
