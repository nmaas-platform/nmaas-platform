package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.entities.AnsiblePlaybookVpnConfig;
import net.geant.nmaas.dcn.deployment.entities.DcnCloudEndpointDetails;
import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.dcn.deployment.repositories.DcnInfoRepository;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DcnRepositoryManagerTest {

    @Autowired
    private DcnRepositoryManager dcnRepositoryManager;
    @Autowired
    private DcnInfoRepository dcnInfoRepository;
    @Autowired
    private AppDeploymentRepository appDeploymentRepository;

    private static Identifier DEPLOYMENT_ID = Identifier.newInstance("did");
    private static String DOMAIN = "domain";
    private static String DEPLOYMENT_NAME = "deploymentName";

    @BeforeEach
    public void populateRepositories() {
        AppDeployment appDeployment = AppDeployment.builder().deploymentId(DEPLOYMENT_ID)
                .domain(DOMAIN)
                .applicationId(Identifier.newInstance(""))
                .deploymentName(DEPLOYMENT_NAME)
                .configFileRepositoryRequired(true)
                .storageSpace(20)
                .build();
        appDeploymentRepository.save(appDeployment);
    }

    @AfterEach
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

    @Test
    public void shouldThrowExceptionOnMissingDeployment(){
        assertThrows(InvalidDomainException.class, () -> {
            appDeploymentRepository.deleteAll();
            dcnRepositoryManager.loadNetwork(DOMAIN);
        });
    }

    @Test
    public void shouldThrowExceptionOnMissingDcnForGivenClient(){
        assertThrows(InvalidDomainException.class, () -> {
            dcnRepositoryManager.loadNetwork(DOMAIN);
        });
    }

    @Test
    public void shouldThrowExceptionDuringRemovalOnMissingDcnForGivenClient() {
        assertThrows(InvalidDomainException.class, () -> {
            dcnRepositoryManager.removeDcnInfo(DOMAIN);
        });
    }

    @Test
    public void shouldThrowExceptionDuringCloudConfigUpdateOnMissingDcnForGivenClient() {
        assertThrows(InvalidDomainException.class, () -> {
            dcnRepositoryManager.updateAnsiblePlaybookForCloudSideRouter(DOMAIN, null);
        });
    }

    @Test
    public void shouldThrowExceptionDuringClientConfigUpdateOnMissingDcnForGivenClient() {
        assertThrows(InvalidDomainException.class, () -> {
            dcnRepositoryManager.updateAnsiblePlaybookForClientSideRouter(DOMAIN, null);
        });
    }

    @Test
    public void shouldThrowExceptionDuringCloudEndpointDetailsUpdateOnMissingDcnForGivenClient() {
        assertThrows(InvalidDomainException.class, () -> {
            dcnRepositoryManager.updateDcnCloudEndpointDetails(DOMAIN, null);
        });
    }

    @Test
    public void shouldThrowExceptionDuringStateNotificationOnMissingDcnForGivenClient() {
        assertThrows(InvalidDomainException.class, () -> {
            dcnRepositoryManager.notifyStateChange(new DcnDeploymentStateChangeEvent(this, DOMAIN, DcnDeploymentState.DEPLOYMENT_INITIATED));
        });
    }

    @Test
    public void shouldThrowExceptionDuringStateRetrievalOnMissingDcnForGivenClient() {
        assertThrows(InvalidDomainException.class, () -> {
            dcnRepositoryManager.loadCurrentState(DOMAIN);
        });
    }

    private void storeDefaultDcnInfoInRepository() throws InvalidDomainException {
        DcnSpec spec = new DcnSpec("", DOMAIN, DcnDeploymentType.NONE);
        dcnRepositoryManager.storeDcnInfo(new DcnInfo(spec));
    }
}