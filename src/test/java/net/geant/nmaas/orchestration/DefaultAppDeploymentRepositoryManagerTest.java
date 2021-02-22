package net.geant.nmaas.orchestration;

import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentOwner;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import net.geant.nmaas.portal.persistent.entity.SSHKeyEntity;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.SSHKeyRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultAppDeploymentRepositoryManagerTest {

    Identifier deploymentId = Identifier.newInstance(1L);

    private final AppDeploymentRepository appDeploymentRepository = mock(AppDeploymentRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final SSHKeyRepository sshKeyRepository = mock(SSHKeyRepository.class);

    private DefaultAppDeploymentRepositoryManager manager;

    @BeforeEach
    public void setup() {
        manager = new DefaultAppDeploymentRepositoryManager(appDeploymentRepository, userRepository, sshKeyRepository);
        when(appDeploymentRepository.findByDeploymentId(deploymentId)).thenReturn(Optional.of(AppDeployment.builder().owner("user1").build()));
    }

    @Test
    public void shouldReturnAppDeploymentOwnerWithDefaultNameAndNoKeys() {
        User user1 = new User("user1");
        user1.setEmail("user1@test.eu");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));
        when(sshKeyRepository.findAllByOwner(user1)).thenReturn(Lists.emptyList());
        AppDeploymentOwner appDeploymentOwner = manager.loadOwner(deploymentId);
        assertEquals("user1", appDeploymentOwner.getUsername());
        assertEquals("user1", appDeploymentOwner.getName());
        assertEquals("user1@test.eu", appDeploymentOwner.getEmail());
        assertEquals(0, appDeploymentOwner.getSshKeys().size());
    }

    @Test
    public void shouldReturnAppDeploymentOwnerWithProperNameAndAKey() {
        User user1 = new User("user1");
        user1.setEmail("user1@test.eu");
        user1.setFirstname("firstname1");
        user1.setLastname("lastname1");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));
        when(sshKeyRepository.findAllByOwner(user1)).thenReturn(Lists.newArrayList(new SSHKeyEntity(user1, "key1", "keycontent")));
        AppDeploymentOwner appDeploymentOwner = manager.loadOwner(deploymentId);
        assertEquals("user1", appDeploymentOwner.getUsername());
        assertEquals("firstname1 lastname1", appDeploymentOwner.getName());
        assertEquals("user1@test.eu", appDeploymentOwner.getEmail());
        assertEquals(1, appDeploymentOwner.getSshKeys().size());
        assertEquals("keycontent", appDeploymentOwner.getSshKeys().get(0));
    }

}
