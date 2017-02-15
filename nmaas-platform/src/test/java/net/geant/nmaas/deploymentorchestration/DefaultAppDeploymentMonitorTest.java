package net.geant.nmaas.deploymentorchestration;

import net.geant.nmaas.dcn.deployment.DcnDeploymentState;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DefaultAppDeploymentMonitorTest {

    @Autowired
    private AppLifecycleRepository repository;

    @Autowired
    private DefaultAppDeploymentMonitor monitor;

    private Identifier deploymentId = new Identifier("testDeploymentId");

    @Before
    public void setup() {
        repository.updateDeploymentState(deploymentId, AppDeploymentState.REQUESTED);
    }

    @Test
    public void shouldTransitStatesAndReturnCorrectOverallState() throws InvalidDeploymentIdException {
        // request verification
        monitor.notifyStateChange(deploymentId, NmServiceDeploymentState.REQUEST_VERIFIED);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.REQUEST_VALIDATION_IN_PROGRESS));
        monitor.notifyStateChange(deploymentId, DcnDeploymentState.REQUEST_VERIFIED);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.REQUEST_VALIDATED));
        // environment preparation
        monitor.notifyStateChange(deploymentId, NmServiceDeploymentState.ENVIRONMENT_PREPARED);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS));
        monitor.notifyStateChange(deploymentId, DcnDeploymentState.ENVIRONMENT_PREPARED);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARED));
        // dcn deployment
        monitor.notifyStateChange(deploymentId, DcnDeploymentState.DEPLOYMENT_INITIATED);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.MANAGEMENT_VPN_CONFIGURATION_IN_PROGRESS));
        monitor.notifyStateChange(deploymentId, DcnDeploymentState.DEPLOYED);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.MANAGEMENT_VPN_CONFIGURED));
        // app configuration
        monitor.notifyStateChange(deploymentId, NmServiceDeploymentState.CONFIGURATION_INITIATED);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_CONFIGURATION_IN_PROGRESS));
        monitor.notifyStateChange(deploymentId, NmServiceDeploymentState.CONFIGURED);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_CONFIGURED));
        // app deployment
        monitor.notifyStateChange(deploymentId, NmServiceDeploymentState.DEPLOYMENT_INITIATED);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_DEPLOYMENT_IN_PROGRESS));
        monitor.notifyStateChange(deploymentId, NmServiceDeploymentState.DEPLOYED);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_DEPLOYED));
        // app deployment verification
        monitor.notifyStateChange(deploymentId, NmServiceDeploymentState.VERIFIED);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED));
        // app removal
        monitor.notifyStateChange(deploymentId, NmServiceDeploymentState.REMOVED);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_REMOVAL_IN_PROGRESS));
        monitor.notifyStateChange(deploymentId, DcnDeploymentState.REMOVED);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_REMOVED));
    }

}
