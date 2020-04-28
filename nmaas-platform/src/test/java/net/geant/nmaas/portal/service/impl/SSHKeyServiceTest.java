package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.domain.SSHKeyRequest;
import net.geant.nmaas.portal.api.domain.SSHKeyView;
import net.geant.nmaas.portal.persistent.entity.SSHKeyEntity;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.SSHKeyRepository;
import net.geant.nmaas.portal.service.SSHKeyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class SSHKeyServiceTest {

    private SSHKeyRepository repository = mock(SSHKeyRepository.class);

    private SSHKeyService sut;

    private User owner;

    @BeforeEach
    public void setup() {
        this.owner = new User("owner");
        this.owner.setId(1L);
        SSHKeyEntity key = new SSHKeyEntity(owner, "name", "key long long long");
        key.setId(1L);
        List<SSHKeyEntity> keys = new ArrayList<>();
        keys.add(key);
        when(repository.findAllByOwner(this.owner)).thenReturn(keys);
        when(repository.findById(anyLong())).thenReturn(Optional.empty());
        when(repository.findById(1L)).thenReturn(Optional.of(key));
        this.sut = new SSHKeyServiceImpl(this.repository);
    }

    @Test
    public void ShouldReturnAllKeysForUser() {
        List<SSHKeyView> result = this.sut.findAllByUser(this.owner);

        assertEquals(1, result.size());
        assertEquals("name", result.get(0).getName());
    }

    @Test
    public void shouldDeleteKeyIfValidRequest() {

        this.sut.invalidate(this.owner, 1L);

        verify(this.repository, times(1)).deleteById(1L);
    }

    @Test
    public void shouldThrowExceptionWhenKeyDoesNotExist() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            this.sut.invalidate(this.owner, 2L);
        });

        assertEquals("Key does not exist", e.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenKeyDoesNotBelongToTheUser() {
        User anonymous = new User("anonymous");
        anonymous.setId(31L);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            this.sut.invalidate(anonymous, 1L);
        });

        assertEquals("Invalid key owner", e.getMessage());

    }

    @Test
    public void shouldCreateNewSSHKeyEntityWhenNameValid() {
        when(repository.existsByOwnerAndName(this.owner, "new key")).thenReturn(false);
        SSHKeyRequest request = new SSHKeyRequest("new key", "so long key");
        SSHKeyEntity res = new SSHKeyEntity(owner, "new key", "so long key");
        res.setId(123L);
        when(repository.save(any(SSHKeyEntity.class))).thenReturn(res);

        SSHKeyView result = sut.create(request, this.owner);

        verify(this.repository, times(1)).save(any(SSHKeyEntity.class));
        assertEquals(result.getName(), res.getName());
        assertEquals(result.getId(), res.getId());
    }

    @Test
    public void shouldThrowExceptionWhenNameIsNotUnique() {
        when(repository.existsByOwnerAndName(this.owner, "name")).thenReturn(true);
        SSHKeyRequest request = new SSHKeyRequest("name", "so long key");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            this.sut.create(request, this.owner);
        });

        assertEquals("Name is already taken", e.getMessage());
    }
}
