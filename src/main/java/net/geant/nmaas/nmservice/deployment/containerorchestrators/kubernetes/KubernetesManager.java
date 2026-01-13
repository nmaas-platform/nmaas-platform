package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.gitlab.GitLabManager;
import net.geant.nmaas.gitlab.exceptions.GitLabInvalidConfigurationException;
import net.geant.nmaas.kubernetes.JanitorException;
import net.geant.nmaas.kubernetes.KubernetesApiJanitorService;
import net.geant.nmaas.kubernetes.KubernetesClusterIngressManager;
import net.geant.nmaas.kubernetes.remote.RemoteClusterManagementService;
import net.geant.nmaas.kubernetes.remote.RemoteClusterMonitoringService;
import net.geant.nmaas.kubernetes.remote.entities.IngressControllerConfigOption;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import net.geant.nmaas.nmservice.deployment.ContainerOrchestrator;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster.KClusterCheckException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmChartIngressVariable;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress.IngressControllerManipulationException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethod;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceStorageVolume;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.exceptions.KServiceManipulationException;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerCheckFailedException;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerOrchestratorInternalErrorException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotDeployServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotPauseServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotPrepareEnvironmentException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRemoveServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRestartServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotResumeServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotUpgradeKubernetesServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.ServiceRequestVerificationException;
import net.geant.nmaas.orchestration.AppComponentDetails;
import net.geant.nmaas.orchestration.AppComponentLogs;
import net.geant.nmaas.orchestration.AppUiAccessDetails;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppAccessMethod;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.AppStorageVolume;
import net.geant.nmaas.orchestration.exceptions.InvalidConfigurationException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethod.DEFAULT_INTERNAL_SSH_ACCESS_USERNAME;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethod.copy;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType.EXTERNAL;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType.INTERNAL;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType.LOCAL;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType.PUBLIC;

/**
 * Implements service deployment mechanism on Kubernetes cluster.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KubernetesManager implements ContainerOrchestrator {

    public static final String RANDOM_ARGUMENT_EXPRESSION_PREFIX = "RANDOM_";
    public static final String PUBLIC_ACCESS_SELECTOR_ARGUMENT_EXPRESSION_PREFIX = "accessmethods.public.";

    private final KubernetesRepositoryManager repositoryManager;
    private final KubernetesDeploymentParametersProvider deploymentParametersProvider;
    private final KubernetesDeploymentRemoteClusterParametersProvider deploymentRemoteClusterParametersProvider;
    private final KClusterValidator clusterValidator;
    private final KServiceLifecycleManager serviceLifecycleManager;
    private final KServiceOperationsManager serviceOperationsManager;
    private final IngressControllerManager ingressControllerManager;
    private final IngressResourceManager ingressResourceManager;
    private final KubernetesClusterIngressManager ingressManager;
    private final GitLabManager gitLabManager;
    private final KubernetesApiJanitorService kubernetesApiJanitorService;
    private final RemoteClusterManagementService remoteClusterManager;
    private final RemoteClusterMonitoringService remoteClusterMonitor;

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(Identifier deploymentId, AppDeployment appDeployment, AppDeploymentSpec appDeploymentSpec) {
        try {
            Validate.isTrue(appDeployment != null, "App deployment cannot be null");
            Validate.isTrue(appDeploymentSpec != null, "App deployment spec cannot be null");
            Validate.isTrue(appDeploymentSpec.getSupportedDeploymentEnvironments().contains(AppDeploymentEnv.KUBERNETES),
                    "Service deployment not possible with currently used container orchestrator");
            Validate.isTrue(appDeploymentSpec.getKubernetesTemplate() != null, "Kubernetes template cannot be null");
            Validate.isTrue(appDeploymentSpec.getAccessMethods() != null && !appDeploymentSpec.getAccessMethods().isEmpty(),
                    "Service access methods cannot be null");
        } catch (IllegalArgumentException iae) {
            throw new ServiceRequestVerificationException(iae.getMessage());
        }

        if (Objects.nonNull(appDeployment.getRemoteClusterId())) {
            if (!remoteClusterManager.clusterExists(appDeployment.getRemoteClusterId())) {
                throw new ServiceRequestVerificationException(String.format("Remote cluster with id %s doesn't exist", appDeployment.getRemoteClusterId()));
            } else {
                if (!remoteClusterMonitor.clusterAvailable(appDeployment.getRemoteClusterId())) {
                    throw new ServiceRequestVerificationException(String.format("Remote cluster with id %s is currently unavailable", appDeployment.getRemoteClusterId()));
                }
            }
        }

        KubernetesNmServiceInfo serviceInfo = new KubernetesNmServiceInfo(
                deploymentId,
                appDeployment.getDeploymentName(),
                appDeployment.getDomain(),
                appDeployment.getDescriptiveDeploymentId()
        );
        if (Objects.nonNull(appDeployment.getRemoteClusterId())) {
            serviceInfo.setRemoteCluster(remoteClusterManager.getClusterEntity(appDeployment.getRemoteClusterId()));
        }
        serviceInfo.setKubernetesTemplate(KubernetesTemplate.copy(appDeploymentSpec.getKubernetesTemplate()));
        serviceInfo.setStorageVolumes(generateTemplateStorageVolumes(appDeploymentSpec.getStorageVolumes()));
        serviceInfo.setAccessMethods(generateTemplateAccessMethods(appDeploymentSpec.getAccessMethods()));
        Map<String, String> additionalParameters = new HashMap<>();
        if (appDeploymentSpec.getDeployParameters() != null && !appDeploymentSpec.getDeployParameters().isEmpty()) {
            additionalParameters.putAll(createAdditionalParametersMap(deploymentId, appDeploymentSpec.getDeployParameters(), serviceInfo.getRemoteCluster()));
        }
        if (appDeploymentSpec.getGlobalDeployParameters() != null && !appDeploymentSpec.getGlobalDeployParameters().isEmpty()) {
            additionalParameters.putAll(KubernetesParameterGenerator.createAdditionalGlobalParametersMap(appDeploymentSpec.getGlobalDeployParameters()));
        }
        serviceInfo.setAdditionalParameters(additionalParameters);
        repositoryManager.storeService(serviceInfo);
    }

    private Set<ServiceStorageVolume> generateTemplateStorageVolumes(Set<AppStorageVolume> storageVolumes) {
        return storageVolumes.stream()
                .map(ServiceStorageVolume::fromAppStorageVolume)
                .collect(Collectors.toSet());
    }

    private Set<ServiceAccessMethod> generateTemplateAccessMethods(Set<AppAccessMethod> accessMethods) {
        return accessMethods.stream()
                .map(ServiceAccessMethod::fromAppAccessMethod)
                .collect(Collectors.toSet());
    }

    private Map<String, String> createAdditionalParametersMap(Identifier deploymentId, Map<String, String> deployParameters, KCluster cluster) {
        Map<String, String> additionalParameters = new HashMap<>();
        Map<String, String> deploymentParameters;
        if (cluster != null) {
            deploymentParameters = deploymentRemoteClusterParametersProvider.deploymentRemoteParameters(deploymentId, cluster);
        } else {
            deploymentParameters = deploymentParametersProvider.deploymentParameters(deploymentId);
        }

        deployParameters.forEach((k, v) -> {
            switch (ParameterType.fromValue(k)) {
                case SMTP_HOSTNAME:
                    additionalParameters.put(v, deploymentParameters.get(ParameterType.SMTP_HOSTNAME.name()));
                    break;
                case SMTP_PORT:
                    additionalParameters.put(v, deploymentParameters.get(ParameterType.SMTP_PORT.name()));
                    break;
                case SMTP_HOST_WITH_PORT:
                    additionalParameters.put(v, deploymentParameters.get(ParameterType.SMTP_HOST_WITH_PORT.name()));
                    break;
                case SMTP_FROM_DEFAULT_DOMAIN:
                    additionalParameters.put(v, deploymentParameters.get(ParameterType.SMTP_FROM_DEFAULT_DOMAIN.name()));
                    break;
                case SMTP_USERNAME:
                    if (deploymentParameters.containsKey(ParameterType.SMTP_USERNAME.name())) {
                        additionalParameters.put(v, deploymentParameters.get(ParameterType.SMTP_USERNAME.name()));
                    }
                    break;
                case SMTP_PASSWORD:
                    if (deploymentParameters.containsKey(ParameterType.SMTP_PASSWORD.name())) {
                        additionalParameters.put(v, deploymentParameters.get(ParameterType.SMTP_PASSWORD.name()));
                    }
                    break;
                case DOMAIN_CODENAME:
                    additionalParameters.put(v, deploymentParameters.get(ParameterType.DOMAIN_CODENAME.name()));
                    break;
                case BASE_URL:
                    additionalParameters.put(v, deploymentParameters.get(ParameterType.BASE_URL.name()));
                    break;
                case RELEASE_NAME:
                    additionalParameters.put(v, deploymentParameters.get(ParameterType.RELEASE_NAME.name()));
                    break;
                case APP_INSTANCE_NAME:
                    additionalParameters.put(v, deploymentParameters.get(ParameterType.APP_INSTANCE_NAME.name()));
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        });
        return additionalParameters;
    }

    @Override
    @Loggable(LogLevel.TRACE)
    public void verifyRequestAndObtainInitialDeploymentDetails(Identifier deploymentId) {
        try {
            clusterValidator.checkClusterStatusAndPrerequisites();
        } catch (KClusterCheckException e) {
            throw new ContainerOrchestratorInternalErrorException(e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.TRACE)
    public void prepareDeploymentEnvironment(Identifier deploymentId, boolean configFileRepositoryRequired) {
        try {
            if (configFileRepositoryRequired) {
                gitLabManager.validateGitLabInstance();
            }
            if (!ingressManager.getControllerConfigOption().equals(IngressControllerConfigOption.USE_EXISTING)) {
                String domain = repositoryManager.loadDomain(deploymentId);
                ingressControllerManager.deployIngressControllerIfMissing(domain);
                //Not implemented yet, so no rewrite for remote clusters
            }
        } catch (InvalidDeploymentIdException idie) {
            throw new ContainerOrchestratorInternalErrorException(serviceNotFoundMessage(idie.getMessage()));
        } catch (IngressControllerManipulationException | GitLabInvalidConfigurationException icme) {
            throw new CouldNotPrepareEnvironmentException(icme.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void deployNmService(Identifier deploymentId) {
        try {
            KubernetesNmServiceInfo service = repositoryManager.loadService(deploymentId);
            String serviceExternalUrl;
            if (service.getRemoteCluster() == null) {
                serviceExternalUrl = ingressResourceManager.generateServiceExternalURL(
                        service.getDomain(),
                        service.getDeploymentName(),
                        ingressManager.getExternalServiceDomain(service.getDomain()),
                        ingressManager.getIngressPerDomain());
            } else {
                serviceExternalUrl = ingressResourceManager.generateServiceExternalURL(
                        service.getDomain(),
                        service.getDeploymentName(),
                        service.getRemoteCluster().getIngress().getExternalServiceDomain(),
                        service.getRemoteCluster().getIngress().getIngressPerDomain());
            }
            log.debug("Generated service external URL: {}", serviceExternalUrl);
            String servicePublicUrl = generateServicePublicUrl(service);

            Set<ServiceAccessMethod> accessMethods = retrieveAccessMethods(service);
            disableAccessMethodsBasedOnCondition(accessMethods, service.getAdditionalParameters());
            accessMethods = populateAccessMethodsWithUrl(accessMethods, serviceExternalUrl, servicePublicUrl);
            log.debug("Updated list of access methods:");
            accessMethods.forEach(am -> log.debug("{}:{}:{}", am.getType(), am.getName(), am.getUrl()));
            repositoryManager.updateKServiceAccessMethods(accessMethods);
            serviceLifecycleManager.deployService(deploymentId);
        } catch (InvalidDeploymentIdException | InvalidConfigurationException ex) {
            throw new ContainerOrchestratorInternalErrorException(serviceNotFoundMessage(ex.getMessage()));
        } catch (KServiceManipulationException e) {
            throw new CouldNotDeployServiceException(e.getMessage());
        }
    }

    private Set<ServiceAccessMethod> retrieveAccessMethods(KubernetesNmServiceInfo service) {
        return service.getAccessMethods().stream()
                .map(am -> {
                    if (am.isOfType(PUBLIC)) {
                        if (!shouldRemainPublic(service.getAdditionalParameters(), am)) {
                            log.info("{} access will remain public: no", am.getName());
                            return new ServiceAccessMethod(am.getId(), EXTERNAL, am.getName(), am.getUrl(), am.getProtocol(), am.getCondition(), am.isEnabled(), am.getDeployParameters());
                        }
                        log.info("{} access will remain public: yes", am.getName());
                    }
                    return am;
                }).collect(Collectors.toSet());
    }

    private boolean shouldRemainPublic(Map<String, String> parameters, ServiceAccessMethod accessMethod) {
        return parameters == null
                || parameters.getOrDefault(PUBLIC_ACCESS_SELECTOR_ARGUMENT_EXPRESSION_PREFIX + accessMethod.getName(), "yes").equals("yes");
    }

    private void disableAccessMethodsBasedOnCondition(Set<ServiceAccessMethod> accessMethods, Map<String, String> deploymentParameters) {
        accessMethods.forEach(am -> {
            if (shouldBeDisabled(am, deploymentParameters)) {
                log.debug("Access method marked as disabled.");
                am.setEnabled(false);
            }
        });
    }

    private boolean shouldBeDisabled(ServiceAccessMethod accessMethod, Map<String, String> deploymentParameters) {
        if (StringUtils.isEmpty(accessMethod.getCondition())) {
            return false;
        }
        log.debug("Access method is enabled conditionally (condition parameter key: {})", accessMethod.getCondition());
        String conditionValue = deploymentParameters.get(accessMethod.getCondition());
        if (StringUtils.isEmpty(conditionValue)) {
            log.debug("Condition value is null or empty.");
            return false;
        } else {
            return !conditionValue.equalsIgnoreCase("true");
        }
    }

    private String generateServicePublicUrl(KubernetesNmServiceInfo service) {
        if (service.getRemoteCluster() != null) {
            return service.getDeploymentName().toLowerCase() + "-" + service.getDomain() + "." + service.getRemoteCluster().getIngress().getPublicServiceDomain();
        }
        return service.getDeploymentName().toLowerCase() + "-" + service.getDomain() + "." + ingressManager.getPublicServiceDomain();
    }

    private Set<ServiceAccessMethod> populateAccessMethodsWithUrl(Set<ServiceAccessMethod> inputAccessMethods, String serviceExternalUrl, String servicePublicUrl) {
        Set<ServiceAccessMethod> accessMethods = new HashSet<>();
        inputAccessMethods.forEach(m -> {
            ServiceAccessMethod updated = copy(m);
            switch (m.getType()) {
                case DEFAULT -> updated.setUrl(serviceExternalUrl);
                case EXTERNAL -> updated.setUrl(updated.getName().toLowerCase() + "-" + serviceExternalUrl);
                case PUBLIC -> {
                    if (servicePublicUrl != null) {
                        updated.setUrl(servicePublicUrl);
                    }
                }
            }
            accessMethods.add(updated);
        });
        return accessMethods;
    }

    @Override
    @Loggable(LogLevel.TRACE)
    public boolean checkService(Identifier deploymentId) {
        try {
            if (!serviceLifecycleManager.checkServiceDeployed(deploymentId)) {
                return false;
            }

            KubernetesNmServiceInfo service = repositoryManager.loadService(deploymentId);
            if (!kubernetesApiJanitorService.checkIfReady(
                    service.getRemoteCluster(),
                    service.getDescriptiveDeploymentId(),
                    service.getDomain())
            ) {
                return false;
            }

            retrieveOrUpdateInternalServiceIpAddress(service);
            retrieveOrUpdateLocalServiceName(service);

            return true;
        } catch (KServiceManipulationException | JanitorException ex) {
            throw new ContainerCheckFailedException(ex.getMessage());
        }
    }

    private void retrieveOrUpdateInternalServiceIpAddress(KubernetesNmServiceInfo service) {
        try {
            Set<ServiceAccessMethod> accessMethods = new HashSet<>();
            service.getAccessMethods().forEach(m -> {
                final ServiceAccessMethod copy = ServiceAccessMethod.copy(m);
                if (m.isOfType(INTERNAL) && StringUtils.isEmpty(m.getUrl())) {
                    final String lbServiceIp = kubernetesApiJanitorService.retrieveServiceIp(
                            service.getRemoteCluster(),
                            buildServiceId(service.getDescriptiveDeploymentId(), m.getDeployParameters()),
                            service.getDomain());
                    final String ipWithPortString = getIpAddressWithPort(lbServiceIp, m.getDeployParameters());
                    log.debug("Setting internal access URL to: {}", getUserAtIpAddressUrl(ipWithPortString, m.getProtocol(), m.getDeployParameters()));
                    copy.setUrl(getUserAtIpAddressUrl(ipWithPortString, m.getProtocol(), m.getDeployParameters()));
                }
                accessMethods.add(copy);
            });
            repositoryManager.updateKServiceAccessMethods(accessMethods);
        } catch (JanitorException je) {
            log.error("Could not retrieve IP for {}", service.getDescriptiveDeploymentId());
        }
    }

    private Identifier buildServiceId(Identifier deploymentId, Map<HelmChartIngressVariable, String> deployParameters) {
        return deployParameters != null && deployParameters.get(HelmChartIngressVariable.K8S_SERVICE_SUFFIX) != null ?
                Identifier.newInstance(deploymentId + "-" + deployParameters.get(HelmChartIngressVariable.K8S_SERVICE_SUFFIX)) :
                deploymentId;
    }

    private String getIpAddressWithPort(String ip, Map<HelmChartIngressVariable, String> deployParameters) {
        if (deployParameters != null && deployParameters.containsKey(HelmChartIngressVariable.K8S_SERVICE_PORT)) {
            return ip + " (port: " + deployParameters.get(HelmChartIngressVariable.K8S_SERVICE_PORT) + ")";
        } else {
            return ip;
        }
    }

    private String getUserAtIpAddressUrl(String ipAddress, String protocol, Map<HelmChartIngressVariable, String> deployParameters) {
        String username;
        if (deployParameters != null
                && deployParameters.containsKey(HelmChartIngressVariable.ACCESS_USER)
                && !deployParameters.get(HelmChartIngressVariable.ACCESS_USER).isEmpty()) {
            username = deployParameters.get(HelmChartIngressVariable.ACCESS_USER);
            return username + "@" + ipAddress;
        } else {
            return "SSH".equals(protocol) ? DEFAULT_INTERNAL_SSH_ACCESS_USERNAME + "@" + ipAddress : ipAddress;
        }
    }

    private void retrieveOrUpdateLocalServiceName(KubernetesNmServiceInfo service) {
        Set<ServiceAccessMethod> accessMethods = new HashSet<>();
        service.getAccessMethods().forEach(m -> {
            final ServiceAccessMethod copy = ServiceAccessMethod.copy(m);
            if (m.isOfType(LOCAL) && StringUtils.isEmpty(m.getUrl())) {
                final Identifier serviceName = buildServiceId(service.getDescriptiveDeploymentId(), m.getDeployParameters());
                if (!kubernetesApiJanitorService.checkServiceExists(service.getRemoteCluster(), serviceName, service.getDomain())) {
                    log.error("Could not retrieve service name for {}", service.getDescriptiveDeploymentId());
                    return;
                }
                String username = m.getDeployParameters().get(HelmChartIngressVariable.ACCESS_USER);
                copy.setUrl(username != null && !username.isEmpty() ?
                        username + "@" + serviceName.value() : serviceName.value());
                if (m.getDeployParameters().containsKey(HelmChartIngressVariable.K8S_SERVICE_PORT)) {
                    copy.setUrl(copy.getUrl() + " (port: " + m.getDeployParameters().get(HelmChartIngressVariable.K8S_SERVICE_PORT) + ")");
                }
            }
            accessMethods.add(copy);
        });
        repositoryManager.updateKServiceAccessMethods(accessMethods);
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeNmService(Identifier deploymentId) {
        try {
            serviceLifecycleManager.deleteServiceIfExists(deploymentId);
            KubernetesNmServiceInfo service = repositoryManager.loadService(deploymentId);
            kubernetesApiJanitorService.deleteConfigMapIfExists(service.getRemoteCluster(), service.getDescriptiveDeploymentId(), service.getDomain());
            kubernetesApiJanitorService.deleteBasicAuthIfExists(service.getRemoteCluster(), service.getDescriptiveDeploymentId(), service.getDomain());
            kubernetesApiJanitorService.deleteTlsIfExists(service.getRemoteCluster(), service.getDescriptiveDeploymentId(), service.getDomain());
        } catch (InvalidDeploymentIdException idie) {
            throw new ContainerOrchestratorInternalErrorException(serviceNotFoundMessage(idie.getMessage()));
        } catch (KServiceManipulationException e) {
            throw new CouldNotRemoveServiceException(e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void restartNmService(Identifier deploymentId) {
        try {
            serviceOperationsManager.restartService(deploymentId);
        } catch (InvalidDeploymentIdException idie) {
            throw new ContainerOrchestratorInternalErrorException(serviceNotFoundMessage(idie.getMessage()));
        } catch (KServiceManipulationException e) {
            throw new CouldNotRestartServiceException(e.getMessage());
        }
    }

    @Override
    public String info() {
        return "Kubernetes Container Orchestrator";
    }

    @Override
    public AppUiAccessDetails serviceAccessDetails(Identifier deploymentId) {
        try {
            retrieveOrUpdateInternalServiceIpAddress(repositoryManager.loadService(deploymentId));
            Set<ServiceAccessMethodView> serviceAccessMethodViewSet = new HashSet<>();
            repositoryManager.loadService(deploymentId).getAccessMethods().stream()
                    .filter(ServiceAccessMethod::isEnabled)
                    .forEach(m -> serviceAccessMethodViewSet.add(ServiceAccessMethodView.fromServiceAccessMethod(m)));
            return new AppUiAccessDetails(serviceAccessMethodViewSet);
        } catch (InvalidDeploymentIdException idie) {
            throw new ContainerOrchestratorInternalErrorException(serviceNotFoundMessage(idie.getMessage()));
        }
    }

    @Override
    public Map<String, String> serviceDeployParameters(Identifier deploymentId) {
        try {
            // TODO filter only relevant parameters
            return repositoryManager.loadService(deploymentId).getAdditionalParameters();
        } catch (Exception e) {
            throw new ProcessingException("Can't find additional parameters for " + deploymentId.value());
        }
    }

    @Override
    public List<AppComponentDetails> serviceComponents(Identifier deploymentId) {
        try {
            KubernetesNmServiceInfo service = repositoryManager.loadService(deploymentId);
            return kubernetesApiJanitorService.getPodNames(service.getRemoteCluster(), service.getDescriptiveDeploymentId(), service.getDomain());
        } catch (InvalidDeploymentIdException idie) {
            throw new ContainerOrchestratorInternalErrorException(serviceNotFoundMessage(idie.getMessage()));
        } catch (JanitorException je) {
            throw new ContainerOrchestratorInternalErrorException("Problem with retrieving service components", je);
        }
    }

    @Override
    public AppComponentLogs serviceComponentLogs(Identifier deploymentId, String serviceComponentName, String serviceSubComponentName, int limit) {
        try {
            KubernetesNmServiceInfo service = repositoryManager.loadService(deploymentId);
            return new AppComponentLogs(
                    serviceComponentName,
                    kubernetesApiJanitorService.getPodLogs(service.getRemoteCluster(), serviceComponentName, serviceSubComponentName, service.getDomain(), limit)
            );
        } catch (InvalidDeploymentIdException idie) {
            throw new ContainerOrchestratorInternalErrorException(serviceNotFoundMessage(idie.getMessage()));
        } catch (JanitorException je) {
            throw new ContainerOrchestratorInternalErrorException("Problem with retrieving service component logs", je);
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void upgradeKubernetesService(Identifier deploymentId, KubernetesTemplate targetVersion) {
        try {
            serviceLifecycleManager.upgradeService(deploymentId, targetVersion);
        } catch (InvalidDeploymentIdException idie) {
            throw new ContainerOrchestratorInternalErrorException(serviceNotFoundMessage(idie.getMessage()));
        } catch (KServiceManipulationException e) {
            throw new CouldNotUpgradeKubernetesServiceException(e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void pauseNmService(Identifier deploymentId) {
        try {
            serviceOperationsManager.scaleService(deploymentId, 0);
        } catch (InvalidDeploymentIdException idie) {
            throw new ContainerOrchestratorInternalErrorException(serviceNotFoundMessage(idie.getMessage()));
        } catch (Exception e) {
            throw new CouldNotPauseServiceException(e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void resumeNmService(Identifier deploymentId) {
        try {
            serviceOperationsManager.scaleService(deploymentId, 1);
        } catch (InvalidDeploymentIdException idie) {
            throw new ContainerOrchestratorInternalErrorException(serviceNotFoundMessage(idie.getMessage()));
        } catch (Exception e) {
            throw new CouldNotResumeServiceException(e.getMessage());
        }
    }

    private static String serviceNotFoundMessage(String exceptionMessage) {
        return String.format("Service not found in repository -> Invalid deployment id %s", exceptionMessage);
    }

}
