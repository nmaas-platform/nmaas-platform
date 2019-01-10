package net.geant.nmaas.dcn.deployment;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.dcn.deployment.api.model.AnsiblePlaybookStatus;
import net.geant.nmaas.dcn.deployment.entities.AnsiblePlaybookVpnConfig;
import net.geant.nmaas.dcn.deployment.entities.DcnCloudEndpointDetails;
import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.dcn.deployment.entities.DcnState;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotDeployDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotRemoveDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.DcnRequestVerificationException;
import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterAttachPointManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KClusterAttachPoint;
import net.geant.nmaas.externalservices.inventory.network.NetworkAttachPoint;
import net.geant.nmaas.externalservices.inventory.network.exceptions.AttachPointNotFoundException;
import net.geant.nmaas.externalservices.inventory.network.repositories.DomainNetworkAttachPointRepository;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookContainerBuilder.buildContainerForClientSideRouterConfig;
import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookContainerBuilder.buildContainerForClientSideRouterConfigRemoval;
import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookContainerBuilder.buildContainerForCloudSideRouterConfig;
import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookContainerBuilder.buildContainerForCloudSideRouterConfigRemoval;
import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookIdentifierConverter.AnsiblePlaybookIdentifierConverterException;
import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookIdentifierConverter.decode;
import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookIdentifierConverter.encodeForClientSideRouter;
import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookIdentifierConverter.encodeForCloudSideRouter;
import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookIdentifierConverter.wasEncodedForClientSideRouter;
import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookIdentifierConverter.wasEncodedForCloudSideRouter;

/**
 * Executor used when DCN should be configured by Ansible playbooks.
 */
@Component
@Profile("dcn_ansible")
@Log4j2
public class AnsibleDcnDeploymentExecutor implements DcnDeploymentProvider, AnsiblePlaybookExecutionStateListener {

    private static final String DEFAULT_ANSIBLE_CONTAINER_NAME = "nmaas-ansible-test";

    private DcnRepositoryManager dcnRepositoryManager;
    private ApplicationEventPublisher applicationEventPublisher;
    private DomainNetworkAttachPointRepository basicCustomerNetworkAttachPointRepository;
    private KClusterAttachPointManager kClusterAttachPointManager;
    private DockerApiClient dockerApiClient;

    private String ansibleDockerApiUrl;

    @Autowired
    public AnsibleDcnDeploymentExecutor(DcnRepositoryManager dcnRepositoryManager,
                                        ApplicationEventPublisher applicationEventPublisher,
                                        DomainNetworkAttachPointRepository basicCustomerNetworkAttachPointRepository,
                                        KClusterAttachPointManager kClusterAttachPointManager,
                                        DockerApiClient dockerApiClient) {
        this.dcnRepositoryManager = dcnRepositoryManager;
        this.applicationEventPublisher = applicationEventPublisher;
        this.basicCustomerNetworkAttachPointRepository = basicCustomerNetworkAttachPointRepository;
        this.kClusterAttachPointManager = kClusterAttachPointManager;
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
    public void verifyRequest(String domain, DcnSpec dcnSpec) {
        try {
            storeDcnInfoIfNotExists(domain, dcnSpec);
            notifyStateChangeListeners(domain, DcnDeploymentState.REQUESTED);
            final KClusterAttachPoint kClusterAttachPoint = kClusterAttachPointManager.getAttachPoint();
            final DcnCloudEndpointDetails dcnCloudEndpointDetails = new DcnCloudEndpointDetails(
                    kClusterAttachPoint.getVlanNumber(),
                    kClusterAttachPoint.getSubnet(),
                    kClusterAttachPoint.getGateway()
            );
            dcnRepositoryManager.updateDcnCloudEndpointDetails(domain, dcnCloudEndpointDetails);
            NetworkAttachPoint customerNetworkAttachPoint = basicCustomerNetworkAttachPointRepository
                   .findByDomain(domain)
                   .orElseThrow(() -> new AttachPointNotFoundException(domain));
            AnsiblePlaybookVpnConfig customerSideRouterVpnConfig =
                    AnsiblePlaybookVpnConfigBuilder.fromCustomerNetworkAttachPoint(customerNetworkAttachPoint);
            dcnRepositoryManager.updateAnsiblePlaybookForClientSideRouter(
                    domain,
                    customerSideRouterVpnConfig);
            AnsiblePlaybookVpnConfig cloudSideRouterVpnConfig =
                    AnsiblePlaybookVpnConfigBuilder.fromCloudAttachPoint(customerSideRouterVpnConfig, kClusterAttachPoint);
            cloudSideRouterVpnConfig.merge(dcnCloudEndpointDetails);
            dcnRepositoryManager.updateAnsiblePlaybookForCloudSideRouter(domain, cloudSideRouterVpnConfig);
            notifyStateChangeListeners(domain, DcnDeploymentState.REQUEST_VERIFIED);
        } catch ( InvalidDomainException
                | AttachPointNotFoundException e) {
            notifyStateChangeListeners(domain, DcnDeploymentState.REQUEST_VERIFICATION_FAILED);
            throw new DcnRequestVerificationException("Exception during DCN request verification -> " + e.getMessage());
        }
    }

    private void storeDcnInfoIfNotExists(String domain, DcnSpec dcnSpec) {
        if (!dcnRepositoryManager.exists(domain))
            dcnRepositoryManager.storeDcnInfo(new DcnInfo(dcnSpec));
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void deployDcn(String domain) {
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
                | DockerException anyException) {
            notifyStateChangeListeners(domain, DcnDeploymentState.DEPLOYMENT_FAILED);
            throw new CouldNotDeployDcnException("Exception during DCN deployment -> " + anyException.getMessage());
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    private void removeOldAnsiblePlaybookContainers() {
        try {
            final List<Container> containers = dockerApiClient.listContainers(ansibleDockerApiUrl, DockerClient.ListContainersParam.withStatusExited());
            for (Container container : containers) {
                log.debug("Removing old container " + container.id());
                dockerApiClient.removeContainer(ansibleDockerApiUrl, container.id());
            }
        } catch (DockerException e) {
            log.warn("Failed to remove old Ansible containers", e);
        } catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyDcn(String domain) {
        try {
            notifyStateChangeListeners(domain, DcnDeploymentState.VERIFICATION_INITIATED);
            // TODO implement DCN verification functionality
            Thread.sleep(1000);
            notifyStateChangeListeners(domain, DcnDeploymentState.VERIFIED);
        } catch (InterruptedException e) {
            notifyStateChangeListeners(domain, DcnDeploymentState.VERIFICATION_FAILED);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeDcn(String domain) {
        try {
            final DcnInfo dcnInfo = dcnRepositoryManager.loadNetwork(domain);
            deployAnsiblePlaybookContainers(
                    buildContainerForClientSideRouterConfigRemoval(dcnInfo.getPlaybookForClientSideRouter(), encodeForClientSideRouter(domain)),
                    buildContainerForCloudSideRouterConfigRemoval(dcnInfo.getPlaybookForCloudSideRouter(), encodeForCloudSideRouter(domain)));
            notifyStateChangeListeners(domain, DcnDeploymentState.REMOVAL_INITIATED);
        } catch ( InvalidDomainException
                | DockerException e) {
            notifyStateChangeListeners(domain, DcnDeploymentState.REMOVAL_FAILED);
            throw new CouldNotRemoveDcnException("Exception during DCN removal -> " + e.getMessage());
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
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
