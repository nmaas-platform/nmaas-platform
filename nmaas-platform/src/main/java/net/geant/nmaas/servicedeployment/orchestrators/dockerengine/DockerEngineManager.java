package net.geant.nmaas.servicedeployment.orchestrators.dockerengine;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.NetworkConfig;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostsRepository;
import net.geant.nmaas.externalservices.inventory.providernetwork.NetworkStateTracker;
import net.geant.nmaas.servicedeployment.ContainerOrchestrationProvider;
import net.geant.nmaas.servicedeployment.exceptions.*;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceDeploymentHost;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceInfo;
import net.geant.nmaas.servicedeployment.nmservice.NmServiceState;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.container.ContainerConfigBuilder;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.container.DockerContainerClient;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.network.ContainerNetworkConfigBuilder;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.network.ContainerNetworkDetails;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.network.DockerNetworkClient;
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
    private DockerNetworkClient dockerNetworkClient;

    @Autowired
    private NmServiceRepository nmServices;

    @Autowired
    private DockerHostsRepository dockerHosts;

    @Autowired
    private NetworkStateTracker networkStateTracker;

    @Override
    public void verifyRequestObtainTargetAndNetworkDetails(String serviceName) throws OrchestratorInternalErrorException {
        try {
            final DockerHost host = dockerHosts.loadPreferredDockerHost();
            nmServices.updateServiceHost(serviceName, host);
            nmServices.updateServiceNetworkDetails(serviceName, networkStateTracker.prepareNetworkConfigForContainer(serviceName, host));
            nmServices.updateServiceState(serviceName, NmServiceInfo.ServiceState.VERIFIED);
        } catch (DockerHostNotFoundException dockerHostNotFoundException) {
            throw new OrchestratorInternalErrorException(
                    "Did not find any suitable Docker host for deployment.");
        } catch (NmServiceRepository.ServiceNotFoundException serviceNotFoundException) {
            throw new OrchestratorInternalErrorException(
                    "Service not found in repository -> " + serviceNotFoundException.getMessage());
        }
    }

    @Override
    public void prepareDeploymentEnvironment(String serviceName)
            throws CouldNotPrepareEnvironmentException, CouldNotConnectToOrchestratorException, OrchestratorInternalErrorException {
        try {
            final NmServiceInfo service = nmServices.loadService(serviceName);
            final DockerHost host = (DockerHost) service.getHost();
            final NetworkConfig networkConfig = ContainerNetworkConfigBuilder.build(service);
            final String networkId = dockerNetworkClient.create(networkConfig, host);
            nmServices.updateNetworkId(serviceName, networkId);
            final String imageName = ((DockerEngineContainerTemplate) service.getSpec().template()).getImage();
            dockerContainerClient.pullImage(imageName, host);
            nmServices.updateServiceState(serviceName, NmServiceInfo.ServiceState.READY);
        } catch (NmServiceRepository.ServiceNotFoundException serviceNotFoundException) {
            throw new CouldNotPrepareEnvironmentException(
                    "Service not found in repository -> " + serviceNotFoundException.getMessage());
        } catch (ServiceVerificationException serviceSpecVerificationException) {
            throw new CouldNotPrepareEnvironmentException(
                    "Service spec verification failed -> " + serviceSpecVerificationException.getMessage());
        } catch (ContainerNetworkDetailsVerificationException containerNetworkDetailsVerificationException) {
            throw new CouldNotPrepareEnvironmentException(
                    "Service network details verification failed -> " + containerNetworkDetailsVerificationException.getMessage());
        } catch (CouldNotCreateContainerNetworkException couldNotCreateContainerNetworkException) {
            throw new CouldNotPrepareEnvironmentException(
                    "Failed to create network -> " + couldNotCreateContainerNetworkException.getMessage());
        }
    }

    @Override
    public void deployNmService(String serviceName)
            throws CouldNotDeployNmServiceException, CouldNotConnectToOrchestratorException, OrchestratorInternalErrorException {
        try {
            final NmServiceInfo service = nmServices.loadService(serviceName);
            final DockerContainerSpec spec = (DockerContainerSpec) service.getSpec();
            ContainerConfigBuilder.verifyInput(service);
            final DockerHost host = (DockerHost) service.getHost();
            final ContainerConfig config = ContainerConfigBuilder.build(service.getSpec(), host);
            String containerId = dockerContainerClient.deploy(config, spec.uniqueDeploymentName(), host);
            dockerNetworkClient.connectContainerToNetwork(containerId, ((ContainerNetworkDetails)service.getNetwork()).getDeploymentId(), host);
            nmServices.updateServiceId(serviceName, containerId);
            nmServices.updateServiceState(serviceName, NmServiceInfo.ServiceState.RUNNING);
        } catch (CouldNotDeployNmServiceException couldNotDeployNmServiceException) {
            throw new CouldNotDeployNmServiceException(
                    "Could not deploy service -> " + couldNotDeployNmServiceException.getMessage());
        } catch (NmServiceRepository.ServiceNotFoundException serviceNotFoundException) {
            throw new CouldNotDeployNmServiceException(
                    "Service not found in repository -> " + serviceNotFoundException.getMessage());
        } catch (ServiceVerificationException serviceVerificationException) {
            throw new CouldNotDeployNmServiceException(
                    "Service spec verification failed -> " + serviceVerificationException.getMessage());
        } catch (CouldNotConnectContainerToNetworkException couldNotConnectContainerToNetworkException) {
            throw new CouldNotDeployNmServiceException(
                    "Failed to connect container to network -> " + couldNotConnectContainerToNetworkException.getMessage());
        }
    }

    @Override
    public NmServiceState checkService(String serviceName)
            throws CouldNotCheckNmServiceStateException, CouldNotConnectToOrchestratorException, OrchestratorInternalErrorException {
        try {
            final NmServiceInfo service = nmServices.loadService(serviceName);
            return dockerContainerClient.checkService(service.getDeploymentId(), (DockerHost) service.getHost());
        } catch (NmServiceRepository.ServiceNotFoundException serviceNotFoundException) {
            throw new CouldNotCheckNmServiceStateException(
                    "Service not found in repository -> " + serviceNotFoundException.getMessage());
        }  catch (ContainerNotFoundException containerNotFoundException) {
            throw new CouldNotCheckNmServiceStateException(
                    "Container not found on the deployment host -> " + containerNotFoundException.getMessage());
        }
    }

    @Override
    public void removeNmService(String serviceName)
            throws CouldNotDestroyNmServiceException, CouldNotConnectToOrchestratorException, OrchestratorInternalErrorException {
        try {
            final NmServiceInfo service = nmServices.loadService(serviceName);
            final DockerHost host = (DockerHost) service.getHost();
            dockerContainerClient.remove(service.getDeploymentId(), host);
            dockerNetworkClient.remove(((ContainerNetworkDetails)service.getNetwork()).getDeploymentId(), host);
            nmServices.updateServiceState(serviceName, NmServiceInfo.ServiceState.STOPPED);
        } catch (NmServiceRepository.ServiceNotFoundException serviceNotFoundException) {
            throw new CouldNotDestroyNmServiceException(
                    "Service not found in repository -> " + serviceNotFoundException.getMessage());
        } catch (CouldNotDestroyNmServiceException couldNotDestroyNmServiceException) {
            throw new CouldNotDestroyNmServiceException(
                    "Could not destroy service -> " + couldNotDestroyNmServiceException.getMessage());
        } catch (CouldNotRemoveContainerNetworkException couldNotRemoveContainerNetworkException) {
            throw new CouldNotDestroyNmServiceException(
                    "Failed to remove network -> " + couldNotRemoveContainerNetworkException.getMessage());
        }
    }

    @Override
    public List<String> listServices(NmServiceDeploymentHost host)
            throws CouldNotConnectToOrchestratorException, OrchestratorInternalErrorException {
        return dockerContainerClient.containers((DockerHost) host);
    }

    @Override
    public String info() {
        return "DockerEngine Container Orchestrator";
    }

}
