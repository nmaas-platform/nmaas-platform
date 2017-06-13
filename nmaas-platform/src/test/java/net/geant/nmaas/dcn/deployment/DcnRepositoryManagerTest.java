package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.entities.*;
import net.geant.nmaas.dcn.deployment.repositories.DcnInfoRepository;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidClientIdException;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
public class DcnRepositoryManagerTest {

    @Autowired
    private DcnRepositoryManager dcnRepositoryManager;

    @Autowired
    private DcnInfoRepository dcnInfoRepository;

    @Autowired
    private AppDeploymentRepository appDeploymentRepository;

    private Identifier deploymentId = Identifier.newInstance("deploymentId");

    private Identifier clientId = Identifier.newInstance("clientId");

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
    public void shouldAddUpdateAndRemoteDcns() throws InvalidClientIdException {
        storeDefaultDcnInfoInRepository();
        assertThat(dcnRepositoryManager.loadCurrentState(clientId), equalTo(DcnDeploymentState.INIT));
        assertThat(dcnRepositoryManager.loadNetwork(clientId).getPlaybookForClientSideRouter(), is(nullValue()));
        dcnRepositoryManager.notifyStateChange(new DcnDeploymentStateChangeEvent(this, clientId, DcnDeploymentState.REQUEST_VERIFIED));
        assertThat(dcnRepositoryManager.loadCurrentState(clientId), equalTo(DcnDeploymentState.REQUEST_VERIFIED));
        assertThat(dcnRepositoryManager.loadNetwork(clientId), is(notNullValue()));
        dcnRepositoryManager.removeDcnInfo(clientId);
        assertThat(dcnInfoRepository.count(), equalTo(0L));
    }

    @Test
    public void shouldUpdateClientSideRouterVpnConfigs() throws InvalidClientIdException {
        storeDefaultDcnInfoInRepository();
        AnsiblePlaybookVpnConfig vpn = new AnsiblePlaybookVpnConfig(AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE);
        dcnRepositoryManager.updateAnsiblePlaybookForClientSideRouter(clientId, vpn);
        assertThat(dcnRepositoryManager.loadNetwork(clientId).getPlaybookForClientSideRouter(), is(notNullValue()));
        assertThat(dcnRepositoryManager.loadNetwork(clientId).getPlaybookForClientSideRouter().getId(), is(notNullValue()));
        assertThat(dcnRepositoryManager.loadNetwork(clientId).getPlaybookForClientSideRouter().getLogicalInterface(), is(nullValue()));
        vpn = dcnRepositoryManager.loadNetwork(clientId).getPlaybookForClientSideRouter();
        vpn.setLogicalInterface("ifaceName");
        dcnRepositoryManager.updateAnsiblePlaybookForClientSideRouter(clientId, vpn);
        assertThat(dcnRepositoryManager.loadNetwork(clientId).getPlaybookForClientSideRouter().getLogicalInterface(), equalTo("ifaceName"));
    }

    @Test
    public void shouldUpdateCloudSideRouterVpnConfigs() throws InvalidClientIdException {
        storeDefaultDcnInfoInRepository();
        AnsiblePlaybookVpnConfig vpn = new AnsiblePlaybookVpnConfig(AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE);
        dcnRepositoryManager.updateAnsiblePlaybookForCloudSideRouter(clientId, vpn);
        assertThat(dcnRepositoryManager.loadNetwork(clientId).getPlaybookForCloudSideRouter(), is(notNullValue()));
        assertThat(dcnRepositoryManager.loadNetwork(clientId).getPlaybookForCloudSideRouter().getId(), is(notNullValue()));
        assertThat(dcnRepositoryManager.loadNetwork(clientId).getPlaybookForCloudSideRouter().getLogicalInterface(), is(nullValue()));
        vpn = dcnRepositoryManager.loadNetwork(clientId).getPlaybookForCloudSideRouter();
        vpn.setLogicalInterface("ifaceName");
        dcnRepositoryManager.updateAnsiblePlaybookForCloudSideRouter(clientId, vpn);
        assertThat(dcnRepositoryManager.loadNetwork(clientId).getPlaybookForCloudSideRouter().getLogicalInterface(), equalTo("ifaceName"));
    }

    @Test
    public void shouldUpdateCloudEndpointDetails() throws InvalidClientIdException {
        storeDefaultDcnInfoInRepository();
        DcnCloudEndpointDetails dcnCloudEndpointDetails = new DcnCloudEndpointDetails(501, "10.10.0.0/24", "10.10.0.254");
        dcnRepositoryManager.updateDcnCloudEndpointDetails(clientId, dcnCloudEndpointDetails);
        assertThat(dcnRepositoryManager.loadNetwork(clientId).getCloudEndpointDetails(), is(notNullValue()));
        assertThat(dcnRepositoryManager.loadNetwork(clientId).getCloudEndpointDetails().getId(), is(notNullValue()));
        assertThat(dcnRepositoryManager.loadNetwork(clientId).getCloudEndpointDetails().getVlanNumber(), equalTo(501));
        assertThat(dcnRepositoryManager.loadNetwork(clientId).getCloudEndpointDetails().getSubnet(), equalTo("10.10.0.0/24"));
        assertThat(dcnRepositoryManager.loadNetwork(clientId).getCloudEndpointDetails().getGateway(), equalTo("10.10.0.254"));
        dcnCloudEndpointDetails = dcnRepositoryManager.loadNetwork(clientId).getCloudEndpointDetails();
        dcnCloudEndpointDetails.setGateway("gw");
        dcnRepositoryManager.updateDcnCloudEndpointDetails(clientId, dcnCloudEndpointDetails);
        assertThat(dcnRepositoryManager.loadNetwork(clientId).getCloudEndpointDetails().getGateway(), equalTo("gw"));
    }

    @Test(expected = InvalidClientIdException.class)
    public void shouldThrowExceptionOnMissingDeployment() throws InvalidClientIdException {
        appDeploymentRepository.deleteAll();
        dcnRepositoryManager.loadNetwork(clientId);
    }

    @Test(expected = InvalidClientIdException.class)
    public void shouldThrowExceptionOnMissingDcnForGivenClient() throws InvalidClientIdException {
        dcnRepositoryManager.loadNetwork(clientId);
    }

    @Test(expected = InvalidClientIdException.class)
    public void shouldThrowExceptionDuringRemovalOnMissingDcnForGivenClient() throws InvalidClientIdException {
        dcnRepositoryManager.removeDcnInfo(clientId);
    }

    @Test(expected = InvalidClientIdException.class)
    public void shouldThrowExceptionDuringCloudConfigUpdateOnMissingDcnForGivenClient() throws InvalidClientIdException {
        dcnRepositoryManager.updateAnsiblePlaybookForCloudSideRouter(clientId, null);
    }

    @Test(expected = InvalidClientIdException.class)
    public void shouldThrowExceptionDuringClientConfigUpdateOnMissingDcnForGivenClient() throws InvalidClientIdException {
        dcnRepositoryManager.updateAnsiblePlaybookForClientSideRouter(clientId, null);
    }

    @Test(expected = InvalidClientIdException.class)
    public void shouldThrowExceptionDuringCloudEndpointDetailsUpdateOnMissingDcnForGivenClient() throws InvalidClientIdException {
        dcnRepositoryManager.updateDcnCloudEndpointDetails(clientId, null);
    }

    @Test(expected = InvalidClientIdException.class)
    public void shouldThrowExceptionDuringStateNotificationOnMissingDcnForGivenClient() throws InvalidClientIdException {
        dcnRepositoryManager.notifyStateChange(new DcnDeploymentStateChangeEvent(this, clientId, DcnDeploymentState.DEPLOYMENT_INITIATED));
    }

    @Test(expected = InvalidClientIdException.class)
    public void shouldThrowExceptionDuringStateRetrievalOnMissingDcnForGivenClient() throws InvalidClientIdException {
        dcnRepositoryManager.loadCurrentState(clientId);
    }

    private void storeDefaultDcnInfoInRepository() throws InvalidClientIdException {
        DcnSpec spec = new DcnSpec("", clientId);
        dcnRepositoryManager.storeDcnInfo(new DcnInfo(spec));
    }
}
