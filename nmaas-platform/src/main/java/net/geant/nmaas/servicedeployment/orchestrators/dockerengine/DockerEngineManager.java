package net.geant.nmaas.servicedeployment.orchestrators.dockerengine;

import com.spotify.docker.client.messages.ContainerConfig;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostsRepository;
import net.geant.nmaas.servicedeployment.ContainerOrchestrationProvider;
import net.geant.nmaas.servicedeployment.exceptions.*;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceDeploymentHost;
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
            throw new CouldNotDeployNmServiceException("Service not found in repository -> " + serviceNotFoundException.getMessage());
        } catch (ServiceSpecVerificationException serviceSpecVerificationException) {
            throw new CouldNotDeployNmServiceException("Service spec verification failed -> " + serviceSpecVerificationException.getMessage());
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
            throw new CouldNotPrepareEnvironmentException("Service not found in repository -> " + serviceNotFoundException.getMessage());
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
            final ContainerConfig config = ContainerConfigBuilder.build(service.getSpec(), service.getHost());
            String containerId = dockerContainerClient.deploy(config, spec.uniqueDeploymentName(), (DockerHost) service.getHost());
            nmServices.updateServiceId(serviceName, containerId);
            nmServices.updateServiceState(serviceName, NmServiceInfo.ServiceState.RUNNING);
        } catch (CouldNotDeployNmServiceException couldNotDeployNmServiceException) {
            throw new CouldNotDeployNmServiceException("Could not deploy service -> " + couldNotDeployNmServiceException.getMessage());
        } catch (UnknownInternalException unknownInternalException) {
            throw new OrchestratorInternalErrorException("Could not deploy service -> " + unknownInternalException.getMessage());
        } catch (NmServiceRepository.ServiceNotFoundException serviceNotFoundException) {
            throw new CouldNotDeployNmServiceException("Service not found in repository -> " + serviceNotFoundException.getMessage());
        }
    }

    @Override
    public NmServiceState checkService(String serviceName) throws CouldNotCheckNmServiceStateException, OrchestratorInternalErrorException {
        try {
            final NmServiceInfo service = nmServices.loadService(serviceName);
            return dockerContainerClient.checkService(service.getDeploymentId(), (DockerHost) service.getHost());
        } catch (NmServiceRepository.ServiceNotFoundException serviceNotFoundException) {
            throw new CouldNotCheckNmServiceStateException("Service not found in repository -> " + serviceNotFoundException.getMessage());
        }  catch (ContainerNotFoundException containerNotFoundException) {
            throw new CouldNotCheckNmServiceStateException("Container not found on the deployment host -> " + containerNotFoundException.getMessage());
        } catch (OrchestratorInternalErrorException orchestratorInternalErrorException) {
            throw new OrchestratorInternalErrorException("Could not check service state -> " + orchestratorInternalErrorException.getMessage());
        } catch (UnknownInternalException unknownInternalException) {
            throw new OrchestratorInternalErrorException("Could not check service state -> " + unknownInternalException.getMessage());
        }
    }

    @Override
    public void removeNmService(String serviceName) throws CouldNotDestroyNmServiceException, OrchestratorInternalErrorException {
        try {
            final NmServiceInfo service = nmServices.loadService(serviceName);
            dockerContainerClient.remove(service.getDeploymentId(), (DockerHost) service.getHost());
            nmServices.updateServiceState(serviceName, NmServiceInfo.ServiceState.STOPPED);
        } catch (UnknownInternalException e) {
            e.printStackTrace();
        } catch (NmServiceRepository.ServiceNotFoundException serviceNotFoundException) {
            throw new CouldNotDestroyNmServiceException("Service not found in repository -> " + serviceNotFoundException.getMessage());
        } catch (CouldNotDestroyNmServiceException couldNotDestroyNmServiceException) {
            throw new CouldNotDestroyNmServiceException("Could not destroy service -> " + couldNotDestroyNmServiceException.getMessage());
        } catch (CouldNotConnectToOrchestratorException unknownInternalException) {
            throw new OrchestratorInternalErrorException("Could not destroy service -> " + unknownInternalException.getMessage());
        }
    }

    @Override
    public List<String> listServices(NmServiceDeploymentHost host) throws UnknownInternalException, OrchestratorInternalErrorException {
        return dockerContainerClient.containers((DockerHost) host);
    }

    @Override
    public String info() {
        return "DockerEngine Container Orchestrator";
    }

}
