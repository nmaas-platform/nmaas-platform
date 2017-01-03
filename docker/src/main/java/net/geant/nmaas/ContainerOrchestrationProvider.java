package net.geant.nmaas;

import net.geant.nmaas.exception.*;
import net.geant.nmaas.nmservice.NmServiceSpec;
import net.geant.nmaas.nmservice.NmServiceTemplate;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface ContainerOrchestrationProvider {

    String info();

    void deployNmService(NmServiceTemplate template, NmServiceSpec spec)
            throws CouldNotDeployNmServiceException, CouldNotConnectToOrchestratorException, OrchestratorInternalErrorException;

    void verifyService(String serviceName)
            throws NmServiceStateException, CouldNotConnectToOrchestratorException, OrchestratorInternalErrorException;

    void destroyNmService(String serviceName)
            throws NmServiceNotFoundException, CouldNotConnectToOrchestratorException, OrchestratorInternalErrorException;

}
