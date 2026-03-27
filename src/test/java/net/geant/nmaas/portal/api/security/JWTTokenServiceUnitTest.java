package net.geant.nmaas.portal.api.security;

import io.jsonwebtoken.Claims;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JWTTokenServiceUnitTest {

    private static final String SIGNING_KEY =
            "TESTONLYqYqY6+X7sZKc7Jt8T+ZP5lJZ9xH3Hh6Jc6oZP5lJZ9xH3Hh6Jc6oZP5lJZ9xH3Hh6Jc6o+X3sZKc7Jt8T+ZP5lJZ9xH3Hh6Jc6o=";
    private static final String RESET_SIGNING_KEY =
            "TESTONLYqYzY2MSUvYtN7aL8h8wb+5I5lTlYWx6GQu8U5R3K6Q4SKf4q3BvD8nFf";

    @Mock
    private JWTSettings jwtSettings;

    @Test
    void shouldThrowExceptionWhenUserIsNull() {
        JWTTokenService jwtTokenService = createTokenService();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                jwtTokenService.getToken(null));
        assertEquals("User or username is not set", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUsernameIsEmpty() {
        JWTTokenService jwtTokenService = createTokenService();
        User user = new User("", true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                jwtTokenService.getToken(user));
        assertEquals("User or username is not set", exception.getMessage());
    }

    @Test
    void shouldGenerateAccessTokenWithExpectedClaims() {
        JWTTokenService jwtTokenService = createTokenService();
        Map<String, Object> roleDefinitions = Map.of(
                "GLOBAL", Role.ROLE_SYSTEM_ADMIN,
                "TEST-DOMAIN", Role.ROLE_USER
        );

        User user = userWithRoles("testUser", roleDefinitions);
        user.setFirstname("John");
        user.setSelectedLanguage("en");
        user.setSelectedThemeMode("light");

        String token = jwtTokenService.getToken(user);
        Claims claims = jwtTokenService.getClaims(token);

        assertNotNull(token);
        assertEquals("testUser", claims.getSubject());
        assertEquals("John", claims.get("preferred_username"));
        assertEquals(List.of("ROLE_SYSTEM_ADMIN"), claims.get("global_role"));
        assertTrue(((List<?>) claims.get("roles")).contains("ROLE_USER"));
        assertEquals("en", claims.get("language"));
        assertEquals("light", claims.get("thememode"));
    }

    @Test
    void shouldFallbackToUsernameWhenFirstNameMissing() {
        JWTTokenService jwtTokenService = createTokenService();
        Map<String, Object> roleDefinitions = Map.of("GLOBAL", Role.ROLE_SYSTEM_ADMIN);
        User user = userWithRoles("testUser", roleDefinitions);
        user.setFirstname(null);

        String token = jwtTokenService.getToken(user);
        Claims claims = jwtTokenService.getClaims(token);

        assertEquals("testUser", claims.get("preferred_username"));
    }

    @Test
    void shouldGenerateRefreshTokenAndValidateIt() {
        JWTTokenService jwtTokenService = createTokenService();
        Map<String, Object> roleDefinitions = Map.of("GLOBAL", Role.ROLE_SYSTEM_ADMIN);

        User user = userWithRoles("testUser", roleDefinitions);
        user.setSelectedLanguage("pl");

        String refreshToken = jwtTokenService.getRefreshToken(user);

        assertTrue(jwtTokenService.validateRefreshToken(refreshToken));
        Claims claims = jwtTokenService.getClaims(refreshToken);
        assertEquals("testUser", claims.getSubject());
        assertEquals(List.of("REFRESH_TOKEN"), claims.get("scopes"));
        assertEquals("pl", claims.get("language"));
    }

    @Test
    void shouldNotValidateAccessTokenAsRefreshToken() {
        JWTTokenService jwtTokenService = createTokenService();
        Map<String, Object> roleDefinitions = Map.of("GLOBAL", Role.ROLE_SYSTEM_ADMIN);
        User user = userWithRoles("testUser", roleDefinitions);

        String accessToken = jwtTokenService.getToken(user);

        assertFalse(jwtTokenService.validateRefreshToken(accessToken));
    }

    @Test
    void shouldNotValidateMalformedTokenAsRefreshToken() {
        JWTTokenService jwtTokenService = createTokenService();
        assertFalse(jwtTokenService.validateRefreshToken("notajwt"));
    }

    @Test
    void shouldThrowExceptionForNullOrEmptyEmailInResetToken() {
        JWTTokenService jwtTokenService = createTokenService();
        assertThrows(IllegalArgumentException.class, () -> jwtTokenService.getResetToken(null));
        assertThrows(IllegalArgumentException.class, () -> jwtTokenService.getResetToken(""));
    }

    @Test
    void shouldGenerateAndParseResetToken() {
        JWTTokenService jwtTokenService = createTokenService();
        String token = jwtTokenService.getResetToken("john@example.org");

        Claims claims = jwtTokenService.getResetClaims(token);

        assertEquals("john@example.org", claims.getSubject());
        assertEquals("testIssuer", claims.getIssuer());
    }

    @Test
    void shouldGenerateResetToken24Hours() {
        JWTTokenService jwtTokenService = createTokenService();

        assertDoesNotThrow(() -> jwtTokenService.getResetToken24Hours("john@example.org"));
    }

    private JWTTokenService createTokenService() {
        when(jwtSettings.getSigningKey()).thenReturn(SIGNING_KEY);
        when(jwtSettings.getResetSigningKey()).thenReturn(RESET_SIGNING_KEY);
        when(jwtSettings.getIssuer()).thenReturn("testIssuer");
        when(jwtSettings.getTokenValidFor()).thenReturn(3600000L);
        when(jwtSettings.getRefreshTokenExpTime()).thenReturn(7200000L);
        when(jwtSettings.getResetTokenExpTime()).thenReturn(900000L);
        when(jwtSettings.getRegistrationResetTokenExpTime()).thenReturn(86400000L);

        JWTTokenService service = new JWTTokenService(jwtSettings);
        service.globalDomain = "GLOBAL";
        return service;
    }

    private User userWithRoles(String username, Map<String, Object> roleDefinitions) {
        User user = new User(username, true);

        roleDefinitions.forEach((domainName, role) -> {
            Domain domain = new Domain(domainName, domainName.toLowerCase());
            UserRole userRole = new UserRole(user, domain, (Role) role);
            user.getRoles().add(userRole);
        });
        return user;

    }
}
