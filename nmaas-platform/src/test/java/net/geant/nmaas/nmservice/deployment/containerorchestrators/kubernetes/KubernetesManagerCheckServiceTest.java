package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster.DefaultKClusterValidator;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmKServiceManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress.DefaultIngressControllerManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress.DefaultIngressResourceManager;
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
    private KubernetesRepositoryManager repositoryManager = mock(KubernetesRepositoryManager.class);
    private DefaultKClusterValidator clusterValidator = mock(DefaultKClusterValidator.class);
    private KServiceManager serviceManager = mock(HelmKServiceManager.class);
    private IngressControllerManager ingressControllerManager = mock(DefaultIngressControllerManager.class);
    private IngressResourceManager ingressResourceManager = mock(DefaultIngressResourceManager.class);

    @Before
    public void setup() {
        manager = new KubernetesManager(repositoryManager,
                clusterValidator,
                serviceManager,
                ingressControllerManager,
                ingressResourceManager);
    }

    @Test
    public void shouldVerifyThatServiceIsDeployed() throws Exception {
        when(serviceManager.checkServiceDeployed(any(Identifier.class))).thenReturn(true);
        manager.checkService(Identifier.newInstance("deploymentId"));
    }

    @Test(expected = ContainerCheckFailedException.class)
    public void shouldThrowExceptionSinceServiceNotDeployed() throws Exception {
        when(serviceManager.checkServiceDeployed(any(Identifier.class))).thenReturn(false);
        manager.checkService(Identifier.newInstance("deploymentId"));
    }

}
