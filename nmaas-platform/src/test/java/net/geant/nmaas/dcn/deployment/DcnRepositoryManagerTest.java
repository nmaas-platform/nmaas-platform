package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.entities.AnsiblePlaybookVpnConfig;
import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.dcn.deployment.repositories.DcnInfoRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.ContainerNetworkDetails;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.ContainerNetworkIpamSpec;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidClientIdException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DcnRepositoryManagerTest {

    @Autowired
    private DcnRepositoryManager dcnRepositoryManager;

    @Autowired
    private DcnInfoRepository dcnInfoRepository;

    @Autowired
    private AppDeploymentRepository appDeploymentRepository;

    private Identifier deploymentId = Identifier.newInstance("deploymentId");

    private Identifier clientId = Identifier.newInstance("clientId");

    private String dcnName = "dcnName";

    @Before
    public void populateRepositories() {
        appDeploymentRepository.save(new AppDeployment(deploymentId, clientId, Identifier.newInstance("")));
    }

    @After
    public void cleanRepositories() {
        appDeploymentRepository.deleteAll();
        dcnInfoRepository.deleteAll();
    }

    @Test
    public void shouldAddUpdateAndRemoteDcns() throws InvalidDeploymentIdException, InvalidClientIdException {
        storeDefaultDcnInfoInRepository();
        assertThat(dcnRepositoryManager.loadCurrentState(deploymentId), equalTo(DcnDeploymentState.INIT));
        assertThat(dcnRepositoryManager.loadNetwork(deploymentId).getAnsiblePlaybookForClientSideRouter(), is(nullValue()));
        dcnRepositoryManager.notifyStateChange(new DcnDeploymentStateChangeEvent(this, deploymentId, DcnDeploymentState.REQUEST_VERIFIED));
        assertThat(dcnRepositoryManager.loadCurrentState(deploymentId), equalTo(DcnDeploymentState.REQUEST_VERIFIED));
        assertThat(dcnRepositoryManager.loadNetwork(deploymentId), is(notNullValue()));
        dcnRepositoryManager.removeDcnInfo(clientId);
        assertThat(dcnInfoRepository.count(), equalTo(0L));
    }

    @Test
    public void shouldUpdateClientSideRouterVpnConfigs() throws InvalidDeploymentIdException, InvalidClientIdException {
        storeDefaultDcnInfoInRepository();
        AnsiblePlaybookVpnConfig vpn = new AnsiblePlaybookVpnConfig(AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE);
        dcnRepositoryManager.updateAnsiblePlaybookForClientSideRouter(deploymentId, vpn);
        assertThat(dcnRepositoryManager.loadNetwork(deploymentId).getAnsiblePlaybookForClientSideRouter(), is(notNullValue()));
        assertThat(dcnRepositoryManager.loadNetwork(deploymentId).getAnsiblePlaybookForClientSideRouter().getId(), is(notNullValue()));
        assertThat(dcnRepositoryManager.loadNetwork(deploymentId).getAnsiblePlaybookForClientSideRouter().getLogicalInterface(), is(nullValue()));
        vpn = dcnRepositoryManager.loadNetwork(deploymentId).getAnsiblePlaybookForClientSideRouter();
        vpn.setLogicalInterface("ifaceName");
        dcnRepositoryManager.updateAnsiblePlaybookForClientSideRouter(deploymentId, vpn);
        assertThat(dcnRepositoryManager.loadNetwork(deploymentId).getAnsiblePlaybookForClientSideRouter().getLogicalInterface(), equalTo("ifaceName"));
    }

    @Test
    public void shouldUpdateCloudSideRouterVpnConfigs() throws InvalidDeploymentIdException, InvalidClientIdException {
        storeDefaultDcnInfoInRepository();
        AnsiblePlaybookVpnConfig vpn = new AnsiblePlaybookVpnConfig(AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE);
        dcnRepositoryManager.updateAnsiblePlaybookForCloudSideRouter(deploymentId, vpn);
        assertThat(dcnRepositoryManager.loadNetwork(deploymentId).getAnsiblePlaybookForCloudSideRouter(), is(notNullValue()));
        assertThat(dcnRepositoryManager.loadNetwork(deploymentId).getAnsiblePlaybookForCloudSideRouter().getId(), is(notNullValue()));
        assertThat(dcnRepositoryManager.loadNetwork(deploymentId).getAnsiblePlaybookForCloudSideRouter().getLogicalInterface(), is(nullValue()));
        vpn = dcnRepositoryManager.loadNetwork(deploymentId).getAnsiblePlaybookForCloudSideRouter();
        vpn.setLogicalInterface("ifaceName");
        dcnRepositoryManager.updateAnsiblePlaybookForCloudSideRouter(deploymentId, vpn);
        assertThat(dcnRepositoryManager.loadNetwork(deploymentId).getAnsiblePlaybookForCloudSideRouter().getLogicalInterface(), equalTo("ifaceName"));
    }

    @Test(expected = InvalidDeploymentIdException.class)
    public void shouldThrowExceptionOnMissingDeployment() throws InvalidDeploymentIdException, InvalidClientIdException {
        appDeploymentRepository.deleteAll();
        dcnRepositoryManager.loadNetwork(deploymentId);
    }

    @Test(expected = InvalidClientIdException.class)
    public void shouldThrowExceptionOnMissingDcnForGivenClient() throws InvalidDeploymentIdException, InvalidClientIdException {
        dcnRepositoryManager.loadNetwork(deploymentId);
    }

    @Test(expected = InvalidClientIdException.class)
    public void shouldThrowExceptionDuringRemovalOnMissingDcnForGivenClient() throws InvalidDeploymentIdException, InvalidClientIdException {
        dcnRepositoryManager.removeDcnInfo(clientId);
    }

    @Test(expected = InvalidClientIdException.class)
    public void shouldThrowExceptionDuringCloudConfigUpdateOnMissingDcnForGivenClient() throws InvalidDeploymentIdException, InvalidClientIdException {
        dcnRepositoryManager.updateAnsiblePlaybookForCloudSideRouter(deploymentId, null);
    }

    @Test(expected = InvalidClientIdException.class)
    public void shouldThrowExceptionDuringClientConfigUpdateOnMissingDcnForGivenClient() throws InvalidDeploymentIdException, InvalidClientIdException {
        dcnRepositoryManager.updateAnsiblePlaybookForClientSideRouter(deploymentId, null);
    }

    @Test(expected = InvalidClientIdException.class)
    public void shouldThrowExceptionDuringStateNotificationOnMissingDcnForGivenClient() throws InvalidDeploymentIdException, InvalidClientIdException {
        dcnRepositoryManager.notifyStateChange(new DcnDeploymentStateChangeEvent(this, deploymentId, DcnDeploymentState.DEPLOYMENT_INITIATED));
    }

    @Test(expected = InvalidClientIdException.class)
    public void shouldThrowExceptionDuringStateRetrievalOnMissingDcnForGivenClient() throws InvalidDeploymentIdException, InvalidClientIdException {
        dcnRepositoryManager.loadCurrentState(deploymentId);
    }

    private void storeDefaultDcnInfoInRepository() {
        DcnSpec spec = new DcnSpec(dcnName, clientId);
        ContainerNetworkIpamSpec containerNetworkIpamSpec = new ContainerNetworkIpamSpec("10.10.0.0/24", "10.10.0.254");
        ContainerNetworkDetails containerNetworkDetails = new ContainerNetworkDetails(8080, containerNetworkIpamSpec, 505);
        spec.setNmServiceDeploymentNetworkDetails(containerNetworkDetails);
        dcnRepositoryManager.storeDcnInfo(new DcnInfo(spec));
    }
}
