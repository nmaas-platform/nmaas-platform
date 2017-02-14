package net.geant.nmaas.orchestration;

import net.geant.nmaas.dcn.deployment.DcnDeploymentState;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceTemplateRepository;
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
public class OxidizedAppDeploymentTest {

    private static final String TEST_CLIENT_ID = "testClientId";

    @Autowired
    private AppLifecycleManager appLifecycleManager;

    @Autowired
    private AppDeploymentStateChangeListener appDeploymentStateChangeListener;

    @Autowired
    private AppDeploymentMonitor appDeploymentMonitor;

    private Identifier clientId;

    private Identifier applicationId;

    @Before
    public void setup() {
        clientId = Identifier.newInstance(TEST_CLIENT_ID);
        applicationId = NmServiceTemplateRepository.OXIDIZED_APPLICATION_ID;
    }

    @Test
    public void shouldTriggerAndFollowTheAppDeploymentWorkflow() throws InvalidDeploymentIdException, InterruptedException {
        final Identifier deploymentId = appLifecycleManager.deployApplication(clientId, applicationId);
        waitAndVerifyRequestValidated(deploymentId);
        waitAndVerifyDeploymentEnvironmentPrepared(deploymentId);
        waitAndVerifyManagementVpnConfigurationInProgress(deploymentId);
        manuallyNotifyDcnDeploymentStateToDeployedAndVerifyManagementVpnConfigured(deploymentId);
        appLifecycleManager.applyConfiguration(deploymentId, new AppConfiguration());
        waitAndVerifyApplicationConfigured(deploymentId);
        waitAndVerifyApplicationDeployed(deploymentId);
        waitAndVerifyApplicationDeploymentVerified(deploymentId);
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
        appDeploymentStateChangeListener.notifyStateChange(deploymentId, DcnDeploymentState.DEPLOYED);
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
