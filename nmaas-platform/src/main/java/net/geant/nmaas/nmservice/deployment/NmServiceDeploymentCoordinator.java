package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.AppUiAccessDetails;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import static net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class NmServiceDeploymentCoordinator implements NmServiceDeploymentProvider {

    private final static Logger log = LogManager.getLogger(NmServiceDeploymentCoordinator.class);

    @Autowired
    private ContainerOrchestrator orchestrator;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyRequest(Identifier deploymentId, Identifier applicationId, Identifier clientId, AppDeploymentSpec deploymentSpec)
            throws NmServiceRequestVerificationException {
        try {
            orchestrator.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(deploymentId, applicationId, clientId, deploymentSpec);
            orchestrator.verifyRequestAndObtainInitialDeploymentDetails(deploymentId);
            notifyStateChangeListeners(deploymentId, REQUEST_VERIFIED);
        } catch (NmServiceRequestVerificationException
                | ContainerOrchestratorInternalErrorException e) {
            log.error("NM Service request verification failed -> " + e.getMessage());
            notifyStateChangeListeners(deploymentId, REQUEST_VERIFICATION_FAILED);
            throw new NmServiceRequestVerificationException(e.getMessage());
        } catch (Exception e) {
            log.error("NM Service request verification failed -> Unknown exception", e);
            notifyStateChangeListeners(deploymentId, REQUEST_VERIFICATION_FAILED);
            throw new NmServiceRequestVerificationException(e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void prepareDeploymentEnvironment(Identifier deploymentId) throws CouldNotPrepareEnvironmentException {
        try {
            notifyStateChangeListeners(deploymentId, ENVIRONMENT_PREPARATION_INITIATED);
            orchestrator.prepareDeploymentEnvironment(deploymentId);
            notifyStateChangeListeners(deploymentId, ENVIRONMENT_PREPARED);
        } catch (CouldNotPrepareEnvironmentException
                | ContainerOrchestratorInternalErrorException e) {
            notifyStateChangeListeners(deploymentId, ENVIRONMENT_PREPARATION_FAILED);
            throw new CouldNotPrepareEnvironmentException("NM Service deployment environment preparation failed -> " + e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void deployNmService(Identifier deploymentId) throws CouldNotDeployNmServiceException {
        try {
            notifyStateChangeListeners(deploymentId, DEPLOYMENT_INITIATED);
            orchestrator.deployNmService(deploymentId);
            notifyStateChangeListeners(deploymentId, DEPLOYED);
        } catch (CouldNotDeployNmServiceException
                | ContainerOrchestratorInternalErrorException e) {
            notifyStateChangeListeners(deploymentId, DEPLOYMENT_FAILED);
            throw new CouldNotDeployNmServiceException("NM Service deployment failed -> " + e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyNmService(Identifier deploymentId) throws CouldNotVerifyNmServiceException {
        try {
            orchestrator.checkService(deploymentId);
            notifyStateChangeListeners(deploymentId, VERIFIED);
        } catch (ContainerCheckFailedException
                | DockerNetworkCheckFailedException
                | ContainerOrchestratorInternalErrorException e) {
            notifyStateChangeListeners(deploymentId, VERIFICATION_FAILED);
            throw new CouldNotVerifyNmServiceException("NM Service deployment verification failed -> " + e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public AppUiAccessDetails serviceAccessDetails(Identifier deploymentId) throws CouldNotRetrieveNmServiceAccessDetailsException {
        try {
            return orchestrator.serviceAccessDetails(deploymentId);
        } catch (ContainerOrchestratorInternalErrorException e) {
            throw new CouldNotRetrieveNmServiceAccessDetailsException("Exception thrown during access details retrieval -> " + e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeNmService(Identifier deploymentId) throws CouldNotRemoveNmServiceException {
        try {
            orchestrator.removeNmService(deploymentId);
            notifyStateChangeListeners(deploymentId, REMOVED);
        } catch (CouldNotRemoveNmServiceException
                | ContainerOrchestratorInternalErrorException e) {
            notifyStateChangeListeners(deploymentId, REMOVAL_FAILED);
            throw new CouldNotRemoveNmServiceException("NM Service removal failed -> " + e.getMessage());
        }
    }

    private void notifyStateChangeListeners(Identifier deploymentId, NmServiceDeploymentState state) {
        applicationEventPublisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, state));
    }

}
