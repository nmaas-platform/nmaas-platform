package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostStateKeeper;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.nmservice.deployment.ContainerOrchestrator;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DcnAttachedContainer;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFileTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeService;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeServiceComponent;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions.DockerComposeFileTemplateHandlingException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions.DockerComposeFileTemplateNotFoundException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions.InternalErrorException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.network.DockerNetworkLifecycleManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.network.DockerNetworkResourceManager;
import net.geant.nmaas.nmservice.deployment.entities.DockerHostNetwork;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerCheckFailedException;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerOrchestratorInternalErrorException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotCreateContainerNetworkException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotDeployNmServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotPrepareEnvironmentException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRemoveContainerNetworkException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRemoveNmServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRestartNmServiceException;
import net.geant.nmaas.nmservice.deployment.exceptions.DockerNetworkCheckFailedException;
import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceRequestVerificationException;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.AppUiAccessDetails;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Profile("env_docker-compose")
public class DockerComposeManager implements ContainerOrchestrator {

    private DockerComposeServiceRepositoryManager repositoryManager;

    private DockerHostRepositoryManager dockerHosts;

    private DockerHostStateKeeper dockerHostStateKeeper;

    private DockerComposeFilePreparer composeFilePreparer;

    private DockerComposeCommandExecutor composeCommandExecutor;

    private DockerNetworkLifecycleManager dockerNetworkLifecycleManager;

    private DockerNetworkResourceManager dockerNetworkResourceManager;

    private StaticRoutingConfigManager routingConfigManager;

    private ApplicationRepository applicationRepository;

    @Autowired
    public DockerComposeManager(DockerComposeServiceRepositoryManager repositoryManager,
                                DockerHostRepositoryManager dockerHosts,
                                DockerHostStateKeeper dockerHostStateKeeper,
                                DockerComposeFilePreparer composeFilePreparer,
                                DockerComposeCommandExecutor composeCommandExecutor,
                                DockerNetworkLifecycleManager dockerNetworkLifecycleManager,
                                DockerNetworkResourceManager dockerNetworkResourceManager,
                                StaticRoutingConfigManager routingConfigManager,
                                ApplicationRepository applicationRepository){
        this.repositoryManager = repositoryManager;
        this.dockerHosts = dockerHosts;
        this.dockerHostStateKeeper = dockerHostStateKeeper;
        this.composeFilePreparer = composeFilePreparer;
        this.composeCommandExecutor = composeCommandExecutor;
        this.dockerNetworkLifecycleManager = dockerNetworkLifecycleManager;
        this.dockerNetworkResourceManager = dockerNetworkResourceManager;
        this.routingConfigManager = routingConfigManager;
        this.applicationRepository = applicationRepository;
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyDeploymentEnvironmentSupportAndBuildNmServiceInfo(Identifier deploymentId, String deploymentName, String domain, AppDeploymentSpec appDeploymentSpec)
            throws NmServiceRequestVerificationException {
        if(!appDeploymentSpec.getSupportedDeploymentEnvironments().contains(AppDeploymentEnv.DOCKER_COMPOSE))
            throw new NmServiceRequestVerificationException(
                    "Service deployment not possible with currently used container orchestrator");
        DockerComposeNmServiceInfo serviceInfo = new DockerComposeNmServiceInfo(
                deploymentId,
                deploymentName,
                domain,
                DockerComposeFileTemplate.copy(appDeploymentSpec.getDockerComposeFileTemplate())
        );
        repositoryManager.storeService(serviceInfo);
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
            String assignedHostVolume = constructHostVolumeDirectoryName(network.getHost().getVolumesPath(), deploymentId.value());
            DockerComposeService dockerComposeService = new DockerComposeService();
            dockerComposeService.setAttachedVolumeName(assignedHostVolume);
            dockerComposeService.setPublicPort(dockerNetworkResourceManager.obtainPortForClientNetwork(domain, deploymentId));
            repositoryManager.updateDockerComposeService(deploymentId, dockerComposeService);
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (DockerHostNotFoundException dockerHostNotFoundException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Did not find any suitable Docker Host for deployment -> " + dockerHostNotFoundException.getMessage());
        }
    }

    private void declareNewNetworkForClientIfNotExists(String domain)
            throws ContainerOrchestratorInternalErrorException, DockerHostNotFoundException {
        if (!dockerNetworkLifecycleManager.networkForDomainAlreadyConfigured(domain))
            dockerNetworkLifecycleManager.declareNewNetworkForClientOnHost(domain, dockerHosts.loadPreferredDockerHost());
    }

    private String constructHostVolumeDirectoryName(String baseVolumePath, String deploymentDirectory) {
        return baseVolumePath + "/" + deploymentDirectory + "-1";
    }

    @Override
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void prepareDeploymentEnvironment(Identifier deploymentId, boolean configFileRepositoryRequired)
            throws CouldNotPrepareEnvironmentException, ContainerOrchestratorInternalErrorException {
        try {
            final DockerComposeNmServiceInfo service = loadService(deploymentId);
            String networkName = deployNetworkForClientOnDockerHostIfNotDoneBefore(service);
            service.getDockerComposeService().setDcnNetworkName(networkName);
            DockerComposeFileTemplate dockerComposeFileTemplate = service.getDockerComposeFileTemplate();
            for(DcnAttachedContainer container : dockerComposeFileTemplate.getDcnAttachedContainers()) {
                DockerComposeServiceComponent component = new DockerComposeServiceComponent();
                component.setName(container.getName());
                component.setDeploymentName(deploymentId.value() + "-" + container.getName());
                component.setDescription(container.getDescription());
                component.setIpAddressOfContainer(dockerNetworkResourceManager.assignNewIpAddressForContainer(service.getDomain()));
                service.getDockerComposeService().getServiceComponents().add(component);
            }
            buildAndStoreComposeFile(service.getDeploymentId(), service.getDockerComposeService(), dockerComposeFileTemplate);
            downloadComposeFileOnDockerHost(service);
            downloadContainerImageOnDockerHost(service);
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new CouldNotPrepareEnvironmentException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (CommandExecutionException commandExecutionException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Couldn't execute command on remote host -> " + commandExecutionException.getMessage());
        } catch (CouldNotCreateContainerNetworkException couldNotCreateContainerNetworkException) {
            throw new CouldNotPrepareEnvironmentException(
                    "Failed to create network -> " + couldNotCreateContainerNetworkException.getMessage());
        } catch (DockerComposeFileTemplateHandlingException dockerComposeFileTemplateHandlingException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Problem occurred during Docker Compose file preparation -> " + dockerComposeFileTemplateHandlingException.getMessage());
        } catch (DockerComposeFileTemplateNotFoundException
                | InternalErrorException exception) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Problem occurred while loading Docker Compose template file -> " + exception.getMessage());
        }
    }

    private DockerComposeNmServiceInfo loadService(Identifier deploymentId) throws InvalidDeploymentIdException {
        return repositoryManager.loadService(deploymentId);
    }

    private String deployNetworkForClientOnDockerHostIfNotDoneBefore(NmServiceInfo service)
            throws CouldNotCreateContainerNetworkException, ContainerOrchestratorInternalErrorException {
        return dockerNetworkLifecycleManager.deployNetworkForDomain(service.getDomain());
    }

    private void buildAndStoreComposeFile(Identifier deploymentId, DockerComposeService service, DockerComposeFileTemplate dockerComposeFileTemplate)
            throws DockerComposeFileTemplateHandlingException, DockerComposeFileTemplateNotFoundException, InvalidDeploymentIdException, InternalErrorException {
        composeFilePreparer.buildAndStoreComposeFile(deploymentId, service, dockerComposeFileTemplate);
    }

    private void downloadComposeFileOnDockerHost(DockerComposeNmServiceInfo service) throws CommandExecutionException {
        composeCommandExecutor.executeComposeFileDownloadCommand(service.getDeploymentId(), service.getHost());
    }

    private void downloadContainerImageOnDockerHost(DockerComposeNmServiceInfo service) throws CommandExecutionException {
        composeCommandExecutor.executeComposePullCommand(service.getDeploymentId(), service.getHost());
    }

    @Override
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deployNmService(Identifier deploymentId)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException {
        try {
            final DockerComposeNmServiceInfo service = loadService(deploymentId);
            deployContainers(service);
            configureRoutingOnStartedContainer(service);
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new CouldNotDeployNmServiceException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (CommandExecutionException commandExecutionException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Problem with docker compose command execution on remote host -> " + commandExecutionException.getMessage());
        }
    }

    private void deployContainers(DockerComposeNmServiceInfo service) throws CommandExecutionException {
        composeCommandExecutor.executeComposeUpCommand(service.getDeploymentId(), service.getHost());
    }

    private void configureRoutingOnStartedContainer(NmServiceInfo service)
            throws CouldNotDeployNmServiceException, ContainerOrchestratorInternalErrorException, CommandExecutionException, InvalidDeploymentIdException {
        routingConfigManager.configure(service.getDeploymentId());
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void checkService(Identifier deploymentId)
            throws ContainerCheckFailedException, DockerNetworkCheckFailedException, ContainerOrchestratorInternalErrorException {
        // TODO implement relevant checks
    }

    @Override
    @Loggable(LogLevel.INFO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeNmService(Identifier deploymentId) throws CouldNotRemoveNmServiceException, ContainerOrchestratorInternalErrorException {
        try {
            final DockerComposeNmServiceInfo service = loadService(deploymentId);
            stopAndRemoveContainers(service);
            removeNetworkIfNoContainerAttached(service);
        } catch (InvalidDeploymentIdException invalidDeploymentIdException) {
            throw new CouldNotRemoveNmServiceException(
                    "Service not found in repository -> Invalid deployment id " + invalidDeploymentIdException.getMessage());
        } catch (CommandExecutionException commandExecutionException) {
            throw new ContainerOrchestratorInternalErrorException(
                    "Couldn't execute docker compose remove command on remote host -> " + commandExecutionException.getMessage());
        } catch (CouldNotRemoveContainerNetworkException couldNotRemoveContainerNetworkException) {
            throw new CouldNotRemoveNmServiceException(
                    "Failed to remove network -> " + couldNotRemoveContainerNetworkException.getMessage());
        }
    }

    private void stopAndRemoveContainers(DockerComposeNmServiceInfo service) throws CommandExecutionException, ContainerOrchestratorInternalErrorException {
        composeCommandExecutor.executeComposeDownCommand(service.getDeploymentId(), service.getHost());
        for(DockerComposeServiceComponent component : service.getDockerComposeService().getServiceComponents()) {
            dockerNetworkResourceManager.removeAddressAssignment(service.getDomain(), component.getIpAddressOfContainer());
        }
    }

    private void removeNetworkIfNoContainerAttached(NmServiceInfo justRemovedService)
            throws CouldNotRemoveContainerNetworkException, ContainerOrchestratorInternalErrorException {
        List<DockerComposeNmServiceInfo> runningServices = repositoryManager.loadAllRunningServicesInDomain(justRemovedService.getDomain());
        if (noRunningClientServices(justRemovedService, runningServices)) {
            dockerNetworkLifecycleManager.removeNetwork(justRemovedService.getDomain());
        }
    }

    private boolean noRunningClientServices(NmServiceInfo justRemovedService, List<DockerComposeNmServiceInfo> runningServices) {
        return runningServices.size() == 1 && runningServices.get(0).getDeploymentId().equals(justRemovedService.getDeploymentId());
    }

    @Override
    @Loggable(LogLevel.INFO)
    public String info() {
        return "DockerCompose Container Orchestrator";
    }

    @Override
    @Loggable(LogLevel.INFO)
    public AppUiAccessDetails serviceAccessDetails(Identifier deploymentId) throws ContainerOrchestratorInternalErrorException {
        try {
            final DockerComposeNmServiceInfo service = repositoryManager.loadService(deploymentId);
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

    private AppUiAccessDetails accessDetails(DockerComposeNmServiceInfo serviceInfo) {
        final String accessAddress = serviceInfo.getHost().getPublicIpAddress().getHostAddress();
        final Integer accessPort = serviceInfo.getDockerComposeService().getPublicPort();
        return new AppUiAccessDetails(new StringBuilder().append("http://").append(accessAddress).append(":").append(accessPort).toString());
    }

}
