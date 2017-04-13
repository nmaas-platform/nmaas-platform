package net.geant.nmaas.orchestration;

import net.geant.nmaas.dcn.deployment.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.DcnDeploymentStateChangeEvent;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.AppLifecycleState;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit4.SpringRunner;

import static net.geant.nmaas.orchestration.AppLifecycleManager.OXIDIZED_APPLICATION_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class OxidizedAppDeploymentTest {

    private static final String TEST_CLIENT_ID = "testClientId";

    @Autowired
    private AppLifecycleManager appLifecycleManager;

    @Autowired
    private AppDeploymentMonitor appDeploymentMonitor;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    private Identifier clientId;

    private Identifier applicationId;

    private String jsonInput;

    @Before
    public void setup() {
        clientId = Identifier.newInstance(TEST_CLIENT_ID);
        applicationId = OXIDIZED_APPLICATION_ID;
        jsonInput = "" +
                "{" +
                    "\"routers\": [\"1.1.1.1\",\"2.2.2.2\"], " +
                    "\"oxidizedUsername\":\"oxidized\", " +
                    "\"oxidizedPassword\":\"9v5oEo3n\"" +
                "}";
    }

    @Ignore
    @Test
    public void shouldTriggerAndFollowTheAppDeploymentWorkflow() throws InvalidDeploymentIdException, InterruptedException, InvalidAppStateException {
        final Identifier deploymentId = appLifecycleManager.deployApplication(clientId, applicationId);
        waitAndVerifyRequestValidated(deploymentId);
        waitAndVerifyDeploymentEnvironmentPrepared(deploymentId);
        waitAndVerifyManagementVpnConfigurationInProgress(deploymentId);
        manuallyNotifyDcnDeploymentStateToDeployedAndVerifyManagementVpnConfigured(deploymentId);
        appLifecycleManager.applyConfiguration(deploymentId, new AppConfiguration(jsonInput));
        // this won't work since the configuration file needs to be download from a running NMaaS Portal instance
        waitAndVerifyApplicationConfigured(deploymentId);
        waitAndVerifyApplicationDeployed(deploymentId);
        waitAndVerifyApplicationDeploymentVerified(deploymentId);
        assertThat(appDeploymentMonitor.userAccessDetails(deploymentId), is(notNullValue()));
    }

    private void waitAndVerifyRequestValidated(Identifier deploymentId) throws InvalidDeploymentIdException, InterruptedException {
        while(!appDeploymentMonitor.state(deploymentId).equals(AppLifecycleState.REQUEST_VALIDATED)) {
            System.out.println("Waiting for AppLifecycleState.REQUEST_VALIDATED)");
            Thread.sleep(200);
        }
        assertThat(appDeploymentMonitor.state(deploymentId), equalTo(AppLifecycleState.REQUEST_VALIDATED));
    }

    private void waitAndVerifyDeploymentEnvironmentPrepared(Identifier deploymentId) throws InvalidDeploymentIdException, InterruptedException {
        while(!appDeploymentMonitor.state(deploymentId).equals(AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARED)) {
            System.out.println("Waiting for AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARED)");
            Thread.sleep(200);
        }
        assertThat(appDeploymentMonitor.state(deploymentId), equalTo(AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARED));
    }

    private void waitAndVerifyManagementVpnConfigurationInProgress(Identifier deploymentId) throws InvalidDeploymentIdException, InterruptedException {
        while(!appDeploymentMonitor.state(deploymentId).equals(AppLifecycleState.MANAGEMENT_VPN_CONFIGURATION_IN_PROGRESS)) {
            System.out.println("Waiting for AppLifecycleState.MANAGEMENT_VPN_CONFIGURATION_IN_PROGRESS)");
            Thread.sleep(200);
        }
        assertThat(appDeploymentMonitor.state(deploymentId), equalTo(AppLifecycleState.MANAGEMENT_VPN_CONFIGURATION_IN_PROGRESS));
    }

    private void manuallyNotifyDcnDeploymentStateToDeployedAndVerifyManagementVpnConfigured(Identifier deploymentId) throws InvalidDeploymentIdException {
        applicationEventPublisher.publishEvent(new DcnDeploymentStateChangeEvent(this, deploymentId, DcnDeploymentState.DEPLOYED));
        assertThat(appDeploymentMonitor.state(deploymentId), equalTo(AppLifecycleState.MANAGEMENT_VPN_CONFIGURED));
    }

    private void waitAndVerifyApplicationConfigured(Identifier deploymentId) throws InvalidDeploymentIdException, InterruptedException {
        while(!appDeploymentMonitor.state(deploymentId).equals(AppLifecycleState.APPLICATION_CONFIGURED)) {
            System.out.println("Waiting for AppLifecycleState.APPLICATION_CONFIGURED)");
            Thread.sleep(200);
        }
        assertThat(appDeploymentMonitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_CONFIGURED));
    }

    private void waitAndVerifyApplicationDeployed(Identifier deploymentId) throws InvalidDeploymentIdException, InterruptedException {
        while(!appDeploymentMonitor.state(deploymentId).equals(AppLifecycleState.APPLICATION_DEPLOYED)) {
            System.out.println("Waiting for AppLifecycleState.APPLICATION_DEPLOYED)");
            Thread.sleep(200);
        }
        assertThat(appDeploymentMonitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_DEPLOYED));
    }

    private void waitAndVerifyApplicationDeploymentVerified(Identifier deploymentId) throws InvalidDeploymentIdException, InterruptedException {
        while(!appDeploymentMonitor.state(deploymentId).equals(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED)) {
            System.out.println("Waiting for AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED)");
            Thread.sleep(200);
        }
        assertThat(appDeploymentMonitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED));
    }
}
