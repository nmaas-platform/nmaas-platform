package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine;

import com.spotify.docker.client.messages.ContainerConfig;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.nmservice.deployment.ContainerOrchestrator;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container.ContainerConfigBuilder;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container.DockerContainerManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainer;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerNetDetails;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNetwork;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.DockerNetworkManager;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Profile("docker-engine")
public class DockerEngineManager implements ContainerOrchestrator {

    @Autowired
    private DockerContainerManager dockerContainerManager;

    @Autowired
    private DockerNetworkManager dockerNetworkManager;

    @Autowired
    private NmServiceRepositoryManager repositoryManager;

    @Autowired
    private DockerHostRepositoryManager dockerHosts;

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyDeploymentEnvironmentSupport(List<AppDeploymentEnv> supportedDeploymentEnvironments)
            throws NmServiceRequestVerificationException {
        if(!supportedDeploymentEnvironments.contains(AppDeploymentEnv.DOCKER_ENGINE))
            throw new NmServiceRequestVerificationException(
                    "Service deployment not possible with currently used container orchestrator");
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyRequestAndObtainInitialDeploymentDetails(Identifier deploymentId)
            throws NmServiceRequestVerificationException, ContainerOrchestratorInternalErrorException {
        try {
            final Identifier clientId = repositoryManager.loadClientId(deploymentId);
            declareNewNetworkForClientIfNotExists(clientId);
            final DockerNetwork network = dockerNetworkManager.networkForClient(clientId);
            repositoryManager.updateDockerHost(deploymentId, network.getDockerHost());
            final DockerContainer container = dockerContainerManager.declareNewContainerForDeployment(deploymentId);
            repositoryManager.updateDockerContainer(deploymentId, container);
            ContainerConfigBuilder.verifyInitInput(repositoryManager.loadService(deploymentId));
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
            final NmServiceInfo service = repositoryManager.loadService(deploymentId);
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
            final NmServiceInfo service = repositoryManager.loadService(deploymentId);
            final DockerContainerNetDetails netDetails = obtainNetworkDetailsFoNewContainerAndStoreInRepository(deploymentId);
            service.getDockerContainer().setNetworkDetails(netDetails);
            final String containerId = createContainerAndStoreIdInRepository(service);
            service.getDockerContainer().setDeploymentId(containerId);
            connectContainerToNetwork(service);
            startContainer(service);
            configureRoutingOnStartedContainer(service);
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new CouldNotDeployNmServiceException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (CouldNotDeployNmServiceException couldNotDeployNmServiceException) {
            throw new CouldNotDeployNmServiceException(
                    "Could not deploy NmService service -> " + couldNotDeployNmServiceException.getMessage());
        } catch (CouldNotConnectContainerToNetworkException couldNotConnectContainerToNetworkException) {
            throw new CouldNotDeployNmServiceException(
                    "Failed to connect container to network -> " + couldNotConnectContainerToNetworkException.getMessage());
        } catch (NmServiceRequestVerificationException nmServiceRequestVerificationException) {
            throw new CouldNotDeployNmServiceException(
                    "Container spec is missing some parameters -> " + nmServiceRequestVerificationException.getMessage());
        }
    }

    private DockerContainerNetDetails obtainNetworkDetailsFoNewContainerAndStoreInRepository(Identifier deploymentId)
            throws InvalidDeploymentIdException, ContainerOrchestratorInternalErrorException {
        final Identifier clientId = repositoryManager.loadClientId(deploymentId);
        final DockerContainerNetDetails netDetails = obtainNetworkDetailsForContainer(clientId, deploymentId);
        repositoryManager.updateDockerContainerNetworkDetails(deploymentId, netDetails);
        return netDetails;
    }

    private DockerContainerNetDetails obtainNetworkDetailsForContainer(Identifier clientId, Identifier deploymentId)
            throws ContainerOrchestratorInternalErrorException {
        return dockerNetworkManager.obtainNetworkDetailsForContainer(clientId, deploymentId);
    }

    private String createContainerAndStoreIdInRepository(NmServiceInfo service)
            throws NmServiceRequestVerificationException, CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException, InvalidDeploymentIdException {
        ContainerConfigBuilder.verifyFinalInput(service);
        final String containerId = createContainer(service);
        repositoryManager.updateDockerContainerDeploymentId(service.getDeploymentId(), containerId);
        return containerId;
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
            final NmServiceInfo service = repositoryManager.loadService(deploymentId);
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
            final NmServiceInfo service = repositoryManager.loadService(deploymentId);
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
    public String info() {
        return "DockerEngine Container Orchestrator";
    }

}
