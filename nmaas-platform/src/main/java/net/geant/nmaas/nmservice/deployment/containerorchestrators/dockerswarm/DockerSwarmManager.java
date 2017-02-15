package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerswarm;

import com.spotify.docker.client.messages.swarm.ServiceSpec;
import net.geant.nmaas.externalservices.inventory.dockerswams.DockerSwarmNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerswams.DockerSwarmsRepository;
import net.geant.nmaas.nmservice.deployment.ContainerOrchestrationProvider;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerswarm.service.SwarmServiceSpecBuilder;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerswarm.service.SwarmServicesClient;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentHost;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceSpec;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceRepository;
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
    private SwarmServicesClient servicesManager;

    @Autowired
    private NmServiceRepository nmServices;

    @Autowired
    private DockerSwarmsRepository dockerSwarms;

    @Override
    public void verifyRequestObtainTargetHostAndNetworkDetails(String serviceName)
            throws ContainerOrchestratorInternalErrorException {
        throw new ContainerOrchestratorInternalErrorException("DockerSwarm orchestrator is not currently supported");
    }

    @Override
    public void prepareDeploymentEnvironment(String serviceName)
            throws ContainerOrchestratorInternalErrorException {
        throw new ContainerOrchestratorInternalErrorException("Method not implemented");
    }

    @Override
    public void deployNmService(String serviceName)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException, CouldNotConnectToOrchestratorException {
        try {
            final NmServiceSpec spec = nmServices.loadService(serviceName).getSpec();
            ServiceSpec dockerSpec = SwarmServiceSpecBuilder.build(spec);
            String serviceId = servicesManager.deployService(dockerSpec, dockerSwarms.loadPreferredDockerSwarmManager());
            nmServices.updateServiceId(serviceName, serviceId);
            nmServices.updateServiceState(serviceName, NmServiceDeploymentState.DEPLOYED);
        } catch (NmServiceRequestVerificationException serviceSpecVerificationException) {
            throw new CouldNotDeployNmServiceException("Could not deploy service -> " + serviceSpecVerificationException.getMessage());
        } catch (CouldNotDeployNmServiceException couldNotDeployNmServiceException) {
            throw new CouldNotDeployNmServiceException("Could not deploy service -> " + couldNotDeployNmServiceException.getMessage());
        } catch (DockerSwarmNotFoundException dockerSwarmNotFoundException) {
            throw new ContainerOrchestratorInternalErrorException("Could not deploy service -> " + dockerSwarmNotFoundException.getMessage());
        } catch (NmServiceRepository.ServiceNotFoundException serviceNotFoundException) {
            throw new ContainerOrchestratorInternalErrorException("Could not deploy service -> " + serviceNotFoundException.getMessage());
        }
    }

    @Override
    public void removeNmService(String serviceName) throws CouldNotDestroyNmServiceException, ContainerOrchestratorInternalErrorException {
        try {
            String serviceId = nmServices.getServiceId(serviceName);
            servicesManager.destroyService(serviceId, dockerSwarms.loadPreferredDockerSwarmManager());
            nmServices.updateServiceState(serviceName, NmServiceDeploymentState.REMOVED);
        } catch (CouldNotDestroyNmServiceException couldNotDestroyNmServiceException) {
            throw new CouldNotDestroyNmServiceException("Could not destroy service -> " + couldNotDestroyNmServiceException.getMessage());
        } catch (DockerSwarmNotFoundException dockerSwarmNotFoundException) {
            throw new ContainerOrchestratorInternalErrorException("Could not destroy service -> " + dockerSwarmNotFoundException.getMessage());
        } catch (NmServiceRepository.ServiceNotFoundException serviceNotFoundException) {
            throw new ContainerOrchestratorInternalErrorException("Could not destroy service -> " + serviceNotFoundException.getMessage());
        } catch (CouldNotConnectToOrchestratorException couldNotConnectToOrchestratorException) {
            throw new CouldNotDestroyNmServiceException("Could not destroy service -> " + couldNotConnectToOrchestratorException.getMessage());
        }
    }

    @Override
    public void checkService(String serviceName) throws ContainerOrchestratorInternalErrorException {
        throw new ContainerOrchestratorInternalErrorException("Method not implemented");
    }

    @Override
    public List<String> listServices(NmServiceDeploymentHost host) throws ContainerOrchestratorInternalErrorException {
        return servicesManager.listServices((net.geant.nmaas.externalservices.inventory.dockerswams.DockerSwarmManager) host);
    }

    @Override
    public String info() {
        return "DockerSwarm Container Orchestrator";
    }
}
