package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.DeploymentIdToNmServiceNameMapper;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigTemplateHandlingException;
import net.geant.nmaas.nmservice.configuration.ssh.SshCommandExecutor;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceRepository;
import net.geant.nmaas.orchestration.*;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

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
    private AppLifecycleRepository appLifecycleRepository;

    @Autowired
    private AppDeploymentStateChangeListener stateChangeListener;

    @Mock
    private NmServiceConfigurationsPreparer configurationsPreparer;

    @Mock
    private SshCommandExecutor sshCommandExecutor;

    private Identifier deploymentId;

    private AppConfiguration configuration;

    @Before
    public void setup() {
        deploymentId = Identifier.newInstance("id");
        configuration = new AppConfiguration();
        configuration.setApplicationId(null);
        configuration.setJsonInput("");
        appLifecycleRepository.storeNewDeployment(deploymentId);
        appLifecycleRepository.updateDeploymentState(deploymentId, AppDeploymentState.MANAGEMENT_VPN_CONFIGURED);
        configurationExecutor = new SimpleNmServiceConfigurationExecutor(stateChangeListener, configurationsPreparer, sshCommandExecutor);
    }

    @Test
    public void shouldExecuteConfigurationWorkflow() throws InvalidDeploymentIdException, DeploymentIdToNmServiceNameMapper.EntryNotFoundException, ConfigTemplateHandlingException, NmServiceRepository.ServiceNotFoundException, IOException {
        configurationExecutor.configureNmService(deploymentId, configuration, null);
        assertThat(appDeploymentMonitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_CONFIGURED));
    }

}
