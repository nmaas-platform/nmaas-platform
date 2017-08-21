package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostStateKeeper;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.nmservice.deployment.ContainerOrchestrator;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions.DockerComposeFileTemplateHandlingException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions.DockerComposeFileTemplateNotFoundException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions.InternalErrorException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainer;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerNetDetails;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerVolumesDetails;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNetwork;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.DockerNetworkManager;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Profile("docker-compose")
public class DockerComposeManager implements ContainerOrchestrator {

    @Autowired
    private NmServiceRepositoryManager repositoryManager;

    @Autowired
    private DockerHostRepositoryManager dockerHosts;

    @Autowired
    private DockerHostStateKeeper dockerHostStateKeeper;

    @Autowired
    private DockerComposeFilePreparer composeFilePreparer;

    @Autowired
    private DockerComposeCommandExecutor composeCommandExecutor;

    @Autowired
    private DockerNetworkManager dockerNetworkManager;

    @Autowired
    private StaticRoutingConfigManager routingConfigManager;

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyDeploymentEnvironmentSupport(List<AppDeploymentEnv> supportedDeploymentEnvironments)
            throws NmServiceRequestVerificationException {
        if(!supportedDeploymentEnvironments.contains(AppDeploymentEnv.DOCKER_COMPOSE))
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
            updateDockerHost(deploymentId, network.getDockerHost());
            assignVolumeAndUpdateDockerContainer(deploymentId, network.getDockerHost());
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (DockerHostNotFoundException dockerHostNotFoundException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Did not find any suitable Docker Host for deployment -> " + dockerHostNotFoundException.getMessage());
        }
    }

    private void declareNewNetworkForClientIfNotExists(Identifier clientId)
            throws ContainerOrchestratorInternalErrorException, DockerHostNotFoundException {
        if (!dockerNetworkManager.networkForClientAlreadyConfigured(clientId))
            dockerNetworkManager.declareNewNetworkForClientOnHost(clientId, dockerHosts.loadPreferredDockerHost());
    }

    private void updateDockerHost(Identifier deploymentId, DockerHost dockerHost) throws InvalidDeploymentIdException {
        repositoryManager.updateDockerHost(deploymentId, dockerHost);
    }

    private void assignVolumeAndUpdateDockerContainer(Identifier deploymentId, DockerHost dockerHost) throws InvalidDeploymentIdException {
        String assignedHostVolume = constructHostVolumeDirectoryName(dockerHost.getVolumesPath(), deploymentId.value());
        DockerContainer container = new DockerContainer();
        container.setVolumesDetails(new DockerContainerVolumesDetails(assignedHostVolume));
        repositoryManager.updateDockerContainer(deploymentId, container);
    }

    private String constructHostVolumeDirectoryName(String baseVolumePath, String deploymentDirectory) {
        return baseVolumePath + "/" + deploymentDirectory + "-1";
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void prepareDeploymentEnvironment(Identifier deploymentId)
            throws CouldNotPrepareEnvironmentException, ContainerOrchestratorInternalErrorException {
        try {
            final NmServiceInfo service = loadService(deploymentId);
            String dockerNetworkName = deployNetworkForClientOnDockerHostIfNotDoneBefore(service);
            DockerContainerNetDetails netDetails = obtainNetworkDetailsForContainerAndUpdate(service);
            addContainerToNetwork(loadService(deploymentId));
            buildAndStoreComposeFile(service, dockerNetworkName, netDetails);
            downloadComposeFileOnDockerHost(service);
            downloadContainerImageOnDockerHost(service);
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new CouldNotPrepareEnvironmentException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (CommandExecutionException commandExecutionException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Couldn't execute command on remote host -> " + commandExecutionException.getMessage());
        } catch (CouldNotCreateContainerNetworkException couldNotCreateContainerNetworkException) {
            throw new CouldNotPrepareEnvironmentException(
                    "Failed to create network -> " + couldNotCreateContainerNetworkException.getMessage());
        } catch (DockerComposeFileTemplateHandlingException dockerComposeFileTemplateHandlingException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Problem occurred during Docker Compose file preparation -> " + dockerComposeFileTemplateHandlingException.getMessage());
        } catch (DockerComposeFileTemplateNotFoundException
                | InternalErrorException exception) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Problem occurred while loading Docker Compose template file -> " + exception.getMessage());
        }
    }

    private NmServiceInfo loadService(Identifier deploymentId) throws InvalidDeploymentIdException {
        return repositoryManager.loadService(deploymentId);
    }

    private String deployNetworkForClientOnDockerHostIfNotDoneBefore(NmServiceInfo service)
            throws CouldNotCreateContainerNetworkException, ContainerOrchestratorInternalErrorException {
        return dockerNetworkManager.deployNetworkForClient(service.getClientId());
    }

    private DockerContainerNetDetails obtainNetworkDetailsForContainerAndUpdate(NmServiceInfo service)
            throws ContainerOrchestratorInternalErrorException, InvalidDeploymentIdException {
        DockerContainerNetDetails netDetails = dockerNetworkManager.obtainNetworkDetailsForContainer(service.getClientId(), service.getDeploymentId());
        repositoryManager.updateDockerContainerNetworkDetails(service.getDeploymentId(), netDetails);
        return netDetails;
    }

    private void addContainerToNetwork(NmServiceInfo service) throws ContainerOrchestratorInternalErrorException {
        dockerNetworkManager.addContainerToNetwork(service.getClientId(), service.getDockerContainer());
    }

    private void buildAndStoreComposeFile(NmServiceInfo service, String dockerNetworkName, DockerContainerNetDetails containerNetDetails)
            throws DockerComposeFileTemplateHandlingException, DockerComposeFileTemplateNotFoundException, InvalidDeploymentIdException, InternalErrorException {
        String assignedHostVolume = service.getDockerContainer().getVolumesDetails().getAttachedVolumeName();
        final DockerComposeFileInput dockerComposeFileInput = new DockerComposeFileInput(containerNetDetails.getPublicPort(), assignedHostVolume);
        dockerComposeFileInput.setContainerName(service.getDeploymentId().value());
        dockerComposeFileInput.setContainerIpAddress(containerNetDetails.getIpAddresses().getIpAddressOfContainer());
        dockerComposeFileInput.setDcnNetworkName(dockerNetworkName);
        composeFilePreparer.buildAndStoreComposeFile(service.getDeploymentId(), repositoryManager.loadApplicationId(service.getDeploymentId()), dockerComposeFileInput);
    }

    private void downloadComposeFileOnDockerHost(NmServiceInfo service) throws CommandExecutionException {
        composeCommandExecutor.executeComposeFileDownloadCommand(service.getDeploymentId(), service.getHost());
    }

    private void downloadContainerImageOnDockerHost(NmServiceInfo service) throws CommandExecutionException {
        composeCommandExecutor.executeComposePullCommand(service.getDeploymentId(), service.getHost());
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void deployNmService(Identifier deploymentId)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException {
        try {
            final NmServiceInfo service = loadService(deploymentId);
            deployContainers(service);
            configureRoutingOnStartedContainer(service);
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new CouldNotDeployNmServiceException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (CommandExecutionException commandExecutionException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Problem with docker compose command execution on remote host -> " + commandExecutionException.getMessage());
        }
    }

    private void deployContainers(NmServiceInfo service) throws CommandExecutionException {
        composeCommandExecutor.executeComposeUpCommand(service.getDeploymentId(), service.getHost());
    }

    private void configureRoutingOnStartedContainer(NmServiceInfo service)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException, CommandExecutionException {
        routingConfigManager.configure(service);
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void checkService(Identifier deploymentId)
            throws ContainerCheckFailedException, DockerNetworkCheckFailedException, ContainerOrchestratorInternalErrorException {
        // TODO implement relevant checks
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeNmService(Identifier deploymentId) throws CouldNotRemoveNmServiceException, ContainerOrchestratorInternalErrorException {
        try {
            final NmServiceInfo service = loadService(deploymentId);
            stopAndRemoveContainers(service);
            removeNetworkIfNoContainerAttached(service);
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new CouldNotRemoveNmServiceException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (CommandExecutionException commandExecutionException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Couldn't execute docker compose remove command on remote host -> " + commandExecutionException.getMessage());
        } catch (CouldNotRemoveContainerNetworkException couldNotRemoveContainerNetworkException) {
            throw new CouldNotRemoveNmServiceException(
                    "Failed to remove network -> " + couldNotRemoveContainerNetworkException.getMessage());
        }
    }

    private void stopAndRemoveContainers(NmServiceInfo service) throws CommandExecutionException, ContainerOrchestratorInternalErrorException {
        composeCommandExecutor.executeComposeStopCommand(service.getDeploymentId(), service.getHost());
        composeCommandExecutor.executeComposeRemoveCommand(service.getDeploymentId(), service.getHost());
        dockerNetworkManager.removeContainerFromNetwork(service.getClientId(), service.getDockerContainer().getId());
    }

    private void removeNetworkIfNoContainerAttached(NmServiceInfo service)
            throws CouldNotRemoveContainerNetworkException, ContainerOrchestratorInternalErrorException {
        dockerNetworkManager.removeIfNoContainersAttached(service.getClientId());
    }

    @Override
    @Loggable(LogLevel.INFO)
    public String info() {
        return "DockerCompose Container Orchestrator";
    }
}
