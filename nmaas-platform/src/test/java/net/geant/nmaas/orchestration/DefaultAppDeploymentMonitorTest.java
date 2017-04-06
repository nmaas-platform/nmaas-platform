package net.geant.nmaas.orchestration;

import net.geant.nmaas.dcn.deployment.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.DcnDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class DefaultAppDeploymentMonitorTest {

    @Autowired
    private AppLifecycleRepository repository;

    @Autowired
    private DefaultAppDeploymentMonitor monitor;

    @Autowired
    private ApplicationEventPublisher publisher;

    private Identifier deploymentId = new Identifier("testDeploymentId");

    @Before
    public void setup() {
        repository.updateDeploymentState(deploymentId, AppDeploymentState.REQUESTED);
    }

    @Test
    public void shouldTransitStatesAndReturnCorrectOverallState() throws InvalidDeploymentIdException {
        // request verification
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.REQUEST_VERIFIED));
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.REQUEST_VALIDATION_IN_PROGRESS));
        publisher.publishEvent(new DcnDeploymentStateChangeEvent(this, deploymentId, DcnDeploymentState.REQUEST_VERIFIED));
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.REQUEST_VALIDATED));
        // environment preparation
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.ENVIRONMENT_PREPARED));
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS));
        publisher.publishEvent(new DcnDeploymentStateChangeEvent(this, deploymentId, DcnDeploymentState.ENVIRONMENT_PREPARED));
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARED));
        // dcn deployment
        publisher.publishEvent(new DcnDeploymentStateChangeEvent(this, deploymentId, DcnDeploymentState.DEPLOYMENT_INITIATED));
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.MANAGEMENT_VPN_CONFIGURATION_IN_PROGRESS));
        publisher.publishEvent(new DcnDeploymentStateChangeEvent(this, deploymentId, DcnDeploymentState.DEPLOYED));
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.MANAGEMENT_VPN_CONFIGURED));
        // app configuration
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.CONFIGURATION_INITIATED));
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_CONFIGURATION_IN_PROGRESS));
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.CONFIGURED));
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_CONFIGURED));
        // app deployment
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.DEPLOYMENT_INITIATED));
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_DEPLOYMENT_IN_PROGRESS));
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.DEPLOYED));
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_DEPLOYED));
        // app deployment verification
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.VERIFIED));
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED));
        // app removal
        publisher.publishEvent(new DcnDeploymentStateChangeEvent(this, deploymentId, DcnDeploymentState.REMOVAL_INITIATED));
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_REMOVAL_IN_PROGRESS));
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.REMOVED));
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_REMOVAL_IN_PROGRESS));
        publisher.publishEvent(new DcnDeploymentStateChangeEvent(this, deploymentId, DcnDeploymentState.REMOVED));
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_REMOVED));
    }

    @Test
    public void shouldTransitStatesFromInternalError() throws InvalidDeploymentIdException {
        // introduction of Internal Error
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED));
        // app removal
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.REMOVED));
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_REMOVAL_IN_PROGRESS));
        publisher.publishEvent(new DcnDeploymentStateChangeEvent(this, deploymentId, DcnDeploymentState.REMOVAL_INITIATED));
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_REMOVAL_IN_PROGRESS));
        publisher.publishEvent(new DcnDeploymentStateChangeEvent(this, deploymentId, DcnDeploymentState.REMOVED));
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_REMOVED));
    }

}
