package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.api.dto.users.UserApiTokenDto;
import net.geant.nmaas.portal.exceptions.DataConflictException;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserApiToken;
import net.geant.nmaas.portal.persistence.repositories.UserApiTokenRepository;
import net.geant.nmaas.portal.service.impl.security.SecretPasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomAccessTokenServiceImplTest {

    private final UserApiTokenRepository userApiTokenRepository = mock(UserApiTokenRepository.class);
    private final SecretPasswordService secretPasswordService = mock(SecretPasswordService.class);

    private CustomAccessTokenServiceImpl sut;

    private User owner;

    private UserApiToken token(Long id, String name, String value, boolean valid, boolean deleted) {
        return new UserApiToken(id, name, owner, value, valid, deleted);
    }

    @BeforeEach
    void setup() {
        this.owner = new User("owner");
        this.owner.setId(1L);

        when(this.secretPasswordService.hashSecret(anyString())).thenReturn("hashed-value");

        this.sut = new CustomAccessTokenServiceImpl(userApiTokenRepository, secretPasswordService);
    }

    @Test
    void shouldCreateNewTokenAndReturnRawValue() {
        when(userApiTokenRepository.findAllByUserIdAndName(owner.getId(), "token-name")).thenReturn(List.of());

        UserApiTokenDto result = this.sut.createToken(owner, "token-name");

        assertNotNull(result);
        assertEquals("token-name", result.name());
        // the view is mapped before hashing, so the caller receives the raw token value once
        assertNotNull(result.tokenValue());
        assertNotEquals("hashed-value", result.tokenValue());
        assertTrue(result.valid());
        assertFalse(result.deleted());

        verify(userApiTokenRepository, times(1)).save(any(UserApiToken.class));
    }

    @Test
    void shouldStoreHashedTokenValue() {
        when(userApiTokenRepository.findAllByUserIdAndName(owner.getId(), "token-name")).thenReturn(List.of());

        this.sut.createToken(owner, "token-name");

        verify(userApiTokenRepository, times(1)).save(org.mockito.ArgumentMatchers.argThat(token ->
                "hashed-value".equals(token.getTokenValue())));
    }

    @Test
    void shouldThrowExceptionWhenTokenNameIsAlreadyInUse() {
        UserApiToken existing = token(2L, "token-name", "hashed-value", true, false);
        when(userApiTokenRepository.findAllByUserIdAndName(owner.getId(), "token-name")).thenReturn(List.of(existing));

        DataConflictException e = assertThrows(DataConflictException.class, () -> {
            this.sut.createToken(owner, "token-name");
        });

        assertEquals("Token name is already in use.", e.getMessage());
    }

    @Test
    void shouldCreateTokenWhenOnlyDeletedTokensWithSameNameExist() {
        UserApiToken deleted = token(2L, "token-name", "hashed-value", false, true);
        when(userApiTokenRepository.findAllByUserIdAndName(owner.getId(), "token-name")).thenReturn(List.of(deleted));

        UserApiTokenDto result = this.sut.createToken(owner, "token-name");

        assertNotNull(result);
        assertEquals("token-name", result.name());
        verify(userApiTokenRepository, times(1)).save(any(UserApiToken.class));
    }

    @Test
    void shouldReturnAllNonDeletedTokensForUser() {
        UserApiToken active = token(1L, "active", "hashed-value", true, false);
        UserApiToken deleted = token(2L, "deleted", "hashed-value", true, true);
        when(userApiTokenRepository.findAllByUserId(owner.getId())).thenReturn(List.of(active, deleted));

        List<UserApiTokenDto> result = this.sut.getAll(owner.getId());

        assertEquals(1, result.size());
        assertEquals("active", result.getFirst().name());
        assertTrue(result.getFirst().valid());
    }

    @Test
    void shouldInvalidateToken() {
        UserApiToken token = token(1L, "token-name", "hashed-value", true, false);
        when(userApiTokenRepository.findById(1L)).thenReturn(Optional.of(token));

        this.sut.invalidate(1L);

        assertFalse(token.isValid());
        verify(userApiTokenRepository, times(1)).save(token);
    }

    @Test
    void shouldThrowExceptionWhenInvalidatingNonExistingToken() {
        when(userApiTokenRepository.findById(1L)).thenReturn(Optional.empty());

        ObjectNotFoundException e = assertThrows(ObjectNotFoundException.class, () -> {
            this.sut.invalidate(1L);
        });

        assertEquals("Could not find access token with id: 1", e.getMessage());
    }

    @Test
    void shouldDeleteInvalidatedToken() {
        UserApiToken token = token(1L, "token-name", "hashed-value", false, false);
        when(userApiTokenRepository.findById(1L)).thenReturn(Optional.of(token));

        this.sut.delete(1L);

        assertTrue(token.isDeleted());
        verify(userApiTokenRepository, times(1)).save(token);
    }

    @Test
    void shouldThrowExceptionWhenDeletingValidToken() {
        UserApiToken token = token(1L, "token-name", "hashed-value", true, false);
        when(userApiTokenRepository.findById(1L)).thenReturn(Optional.of(token));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            this.sut.delete(1L);
        });

        assertEquals("Token is still valid, can not delete valid token", e.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingToken() {
        when(userApiTokenRepository.findById(1L)).thenReturn(Optional.empty());

        ObjectNotFoundException e = assertThrows(ObjectNotFoundException.class, () -> {
            this.sut.delete(1L);
        });

        assertEquals("Could not find access token with id: 1", e.getMessage());
    }
}
