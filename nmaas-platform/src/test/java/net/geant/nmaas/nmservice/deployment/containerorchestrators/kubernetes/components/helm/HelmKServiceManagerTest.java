package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterDeploymentManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterIngressManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.KNamespaceService;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethod;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.repositories.DomainTechDetailsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
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
        service.setKubernetesTemplate(new KubernetesTemplate("chartName", "chartVersion", "archiveName"));
        when(repositoryManager.loadService(any())).thenReturn(service);
        when(repositoryManager.loadDescriptiveDeploymentId(deploymentId)).thenReturn(Identifier.newInstance("descriptiveDeploymentId"));
    }

    @Test
    public void shouldDeployService() {
        when(namespaceService.namespace("domain")).thenReturn("namespace");
        manager.deployService(deploymentId);

        verify(helmCommandExecutor, times(1)).executeHelmRepoUpdateCommand();
        verify(helmCommandExecutor, times(1)).executeHelmInstallCommand(
                eq("namespace"),
                eq("descriptiveDeploymentId"),
                isNotNull(),
                anyMap()
        );
    }

    @Test
    public void shouldCheckServiceDeployedTrue() {
        when(helmCommandExecutor.executeHelmStatusCommand("descriptiveDeploymentId")).thenReturn(HelmPackageStatus.DEPLOYED);

        boolean status = manager.checkServiceDeployed(deploymentId);
        verify(helmCommandExecutor, times(1)).executeHelmStatusCommand(eq("descriptiveDeploymentId"));
        assertTrue(status);
    }

    @Test
    public void shouldCheckServiceDeployedFalse() {
        when(helmCommandExecutor.executeHelmStatusCommand("descriptiveDeploymentId")).thenReturn(HelmPackageStatus.UNKNOWN);

        boolean status = manager.checkServiceDeployed(deploymentId);
        verify(helmCommandExecutor, times(1)).executeHelmStatusCommand(eq("descriptiveDeploymentId"));
        assertFalse(status);
    }

    @Test
    public void shouldDeleteServiceSinceExists() {
        when(helmCommandExecutor.executeHelmListCommand()).thenReturn(Arrays.asList("descriptiveDeploymentId", "otherString"));

        manager.deleteServiceIfExists(deploymentId);
        verify(helmCommandExecutor, times(1)).executeHelmDeleteCommand(eq("descriptiveDeploymentId"));
    }

    @Test
    public void shouldNotDeleteServiceSinceNotExists() {
        when(helmCommandExecutor.executeHelmListCommand()).thenReturn(Collections.singletonList("otherString"));

        manager.deleteServiceIfExists(deploymentId);
        verify(helmCommandExecutor, times(0)).executeHelmDeleteCommand(any());
    }

    @Test
    public void shouldUpgradeService() {
        manager.upgradeService(deploymentId);
        verify(helmCommandExecutor, times(1)).executeHelmRepoUpdateCommand();
        verify(helmCommandExecutor, times(1)).executeHelmUpgradeCommand(
                eq("descriptiveDeploymentId"),
                eq("archiveName")
        );
    }

}
