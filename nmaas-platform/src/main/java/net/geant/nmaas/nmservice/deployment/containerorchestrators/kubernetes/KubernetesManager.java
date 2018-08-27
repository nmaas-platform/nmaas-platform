package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;


import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterApiManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterIngressManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressControllerConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressResourceConfigOption;
import net.geant.nmaas.nmservice.deployment.ContainerOrchestrator;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster.KClusterCheckException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress.IngressControllerManipulationException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress.IngressResourceManipulationException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.exceptions.KServiceManipulationException;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerCheckFailedException;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerOrchestratorInternalErrorException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotDeployNmServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotPrepareEnvironmentException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRemoveNmServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRestartNmServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceRequestVerificationException;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.AppUiAccessDetails;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Implements service deployment mechanism on Kubernetes cluster.
 */
@Component
@Profile("env_kubernetes")
public class KubernetesManager implements ContainerOrchestrator {

    private KubernetesRepositoryManager repositoryManager;
    private KClusterValidator clusterValidator;
    private KServiceLifecycleManager serviceLifecycleManager;
    private KServiceOperationsManager serviceOperationsManager;
    private KClusterIngressManager clusterIngressManager;
    private IngressControllerManager ingressControllerManager;
    private IngressResourceManager ingressResourceManager;
    private KClusterApiManager clusterApiManager;

    @Autowired
    public KubernetesManager(KubernetesRepositoryManager repositoryManager,
                             KClusterValidator clusterValidator,
                             KServiceLifecycleManager serviceLifecycleManager,
                             KServiceOperationsManager serviceOperationsManager,
                             KClusterIngressManager clusterIngressManager,
                             IngressControllerManager ingressControllerManager,
                             IngressResourceManager ingressResourceManager,
                             KClusterApiManager clusterApiManager) {
        this.repositoryManager = repositoryManager;
        this.clusterValidator = clusterValidator;
        this.serviceLifecycleManager = serviceLifecycleManager;
        this.serviceOperationsManager = serviceOperationsManager;
        this.clusterIngressManager = clusterIngressManager;
        this.ingressControllerManager = ingressControllerManager;
        this.ingressResourceManager = ingressResourceManager;
        this.clusterApiManager = clusterApiManager;
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(Identifier deploymentId, String deploymentName, String domain, AppDeploymentSpec appDeploymentSpec)
            throws NmServiceRequestVerificationException {
        if(!appDeploymentSpec.getSupportedDeploymentEnvironments().contains(AppDeploymentEnv.KUBERNETES))
            throw new NmServiceRequestVerificationException(
                    "Service deployment not possible with currently used container orchestrator");
        repositoryManager.storeService(new KubernetesNmServiceInfo(
                deploymentId,
                deploymentName,
                domain,
                KubernetesTemplate.copy(appDeploymentSpec.getKubernetesTemplate()))
        );
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyRequestAndObtainInitialDeploymentDetails(Identifier deploymentId)
            throws NmServiceRequestVerificationException, ContainerOrchestratorInternalErrorException {
        try {
            if(this.clusterApiManager.getUseClusterApi()) {
                clusterValidator.checkClusterStatusAndPrerequisites();
            }
        } catch (KClusterCheckException e) {
            throw new ContainerOrchestratorInternalErrorException(e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void prepareDeploymentEnvironment(Identifier deploymentId)
            throws CouldNotPrepareEnvironmentException, ContainerOrchestratorInternalErrorException {
        try {
            if(!IngressControllerConfigOption.USE_EXISTING.equals(clusterIngressManager.getControllerConfigOption())) {
                String domain = repositoryManager.loadDomain(deploymentId);
                ingressControllerManager.deployIngressControllerIfMissing(domain);
            }
        } catch (InvalidDeploymentIdException idie) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Service not found in repository -> Invalid deployment id " + idie.getMessage());
        } catch (IngressControllerManipulationException icme) {
            throw new CouldNotPrepareEnvironmentException(icme.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void deployNmService(Identifier deploymentId)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException {
        try {
            KubernetesNmServiceInfo service = repositoryManager.loadService(deploymentId);
            String serviceExternalUrl = ingressResourceManager.generateServiceExternalURL(
                    service.getDomain(),
                    service.getDeploymentName(),
                    clusterIngressManager.getExternalServiceDomain());
            repositoryManager.updateKServiceExternalUrl(deploymentId, serviceExternalUrl);
            serviceLifecycleManager.deployService(deploymentId);
            if (IngressResourceConfigOption.DEPLOY_USING_API.equals(clusterIngressManager.getResourceConfigOption())) {
                    ingressResourceManager.createOrUpdateIngressResource(
                            deploymentId,
                            service.getDomain(),
                            serviceExternalUrl);
            }
        } catch (InvalidDeploymentIdException idie) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Service not found in repository -> Invalid deployment id " + idie.getMessage());
        } catch (KServiceManipulationException
                | IngressResourceManipulationException e) {
            throw new CouldNotDeployNmServiceException(e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void checkService(Identifier deploymentId) throws ContainerCheckFailedException, ContainerOrchestratorInternalErrorException {
        try {
            if (!serviceLifecycleManager.checkServiceDeployed(deploymentId))
                throw new ContainerCheckFailedException("Service not deployed.");
        } catch (KServiceManipulationException e) {
            throw new ContainerCheckFailedException(e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeNmService(Identifier deploymentId) throws CouldNotRemoveNmServiceException, ContainerOrchestratorInternalErrorException {
        try {
            serviceLifecycleManager.deleteService(deploymentId);
            KubernetesNmServiceInfo service = repositoryManager.loadService(deploymentId);
            if (IngressResourceConfigOption.DEPLOY_USING_API.equals(clusterIngressManager.getResourceConfigOption())) {
                ingressResourceManager.deleteIngressRule(service.getServiceExternalUrl(), service.getDomain());
            }
        } catch (InvalidDeploymentIdException idie) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Service not found in repository -> Invalid deployment id " + idie.getMessage());
        } catch (KServiceManipulationException
                | IngressResourceManipulationException e) {
            throw new CouldNotRemoveNmServiceException(e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void restartNmService(Identifier deploymentId) throws CouldNotRestartNmServiceException, ContainerOrchestratorInternalErrorException {
        try {
            serviceOperationsManager.restartService(deploymentId);
        } catch (InvalidDeploymentIdException idie) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Service not found in repository -> Invalid deployment id " + idie.getMessage());
        } catch (KServiceManipulationException e) {
            throw new CouldNotRestartNmServiceException(e.getMessage());
        }
    }

    @Override
    public String info() {
        return "Kubernetes Container Orchestrator";
    }

    @Override
    public AppUiAccessDetails serviceAccessDetails(Identifier deploymentId) throws ContainerOrchestratorInternalErrorException {
        try {
            String serviceExternalUrl = repositoryManager.loadService(deploymentId).getServiceExternalUrl();
            return new AppUiAccessDetails("http://" + serviceExternalUrl);
        } catch (InvalidDeploymentIdException idie) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Service not found in repository -> Invalid deployment id " + idie.getMessage());
        }
    }

}
