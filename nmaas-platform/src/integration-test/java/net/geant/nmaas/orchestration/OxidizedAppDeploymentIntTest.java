package net.geant.nmaas.orchestration;

import com.spotify.docker.client.exceptions.DockerException;
import net.geant.nmaas.dcn.deployment.DcnDeploymentStateChangeEvent;
import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryInit;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.externalservices.inventory.network.repositories.BasicCustomerNetworkAttachPointRepository;
import net.geant.nmaas.externalservices.inventory.network.repositories.DockerHostAttachPointRepository;
import net.geant.nmaas.helpers.DockerApiClientMockInit;
import net.geant.nmaas.helpers.DockerContainerTemplatesInit;
import net.geant.nmaas.helpers.NetworkAttachPointsInit;
import net.geant.nmaas.nmservice.configuration.ConfigDownloadCommandExecutor;
import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfigurationTemplate;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfigurationTemplatesRepository;
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

    private static final String OXIDIZED_APP_NAME = "Oxidized";
    private static final String OXIDIZED_APP_VERSION = "0.19.0";
    private static final long CUSTOMER_ID = 1L;

    @Autowired
    private AppLifecycleManager appLifecycleManager;
    @Autowired
    private AppDeploymentMonitor appDeploymentMonitor;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private NmServiceConfigurationTemplatesRepository templatesRepository;

    @MockBean
    private DockerApiClient dockerApiClient;
    @MockBean
    private ConfigDownloadCommandExecutor configDownloadCommandExecutor;
    @Autowired
    private DockerNetworkRepositoryManager dockerNetworkRepositoryManager;
    @Autowired
    private DockerHostRepositoryManager dockerHostRepositoryManager;
    @Autowired
    private DockerHostAttachPointRepository dockerHostAttachPointRepository;
    @Autowired
    private BasicCustomerNetworkAttachPointRepository basicCustomerNetworkAttachPointRepository;

    private Identifier clientId;
    private Long testAppId;

    @Before
    public void setup() throws DockerException, InterruptedException {
        clientId = Identifier.newInstance(String.valueOf(CUSTOMER_ID));
        storeOxidizedApp();
        storeOxidizedConfigurationTemplates();
        DockerApiClientMockInit.mockMethods(dockerApiClient);
        DockerHostRepositoryInit.addDefaultDockerHost(dockerHostRepositoryManager);
        NetworkAttachPointsInit.initDockerHostAttachPoints(dockerHostAttachPointRepository);
        NetworkAttachPointsInit.initBasicCustomerNetworkAttachPoints(basicCustomerNetworkAttachPointRepository);
    }

    @After
    public void clear() throws InvalidClientIdException {
        dockerNetworkRepositoryManager.removeNetwork(clientId);
        DockerHostRepositoryInit.removeDefaultDockerHost(dockerHostRepositoryManager);
        NetworkAttachPointsInit.cleanDockerHostAttachPoints(dockerHostAttachPointRepository);
        NetworkAttachPointsInit.cleanBasicCustomerNetworkAttachPoints(basicCustomerNetworkAttachPointRepository);
        applicationRepository.deleteAll();
        templatesRepository.deleteAll();
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

    private void storeOxidizedConfigurationTemplates() {
        NmServiceConfigurationTemplate oxidizedConfigTemplate1 = new NmServiceConfigurationTemplate();
        oxidizedConfigTemplate1.setApplicationId(testAppId);
        oxidizedConfigTemplate1.setConfigFileName("config");
        oxidizedConfigTemplate1.setConfigFileTemplateContent("");
        templatesRepository.save(oxidizedConfigTemplate1);
        NmServiceConfigurationTemplate oxidizedConfigTemplate2 = new NmServiceConfigurationTemplate();
        oxidizedConfigTemplate2.setApplicationId(testAppId);
        oxidizedConfigTemplate2.setConfigFileName("router.db");
        oxidizedConfigTemplate2.setConfigFileTemplateContent("");
        templatesRepository.save(oxidizedConfigTemplate2);
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
