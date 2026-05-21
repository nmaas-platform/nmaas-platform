package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster;

import net.geant.nmaas.kubernetes.KubernetesApiJanitorService;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.orchestration.Identifier;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultKServiceOperationsManagerTest {

    private static final Identifier DEPLOYMENT_ID = Identifier.newInstance("deploymentId");
    private static final Identifier DESCRIPTIVE_DEPLOYMENT_ID = Identifier.newInstance("descriptiveDeploymentId");

    private final KubernetesRepositoryManager repositoryManager = mock(KubernetesRepositoryManager.class);
    private final KubernetesApiJanitorService kubernetesApiJanitorService = mock(KubernetesApiJanitorService.class);

    private DefaultKServiceOperationsManager manager;

    @BeforeEach
    void setup() {
        manager = new DefaultKServiceOperationsManager(repositoryManager, kubernetesApiJanitorService);
    }

    @Test
    void shouldDelegateLocalServiceScaleToJanitorService() {
        KubernetesNmServiceInfo serviceInfo = new KubernetesNmServiceInfo();
        serviceInfo.setRemoteCluster(null);
        serviceInfo.setDescriptiveDeploymentId(DESCRIPTIVE_DEPLOYMENT_ID);
        serviceInfo.setDomain("domain");
        when(repositoryManager.loadService(DEPLOYMENT_ID)).thenReturn(serviceInfo);

        manager.scaleService(DEPLOYMENT_ID, 3);

        verify(repositoryManager, times(1)).loadService(DEPLOYMENT_ID);
        verify(kubernetesApiJanitorService, times(1))
                .scaleService(null, DESCRIPTIVE_DEPLOYMENT_ID, "domain", 3);
    }

    @Test
    void shouldDelegateRemoteServiceScaleToJanitorService() {
        KCluster remoteCluster = KCluster.builder().id(1L).build();
        KubernetesNmServiceInfo serviceInfo = new KubernetesNmServiceInfo();
        serviceInfo.setRemoteCluster(remoteCluster);
        serviceInfo.setDescriptiveDeploymentId(DESCRIPTIVE_DEPLOYMENT_ID);
        serviceInfo.setDomain("domain");
        when(repositoryManager.loadService(DEPLOYMENT_ID)).thenReturn(serviceInfo);

        manager.scaleService(DEPLOYMENT_ID, 5);

        verify(repositoryManager, times(1)).loadService(DEPLOYMENT_ID);
        verify(kubernetesApiJanitorService, times(1))
                .scaleService(remoteCluster, DESCRIPTIVE_DEPLOYMENT_ID, "domain", 5);
    }

    @Test
    void shouldNotSupportServiceRestart() {
        assertThrows(NotImplementedException.class, () -> manager.restartService(DEPLOYMENT_ID));
    }

}
