package net.geant.nmaas.servicedeployment;

import net.geant.nmaas.servicedeployment.exceptions.*;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceInfo;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceSpec;
import net.geant.nmaas.servicedeployment.repository.NmServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ServiceDeploymentCoordinator {

    @Autowired
    @Qualifier("DockerEngine")
    private ContainerOrchestrationProvider orchestrator;

    @Autowired
    private NmServiceRepository serviceRepository;

    public void deployNmService(NmServiceSpec serviceSpec) {
        serviceRepository.storeService(new NmServiceInfo(serviceSpec.name(), NmServiceInfo.ServiceState.INIT, serviceSpec));
        // TODO add subsequent steps
        try {
            orchestrator.verifyRequestAndSelectTarget(serviceSpec.name());
            orchestrator.prepareDeploymentEnvironment(serviceSpec.name());
            orchestrator.deployNmService(serviceSpec.name());
            orchestrator.checkService(serviceSpec.name());
        } catch (CouldNotPrepareEnvironmentException
                | CouldNotDeployNmServiceException
                | CouldNotConnectToOrchestratorException
                | OrchestratorInternalErrorException
                | CouldNotCheckNmServiceStateException exception) {
            try {
                serviceRepository.updateServiceState(serviceSpec.name(), NmServiceInfo.ServiceState.ERROR);
            } catch (NmServiceRepository.ServiceNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void removeNmService(String serviceName) {

    }

}
