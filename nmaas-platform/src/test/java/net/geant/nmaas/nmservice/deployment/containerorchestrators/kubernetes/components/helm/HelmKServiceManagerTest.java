package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterDeploymentManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterIngressManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.KNamespaceService;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethod;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.repositories.DomainTechDetailsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HelmKServiceManagerTest {

    private KubernetesRepositoryManager repositoryManager = mock(KubernetesRepositoryManager.class);
    private KNamespaceService namespaceService = mock(KNamespaceService.class);
    private KClusterDeploymentManager deploymentManager = mock(KClusterDeploymentManager.class);
    private KClusterIngressManager ingressManager = mock(KClusterIngressManager.class);
    private HelmCommandExecutor helmCommandExecutor = mock(HelmCommandExecutor.class);
    private DomainTechDetailsRepository domainTechDetailsRepository = mock(DomainTechDetailsRepository.class);

    private Identifier deploymentId = Identifier.newInstance("deploymentId");

    private HelmKServiceManager manager = new HelmKServiceManager(
            repositoryManager,
            namespaceService,
            deploymentManager,
            ingressManager,
            helmCommandExecutor,
            domainTechDetailsRepository
    );

    @BeforeEach
    public void setup() {
        KubernetesNmServiceInfo service = new KubernetesNmServiceInfo();
        service.setDomain("domain");
        Set<ServiceAccessMethod> accessMethods = new HashSet<>();
        accessMethods.add(new ServiceAccessMethod(ServiceAccessMethodType.DEFAULT, "Default", null, null));
        accessMethods.add(new ServiceAccessMethod(ServiceAccessMethodType.EXTERNAL, "Web", null, null));
        accessMethods.add(new ServiceAccessMethod(ServiceAccessMethodType.INTERNAL, "SSH", null, null));
        service.setAccessMethods(accessMethods);
        service.setDescriptiveDeploymentId(Identifier.newInstance("descriptiveDeploymentId"));
        service.setStorageSpace(2);
        when(repositoryManager.loadService(any())).thenReturn(service);
    }

    @Test
    public void shouldDeployService() {
        when(namespaceService.namespace("domain")).thenReturn("namespace");
        manager.deployService(deploymentId);

        verify(helmCommandExecutor, times(1)).executeHelmRepoUpdateCommand();
        verify(helmCommandExecutor, times(1)).executeHelmInstallCommand(any(), any(), any(), any());
    }

}
