package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerCheckFailedException;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerOrchestratorInternalErrorException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotDeployNmServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotPrepareEnvironmentException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRemoveNmServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRestartNmServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRetrieveNmServiceAccessDetailsException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotVerifyNmServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceRequestVerificationException;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.AppUiAccessDetails;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.DEPLOYED;
import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.DEPLOYMENT_FAILED;
import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.DEPLOYMENT_INITIATED;
import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.ENVIRONMENT_PREPARATION_FAILED;
import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.ENVIRONMENT_PREPARATION_INITIATED;
import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.ENVIRONMENT_PREPARED;
import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.REMOVAL_FAILED;
import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.REMOVAL_INITIATED;
import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.REMOVED;
import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.REQUEST_VERIFICATION_FAILED;
import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.REQUEST_VERIFIED;
import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.RESTARTED;
import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.RESTART_FAILED;
import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.RESTART_INITIATED;
import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.VERIFICATION_FAILED;
import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.VERIFICATION_INITIATED;
import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.VERIFIED;

/**
 * Default implementation of the {@link NmServiceDeploymentProvider}. Coordinates NM service deployment workflow and
 * delegates particular tasks to currently used {@link ContainerOrchestrator}.
 */
@Component
public class NmServiceDeploymentCoordinator implements NmServiceDeploymentProvider {

    private ContainerOrchestrator orchestrator;

    private ApplicationEventPublisher applicationEventPublisher;

    @Value("${nmaas.service.deployment.check.interval}")
    int serviceDeploymentCheckInternal;

    @Value("${nmaas.service.deployment.max.duration}")
    int serviceDeploymentCheckMaxWaitTime;

    @Autowired
    public NmServiceDeploymentCoordinator(ContainerOrchestrator orchestrator, ApplicationEventPublisher applicationEventPublisher) {
        this.orchestrator = orchestrator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyRequest(Identifier deploymentId, AppDeployment appDeployment, AppDeploymentSpec deploymentSpec) {
        try {
            orchestrator.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(deploymentId, appDeployment, deploymentSpec);
            orchestrator.verifyRequestAndObtainInitialDeploymentDetails(deploymentId);
            notifyStateChangeListeners(deploymentId, REQUEST_VERIFIED);
        } catch (Exception e) {
            notifyStateChangeListeners(deploymentId, REQUEST_VERIFICATION_FAILED, e.getMessage());
            throw new NmServiceRequestVerificationException(e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void prepareDeploymentEnvironment(Identifier deploymentId, boolean configFileRepositoryRequired) {
        try {
            notifyStateChangeListeners(deploymentId, ENVIRONMENT_PREPARATION_INITIATED);
            orchestrator.prepareDeploymentEnvironment(deploymentId, configFileRepositoryRequired);
            notifyStateChangeListeners(deploymentId, ENVIRONMENT_PREPARED);
        } catch (CouldNotPrepareEnvironmentException
                | ContainerOrchestratorInternalErrorException e) {
            notifyStateChangeListeners(deploymentId, ENVIRONMENT_PREPARATION_FAILED, e.getMessage());
            throw new CouldNotPrepareEnvironmentException("NM Service deployment environment preparation failed -> " + e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void deployNmService(Identifier deploymentId) {
        try {
            notifyStateChangeListeners(deploymentId, DEPLOYMENT_INITIATED);
            orchestrator.deployNmService(deploymentId);
            notifyStateChangeListeners(deploymentId, DEPLOYED);
        } catch (CouldNotDeployNmServiceException
                | ContainerOrchestratorInternalErrorException e) {
            notifyStateChangeListeners(deploymentId, DEPLOYMENT_FAILED, e.getMessage());
            throw new CouldNotDeployNmServiceException("NM Service deployment failed -> " + e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyNmService(Identifier deploymentId) {
        try {
            notifyStateChangeListeners(deploymentId, VERIFICATION_INITIATED);
            int currentWaitTime = 0;
            while (currentWaitTime <= serviceDeploymentCheckMaxWaitTime) {
                try {
                    orchestrator.checkService(deploymentId);
                    notifyStateChangeListeners(deploymentId, VERIFIED, "");
                    return;
                }catch(ContainerCheckFailedException e) {
                    Thread.sleep(serviceDeploymentCheckInternal * 1000L);
                    currentWaitTime += serviceDeploymentCheckInternal;
                }
            }
            throw new ContainerCheckFailedException("Maximum wait time for container deployment exceeded");
        } catch (ContainerCheckFailedException
                | ContainerOrchestratorInternalErrorException e) {
            notifyStateChangeListeners(deploymentId, VERIFICATION_FAILED, e.getMessage());
            throw new CouldNotVerifyNmServiceException("NM Service deployment verification failed -> " + e.getMessage());
        } catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    @Override
    @Loggable(LogLevel.DEBUG)
    public AppUiAccessDetails serviceAccessDetails(Identifier deploymentId) {
        try {
            return orchestrator.serviceAccessDetails(deploymentId);
        } catch (ContainerOrchestratorInternalErrorException e) {
            throw new CouldNotRetrieveNmServiceAccessDetailsException("Exception thrown during access details retrieval -> " + e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeNmService(Identifier deploymentId) {
        try {
            notifyStateChangeListeners(deploymentId, REMOVAL_INITIATED);
            orchestrator.removeNmService(deploymentId);
            notifyStateChangeListeners(deploymentId, REMOVED);
        } catch (CouldNotRemoveNmServiceException
                | ContainerOrchestratorInternalErrorException e) {
            notifyStateChangeListeners(deploymentId, REMOVAL_FAILED, e.getMessage());
            throw new CouldNotRemoveNmServiceException("NM Service removal failed -> " + e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void restartNmService(Identifier deploymentId) {
        try {
            notifyStateChangeListeners(deploymentId, RESTART_INITIATED);
            orchestrator.restartNmService(deploymentId);
            notifyStateChangeListeners(deploymentId, RESTARTED);
        } catch (CouldNotRestartNmServiceException
                | ContainerOrchestratorInternalErrorException e) {
            notifyStateChangeListeners(deploymentId, RESTART_FAILED, e.getMessage());
            throw new CouldNotRestartNmServiceException("NM Service restart failed -> " + e.getMessage());
        }
    }

    private void notifyStateChangeListeners(Identifier deploymentId, NmServiceDeploymentState state) {
        applicationEventPublisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, state, ""));
    }

    private void notifyStateChangeListeners(Identifier deploymentId, NmServiceDeploymentState state, String errorMessage) {
        applicationEventPublisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, state, errorMessage));
    }

}
