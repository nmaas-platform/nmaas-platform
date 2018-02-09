package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.exceptions.ConfigTemplateHandlingException;
import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.nmservice.configuration.exceptions.UserConfigHandlingException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerEngineServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerPortForwarding;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerTemplate;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.entities.*;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.tasks.app.AppServiceDeploymentTask;
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

import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test-engine.properties")
public class DockerNmServiceConfigurationTest {

    @Autowired
    private NmServiceConfigurationProvider configurationProvider;
    @Autowired
    private AppDeploymentMonitor appDeploymentMonitor;
    @Autowired
    private AppDeploymentRepositoryManager appDeploymentRepositoryManager;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @MockBean
    private NmServiceConfigurationFilePreparer configurationsPreparer;
    @MockBean
    private DockerHostConfigDownloadCommandExecutor dockerHostConfigDownloadCommandExecutor;
    @MockBean
    private DockerEngineServiceRepositoryManager nmServiceRepositoryManager;
    @MockBean
    private AppServiceDeploymentTask appServiceDeploymentTask;

    private Identifier deploymentId = Identifier.newInstance("deploymentId");
    private Identifier clientId = Identifier.newInstance("clientId");
    private Identifier applicationId = Identifier.newInstance("appId");
    private AppConfiguration configuration;

    @Before
    public void setup() throws InvalidDeploymentIdException, InterruptedException {
        configuration = new AppConfiguration("");
        appDeploymentRepositoryManager.store(new AppDeployment(deploymentId, Identifier.newInstance("clientId"), applicationId));
        appDeploymentRepositoryManager.updateState(deploymentId, AppDeploymentState.MANAGEMENT_VPN_CONFIGURED);
    }

    @After
    public void cleanRepository() throws InvalidDeploymentIdException {
        appDeploymentRepositoryManager.removeAll();
        nmServiceRepositoryManager.removeService(deploymentId);
    }

    @Test
    public void shouldExecuteConfigurationWorkflow() throws NmServiceConfigurationFailedException, InvalidDeploymentIdException, InterruptedException, UserConfigHandlingException, ConfigTemplateHandlingException {
        when(configurationsPreparer.generateAndStoreConfigFiles(any(), any(), any())).thenAnswer((invocationOnMock) -> {Thread.sleep(500); return new ArrayList<String>();});
        configurationProvider.configureNmService(deploymentId, applicationId, configuration);
        Thread.sleep(200);
        assertThat(appDeploymentMonitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_CONFIGURED));
    }

    public static DockerContainerTemplate oxidizedTemplate() {
        DockerContainerTemplate oxidizedTemplate = new DockerContainerTemplate("oxidized/oxidized:latest");
        oxidizedTemplate.setEnvVariables(Arrays.asList("CONFIG_RELOAD_INTERVAL=600"));
        oxidizedTemplate.setExposedPort(new DockerContainerPortForwarding(DockerContainerPortForwarding.Protocol.TCP, 8888));
        oxidizedTemplate.setContainerVolumes(Arrays.asList("/root/.config/oxidized"));
        return oxidizedTemplate;
    }

}
