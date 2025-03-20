package net.geant.nmaas.portal.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.api.security.exceptions.AuthenticationMethodNotSupportedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        String token = "token123";
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
        String token = "token123";
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
}
