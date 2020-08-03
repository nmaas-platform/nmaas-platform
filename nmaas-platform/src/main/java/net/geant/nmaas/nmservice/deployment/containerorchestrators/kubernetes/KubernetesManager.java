package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.externalservices.inventory.gitlab.GitLabManager;
import net.geant.nmaas.externalservices.inventory.gitlab.exceptions.GitLabInvalidConfigurationException;
import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterIngressManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressControllerConfigOption;
import net.geant.nmaas.nmservice.deployment.ContainerOrchestrator;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster.KClusterCheckException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmChartIngressVariable;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress.IngressControllerManipulationException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.janitor.JanitorResponseException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.janitor.JanitorService;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethod;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceStorageVolume;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.exceptions.KServiceManipulationException;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerCheckFailedException;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerOrchestratorInternalErrorException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotDeployNmServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotPrepareEnvironmentException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRemoveNmServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRestartNmServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceRequestVerificationException;
import net.geant.nmaas.orchestration.AppUiAccessDetails;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppAccessMethod;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.AppStorageVolume;
import net.geant.nmaas.orchestration.exceptions.InvalidConfigurationException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethod.DEFAULT_INTERNAL_SSH_ACCESS_USERNAME;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType.DEFAULT;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType.EXTERNAL;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType.INTERNAL;

/**
 * Implements service deployment mechanism on Kubernetes cluster.
 */
@Component
@Profile("env_kubernetes")
@Log4j2
@AllArgsConstructor
public class KubernetesManager implements ContainerOrchestrator {

    private KubernetesRepositoryManager repositoryManager;
    private KubernetesDeploymentParametersProvider deploymentParametersProvider;
    private KClusterValidator clusterValidator;
    private KServiceLifecycleManager serviceLifecycleManager;
    private KServiceOperationsManager serviceOperationsManager;
    private IngressControllerManager ingressControllerManager;
    private IngressResourceManager ingressResourceManager;
    private KClusterIngressManager ingressManager;
    private GitLabManager gitLabManager;
    private JanitorService janitorService;

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(Identifier deploymentId, AppDeployment appDeployment, AppDeploymentSpec appDeploymentSpec) {
        try {
            checkArgument(appDeployment != null, "App deployment cannot be null");
            checkArgument(appDeploymentSpec != null, "App deployment spec cannot be null");
            checkArgument(appDeploymentSpec.getSupportedDeploymentEnvironments().contains(AppDeploymentEnv.KUBERNETES),
                    "Service deployment not possible with currently used container orchestrator");
            checkArgument(appDeploymentSpec.getKubernetesTemplate() != null, "Kubernetes template cannot be null");
            checkArgument(appDeploymentSpec.getAccessMethods() != null && !appDeploymentSpec.getAccessMethods().isEmpty(),
                    "Service access methods cannot be null");
        } catch (IllegalArgumentException iae) {
            throw new NmServiceRequestVerificationException(iae.getMessage());
        }

        KubernetesNmServiceInfo serviceInfo = new KubernetesNmServiceInfo(
                deploymentId,
                appDeployment.getDeploymentName(),
                appDeployment.getDomain(),
                appDeployment.getDescriptiveDeploymentId()
        );
        serviceInfo.setKubernetesTemplate(KubernetesTemplate.copy(appDeploymentSpec.getKubernetesTemplate()));
        serviceInfo.setStorageVolumes(generateTemplateStorageVolumes(appDeploymentSpec.getStorageVolumes()));
        serviceInfo.setAccessMethods(generateTemplateAccessMethods(appDeploymentSpec.getAccessMethods()));
        Map<String, String> additionalParameters = new HashMap<>();
        if(appDeploymentSpec.getDeployParameters() != null && !appDeploymentSpec.getDeployParameters().isEmpty()) {
            additionalParameters.putAll(createAdditionalParametersMap(deploymentId, appDeploymentSpec.getDeployParameters()));
        }
        if(appDeploymentSpec.getGlobalDeployParameters() != null && !appDeploymentSpec.getGlobalDeployParameters().isEmpty()) {
            additionalParameters.putAll(appDeploymentSpec.getGlobalDeployParameters());
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

    private Map<String, String> createAdditionalParametersMap(Identifier deploymentId, Map<String, String> deployParameters){
        Map<String, String> additionalParameters = new HashMap<>();
        Map<String, String> deploymentParameters = deploymentParametersProvider.deploymentParameters(deploymentId);
        deployParameters.forEach((k,v) ->{
            switch (ParameterType.fromValue(k)) {
                case SMTP_HOSTNAME:
                    additionalParameters.put(v, deploymentParameters.get(ParameterType.SMTP_HOSTNAME.name()));
                    break;
                case SMTP_PORT:
                    additionalParameters.put(v, deploymentParameters.get(ParameterType.SMTP_PORT.name()));
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
            }
        });
        return additionalParameters;
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyRequestAndObtainInitialDeploymentDetails(Identifier deploymentId) {
        try {
            clusterValidator.checkClusterStatusAndPrerequisites();
        } catch (KClusterCheckException e) {
            throw new ContainerOrchestratorInternalErrorException(e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void prepareDeploymentEnvironment(Identifier deploymentId, boolean configFileRepositoryRequired) {
        try {
            if(configFileRepositoryRequired){
                gitLabManager.validateGitLabInstance();
            }
            if(!ingressManager.getControllerConfigOption().equals(IngressControllerConfigOption.USE_EXISTING)) {
                String domain = repositoryManager.loadDomain(deploymentId);
                ingressControllerManager.deployIngressControllerIfMissing(domain);
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
            String serviceExternalUrl = ingressResourceManager.generateServiceExternalURL(
                    service.getDomain(),
                    service.getDeploymentName(),
                    ingressManager.getExternalServiceDomain(service.getDomain()),
                    ingressManager.getIngressPerDomain());

            Set<ServiceAccessMethod> accessMethods = populateAccessMethodsWithUrl(service, serviceExternalUrl);
            repositoryManager.updateKServiceAccessMethods(accessMethods);
            serviceLifecycleManager.deployService(deploymentId);
        } catch (InvalidDeploymentIdException | InvalidConfigurationException ex) {
            throw new ContainerOrchestratorInternalErrorException(serviceNotFoundMessage(ex.getMessage()));
        } catch (KServiceManipulationException e) {
            throw new CouldNotDeployNmServiceException(e.getMessage());
        }
    }

    private Set<ServiceAccessMethod> populateAccessMethodsWithUrl(KubernetesNmServiceInfo service, String serviceExternalUrl) {
        Set<ServiceAccessMethod> accessMethods = service.getAccessMethods().stream()
                .filter(m -> m.isOfType(INTERNAL))
                .collect(Collectors.toSet());
        accessMethods.addAll(service.getAccessMethods().stream()
                .filter(m -> m.isOfType(DEFAULT))
                .peek(m -> m.setUrl(serviceExternalUrl))
                .collect(Collectors.toSet()));
        accessMethods.addAll(service.getAccessMethods().stream()
                .filter(m -> m.isOfType(EXTERNAL))
                .peek(m -> m.setUrl(m.getName().toLowerCase() + "-" + serviceExternalUrl))
                .collect(Collectors.toSet()));
        return accessMethods;
    }

    @Override
    @Loggable(LogLevel.INFO)
    public boolean checkService(Identifier deploymentId) {
        try {
            if (!serviceLifecycleManager.checkServiceDeployed(deploymentId)) {
                return false;
            }

            KubernetesNmServiceInfo service = repositoryManager.loadService(deploymentId);

            if (!janitorService.checkIfReady(
                    getDeploymentIdForJanitorStatusCheck(service.getDescriptiveDeploymentId().value(), service.getKubernetesTemplate().getMainDeploymentName()),
                    service.getDomain())) {
                return false;
            }

            retrieveOrUpdateInternalServiceIpAddress(service);

            return true;

        } catch (KServiceManipulationException | JanitorResponseException ex) {
            throw new ContainerCheckFailedException(ex.getMessage());
        }
    }

    private void retrieveOrUpdateInternalServiceIpAddress(KubernetesNmServiceInfo service) {
        try {
            Set<ServiceAccessMethod> accessMethods = service.getAccessMethods().stream()
                    .peek(m -> {
                        if (m.isOfType(INTERNAL) && StringUtils.isEmpty(m.getUrl())) {
                            m.setUrl(getUserAtIpAddressUrl(
                                    janitorService.retrieveServiceIp(
                                            buildServiceId(service.getDescriptiveDeploymentId(), m.getDeployParameters()),
                                            service.getDomain()
                                    ),
                                    m.getProtocol()
                            ));
                        }
                    })
                    .collect(Collectors.toSet());
            repositoryManager.updateKServiceAccessMethods(accessMethods);
        } catch (JanitorResponseException je) {
            log.error("Could not retrieve IP for " + service.getDescriptiveDeploymentId());
        }
    }

    private Identifier buildServiceId(Identifier deploymentId, Map<HelmChartIngressVariable, String> deployParameters) {
        return deployParameters != null && deployParameters.get(HelmChartIngressVariable.K8S_SERVICE_SUFFIX) != null ?
                Identifier.newInstance(deploymentId + "-" + deployParameters.get(HelmChartIngressVariable.K8S_SERVICE_SUFFIX)) :
                deploymentId;
    }

    private String getUserAtIpAddressUrl(String ipAddress, String protocol) {
        return "SSH".equals(protocol) ? DEFAULT_INTERNAL_SSH_ACCESS_USERNAME + "@" + ipAddress : ipAddress;
    }

    private Identifier getDeploymentIdForJanitorStatusCheck(String releaseName, String componentName) {
        return componentName != null ?
                Identifier.newInstance(releaseName + "-" + componentName) :
                Identifier.newInstance(releaseName);
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeNmService(Identifier deploymentId) {
        try {
            serviceLifecycleManager.deleteServiceIfExists(deploymentId);
            KubernetesNmServiceInfo service = repositoryManager.loadService(deploymentId);
            janitorService.deleteConfigMapIfExists(service.getDescriptiveDeploymentId(), service.getDomain());
            janitorService.deleteBasicAuthIfExists(service.getDescriptiveDeploymentId(), service.getDomain());
            janitorService.deleteTlsIfExists(service.getDescriptiveDeploymentId(), service.getDomain());
        } catch (InvalidDeploymentIdException idie) {
            throw new ContainerOrchestratorInternalErrorException(serviceNotFoundMessage(idie.getMessage()));
        } catch (KServiceManipulationException e) {
            throw new CouldNotRemoveNmServiceException(e.getMessage());
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
            throw new CouldNotRestartNmServiceException(e.getMessage());
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
            repositoryManager.loadService(deploymentId).getAccessMethods().forEach(
                    m -> serviceAccessMethodViewSet.add(ServiceAccessMethodView.fromServiceAccessMethod(m))
            );
            return new AppUiAccessDetails(serviceAccessMethodViewSet);
        } catch (InvalidDeploymentIdException idie) {
            throw new ContainerOrchestratorInternalErrorException(serviceNotFoundMessage(idie.getMessage()));
        }
    }

    private String serviceNotFoundMessage(String exceptionMessage) {
        return String.format("Service not found in repository -> Invalid deployment id %s", exceptionMessage);
    }

}
