package net.geant.nmaas.orchestration;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostAlreadyExistsException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostInvalidException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.DockerComposeServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeService;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test-compose.properties")
public class DefaultAppDeploymentMonitorTest {

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
    private DockerComposeServiceRepositoryManager nmServiceRepositoryManager;
    @Autowired
    private ApplicationEventPublisher publisher;
    @Autowired
    private AppDeploymentRepository appDeploymentRepository;
    @Autowired
    private DockerHostRepositoryManager dockerHostRepositoryManager;

    private static final String DOMAIN = "domain1";
    private static final String DEPLOYMENT_NAME = "this-is-example-deployment-name";
    private final Identifier deploymentId = Identifier.newInstance("this-is-example-deployment-id");

    @Before
    public void setup() throws InvalidDeploymentIdException, UnknownHostException, DockerHostAlreadyExistsException, DockerHostInvalidException, DockerHostNotFoundException {
        dockerHostRepositoryManager.addDockerHost(dockerHost());
        DockerComposeNmServiceInfo nmServiceInfo = new DockerComposeNmServiceInfo(deploymentId, DEPLOYMENT_NAME, DOMAIN, null);
        nmServiceRepositoryManager.storeService(nmServiceInfo);
        DockerComposeService dockerComposeService = new DockerComposeService();
        dockerComposeService.setAttachedVolumeName("testVolumeName");
        dockerComposeService.setPublicPort(8080);
        nmServiceRepositoryManager.updateDockerComposeService(deploymentId, dockerComposeService);
        nmServiceRepositoryManager.updateDockerHost(deploymentId, dockerHostRepositoryManager.loadByName("dh"));
        appDeploymentRepository.save(new AppDeployment(deploymentId, DOMAIN, Identifier.newInstance(""), DEPLOYMENT_NAME, true, 20.0));
        repository.updateState(deploymentId, AppDeploymentState.REQUESTED);
    }

    @After
    public void cleanRepository() throws InvalidDeploymentIdException, DockerHostNotFoundException, DockerHostInvalidException {
        appDeploymentRepository.deleteAll();
        nmServiceRepositoryManager.removeService(deploymentId);
        dockerHostRepositoryManager.removeDockerHost("dh");
    }

    private static final int DELAY = 200;

    @Test
    public void shouldTransitStatesAndReturnCorrectOverallState() throws InvalidDeploymentIdException, InterruptedException {
        // request verification
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.REQUEST_VERIFIED));
        Thread.sleep(DELAY);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.REQUEST_VALIDATED));
        // environment preparation
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.ENVIRONMENT_PREPARATION_INITIATED));
        Thread.sleep(DELAY);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS));
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.ENVIRONMENT_PREPARED));
        Thread.sleep(DELAY);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARED));
        // dcn already exists or was just deployed
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.READY_FOR_DEPLOYMENT));
        Thread.sleep(DELAY);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.MANAGEMENT_VPN_CONFIGURED));
        // app configuration
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.CONFIGURATION_INITIATED));
        Thread.sleep(DELAY);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_CONFIGURATION_IN_PROGRESS));
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.CONFIGURED));
        Thread.sleep(DELAY);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_CONFIGURED));
        // app deployment
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.DEPLOYMENT_INITIATED));
        Thread.sleep(DELAY);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_DEPLOYMENT_IN_PROGRESS));
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.DEPLOYED));
        Thread.sleep(DELAY);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_DEPLOYED));
        // app deployment verification
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.VERIFIED));
        Thread.sleep(DELAY);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED));
        // app removal
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.REMOVED));
        Thread.sleep(DELAY);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_REMOVED));
    }

    @Test
    public void shouldTransitStatesFromInternalError() throws InvalidDeploymentIdException, InterruptedException {
        // introduction of Internal Error
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.CONFIGURATION_FAILED));
        Thread.sleep(DELAY);
        // app removal
        publisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.REMOVED));
        Thread.sleep(DELAY);
        assertThat(monitor.state(deploymentId), equalTo(AppLifecycleState.APPLICATION_REMOVED));
    }

    @Test(expected = InvalidAppStateException.class)
    public void shouldThrowExceptionWhenReadingAccessDetailsInWrongAppState() throws InvalidAppStateException, InvalidDeploymentIdException {
        repository.updateState(deploymentId, AppDeploymentState.DEPLOYMENT_ENVIRONMENT_PREPARED);
        monitor.userAccessDetails(deploymentId);
    }

    @Test
    public void shouldBuildProperAccessDetails() throws InvalidAppStateException, InvalidDeploymentIdException {
        repository.updateState(deploymentId, AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFIED);
        assertThat(monitor.userAccessDetails(deploymentId).getUrl(), equalTo("http://192.168.0.1:8080"));
    }

    public static DockerHost dockerHost() throws UnknownHostException {
        return new DockerHost(
                "dh",
                InetAddress.getByName("192.168.0.1"),
                9999,
                InetAddress.getByName("192.168.0.1"),
                "eth0",
                "eth1",
                InetAddress.getByName("192.168.1.1"),
                "/home/mgmt/scripts",
                "/home/mgmt/volumes",
                false);
    }

}
