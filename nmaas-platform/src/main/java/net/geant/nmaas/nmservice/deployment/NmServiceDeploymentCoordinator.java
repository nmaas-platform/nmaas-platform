package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.deploymentorchestration.AppDeploymentStateChangeListener;
import net.geant.nmaas.deploymentorchestration.Identifier;
import net.geant.nmaas.nmservice.DeploymentIdToNmServiceNameMapper;
import net.geant.nmaas.nmservice.InvalidDeploymentIdException;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceSpec;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState.*;

@Service
public class NmServiceDeploymentCoordinator implements NmServiceDeploymentProvider {

    @Autowired
    @Qualifier("DockerEngine")
    private ContainerOrchestrationProvider orchestrator;

    @Autowired
    private NmServiceRepository serviceRepository;

    @Autowired
    private DeploymentIdToNmServiceNameMapper deploymentIdMapper;

    private List<AppDeploymentStateChangeListener> stateChangeListeners = new ArrayList<>();

    @Override
    public void verifyRequest(Identifier deploymentId, NmServiceSpec nmServiceSpec) {
        final String nmServiceName = nmServiceSpec.name();
        deploymentIdMapper.storeMapping(deploymentId, nmServiceName);
        serviceRepository.storeService(new NmServiceInfo(nmServiceName, INIT, nmServiceSpec));
        notifyStateChangeListeners(deploymentId, INIT);
        try {
            orchestrator.verifyRequestObtainTargetAndNetworkDetails(nmServiceName);
            notifyStateChangeListeners(deploymentId, REQUEST_VERIFIED);
        } catch (CouldNotConnectToOrchestratorException
                | ContainerOrchestratorInternalErrorException e) {
            notifyStateChangeListeners(deploymentId, REQUEST_VERIFICATION_FAILED);
        }
    }

    @Override
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
            notifyStateChangeListeners(deploymentId, ENVIRONMENT_PREPARATION_FAILED);
        }
    }

    @Override
    public void deployNmService(Identifier deploymentId) throws InvalidDeploymentIdException {
        String serviceName = null;
        try {
            serviceName = deploymentIdMapper.nmServiceName(deploymentId);
            orchestrator.deployNmService(serviceName);
            notifyStateChangeListeners(deploymentId, DEPLOYED);
        } catch (DeploymentIdToNmServiceNameMapper.EntryNotFoundException e) {
            throw new InvalidDeploymentIdException();
        } catch (CouldNotDeployNmServiceException
                | CouldNotConnectToOrchestratorException
                | ContainerOrchestratorInternalErrorException e) {
            notifyStateChangeListeners(deploymentId, DEPLOYMENT_FAILED);
        }
    }

    @Override
    public void verifyNmService(Identifier deploymentId) throws InvalidDeploymentIdException {
        String serviceName = null;
        try {
            serviceName = deploymentIdMapper.nmServiceName(deploymentId);
            orchestrator.checkService(serviceName);
            notifyStateChangeListeners(deploymentId, VERIFIED);
        } catch (DeploymentIdToNmServiceNameMapper.EntryNotFoundException e) {
            throw new InvalidDeploymentIdException();
        } catch (CouldNotCheckNmServiceStateException
                | CouldNotConnectToOrchestratorException
                | ContainerOrchestratorInternalErrorException e) {
            notifyStateChangeListeners(deploymentId, VERIFICATION_FAILED);
        }
    }

    @Override
    public void removeNmService(Identifier deploymentId) throws InvalidDeploymentIdException {
        String serviceName = null;
        try {
            serviceName = deploymentIdMapper.nmServiceName(deploymentId);
            orchestrator.removeNmService(serviceName);
        } catch (DeploymentIdToNmServiceNameMapper.EntryNotFoundException e) {
            throw new InvalidDeploymentIdException();
        } catch (CouldNotDestroyNmServiceException
                | ContainerOrchestratorInternalErrorException
                | CouldNotConnectToOrchestratorException exception) {
            try {
                serviceRepository.updateServiceState(serviceName, REMOVAL_FAILED);
                notifyStateChangeListeners(deploymentId, REMOVAL_FAILED);
            } catch (NmServiceRepository.ServiceNotFoundException e) { }
        }
    }

    private void notifyStateChangeListeners(Identifier deploymentId, NmServiceDeploymentState state) {
        stateChangeListeners.forEach((listener) -> listener.notifyStateChange(deploymentId, state));
    }

    @Override
    public void addStateChangeListener(AppDeploymentStateChangeListener stateChangeListener) {
        stateChangeListeners.add(stateChangeListener);
    }

}
