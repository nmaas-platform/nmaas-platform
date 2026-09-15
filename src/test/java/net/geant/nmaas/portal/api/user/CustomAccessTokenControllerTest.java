package net.geant.nmaas.portal.api.user;

import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.service.CustomAccessTokenService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomAccessTokenControllerTest {

    private final CustomAccessTokenService accessTokenService = mock(CustomAccessTokenService.class);
    private final UserService userService = mock(UserService.class);

    private CustomAccessTokenController sut;

    private final Principal present = mock(Principal.class);
    private final Principal absent = mock(Principal.class);

    private User presentUser;

    @BeforeEach
    void setup() {
        when(present.getName()).thenReturn("present");
        when(absent.getName()).thenReturn("absent");

        this.presentUser = new User("present");

        when(this.userService.findByUsername("present")).thenReturn(Optional.of(presentUser));
        when(this.userService.findByUsername("absent")).thenReturn(Optional.empty());

        this.sut = new CustomAccessTokenController(accessTokenService, userService);
    }

    @Test
    void shouldGetAllTokensForPresentUser() {
        this.sut.getAll(present);

        verify(accessTokenService, times(1)).getAll(presentUser.getId());
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        ObjectNotFoundException e = assertThrows(ObjectNotFoundException.class, () -> {
            this.sut.getAll(absent);
        });

        assertEquals("User not found", e.getMessage());
    }

    @Test
    void shouldCreateNewTokenForPresentUser() {
        this.sut.createNewToken(present, "token-name");

        verify(accessTokenService, times(1)).createToken(presentUser, "token-name");
    }

    @Test
    void shouldThrowExceptionWhenCreatingTokenForAbsentUser() {
        ObjectNotFoundException e = assertThrows(ObjectNotFoundException.class, () -> {
            this.sut.createNewToken(absent, "token-name");
        });

        assertEquals("User not found", e.getMessage());
    }

    @Test
    void shouldInvalidateToken() {
        this.sut.invalidateToken(12L);

        verify(accessTokenService, times(1)).invalidate(12L);
    }

    @Test
    void shouldDeleteToken() {
        this.sut.deleteToken(12L);

        verify(accessTokenService, times(1)).delete(12L);
    }
}
