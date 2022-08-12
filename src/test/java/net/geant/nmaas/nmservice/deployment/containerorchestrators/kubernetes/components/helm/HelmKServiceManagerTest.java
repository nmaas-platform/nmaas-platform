package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import net.geant.nmaas.externalservices.kubernetes.KubernetesClusterDeploymentManager;
import net.geant.nmaas.externalservices.kubernetes.KubernetesClusterIngressManager;
import net.geant.nmaas.externalservices.kubernetes.KubernetesClusterNamespaceService;
import net.geant.nmaas.externalservices.kubernetes.model.IngressCertificateConfigOption;
import net.geant.nmaas.externalservices.kubernetes.model.IngressResourceConfigOption;
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

import static org.assertj.core.api.Assertions.assertThat;
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

    private final Identifier deploymentId = Identifier.newInstance("deploymentId");

    private final KubernetesRepositoryManager repositoryManager = mock(KubernetesRepositoryManager.class);
    private final KubernetesClusterNamespaceService namespaceService = mock(KubernetesClusterNamespaceService.class);
    private final KubernetesClusterDeploymentManager deploymentManager = mock(KubernetesClusterDeploymentManager.class);
    private final KubernetesClusterIngressManager ingressManager = mock(KubernetesClusterIngressManager.class);
    private final HelmCommandExecutor helmCommandExecutor = mock(HelmCommandExecutor.class);
    private final DomainTechDetailsRepository domainTechDetailsRepository = mock(DomainTechDetailsRepository.class);

    private HelmKServiceManager manager;

    @BeforeEach
    void setup() {
        manager = new HelmKServiceManager(
                repositoryManager,
                namespaceService,
                deploymentManager,
                ingressManager,
                helmCommandExecutor,
                domainTechDetailsRepository);
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
    void shouldDeployService() {
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
    void shouldRemoveRedundantParametersFromMap() {
        Map<String, String> allParameters = new HashMap<>();
        allParameters.put("key1", "value1");
        allParameters.put("%RANDOM_STRING_8%", "value2");
        allParameters.put("key2", "value3");
        allParameters.put("accessmethods.public.method", "value4");

        Map<String, String> filteredParameters = HelmKServiceManager.removeRedundantParameters(allParameters);
        assertThat(filteredParameters)
                .hasSize(2)
                .containsEntry("key1", "value1")
                .containsEntry("key2", "value3");
    }

    @Test
    void shouldCheckServiceDeployedTrue() {
        when(namespaceService.namespace("domain")).thenReturn("namespace");
        when(helmCommandExecutor.executeHelmStatusCommand("namespace", "descriptiveDeploymentId"))
                .thenReturn(HelmPackageStatus.DEPLOYED);

        boolean status = manager.checkServiceDeployed(deploymentId);
        verify(helmCommandExecutor, times(1))
                .executeHelmStatusCommand(eq("namespace"), eq("descriptiveDeploymentId"));
        assertTrue(status);
    }

    @Test
    void shouldCheckServiceDeployedFalse() {
        when(namespaceService.namespace("domain")).thenReturn("namespace");
        when(helmCommandExecutor.executeHelmStatusCommand("namespace", "descriptiveDeploymentId")).
                thenReturn(HelmPackageStatus.UNKNOWN);

        boolean status = manager.checkServiceDeployed(deploymentId);
        verify(helmCommandExecutor, times(1))
                .executeHelmStatusCommand(eq("namespace"), eq("descriptiveDeploymentId"));
        assertFalse(status);
    }

    @Test
    void shouldDeleteServiceSinceExists() {
        when(namespaceService.namespace("domain")).thenReturn("namespace");
        when(helmCommandExecutor.executeHelmListCommand("namespace"))
                .thenReturn(Arrays.asList("descriptiveDeploymentId", "otherString"));

        manager.deleteServiceIfExists(deploymentId);
        verify(helmCommandExecutor, times(1)).
                executeHelmDeleteCommand(eq("namespace"), eq("descriptiveDeploymentId"));
    }

    @Test
    void shouldNotDeleteServiceSinceNotExists() {
        when(namespaceService.namespace("domain")).thenReturn("namespace");
        when(helmCommandExecutor.executeHelmListCommand("namespace")).thenReturn(Collections.singletonList("otherString"));

        manager.deleteServiceIfExists(deploymentId);
        verify(helmCommandExecutor, times(0))
                .executeHelmDeleteCommand(eq("namespace"), any());
    }

    @Test
    void shouldUpgradeService() {
        when(namespaceService.namespace("domain")).thenReturn("namespace");
        manager.upgradeService(deploymentId, new KubernetesTemplate());
        verify(helmCommandExecutor, times(1)).executeHelmRepoUpdateCommand();
        verify(helmCommandExecutor, times(1)).executeHelmUpgradeCommand(
                eq("namespace"),
                eq("descriptiveDeploymentId"),
                any(KubernetesTemplate.class)
        );
    }

}
