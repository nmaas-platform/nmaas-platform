package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import net.geant.nmaas.nmservice.deployment.ContainerOrchestrator;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implements service deployment mechanism on Kubernetes cluster.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Profile("kubernetes")
public class KubernetesManager implements ContainerOrchestrator {

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyDeploymentEnvironmentSupport(List<AppDeploymentEnv> supportedDeploymentEnvironments)
            throws NmServiceRequestVerificationException {
        if(!supportedDeploymentEnvironments.contains(AppDeploymentEnv.KUBERNETES))
            throw new NmServiceRequestVerificationException(
                    "Service deployment not possible with currently used container orchestrator");
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyRequestAndObtainInitialDeploymentDetails(Identifier deploymentId)
            throws NmServiceRequestVerificationException, ContainerOrchestratorInternalErrorException {

    }

    @Override
    @Loggable(LogLevel.INFO)
    public void prepareDeploymentEnvironment(Identifier deploymentId)
            throws CouldNotPrepareEnvironmentException, ContainerOrchestratorInternalErrorException {

    }

    @Override
    @Loggable(LogLevel.INFO)
    public void deployNmService(Identifier deploymentId)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException {

    }

    @Override
    @Loggable(LogLevel.INFO)
    public void checkService(Identifier deploymentId)
            throws ContainerCheckFailedException, DockerNetworkCheckFailedException, ContainerOrchestratorInternalErrorException {

    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeNmService(Identifier deploymentId)
            throws CouldNotRemoveNmServiceException, ContainerOrchestratorInternalErrorException {

    }

    @Override
    public String info() {
        return "Kubernetes Container Orchestrator";
    }

}
