package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentHost;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface ContainerOrchestrationProvider {

    /**
     * Provides basic information about currently used orchestration provider.
     *
     * @return information about the orchestration provider
     */
    String info();

    /**
     * Checks if requested NM service deployment is possible taking into account available resources, currently
     * running services and other constraints.
     * Based on implemented optimisation strategy and current state of the system selects the target host (e.g. server)
     * on which requested service should be deployed.
     * It also obtains the target host network configuration details.
     *
     * @param serviceName service to be deployed
     */
    void verifyRequestObtainTargetHostAndNetworkDetails(String serviceName)
            throws NmServiceRequestVerificationException, CouldNotConnectToOrchestratorException, ContainerOrchestratorInternalErrorException;

    /**
     * Executes all initial configuration steps in order to enable further deployment of the service. This step includes
     * dedicated network configuration on the host.
     *
     * @param serviceName service to be deployed
     */
    void prepareDeploymentEnvironment(String serviceName)
            throws CouldNotPrepareEnvironmentException, CouldNotConnectToOrchestratorException, ContainerOrchestratorInternalErrorException;

    void deployNmService(String serviceName)
            throws CouldNotDeployNmServiceException, CouldNotConnectToOrchestratorException, ContainerOrchestratorInternalErrorException;

    void checkService(String serviceName)
            throws ContainerCheckFailedException, ContainerNetworkCheckFailedException, CouldNotConnectToOrchestratorException, ContainerOrchestratorInternalErrorException;

    void removeNmService(String serviceName)
            throws CouldNotRemoveNmServiceException, ContainerOrchestratorInternalErrorException, CouldNotConnectToOrchestratorException;

    List<String> listServices(NmServiceDeploymentHost host)
            throws CouldNotConnectToOrchestratorException, ContainerOrchestratorInternalErrorException;
}
