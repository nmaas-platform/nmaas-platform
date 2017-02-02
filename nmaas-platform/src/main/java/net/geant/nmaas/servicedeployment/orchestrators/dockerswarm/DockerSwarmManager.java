package net.geant.nmaas.servicedeployment.orchestrators.dockerswarm;

import com.spotify.docker.client.messages.swarm.ServiceSpec;
import net.geant.nmaas.externalservices.inventory.dockerswams.DockerSwarmNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerswams.DockerSwarmsRepository;
import net.geant.nmaas.servicedeployment.ContainerOrchestrationProvider;
import net.geant.nmaas.servicedeployment.exceptions.*;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceDeploymentHost;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceInfo;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceSpec;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceState;
import net.geant.nmaas.servicedeployment.orchestrators.dockerswarm.service.ServiceSpecBuilder;
import net.geant.nmaas.servicedeployment.orchestrators.dockerswarm.service.ServicesClient;
import net.geant.nmaas.servicedeployment.repository.NmServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The DockerSwarmManager class is the main entry point to Docker Swarm orchestrator implementation.
 *
 * Implementation of Docker Swarm support was dropped and therefore it is not yet fully functional.
 * Some of the methods remained not implemented.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service("DockerSwarm")
public class DockerSwarmManager implements ContainerOrchestrationProvider {

    @Autowired
    private ServicesClient servicesManager;

    @Autowired
    private NmServiceRepository nmServices;

    @Autowired
    private DockerSwarmsRepository dockerSwarms;

    @Override
    public void verifyRequestObtainTargetAndNetworkDetails(String serviceName)
            throws OrchestratorInternalErrorException {
        throw new OrchestratorInternalErrorException("DockerSwarm orchestrator is not currently supported");
    }

    @Override
    public void prepareDeploymentEnvironment(String serviceName)
            throws OrchestratorInternalErrorException {
        throw new OrchestratorInternalErrorException("Method not implemented");
    }

    @Override
    public void deployNmService(String serviceName)
            throws CouldNotDeployNmServiceException, OrchestratorInternalErrorException, CouldNotConnectToOrchestratorException {
        try {
            final NmServiceSpec spec = nmServices.loadService(serviceName).getSpec();
            ServiceSpec dockerSpec = ServiceSpecBuilder.build(spec);
            String serviceId = servicesManager.deployService(dockerSpec, dockerSwarms.loadPreferredDockerSwarmManager());
            nmServices.updateServiceId(serviceName, serviceId);
            nmServices.updateServiceState(serviceName, NmServiceInfo.ServiceState.DEPLOYED);
        } catch (ServiceVerificationException serviceSpecVerificationException) {
            throw new CouldNotDeployNmServiceException("Could not deploy service -> " + serviceSpecVerificationException.getMessage());
        } catch (CouldNotDeployNmServiceException couldNotDeployNmServiceException) {
            throw new CouldNotDeployNmServiceException("Could not deploy service -> " + couldNotDeployNmServiceException.getMessage());
        } catch (DockerSwarmNotFoundException dockerSwarmNotFoundException) {
            throw new OrchestratorInternalErrorException("Could not deploy service -> " + dockerSwarmNotFoundException.getMessage());
        } catch (NmServiceRepository.ServiceNotFoundException serviceNotFoundException) {
            throw new OrchestratorInternalErrorException("Could not deploy service -> " + serviceNotFoundException.getMessage());
        }
    }

    @Override
    public void removeNmService(String serviceName) throws CouldNotDestroyNmServiceException, OrchestratorInternalErrorException {
        try {
            String serviceId = nmServices.getServiceId(serviceName);
            servicesManager.destroyService(serviceId, dockerSwarms.loadPreferredDockerSwarmManager());
        } catch (CouldNotDestroyNmServiceException couldNotDestroyNmServiceException) {
            throw new CouldNotDestroyNmServiceException("Could not destroy service -> " + couldNotDestroyNmServiceException.getMessage());
        } catch (DockerSwarmNotFoundException dockerSwarmNotFoundException) {
            throw new OrchestratorInternalErrorException("Could not destroy service -> " + dockerSwarmNotFoundException.getMessage());
        } catch (NmServiceRepository.ServiceNotFoundException serviceNotFoundException) {
            throw new OrchestratorInternalErrorException("Could not destroy service -> " + serviceNotFoundException.getMessage());
        } catch (CouldNotConnectToOrchestratorException couldNotConnectToOrchestratorException) {
            throw new CouldNotDestroyNmServiceException("Could not destroy service -> " + couldNotConnectToOrchestratorException.getMessage());
        }
    }

    @Override
    public NmServiceState checkService(String serviceName) throws OrchestratorInternalErrorException {
        throw new OrchestratorInternalErrorException("Method not implemented");
    }

    @Override
    public List<String> listServices(NmServiceDeploymentHost host) throws OrchestratorInternalErrorException {
        return servicesManager.listServices((net.geant.nmaas.externalservices.inventory.dockerswams.DockerSwarmManager) host);
    }

    @Override
    public String info() {
        return "DockerSwarm Container Orchestrator";
    }
}
