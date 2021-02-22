package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterDeploymentManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterIngressManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.KNamespaceService;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressCertificateConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressResourceConfigOption;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethod;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceStorageVolume;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceStorageVolumeType;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.repositories.DomainTechDetailsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
        Set<ServiceStorageVolume> storageVolumes = new HashSet<>();
        Map<HelmChartPersistenceVariable, String> pvMap = new HashMap<>();
        pvMap.put(HelmChartPersistenceVariable.PERSISTENCE_ENABLED, "persistence.enabled");
        pvMap.put(HelmChartPersistenceVariable.PERSISTENCE_NAME, "persistence.name");
        pvMap.put(HelmChartPersistenceVariable.PERSISTENCE_STORAGE_CLASS, "persistence.storageClass");
        pvMap.put(HelmChartPersistenceVariable.PERSISTENCE_STORAGE_SPACE, "persistence.size");
        storageVolumes.add(new ServiceStorageVolume(ServiceStorageVolumeType.MAIN, 2, pvMap));
        Set<ServiceAccessMethod> accessMethods = new HashSet<>();
        Map<HelmChartIngressVariable, String> ivMap = new HashMap<>();
        ivMap.put(HelmChartIngressVariable.INGRESS_ENABLED, "ingress.enabled");
        ivMap.put(HelmChartIngressVariable.INGRESS_CLASS, "ingress.class");
        ivMap.put(HelmChartIngressVariable.INGRESS_HOSTS, "ingress.hosts");
        ivMap.put(HelmChartIngressVariable.INGRESS_LETSENCRYPT, "ingress.tls.acme");
        ivMap.put(HelmChartIngressVariable.INGRESS_TLS_ENABLED, "ingress.tls.enabled");
        ivMap.put(HelmChartIngressVariable.INGRESS_WILDCARD_OR_ISSUER, "ingress.tls.certOrIssuer");
        accessMethods.add(new ServiceAccessMethod(ServiceAccessMethodType.DEFAULT, "Default", null, "Web", ivMap));
        accessMethods.add(new ServiceAccessMethod(ServiceAccessMethodType.EXTERNAL, "web-service", null, "Web", ivMap));
        accessMethods.add(new ServiceAccessMethod(ServiceAccessMethodType.INTERNAL, "ssh-service", null, "SSH", null));
        service.setStorageVolumes(storageVolumes);
        service.setAccessMethods(accessMethods);
        service.setDescriptiveDeploymentId(Identifier.newInstance("descriptiveDeploymentId"));
        service.setKubernetesTemplate(new KubernetesTemplate("chartName", "chartVersion", "archiveName"));
        when(repositoryManager.loadService(any())).thenReturn(service);
        when(repositoryManager.loadDescriptiveDeploymentId(deploymentId)).thenReturn(Identifier.newInstance("descriptiveDeploymentId"));
        when(repositoryManager.loadDomain(deploymentId)).thenReturn("domain");
    }

    @Test
    public void shouldDeployService() {
        when(namespaceService.namespace("domain")).thenReturn("namespace");
        when(ingressManager.getResourceConfigOption()).thenReturn(IngressResourceConfigOption.DEPLOY_FROM_CHART);
        when(ingressManager.getIngressPerDomain()).thenReturn(false);
        when(ingressManager.getSupportedIngressClass()).thenReturn("testIngressClass");
        when(ingressManager.getTlsSupported()).thenReturn(true);
        when(ingressManager.getIssuerOrWildcardName()).thenReturn("testIssuerName");
        when(ingressManager.getCertificateConfigOption()).thenReturn(IngressCertificateConfigOption.USE_LETSENCRYPT);
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
        when(namespaceService.namespace("domain")).thenReturn("namespace");
        when(helmCommandExecutor.executeHelmStatusCommand("namespace", "descriptiveDeploymentId"))
                .thenReturn(HelmPackageStatus.DEPLOYED);

        boolean status = manager.checkServiceDeployed(deploymentId);
        verify(helmCommandExecutor, times(1))
                .executeHelmStatusCommand(eq("namespace"), eq("descriptiveDeploymentId"));
        assertTrue(status);
    }

    @Test
    public void shouldCheckServiceDeployedFalse() {
        when(namespaceService.namespace("domain")).thenReturn("namespace");
        when(helmCommandExecutor.executeHelmStatusCommand("namespace", "descriptiveDeploymentId")).
                thenReturn(HelmPackageStatus.UNKNOWN);

        boolean status = manager.checkServiceDeployed(deploymentId);
        verify(helmCommandExecutor, times(1))
                .executeHelmStatusCommand(eq("namespace"), eq("descriptiveDeploymentId"));
        assertFalse(status);
    }

    @Test
    public void shouldDeleteServiceSinceExists() {
        when(namespaceService.namespace("domain")).thenReturn("namespace");
        when(helmCommandExecutor.executeHelmListCommand("namespace"))
                .thenReturn(Arrays.asList("descriptiveDeploymentId", "otherString"));

        manager.deleteServiceIfExists(deploymentId);
        verify(helmCommandExecutor, times(1)).
                executeHelmDeleteCommand(eq("namespace"), eq("descriptiveDeploymentId"));
    }

    @Test
    public void shouldNotDeleteServiceSinceNotExists() {
        when(namespaceService.namespace("domain")).thenReturn("namespace");
        when(helmCommandExecutor.executeHelmListCommand("namespace")).thenReturn(Collections.singletonList("otherString"));

        manager.deleteServiceIfExists(deploymentId);
        verify(helmCommandExecutor, times(0))
                .executeHelmDeleteCommand(eq("namespace"), any());
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
