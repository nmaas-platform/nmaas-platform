package net.geant.nmaas.servicedeployment;

import net.geant.nmaas.servicedeployment.exceptions.*;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceDeploymentHost;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceState;

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
    void verifyRequestObtainTargetAndNetworkDetails(String serviceName)
            throws CouldNotConnectToOrchestratorException, OrchestratorInternalErrorException;

    /**
     * Executes all initial configuration steps in order to enable further deployment of the service. This step includes
     * dedicated network configuration on the host.
     *
     * @param serviceName service to be deployed
     */
    void prepareDeploymentEnvironment(String serviceName)
            throws CouldNotPrepareEnvironmentException, CouldNotConnectToOrchestratorException, OrchestratorInternalErrorException;

    void deployNmService(String serviceName)
            throws CouldNotDeployNmServiceException, CouldNotConnectToOrchestratorException, OrchestratorInternalErrorException;

    NmServiceState checkService(String serviceName)
            throws CouldNotCheckNmServiceStateException, OrchestratorInternalErrorException, CouldNotConnectToOrchestratorException;

    void removeNmService(String serviceName)
            throws CouldNotDestroyNmServiceException, OrchestratorInternalErrorException, CouldNotConnectToOrchestratorException;

    List<String> listServices(NmServiceDeploymentHost host)
            throws CouldNotConnectToOrchestratorException, OrchestratorInternalErrorException;
}
