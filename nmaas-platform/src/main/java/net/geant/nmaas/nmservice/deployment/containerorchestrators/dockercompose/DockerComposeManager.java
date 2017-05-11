package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostInvalidException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostStateKeeper;
import net.geant.nmaas.nmservice.deployment.ContainerOrchestrationProvider;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
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
@Profile("docker-compose")
public class DockerComposeManager implements ContainerOrchestrationProvider {

    @Autowired
    private NmServiceRepositoryManager repositoryManager;

    @Autowired
    private DockerHostRepositoryManager dockerHosts;

    @Autowired
    private DockerHostStateKeeper dockerHostStateKeeper;

    @Autowired
    private DockerComposeFilePreparer composeFilePreparer;

    @Override
    public void verifyRequestObtainTargetHostAndNetworkDetails(Identifier deploymentId) throws NmServiceRequestVerificationException, ContainerOrchestratorInternalErrorException {
        try {
            final DockerHost selectedDockerHost = dockerHosts.loadPreferredDockerHost();
            final int assignedPublicPort = dockerHostStateKeeper.assignPortForContainer(selectedDockerHost.getName(), null);
            final String assignedHostVolume = constructHostVolumeDirectoryName(selectedDockerHost.getVolumesPath(), deploymentId.value());
            repositoryManager.updateDockerHost(deploymentId, selectedDockerHost);
            // TODO complete
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (DockerHostNotFoundException dockerHostNotFoundException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Did not find any suitable Docker Host for deployment -> " + dockerHostNotFoundException.getMessage());
        } catch (DockerHostInvalidException dockerHostInvalidException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Selected Docker Host can't be used for deployment -> " + dockerHostInvalidException.getMessage());
        }
    }

    private String constructHostVolumeDirectoryName(String baseVolumePath, String deploymentDirectory) {
        return baseVolumePath + "/" + deploymentDirectory + "-1";
    }

    @Override
    public void prepareDeploymentEnvironment(Identifier deploymentId) throws CouldNotPrepareEnvironmentException, ContainerOrchestratorInternalErrorException {

    }

    @Override
    public void deployNmService(Identifier deploymentId) throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException {

    }

    @Override
    public void checkService(Identifier deploymentId) throws ContainerCheckFailedException, DockerNetworkCheckFailedException, ContainerOrchestratorInternalErrorException {

    }

    @Override
    public void removeNmService(Identifier deploymentId) throws CouldNotRemoveNmServiceException, ContainerOrchestratorInternalErrorException {

    }

    @Override
    public List<String> listServices(DockerHost host) throws ContainerOrchestratorInternalErrorException {
        return null;
    }

    @Override
    @Loggable(LogLevel.INFO)
    public String info() {
        return "DockerCompose Container Orchestrator";
    }
}
