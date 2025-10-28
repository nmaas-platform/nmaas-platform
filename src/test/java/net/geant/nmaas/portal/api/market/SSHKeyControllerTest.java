package net.geant.nmaas.portal.api.market;

import net.geant.nmaas.portal.api.domain.SSHKeyRequest;
import net.geant.nmaas.portal.api.user.SSHKeysController;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserRole;
import net.geant.nmaas.portal.service.SSHKeyService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SSHKeyControllerTest {

    private SSHKeyService sshKeyService = mock(SSHKeyService.class);
    private UserService userService = mock(UserService.class);

    private SSHKeysController sut;

    private Principal present = mock(Principal.class);
    private Principal absent = mock(Principal.class);

    private Principal adminPrincipal = mock(Principal.class);

    private User presentUser;

    @BeforeEach
    private void setup() {

        when(present.getName()).thenReturn("present");
        when(absent.getName()).thenReturn("absent");
        when(adminPrincipal.getName()).thenReturn("admin");

        this.presentUser = new User("present");

       User admin = new User("admin");
       admin.setRoles( List.of(new UserRole(new User("admin", true), new Domain("name", "name"), Role.ROLE_SYSTEM_ADMIN)));

        when(this.userService.findByUsername("present")).thenReturn(Optional.of(presentUser));
        when(this.userService.findByUsername("absent")).thenReturn(Optional.empty());
        when(this.userService.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(this.userService.findById(1L)).thenReturn(Optional.of(presentUser));

        this.sut = new SSHKeysController(sshKeyService, userService);
    }

    @Test
    void shouldGetAllKeysForPresentUser() {
        this.sut.getAllByUser(present);

        verify(sshKeyService, times(1)).findAllByUser(presentUser);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            this.sut.getAllByUser(absent);
        });

        assertEquals("User not found", e.getMessage());
    }

    @Test
    void shouldInvalidateKey() {
        this.sut.invalidate(present, 12L);

        verify(sshKeyService, times(1)).invalidate(presentUser, 12L);
    }

    @Test
    void shouldCreateKeyFromRequest() {
        SSHKeyRequest request = new SSHKeyRequest("name", "key");

        this.sut.create(present, request);

        verify(sshKeyService, times(1)).create(request, presentUser);
    }

    @Test
    void shouldGetUserKeyById() {
        this.sut.getAllByUserId(adminPrincipal, 1L);

        verify(sshKeyService, times(1)).findAllByUser(presentUser);
    }

    @Test
    void shouldGetUserKeyByIdTriggerError() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            this.sut.getAllByUserId(present, 1L);
        });

        assertEquals("You need admin privileges to view user keys.", e.getMessage());
    }


    @Test
    void shouldInvalidateKeyUser() {
        this.sut.invalidateUserKey(adminPrincipal,12L, 1L);

        verify(sshKeyService, times(1)).invalidate(presentUser, 12L);
    }

    @Test
    void shouldCreateKeyFromRequestForUser() {
        SSHKeyRequest request = new SSHKeyRequest("name", "key");

        this.sut.createUserKey(adminPrincipal, 1L, request);

        verify(sshKeyService, times(1)).create(request, presentUser);
    }
}
