package net.geant.nmaas.servicedeployment.orchestrators.dockerswarm;

import com.spotify.docker.client.messages.swarm.ServiceSpec;
import net.geant.nmaas.externalservices.inventory.dockerswams.DockerSwarmNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerswams.DockerSwarmsRepository;
import net.geant.nmaas.servicedeployment.ContainerOrchestrationProvider;
import net.geant.nmaas.servicedeployment.exceptions.*;
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
    public void verifyRequestAndSelectTarget(String serviceName)
            throws CouldNotDeployNmServiceException, CouldNotConnectToOrchestratorException, OrchestratorInternalErrorException {
        throw new OrchestratorInternalErrorException("DockerSwarm orchestrator is not currently supported");
    }

    @Override
    public void prepareDeploymentEnvironment(String serviceName)
            throws CouldNotPrepareEnvironmentException, CouldNotConnectToOrchestratorException, OrchestratorInternalErrorException {
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
        } catch (ServiceSpecVerificationException serviceSpecVerificationException) {
            throw new CouldNotDeployNmServiceException("Could not deploy service -> " + serviceSpecVerificationException.getMessage());
        } catch (CouldNotDeployNmServiceException couldNotDeployNmServiceException) {
            throw new CouldNotDeployNmServiceException("Could not deploy service -> " + couldNotDeployNmServiceException.getMessage());
        } catch (UnknownInternalException unknownInternalException) {
            throw new OrchestratorInternalErrorException("Could not deploy service -> " + unknownInternalException.getMessage());
        } catch (DockerSwarmNotFoundException dockerSwarmNotFoundException) {
            throw new OrchestratorInternalErrorException("Could not deploy service -> " + dockerSwarmNotFoundException.getMessage());
        } catch (NmServiceRepository.ServiceNotFoundException serviceNotFoundException) {
            throw new OrchestratorInternalErrorException("Could not deploy service -> " + serviceNotFoundException.getMessage());
        }
    }

    @Override
    public void removeNmService(String serviceName) throws CouldNotDestroyNmServiceException, OrchestratorInternalErrorException, CouldNotConnectToOrchestratorException {
        try {
            String serviceId = nmServices.getServiceId(serviceName);
            servicesManager.destroyService(serviceId, dockerSwarms.loadPreferredDockerSwarmManager());
        } catch (CouldNotDestroyNmServiceException couldNotDestroyNmServiceException) {
            throw new CouldNotDestroyNmServiceException("Could not destroy service -> " + couldNotDestroyNmServiceException.getMessage());
        } catch (UnknownInternalException unknownInternalException) {
            throw new OrchestratorInternalErrorException("Could not destroy service -> " + unknownInternalException.getMessage());
        } catch (DockerSwarmNotFoundException dockerSwarmNotFoundException) {
            throw new OrchestratorInternalErrorException("Could not destroy service -> " + dockerSwarmNotFoundException.getMessage());
        } catch (NmServiceRepository.ServiceNotFoundException serviceNotFoundException) {
            throw new OrchestratorInternalErrorException("Could not destroy service -> " + serviceNotFoundException.getMessage());
        }
    }

    @Override
    public NmServiceState checkService(String serviceName) throws OrchestratorInternalErrorException {
        throw new OrchestratorInternalErrorException("Method not implemented");
    }

    @Override
    public List<String> listServices() throws CouldNotConnectToOrchestratorException, OrchestratorInternalErrorException {
        try {
            return servicesManager.listServices(dockerSwarms.loadPreferredDockerSwarmManager());
        } catch (DockerSwarmNotFoundException dockerSwarmNotFoundException) {
            throw new OrchestratorInternalErrorException("Could not destroy service -> " + dockerSwarmNotFoundException.getMessage());
        }
    }

    @Override
    public String info() {
        return "DockerSwarm Container Orchestrator";
    }
}
