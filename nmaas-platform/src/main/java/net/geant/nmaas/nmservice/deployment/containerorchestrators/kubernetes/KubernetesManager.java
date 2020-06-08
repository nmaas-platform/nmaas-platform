package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.externalservices.inventory.gitlab.GitLabManager;
import net.geant.nmaas.externalservices.inventory.gitlab.exceptions.GitLabInvalidConfigurationException;
import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterDeploymentManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterIngressManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressControllerConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressResourceConfigOption;
import net.geant.nmaas.nmservice.deployment.ContainerOrchestrator;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster.KClusterCheckException;
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
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethod.DEFAULT_INTERNAL_ACCESS_USERNAME;
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
    private KClusterValidator clusterValidator;
    private KServiceLifecycleManager serviceLifecycleManager;
    private KServiceOperationsManager serviceOperationsManager;
    private IngressControllerManager ingressControllerManager;
    private IngressResourceManager ingressResourceManager;
    private KClusterIngressManager ingressManager;
    private KClusterDeploymentManager deploymentManager;
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
            additionalParameters.putAll(createAdditionalParametersMap(appDeploymentSpec.getDeployParameters(), appDeployment));
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

    private Map<String, String> createAdditionalParametersMap(Map<String, String> deployParameters, AppDeployment appDeployment){
        Map<String, String> additionalParameters = new HashMap<>();
        deployParameters.forEach((k,v) ->{
            switch (ParameterType.fromValue(k)) {
                case SMTP_HOSTNAME:
                    additionalParameters.put(v, deploymentManager.getSMTPServerHostname());
                    break;
                case SMTP_PORT:
                    additionalParameters.put(v, deploymentManager.getSMTPServerPort().toString());
                    break;
                case SMTP_USERNAME:
                    deploymentManager.getSMTPServerUsername().ifPresent(username->{
                        if(!username.isEmpty())
                            additionalParameters.put(v, username);
                    });
                    break;
                case SMTP_PASSWORD:
                    deploymentManager.getSMTPServerPassword().ifPresent(value->{
                        if(!value.isEmpty())
                            additionalParameters.put(v, value);
                    });
                    break;
                case DOMAIN_CODENAME:
                    additionalParameters.put(v, appDeployment.getDomain());
                    break;
                case BASE_URL:
                    additionalParameters.put(v, ingressManager.getExternalServiceDomain());
                    break;
                case RELEASE_NAME:
                    additionalParameters.put(v, appDeployment.getDescriptiveDeploymentId().value());
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
            if (IngressResourceConfigOption.DEPLOY_USING_API.equals(ingressManager.getResourceConfigOption())) {
                    ingressResourceManager.createOrUpdateIngressResource(
                            deploymentId,
                            service.getDomain(),
                            serviceExternalUrl);
            }
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

            // NOTE: Current assumption is that there will be at max one INTERNAL access method identifiable by deployment name
            try {
                Set<ServiceAccessMethod> accessMethods = service.getAccessMethods().stream()
                        .peek(m -> {
                            if (m.isOfType(INTERNAL)) {
                                m.setUrl(getUserAtIpAddressUrl(janitorService.retrieveServiceIp(service.getDescriptiveDeploymentId(), service.getDomain())));
                            }
                        })
                        .collect(Collectors.toSet());
                repositoryManager.updateKServiceAccessMethods(accessMethods);
            } catch (JanitorResponseException je) {
                log.error("Could not retrieve IP for " + service.getDescriptiveDeploymentId());
                return true;
            }

            return true;

        } catch (KServiceManipulationException | JanitorResponseException ex) {
            throw new ContainerCheckFailedException(ex.getMessage());
        }
    }

    private String getUserAtIpAddressUrl(String ipAddress) {
        return DEFAULT_INTERNAL_ACCESS_USERNAME + "@" + ipAddress;
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
            /*
            NOTE:
            Currently (January 2020) option DEPLOY_USING_API is not used and shall be removed in future releases
             */
            if (IngressResourceConfigOption.DEPLOY_USING_API.equals(ingressManager.getResourceConfigOption())) {
                Optional<ServiceAccessMethod> serviceAccessMethod = Optional.of((new ArrayList<>(service.getAccessMethods())).get(0));
                ingressResourceManager.deleteIngressRule(serviceAccessMethod.orElseThrow(() -> new ContainerOrchestratorInternalErrorException("External access  url not found")).getUrl(), service.getDomain());
            }
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
