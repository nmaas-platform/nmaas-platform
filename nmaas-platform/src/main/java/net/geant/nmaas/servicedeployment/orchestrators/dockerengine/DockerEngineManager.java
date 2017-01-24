package net.geant.nmaas.servicedeployment.orchestrators.dockerengine;

import com.spotify.docker.client.messages.ContainerConfig;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostsRepository;
import net.geant.nmaas.servicedeployment.ContainerOrchestrationProvider;
import net.geant.nmaas.servicedeployment.exceptions.*;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceInfo;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceState;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.container.ContainerConfigBuilder;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.container.DockerContainerClient;
import net.geant.nmaas.servicedeployment.repository.NmServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service("DockerEngine")
public class DockerEngineManager implements ContainerOrchestrationProvider {

    @Autowired
    private DockerContainerClient dockerContainerClient;

    @Autowired
    private NmServiceRepository nmServices;

    @Autowired
    private DockerHostsRepository dockerHosts;

    @Override
    public void verifyRequestAndSelectTarget(String serviceName)
            throws CouldNotDeployNmServiceException, CouldNotConnectToOrchestratorException, OrchestratorInternalErrorException {
        try {
            ContainerConfigBuilder.verifyInput(nmServices.loadService(serviceName).getSpec());
            nmServices.updateServiceHost(serviceName, dockerHosts.loadPreferredDockerHost());
            nmServices.updateServiceState(serviceName, NmServiceInfo.ServiceState.VERIFIED);
        } catch (DockerHostNotFoundException e) {
            throw new CouldNotDeployNmServiceException("Did not find any suitable Docker host for deployment.");
        } catch (NmServiceRepository.ServiceNotFoundException serviceNotFoundException) {
            throw new CouldNotDeployNmServiceException("Service not found in repository -> " + serviceNotFoundException.getCause().getMessage());
        } catch (ServiceSpecVerificationException serviceSpecVerificationException) {
            throw new CouldNotDeployNmServiceException("Service spec verification failed -> " + serviceSpecVerificationException.getCause().getMessage());
        }
    }

    @Override
    public void prepareDeploymentEnvironment(String serviceName)
            throws CouldNotPrepareEnvironmentException, CouldNotConnectToOrchestratorException, OrchestratorInternalErrorException {
        try {
            final NmServiceInfo service = nmServices.loadService(serviceName);
            final String imageName = ((DockerEngineContainerTemplate) service.getSpec().template()).getImage();
            dockerContainerClient.pullImage(imageName, (DockerHost) service.getHost());
            nmServices.updateServiceState(serviceName, NmServiceInfo.ServiceState.READY);
        } catch (NmServiceRepository.ServiceNotFoundException serviceNotFoundException) {
            throw new CouldNotPrepareEnvironmentException("Service not found in repository -> " + serviceNotFoundException.getCause().getMessage());
        } catch (UnknownInternalException e) {
            e.printStackTrace();
        } catch (CouldNotDestroyNmServiceException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deployNmService(String serviceName)
            throws CouldNotDeployNmServiceException, CouldNotConnectToOrchestratorException, OrchestratorInternalErrorException {
        try {
            final NmServiceInfo service = nmServices.loadService(serviceName);
            final DockerContainerSpec spec = (DockerContainerSpec) service.getSpec();
            final ContainerConfig config = ContainerConfigBuilder.build(service.getSpec());
            String containerId = dockerContainerClient.deploy(config, spec.uniqueDeploymentName(), (DockerHost) service.getHost());
            nmServices.updateServiceId(serviceName, containerId);
            nmServices.updateServiceState(serviceName, NmServiceInfo.ServiceState.RUNNING);
        } catch (CouldNotDeployNmServiceException couldNotDeployNmServiceException) {
            throw new CouldNotDeployNmServiceException("Could not deploy service -> " + couldNotDeployNmServiceException.getMessage());
        } catch (UnknownInternalException unknownInternalException) {
            throw new OrchestratorInternalErrorException("Could not deploy service -> " + unknownInternalException.getMessage());
        } catch (NmServiceRepository.ServiceNotFoundException serviceNotFoundException) {
            throw new CouldNotDeployNmServiceException("Service not found in repository -> " + serviceNotFoundException.getCause().getMessage());
        }
    }

    @Override
    public NmServiceState checkService(String serviceName)
            throws NmServiceNotFoundException, CouldNotConnectToOrchestratorException, OrchestratorInternalErrorException {
        throw new OrchestratorInternalErrorException("Method not implemented");
    }

    @Override
    public void removeNmService(String serviceName)
            throws CouldNotDestroyNmServiceException, NmServiceNotFoundException, CouldNotConnectToOrchestratorException, OrchestratorInternalErrorException {

    }

    @Override
    public List<String> listServices() throws CouldNotConnectToOrchestratorException, OrchestratorInternalErrorException {
        return null;
    }

    @Override
    public String info() {
        return "DockerEngine Container Orchestrator";
    }

}
