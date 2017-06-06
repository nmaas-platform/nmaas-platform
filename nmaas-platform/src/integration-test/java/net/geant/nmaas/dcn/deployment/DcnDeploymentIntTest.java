package net.geant.nmaas.dcn.deployment;

import com.spotify.docker.client.exceptions.DockerException;
import net.geant.nmaas.dcn.deployment.api.AnsiblePlaybookStatus;
import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotDeployDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotRemoveDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.DcnRequestVerificationException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.externalservices.inventory.vpnconfigs.AnsiblePlaybookVpnConfigExistsException;
import net.geant.nmaas.externalservices.inventory.vpnconfigs.AnsiblePlaybookVpnConfigInvalidException;
import net.geant.nmaas.externalservices.inventory.vpnconfigs.AnsiblePlaybookVpnConfigRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerApiClient;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNetwork;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.repositories.DockerNetworkRepository;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.orchestration.DcnDeploymentStateChangeManager;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidClientIdException;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DcnDeploymentIntTest {

    @Autowired
    private DcnRepositoryManager dcnRepositoryManager;
    @Autowired
    private AnsiblePlaybookVpnConfigRepository vpnConfigRepository;
    @Autowired
    private AppDeploymentRepository appDeploymentRepository;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private DockerNetworkRepository dockerNetworkRepository;
    @Mock
    private DockerApiClient dockerApiClient;

    @Autowired
    private DockerHostRepositoryManager dockerHostRepositoryManager;
    @Autowired
    private AnsiblePlaybookVpnConfigRepositoryInit ansiblePlaybookVpnConfigRepositoryInit;
    @MockBean
    private DcnDeploymentStateChangeManager dcnDeploymentStateChangeManager;

    private Identifier deploymentId = Identifier.newInstance("deploymentId");
    private Identifier clientId = Identifier.newInstance("clientId");
    private Identifier applicationId = Identifier.newInstance("applicationId");
    private String dcnName = "dcnName1";

    private DcnDeploymentCoordinator dcnDeployment;

    @Before
    public void setup() throws DockerHostNotFoundException,
            AnsiblePlaybookVpnConfigInvalidException,
            AnsiblePlaybookVpnConfigExistsException,
            DockerException,
            InterruptedException {
        ansiblePlaybookVpnConfigRepositoryInit.initWithDefaults();
        appDeploymentRepository.save(new AppDeployment(deploymentId, clientId, applicationId));
        dockerNetworkRepository.save(new DockerNetwork(clientId, dockerHost(), 505, "10.10.10.0/24", "10.10.10.254"));
        dcnDeployment = new DcnDeploymentCoordinator(
                dcnRepositoryManager,
                vpnConfigRepository,
                applicationEventPublisher,
                dockerNetworkRepository,
                dockerApiClient);
        when(dockerApiClient.createContainer(any(), any(), any())).thenReturn("containerId");
    }

    @After
    public void clear() {
        dockerNetworkRepository.deleteAll();
    }

    @Test
    public void shouldExecuteCompleteDcnDeploymentWorkflow() throws
            DcnRequestVerificationException,
            CouldNotDeployDcnException,
            CouldNotRemoveDcnException,
            InvalidClientIdException, InterruptedException {
        dcnDeployment.verifyRequest(clientId, new DcnSpec(dcnName, clientId));
        Thread.sleep(500);
        assertThat(dcnRepositoryManager.loadCurrentState(clientId), equalTo(DcnDeploymentState.REQUEST_VERIFIED));
        dcnDeployment.deployDcn(clientId);
        Thread.sleep(300);
        assertThat(dcnRepositoryManager.loadCurrentState(clientId), equalTo(DcnDeploymentState.DEPLOYMENT_INITIATED));
        dcnDeployment.notifyPlaybookExecutionState(AnsiblePlaybookIdentifierConverter.encodeForClientSideRouter(clientId.value()), AnsiblePlaybookStatus.Status.SUCCESS);
        Thread.sleep(300);
        dcnDeployment.notifyPlaybookExecutionState(AnsiblePlaybookIdentifierConverter.encodeForCloudSideRouter(clientId.value()), AnsiblePlaybookStatus.Status.SUCCESS);
        Thread.sleep(500);
        assertThat(dcnRepositoryManager.loadCurrentState(clientId), equalTo(DcnDeploymentState.DEPLOYED));
        assertThat(dcnRepositoryManager.loadNetwork(clientId).getCloudEndpointDetails().getVlanNumber(), equalTo(505));
        dcnDeployment.removeDcn(clientId);
        Thread.sleep(300);
        assertThat(dcnRepositoryManager.loadCurrentState(clientId), equalTo(DcnDeploymentState.REMOVAL_INITIATED));
        dcnDeployment.notifyPlaybookExecutionState(AnsiblePlaybookIdentifierConverter.encodeForClientSideRouter(clientId.value()), AnsiblePlaybookStatus.Status.SUCCESS);
        Thread.sleep(300);
        dcnDeployment.notifyPlaybookExecutionState(AnsiblePlaybookIdentifierConverter.encodeForCloudSideRouter(clientId.value()), AnsiblePlaybookStatus.Status.SUCCESS);
        Thread.sleep(500);
        assertThat(dcnRepositoryManager.loadAllNetworks().size(), equalTo(0));
    }

    private DockerHost dockerHost() throws DockerHostNotFoundException {
        return dockerHostRepositoryManager.loadPreferredDockerHost();
    }

}
