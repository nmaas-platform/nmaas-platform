package net.geant.nmaas.servicedeployment.orchestrators.dockerswarm;

import com.spotify.docker.client.messages.swarm.ServiceSpec;
import net.geant.nmaas.servicedeployment.ContainerOrchestrationProvider;
import net.geant.nmaas.servicedeployment.exceptions.*;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceSpec;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceTemplate;
import net.geant.nmaas.servicedeployment.orchestrators.dockerswarm.service.NmServiceDockerSwarmInfo;
import net.geant.nmaas.servicedeployment.orchestrators.dockerswarm.service.ServiceSpecBuilder;
import net.geant.nmaas.servicedeployment.orchestrators.dockerswarm.service.ServiceSpecVerificationException;
import net.geant.nmaas.servicedeployment.orchestrators.dockerswarm.service.ServicesManager;
import net.geant.nmaas.servicedeployment.repository.NmServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service("DockerSwarm")
public class DockerSwarm implements ContainerOrchestrationProvider {

    @Autowired
    private ServicesManager services;

    @Autowired
    private NmServiceRepository repo;

    @Override
    public void deployNmService(NmServiceTemplate template, NmServiceSpec spec)
            throws CouldNotDeployNmServiceException, OrchestratorInternalErrorException, CouldNotConnectToOrchestratorException {
        try {
            ServiceSpec dockerSpec = ServiceSpecBuilder.build(template, spec);
            String serviceId = services.deployService(dockerSpec);
            repo.storeService(new NmServiceDockerSwarmInfo(spec.getName(), serviceId, (NmServiceDockerSwarmSpec) spec, NmServiceDockerSwarmInfo.DesiredState.RUNNING));
        } catch (ServiceSpecVerificationException serviceSpecVerificationException) {
            throw new CouldNotDeployNmServiceException("Could not deploy service -> " + serviceSpecVerificationException.getMessage());
        } catch (CouldNotDeployNmServiceException couldNotDeployNmServiceException) {
            throw new CouldNotDeployNmServiceException("Could not deploy service -> " + couldNotDeployNmServiceException.getMessage());
        } catch (UnknownInternalException unknownInternalException) {
            throw new OrchestratorInternalErrorException("Could not deploy service -> " + unknownInternalException.getMessage());
        }
    }

    @Override
    public void destroyNmService(String serviceName) throws CouldNotDestroyNmServiceException, OrchestratorInternalErrorException, CouldNotConnectToOrchestratorException {
        try {
            String serviceId = repo.getServiceId(serviceName);
            services.destroyService(serviceId);
        } catch (CouldNotDestroyNmServiceException couldNotDestroyNmServiceException) {
            throw new CouldNotDestroyNmServiceException("Could not destroy service -> " + couldNotDestroyNmServiceException.getMessage());
        } catch (UnknownInternalException unknownInternalException) {
            throw new OrchestratorInternalErrorException("Could not destroy service -> " + unknownInternalException.getMessage());
        }
    }

    @Override
    public void verifyService(String serviceName) {
        //TODO
    }

    @Override
    public List<String> listServices() throws CouldNotConnectToOrchestratorException, OrchestratorInternalErrorException {
        return services.listServices();
    }

    @Override
    public String info() {
        return "DockerSwarm Container Orchestrator";
    }
}
