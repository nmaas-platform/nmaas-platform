package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostInvalidException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostStateKeeper;
import net.geant.nmaas.nmservice.deployment.ContainerOrchestrator;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories.DockerComposeTemplateHandlingException;
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
            final DockerHost selectedDockerHost = dockerHosts.loadPreferredDockerHost();
            final int assignedPublicPort = dockerHostStateKeeper.assignPortForContainer(selectedDockerHost.getName(), null);
            final String assignedHostVolume = constructHostVolumeDirectoryName(selectedDockerHost.getVolumesPath(), deploymentId.value());
            composeFilePreparer.buildAndStoreComposeFile(deploymentId, repositoryManager.loadApplicationId(deploymentId), assignedPublicPort, assignedHostVolume);
            repositoryManager.updateDockerHost(deploymentId, selectedDockerHost);
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (DockerHostNotFoundException dockerHostNotFoundException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Did not find any suitable Docker Host for deployment -> " + dockerHostNotFoundException.getMessage());
        } catch (DockerHostInvalidException dockerHostInvalidException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Selected Docker Host can't be used for deployment -> " + dockerHostInvalidException.getMessage());
        } catch (DockerComposeTemplateHandlingException dockerComposeTemplateHandlingException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Problem occurred during Docker Compose file preparation -> " + dockerComposeTemplateHandlingException.getMessage());
        }
    }

    private String constructHostVolumeDirectoryName(String baseVolumePath, String deploymentDirectory) {
        return baseVolumePath + "/" + deploymentDirectory + "-1";
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void prepareDeploymentEnvironment(Identifier deploymentId)
            throws CouldNotPrepareEnvironmentException, ContainerOrchestratorInternalErrorException {
        try {
            final NmServiceInfo service = repositoryManager.loadService(deploymentId);
            composeCommandExecutor.executeComposeFileDownloadCommand(deploymentId, service.getHost());
            composeCommandExecutor.executeComposePullCommand(deploymentId, service.getHost());
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new CouldNotPrepareEnvironmentException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (CommandExecutionException commandExecutionException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Couldn't execute command on remote host -> " + commandExecutionException.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void deployNmService(Identifier deploymentId)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException {
        try {
            final NmServiceInfo service = repositoryManager.loadService(deploymentId);
            composeCommandExecutor.executeComposeUpCommand(deploymentId, service.getHost());
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new CouldNotDeployNmServiceException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (CommandExecutionException commandExecutionException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Couldn't execute docker compose up command on remote host -> " + commandExecutionException.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void checkService(Identifier deploymentId)
            throws ContainerCheckFailedException, DockerNetworkCheckFailedException, ContainerOrchestratorInternalErrorException {

    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeNmService(Identifier deploymentId) throws CouldNotRemoveNmServiceException, ContainerOrchestratorInternalErrorException {
        try {
            final NmServiceInfo service = repositoryManager.loadService(deploymentId);
            composeCommandExecutor.executeComposeStopCommand(deploymentId, service.getHost());
            composeCommandExecutor.executeComposeRemoveCommand(deploymentId, service.getHost());
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new CouldNotRemoveNmServiceException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (CommandExecutionException commandExecutionException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Couldn't execute docker compose remove command on remote host -> " + commandExecutionException.getMessage());
        }
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
