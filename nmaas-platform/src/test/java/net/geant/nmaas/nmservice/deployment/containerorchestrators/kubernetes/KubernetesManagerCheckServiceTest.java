package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import net.geant.nmaas.externalservices.inventory.kubernetes.KubernetesClusterManager;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerCheckFailedException;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class KubernetesManagerCheckServiceTest {

    private KubernetesManager manager;
    private KubernetesNmServiceRepositoryManager repositoryManager = mock(KubernetesNmServiceRepositoryManager.class);
    private HelmCommandExecutor helmCommandExecutor = mock(HelmCommandExecutor.class);
    private KubernetesApiConnector kubernetesApiConnector = mock(KubernetesApiConnector.class);
    private KubernetesClusterManager kubernetesClusterManager = mock(KubernetesClusterManager.class);

    @Before
    public void setup() {
        manager = new KubernetesManager(repositoryManager, helmCommandExecutor, kubernetesApiConnector, kubernetesClusterManager);
    }

    @Test
    public void shouldVerifyThatServiceIsDeployed() throws Exception {
        when(helmCommandExecutor.executeHelmStatusCommand(any(Identifier.class))).thenReturn(HelmPackageStatus.DEPLOYED);
        manager.checkService(Identifier.newInstance("deploymentId"));
    }

    @Test(expected = ContainerCheckFailedException.class)
    public void shouldThrowExceptionSinceServiceNotDeployed() throws Exception {
        when(helmCommandExecutor.executeHelmStatusCommand(any(Identifier.class))).thenReturn(HelmPackageStatus.UNKNOWN);
        manager.checkService(Identifier.newInstance("deploymentId"));
    }

}
