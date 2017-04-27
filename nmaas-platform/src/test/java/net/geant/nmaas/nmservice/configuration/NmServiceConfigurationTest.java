package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.nmservice.configuration.ssh.SshCommandExecutor;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerPortForwarding;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerTemplate;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.orchestration.AppDeploymentLifecycleStateKeeper;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.entities.*;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import net.geant.nmaas.orchestration.tasks.AppServiceDeploymentTask;
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

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class NmServiceConfigurationTest {

    private NmServiceConfigurationProvider configurationExecutor;

    @Autowired
    private AppDeploymentMonitor appDeploymentMonitor;

    @Autowired
    private AppDeploymentLifecycleStateKeeper appDeploymentLifecycleStateKeeper;

    @Mock
    private NmServiceConfigurationsPreparer configurationsPreparer;

    @Mock
    private SshCommandExecutor sshCommandExecutor;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private NmServiceRepositoryManager nmServiceRepositoryManager;

    @MockBean
    private AppServiceDeploymentTask appServiceDeploymentTask;

    @Autowired
    private AppDeploymentRepository appDeploymentRepository;

    private Identifier deploymentId = Identifier.newInstance("deploymentId");

    private Identifier clientId = Identifier.newInstance("clientId");

    private Identifier applicationId = Identifier.newInstance("appId");

    private AppConfiguration configuration;

    @Before
    public void setup() throws InvalidDeploymentIdException, InterruptedException {
        nmServiceRepositoryManager.storeService(new NmServiceInfo(deploymentId, clientId, oxidizedTemplate()));
        configuration = new AppConfiguration("");
        appDeploymentRepository.save(new AppDeployment(deploymentId, Identifier.newInstance("clientId"), applicationId));
        appDeploymentLifecycleStateKeeper.updateDeploymentState(deploymentId, AppDeploymentState.MANAGEMENT_VPN_VERIFIED);
        Thread.sleep(200);
        configurationExecutor = new SimpleNmServiceConfigurationExecutor(configurationsPreparer, sshCommandExecutor, applicationEventPublisher);
    }

    @After
    public void cleanRepository() throws InvalidDeploymentIdException {
        appDeploymentRepository.deleteAll();
        nmServiceRepositoryManager.removeService(deploymentId);
    }

    @Test
    public void shouldExecuteConfigurationWorkflow() throws NmServiceConfigurationFailedException, InvalidDeploymentIdException, InterruptedException {
        configurationExecutor.configureNmService(deploymentId, applicationId, configuration, null, null);
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
