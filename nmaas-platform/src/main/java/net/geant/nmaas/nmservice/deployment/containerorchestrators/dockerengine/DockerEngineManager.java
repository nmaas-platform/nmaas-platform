package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.NetworkConfig;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepository;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostStateKeeper;
import net.geant.nmaas.nmservice.deployment.ContainerOrchestrationProvider;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container.ContainerConfigBuilder;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container.DockerContainerClient;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.ContainerNetworkConfigBuilder;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.ContainerNetworkDetails;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.ContainerNetworkIpamSpec;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.DockerNetworkClient;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentHost;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState.*;

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
    private DockerHostRepository dockerHosts;

    @Autowired
    private DockerHostStateKeeper dockerHostStateKeeper;

    @Override
    public void verifyRequestObtainTargetHostAndNetworkDetails(String serviceName)
            throws NmServiceVerificationException, ContainerOrchestratorInternalErrorException {
        try {
            final DockerHost host = dockerHosts.loadPreferredDockerHost();
            nmServices.updateServiceHost(serviceName, host);
            final String dockerHostName = host.getName();
            final int publicPort = dockerHostStateKeeper.assignPort(dockerHostName, serviceName);
            final int vlanNumber = dockerHostStateKeeper.assignVlan(dockerHostName, serviceName);
            final ContainerNetworkIpamSpec addresses = dockerHostStateKeeper.assignAddressPool(dockerHostName, serviceName);
            final ContainerNetworkDetails networkDetails = new ContainerNetworkDetails(publicPort, addresses, vlanNumber);
            nmServices.updateServiceNetworkDetails(serviceName, networkDetails);
            nmServices.updateServiceState(serviceName, VERIFIED);
            ContainerConfigBuilder.verifyInput(nmServices.loadService(serviceName));
        } catch (DockerHostNotFoundException dockerHostNotFoundException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Did not find any suitable Docker host for deployment.");
        } catch (NmServiceRepository.ServiceNotFoundException serviceNotFoundException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Service not found in repository -> " + serviceNotFoundException.getMessage());
        }
    }

    @Override
    public void prepareDeploymentEnvironment(String serviceName)
            throws CouldNotPrepareEnvironmentException, CouldNotConnectToOrchestratorException, ContainerOrchestratorInternalErrorException {
        try {
            final NmServiceInfo service = nmServices.loadService(serviceName);
            final DockerHost host = (DockerHost) service.getHost();
            final NetworkConfig networkConfig = ContainerNetworkConfigBuilder.build(service);
            final String networkId = dockerNetworkClient.create(networkConfig, host);
            nmServices.updateNetworkId(serviceName, networkId);
            final String imageName = ((DockerEngineContainerTemplate) service.getSpec().template()).getImage();
            dockerContainerClient.pullImage(imageName, host);
            nmServices.updateServiceState(serviceName, ENVIRONMENT_PREPARED);
        } catch (NmServiceRepository.ServiceNotFoundException serviceNotFoundException) {
            throw new CouldNotPrepareEnvironmentException(
                    "Service not found in repository -> " + serviceNotFoundException.getMessage());
        } catch (NmServiceVerificationException serviceSpecVerificationException) {
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
            throws CouldNotDeployNmServiceException, CouldNotConnectToOrchestratorException, ContainerOrchestratorInternalErrorException {
        try {
            final NmServiceInfo service = nmServices.loadService(serviceName);
            final DockerContainerSpec spec = (DockerContainerSpec) service.getSpec();
            final DockerHost host = (DockerHost) service.getHost();
            final ContainerConfig config = ContainerConfigBuilder.build(service);
            final String containerId = dockerContainerClient.create(config, spec.uniqueDeploymentName(), host);
            dockerNetworkClient.connectContainerToNetwork(containerId, ((ContainerNetworkDetails)service.getNetwork()).getDeploymentId(), host);
            dockerContainerClient.start(containerId, host);
            nmServices.updateServiceId(serviceName, containerId);
            nmServices.updateServiceState(serviceName, DEPLOYED);
        } catch (CouldNotDeployNmServiceException couldNotDeployNmServiceException) {
            throw new CouldNotDeployNmServiceException(
                    "Could not deploy service -> " + couldNotDeployNmServiceException.getMessage());
        } catch (NmServiceRepository.ServiceNotFoundException serviceNotFoundException) {
            throw new CouldNotDeployNmServiceException(
                    "Service not found in repository -> " + serviceNotFoundException.getMessage());
        } catch (CouldNotConnectContainerToNetworkException couldNotConnectContainerToNetworkException) {
            throw new CouldNotDeployNmServiceException(
                    "Failed to connect container to network -> " + couldNotConnectContainerToNetworkException.getMessage());
        }
    }

    @Override
    public NmServiceDeploymentState checkService(String serviceName)
            throws CouldNotCheckNmServiceStateException, CouldNotConnectToOrchestratorException, ContainerOrchestratorInternalErrorException {
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
            throws CouldNotDestroyNmServiceException, CouldNotConnectToOrchestratorException, ContainerOrchestratorInternalErrorException {
        try {
            final NmServiceInfo service = nmServices.loadService(serviceName);
            final DockerHost host = (DockerHost) service.getHost();
            dockerContainerClient.remove(service.getDeploymentId(), host);
            dockerNetworkClient.remove(((ContainerNetworkDetails)service.getNetwork()).getDeploymentId(), host);
            nmServices.updateServiceState(serviceName, REMOVED);
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
            throws CouldNotConnectToOrchestratorException, ContainerOrchestratorInternalErrorException {
        return dockerContainerClient.containers((DockerHost) host);
    }

    @Override
    public String info() {
        return "DockerEngine Container Orchestrator";
    }

}
