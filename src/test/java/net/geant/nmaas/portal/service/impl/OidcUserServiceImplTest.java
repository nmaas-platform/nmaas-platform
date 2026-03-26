package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.auth.OidcApprovals;
import net.geant.nmaas.portal.api.configuration.model.ConfigurationView;
import net.geant.nmaas.portal.api.exceptions.ExternalUserMatchException;
import net.geant.nmaas.portal.api.exceptions.SignupException;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.repositories.UserRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OidcUserServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private DomainService domainService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ConfigurationManager configurationManager;

    @Mock
    private OidcUser oidcUser;

    @InjectMocks
    private OidcUserServiceImpl oidcUserService;

    @BeforeEach
    void setUp() {
        lenient().when(oidcUser.getAttribute("sub")).thenReturn("test-sub");
        lenient().when(oidcUser.getAttribute("email")).thenReturn("test@example.com");
        lenient().when(oidcUser.getAttribute("preferred_username")).thenReturn("testuser");
    }

    @Test
    void shouldReturnExistingUserWhenSamlTokenExistsAndIsValid() {
        User existingUser = new User("testuser");

        when(userService.existsBySamlToken("test-sub")).thenReturn(true);
        when(userService.findBySamlToken("test-sub")).thenReturn(Optional.of(existingUser));

        User result = oidcUserService.checkUser(oidcUser);

        assertEquals(existingUser, result);
    }

    @Test
    void shouldReturnExistingUserWhenSamlTokenMatchesOldEmailOrUsername() {
        User existingUser = new User("testuser");
        existingUser.setEmail("test@example.com");
        existingUser.setSamlToken("test@example.com");

        when(userService.existsBySamlToken("test-sub")).thenReturn(false);
        when(userService.existsByEmail("test@example.com")).thenReturn(true);
        when(userService.findByEmail("test@example.com")).thenReturn(existingUser);

        User result = oidcUserService.checkUser(oidcUser);

        assertEquals(existingUser, result);
        assertEquals("test-sub", existingUser.getSamlToken());
        verify(userService).update(existingUser);
    }

    @Test
    void shouldThrowWhenEmailExistsButSamlTokenDoesNotMatch() {
        User existingUser = new User("testuser");
        existingUser.setEmail("test@example.com");
        existingUser.setSamlToken("different-token");

        when(userService.existsBySamlToken("test-sub")).thenReturn(false);
        when(userService.existsByEmail("test@example.com")).thenReturn(true);
        when(userService.findByEmail("test@example.com")).thenReturn(existingUser);

        assertThrows(ExternalUserMatchException.class, () -> oidcUserService.checkUser(oidcUser));
    }

    @Test
    void registerNewUserFromOidcApprovalsShouldSaveUserWithApprovalsAndDefaults() {
        Domain global = new Domain(100L, "GLOBAL", "GLOBAL", true);
        ConfigurationView configuration = ConfigurationView.builder().defaultLanguage("pl").build();
        OidcApprovals approvals = new OidcApprovals(
                "token",
                "new@example.com",
                "ignored",
                "uuid-123",
                "Jan",
                "Kowalski",
                "jkowalski",
                true,
                true
        );

        when(domainService.getGlobalDomain()).thenReturn(Optional.of(global));
        when(configurationManager.getConfiguration()).thenReturn(configuration);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0, User.class));

        User result = oidcUserService.registerNewUser(approvals);

        assertEquals("jkowalski", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        assertEquals("Jan", result.getFirstname());
        assertEquals("Kowalski", result.getLastname());
        assertEquals("uuid-123", result.getSamlToken());
        assertTrue(result.isTermsOfUseAccepted());
        assertTrue(result.isPrivacyPolicyAccepted());
        assertEquals("pl", result.getSelectedLanguage());
        assertNotNull(result.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerNewUserFromOidcApprovalsShouldMapObjectAlreadyExistsToSignupException() {
        Domain global = new Domain(101L, "GLOBAL", "GLOBAL", true);
        ConfigurationView configuration = ConfigurationView.builder().defaultLanguage("en").build();
        OidcApprovals approvals = new OidcApprovals("token", "x@example.com", "ignored", "uuid", "A", "B", "ab", false, false);

        when(domainService.getGlobalDomain()).thenReturn(Optional.of(global));
        when(configurationManager.getConfiguration()).thenReturn(configuration);
        when(userRepository.save(any(User.class))).thenThrow(new ObjectAlreadyExistsException("exists"));

        SignupException ex = assertThrows(SignupException.class, () -> oidcUserService.registerNewUser(approvals));
        assertEquals("User already exists", ex.getMessage());
    }

    @Test
    void registerNewUserFromOidcUserShouldUseUsernameFallbackWhenPreferredUsernameMissing() {
        Domain global = new Domain(102L, "GLOBAL", "GLOBAL", true);
        ConfigurationView configuration = ConfigurationView.builder().defaultLanguage("en").build();

        when(oidcUser.getAttribute("preferred_username")).thenReturn(null);
        when(oidcUser.getAttribute("username")).thenReturn("fallback-username");
        when(oidcUser.getAttribute("email")).thenReturn("fallback@example.com");
        when(oidcUser.getAttribute("family_name")).thenReturn("Family");
        when(oidcUser.getAttribute("given_name")).thenReturn("Given");
        when(oidcUser.getAttribute("sub")).thenReturn("fallback-sub");

        when(domainService.getGlobalDomain()).thenReturn(Optional.of(global));
        when(configurationManager.getConfiguration()).thenReturn(configuration);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0, User.class));

        User result = oidcUserService.registerNewUser(oidcUser);

        assertEquals("fallback-username", result.getUsername());
        assertEquals("fallback-sub", result.getSamlToken());
    }

    @Test
    void registerNewUserShouldThrowSignupExceptionWhenGlobalDomainMissing() {
        OidcApprovals approvals = new OidcApprovals("token", "x@example.com", "ignored", "uuid", "A", "B", "ab", false, false);
        when(domainService.getGlobalDomain()).thenReturn(Optional.empty());

        SignupException ex = assertThrows(SignupException.class, () -> oidcUserService.registerNewUser(approvals));
        assertEquals("Domain not found", ex.getMessage());
    }

    @Test
    void externalUserRequiresLinkingShouldReturnTrueForUserWithoutSamlToken() {
        User user = new User("testuser");
        user.setEmail("test@example.com");
        user.setSamlToken("");

        when(userService.existsByEmail("test@example.com")).thenReturn(true);
        when(userService.findByEmail("test@example.com")).thenReturn(user);

        assertTrue(oidcUserService.externalUserRequiresLinking(oidcUser));
    }

    @Test
    void externalUserRequiresAupAndPnShouldReturnFalseWhenUserAlreadyExists() {
        when(userService.existsByEmail("test@example.com")).thenReturn(true);

        boolean result = oidcUserService.externalUserRequiresAupAndPn(oidcUser);

        assertEquals(false, result);
    }

    @Test
    void linkUserShouldUpdateAndReturnUser() {
        User existingUser = new User("testuser");
        existingUser.setEmail("test@example.com");

        when(userService.findByEmail("test@example.com")).thenReturn(existingUser);

        User result = oidcUserService.linkUser("test@example.com", "new-sub", "John", "Doe");

        assertEquals(existingUser, result);
        assertEquals("new-sub", result.getSamlToken());
        assertEquals("John", result.getFirstname());
        assertEquals("Doe", result.getLastname());
        verify(userService).update(existingUser);
    }
}
