package net.geant.nmaas.dcn.deployment;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import net.geant.nmaas.dcn.deployment.api.model.AnsiblePlaybookStatus;
import net.geant.nmaas.dcn.deployment.entities.*;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotDeployDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotRemoveDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotVerifyDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.DcnRequestVerificationException;
import net.geant.nmaas.externalservices.inventory.network.CloudAttachPoint;
import net.geant.nmaas.externalservices.inventory.network.NetworkAttachPoint;
import net.geant.nmaas.externalservices.inventory.network.exceptions.AttachPointNotFoundException;
import net.geant.nmaas.externalservices.inventory.network.repositories.DockerHostAttachPointRepository;
import net.geant.nmaas.externalservices.inventory.network.repositories.DomainNetworkAttachPointRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerApiClient;
import net.geant.nmaas.nmservice.deployment.entities.DockerHostNetwork;
import net.geant.nmaas.nmservice.deployment.repository.DockerHostNetworkRepository;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookContainerBuilder.*;
import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookIdentifierConverter.*;

/**
 * Executor used when DCN should be configured by Ansible playbooks.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Profile("dcn_ansible")
public class AnsibleDcnDeploymentExecutor implements DcnDeploymentProvider, AnsiblePlaybookExecutionStateListener {

    private static final Logger log = LogManager.getLogger(AnsibleDcnDeploymentExecutor.class);
    private static final String DEFAULT_ANSIBLE_CONTAINER_NAME = "nmaas-ansible-test";

    private DcnRepositoryManager dcnRepositoryManager;
    private ApplicationEventPublisher applicationEventPublisher;
    private DockerHostAttachPointRepository dockerHostAttachPointRepository;
    private DomainNetworkAttachPointRepository basicCustomerNetworkAttachPointRepository;
    private DockerHostNetworkRepository dockerHostNetworkRepository;
    private DockerApiClient dockerApiClient;

    private String ansibleDockerApiUrl;

    @Autowired
    public AnsibleDcnDeploymentExecutor(DcnRepositoryManager dcnRepositoryManager,
                                        DockerHostAttachPointRepository dockerHostAttachPointRepository,
                                        DomainNetworkAttachPointRepository basicCustomerNetworkAttachPointRepository,
                                        ApplicationEventPublisher applicationEventPublisher,
                                        DockerHostNetworkRepository dockerHostNetworkRepository,
                                        DockerApiClient dockerApiClient) {
        this.dcnRepositoryManager = dcnRepositoryManager;
        this.dockerHostAttachPointRepository = dockerHostAttachPointRepository;
        this.basicCustomerNetworkAttachPointRepository = basicCustomerNetworkAttachPointRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.dockerHostNetworkRepository = dockerHostNetworkRepository;
        this.dockerApiClient = dockerApiClient;
    }

    @Override
    @Loggable(LogLevel.INFO)
    public DcnState checkState(String domain) {
        try {
            return DcnState.fromDcnDeploymentState(dcnRepositoryManager.loadCurrentState(domain));
        } catch (InvalidDomainException e) {
            return DcnState.NONE;
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyRequest(String domain, DcnSpec dcnSpec) throws DcnRequestVerificationException {
        try {
            storeDcnInfoIfNotExists(domain, dcnSpec);
            notifyStateChangeListeners(domain, DcnDeploymentState.REQUESTED);
            final DockerHostNetwork dockerHostNetwork = dockerHostNetworkRepository
                    .findByDomain(dcnSpec.getDomain())
                    .orElseThrow(() -> new InvalidDomainException("No Docker network found for domain " + domain));
            final DcnCloudEndpointDetails dcnCloudEndpointDetails = new DcnCloudEndpointDetails(dockerHostNetwork);
            dcnRepositoryManager.updateDcnCloudEndpointDetails(domain, dcnCloudEndpointDetails);
            NetworkAttachPoint customerNetworkAttachPoint = basicCustomerNetworkAttachPointRepository
                   .findByDomain(domain)
                   .orElseThrow(() -> new AttachPointNotFoundException(domain));
            AnsiblePlaybookVpnConfig customerSideRouterVpnConfig =
                    AnsiblePlaybookVpnConfigBuilder.fromCustomerNetworkAttachPoint(customerNetworkAttachPoint);
            dcnRepositoryManager.updateAnsiblePlaybookForClientSideRouter(
                    domain,
                    customerSideRouterVpnConfig);
            CloudAttachPoint cloudAttachPoint = dockerHostAttachPointRepository
                    .findByDockerHostName(dockerHostNetwork.getHost().getName())
                    .orElseThrow(() -> new AttachPointNotFoundException(dockerHostNetwork.getHost().getName()));
            AnsiblePlaybookVpnConfig cloudSideRouterVpnConfig =
                    AnsiblePlaybookVpnConfigBuilder.fromCloudAttachPoint(customerSideRouterVpnConfig, cloudAttachPoint);
            cloudSideRouterVpnConfig.merge(dcnCloudEndpointDetails);
            dcnRepositoryManager.updateAnsiblePlaybookForCloudSideRouter(domain, cloudSideRouterVpnConfig);
            notifyStateChangeListeners(domain, DcnDeploymentState.REQUEST_VERIFIED);
        } catch ( InvalidDomainException
                | AttachPointNotFoundException e) {
            notifyStateChangeListeners(domain, DcnDeploymentState.REQUEST_VERIFICATION_FAILED);
            throw new DcnRequestVerificationException("Exception during DCN request verification -> " + e.getMessage());
        }
    }

    private void storeDcnInfoIfNotExists(String domain, DcnSpec dcnSpec) throws InvalidDomainException {
        if (!dcnRepositoryManager.exists(domain))
            dcnRepositoryManager.storeDcnInfo(new DcnInfo(dcnSpec));
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void deployDcn(String domain) throws CouldNotDeployDcnException {
        try {
            final DcnInfo dcnInfo = dcnRepositoryManager.loadNetwork(domain);
            removeOldAnsiblePlaybookContainers();
            deployAnsiblePlaybookContainers(
                    buildContainerForClientSideRouterConfig(
                            dcnInfo.getPlaybookForClientSideRouter(),
                            encodeForClientSideRouter(domain)),
                    buildContainerForCloudSideRouterConfig(
                            dcnInfo.getPlaybookForCloudSideRouter(),
                            encodeForCloudSideRouter(domain)));
            notifyStateChangeListeners(domain, DcnDeploymentState.DEPLOYMENT_INITIATED);
        } catch ( InvalidDomainException
                | InterruptedException
                | DockerException anyException) {
            notifyStateChangeListeners(domain, DcnDeploymentState.DEPLOYMENT_FAILED);
            throw new CouldNotDeployDcnException("Exception during DCN deployment -> " + anyException.getMessage());
        }
    }

    private void removeOldAnsiblePlaybookContainers() {
        try {
            final List<Container> containers = dockerApiClient.listContainers(ansibleDockerApiUrl, DockerClient.ListContainersParam.withStatusExited());
            for (Container container : containers) {
                log.debug("Removing old container " + container.id());
                dockerApiClient.removeContainer(ansibleDockerApiUrl, container.id());
            }
        } catch (DockerException
                | InterruptedException e) {
            log.warn("Failed to remove old Ansible containers", e);
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyDcn(String domain) throws CouldNotVerifyDcnException {
        try {
            notifyStateChangeListeners(domain, DcnDeploymentState.VERIFICATION_INITIATED);
            // TODO implement DCN verification functionality
            Thread.sleep(1000);
            notifyStateChangeListeners(domain, DcnDeploymentState.VERIFIED);
        } catch (InterruptedException e) {
            notifyStateChangeListeners(domain, DcnDeploymentState.VERIFICATION_FAILED);
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeDcn(String domain) throws CouldNotRemoveDcnException {
        try {
            final DcnInfo dcnInfo = dcnRepositoryManager.loadNetwork(domain);
            deployAnsiblePlaybookContainers(
                    buildContainerForClientSideRouterConfigRemoval(dcnInfo.getPlaybookForClientSideRouter(), encodeForClientSideRouter(domain)),
                    buildContainerForCloudSideRouterConfigRemoval(dcnInfo.getPlaybookForCloudSideRouter(), encodeForCloudSideRouter(domain)));
            notifyStateChangeListeners(domain, DcnDeploymentState.REMOVAL_INITIATED);
        } catch ( InvalidDomainException
                | InterruptedException
                | DockerException e) {
            notifyStateChangeListeners(domain, DcnDeploymentState.REMOVAL_FAILED);
            throw new CouldNotRemoveDcnException("Exception during DCN removal -> " + e.getMessage());
        }
    }

    private void notifyStateChangeListeners(String domain, DcnDeploymentState state) {
        applicationEventPublisher.publishEvent(new DcnDeploymentStateChangeEvent(this, domain, state));
    }

    private void deployAnsiblePlaybookContainers(ContainerConfig... ansibleContainerConfigs) throws DockerException, InterruptedException {
        for (ContainerConfig containerConfig : ansibleContainerConfigs) {
            String ansibleContainerId = dockerApiClient.createContainer(ansibleDockerApiUrl, containerConfig, ansibleContainerName());
            dockerApiClient.startContainer(ansibleDockerApiUrl, ansibleContainerId);
        }
    }

    private String ansibleContainerName() {
        return DEFAULT_ANSIBLE_CONTAINER_NAME + "-" + System.nanoTime();
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void notifyPlaybookExecutionState(String encodedPlaybookIdentifier, AnsiblePlaybookStatus.Status status) {
        String domain = null;
        try {
            domain = decode(encodedPlaybookIdentifier);
            DcnDeploymentState currentDcnDeploymentState = dcnRepositoryManager.loadCurrentState(domain);
            DcnDeploymentState newDcnDeploymentState = null;
            switch (status) {
                case SUCCESS:
                    switch(currentDcnDeploymentState) {
                        case DEPLOYMENT_INITIATED:
                            if (wasEncodedForClientSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.ANSIBLE_PLAYBOOK_CONFIG_FOR_CLIENT_SIDE_ROUTER_COMPLETED;
                            else if (wasEncodedForCloudSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.ANSIBLE_PLAYBOOK_CONFIG_FOR_CLOUD_SIDE_ROUTER_COMPLETED;
                            break;
                        case ANSIBLE_PLAYBOOK_CONFIG_FOR_CLIENT_SIDE_ROUTER_COMPLETED:
                            if (wasEncodedForCloudSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.DEPLOYED;
                            else
                                newDcnDeploymentState = DcnDeploymentState.ERROR;
                            break;
                        case ANSIBLE_PLAYBOOK_CONFIG_FOR_CLOUD_SIDE_ROUTER_COMPLETED:
                            if(wasEncodedForClientSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.DEPLOYED;
                            else
                                newDcnDeploymentState = DcnDeploymentState.ERROR;
                            break;
                        case REMOVAL_INITIATED:
                            if (wasEncodedForClientSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.ANSIBLE_PLAYBOOK_CONFIG_REMOVAL_FOR_CLIENT_SIDE_ROUTER_COMPLETED;
                            else if (wasEncodedForCloudSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.ANSIBLE_PLAYBOOK_CONFIG_REMOVAL_FOR_CLOUD_SIDE_ROUTER_COMPLETED;
                            break;
                        case ANSIBLE_PLAYBOOK_CONFIG_REMOVAL_FOR_CLIENT_SIDE_ROUTER_COMPLETED:
                            if (wasEncodedForCloudSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.REMOVED;
                            else
                                newDcnDeploymentState = DcnDeploymentState.ERROR;
                            break;
                        case ANSIBLE_PLAYBOOK_CONFIG_REMOVAL_FOR_CLOUD_SIDE_ROUTER_COMPLETED:
                            if(wasEncodedForClientSideRouter(encodedPlaybookIdentifier))
                                newDcnDeploymentState = DcnDeploymentState.REMOVED;
                            else
                                newDcnDeploymentState = DcnDeploymentState.ERROR;
                            break;
                        default:
                            newDcnDeploymentState = DcnDeploymentState.ERROR;
                    }
                    break;
                case FAILURE:
                default:
                    newDcnDeploymentState = deploymentOrRemovalFailureDependingOnLastState(currentDcnDeploymentState);
            }
             notifyStateChangeListeners(domain, newDcnDeploymentState);
        } catch ( InvalidDomainException
                | AnsiblePlaybookIdentifierConverterException e) {
            log.error("Exception during playbook execution state reception -> " + e.getMessage());
            notifyStateChangeListeners(domain, DcnDeploymentState.ERROR);
        }
    }

    DcnDeploymentState deploymentOrRemovalFailureDependingOnLastState(DcnDeploymentState currentDcnDeploymentState) {
        switch (currentDcnDeploymentState) {
            case DEPLOYMENT_INITIATED:
            case ANSIBLE_PLAYBOOK_CONFIG_FOR_CLIENT_SIDE_ROUTER_COMPLETED:
            case ANSIBLE_PLAYBOOK_CONFIG_FOR_CLOUD_SIDE_ROUTER_COMPLETED:
                return DcnDeploymentState.DEPLOYMENT_FAILED;
            case REMOVAL_INITIATED:
            case ANSIBLE_PLAYBOOK_CONFIG_REMOVAL_FOR_CLIENT_SIDE_ROUTER_COMPLETED:
            case ANSIBLE_PLAYBOOK_CONFIG_REMOVAL_FOR_CLOUD_SIDE_ROUTER_COMPLETED:
                return DcnDeploymentState.REMOVAL_FAILED;
            default:
                return DcnDeploymentState.ERROR;
        }
    }

    @Value("${ansible.docker.api.url}")
    public void setAnsibleDockerApiUrl(String ansibleDockerApiUrl) {
        this.ansibleDockerApiUrl = ansibleDockerApiUrl;
    }

}
