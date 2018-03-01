package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import net.geant.nmaas.nmservice.deployment.ContainerOrchestrator;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster.KClusterCheckException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress.IngressControllerManipulationException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress.IngressResourceManipulationException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.exceptions.KServiceManipulationException;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
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
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Profile("env_kubernetes")
public class KubernetesManager implements ContainerOrchestrator {

    private KubernetesRepositoryManager repositoryManager;
    private KClusterValidator clusterValidator;
    private KServiceLifecycleManager serviceLifecycleManager;
    private KServiceOperationsManager serviceOperationsManager;
    private IngressControllerManager ingressControllerManager;
    private IngressResourceManager ingressResourceManager;

    @Autowired
    public KubernetesManager(KubernetesRepositoryManager repositoryManager,
                             KClusterValidator clusterValidator,
                             KServiceLifecycleManager serviceLifecycleManager,
                             IngressControllerManager ingressControllerManager,
                             IngressResourceManager ingressResourceManager) {
        this.repositoryManager = repositoryManager;
        this.clusterValidator = clusterValidator;
        this.serviceLifecycleManager = serviceLifecycleManager;
        this.ingressControllerManager = ingressControllerManager;
        this.ingressResourceManager = ingressResourceManager;
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(Identifier deploymentId, Identifier applicationId, Identifier clientId, AppDeploymentSpec appDeploymentSpec)
            throws NmServiceRequestVerificationException {
        if(!appDeploymentSpec.getSupportedDeploymentEnvironments().contains(AppDeploymentEnv.KUBERNETES))
            throw new NmServiceRequestVerificationException(
                    "Service deployment not possible with currently used container orchestrator");
        repositoryManager.storeService(new KubernetesNmServiceInfo(deploymentId, applicationId, clientId, KubernetesTemplate.copy(appDeploymentSpec.getKubernetesTemplate())));
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyRequestAndObtainInitialDeploymentDetails(Identifier deploymentId)
            throws NmServiceRequestVerificationException, ContainerOrchestratorInternalErrorException {
        try {
            clusterValidator.checkClusterStatusAndPrerequisites();
        } catch (KClusterCheckException e) {
            throw new ContainerOrchestratorInternalErrorException(e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void prepareDeploymentEnvironment(Identifier deploymentId)
            throws CouldNotPrepareEnvironmentException, ContainerOrchestratorInternalErrorException {
        try {
            Identifier clientId = repositoryManager.loadClientId(deploymentId);
            ingressControllerManager.deployIngressControllerIfMissing(clientId);
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
            Identifier clientId = repositoryManager.loadClientId(deploymentId);
            serviceLifecycleManager.deployService(deploymentId);
            ingressResourceManager.createOrUpdateIngressResource(deploymentId, clientId);
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
            if(!serviceLifecycleManager.checkServiceDeployed(deploymentId))
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
            ingressResourceManager.deleteIngressRule(deploymentId, repositoryManager.loadClientId(deploymentId));
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
        return null;
    }

}
