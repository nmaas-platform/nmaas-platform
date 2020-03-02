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
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.janitor.JanitorService;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ParameterType;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethod;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodView;
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
    private KClusterIngressManager clusterIngressManager;
    private IngressControllerManager ingressControllerManager;
    private IngressResourceManager ingressResourceManager;
    private KClusterDeploymentManager deploymentManager;
    private GitLabManager gitLabManager;
    private JanitorService janitorService;

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(Identifier deploymentId, AppDeployment appDeployment, AppDeploymentSpec appDeploymentSpec) {
        if(appDeployment == null){
            throw new NmServiceRequestVerificationException("App deployment cannot be null");
        }
        if(appDeploymentSpec == null){
            throw new NmServiceRequestVerificationException("App deployment spec cannot be null");
        }
        if(!appDeploymentSpec.getSupportedDeploymentEnvironments().contains(AppDeploymentEnv.KUBERNETES)) {
            throw new NmServiceRequestVerificationException(
                    "Service deployment not possible with currently used container orchestrator");
        }
        if(appDeploymentSpec.getKubernetesTemplate() == null) {
            throw new NmServiceRequestVerificationException("Kubernetes template cannot be null");
        }
        if(appDeploymentSpec.getAccessMethods() == null) {
            throw new NmServiceRequestVerificationException("Service access methods cannot be null");
        }

        KubernetesNmServiceInfo serviceInfo = new KubernetesNmServiceInfo(
                deploymentId,
                appDeployment.getDeploymentName(),
                appDeployment.getDomain(),
                appDeployment.getStorageSpace(),
                appDeployment.getDescriptiveDeploymentId()
        );
        serviceInfo.setKubernetesTemplate(KubernetesTemplate.copy(appDeploymentSpec.getKubernetesTemplate()));
        serviceInfo.setAccessMethods(generateTemplateAccessMethods(appDeploymentSpec.getAccessMethods()));
        if(appDeploymentSpec.getDeployParameters() != null && !appDeploymentSpec.getDeployParameters().isEmpty()) {
            serviceInfo.setAdditionalParameters(createAdditionalParametersMap(appDeploymentSpec.getDeployParameters(), appDeployment));
        }
        repositoryManager.storeService(serviceInfo);
    }

    private Set<ServiceAccessMethod> generateTemplateAccessMethods(Set<AppAccessMethod> accessMethods) {
        return accessMethods.stream()
                .map(ServiceAccessMethod::fromAppAccessMethod)
                .collect(Collectors.toSet());
    }

    private Map<String, String> createAdditionalParametersMap(Map<ParameterType, String> deployParameters, AppDeployment appDeployment){
        Map<String, String> additionalParameters = new HashMap<>();
        deployParameters.forEach((k,v) ->{
            switch (k){
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
            if(!clusterIngressManager.getControllerConfigOption().equals(IngressControllerConfigOption.USE_EXISTING)) {
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
                    clusterIngressManager.getExternalServiceDomain(service.getDomain()),
                    clusterIngressManager.getIngressPerDomain());

            Set<ServiceAccessMethod> accessMethods = populateAccessMethodsWithUrl(service, serviceExternalUrl);
            repositoryManager.updateKServiceAccessMethods(deploymentId, accessMethods);

            serviceLifecycleManager.deployService(deploymentId);
            if (IngressResourceConfigOption.DEPLOY_USING_API.equals(clusterIngressManager.getResourceConfigOption())) {
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
    public void checkService(Identifier deploymentId) {
        try {
            if (!serviceLifecycleManager.checkServiceDeployed(deploymentId))
                throw new ContainerCheckFailedException("Service not deployed.");
            KubernetesNmServiceInfo service = repositoryManager.loadService(deploymentId);
            if (!janitorService.checkIfReady(service.getDescriptiveDeploymentId(), service.getDomain())) {
                throw new ContainerCheckFailedException("Service is not ready yet.");
            }
            /*
            NOTE:
            Current assumption is that there will be at max one INTERNAL access methods identifiable by deployment
             */
            Set<ServiceAccessMethod> accessMethods = service.getAccessMethods().stream()
                    .peek(m -> {
                        if(m.isOfType(INTERNAL)) {
                            m.setUrl(janitorService.retrieveServiceIp(service.getDescriptiveDeploymentId(), service.getDomain()));
                        }
                    })
                    .collect(Collectors.toSet());
            repositoryManager.updateKServiceAccessMethods(deploymentId, accessMethods);
        } catch (KServiceManipulationException e) {
            throw new ContainerCheckFailedException(e.getMessage());
        }
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
            if (IngressResourceConfigOption.DEPLOY_USING_API.equals(clusterIngressManager.getResourceConfigOption())) {
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
