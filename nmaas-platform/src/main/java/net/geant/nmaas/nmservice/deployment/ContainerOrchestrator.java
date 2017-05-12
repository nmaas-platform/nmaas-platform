package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;
import net.geant.nmaas.orchestration.entities.Identifier;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface ContainerOrchestrator {

    /**
     * Provides basic information about currently used container orchestrator.
     *
     * @return information about the orchestration provider
     */
    String info();

    /**
     * Verifies if currently used container orchestrator is on the list of supported deployment environments specified
     * for NM service being requested.
     *
     * @param supportedDeploymentEnvironments list of deployment environments supported by an application
     */
    void verifyDeploymentEnvironmentSupport(List<AppDeploymentEnv> supportedDeploymentEnvironments)
            throws NmServiceRequestVerificationException;

    /**
     * Checks if requested NM service deployment is possible taking into account available resources, currently
     * running services and other constraints.
     * Based on implemented optimisation strategy and current state of the system selects the target host (e.g. server)
     * on which requested service should be deployed.
     * It also obtains the target host network configuration details.
     *
     * @param deploymentId unique identifier of service deployment
     */
    void verifyRequestObtainTargetHostAndNetworkDetails(Identifier deploymentId)
            throws NmServiceRequestVerificationException, ContainerOrchestratorInternalErrorException;

    /**
     * Executes all initial configuration steps in order to enable further deployment of the service. This step includes
     * dedicated network configuration on the host.
     *
     * @param deploymentId unique identifier of service deployment
     */
    void prepareDeploymentEnvironment(Identifier deploymentId)
            throws CouldNotPrepareEnvironmentException, ContainerOrchestratorInternalErrorException;

    void deployNmService(Identifier deploymentId)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException;

    void checkService(Identifier deploymentId)
            throws ContainerCheckFailedException, DockerNetworkCheckFailedException, ContainerOrchestratorInternalErrorException;

    void removeNmService(Identifier deploymentId)
            throws CouldNotRemoveNmServiceException, ContainerOrchestratorInternalErrorException;

    List<String> listServices(DockerHost host)
            throws ContainerOrchestratorInternalErrorException;
}
