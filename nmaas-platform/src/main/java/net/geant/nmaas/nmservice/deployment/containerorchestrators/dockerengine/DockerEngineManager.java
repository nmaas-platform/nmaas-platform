package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine;

import com.spotify.docker.client.messages.ContainerConfig;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.nmservice.deployment.ContainerOrchestrator;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container.ContainerConfigBuilder;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container.DockerContainerManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.*;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.DockerNetworkLifecycleManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.DockerNetworkResourceManager;
import net.geant.nmaas.nmservice.deployment.entities.DockerHostNetwork;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.exceptions.*;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.AppUiAccessDetails;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Profile("env_docker-engine")
public class DockerEngineManager implements ContainerOrchestrator {

    @Autowired
    private DockerContainerManager dockerContainerManager;
    @Autowired
    private DockerNetworkLifecycleManager dockerNetworkLifecycleManager;
    @Autowired
    private DockerNetworkResourceManager dockerNetworkResourceManager;
    @Autowired
    private DockerEngineServiceRepositoryManager repositoryManager;
    @Autowired
    private DockerHostRepositoryManager dockerHosts;

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(Identifier deploymentId, String deploymentName, String domain, AppDeploymentSpec appDeploymentSpec)
            throws NmServiceRequestVerificationException {
        if(!appDeploymentSpec.getSupportedDeploymentEnvironments().contains(AppDeploymentEnv.DOCKER_ENGINE))
            throw new NmServiceRequestVerificationException(
                    "Service deployment not possible with currently used container orchestrator");
        repositoryManager.storeService(new DockerEngineNmServiceInfo(
                deploymentId,
                deploymentName,
                domain,
                DockerContainerTemplate.copy(appDeploymentSpec.getDockerContainerTemplate()))
        );
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyRequestAndObtainInitialDeploymentDetails(Identifier deploymentId)
            throws NmServiceRequestVerificationException, ContainerOrchestratorInternalErrorException {
        try {
            final String domain = repositoryManager.loadDomain(deploymentId);
            declareNewNetworkForClientIfNotExists(domain);
            final DockerHostNetwork network = dockerNetworkLifecycleManager.networkForDomain(domain);
            repositoryManager.updateDockerHost(deploymentId, network.getHost());
            final DockerContainer container = dockerContainerManager.declareNewContainerForDeployment(deploymentId);
            repositoryManager.updateDockerContainer(deploymentId, container);
            ContainerConfigBuilder.verifyInitInput(repositoryManager.loadService(deploymentId));
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (DockerHostNotFoundException dockerHostNotFoundException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Did not find any suitable Docker Host for deployment.");
        }
    }

    private void declareNewNetworkForClientIfNotExists(String domain)
            throws ContainerOrchestratorInternalErrorException, DockerHostNotFoundException {
        if (!dockerNetworkLifecycleManager.networkForDomainAlreadyConfigured(domain))
            dockerNetworkLifecycleManager.declareNewNetworkForClientOnHost(domain, dockerHosts.loadPreferredDockerHost());
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void prepareDeploymentEnvironment(Identifier deploymentId)
            throws CouldNotPrepareEnvironmentException, ContainerOrchestratorInternalErrorException {
        try {
            final DockerEngineNmServiceInfo service = repositoryManager.loadService(deploymentId);
            deployNetworkForClientOnDockerHostIfNotDoneBefore(service);
            downloadContainerImageOnDockerHost(service);
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new CouldNotPrepareEnvironmentException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (CouldNotCreateContainerNetworkException couldNotCreateContainerNetworkException) {
            throw new CouldNotPrepareEnvironmentException(
                    "Failed to create network -> " + couldNotCreateContainerNetworkException.getMessage());
        }
    }

    private void deployNetworkForClientOnDockerHostIfNotDoneBefore(NmServiceInfo service)
            throws CouldNotCreateContainerNetworkException, ContainerOrchestratorInternalErrorException {
        dockerNetworkLifecycleManager.deployNetworkForDomain(service.getDomain());
    }

    private void downloadContainerImageOnDockerHost(DockerEngineNmServiceInfo service)
            throws ContainerOrchestratorInternalErrorException {
        dockerContainerManager.pullImage(service.getDockerContainerTemplate().getImage(), service.getHost());
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void deployNmService(Identifier deploymentId)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException {
        try {
            final DockerEngineNmServiceInfo service = repositoryManager.loadService(deploymentId);
            final DockerContainerNetDetails netDetails = obtainNetworkDetailsFoNewContainerAndStoreInRepository(deploymentId);
            service.getDockerContainer().setNetworkDetails(netDetails);
            final String containerId = createContainerAndStoreIdInRepository(service);
            service.getDockerContainer().setDeploymentId(containerId);
            connectContainerToNetwork(service);
            startContainer(service);
            configureRoutingOnStartedContainer(service);
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new CouldNotDeployNmServiceException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (CouldNotDeployNmServiceException couldNotDeployNmServiceException) {
            throw new CouldNotDeployNmServiceException(
                    "Could not deploy NmService service -> " + couldNotDeployNmServiceException.getMessage());
        } catch (CouldNotConnectContainerToNetworkException couldNotConnectContainerToNetworkException) {
            throw new CouldNotDeployNmServiceException(
                    "Failed to connect container to network -> " + couldNotConnectContainerToNetworkException.getMessage());
        } catch (NmServiceRequestVerificationException nmServiceRequestVerificationException) {
            throw new CouldNotDeployNmServiceException(
                    "Container spec is missing some parameters -> " + nmServiceRequestVerificationException.getMessage());
        }
    }

    private DockerContainerNetDetails obtainNetworkDetailsFoNewContainerAndStoreInRepository(Identifier deploymentId)
            throws InvalidDeploymentIdException, ContainerOrchestratorInternalErrorException {
        final String domain = repositoryManager.loadDomain(deploymentId);
        final DockerContainerNetDetails netDetails = obtainNetworkDetailsForContainer(domain, deploymentId);
        repositoryManager.updateDockerContainerNetworkDetails(deploymentId, netDetails);
        return netDetails;
    }

    private DockerContainerNetDetails obtainNetworkDetailsForContainer(String domain, Identifier deploymentId)
            throws ContainerOrchestratorInternalErrorException {
        DockerContainerNetDetails details = new DockerContainerNetDetails();
        details.setPublicPort(dockerNetworkResourceManager.obtainPortForClientNetwork(domain, deploymentId));
        DockerNetworkIpam ipam = new DockerNetworkIpam(
                dockerNetworkResourceManager.assignNewIpAddressForContainer(domain),
                dockerNetworkResourceManager.obtainSubnetFromClientNetwork(domain),
                dockerNetworkResourceManager.obtainGatewayFromClientNetwork(domain));
        details.setIpam(ipam);
        return details;
    }

    private String createContainerAndStoreIdInRepository(DockerEngineNmServiceInfo service)
            throws NmServiceRequestVerificationException, CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException, InvalidDeploymentIdException {
        ContainerConfigBuilder.verifyFinalInput(service);
        final String containerId = createContainer(service);
        repositoryManager.updateDockerContainerDeploymentId(service.getDeploymentId(), containerId);
        return containerId;
    }

    private String createContainer(DockerEngineNmServiceInfo service)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException {
        final ContainerConfig config = ContainerConfigBuilder.build(service);
        return dockerContainerManager.create(config, service.getDeploymentId().value(), service.getHost());
    }

    private void connectContainerToNetwork(DockerEngineNmServiceInfo service)
            throws CouldNotConnectContainerToNetworkException, ContainerOrchestratorInternalErrorException {
        dockerNetworkLifecycleManager.connectContainerToNetwork(service.getDomain(), service.getDockerContainer());
    }

    private void startContainer(DockerEngineNmServiceInfo service)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException {
        dockerContainerManager.start(service.getDockerContainer().getDeploymentId(), service.getHost());
    }

    private void configureRoutingOnStartedContainer(DockerEngineNmServiceInfo service)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException {
        for (String managedDeviceIpAddress : service.getManagedDevicesIpAddresses()) {
            dockerContainerManager.addStaticRoute(
                    service.getDockerContainer().getDeploymentId(),
                    managedDeviceIpAddress,
                    service.getDockerContainer().getNetworkDetails().getIpam().getGateway(),
                    service.getHost());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void checkService(Identifier deploymentId)
            throws ContainerCheckFailedException, DockerNetworkCheckFailedException, ContainerOrchestratorInternalErrorException {
        try {
            final DockerEngineNmServiceInfo service = repositoryManager.loadService(deploymentId);
            checkContainerNetworkAndContainerItself(service);
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        }
    }

    private void checkContainerNetworkAndContainerItself(DockerEngineNmServiceInfo service)
            throws ContainerCheckFailedException, DockerNetworkCheckFailedException, ContainerOrchestratorInternalErrorException {
        dockerContainerManager.checkService(service.getDockerContainer().getDeploymentId(), service.getHost());
        dockerNetworkLifecycleManager.verifyNetwork(service.getDomain());
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeNmService(Identifier deploymentId)
            throws CouldNotRemoveNmServiceException, ContainerOrchestratorInternalErrorException {
        try {
            final DockerEngineNmServiceInfo service = repositoryManager.loadService(deploymentId);
            removeContainer(service);
            removeNetworkIfNoContainerAttached(service);
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (CouldNotRemoveNmServiceException couldNotRemoveNmServiceException) {
            throw new CouldNotRemoveNmServiceException(
                    "Could not remove container -> " + couldNotRemoveNmServiceException.getMessage());
        } catch (CouldNotRemoveContainerNetworkException couldNotRemoveContainerNetworkException) {
            throw new CouldNotRemoveNmServiceException(
                    "Failed to remove network -> " + couldNotRemoveContainerNetworkException.getMessage());
        }
    }

    private void removeContainer(DockerEngineNmServiceInfo service)
            throws CouldNotRemoveNmServiceException, ContainerOrchestratorInternalErrorException {
        dockerContainerManager.remove(service.getDockerContainer().getDeploymentId(), service.getHost());
        dockerNetworkResourceManager.removeAddressAssignment(service.getDomain(), service.getDockerContainer().getNetworkDetails().getIpam().getIpAddressOfContainer());
        dockerNetworkLifecycleManager.disconnectContainerFromNetwork(service.getDomain(), service.getDockerContainer());
    }

    private void removeNetworkIfNoContainerAttached(NmServiceInfo justRemovedService)
            throws CouldNotRemoveContainerNetworkException, ContainerOrchestratorInternalErrorException {
        List<DockerEngineNmServiceInfo> runningServices = repositoryManager.loadAllRunningServicesInDomain(justRemovedService.getDomain());
        if (noRunningClientServices(justRemovedService, runningServices))
            dockerNetworkLifecycleManager.removeNetwork(justRemovedService.getDomain());
    }

    private boolean noRunningClientServices(NmServiceInfo justRemovedService, List<DockerEngineNmServiceInfo> runningServices) {
        return runningServices.size() == 1 && runningServices.get(0).getDeploymentId().equals(justRemovedService.getDeploymentId());
    }

    @Override
    @Loggable(LogLevel.INFO)
    public String info() {
        return "DockerEngine Container Orchestrator";
    }

    @Override
    @Loggable(LogLevel.INFO)
    public AppUiAccessDetails serviceAccessDetails(Identifier deploymentId) throws ContainerOrchestratorInternalErrorException {
        try {
            final DockerEngineNmServiceInfo service = repositoryManager.loadService(deploymentId);
            return accessDetails(service);
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        }
    }

    @Override
    public void restartNmService(Identifier deploymentId) throws CouldNotRestartNmServiceException, ContainerOrchestratorInternalErrorException {
        throw new NotImplementedException();
    }

    private AppUiAccessDetails accessDetails(DockerEngineNmServiceInfo serviceInfo) {
        final String accessAddress = serviceInfo.getHost().getPublicIpAddress().getHostAddress();
        final Integer accessPort = serviceInfo.getDockerContainer().getNetworkDetails().getPublicPort();
        return new AppUiAccessDetails(new StringBuilder().append("http://").append(accessAddress).append(":").append(accessPort).toString());
    }

}
