package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine;

import com.spotify.docker.client.messages.ContainerConfig;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.nmservice.deployment.ContainerOrchestrationProvider;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container.ContainerConfigBuilder;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container.DockerContainerManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainer;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerNetDetails;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNetwork;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.DockerNetworkManager;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service("DockerEngine")
public class DockerEngineManager implements ContainerOrchestrationProvider {

    @Autowired
    private DockerContainerManager dockerContainerManager;

    @Autowired
    private DockerNetworkManager dockerNetworkManager;

    @Autowired
    private NmServiceRepositoryManager nmServiceRepositoryManager;

    @Autowired
    private DockerHostRepositoryManager dockerHosts;

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyRequestObtainTargetHostAndNetworkDetails(Identifier deploymentId)
            throws NmServiceRequestVerificationException, ContainerOrchestratorInternalErrorException {
        try {
            final Identifier clientId = nmServiceRepositoryManager.loadClientId(deploymentId);
            declareNewNetworkForClientIfNotExists(clientId);
            final DockerNetwork network = dockerNetworkManager.networkForClient(clientId);
            nmServiceRepositoryManager.updateDockerHost(deploymentId, network.getDockerHost());
            final DockerContainer container = dockerContainerManager.declareNewContainerForDeployment(deploymentId);
            nmServiceRepositoryManager.updateDockerContainer(deploymentId, container);
            ContainerConfigBuilder.verifyInput(nmServiceRepositoryManager.loadService(deploymentId));
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (DockerHostNotFoundException dockerHostNotFoundException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Did not find any suitable Docker Host for deployment.");
        }
    }

    private void declareNewNetworkForClientIfNotExists(Identifier clientId)
            throws ContainerOrchestratorInternalErrorException, DockerHostNotFoundException {
        if (!dockerNetworkManager.networkForClientAlreadyConfigured(clientId))
            dockerNetworkManager.declareNewNetworkForClientOnHost(clientId, dockerHosts.loadPreferredDockerHost());
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void prepareDeploymentEnvironment(Identifier deploymentId)
            throws CouldNotPrepareEnvironmentException, ContainerOrchestratorInternalErrorException {
        try {
            final NmServiceInfo service = nmServiceRepositoryManager.loadService(deploymentId);
            deployNetworkForClientOnDockerHostIfNotDoneBefore(service);
            downloadContainerImageOnDockerHost(service);
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new CouldNotPrepareEnvironmentException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (CouldNotCreateContainerNetworkException couldNotCreateContainerNetworkException) {
            throw new CouldNotPrepareEnvironmentException(
                    "Failed to create network -> " + couldNotCreateContainerNetworkException.getMessage());
        }
    }

    private void deployNetworkForClientOnDockerHostIfNotDoneBefore(NmServiceInfo service)
            throws CouldNotCreateContainerNetworkException, ContainerOrchestratorInternalErrorException {
        dockerNetworkManager.deployNetworkForClient(service.getClientId());
    }

    private void downloadContainerImageOnDockerHost(NmServiceInfo service)
            throws ContainerOrchestratorInternalErrorException {
        dockerContainerManager.pullImage(service.getTemplate().getImage(), service.getHost());
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void deployNmService(Identifier deploymentId)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException {
        try {
            final NmServiceInfo service = nmServiceRepositoryManager.loadService(deploymentId);
            final DockerContainer container = service.getDockerContainer();
            container.setNetworkDetails(obtainNetworkDetailsForContainer(service.getClientId()));
            final String containerId = createContainer(service);
            container.setDeploymentId(containerId);
            nmServiceRepositoryManager.updateDockerContainer(deploymentId, container);
            connectContainerToNetwork(service);
            startContainer(service);
            configureRoutingOnStartedContainer(service);
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new CouldNotDeployNmServiceException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (CouldNotDeployNmServiceException couldNotDeployNmServiceException) {
            throw new CouldNotDeployNmServiceException(
                    "Could not deployNmService service -> " + couldNotDeployNmServiceException.getMessage());
        } catch (CouldNotConnectContainerToNetworkException couldNotConnectContainerToNetworkException) {
            throw new CouldNotDeployNmServiceException(
                    "Failed to connect container to network -> " + couldNotConnectContainerToNetworkException.getMessage());
        }
    }

    private DockerContainerNetDetails obtainNetworkDetailsForContainer(Identifier clientId) throws ContainerOrchestratorInternalErrorException {
        return dockerNetworkManager.obtainNetworkDetailsForContainer(clientId);
    }

    private String createContainer(NmServiceInfo service)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException {
        final ContainerConfig config = ContainerConfigBuilder.build(service);
        return dockerContainerManager.create(config, service.getDeploymentId().value(), service.getHost());
    }

    private void connectContainerToNetwork(NmServiceInfo service)
            throws CouldNotConnectContainerToNetworkException, ContainerOrchestratorInternalErrorException {
        dockerNetworkManager.connectContainerToNetwork(service.getClientId(), service.getDockerContainer());
    }

    private void startContainer(NmServiceInfo service)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException {
        dockerContainerManager.start(service.getDockerContainer().getDeploymentId(), service.getHost());
    }

    private void configureRoutingOnStartedContainer(NmServiceInfo service)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException {
        for (String managedDeviceIpAddress : service.getManagedDevicesIpAddresses()) {
            dockerContainerManager.addStaticRoute(
                    service.getDockerContainer().getDeploymentId(),
                    managedDeviceIpAddress,
                    service.getDockerContainer().getNetworkDetails().getIpAddresses().getGateway(),
                    service.getHost());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void checkService(Identifier deploymentId)
            throws ContainerCheckFailedException, DockerNetworkCheckFailedException, ContainerOrchestratorInternalErrorException {
        try {
            final NmServiceInfo service = nmServiceRepositoryManager.loadService(deploymentId);
            checkContainerNetworkAndContainerItself(service);
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        }
    }

    private void checkContainerNetworkAndContainerItself(NmServiceInfo service)
            throws ContainerCheckFailedException, DockerNetworkCheckFailedException, ContainerOrchestratorInternalErrorException {
        dockerContainerManager.checkService(service.getDockerContainer().getDeploymentId(), service.getHost());
        dockerNetworkManager.verifyNetwork(service.getClientId());
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeNmService(Identifier deploymentId)
            throws CouldNotRemoveNmServiceException, ContainerOrchestratorInternalErrorException {
        try {
            final NmServiceInfo service = nmServiceRepositoryManager.loadService(deploymentId);
            removeContainer(service);
            removeNetworkIfNoContainerAttached(service);
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (CouldNotRemoveNmServiceException couldNotRemoveNmServiceException) {
            throw new CouldNotRemoveNmServiceException(
                    "Could not remove container -> " + couldNotRemoveNmServiceException.getMessage());
        } catch (CouldNotRemoveContainerNetworkException couldNotRemoveContainerNetworkException) {
            throw new CouldNotRemoveNmServiceException(
                    "Failed to remove network -> " + couldNotRemoveContainerNetworkException.getMessage());
        }
    }

    private void removeContainer(NmServiceInfo service)
            throws CouldNotRemoveNmServiceException, ContainerOrchestratorInternalErrorException {
        dockerContainerManager.remove(service.getDockerContainer().getDeploymentId(), service.getHost());
        dockerNetworkManager.disconnectContainerFromNetwork(service.getClientId(), service.getDockerContainer().getDeploymentId());
    }

    private void removeNetworkIfNoContainerAttached(NmServiceInfo service)
            throws CouldNotRemoveContainerNetworkException, ContainerOrchestratorInternalErrorException {
        dockerNetworkManager.removeIfNoContainersAttached(service.getClientId());
    }

    @Override
    @Loggable(LogLevel.INFO)
    public List<String> listServices(DockerHost host)
            throws ContainerOrchestratorInternalErrorException {
        return dockerContainerManager.containers(host);
    }

    @Override
    @Loggable(LogLevel.INFO)
    public String info() {
        return "DockerEngine Container Orchestrator";
    }

}
