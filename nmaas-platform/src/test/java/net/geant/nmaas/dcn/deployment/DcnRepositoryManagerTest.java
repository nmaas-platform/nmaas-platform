package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.entities.*;
import net.geant.nmaas.dcn.deployment.repositories.DcnInfoRepository;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
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
@TestPropertySource("classpath:application-test-engine.properties")
public class DcnRepositoryManagerTest {

    @Autowired
    private DcnRepositoryManager dcnRepositoryManager;
    @Autowired
    private DcnInfoRepository dcnInfoRepository;
    @Autowired
    private AppDeploymentRepository appDeploymentRepository;

    private static Identifier DEPLOYMENT_ID = Identifier.newInstance("did");
    private static String DOMAIN = "domain";

    @Before
    public void populateRepositories() {
        appDeploymentRepository.save(new AppDeployment(DEPLOYMENT_ID, DOMAIN, Identifier.newInstance("")));
    }

    @After
    public void cleanRepositories() {
        appDeploymentRepository.deleteAll();
        dcnInfoRepository.deleteAll();
    }

    @Test
    public void shouldAddUpdateAndRemoteDcns() throws InvalidDomainException {
        storeDefaultDcnInfoInRepository();
        assertThat(dcnRepositoryManager.loadCurrentState(DOMAIN), equalTo(DcnDeploymentState.INIT));
        assertThat(dcnRepositoryManager.loadNetwork(DOMAIN).getPlaybookForClientSideRouter(), is(nullValue()));
        dcnRepositoryManager.notifyStateChange(new DcnDeploymentStateChangeEvent(this, DOMAIN, DcnDeploymentState.REQUEST_VERIFIED));
        assertThat(dcnRepositoryManager.loadCurrentState(DOMAIN), equalTo(DcnDeploymentState.REQUEST_VERIFIED));
        assertThat(dcnRepositoryManager.loadNetwork(DOMAIN), is(notNullValue()));
        dcnRepositoryManager.removeDcnInfo(DOMAIN);
        assertThat(dcnInfoRepository.count(), equalTo(0L));
    }

    @Test
    public void shouldUpdateClientSideRouterVpnConfigs() throws InvalidDomainException {
        storeDefaultDcnInfoInRepository();
        AnsiblePlaybookVpnConfig vpn = new AnsiblePlaybookVpnConfig(AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE);
        dcnRepositoryManager.updateAnsiblePlaybookForClientSideRouter(DOMAIN, vpn);
        assertThat(dcnRepositoryManager.loadNetwork(DOMAIN).getPlaybookForClientSideRouter(), is(notNullValue()));
        assertThat(dcnRepositoryManager.loadNetwork(DOMAIN).getPlaybookForClientSideRouter().getId(), is(notNullValue()));
        assertThat(dcnRepositoryManager.loadNetwork(DOMAIN).getPlaybookForClientSideRouter().getLogicalInterface(), is(nullValue()));
        vpn = dcnRepositoryManager.loadNetwork(DOMAIN).getPlaybookForClientSideRouter();
        vpn.setLogicalInterface("ifaceName");
        dcnRepositoryManager.updateAnsiblePlaybookForClientSideRouter(DOMAIN, vpn);
        assertThat(dcnRepositoryManager.loadNetwork(DOMAIN).getPlaybookForClientSideRouter().getLogicalInterface(), equalTo("ifaceName"));
    }

    @Test
    public void shouldUpdateCloudSideRouterVpnConfigs() throws InvalidDomainException {
        storeDefaultDcnInfoInRepository();
        AnsiblePlaybookVpnConfig vpn = new AnsiblePlaybookVpnConfig(AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE);
        dcnRepositoryManager.updateAnsiblePlaybookForCloudSideRouter(DOMAIN, vpn);
        assertThat(dcnRepositoryManager.loadNetwork(DOMAIN).getPlaybookForCloudSideRouter(), is(notNullValue()));
        assertThat(dcnRepositoryManager.loadNetwork(DOMAIN).getPlaybookForCloudSideRouter().getId(), is(notNullValue()));
        assertThat(dcnRepositoryManager.loadNetwork(DOMAIN).getPlaybookForCloudSideRouter().getLogicalInterface(), is(nullValue()));
        vpn = dcnRepositoryManager.loadNetwork(DOMAIN).getPlaybookForCloudSideRouter();
        vpn.setLogicalInterface("ifaceName");
        dcnRepositoryManager.updateAnsiblePlaybookForCloudSideRouter(DOMAIN, vpn);
        assertThat(dcnRepositoryManager.loadNetwork(DOMAIN).getPlaybookForCloudSideRouter().getLogicalInterface(), equalTo("ifaceName"));
    }

    @Test
    public void shouldUpdateCloudEndpointDetails() throws InvalidDomainException {
        storeDefaultDcnInfoInRepository();
        DcnCloudEndpointDetails dcnCloudEndpointDetails = new DcnCloudEndpointDetails(501, "10.10.0.0/24", "10.10.0.254");
        dcnRepositoryManager.updateDcnCloudEndpointDetails(DOMAIN, dcnCloudEndpointDetails);
        assertThat(dcnRepositoryManager.loadNetwork(DOMAIN).getCloudEndpointDetails(), is(notNullValue()));
        assertThat(dcnRepositoryManager.loadNetwork(DOMAIN).getCloudEndpointDetails().getId(), is(notNullValue()));
        assertThat(dcnRepositoryManager.loadNetwork(DOMAIN).getCloudEndpointDetails().getVlanNumber(), equalTo(501));
        assertThat(dcnRepositoryManager.loadNetwork(DOMAIN).getCloudEndpointDetails().getSubnet(), equalTo("10.10.0.0/24"));
        assertThat(dcnRepositoryManager.loadNetwork(DOMAIN).getCloudEndpointDetails().getGateway(), equalTo("10.10.0.254"));
        dcnCloudEndpointDetails = dcnRepositoryManager.loadNetwork(DOMAIN).getCloudEndpointDetails();
        dcnCloudEndpointDetails.setGateway("gw");
        dcnRepositoryManager.updateDcnCloudEndpointDetails(DOMAIN, dcnCloudEndpointDetails);
        assertThat(dcnRepositoryManager.loadNetwork(DOMAIN).getCloudEndpointDetails().getGateway(), equalTo("gw"));
    }

    @Test(expected = InvalidDomainException.class)
    public void shouldThrowExceptionOnMissingDeployment() throws InvalidDomainException {
        appDeploymentRepository.deleteAll();
        dcnRepositoryManager.loadNetwork(DOMAIN);
    }

    @Test(expected = InvalidDomainException.class)
    public void shouldThrowExceptionOnMissingDcnForGivenClient() throws InvalidDomainException {
        dcnRepositoryManager.loadNetwork(DOMAIN);
    }

    @Test(expected = InvalidDomainException.class)
    public void shouldThrowExceptionDuringRemovalOnMissingDcnForGivenClient() throws InvalidDomainException {
        dcnRepositoryManager.removeDcnInfo(DOMAIN);
    }

    @Test(expected = InvalidDomainException.class)
    public void shouldThrowExceptionDuringCloudConfigUpdateOnMissingDcnForGivenClient() throws InvalidDomainException {
        dcnRepositoryManager.updateAnsiblePlaybookForCloudSideRouter(DOMAIN, null);
    }

    @Test(expected = InvalidDomainException.class)
    public void shouldThrowExceptionDuringClientConfigUpdateOnMissingDcnForGivenClient() throws InvalidDomainException {
        dcnRepositoryManager.updateAnsiblePlaybookForClientSideRouter(DOMAIN, null);
    }

    @Test(expected = InvalidDomainException.class)
    public void shouldThrowExceptionDuringCloudEndpointDetailsUpdateOnMissingDcnForGivenClient() throws InvalidDomainException {
        dcnRepositoryManager.updateDcnCloudEndpointDetails(DOMAIN, null);
    }

    @Test(expected = InvalidDomainException.class)
    public void shouldThrowExceptionDuringStateNotificationOnMissingDcnForGivenClient() throws InvalidDomainException {
        dcnRepositoryManager.notifyStateChange(new DcnDeploymentStateChangeEvent(this, DOMAIN, DcnDeploymentState.DEPLOYMENT_INITIATED));
    }

    @Test(expected = InvalidDomainException.class)
    public void shouldThrowExceptionDuringStateRetrievalOnMissingDcnForGivenClient() throws InvalidDomainException {
        dcnRepositoryManager.loadCurrentState(DOMAIN);
    }

    private void storeDefaultDcnInfoInRepository() throws InvalidDomainException {
        DcnSpec spec = new DcnSpec("", DOMAIN);
        dcnRepositoryManager.storeDcnInfo(new DcnInfo(spec));
    }
}
