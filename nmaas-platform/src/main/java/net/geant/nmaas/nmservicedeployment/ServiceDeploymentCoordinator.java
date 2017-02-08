package net.geant.nmaas.nmservicedeployment;

import net.geant.nmaas.nmservicedeployment.exceptions.*;
import net.geant.nmaas.nmservicedeployment.nmservice.NmServiceInfo;
import net.geant.nmaas.nmservicedeployment.nmservice.NmServiceSpec;
import net.geant.nmaas.nmservicedeployment.repository.NmServiceRepository;
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
        String serviceName = serviceSpec.name();
        serviceRepository.storeService(new NmServiceInfo(serviceName, NmServiceInfo.ServiceState.INIT, serviceSpec));
        try {
            orchestrator.verifyRequestObtainTargetAndNetworkDetails(serviceName);
            orchestrator.prepareDeploymentEnvironment(serviceName);
            orchestrator.deployNmService(serviceName);
            orchestrator.checkService(serviceName);
        } catch (CouldNotPrepareEnvironmentException
                | CouldNotDeployNmServiceException
                | CouldNotConnectToOrchestratorException
                | ContainerOrchestratorInternalErrorException
                | CouldNotCheckNmServiceStateException exception) {
            try {
                serviceRepository.updateServiceState(serviceName, NmServiceInfo.ServiceState.ERROR);
            } catch (NmServiceRepository.ServiceNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void removeNmService(String serviceName) {
        try {
            orchestrator.removeNmService(serviceName);
        } catch (CouldNotDestroyNmServiceException
                | ContainerOrchestratorInternalErrorException
                | CouldNotConnectToOrchestratorException exception) {
            try {
                serviceRepository.updateServiceState(serviceName, NmServiceInfo.ServiceState.ERROR);
            } catch (NmServiceRepository.ServiceNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}
