package net.geant.nmaas.nmservice.deployment;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.kubernetes.KubernetesClientSetupException;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerCheckFailedException;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerOrchestratorInternalErrorException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotDeployServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotPauseServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotPrepareEnvironmentException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRemoveServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRestartServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotResumeServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRetrieveServiceAccessDetailsException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRetrieveServiceComponentLogsException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRetrieveServiceComponentsException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotUpgradeKubernetesServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotVerifyServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.ServiceRequestVerificationException;
import net.geant.nmaas.orchestration.AppComponentDetails;
import net.geant.nmaas.orchestration.AppComponentLogs;
import net.geant.nmaas.orchestration.AppUiAccessDetails;
import net.geant.nmaas.orchestration.AppUpgradeMode;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.portal.domain.RejectionReason;
import net.geant.nmaas.portal.service.ResourcesLimitService;
import net.geant.nmaas.portal.domain.ResourcesLimitValidationResult;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.DEPLOYED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.DEPLOYMENT_FAILED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.DEPLOYMENT_INITIATED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.ENVIRONMENT_PREPARATION_FAILED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.ENVIRONMENT_PREPARATION_INITIATED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.ENVIRONMENT_PREPARED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.PAUSED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.PAUSE_FAILED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.PAUSE_INITIATED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.REMOVAL_FAILED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.REMOVAL_INITIATED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.REMOVED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.REQUEST_VERIFICATION_FAILED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.REQUEST_VERIFIED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.RESTARTED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.RESTART_FAILED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.RESTART_INITIATED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.RESUMED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.RESUME_FAILED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.RESUME_INITIATED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.UPGRADED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.UPGRADE_FAILED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.UPGRADE_INITIATED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.VERIFICATION_FAILED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.VERIFICATION_INITIATED;
import static net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState.VERIFIED;

/**
 * Default implementation of the {@link NmServiceDeploymentProvider}. Coordinates NM service deployment workflow and
 * delegates particular tasks to currently used {@link ContainerOrchestrator}.
 */
@Component
@RequiredArgsConstructor
public class NmServiceDeploymentCoordinator implements NmServiceDeploymentProvider {

    private final ContainerOrchestrator orchestrator;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ResourcesLimitService resourcesLimitService;

    @Value("${nmaas.service.deployment.check.interval}")
    int serviceDeploymentCheckInternal;

    @Value("${nmaas.service.deployment.max.duration}")
    int serviceDeploymentCheckMaxWaitTime;

    @Override
    @Loggable(LogLevel.TRACE)
    public void verifyRequest(Identifier deploymentId, AppDeployment appDeployment, AppDeploymentSpec deploymentSpec) {
        try {
            orchestrator.verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(deploymentId, appDeployment, deploymentSpec);
            // Validate against resource limits
            ResourcesLimitValidationResult validation = resourcesLimitService
                    .validateNewDeployment(appDeployment.getDomain(), appDeployment.getApplicationId(), 1, deploymentSpec);
            if (!validation.isAccepted()) {
                String errorReason = "Request validation failed for the following reasons: " + validation.getReasons().stream().map(RejectionReason::getDescription).collect(Collectors.joining(","));
                notifyStateChangeListeners(deploymentId, REQUEST_VERIFICATION_FAILED, errorReason);
                throw new ServiceRequestVerificationException(errorReason);
            }
            orchestrator.verifyRequestAndObtainInitialDeploymentDetails(deploymentId);
            notifyStateChangeListeners(deploymentId, REQUEST_VERIFIED);
        } catch (Exception e) {
            notifyStateChangeListeners(deploymentId, REQUEST_VERIFICATION_FAILED, e.getMessage());
            throw new ServiceRequestVerificationException(e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.TRACE)
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
    public void deployService(Identifier deploymentId) {
        try {
            notifyStateChangeListeners(deploymentId, DEPLOYMENT_INITIATED);
            orchestrator.deployNmService(deploymentId);
            notifyStateChangeListeners(deploymentId, DEPLOYED);
        } catch (CouldNotDeployServiceException
                 | ContainerOrchestratorInternalErrorException e) {
            notifyStateChangeListeners(deploymentId, DEPLOYMENT_FAILED, e.getMessage());
            throw new CouldNotDeployServiceException("NM Service deployment failed -> " + e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyService(Identifier deploymentId) {
        try {
            notifyStateChangeListeners(deploymentId, VERIFICATION_INITIATED);
            int currentWaitTime = 0;
            while (currentWaitTime <= serviceDeploymentCheckMaxWaitTime) {
                if (orchestrator.checkService(deploymentId)) {
                    notifyStateChangeListeners(deploymentId, VERIFIED, "");
                    return;
                } else {
                    Thread.sleep(serviceDeploymentCheckInternal * 1000L);
                    currentWaitTime += serviceDeploymentCheckInternal;
                }
            }
            throw new ContainerCheckFailedException("Maximum wait time for container deployment exceeded");
        } catch (ContainerCheckFailedException
                 | ContainerOrchestratorInternalErrorException e) {
            notifyStateChangeListeners(deploymentId, VERIFICATION_FAILED, e.getMessage());
            throw new CouldNotVerifyServiceException("NM Service deployment verification failed -> " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    @Loggable(LogLevel.TRACE)
    public AppUiAccessDetails serviceAccessDetails(Identifier deploymentId) {
        try {
            return orchestrator.serviceAccessDetails(deploymentId);
        } catch (ContainerOrchestratorInternalErrorException e) {
            throw new CouldNotRetrieveServiceAccessDetailsException("Exception thrown during access details retrieval -> " + e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.TRACE)
    public Map<String, String> serviceDeployParameters(Identifier deploymentId) {
        return orchestrator.serviceDeployParameters(deploymentId);
    }

    @Override
    public List<AppComponentDetails> serviceComponents(Identifier deploymentId) {
        try {
            return orchestrator.serviceComponents(deploymentId);
        } catch (ContainerOrchestratorInternalErrorException e) {
            throw new CouldNotRetrieveServiceComponentsException("Exception thrown during components retrieval -> " + e.getMessage());
        }
    }

    @Override
    public AppComponentLogs serviceComponentLogs(Identifier deploymentId, String serviceComponentName, String serviceSubComponentName, int limit) {
        try {
            return orchestrator.serviceComponentLogs(deploymentId, serviceComponentName, serviceSubComponentName, limit);
        } catch (ContainerOrchestratorInternalErrorException e) {
            throw new CouldNotRetrieveServiceComponentLogsException("Exception thrown during component logs retrieval -> " + e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeService(Identifier deploymentId) {
        try {
            notifyStateChangeListeners(deploymentId, REMOVAL_INITIATED);
            orchestrator.removeNmService(deploymentId);
            notifyStateChangeListeners(deploymentId, REMOVED);
        } catch (CouldNotRemoveServiceException
                 | ContainerOrchestratorInternalErrorException e) {
            notifyStateChangeListeners(deploymentId, REMOVAL_FAILED, e.getMessage());
            throw new CouldNotRemoveServiceException("NM Service removal failed -> " + e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void restartService(Identifier deploymentId) {
        try {
            notifyStateChangeListeners(deploymentId, RESTART_INITIATED);
            orchestrator.restartNmService(deploymentId);
            notifyStateChangeListeners(deploymentId, RESTARTED);
        } catch (CouldNotRestartServiceException
                 | ContainerOrchestratorInternalErrorException e) {
            notifyStateChangeListeners(deploymentId, RESTART_FAILED, e.getMessage());
            throw new CouldNotRestartServiceException("Service restart failed -> " + e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.DEBUG)
    public void pauseService(Identifier deploymentId) {
        try {
            notifyStateChangeListeners(deploymentId, PAUSE_INITIATED);
            orchestrator.pauseNmService(deploymentId);
            notifyStateChangeListeners(deploymentId, PAUSED);
        } catch (CouldNotPauseServiceException e) {
            notifyStateChangeListeners(deploymentId, PAUSE_FAILED, e.getMessage());
            throw new CouldNotPauseServiceException("Service scale down failed -> " + e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.DEBUG)
    public void resumeService(Identifier deploymentId) {
        try {
            notifyStateChangeListeners(deploymentId, RESUME_INITIATED);
            orchestrator.resumeNmService(deploymentId);
            notifyStateChangeListeners(deploymentId, RESUMED);
        } catch (KubernetesClientSetupException e) {
            notifyStateChangeListeners(deploymentId, RESUME_FAILED, e.getMessage());
            throw new CouldNotResumeServiceException("Service scale up failed -> " + e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.DEBUG)
    public void upgradeKubernetesService(Identifier deploymentId, AppUpgradeMode mode, Identifier targetApplicationId, KubernetesTemplate kubernetesTemplate) {
        try {
            notifyStateChangeListeners(deploymentId, UPGRADE_INITIATED);
            orchestrator.upgradeKubernetesService(deploymentId, kubernetesTemplate);
            NmServiceDeploymentStateChangeEvent event = new NmServiceDeploymentStateChangeEvent(this, deploymentId, UPGRADED, "");
            event.addDetail(NmServiceDeploymentStateChangeEvent.EventDetailType.UPGRADE_TRIGGER_TYPE, mode.name());
            event.addDetail(NmServiceDeploymentStateChangeEvent.EventDetailType.NEW_APPLICATION_ID, targetApplicationId.value());
            applicationEventPublisher.publishEvent(event);
        } catch (CouldNotUpgradeKubernetesServiceException
                 | ContainerOrchestratorInternalErrorException e) {
            notifyStateChangeListeners(deploymentId, UPGRADE_FAILED, e.getMessage());
            throw new CouldNotUpgradeKubernetesServiceException("NM Service upgrade failed -> " + e.getMessage());
        }
    }

    private void notifyStateChangeListeners(Identifier deploymentId, ServiceDeploymentState state) {
        applicationEventPublisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, state, ""));
    }

    private void notifyStateChangeListeners(Identifier deploymentId, ServiceDeploymentState state, String errorMessage) {
        applicationEventPublisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, state, errorMessage));
    }

}
