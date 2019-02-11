package net.geant.nmaas.orchestration;

import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.entities.AppLifecycleState;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import net.geant.nmaas.orchestration.tasks.app.AppConfigurationTask;
import net.geant.nmaas.orchestration.tasks.app.AppDcnRequestOrVerificationTask;
import net.geant.nmaas.orchestration.tasks.app.AppEnvironmentPreparationTask;
import net.geant.nmaas.orchestration.tasks.app.AppRemovalTask;
import net.geant.nmaas.orchestration.tasks.app.AppRequestVerificationTask;
import net.geant.nmaas.orchestration.tasks.app.AppServiceDeploymentTask;
import net.geant.nmaas.orchestration.tasks.app.AppServiceVerificationTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DefaultAppDeploymentMonitorIntTest {

    @MockBean
    private AppRequestVerificationTask appRequestVerificationTask;
    @MockBean
    private AppEnvironmentPreparationTask appEnvironmentPreparationTask;
    @MockBean
    private AppDcnRequestOrVerificationTask appDcnRequestOrVerificationTask;
    @MockBean
    private AppConfigurationTask appConfigurationTask;
    @MockBean
    private AppServiceDeploymentTask appServiceDeploymentTask;
    @MockBean
    private AppServiceVerificationTask appServiceVerificationTask;
    @MockBean
    private AppRemovalTask appRemovalTask;

    @Autowired
    private AppDeploymentRepositoryManager repository;
    @Autowired
    private AppDeploymentMonitor monitor;
    @Autowired
    private KubernetesRepositoryManager kubernetesRepositoryManager;
    @Autowired
    private ApplicationEventPublisher publisher;
    @Autowired
    private AppDeploymentRepository appDeploymentRepository;

    private static final String DOMAIN = "domain1";
    private static final String DEPLOYMENT_NAME = "this-is-example-deployment-name";
    private static final int DELAY = 200;
    private final Identifier deploymentId = Identifier.newInstance("this-is-example-deployment-id");

    @Before
    public void setup() throws InvalidDeploymentIdException {
        KubernetesNmServiceInfo nmServiceInfo = new KubernetesNmServiceInfo(deploymentId, DEPLOYMENT_NAME, DOMAIN, 20, null);
        kubernetesRepositoryManager.storeService(nmServiceInfo);
        AppDeployment appDeployment = AppDeployment.builder()
                .deploymentId(deploymentId)
                .domain("domain")
                .applicationId(Identifier.newInstance(""))
                .deploymentName("deploymentName")
                .configFileRepositoryRequired(true)
                .storageSpace(20)
                .build();
        appDeploymentRepository.save(appDeployment);
        repository.updateState(deploymentId, AppDeploymentState.REQUESTED);
    }

    @After
    public void cleanRepository() throws InvalidDeploymentIdException {
        appDeploymentRepository.deleteAll();
        kubernetesRepositoryManager.removeService(deploymentId);
    }

    @Test
    public void shouldTransitStatesAndReturnCorrectOverallState() throws InvalidDeploymentIdException, InterruptedException {
        // request verification
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.REQUEST_VERIFIED, ""));
        Thread.sleep(DELAY);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.REQUEST_VALIDATED));
        // environment preparation
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.ENVIRONMENT_PREPARATION_INITIATED, ""));
        Thread.sleep(DELAY);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS));
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.ENVIRONMENT_PREPARED, ""));
        Thread.sleep(DELAY);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARED));
        // dcn already exists or was just deployed
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.READY_FOR_DEPLOYMENT, ""));
        Thread.sleep(DELAY);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.MANAGEMENT_VPN_CONFIGURED));
        // app configuration
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.CONFIGURATION_INITIATED, ""));
        Thread.sleep(DELAY);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_CONFIGURATION_IN_PROGRESS));
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.CONFIGURED, ""));
        Thread.sleep(DELAY);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_CONFIGURED));
        // app deployment
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.DEPLOYMENT_INITIATED, ""));
        Thread.sleep(DELAY);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_DEPLOYMENT_IN_PROGRESS));
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.DEPLOYED, ""));
        Thread.sleep(DELAY);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_DEPLOYED));
        // app deployment verification
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.VERIFIED, ""));
        Thread.sleep(DELAY);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED));
        // app removal
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.REMOVAL_INITIATED, ""));
        Thread.sleep(DELAY);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_REMOVAL_IN_PROGRESS));
    }

    @Test
    public void shouldTransitStatesFromInternalError() throws InvalidDeploymentIdException, InterruptedException {
        // introduction of Internal Error
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED, ""));
        Thread.sleep(DELAY);
        // app removal
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.REMOVAL_INITIATED, ""));
        Thread.sleep(DELAY);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_REMOVAL_IN_PROGRESS));
    }

    @Test(expected = InvalidAppStateException.class)
    public void shouldThrowExceptionWhenReadingAccessDetailsInWrongAppState() throws InvalidAppStateException, InvalidDeploymentIdException {
        repository.updateState(deploymentId, AppDeploymentState.DEPLOYMENT_ENVIRONMENT_PREPARED);
        monitor.userAccessDetails(deploymentId);
    }

}
