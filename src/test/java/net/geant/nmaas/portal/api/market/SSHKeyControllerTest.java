package net.geant.nmaas.portal.api.market;

import net.geant.nmaas.portal.api.domain.SSHKeyRequest;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.SSHKeyService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class SSHKeyControllerTest {

    private SSHKeyService sshKeyService = mock(SSHKeyService.class);
    private UserService userService = mock(UserService.class);

    private SSHKeysController sut;

    private Principal present = mock(Principal.class);
    private Principal absent = mock(Principal.class);

    private User presentUser;

    @BeforeEach
    private void setup() {

        when(present.getName()).thenReturn("present");
        when(absent.getName()).thenReturn("absent");

        this.presentUser = new User("present");

        when(this.userService.findByUsername("present")).thenReturn(Optional.of(presentUser));
        when(this.userService.findByUsername("absent")).thenReturn(Optional.empty());

        this.sut = new SSHKeysController(sshKeyService, userService);
    }

    @Test
    public void shouldGetAllKeysForPresentUser() {
        this.sut.getAllByUser(present);

        verify(sshKeyService, times(1)).findAllByUser(presentUser);
    }

    @Test
    public void shouldThrowExceptionWhenUserDoesNotExist() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            this.sut.getAllByUser(absent);
        });

        assertEquals("User not found", e.getMessage());
    }

    @Test
    public void shouldInvalidateKey() {
        this.sut.invalidate(present, 12L);

        verify(sshKeyService, times(1)).invalidate(presentUser, 12L);
    }

    @Test
    public void shouldCreateKeyFromRequest() {
        SSHKeyRequest request = new SSHKeyRequest("name", "key");

        this.sut.create(present, request);

        verify(sshKeyService, times(1)).create(request, presentUser);
    }
}
