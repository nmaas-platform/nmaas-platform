package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.DeploymentIdToNmServiceNameMapper;
import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.nmservice.configuration.ssh.SshCommandExecutor;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceRepository;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppLifecycleRepository;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.entities.AppLifecycleState;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit4.SpringRunner;

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

    @Mock
    private NmServiceConfigurationsPreparer configurationsPreparer;

    @Mock
    private SshCommandExecutor sshCommandExecutor;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private DeploymentIdToNmServiceNameMapper mapper;

    @Autowired
    private NmServiceRepository nmServiceRepository;

    private Identifier deploymentId;

    private Identifier applicationId;

    private AppConfiguration configuration;

    @Before
    public void setup() {
        String serviceName = "name";
        deploymentId = Identifier.newInstance("id");
        applicationId = Identifier.newInstance("appId");
        nmServiceRepository.storeService(new NmServiceInfo(serviceName, null, null));
        mapper.storeMapping(deploymentId, serviceName);
        configuration = new AppConfiguration("");
        appLifecycleRepository.storeNewDeployment(deploymentId);
        appLifecycleRepository.updateDeploymentState(deploymentId, AppDeploymentState.MANAGEMENT_VPN_CONFIGURED);
        configurationExecutor = new SimpleNmServiceConfigurationExecutor(configurationsPreparer, sshCommandExecutor, applicationEventPublisher);
    }

    @Test
    public void shouldExecuteConfigurationWorkflow() throws NmServiceConfigurationFailedException, InvalidDeploymentIdException {
        configurationExecutor.configureNmService(deploymentId, applicationId, configuration, null, null);
        assertThat(appDeploymentMonitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_CONFIGURED));
    }

}
