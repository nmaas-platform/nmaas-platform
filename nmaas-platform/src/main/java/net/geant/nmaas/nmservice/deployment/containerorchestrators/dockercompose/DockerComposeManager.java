package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.nmservice.deployment.ContainerOrchestrationProvider;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Profile("docker-compose")
public class DockerComposeManager implements ContainerOrchestrationProvider {

    @Override
    public void verifyRequestObtainTargetHostAndNetworkDetails(Identifier deploymentId) throws NmServiceRequestVerificationException, ContainerOrchestratorInternalErrorException {

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
