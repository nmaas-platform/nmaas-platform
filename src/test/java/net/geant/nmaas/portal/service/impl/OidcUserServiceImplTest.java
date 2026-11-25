package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.configuration.model.ConfigurationDto;
import net.geant.nmaas.portal.api.exceptions.ExternalUserCanNotBeLinked;
import net.geant.nmaas.portal.api.exceptions.ExternalUserMatchException;
import net.geant.nmaas.portal.api.exceptions.SignupException;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.repositories.UserRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.OidcUserClaims;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    private PasswordEncoder passwordEncoder;

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
    void shouldReturnExistingUserWhenExternalIdExistsAndIsValid() {
        User existingUser = new User("testuser");

        when(userService.existsByExternalId("test-sub")).thenReturn(true);
        when(userService.findByExternalId("test-sub")).thenReturn(Optional.of(existingUser));

        User result = oidcUserService.checkUser(oidcUser);

        assertEquals(existingUser, result);
    }

    @Test
    void shouldReturnExistingUserWhenExternalIdMatchesOldEmailOrUsername() {
        User existingUser = new User("testuser");
        existingUser.setEmail("test@example.com");
        existingUser.setExternalId("test@example.com");

        when(userService.existsByExternalId("test-sub")).thenReturn(false);
        when(userService.existsByEmail("test@example.com")).thenReturn(true);
        when(userService.findByEmail("test@example.com")).thenReturn(existingUser);

        User result = oidcUserService.checkUser(oidcUser);

        assertEquals(existingUser, result);
        assertEquals("test-sub", existingUser.getExternalId());
        verify(userService).update(existingUser);
    }

    @Test
    void shouldThrowWhenEmailExistsButExternalIdDoesNotMatch() {
        User existingUser = new User("testuser");
        existingUser.setEmail("test@example.com");
        existingUser.setExternalId("different-token");

        when(userService.existsByExternalId("test-sub")).thenReturn(false);
        when(userService.existsByEmail("test@example.com")).thenReturn(true);
        when(userService.findByEmail("test@example.com")).thenReturn(existingUser);

        assertThrows(ExternalUserMatchException.class, () -> oidcUserService.checkUser(oidcUser));
    }

    @Test
    void checkUserShouldThrowWhenSubjectClaimMissing() {
        when(oidcUser.getAttribute("sub")).thenReturn(null);

        assertThrows(ExternalUserMatchException.class, () -> oidcUserService.checkUser(oidcUser));
    }

    @Test
    void checkUserShouldThrowWhenEmailClaimMissing() {
        when(oidcUser.getAttribute("email")).thenReturn(null);

        assertThrows(ExternalUserMatchException.class, () -> oidcUserService.checkUser(oidcUser));
    }

    @Test
    void checkUserShouldThrowWhenUserHasNoExternalIdentityLinked() {
        User existingUser = new User("testuser");
        existingUser.setEmail("test@example.com");

        when(userService.existsByExternalId("test-sub")).thenReturn(false);
        when(userService.existsByEmail("test@example.com")).thenReturn(true);
        when(userService.findByEmail("test@example.com")).thenReturn(existingUser);

        assertThrows(ExternalUserMatchException.class, () -> oidcUserService.checkUser(oidcUser));
        verify(userService, never()).update(existingUser);
    }

    @Test
    void checkUserShouldHandleConcurrentLinkingOfTheSameExternalIdentity() {
        User existingUser = new User("testuser");
        existingUser.setEmail("test@example.com");
        existingUser.setExternalId("test@example.com");

        when(userService.existsByExternalId("test-sub")).thenReturn(false);
        when(userService.existsByEmail("test@example.com")).thenReturn(true);
        when(userService.findByEmail("test@example.com")).thenReturn(existingUser);
        doThrow(new DataIntegrityViolationException("unique constraint")).when(userService).update(existingUser);

        assertThrows(ExternalUserMatchException.class, () -> oidcUserService.checkUser(oidcUser));
    }

    @Test
    void registerNewUserFromValidatedClaimsShouldSaveUserWithApprovalsAndDefaults() {
        Domain global = new Domain(100L, "GLOBAL", "GLOBAL", true);
        ConfigurationDto configuration = ConfigurationDto.builder().defaultLanguage("pl").build();
        OidcUserClaims claims = new OidcUserClaims("uuid-123", "new@example.com", "jkowalski", "Jan", "Kowalski");

        when(domainService.getGlobalDomain()).thenReturn(Optional.of(global));
        when(configurationManager.getConfiguration()).thenReturn(configuration);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0, User.class));
        when(passwordEncoder.encode(any())).thenReturn("encoded-password");

        User result = oidcUserService.registerNewUser(claims, "jkowalski", true, true);

        assertEquals("jkowalski", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        assertEquals("Jan", result.getFirstname());
        assertEquals("Kowalski", result.getLastname());
        assertEquals("uuid-123", result.getExternalId());
        assertTrue(result.isTermsOfUseAccepted());
        assertTrue(result.isPrivacyPolicyAccepted());
        assertEquals("pl", result.getSelectedLanguage());
        assertEquals("encoded-password", result.getPassword());
        verify(passwordEncoder).encode(any());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerNewUserFromValidatedClaimsShouldFallBackToPreferredUsernameWhenRequestedUsernameBlank() {
        Domain global = new Domain(101L, "GLOBAL", "GLOBAL", true);
        ConfigurationDto configuration = ConfigurationDto.builder().defaultLanguage("en").build();
        OidcUserClaims claims = new OidcUserClaims("uuid-456", "new2@example.com", "tokenuser", "Anna", "Nowak");

        when(domainService.getGlobalDomain()).thenReturn(Optional.of(global));
        when(configurationManager.getConfiguration()).thenReturn(configuration);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0, User.class));

        User result = oidcUserService.registerNewUser(claims, "  ", true, true);

        assertEquals("tokenuser", result.getUsername());
    }

    @Test
    void registerNewUserFromValidatedClaimsShouldThrowWhenNoUsernameAvailable() {
        OidcUserClaims claims = new OidcUserClaims("uuid-789", "new3@example.com", null, null, null);

        SignupException ex = assertThrows(SignupException.class,
                () -> oidcUserService.registerNewUser(claims, null, true, true));
        assertEquals("Username is required", ex.getMessage());
        verifyNoInteractions(userRepository);
    }

    @Test
    void registerNewUserFromValidatedClaimsShouldMapDuplicateInsertFailureToSignupException() {
        Domain global = new Domain(102L, "GLOBAL", "GLOBAL", true);
        ConfigurationDto configuration = ConfigurationDto.builder().defaultLanguage("en").build();
        OidcUserClaims claims = new OidcUserClaims("uuid", "x@example.com", "ab", "A", "B");

        when(domainService.getGlobalDomain()).thenReturn(Optional.of(global));
        when(configurationManager.getConfiguration()).thenReturn(configuration);
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate key"));

        SignupException ex = assertThrows(SignupException.class,
                () -> oidcUserService.registerNewUser(claims, "ab", false, false));
        assertEquals("User already exists", ex.getMessage());
    }

    @Test
    void registerNewUserFromValidatedClaimsShouldRejectWhenUsernameAlreadyTaken() {
        Domain global = new Domain(104L, "GLOBAL", "GLOBAL", true);
        OidcUserClaims claims = new OidcUserClaims("uuid-1", "new@example.com", "taken", "John", "Doe");

        when(domainService.getGlobalDomain()).thenReturn(Optional.of(global));
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        SignupException ex = assertThrows(SignupException.class,
                () -> oidcUserService.registerNewUser(claims, "taken", true, true));
        assertEquals("User already exists", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerNewUserFromValidatedClaimsShouldRejectWhenEmailAlreadyTaken() {
        Domain global = new Domain(105L, "GLOBAL", "GLOBAL", true);
        OidcUserClaims claims = new OidcUserClaims("uuid-2", "taken@example.com", "newuser", "John", "Doe");

        when(domainService.getGlobalDomain()).thenReturn(Optional.of(global));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        SignupException ex = assertThrows(SignupException.class,
                () -> oidcUserService.registerNewUser(claims, "newuser", true, true));
        assertEquals("User already exists", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerNewUserFromValidatedClaimsShouldRejectTooLongUsername() {
        OidcUserClaims claims = new OidcUserClaims("uuid-3", "new@example.com", null, null, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> oidcUserService.registerNewUser(claims, "a".repeat(256), true, true));
        assertTrue(ex.getMessage().contains("maximum length"));
        verifyNoInteractions(domainService);
    }

    @Test
    void checkUserShouldThrowWhenNoLocalAccountMatches() {
        when(userService.existsByExternalId("test-sub")).thenReturn(false);
        when(userService.existsByEmail("test@example.com")).thenReturn(false);

        ExternalUserMatchException ex = assertThrows(ExternalUserMatchException.class,
                () -> oidcUserService.checkUser(oidcUser));
        assertTrue(ex.getMessage().contains("No local account matches"));
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(domainService, configurationManager);
    }

    @Test
    void registerNewUserFromValidatedClaimsShouldThrowWhenExternalIdentityAlreadyLinkedToAnotherUser() {
        OidcUserClaims claims = new OidcUserClaims("taken-sub", "new@example.com", "newuser", "John", "Doe");
        when(userRepository.existsByExternalId("taken-sub")).thenReturn(true);

        SignupException ex = assertThrows(SignupException.class,
                () -> oidcUserService.registerNewUser(claims, "newuser", true, true));
        assertEquals("External identity is already linked to another user", ex.getMessage());
        verifyNoInteractions(domainService);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerNewUserShouldThrowSignupExceptionWhenGlobalDomainMissing() {
        OidcUserClaims claims = new OidcUserClaims("uuid", "x@example.com", "ab", "A", "B");
        when(domainService.getGlobalDomain()).thenReturn(Optional.empty());

        SignupException ex = assertThrows(SignupException.class,
                () -> oidcUserService.registerNewUser(claims, "ab", false, false));
        assertEquals("Domain not found", ex.getMessage());
    }

    @Test
    void externalUserRequiresLinkingShouldReturnTrueForUserWithoutExternalId() {
        User user = new User("testuser");
        user.setEmail("test@example.com");
        user.setExternalId("");

        when(userService.existsByEmail("test@example.com")).thenReturn(true);
        when(userService.findByEmail("test@example.com")).thenReturn(user);

        assertTrue(oidcUserService.externalUserRequiresLinking(oidcUser));
    }

    @Test
    void externalUserRequiresAupAndPnShouldReturnTrueForUserWithoutAccount() {
        when(userService.existsByEmail("test@example.com")).thenReturn(false);

        assertTrue(oidcUserService.externalUserRequiresAupAndPn(oidcUser));
    }

    @Test
    void externalUserRequiresAupAndPnShouldReturnTrueWhenLinkedUserHasPendingApprovals() {
        User user = new User("testuser");
        user.setEmail("test@example.com");
        user.setExternalId("test-sub");

        when(userService.existsByEmail("test@example.com")).thenReturn(true);
        when(userService.findByEmail("test@example.com")).thenReturn(user);

        assertTrue(oidcUserService.externalUserRequiresAupAndPn(oidcUser));
    }

    @Test
    void externalUserRequiresAupAndPnShouldReturnTrueForLegacyLinkedUserWithPendingApprovals() {
        User user = new User("testuser");
        user.setEmail("test@example.com");
        user.setExternalId("test@example.com");

        when(userService.existsByEmail("test@example.com")).thenReturn(true);
        when(userService.findByEmail("test@example.com")).thenReturn(user);

        assertTrue(oidcUserService.externalUserRequiresAupAndPn(oidcUser));
    }

    @Test
    void externalUserRequiresAupAndPnShouldReturnFalseWhenApprovalsAlreadyAccepted() {
        User user = new User("testuser");
        user.setEmail("test@example.com");
        user.setExternalId("test-sub");
        user.setTermsOfUseAccepted(true);
        user.setPrivacyPolicyAccepted(true);

        when(userService.existsByEmail("test@example.com")).thenReturn(true);
        when(userService.findByEmail("test@example.com")).thenReturn(user);

        assertFalse(oidcUserService.externalUserRequiresAupAndPn(oidcUser));
    }

    @Test
    void externalUserRequiresAupAndPnShouldReturnFalseWhenAccountLinkedToDifferentIdentity() {
        User user = new User("testuser");
        user.setEmail("test@example.com");
        user.setExternalId("different-identity");

        when(userService.existsByEmail("test@example.com")).thenReturn(true);
        when(userService.findByEmail("test@example.com")).thenReturn(user);

        assertFalse(oidcUserService.externalUserRequiresAupAndPn(oidcUser));
    }

    @Test
    void externalUserRequiresAupAndPnShouldReturnFalseWhenAccountHasNoLinkedIdentity() {
        User user = new User("testuser");
        user.setEmail("test@example.com");

        when(userService.existsByEmail("test@example.com")).thenReturn(true);
        when(userService.findByEmail("test@example.com")).thenReturn(user);

        assertFalse(oidcUserService.externalUserRequiresAupAndPn(oidcUser));
    }

    @Test
    void registerNewUserShouldRecordApprovalsWhenAccountAlreadyExists() {
        User existingUser = new User("testuser");
        existingUser.setEmail("test@example.com");
        existingUser.setExternalId("uuid-123");

        OidcUserClaims claims = new OidcUserClaims("uuid-123", "test@example.com", "testuser", "John", "Doe");
        when(userService.existsByEmail("test@example.com")).thenReturn(true);
        when(userService.findByEmail("test@example.com")).thenReturn(existingUser);

        User result = oidcUserService.registerNewUser(claims, "testuser", true, true);

        assertEquals(existingUser, result);
        assertTrue(result.isTermsOfUseAccepted());
        assertTrue(result.isPrivacyPolicyAccepted());
        assertEquals("uuid-123", result.getExternalId());
        verify(userService).update(existingUser);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerNewUserShouldMigrateLegacyExternalIdWhenRecordingApprovals() {
        User existingUser = new User("testuser");
        existingUser.setEmail("test@example.com");
        existingUser.setExternalId("test@example.com");

        OidcUserClaims claims = new OidcUserClaims("uuid-legacy", "test@example.com", null, null, null);
        when(userService.existsByEmail("test@example.com")).thenReturn(true);
        when(userService.findByEmail("test@example.com")).thenReturn(existingUser);

        User result = oidcUserService.registerNewUser(claims, null, true, true);

        assertEquals("uuid-legacy", result.getExternalId());
        assertTrue(result.isTermsOfUseAccepted());
        assertTrue(result.isPrivacyPolicyAccepted());
        verify(userService).update(existingUser);
    }

    @Test
    void registerNewUserShouldRejectExistingAccountLinkedToDifferentIdentity() {
        User existingUser = new User("testuser");
        existingUser.setEmail("test@example.com");
        existingUser.setExternalId("other-sub");

        OidcUserClaims claims = new OidcUserClaims("uuid-123", "test@example.com", "testuser", "John", "Doe");
        when(userService.existsByEmail("test@example.com")).thenReturn(true);
        when(userService.findByEmail("test@example.com")).thenReturn(existingUser);

        SignupException ex = assertThrows(SignupException.class,
                () -> oidcUserService.registerNewUser(claims, "testuser", true, true));
        assertEquals("User already exists", ex.getMessage());
        verify(userService, never()).update(existingUser);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerNewUserShouldRejectExistingAccountWithoutLinkedIdentity() {
        User existingUser = new User("testuser");
        existingUser.setEmail("test@example.com");

        OidcUserClaims claims = new OidcUserClaims("uuid-123", "test@example.com", "testuser", "John", "Doe");
        when(userService.existsByEmail("test@example.com")).thenReturn(true);
        when(userService.findByEmail("test@example.com")).thenReturn(existingUser);

        assertThrows(SignupException.class, () -> oidcUserService.registerNewUser(claims, "testuser", true, true));
        verify(userService, never()).update(existingUser);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerNewUserShouldMapConcurrentLinkingFailureWhenRecordingApprovals() {
        User existingUser = new User("testuser");
        existingUser.setEmail("test@example.com");
        existingUser.setExternalId("test@example.com");

        OidcUserClaims claims = new OidcUserClaims("uuid-123", "test@example.com", null, null, null);
        when(userService.existsByEmail("test@example.com")).thenReturn(true);
        when(userService.findByEmail("test@example.com")).thenReturn(existingUser);
        doThrow(new DataIntegrityViolationException("unique constraint")).when(userService).update(existingUser);

        assertThrows(ExternalUserCanNotBeLinked.class,
                () -> oidcUserService.registerNewUser(claims, null, true, true));
    }

    @Test
    void linkUserShouldUpdateAndReturnUser() {
        User existingUser = new User("testuser");
        existingUser.setEmail("test@example.com");

        when(userService.findByEmail("test@example.com")).thenReturn(existingUser);

        OidcUserClaims claims = new OidcUserClaims("new-sub", "test@example.com", null, "John", "Doe");
        User result = oidcUserService.linkUser(claims);

        assertEquals(existingUser, result);
        assertEquals("new-sub", result.getExternalId());
        assertEquals("John", result.getFirstname());
        assertEquals("Doe", result.getLastname());
        verify(userService).update(existingUser);
    }

    @Test
    void linkUserShouldRejectUserAlreadyLinkedToDifferentExternalIdentity() {
        User existingUser = new User("testuser");
        existingUser.setEmail("test@example.com");
        existingUser.setExternalId("other-sub");

        when(userService.findByEmail("test@example.com")).thenReturn(existingUser);

        OidcUserClaims claims = new OidcUserClaims("new-sub", "test@example.com", null, "John", "Doe");

        assertThrows(ExternalUserCanNotBeLinked.class, () -> oidcUserService.linkUser(claims));
        verify(userService, never()).update(existingUser);
        assertEquals("other-sub", existingUser.getExternalId());
    }

    @Test
    void linkUserShouldRejectWhenExternalIdentityLinkedToAnotherAccount() {
        User existingUser = new User("testuser");
        existingUser.setId(1L);
        existingUser.setEmail("test@example.com");

        User otherAccount = new User("otheruser");
        otherAccount.setId(2L);
        otherAccount.setEmail("other@example.com");
        otherAccount.setExternalId("new-sub");

        when(userService.findByEmail("test@example.com")).thenReturn(existingUser);
        when(userRepository.findByExternalId("new-sub")).thenReturn(Optional.of(otherAccount));

        OidcUserClaims claims = new OidcUserClaims("new-sub", "test@example.com", null, "John", "Doe");

        assertThrows(ExternalUserCanNotBeLinked.class, () -> oidcUserService.linkUser(claims));
        verify(userService, never()).update(existingUser);
    }

    @Test
    void linkUserShouldAllowWhenExternalIdentityLinkedToSameAccount() {
        User existingUser = new User("testuser");
        existingUser.setId(1L);
        existingUser.setEmail("test@example.com");
        existingUser.setExternalId("new-sub");

        when(userService.findByEmail("test@example.com")).thenReturn(existingUser);
        when(userRepository.findByExternalId("new-sub")).thenReturn(Optional.of(existingUser));

        OidcUserClaims claims = new OidcUserClaims("new-sub", "test@example.com", null, "John", "Doe");
        User result = oidcUserService.linkUser(claims);

        assertEquals("new-sub", result.getExternalId());
        verify(userService).update(existingUser);
    }

    @Test
    void linkUserShouldAllowRelinkingSameExternalIdentity() {
        User existingUser = new User("testuser");
        existingUser.setEmail("test@example.com");
        existingUser.setExternalId("new-sub");

        when(userService.findByEmail("test@example.com")).thenReturn(existingUser);

        OidcUserClaims claims = new OidcUserClaims("new-sub", "test@example.com", null, "John", "Doe");
        User result = oidcUserService.linkUser(claims);

        assertEquals("new-sub", result.getExternalId());
        verify(userService).update(existingUser);
    }

    @Test
    void linkUserShouldKeepExistingNamesWhenClaimsDoNotContainThem() {
        User existingUser = new User("testuser");
        existingUser.setEmail("test@example.com");
        existingUser.setFirstname("Existing");
        existingUser.setLastname("Name");

        when(userService.findByEmail("test@example.com")).thenReturn(existingUser);

        OidcUserClaims claims = new OidcUserClaims("new-sub", "test@example.com", null, null, null);
        User result = oidcUserService.linkUser(claims);

        assertEquals("Existing", result.getFirstname());
        assertEquals("Name", result.getLastname());
    }
}
