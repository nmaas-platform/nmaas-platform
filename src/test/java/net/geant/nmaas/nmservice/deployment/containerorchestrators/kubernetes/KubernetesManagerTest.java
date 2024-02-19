package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import com.google.common.collect.Sets;
import net.geant.nmaas.externalservices.gitlab.GitLabManager;
import net.geant.nmaas.externalservices.kubernetes.KubernetesClusterIngressManager;
import net.geant.nmaas.externalservices.kubernetes.model.IngressControllerConfigOption;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster.DefaultKClusterValidator;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster.DefaultKServiceOperationsManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster.KClusterCheckException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmChartIngressVariable;
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
import net.geant.nmaas.orchestration.entities.AppAccessMethod.ConditionType;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.AppStorageVolume;
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

    private final KubernetesRepositoryManager repositoryManager = mock(KubernetesRepositoryManager.class);
    private final KubernetesDeploymentParametersProvider deploymentParametersProvider = mock(KubernetesDeploymentParametersProvider.class);
    private final DefaultKClusterValidator clusterValidator = mock(DefaultKClusterValidator.class);
    private final KServiceLifecycleManager serviceLifecycleManager = mock(HelmKServiceManager.class);
    private final KServiceOperationsManager serviceOperationsManager = mock(DefaultKServiceOperationsManager.class);
    private final IngressControllerManager ingressControllerManager = mock(DefaultIngressControllerManager.class);
    private final IngressResourceManager ingressResourceManager = mock(DefaultIngressResourceManager.class);
    private final KubernetesClusterIngressManager ingressManager = mock(KubernetesClusterIngressManager.class);
    private final GitLabManager gitLabManager = mock(GitLabManager.class);
    private final JanitorService janitorService = mock(JanitorService.class);

    private final static Identifier DEPLOYMENT_ID = Identifier.newInstance("deploymentId");

    private final KubernetesManager manager = new KubernetesManager(
            repositoryManager,
            deploymentParametersProvider,
            clusterValidator,
            serviceLifecycleManager,
            serviceOperationsManager,
            ingressControllerManager,
            ingressResourceManager,
            ingressManager,
            gitLabManager,
            janitorService
    );

    @BeforeEach
    void setup() {
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(ParameterType.SMTP_HOSTNAME.name(), "hostname");
        parametersMap.put(ParameterType.SMTP_PORT.name(), "5");
        parametersMap.put(ParameterType.SMTP_USERNAME.name(), "username");
        parametersMap.put(ParameterType.SMTP_PASSWORD.name(), "password");
        parametersMap.put(ParameterType.BASE_URL.name(), "extBaseUrl");
        parametersMap.put(ParameterType.DOMAIN_CODENAME.name(), "domain");
        parametersMap.put(ParameterType.RELEASE_NAME.name(), "descriptiveDeploymentId");
        parametersMap.put(ParameterType.APP_INSTANCE_NAME.name(), "appInstanceName");
        when(deploymentParametersProvider.deploymentParameters(any())).thenReturn(parametersMap);

        KubernetesNmServiceInfo service = new KubernetesNmServiceInfo();
        service.setDomain("domain");
        service.setDeploymentName("DeploymentName");
        service.setDescriptiveDeploymentId(Identifier.newInstance("deploymentId"));
        Set<ServiceAccessMethod> accessMethods = new HashSet<>();
        accessMethods.add(new ServiceAccessMethod(ServiceAccessMethodType.DEFAULT, "Default", null, "Web", null));
        accessMethods.add(
                ServiceAccessMethod.builder()
                        .type(ServiceAccessMethodType.EXTERNAL)
                        .name("web-service")
                        .url(null)
                        .protocol("Web")
                        .condition("ws.enabled")
                        .enabled(true)
                        .build()
        );
        accessMethods.add(
                ServiceAccessMethod.builder()
                        .type(ServiceAccessMethodType.EXTERNAL)
                        .name("web-service-conditional-1")
                        .url(null)
                        .protocol("Web")
                        .condition("ws1.enabled")
                        .enabled(true)
                        .build()
        );
        accessMethods.add(
                ServiceAccessMethod.builder()
                        .type(ServiceAccessMethodType.EXTERNAL)
                        .name("web-service-conditional-2")
                        .url(null)
                        .protocol("Web")
                        .condition("ws2.enabled")
                        .enabled(true)
                        .build()
        );
        accessMethods.add(new ServiceAccessMethod(ServiceAccessMethodType.INTERNAL, "ssh-service", null, "SSH", null));
        Map<HelmChartIngressVariable, String> sshAccessDeploymentParameters = new HashMap<>();
        sshAccessDeploymentParameters.put(HelmChartIngressVariable.K8S_SERVICE_PORT, "22");
        accessMethods.add(new ServiceAccessMethod(ServiceAccessMethodType.INTERNAL, "ssh-service-with-port", null, "SSH", sshAccessDeploymentParameters));
        accessMethods.add(new ServiceAccessMethod(ServiceAccessMethodType.PUBLIC, "public-service", null, "Public",null));
        Map<HelmChartIngressVariable, String> dataAccessDeploymentParameters = new HashMap<>();
        dataAccessDeploymentParameters.put(HelmChartIngressVariable.K8S_SERVICE_SUFFIX, "component1");
        accessMethods.add(new ServiceAccessMethod(ServiceAccessMethodType.INTERNAL, "data-service", null, "DATA", dataAccessDeploymentParameters));
        service.setAccessMethods(accessMethods);
        service.setKubernetesTemplate(new KubernetesTemplate(new KubernetesChart(null, null), null, "subcomponent"));
        Map<String, String> additionalParameters = new HashMap<>();
        additionalParameters.put("ws1.enabled", "True");
        additionalParameters.put("ws2.enabled", "FALSE");
        service.setAdditionalParameters(additionalParameters);
        when(repositoryManager.loadService(any())).thenReturn(service);
    }

    @Test
    void shouldVerifyDeploymentWithEmptyAppDeployment() {
        NmServiceRequestVerificationException thrown = assertThrows(NmServiceRequestVerificationException.class, () -> {
            manager.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(DEPLOYMENT_ID, null, null);
        });
        assertTrue(thrown.getMessage().contains("App deployment cannot be null"));
    }

    @Test
    void shouldVerifyDeploymentWithEmptyAppDeploymentSpec() {
        NmServiceRequestVerificationException thrown = assertThrows(NmServiceRequestVerificationException.class, () -> {
            manager.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(DEPLOYMENT_ID, new AppDeployment(), null);
        });
        assertTrue(thrown.getMessage().contains("App deployment spec cannot be null"));
    }

    @Test
    void shouldVerifyDeploymentWithNotSupportedEnv() {
        AppDeploymentSpec spec = AppDeploymentSpec.builder().supportedDeploymentEnvironments(Collections.emptyList()).build();
        NmServiceRequestVerificationException thrown = assertThrows(NmServiceRequestVerificationException.class, () -> {
            manager.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(DEPLOYMENT_ID, new AppDeployment(), spec);
        });
        assertTrue(thrown.getMessage().contains("Service deployment not possible with currently used container orchestrator"));
    }

    @Test
    void shouldVerifyDeploymentWithNoKubernetesTemplate() {
        AppDeploymentSpec spec = AppDeploymentSpec.builder().supportedDeploymentEnvironments(Collections.singletonList(AppDeploymentEnv.KUBERNETES)).build();
        NmServiceRequestVerificationException thrown = assertThrows(NmServiceRequestVerificationException.class, () -> {
            manager.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(DEPLOYMENT_ID, new AppDeployment(), spec);
        });
        assertTrue(thrown.getMessage().contains("Kubernetes template cannot be null"));
    }

    @Test
    void shouldVerifyDeploymentWithNoServiceAccessMethods() {
        AppDeploymentSpec spec = AppDeploymentSpec.builder()
                .supportedDeploymentEnvironments(Collections.singletonList(AppDeploymentEnv.KUBERNETES))
                .kubernetesTemplate(new KubernetesTemplate())
                .storageVolumes(Sets.newHashSet(new AppStorageVolume(ServiceStorageVolumeType.MAIN, 2, null)))
                .build();
        NmServiceRequestVerificationException thrown = assertThrows(NmServiceRequestVerificationException.class, () -> {
            manager.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(DEPLOYMENT_ID, new AppDeployment(), spec);
        });
        assertTrue(thrown.getMessage().contains("Service access methods cannot be null"));
    }

    @Test
    void shouldVerifyDeploymentAndCreateServiceInfo() {
        AppDeployment deployment = AppDeployment.builder().deploymentId(DEPLOYMENT_ID).deploymentName("deploymentName").build();
        AppDeploymentSpec spec = AppDeploymentSpec.builder()
                .supportedDeploymentEnvironments(Collections.singletonList(AppDeploymentEnv.KUBERNETES))
                .kubernetesTemplate(new KubernetesTemplate("chartName", "chartVersion", null))
                .storageVolumes(Sets.newHashSet(new AppStorageVolume(ServiceStorageVolumeType.MAIN, 2, null)))
                .accessMethods(Sets.newHashSet(
                        AppAccessMethod.builder()
                                .type(ServiceAccessMethodType.EXTERNAL).name("name").tag("tag").conditionType(ConditionType.NONE).condition("redundant")
                                .build()))
                .build();
        ArgumentCaptor<KubernetesNmServiceInfo> serviceInfo = ArgumentCaptor.forClass(KubernetesNmServiceInfo.class);

        manager.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(DEPLOYMENT_ID, deployment, spec);
        verify(repositoryManager, times(1)).storeService(serviceInfo.capture());
        assertEquals(DEPLOYMENT_ID, serviceInfo.getValue().getDeploymentId());
        assertEquals("deploymentName", serviceInfo.getValue().getDeploymentName());
        assertEquals("chartName", serviceInfo.getValue().getKubernetesTemplate().getChart().getName());
        Optional<ServiceAccessMethod> accessMethod = serviceInfo.getValue().getAccessMethods().stream().findFirst();
        assertTrue(accessMethod.isPresent());
        assertTrue(accessMethod.get().isOfType(ServiceAccessMethodType.EXTERNAL));
        assertEquals("tag", accessMethod.get().getName());
        assertNull(accessMethod.get().getUrl());
        assertNull(accessMethod.get().getCondition());
        assertTrue(accessMethod.get().isEnabled());
        assertNotNull(serviceInfo.getValue().getAdditionalParameters());
        assertTrue(serviceInfo.getValue().getAdditionalParameters().isEmpty());
    }

    @Test
    void shouldVerifyDeploymentAndCreateServiceInfoWithAdditionalParameters() {
        AppDeployment deployment = AppDeployment.builder()
                .deploymentId(DEPLOYMENT_ID)
                .descriptiveDeploymentId(Identifier.newInstance("descriptiveDeploymentId"))
                .domain("domain")
                .deploymentName("appInstanceName")
                .build();
        AppDeploymentSpec spec = AppDeploymentSpec.builder()
                .supportedDeploymentEnvironments(Collections.singletonList(AppDeploymentEnv.KUBERNETES))
                .kubernetesTemplate(new KubernetesTemplate("chartName", "chartVersion", null))
                .accessMethods(Sets.newHashSet(
                        AppAccessMethod.builder()
                                .type(ServiceAccessMethodType.EXTERNAL).name("name").tag("tag").conditionType(ConditionType.DEPLOYMENT_PARAMETER).condition("valid")
                                .build()))
                .storageVolumes(Sets.newHashSet(new AppStorageVolume(ServiceStorageVolumeType.MAIN, 2, null)))
                .globalDeployParameters(getStringStringMap())
                .deployParameters(getParameterTypeStringMap())
                .build();
        ArgumentCaptor<KubernetesNmServiceInfo> serviceInfo = ArgumentCaptor.forClass(KubernetesNmServiceInfo.class);

        manager.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(DEPLOYMENT_ID, deployment, spec);

        verify(repositoryManager, times(1)).storeService(serviceInfo.capture());
        assertEquals(DEPLOYMENT_ID, serviceInfo.getValue().getDeploymentId());
        Optional<ServiceAccessMethod> accessMethod = serviceInfo.getValue().getAccessMethods().stream().findFirst();
        assertTrue(accessMethod.isPresent());
        assertEquals("valid", accessMethod.get().getCondition());
        assertTrue(accessMethod.get().isEnabled());
        assertNotNull(serviceInfo.getValue().getAdditionalParameters());
        assertEquals(19, serviceInfo.getValue().getAdditionalParameters().size());
        assertEquals("customvalue1", serviceInfo.getValue().getAdditionalParameters().get("customkey1"));
        assertEquals("customvalue2", serviceInfo.getValue().getAdditionalParameters().get("customkey2"));
        assertEquals(serviceInfo.getValue().getAdditionalParameters().get("customkey3"), serviceInfo.getValue().getAdditionalParameters().get("customkey5"));
        assertEquals(4, serviceInfo.getValue().getAdditionalParameters().get("customkey3").length());
        assertEquals(49, serviceInfo.getValue().getAdditionalParameters().get("customkey4").length());
        assertTrue(serviceInfo.getValue().getAdditionalParameters().get("customkey4").matches("beginning-(.*)-ending"));
        assertEquals("hostname", serviceInfo.getValue().getAdditionalParameters().get("smtpHostname"));
        assertEquals("5", serviceInfo.getValue().getAdditionalParameters().get("smtpPort"));
        assertEquals("username", serviceInfo.getValue().getAdditionalParameters().get("smtpUsername"));
        assertEquals("password", serviceInfo.getValue().getAdditionalParameters().get("smtpPassword"));
        assertEquals("domain", serviceInfo.getValue().getAdditionalParameters().get("domainCodeName"));
        assertEquals("extBaseUrl", serviceInfo.getValue().getAdditionalParameters().get("baseUrl"));
        assertEquals("descriptiveDeploymentId", serviceInfo.getValue().getAdditionalParameters().get("releaseName"));
        assertEquals("appInstanceName", serviceInfo.getValue().getAdditionalParameters().get("appInstanceName"));
        assertNotNull(serviceInfo.getValue().getAdditionalParameters().get("RANDOM_STRING_4"));
        assertNotNull(serviceInfo.getValue().getAdditionalParameters().get("RANDOM_STRING_32"));
        assertEquals(10, serviceInfo.getValue().getAdditionalParameters().get("customkey6").length());
        assertEquals(8, serviceInfo.getValue().getAdditionalParameters().get("customkey7").length());
        assertTrue(serviceInfo.getValue().getAdditionalParameters().get("customkey6").matches("[0-9a-f]+"));
        assertTrue(serviceInfo.getValue().getAdditionalParameters().get("customkey7").matches("\\d+"));
    }

    private Map<String, String> getStringStringMap() {
        Map<String, String> globalDeployParameters = new HashMap<>();
        globalDeployParameters.put("customkey1", "customvalue1");
        globalDeployParameters.put("customkey2", "customvalue2");
        globalDeployParameters.put("customkey3", "%RANDOM_STRING_4%");
        globalDeployParameters.put("customkey4", "beginning-%RANDOM_STRING_32%-ending");
        globalDeployParameters.put("customkey5", "%RANDOM_STRING_4%");
        globalDeployParameters.put("customkey6", "%RANDOM_HEX_10%");
        globalDeployParameters.put("customkey7", "%RANDOM_NUMBER_8%");
        return globalDeployParameters;
    }

    private Map<String, String> getParameterTypeStringMap() {
        Map<String, String> deployParameters = new HashMap<>();
        deployParameters.put(ParameterType.SMTP_HOSTNAME.toString(), "smtpHostname");
        deployParameters.put(ParameterType.SMTP_PORT.toString(), "smtpPort");
        deployParameters.put(ParameterType.SMTP_USERNAME.toString(), "smtpUsername");
        deployParameters.put(ParameterType.SMTP_PASSWORD.toString(), "smtpPassword");
        deployParameters.put(ParameterType.DOMAIN_CODENAME.toString(), "domainCodeName");
        deployParameters.put(ParameterType.BASE_URL.toString(), "baseUrl");
        deployParameters.put(ParameterType.RELEASE_NAME.toString(), "releaseName");
        deployParameters.put(ParameterType.APP_INSTANCE_NAME.toString(), "appInstanceName");
        return deployParameters;
    }

    @Test
    void shouldVerifyRequest () {
        assertDoesNotThrow(() -> {
            manager.verifyRequestAndObtainInitialDeploymentDetails(DEPLOYMENT_ID);
        });
    }

    @Test
    void shouldVerifyRequestAndThrowException () {
        assertThrows(ContainerOrchestratorInternalErrorException.class, () -> {
            doThrow(new KClusterCheckException("")).when(clusterValidator).checkClusterStatusAndPrerequisites();
            manager.verifyRequestAndObtainInitialDeploymentDetails(DEPLOYMENT_ID);
        });
    }

    @Test
    void shouldPrepareDeploymentEnvironmentWithNoRepo() {
        when(ingressManager.getControllerConfigOption()).thenReturn(IngressControllerConfigOption.USE_EXISTING);
        manager.prepareDeploymentEnvironment(DEPLOYMENT_ID, false);
        verifyNoMoreInteractions(gitLabManager);
    }

    @Test
    void shouldPrepareDeploymentEnvironmentWithRepo() {
        when(ingressManager.getControllerConfigOption()).thenReturn(IngressControllerConfigOption.USE_EXISTING);
        manager.prepareDeploymentEnvironment(DEPLOYMENT_ID, true);
        verify(gitLabManager, times(1)).validateGitLabInstance();
    }

    @Test
    void shouldTriggerServiceDeployment() {
        when(ingressResourceManager.generateServiceExternalURL("domain", "DeploymentName", null, false)).thenReturn("base.url");
        when(ingressManager.getPublicServiceDomain()).thenReturn("public-base.url");
        manager.deployNmService(DEPLOYMENT_ID);
        ArgumentCaptor<Set<ServiceAccessMethod>> accessMethodsArg = ArgumentCaptor.forClass(HashSet.class);
        verify(repositoryManager, times(1)).updateKServiceAccessMethods(accessMethodsArg.capture());
        assertEquals(8, accessMethodsArg.getValue().size());
        assertTrue(accessMethodsArg.getValue().stream().anyMatch(m ->
                        m.isOfType(ServiceAccessMethodType.DEFAULT)
                        && m.getName().equals("Default")
                        && m.getProtocol().equals("Web")
                        && m.getUrl().equals("base.url")));
        assertTrue(accessMethodsArg.getValue().stream().anyMatch(m ->
                m.isOfType(ServiceAccessMethodType.EXTERNAL)
                        && m.getName().equals("web-service")
                        && m.getProtocol().equals("Web")
                        && m.getUrl().equals("web-service-base.url")
                        && m.isEnabled()));
        assertTrue(accessMethodsArg.getValue().stream().anyMatch(m ->
                m.isOfType(ServiceAccessMethodType.EXTERNAL)
                        && m.getName().equals("web-service-conditional-1")
                        && m.getProtocol().equals("Web")
                        && m.getUrl().equals("web-service-conditional-1-base.url")
                        && m.isEnabled()));
        assertTrue(accessMethodsArg.getValue().stream().anyMatch(m ->
                m.isOfType(ServiceAccessMethodType.EXTERNAL)
                        && m.getName().equals("web-service-conditional-2")
                        && m.getProtocol().equals("Web")
                        && m.getUrl().equals("web-service-conditional-2-base.url")
                        && !m.isEnabled()));
        assertTrue(accessMethodsArg.getValue().stream().anyMatch(m ->
                m.isOfType(ServiceAccessMethodType.PUBLIC)
                        && m.getName().equals("public-service")
                        && m.getProtocol().equals("Public")
                        && m.getUrl().equals("deploymentname-domain.public-base.url")));
        verify(serviceLifecycleManager, times(1)).deployService(DEPLOYMENT_ID);
    }

    @Test
    void shouldTriggerServiceRestart() {
        manager.restartNmService(DEPLOYMENT_ID);
        verify(serviceOperationsManager, times(1)).restartService(DEPLOYMENT_ID);
    }

    @Test
    void shouldVerifyThatServiceIsDeployedAndUpdateServiceIp() {
        assertDoesNotThrow(() -> {
            when(serviceLifecycleManager.checkServiceDeployed(any(Identifier.class))).thenReturn(true);
            when(janitorService.checkIfReady(any(), any())).thenReturn(true);
            when(janitorService.retrieveServiceIp(Identifier.newInstance("deploymentId"),"domain"))
                    .thenReturn("192.168.100.1");
            when(janitorService.retrieveServiceIp(Identifier.newInstance("deploymentId-component1"),"domain"))
                    .thenReturn("192.168.100.2");
            doThrow(new JanitorResponseException("")).when(janitorService).checkServiceExists(any(), any());

            manager.checkService(Identifier.newInstance("deploymentId"));

            ArgumentCaptor<Set<ServiceAccessMethod>> accessMethodsArg = ArgumentCaptor.forClass(HashSet.class);
            verify(repositoryManager, times(2)).updateKServiceAccessMethods(accessMethodsArg.capture());
            assertEquals(8, accessMethodsArg.getValue().size());
            assertTrue(accessMethodsArg.getValue().stream().anyMatch(m ->
                    m.isOfType(ServiceAccessMethodType.INTERNAL)
                            && m.getName().equals("ssh-service")
                            && m.getProtocol().equals("SSH")
                            && m.getUrl().equals("netops@192.168.100.1")));
            assertTrue(accessMethodsArg.getValue().stream().anyMatch(m ->
                    m.isOfType(ServiceAccessMethodType.INTERNAL)
                            && m.getName().equals("ssh-service-with-port")
                            && m.getProtocol().equals("SSH")
                            && m.getUrl().equals("netops@192.168.100.1 (port: 22)")));
            assertTrue(accessMethodsArg.getValue().stream().anyMatch(m ->
                    m.isOfType(ServiceAccessMethodType.INTERNAL)
                            && m.getName().equals("data-service")
                            && m.getProtocol().equals("DATA")
                            && m.getUrl().equals("192.168.100.2")));
        });
    }

    @Test
    void shouldVerifyThatServiceIsDeployedWithoutServiceIp() {
        assertDoesNotThrow(() -> {
            when(serviceLifecycleManager.checkServiceDeployed(any(Identifier.class))).thenReturn(true);
            when(janitorService.checkIfReady(any(), any())).thenReturn(true);
            when(janitorService.retrieveServiceIp(any(), any())).thenThrow(new JanitorResponseException(""));
            doThrow(new JanitorResponseException("")).when(janitorService).checkServiceExists(any(), any());

            manager.checkService(Identifier.newInstance("deploymentId"));

            verify(repositoryManager, times(1)).updateKServiceAccessMethods(any());
        });
    }

    @Test
    void shouldReturnFalseSinceServiceNotDeployed() {
        when(serviceLifecycleManager.checkServiceDeployed(any(Identifier.class))).thenReturn(false);
        when(janitorService.checkIfReady(any(), any())).thenReturn(false);
        assertFalse(manager.checkService(Identifier.newInstance("deploymentId")));
    }

    @Test
    void shouldRemoveService() {
        manager.removeNmService(DEPLOYMENT_ID);
        verify(serviceLifecycleManager, times(1)).deleteServiceIfExists(DEPLOYMENT_ID);
        verify(janitorService, times(1)).deleteConfigMapIfExists(Identifier.newInstance("deploymentId"), "domain");
        verify(janitorService, times(1)).deleteBasicAuthIfExists(Identifier.newInstance("deploymentId"), "domain");
        verify(janitorService, times(1)).deleteTlsIfExists(Identifier.newInstance("deploymentId"), "domain");
        verifyNoMoreInteractions(ingressResourceManager);
    }

    @Test
    void shouldRetrieveServiceAccessDetails() {
        KubernetesNmServiceInfo service = new KubernetesNmServiceInfo();
        Set<ServiceAccessMethod> accessMethods = new HashSet<>();
        accessMethods.add(new ServiceAccessMethod(ServiceAccessMethodType.EXTERNAL, "web-service", "app1.nmaas.eu", "Web", null));
        accessMethods.add(ServiceAccessMethod.builder()
                .type(ServiceAccessMethodType.EXTERNAL)
                .name("web-service-disabled")
                .url("app2.nmaas.eu")
                .protocol("Web")
                .enabled(false)
                .build());
        accessMethods.add(new ServiceAccessMethod(ServiceAccessMethodType.INTERNAL, "ssh-service", "192.168.1.1", "SSH", null));
        service.setAccessMethods(accessMethods);
        when(repositoryManager.loadService(DEPLOYMENT_ID)).thenReturn(service);

        AppUiAccessDetails appUiAccessDetails = manager.serviceAccessDetails(DEPLOYMENT_ID);

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
