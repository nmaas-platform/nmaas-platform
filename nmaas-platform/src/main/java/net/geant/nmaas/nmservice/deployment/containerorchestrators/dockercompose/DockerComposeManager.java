package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostInvalidException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostStateKeeper;
import net.geant.nmaas.nmservice.deployment.ContainerOrchestrator;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories.DockerComposeTemplateHandlingException;
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
    public void verifyRequestObtainTargetHostAndNetworkDetails(Identifier deploymentId) throws NmServiceRequestVerificationException, ContainerOrchestratorInternalErrorException {
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
            DockerContainerNetDetails netDetails = obtainNetworkDetailsForContainer(service);
            buildAndStoreComposeFile(service, dockerNetworkName, netDetails);
            downloadComposeFileOnDockerHost(service);
            downloadContainerImageOnDockerHost(service);
            updateContainerNetworkDetails(deploymentId, netDetails);
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new CouldNotPrepareEnvironmentException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (CommandExecutionException commandExecutionException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Couldn't execute command on remote host -> " + commandExecutionException.getMessage());
        } catch (CouldNotCreateContainerNetworkException couldNotCreateContainerNetworkException) {
            throw new CouldNotPrepareEnvironmentException(
                    "Failed to create network -> " + couldNotCreateContainerNetworkException.getMessage());
        } catch (DockerComposeTemplateHandlingException dockerComposeTemplateHandlingException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Problem occurred during Docker Compose file preparation -> " + dockerComposeTemplateHandlingException.getMessage());
        } catch (DockerHostInvalidException dockerHostInvalidException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Selected Docker Host can't be used for deployment -> " + dockerHostInvalidException.getMessage());
        } catch (DockerHostNotFoundException dockerHostNotFoundException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Did not find any suitable Docker Host for deployment -> " + dockerHostNotFoundException.getMessage());
        }
    }

    private NmServiceInfo loadService(Identifier deploymentId) throws InvalidDeploymentIdException {
        return repositoryManager.loadService(deploymentId);
    }

    private String deployNetworkForClientOnDockerHostIfNotDoneBefore(NmServiceInfo service)
            throws CouldNotCreateContainerNetworkException, ContainerOrchestratorInternalErrorException {
        return dockerNetworkManager.deployNetworkForClient(service.getClientId());
    }

    private DockerContainerNetDetails obtainNetworkDetailsForContainer(NmServiceInfo service) throws ContainerOrchestratorInternalErrorException {
        return dockerNetworkManager.obtainNetworkDetailsForContainer(service.getClientId());
    }

    private void buildAndStoreComposeFile(NmServiceInfo service, String dockerNetworkName, DockerContainerNetDetails containerNetDetails)
            throws DockerComposeTemplateHandlingException, InvalidDeploymentIdException, DockerHostNotFoundException, DockerHostInvalidException, ContainerOrchestratorInternalErrorException {
        String assignedHostVolume = service.getDockerContainer().getVolumesDetails().getAttachedVolumeName();
        final DockerComposeFileInput dockerComposeFileInput = new DockerComposeFileInput(containerNetDetails.getPublicPort(), assignedHostVolume);
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

    private void updateContainerNetworkDetails(Identifier deploymentId, DockerContainerNetDetails netDetails) throws InvalidDeploymentIdException {
        repositoryManager.updateDockerContainerNetworkDetails(deploymentId, netDetails);
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void deployNmService(Identifier deploymentId)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException {
        try {
            final NmServiceInfo service = loadService(deploymentId);
            deployContainers(service);
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new CouldNotDeployNmServiceException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (CommandExecutionException commandExecutionException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Couldn't execute docker compose up command on remote host -> " + commandExecutionException.getMessage());
        }
    }

    private void deployContainers(NmServiceInfo service) throws CommandExecutionException {
        composeCommandExecutor.executeComposeUpCommand(service.getDeploymentId(), service.getHost());
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

    private void stopAndRemoveContainers(NmServiceInfo service) throws CommandExecutionException {
        composeCommandExecutor.executeComposeStopCommand(service.getDeploymentId(), service.getHost());
        composeCommandExecutor.executeComposeRemoveCommand(service.getDeploymentId(), service.getHost());
    }

    private void removeNetworkIfNoContainerAttached(NmServiceInfo service)
            throws CouldNotRemoveContainerNetworkException, ContainerOrchestratorInternalErrorException {
        dockerNetworkManager.removeIfNoContainersAttached(service.getClientId());
    }

    @Override
    @Loggable(LogLevel.INFO)
    public List<String> listServices(DockerHost host) throws ContainerOrchestratorInternalErrorException {
        return null;
    }

    @Override
    @Loggable(LogLevel.INFO)
    public String info() {
        return "DockerCompose Container Orchestrator";
    }
}
