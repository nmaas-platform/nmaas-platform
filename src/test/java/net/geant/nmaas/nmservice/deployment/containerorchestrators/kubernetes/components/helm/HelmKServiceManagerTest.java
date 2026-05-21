package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import net.geant.nmaas.kubernetes.KubernetesClusterDeploymentManager;
import net.geant.nmaas.kubernetes.KubernetesClusterIngressManager;
import net.geant.nmaas.kubernetes.KubernetesClusterNamespaceService;
import net.geant.nmaas.kubernetes.remote.entities.IngressCertificateConfigOption;
import net.geant.nmaas.kubernetes.remote.entities.IngressResourceConfigOption;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import net.geant.nmaas.kubernetes.remote.entities.KClusterDeployment;
import net.geant.nmaas.kubernetes.remote.entities.KClusterIngress;
import net.geant.nmaas.kubernetes.remote.entities.KClusterState;
import net.geant.nmaas.kubernetes.remote.entities.NamespaceConfigOption;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesChart;
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
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import java.time.OffsetDateTime;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class HelmKServiceManagerTest {

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
        Map<HelmChartIngressVariable, String> ivMapDefault = new HashMap<>();
        ivMapDefault.put(HelmChartIngressVariable.INGRESS_ENABLED, "ingress.enabled");
        ivMapDefault.put(HelmChartIngressVariable.INGRESS_CLASS, "ingress.class");
        ivMapDefault.put(HelmChartIngressVariable.INGRESS_HOSTS, "ingress.hosts");
        ivMapDefault.put(HelmChartIngressVariable.INGRESS_LETSENCRYPT, "ingress.tls.acme");
        ivMapDefault.put(HelmChartIngressVariable.INGRESS_TLS_ENABLED, "ingress.tls.enabled");
        ivMapDefault.put(HelmChartIngressVariable.INGRESS_WILDCARD_OR_ISSUER, "ingress.tls.certOrIssuer");
        accessMethods.add(new ServiceAccessMethod(ServiceAccessMethodType.DEFAULT, "Default", null, "Web", ivMapDefault));
        Map<HelmChartIngressVariable, String> ivMapExternal1 = new HashMap<>();
        ivMapExternal1.put(HelmChartIngressVariable.INGRESS_CLASS, "ingress-ex1.class");
        ivMapExternal1.put(HelmChartIngressVariable.INGRESS_HOSTS, "ingress-ex1.hosts");
        accessMethods.add(new ServiceAccessMethod(ServiceAccessMethodType.EXTERNAL, "web-service", null, "Web", ivMapExternal1));
        Map<HelmChartIngressVariable, String> ivMapExternal2 = new HashMap<>();
        ivMapExternal2.put(HelmChartIngressVariable.INGRESS_CLASS, "ingress-ex2.class");
        ivMapExternal2.put(HelmChartIngressVariable.INGRESS_HOSTS, "ingress-ex2.hosts");
        accessMethods.add(ServiceAccessMethod.builder()
                .type(ServiceAccessMethodType.EXTERNAL)
                .name("web-service-2")
                .url(null)
                .protocol("Web")
                .enabled(false)
                .deployParameters(ivMapExternal2)
                .build());
        accessMethods.add(new ServiceAccessMethod(ServiceAccessMethodType.INTERNAL, "ssh-service", null, "SSH", null));
        service.setStorageVolumes(storageVolumes);
        service.setAccessMethods(accessMethods);
        service.setDescriptiveDeploymentId(Identifier.newInstance("descriptiveDeploymentId"));
        service.setKubernetesTemplate(new KubernetesTemplate("chartName", "chartVersion", "archiveName"));
        service.setRemoteCluster(null);
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

        ArgumentCaptor<KubernetesTemplate> k8sTemplateArg = ArgumentCaptor.forClass(KubernetesTemplate.class);
        ArgumentCaptor<Map<String, String>> argumentsArg = ArgumentCaptor.forClass(HashMap.class);
        verify(helmCommandExecutor, times(1)).executeHelmRepoUpdateCommand();
        verify(helmCommandExecutor, times(1)).executeHelmInstallCommand(
                eq("namespace"),
                eq("descriptiveDeploymentId"),
                k8sTemplateArg.capture(),
                argumentsArg.capture(),
                eq(null)
        );
        assertThat(argumentsArg.getValue()).isNotEmpty();
        assertThat(argumentsArg.getValue().size()).isEqualTo(11);
    }

    @Test
    void shouldDeployServiceWithoutRepoUpdate() {
        when(namespaceService.namespace("domain")).thenReturn("namespace");
        when(ingressManager.getResourceConfigOption()).thenReturn(IngressResourceConfigOption.DEPLOY_FROM_CHART);
        when(ingressManager.getIngressPerDomain()).thenReturn(false);
        when(ingressManager.getSupportedIngressClass()).thenReturn("testIngressClass");
        when(ingressManager.getTlsSupported()).thenReturn(true);
        when(ingressManager.getIssuerOrWildcardName()).thenReturn("testIssuerName");
        when(ingressManager.getCertificateConfigOption()).thenReturn(IngressCertificateConfigOption.USE_LETSENCRYPT);
        manager.setHelmRepoUpdateAsyncEnabled(true);

        manager.deployService(deploymentId);

        verify(helmCommandExecutor, times(0)).executeHelmRepoUpdateCommand();
        verify(helmCommandExecutor, times(1)).executeHelmInstallCommand(
                eq("namespace"),
                eq("descriptiveDeploymentId"),
                any(KubernetesTemplate.class),
                any(Map.class),
                eq(null)
        );
    }

    @Test
    void shouldRetrieveServiceExternalAccessMethods() {
        Set<ServiceAccessMethod> allAccessMethods = new HashSet<>();
        allAccessMethods.add(ServiceAccessMethod.builder().type(ServiceAccessMethodType.EXTERNAL).enabled(true).build());
        allAccessMethods.add(ServiceAccessMethod.builder().type(ServiceAccessMethodType.EXTERNAL).enabled(false).build());
        allAccessMethods.add(new ServiceAccessMethod(ServiceAccessMethodType.EXTERNAL, "web-service", null, "Web", null));
        assertThat(HelmKServiceManager.serviceExternalAccessMethods(allAccessMethods).size()).isEqualTo(2);
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
        when(helmCommandExecutor.executeHelmStatusCommand("namespace", "descriptiveDeploymentId", null))
                .thenReturn(HelmPackageStatus.DEPLOYED);

        boolean status = manager.checkServiceDeployed(deploymentId);
        verify(helmCommandExecutor, times(1))
                .executeHelmStatusCommand(eq("namespace"), eq("descriptiveDeploymentId"), ArgumentMatchers.isNull());
        assertTrue(status);
    }

    @Test
    void shouldCheckServiceDeployedFalse() {
        when(namespaceService.namespace("domain")).thenReturn("namespace");
        when(helmCommandExecutor.executeHelmStatusCommand("namespace", "descriptiveDeploymentId", null)).
                thenReturn(HelmPackageStatus.UNKNOWN);

        boolean status = manager.checkServiceDeployed(deploymentId);
        verify(helmCommandExecutor, times(1))
                .executeHelmStatusCommand(eq("namespace"), eq("descriptiveDeploymentId"), ArgumentMatchers.isNull());
        assertFalse(status);
    }

    @Test
    void shouldCheckRemoteServiceDeployedUsingRemoteNamespaceAndKubeconfigPath() {
        KCluster remoteCluster = remoteCluster();
        KubernetesNmServiceInfo service = repositoryManager.loadService(deploymentId);
        service.setRemoteCluster(remoteCluster);
        when(repositoryManager.loadService(deploymentId)).thenReturn(service);
        when(namespaceService.namespace(remoteCluster, "domain")).thenReturn("remote-namespace");
        when(helmCommandExecutor.executeHelmStatusCommand("remote-namespace", "descriptiveDeploymentId", "mock/path/to/kubeconfig"))
                .thenReturn(HelmPackageStatus.DEPLOYED);

        boolean status = manager.checkServiceDeployed(deploymentId);

        verify(namespaceService, times(1)).namespace(remoteCluster, "domain");
        verify(namespaceService, never()).namespace("domain");
        verify(helmCommandExecutor, times(1))
                .executeHelmStatusCommand(eq("remote-namespace"), eq("descriptiveDeploymentId"), eq("mock/path/to/kubeconfig"));
        assertTrue(status);
    }

    @Test
    void shouldDeployOciServiceWithoutRepoUpdate() {
        when(namespaceService.namespace("domain")).thenReturn("namespace");
        when(ingressManager.getResourceConfigOption()).thenReturn(IngressResourceConfigOption.DEPLOY_FROM_CHART);
        when(ingressManager.getIngressPerDomain()).thenReturn(false);
        when(ingressManager.getSupportedIngressClass()).thenReturn("testIngressClass");
        when(ingressManager.getTlsSupported()).thenReturn(true);
        when(ingressManager.getIssuerOrWildcardName()).thenReturn("testIssuerName");
        when(ingressManager.getCertificateConfigOption()).thenReturn(IngressCertificateConfigOption.USE_LETSENCRYPT);
        KubernetesNmServiceInfo service = repositoryManager.loadService(deploymentId);
        service.getKubernetesTemplate().getChart().setName("oci://registry-1.docker.io/bitnamicharts/postgresql");
        when(repositoryManager.loadService(deploymentId)).thenReturn(service);

        manager.deployService(deploymentId);

        verify(helmCommandExecutor, never()).executeHelmRepoUpdateCommand();
        verify(helmCommandExecutor, times(1)).executeHelmInstallCommand(
                eq("namespace"),
                eq("descriptiveDeploymentId"),
                any(KubernetesTemplate.class),
                any(Map.class),
                eq(null)
        );
    }

    @Test
    void shouldDeleteServiceSinceExists() {
        when(namespaceService.namespace("domain")).thenReturn("namespace");
        when(helmCommandExecutor.executeHelmListCommand("namespace", null))
                .thenReturn(Arrays.asList("descriptiveDeploymentId", "otherString"));

        manager.deleteServiceIfExists(deploymentId);
        verify(helmCommandExecutor, times(1)).
                executeHelmDeleteCommand(eq("namespace"), eq("descriptiveDeploymentId"), isNull());
    }

    @Test
    void shouldNotDeleteServiceSinceNotExists() {
        when(namespaceService.namespace("domain")).thenReturn("namespace");
        when(helmCommandExecutor.executeHelmListCommand("namespace", null))
                .thenReturn(Collections.singletonList("otherString"));

        manager.deleteServiceIfExists(deploymentId);
        verify(helmCommandExecutor, times(0))
                .executeHelmDeleteCommand(eq("namespace"), any(), isNull());
    }

    @Test
    void shouldDeleteRemoteServiceSinceExistsUsingRemoteNamespaceAndKubeconfigPath() {
        KCluster remoteCluster = remoteCluster();
        KubernetesNmServiceInfo service = repositoryManager.loadService(deploymentId);
        service.setRemoteCluster(remoteCluster);
        when(repositoryManager.loadService(deploymentId)).thenReturn(service);
        when(namespaceService.namespace(remoteCluster, "domain")).thenReturn("remote-namespace");
        when(helmCommandExecutor.executeHelmListCommand("remote-namespace", "mock/path/to/kubeconfig"))
                .thenReturn(Arrays.asList("descriptiveDeploymentId", "otherString"));

        manager.deleteServiceIfExists(deploymentId);

        verify(namespaceService, times(1)).namespace(remoteCluster, "domain");
        verify(namespaceService, never()).namespace("domain");
        verify(helmCommandExecutor, times(1))
                .executeHelmDeleteCommand(eq("remote-namespace"), eq("descriptiveDeploymentId"), eq("mock/path/to/kubeconfig"));
    }

    @Test
    void shouldUpgradeService() {
        when(namespaceService.namespace("domain")).thenReturn("namespace");
        manager.upgradeService(deploymentId, new KubernetesTemplate());
        verify(helmCommandExecutor, times(1)).executeHelmRepoUpdateCommand();
        verify(helmCommandExecutor, times(1)).executeHelmUpgradeCommand(
                eq("namespace"),
                eq("descriptiveDeploymentId"),
                any(KubernetesTemplate.class),
                isNull()
        );
    }

    @Test
    void shouldUpgradeRemoteServiceUsingRemoteNamespaceAndKubeconfigPath() {
        KCluster remoteCluster = remoteCluster();
        KubernetesNmServiceInfo service = repositoryManager.loadService(deploymentId);
        service.setRemoteCluster(remoteCluster);
        when(repositoryManager.loadService(deploymentId)).thenReturn(service);
        when(namespaceService.namespace(remoteCluster, "domain")).thenReturn("remote-namespace");

        manager.upgradeService(deploymentId, new KubernetesTemplate());

        verify(namespaceService, times(1)).namespace(remoteCluster, "domain");
        verify(namespaceService, never()).namespace("domain");
        verify(helmCommandExecutor, times(1)).executeHelmRepoUpdateCommand();
        verify(helmCommandExecutor, times(1)).executeHelmUpgradeCommand(
                eq("remote-namespace"),
                eq("descriptiveDeploymentId"),
                any(KubernetesTemplate.class),
                eq("mock/path/to/kubeconfig")
        );
    }

    @Test
    void shouldUpgradeOciServiceWithoutRepoUpdate() {
        when(namespaceService.namespace("domain")).thenReturn("namespace");
        KubernetesNmServiceInfo service = repositoryManager.loadService(deploymentId);
        when(repositoryManager.loadService(deploymentId)).thenReturn(service);
        KubernetesTemplate targetTemplate = new KubernetesTemplate();
        KubernetesChart chart = new KubernetesChart("oci://registry-1.docker.io/bitnamicharts/postgresql", null);
        targetTemplate.setChart(chart);

        manager.upgradeService(deploymentId, targetTemplate);

        verify(helmCommandExecutor, never()).executeHelmRepoUpdateCommand();
        verify(helmCommandExecutor, times(1)).executeHelmUpgradeCommand(
                eq("namespace"),
                eq("descriptiveDeploymentId"),
                any(KubernetesTemplate.class),
                isNull()
        );
    }

    @Test
    void shouldDeployServiceWithKubeconfigPath() {
        when(ingressManager.getResourceConfigOption()).thenReturn(IngressResourceConfigOption.DEPLOY_FROM_CHART);
        when(ingressManager.getIngressPerDomain()).thenReturn(false);
        when(ingressManager.getSupportedIngressClass()).thenReturn("testIngressClass");
        when(ingressManager.getTlsSupported()).thenReturn(true);
        when(ingressManager.getIssuerOrWildcardName()).thenReturn("testIssuerName");
        when(ingressManager.getCertificateConfigOption()).thenReturn(IngressCertificateConfigOption.USE_LETSENCRYPT);

        KClusterDeployment mockDeployment = KClusterDeployment.builder()
                .namespaceConfigOption(NamespaceConfigOption.CREATE_NAMESPACE)
                .defaultNamespace("default-namespace")
                .defaultStorageClass("default-storage-class")
                .smtpServerHostname("smtp.example.com")
                .smtpServerPort(587)
                .smtpServerUsername("user@example.com")
                .smtpServerPassword("password")
                .smtpFromDefaultDomain("example.com")
                .forceDedicatedWorkers(true)
                .build();

        KClusterIngress mockIngress = KClusterIngress.builder().resourceConfigOption(IngressResourceConfigOption.NOT_USED).build();

        KCluster mockCluster = KCluster.builder()
                .id(1L)
                .name("TestCluster")
                .codename("test-cluster")
                .description("Mocked Kubernetes Cluster")
                .creationDate(OffsetDateTime.now())
                .modificationDate(OffsetDateTime.now())
                .clusterConfigFile("mock-cluster-config")
                .pathConfigFile("mock/path/to/kubeconfig")
                .state(KClusterState.UNKNOWN)
                .currentStateSince(OffsetDateTime.now())
                .contactEmail("test@example.com")
                .deployment(mockDeployment)
                .ingress(mockIngress)
                .build();

        KubernetesNmServiceInfo service = repositoryManager.loadService(deploymentId);
        service.setRemoteCluster(mockCluster);
        when(repositoryManager.loadService(deploymentId)).thenReturn(service);
        when(namespaceService.namespace(mockCluster, "domain")).thenReturn("remote-namespace");

        manager.deployService(deploymentId);

        ArgumentCaptor<String> kubeconfigPathArg = ArgumentCaptor.forClass(String.class);
        verify(namespaceService, times(1)).namespace(mockCluster, "domain");
        verify(namespaceService, never()).namespace("domain");
        verify(helmCommandExecutor, times(1)).executeHelmInstallCommand(
                eq("remote-namespace"),
                eq("descriptiveDeploymentId"),
                any(KubernetesTemplate.class),
                any(Map.class),
                kubeconfigPathArg.capture()
        );
        assertThat(kubeconfigPathArg.getValue()).isEqualTo("mock/path/to/kubeconfig");
    }

    private KCluster remoteCluster() {
        KClusterDeployment mockDeployment = KClusterDeployment.builder()
                .namespaceConfigOption(NamespaceConfigOption.CREATE_NAMESPACE)
                .defaultNamespace("default-namespace")
                .defaultStorageClass("default-storage-class")
                .smtpServerHostname("smtp.example.com")
                .smtpServerPort(587)
                .smtpServerUsername("user@example.com")
                .smtpServerPassword("password")
                .smtpFromDefaultDomain("example.com")
                .forceDedicatedWorkers(true)
                .build();

        KClusterIngress mockIngress = KClusterIngress.builder()
                .resourceConfigOption(IngressResourceConfigOption.NOT_USED)
                .build();

        return KCluster.builder()
                .id(1L)
                .name("TestCluster")
                .codename("test-cluster")
                .description("Mocked Kubernetes Cluster")
                .creationDate(OffsetDateTime.now())
                .modificationDate(OffsetDateTime.now())
                .clusterConfigFile("mock-cluster-config")
                .pathConfigFile("mock/path/to/kubeconfig")
                .state(KClusterState.UNKNOWN)
                .currentStateSince(OffsetDateTime.now())
                .contactEmail("test@example.com")
                .deployment(mockDeployment)
                .ingress(mockIngress)
                .build();
    }

}
