package net.geant.nmaas.orchestration;

import com.spotify.docker.client.exceptions.DockerException;
import net.geant.nmaas.dcn.deployment.AnsiblePlaybookVpnConfigRepositoryInit;
import net.geant.nmaas.dcn.deployment.DcnDeploymentStateChangeEvent;
import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.externalservices.inventory.vpnconfigs.AnsiblePlaybookVpnConfigExistsException;
import net.geant.nmaas.externalservices.inventory.vpnconfigs.AnsiblePlaybookVpnConfigInvalidException;
import net.geant.nmaas.helpers.DockerApiClientMockInit;
import net.geant.nmaas.nmservice.configuration.ConfigDownloadCommandExecutor;
import net.geant.nmaas.helpers.DockerContainerTemplatesInit;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerApiClient;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.DockerNetworkRepositoryManager;
import net.geant.nmaas.orchestration.entities.*;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidClientIdException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
public class OxidizedAppDeploymentIntTest {

    private static final String TEST_CLIENT_ID = "testClientId";
    private static final String OXIDIZED_APP_NAME = "Oxidized";
    private static final String OXIDIZED_APP_VERSION = "0.19.0";

    @Autowired
    private AppLifecycleManager appLifecycleManager;
    @Autowired
    private AppDeploymentMonitor appDeploymentMonitor;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private ApplicationRepository applicationRepository;

    @MockBean
    private DockerApiClient dockerApiClient;
    @MockBean
    private ConfigDownloadCommandExecutor configDownloadCommandExecutor;
    @Autowired
    private AnsiblePlaybookVpnConfigRepositoryInit ansiblePlaybookVpnConfigRepositoryInit;
    @Autowired
    private DockerNetworkRepositoryManager dockerNetworkRepositoryManager;

    private Identifier clientId;
    private Long testAppId;

    @Before
    public void setup() throws AnsiblePlaybookVpnConfigInvalidException, AnsiblePlaybookVpnConfigExistsException, DockerException, InterruptedException {
        clientId = Identifier.newInstance(TEST_CLIENT_ID);
        storeOxidizedApp();
        ansiblePlaybookVpnConfigRepositoryInit.initWithDefaults();
        DockerApiClientMockInit.mockMethods(dockerApiClient);
    }

    @After
    public void clear() throws InvalidClientIdException {
        dockerNetworkRepositoryManager.removeNetwork(clientId);
    }

    @Test
    public void shouldTriggerAndFollowTheAppDeploymentWorkflow() throws InvalidDeploymentIdException, InterruptedException, InvalidAppStateException {
        final Identifier deploymentId = appLifecycleManager.deployApplication(clientId, Identifier.newInstance(String.valueOf(testAppId)));
        waitAndVerifyDeploymentEnvironmentPrepared(deploymentId);
        manuallyNotifyDcnDeploymentStateToDeployed(deploymentId);
        waitAndVerifyManagementVpnConfigured(deploymentId);
        appLifecycleManager.applyConfiguration(deploymentId, new AppConfiguration(exampleOxidizedAppConfigurationJson()));
        waitAndVerifyApplicationDeploymentVerified(deploymentId);
        assertThat(appDeploymentMonitor.userAccessDetails(deploymentId), is(notNullValue()));
    }

    private void storeOxidizedApp() {
        Application app = new Application(OXIDIZED_APP_NAME);
        app.setVersion(OXIDIZED_APP_VERSION);
        AppDeploymentSpec appDeploymentSpec = new AppDeploymentSpec();
        appDeploymentSpec.setSupportedDeploymentEnvironments(Arrays.asList(AppDeploymentEnv.DOCKER_ENGINE));
        appDeploymentSpec.setDockerContainerTemplate(DockerContainerTemplatesInit.oxidizedTemplate());
        app.setAppDeploymentSpec(appDeploymentSpec);
        testAppId = applicationRepository.save(app).getId();
    }

    private String exampleOxidizedAppConfigurationJson() {
        return "" +
                "{" +
                "\"oxidizedUsername\": \"testusername\"," +
                "\"oxidizedPassword\": \"testpassword\"," +
                "\"targets\": [" +
                "{" +
                    "\"ipAddress\": \"1.1.1.1\"" +
                "}," +
                "{" +
                    "\"ipAddress\": \"2.2.2.2\"" +
                "}]" +
                "}";
    }

    private void waitAndVerifyDeploymentEnvironmentPrepared(Identifier deploymentId) throws InvalidDeploymentIdException, InterruptedException {
        Thread.sleep(3000);
        assertThat(appDeploymentMonitor.state(deploymentId), equalTo(AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARED));
    }

    private void manuallyNotifyDcnDeploymentStateToDeployed(Identifier deploymentId) throws InvalidDeploymentIdException {
        applicationEventPublisher.publishEvent(new DcnDeploymentStateChangeEvent(this, clientId, DcnDeploymentState.VERIFIED));
    }

    private void waitAndVerifyManagementVpnConfigured(Identifier deploymentId) throws InvalidDeploymentIdException, InterruptedException {
        Thread.sleep(500);
        assertThat(appDeploymentMonitor.state(deploymentId), equalTo(AppLifecycleState.MANAGEMENT_VPN_CONFIGURED));
    }

    private void waitAndVerifyApplicationDeploymentVerified(Identifier deploymentId) throws InvalidDeploymentIdException, InterruptedException {
        Thread.sleep(2000);
        assertThat(appDeploymentMonitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED));
    }

}
