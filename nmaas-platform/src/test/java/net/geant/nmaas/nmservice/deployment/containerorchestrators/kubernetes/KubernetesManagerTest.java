package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import com.google.common.collect.Sets;
import net.geant.nmaas.externalservices.inventory.gitlab.GitLabManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterDeploymentManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterIngressManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressControllerConfigOption;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster.DefaultKClusterValidator;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster.DefaultKServiceOperationsManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster.KClusterCheckException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmKServiceManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress.DefaultIngressControllerManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress.DefaultIngressResourceManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.janitor.JanitorResponseException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.janitor.JanitorService;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesChart;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethod;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceStorageVolumeType;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerOrchestratorInternalErrorException;
import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceRequestVerificationException;
import net.geant.nmaas.orchestration.AppUiAccessDetails;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppAccessMethod;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.AppStorageVolume;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class KubernetesManagerTest {

    private KubernetesRepositoryManager repositoryManager = mock(KubernetesRepositoryManager.class);
    private DefaultKClusterValidator clusterValidator = mock(DefaultKClusterValidator.class);
    private KServiceLifecycleManager serviceLifecycleManager = mock(HelmKServiceManager.class);
    private KServiceOperationsManager serviceOperationsManager = mock(DefaultKServiceOperationsManager.class);
    private IngressControllerManager ingressControllerManager = mock(DefaultIngressControllerManager.class);
    private IngressResourceManager ingressResourceManager = mock(DefaultIngressResourceManager.class);
    private KClusterIngressManager ingressManager = mock(KClusterIngressManager.class);
    private KClusterDeploymentManager deploymentManager = mock(KClusterDeploymentManager.class);
    private GitLabManager gitLabManager = mock(GitLabManager.class);
    private JanitorService janitorService = mock(JanitorService.class);

    private Identifier deploymentId = Identifier.newInstance("deploymentId");

    private KubernetesManager manager = new KubernetesManager(
            repositoryManager,
            clusterValidator,
            serviceLifecycleManager,
            serviceOperationsManager,
            ingressControllerManager,
            ingressResourceManager,
            ingressManager,
            deploymentManager,
            gitLabManager,
            janitorService
    );

    @BeforeEach
    public void setup() {
        when(deploymentManager.getSMTPServerHostname()).thenReturn("hostname");
        when(deploymentManager.getSMTPServerPort()).thenReturn(5);
        when(deploymentManager.getSMTPServerUsername()).thenReturn(Optional.of("username"));
        when(deploymentManager.getSMTPServerPassword()).thenReturn(Optional.of("password"));
        when(ingressManager.getExternalServiceDomain()).thenReturn("extBaseUrl");

        KubernetesNmServiceInfo service = new KubernetesNmServiceInfo();
        service.setDomain("domain");
        service.setDescriptiveDeploymentId(Identifier.newInstance("deploymentId"));
        Set<ServiceAccessMethod> accessMethods = new HashSet<>();
        accessMethods.add(new ServiceAccessMethod(ServiceAccessMethodType.DEFAULT, "Default", null, "Web", null));
        accessMethods.add(new ServiceAccessMethod(ServiceAccessMethodType.EXTERNAL, "web-service", null, "Web", null));
        accessMethods.add(new ServiceAccessMethod(ServiceAccessMethodType.INTERNAL, "ssh-service", null, "SSH", null));
        service.setAccessMethods(accessMethods);
        service.setKubernetesTemplate(new KubernetesTemplate(new KubernetesChart(null, null), null, "subcomponent"));
        when(repositoryManager.loadService(any())).thenReturn(service);
    }

    @Test
    public void shouldVerifyDeploymentWithEmptyAppDeployment() {
        NmServiceRequestVerificationException thrown = assertThrows(NmServiceRequestVerificationException.class, () -> {
            manager.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(deploymentId, null, null);
        });
        assertTrue(thrown.getMessage().contains("App deployment cannot be null"));
    }

    @Test
    public void shouldVerifyDeploymentWithEmptyAppDeploymentSpec() {
        NmServiceRequestVerificationException thrown = assertThrows(NmServiceRequestVerificationException.class, () -> {
            manager.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(deploymentId, new AppDeployment(), null);
        });
        assertTrue(thrown.getMessage().contains("App deployment spec cannot be null"));
    }

    @Test
    public void shouldVerifyDeploymentWithNotSupportedEnv() {
        AppDeploymentSpec spec = AppDeploymentSpec.builder().supportedDeploymentEnvironments(Lists.emptyList()).build();
        NmServiceRequestVerificationException thrown = assertThrows(NmServiceRequestVerificationException.class, () -> {
            manager.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(deploymentId, new AppDeployment(), spec);
        });
        assertTrue(thrown.getMessage().contains("Service deployment not possible with currently used container orchestrator"));
    }

    @Test
    public void shouldVerifyDeploymentWithNoKubernetesTemplate() {
        AppDeploymentSpec spec = AppDeploymentSpec.builder().supportedDeploymentEnvironments(Collections.singletonList(AppDeploymentEnv.KUBERNETES)).build();
        NmServiceRequestVerificationException thrown = assertThrows(NmServiceRequestVerificationException.class, () -> {
            manager.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(deploymentId, new AppDeployment(), spec);
        });
        assertTrue(thrown.getMessage().contains("Kubernetes template cannot be null"));
    }

    @Test
    public void shouldVerifyDeploymentWithNoServiceStorageVolumes() {
        AppDeploymentSpec spec = AppDeploymentSpec.builder()
                .supportedDeploymentEnvironments(Collections.singletonList(AppDeploymentEnv.KUBERNETES))
                .kubernetesTemplate(new KubernetesTemplate())
                .build();
        NmServiceRequestVerificationException thrown = assertThrows(NmServiceRequestVerificationException.class, () -> {
            manager.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(deploymentId, new AppDeployment(), spec);
        });
        assertTrue(thrown.getMessage().contains("Service storage volumes cannot be null"));
    }

    @Test
    public void shouldVerifyDeploymentWithNoServiceAccessMethods() {
        AppDeploymentSpec spec = AppDeploymentSpec.builder()
                .supportedDeploymentEnvironments(Collections.singletonList(AppDeploymentEnv.KUBERNETES))
                .kubernetesTemplate(new KubernetesTemplate())
                .storageVolumes(Sets.newHashSet(new AppStorageVolume(ServiceStorageVolumeType.MAIN, 2, null)))
                .build();
        NmServiceRequestVerificationException thrown = assertThrows(NmServiceRequestVerificationException.class, () -> {
            manager.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(deploymentId, new AppDeployment(), spec);
        });
        assertTrue(thrown.getMessage().contains("Service access methods cannot be null"));
    }

    @Test
    public void shouldVerifyDeploymentAndCreateServiceInfo() {
        AppDeployment deployment = AppDeployment.builder().deploymentId(deploymentId).deploymentName("deploymentName").build();
        AppDeploymentSpec spec = AppDeploymentSpec.builder()
                .supportedDeploymentEnvironments(Collections.singletonList(AppDeploymentEnv.KUBERNETES))
                .kubernetesTemplate(new KubernetesTemplate("chartName", "chartVersion", null))
                .storageVolumes(Sets.newHashSet(new AppStorageVolume(ServiceStorageVolumeType.MAIN, 2, null)))
                .accessMethods(Sets.newHashSet(new AppAccessMethod(ServiceAccessMethodType.EXTERNAL, "name", "tag", null)))
                .build();
        ArgumentCaptor<KubernetesNmServiceInfo> serviceInfo = ArgumentCaptor.forClass(KubernetesNmServiceInfo.class);

        manager.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(deploymentId, deployment, spec);
        verify(repositoryManager, times(1)).storeService(serviceInfo.capture());
        assertEquals(deploymentId, serviceInfo.getValue().getDeploymentId());
        assertEquals("deploymentName", serviceInfo.getValue().getDeploymentName());
        assertEquals("chartName", serviceInfo.getValue().getKubernetesTemplate().getChart().getName());
        Optional<ServiceAccessMethod> accessMethod = serviceInfo.getValue().getAccessMethods().stream().findFirst();
        assertTrue(accessMethod.isPresent());
        assertTrue(accessMethod.get().isOfType(ServiceAccessMethodType.EXTERNAL));
        assertEquals("tag", accessMethod.get().getName());
        assertNull(accessMethod.get().getUrl());
        assertNotNull(serviceInfo.getValue().getAdditionalParameters());
        assertTrue(serviceInfo.getValue().getAdditionalParameters().isEmpty());
    }

    @Test
    public void shouldVerifyDeploymentAndCreateServiceInfoWithAdditionalParameters() {
        AppDeployment deployment = AppDeployment.builder().deploymentId(deploymentId).domain("domain").build();
        AppDeploymentSpec spec = AppDeploymentSpec.builder()
                .supportedDeploymentEnvironments(Collections.singletonList(AppDeploymentEnv.KUBERNETES))
                .kubernetesTemplate(new KubernetesTemplate("chartName", "chartVersion", null))
                .accessMethods(Sets.newHashSet(new AppAccessMethod(ServiceAccessMethodType.EXTERNAL, "name", "tag", null)))
                .storageVolumes(Sets.newHashSet(new AppStorageVolume(ServiceStorageVolumeType.MAIN, 2, null)))
                .globalDeployParameters(getStringStringMap())
                .deployParameters(getParameterTypeStringMap())
                .build();
        ArgumentCaptor<KubernetesNmServiceInfo> serviceInfo = ArgumentCaptor.forClass(KubernetesNmServiceInfo.class);

        manager.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(deploymentId, deployment, spec);
        verify(repositoryManager, times(1)).storeService(serviceInfo.capture());
        assertEquals(deploymentId, serviceInfo.getValue().getDeploymentId());
        assertNotNull(serviceInfo.getValue().getAdditionalParameters());
        assertEquals(8, serviceInfo.getValue().getAdditionalParameters().size());
        assertEquals("customvalue1", serviceInfo.getValue().getAdditionalParameters().get("customkey1"));
        assertEquals("customvalue2", serviceInfo.getValue().getAdditionalParameters().get("customkey2"));
        assertEquals("hostname", serviceInfo.getValue().getAdditionalParameters().get("smtpHostname"));
        assertEquals("5", serviceInfo.getValue().getAdditionalParameters().get("smtpPort"));
        assertEquals("username", serviceInfo.getValue().getAdditionalParameters().get("smtpUsername"));
        assertEquals("password", serviceInfo.getValue().getAdditionalParameters().get("smtpPassword"));
        assertEquals("domain", serviceInfo.getValue().getAdditionalParameters().get("domainCodeName"));
        assertEquals("extBaseUrl", serviceInfo.getValue().getAdditionalParameters().get("baseUrl"));
    }

    private Map<String, String> getStringStringMap() {
        Map<String, String> globalDeployParameters = new HashMap<>();
        globalDeployParameters.put("customkey1", "customvalue1");
        globalDeployParameters.put("customkey2", "customvalue2");
        return globalDeployParameters;
    }

    private Map<ParameterType, String> getParameterTypeStringMap() {
        Map<ParameterType, String> deployParameters = new HashMap<>();
        deployParameters.put(ParameterType.SMTP_HOSTNAME, "smtpHostname");
        deployParameters.put(ParameterType.SMTP_PORT, "smtpPort");
        deployParameters.put(ParameterType.SMTP_USERNAME, "smtpUsername");
        deployParameters.put(ParameterType.SMTP_PASSWORD, "smtpPassword");
        deployParameters.put(ParameterType.DOMAIN_CODENAME, "domainCodeName");
        deployParameters.put(ParameterType.BASE_URL, "baseUrl");
        return deployParameters;
    }

    @Test
    public void shouldVerifyRequest () {
        assertDoesNotThrow(() -> {
            manager.verifyRequestAndObtainInitialDeploymentDetails(deploymentId);
        });
    }

    @Test
    public void shouldVerifyRequestAndThrowException () {
        assertThrows(ContainerOrchestratorInternalErrorException.class, () -> {
            doThrow(new KClusterCheckException("")).when(clusterValidator).checkClusterStatusAndPrerequisites();
            manager.verifyRequestAndObtainInitialDeploymentDetails(deploymentId);
        });
    }

    @Test
    public void shouldPrepareDeploymentEnvironmentWithNoRepo() {
        when(ingressManager.getControllerConfigOption()).thenReturn(IngressControllerConfigOption.USE_EXISTING);
        manager.prepareDeploymentEnvironment(deploymentId, false);
        verifyNoMoreInteractions(gitLabManager);
    }

    @Test
    public void shouldPrepareDeploymentEnvironmentWithRepo() {
        when(ingressManager.getControllerConfigOption()).thenReturn(IngressControllerConfigOption.USE_EXISTING);
        manager.prepareDeploymentEnvironment(deploymentId, true);
        verify(gitLabManager, times(1)).validateGitLabInstance();
    }

    @Test
    public void shouldTriggerServiceDeployment() {
        when(ingressResourceManager.generateServiceExternalURL("domain", null, null, false)).thenReturn("base.url");
        manager.deployNmService(deploymentId);
        ArgumentCaptor<Set<ServiceAccessMethod>> accessMethodsArg = ArgumentCaptor.forClass(HashSet.class);
        verify(repositoryManager, times(1)).updateKServiceAccessMethods(accessMethodsArg.capture());
        assertEquals(3, accessMethodsArg.getValue().size());
        assertTrue(accessMethodsArg.getValue().stream().anyMatch(m ->
                        m.isOfType(ServiceAccessMethodType.DEFAULT)
                        && m.getName().equals("Default")
                        && m.getProtocol().equals("Web")
                        && m.getUrl().equals("base.url")));
        assertTrue(accessMethodsArg.getValue().stream().anyMatch(m ->
                m.isOfType(ServiceAccessMethodType.EXTERNAL)
                        && m.getName().equals("web-service")
                        && m.getProtocol().equals("Web")
                        && m.getUrl().equals("web-service-base.url")));
        verify(serviceLifecycleManager, times(1)).deployService(deploymentId);
    }

    @Test
    public void shouldTriggerServiceRestart() {
        manager.restartNmService(deploymentId);
        verify(serviceOperationsManager, times(1)).restartService(deploymentId);
    }

    @Test
    public void shouldVerifyThatServiceIsDeployedAndUpdateServiceIp() {
        assertDoesNotThrow(() -> {
            when(serviceLifecycleManager.checkServiceDeployed(any(Identifier.class))).thenReturn(true);
            when(janitorService.checkIfReady(any(), any())).thenReturn(true);
            when(janitorService.retrieveServiceIp(any(), any())).thenReturn("192.168.100.1");

            manager.checkService(Identifier.newInstance("deploymentId"));

            ArgumentCaptor<Set<ServiceAccessMethod>> accessMethodsArg = ArgumentCaptor.forClass(HashSet.class);
            verify(repositoryManager, times(1)).updateKServiceAccessMethods(accessMethodsArg.capture());
            assertEquals(3, accessMethodsArg.getValue().size());
            assertTrue(accessMethodsArg.getValue().stream().anyMatch(m ->
                    m.isOfType(ServiceAccessMethodType.INTERNAL)
                            && m.getName().equals("ssh-service")
                            && m.getProtocol().equals("SSH")
                            && m.getUrl().equals("netops@192.168.100.1")));
        });
    }

    @Test
    public void shouldVerifyThatServiceIsDeployedWithoutServiceIp() {
        assertDoesNotThrow(() -> {
            when(serviceLifecycleManager.checkServiceDeployed(any(Identifier.class))).thenReturn(true);
            when(janitorService.checkIfReady(any(), any())).thenReturn(true);
            when(janitorService.retrieveServiceIp(any(), any())).thenThrow(new JanitorResponseException(""));

            manager.checkService(Identifier.newInstance("deploymentId"));

            verify(repositoryManager, times(0)).updateKServiceAccessMethods(any());
        });
    }

    @Test
    public void shouldReturnFalseSinceServiceNotDeployed() {
        when(serviceLifecycleManager.checkServiceDeployed(any(Identifier.class))).thenReturn(false);
        when(janitorService.checkIfReady(any(), any())).thenReturn(false);
        assertFalse(manager.checkService(Identifier.newInstance("deploymentId")));
    }

    @Test
    public void shouldRemoveService() {
        manager.removeNmService(deploymentId);
        verify(serviceLifecycleManager, times(1)).deleteServiceIfExists(deploymentId);
        verify(janitorService, times(1)).deleteConfigMapIfExists(Identifier.newInstance("deploymentId"), "domain");
        verify(janitorService, times(1)).deleteBasicAuthIfExists(Identifier.newInstance("deploymentId"), "domain");
        verify(janitorService, times(1)).deleteTlsIfExists(Identifier.newInstance("deploymentId"), "domain");
        verifyNoMoreInteractions(ingressResourceManager);
    }

    @Test
    public void shouldRetrieveServiceAccessDetails() {
        KubernetesNmServiceInfo service = new KubernetesNmServiceInfo();
        Set<ServiceAccessMethod> accessMethods = new HashSet<>();
        accessMethods.add(new ServiceAccessMethod(ServiceAccessMethodType.EXTERNAL, "web-service", "app1.nmaas.eu", "Web", null));
        accessMethods.add(new ServiceAccessMethod(ServiceAccessMethodType.INTERNAL, "ssh-service", "192.168.1.1", "SSH", null));
        service.setAccessMethods(accessMethods);
        when(repositoryManager.loadService(deploymentId)).thenReturn(service);

        AppUiAccessDetails appUiAccessDetails = manager.serviceAccessDetails(deploymentId);
        assertEquals(2, appUiAccessDetails.getServiceAccessMethods().size());
        assertTrue(appUiAccessDetails.getServiceAccessMethods().stream().anyMatch(m ->
            m.getType().equals(ServiceAccessMethodType.EXTERNAL)
                    && m.getName().equals("web-service")
                    && m.getProtocol().equals("Web")
                    && m.getUrl().equals("app1.nmaas.eu")
        ));
        assertTrue(appUiAccessDetails.getServiceAccessMethods().stream().anyMatch(m ->
                m.getType().equals(ServiceAccessMethodType.INTERNAL)
                        && m.getName().equals("ssh-service")
                        && m.getProtocol().equals("SSH")
                        && m.getUrl().equals("192.168.1.1")
        ));
    }

}
