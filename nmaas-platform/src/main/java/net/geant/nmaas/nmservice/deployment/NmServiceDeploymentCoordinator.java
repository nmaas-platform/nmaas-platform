package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.DeploymentIdToNmServiceNameMapper;
import net.geant.nmaas.nmservice.InvalidDeploymentIdException;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceSpec;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceRepository;
import net.geant.nmaas.orchestration.AppDeploymentStateChangeListener;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState.*;

@Component
public class NmServiceDeploymentCoordinator implements NmServiceDeploymentProvider {

    @Autowired
    @Qualifier("DockerEngine")
    private ContainerOrchestrationProvider orchestrator;

    @Autowired
    private NmServiceRepository serviceRepository;

    @Autowired
    private DeploymentIdToNmServiceNameMapper deploymentIdMapper;

    @Autowired
    AppDeploymentStateChangeListener defaultAppDeploymentStateChangeListener;

    private List<AppDeploymentStateChangeListener> stateChangeListeners = new ArrayList<>();

    @Override
    @Loggable(LogLevel.INFO)
    public NmServiceInfo verifyRequest(Identifier deploymentId, NmServiceSpec nmServiceSpec) {
        final String nmServiceName = nmServiceSpec.name();
        deploymentIdMapper.storeMapping(deploymentId, nmServiceName);
        serviceRepository.storeService(new NmServiceInfo(nmServiceName, INIT, nmServiceSpec));
        try {
            serviceRepository.updateServiceAppDeploymentId(nmServiceName, deploymentId.value());
            orchestrator.verifyRequestObtainTargetHostAndNetworkDetails(nmServiceName);
            notifyStateChangeListeners(deploymentId, REQUEST_VERIFIED);
            return serviceRepository.loadService(nmServiceName);
        } catch (CouldNotConnectToOrchestratorException
                | ContainerOrchestratorInternalErrorException
                | NmServiceRequestVerificationException
                | NmServiceRepository.ServiceNotFoundException e) {
            System.out.println("NM Service request verification failed -> " + e.getMessage());
            notifyStateChangeListeners(deploymentId, REQUEST_VERIFICATION_FAILED);
            return null;
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void prepareDeploymentEnvironment(Identifier deploymentId) throws InvalidDeploymentIdException {
        String serviceName = null;
        try {
            serviceName = deploymentIdMapper.nmServiceName(deploymentId);
            orchestrator.prepareDeploymentEnvironment(serviceName);
            notifyStateChangeListeners(deploymentId, ENVIRONMENT_PREPARED);
        } catch (DeploymentIdToNmServiceNameMapper.EntryNotFoundException e) {
            throw new InvalidDeploymentIdException();
        } catch (CouldNotPrepareEnvironmentException
                | CouldNotConnectToOrchestratorException
                | ContainerOrchestratorInternalErrorException e) {
            System.out.println("NM Service deployment environment preparation failed -> " + e.getMessage());
            notifyStateChangeListeners(deploymentId, ENVIRONMENT_PREPARATION_FAILED);
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void deployNmService(Identifier deploymentId) throws InvalidDeploymentIdException {
        String serviceName = null;
        try {
            serviceName = deploymentIdMapper.nmServiceName(deploymentId);
            notifyStateChangeListeners(deploymentId, DEPLOYMENT_INITIATED);
            orchestrator.deployNmService(serviceName);
            notifyStateChangeListeners(deploymentId, DEPLOYED);
        } catch (DeploymentIdToNmServiceNameMapper.EntryNotFoundException e) {
            throw new InvalidDeploymentIdException();
        } catch (CouldNotDeployNmServiceException
                | CouldNotConnectToOrchestratorException
                | ContainerOrchestratorInternalErrorException e) {
            System.out.println("NM Service deployment failed -> " + e.getMessage());
            notifyStateChangeListeners(deploymentId, DEPLOYMENT_FAILED);
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyNmService(Identifier deploymentId) throws InvalidDeploymentIdException {
        String serviceName = null;
        try {
            serviceName = deploymentIdMapper.nmServiceName(deploymentId);
            orchestrator.checkService(serviceName);
            notifyStateChangeListeners(deploymentId, VERIFIED);
        } catch (DeploymentIdToNmServiceNameMapper.EntryNotFoundException e) {
            throw new InvalidDeploymentIdException();
        } catch (ContainerCheckFailedException
                | ContainerNetworkCheckFailedException
                | CouldNotConnectToOrchestratorException
                | ContainerOrchestratorInternalErrorException e) {
            System.out.println("NM Service deployment verification failed -> " + e.getMessage());
            notifyStateChangeListeners(deploymentId, VERIFICATION_FAILED);
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeNmService(Identifier deploymentId) throws InvalidDeploymentIdException {
        String serviceName = null;
        try {
            serviceName = deploymentIdMapper.nmServiceName(deploymentId);
            orchestrator.removeNmService(serviceName);
            notifyStateChangeListeners(deploymentId, REMOVED);
        } catch (DeploymentIdToNmServiceNameMapper.EntryNotFoundException e) {
            throw new InvalidDeploymentIdException();
        } catch (CouldNotDestroyNmServiceException
                | ContainerOrchestratorInternalErrorException
                | CouldNotConnectToOrchestratorException e) {
            try {
                System.out.println("NM Service removal failed -> " + e.getMessage());
                serviceRepository.updateServiceState(serviceName, REMOVAL_FAILED);
                notifyStateChangeListeners(deploymentId, REMOVAL_FAILED);
            } catch (NmServiceRepository.ServiceNotFoundException ex) { }
        }
    }

    private void notifyStateChangeListeners(Identifier deploymentId, NmServiceDeploymentState state) {
        defaultAppDeploymentStateChangeListener.notifyStateChange(deploymentId, state);
        stateChangeListeners.forEach((listener) -> listener.notifyStateChange(deploymentId, state));
    }

    @Override
    public void addStateChangeListener(AppDeploymentStateChangeListener stateChangeListener) {
        stateChangeListeners.add(stateChangeListener);
    }

}
