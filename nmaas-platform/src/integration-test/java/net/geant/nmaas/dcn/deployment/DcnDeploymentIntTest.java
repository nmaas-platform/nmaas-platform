package net.geant.nmaas.dcn.deployment;

import com.spotify.docker.client.exceptions.DockerException;
import net.geant.nmaas.dcn.deployment.api.AnsiblePlaybookStatus;
import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotDeployDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotRemoveDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.DcnRequestVerificationException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryInit;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.network.repositories.BasicCustomerNetworkAttachPointRepository;
import net.geant.nmaas.externalservices.inventory.network.repositories.DockerHostAttachPointRepository;
import net.geant.nmaas.helpers.NetworkAttachPointsInit;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerApiClient;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNetwork;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.repositories.DockerNetworkRepository;
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

    private static final long CUSTOMER_ID = 1L;

    @Autowired
    private DcnRepositoryManager dcnRepositoryManager;
    @Autowired
    private DockerHostAttachPointRepository dockerHostAttachPointRepository;
    @Autowired
    private BasicCustomerNetworkAttachPointRepository basicCustomerNetworkAttachPointRepository;
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
    @MockBean
    private DcnDeploymentStateChangeManager dcnDeploymentStateChangeManager;

    private Identifier deploymentId = Identifier.newInstance("deploymentId");
    private Identifier clientId = Identifier.newInstance(String.valueOf(CUSTOMER_ID));
    private Identifier applicationId = Identifier.newInstance("applicationId");

    private DcnDeploymentCoordinator dcnDeployment;

    @Before
    public void setup() throws DockerException, InterruptedException, DockerHostNotFoundException {
        DockerHostRepositoryInit.addDefaultDockerHost(dockerHostRepositoryManager);
        NetworkAttachPointsInit.initDockerHostAttachPoints(dockerHostAttachPointRepository);
        NetworkAttachPointsInit.initBasicCustomerNetworkAttachPoints(basicCustomerNetworkAttachPointRepository);
        AppDeployment appDeployment = new AppDeployment(deploymentId, clientId, applicationId);
        appDeploymentRepository.save(appDeployment);
        DockerNetwork dockerNetwork = new DockerNetwork(
                clientId,
                dockerHostRepositoryManager.loadPreferredDockerHost(),
                505,
                "10.10.10.0/24",
                "10.10.10.254");
        dockerNetworkRepository.save(dockerNetwork);
        dcnDeployment = new DcnDeploymentCoordinator(
                dcnRepositoryManager,
                dockerHostAttachPointRepository,
                basicCustomerNetworkAttachPointRepository,
                applicationEventPublisher,
                dockerNetworkRepository,
                dockerApiClient);
        when(dockerApiClient.createContainer(any(), any(), any())).thenReturn("containerId");
    }

    @After
    public void clear() {
        DockerHostRepositoryInit.removeDefaultDockerHost(dockerHostRepositoryManager);
        NetworkAttachPointsInit.cleanDockerHostAttachPoints(dockerHostAttachPointRepository);
        NetworkAttachPointsInit.cleanBasicCustomerNetworkAttachPoints(basicCustomerNetworkAttachPointRepository);
        dockerNetworkRepository.deleteAll();
    }

    @Test
    public void shouldExecuteCompleteDcnDeploymentWorkflow() throws
            DcnRequestVerificationException,
            CouldNotDeployDcnException,
            CouldNotRemoveDcnException,
            InvalidClientIdException, InterruptedException {
        dcnDeployment.verifyRequest(clientId, new DcnSpec("dcnName1", clientId));
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

}
