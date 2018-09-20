package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.exceptions.ConfigTemplateHandlingException;
import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.nmservice.configuration.exceptions.UserConfigHandlingException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.DockerComposeServiceRepositoryManager;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.entities.AppLifecycleState;
import net.geant.nmaas.orchestration.entities.Identifier;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test-compose.properties")
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
    private DockerComposeServiceRepositoryManager nmServiceRepositoryManager;
    @MockBean
    private AppServiceDeploymentTask appServiceDeploymentTask;

    private Identifier deploymentId = Identifier.newInstance("deploymentId");
    private Identifier applicationId = Identifier.newInstance("appId");
    private AppConfiguration configuration;

    @Before
    public void setup() throws InvalidDeploymentIdException, InterruptedException {
        configuration = new AppConfiguration("");
        appDeploymentRepositoryManager.store(new AppDeployment(deploymentId, "domain", applicationId, "deploymentName", true, 20));
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
        configurationProvider.configureNmService(deploymentId, applicationId, configuration, true);
        Thread.sleep(200);
        assertThat(appDeploymentMonitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_CONFIGURED));
    }

}
