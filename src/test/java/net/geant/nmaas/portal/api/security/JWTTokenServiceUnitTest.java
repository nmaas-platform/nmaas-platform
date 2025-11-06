package net.geant.nmaas.portal.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JWTTokenServiceUnitTest {

    @Mock
    private JWTSettings jwtSettings;

    @InjectMocks
    private JWTTokenService jwtTokenService;

    @BeforeEach
    void setUp() {

    }

    @Test
    void shouldThrowExceptionWhenUserIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            jwtTokenService.getToken(null);
        });
        assertEquals("User or username is not set", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUsernameIsEmpty() {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            jwtTokenService.getToken(user);
        });
        assertEquals("User or username is not set", exception.getMessage());
    }

    @Test
    void shouldGenerateTokenSuccessfully() {
        String signingKey = "xJNy3aPz2PqY6+X7sZKc7Jt8T+ZP5lJZ9xH3Hh6Jc6oZP5lJZ9xH3Hh6Jc6oZP5lJZ9xH3Hh6Jc6o+X3sZKc7Jt8T+ZP5lJZ9xH3Hh6Jc6o=";
        when(jwtSettings.getSigningKey()).thenReturn(signingKey);
        when(jwtSettings.getIssuer()).thenReturn("testIssuer");
        when(jwtSettings.getTokenValidFor()).thenReturn(3600000L); // 1h
        Domain domain = new Domain("GLOBAL", "globalTestDomain");
        User user = new User("testUser", true, "", domain, Role.ROLE_GUEST);
        UserRole role = new UserRole(user, domain, Role.ROLE_GUEST);
        List<UserRole> roles = List.of(role);
        user.setRoles(roles);


        String token = jwtTokenService.getToken(user);

        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertDoesNotThrow(() -> Jwts.parser().setSigningKey(signingKey).build().parseClaimsJws(token));
    }

    @Test
    void shouldGenerateTokenSuccessfullyAndHaveRoles() {
        jwtTokenService.globalDomain = "GLOBAL";
        String signingKey = "xJNy3aPz2PqY6+X7sZKc7Jt8T+ZP5lJZ9xH3Hh6Jc6oZP5lJZ9xH3Hh6Jc6oZP5lJZ9xH3Hh6Jc6o+X3sZKc7Jt8T+ZP5lJZ9xH3Hh6Jc6o=";
        Domain domain = new Domain("GLOBAL", "globalTestDomain");
        User user = new User("testUser", true, "", domain, mock(Role.class));
        UserRole role = new UserRole(user, domain, Role.ROLE_USER);
        List<UserRole> roles = List.of(role);
        user.setRoles(roles);
        when(jwtSettings.getSigningKey()).thenReturn(signingKey);
        when(jwtSettings.getIssuer()).thenReturn("testIssuer");
        when(jwtSettings.getTokenValidFor()).thenReturn(3600000L); // 1h

        String token = jwtTokenService.getToken(user);

        Claims claims = jwtTokenService.getClaims(token);

        assertNotNull(claims);
        assertEquals(List.of("ROLE_USER"),claims.get("global_role"));
    }
}