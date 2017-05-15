package net.geant.nmaas.orchestration;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostExistsException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostInvalidException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.*;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.entities.AppLifecycleState;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import net.geant.nmaas.orchestration.tasks.app.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
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
    private DefaultAppDeploymentMonitor monitor;
    @Autowired
    private NmServiceRepositoryManager nmServiceRepositoryManager;
    @Autowired
    private ApplicationEventPublisher publisher;
    @Autowired
    private AppDeploymentRepository appDeploymentRepository;
    @Autowired
    private DockerHostRepositoryManager dockerHostRepositoryManager;

    private final Identifier deploymentId = Identifier.newInstance("this-is-example-deployment-id");
    private final Identifier applicationId = Identifier.newInstance("this-is-example-application-id");
    private final Identifier clientId = Identifier.newInstance("this-is-example-client-id");

    @Before
    public void setup() throws InvalidDeploymentIdException, UnknownHostException, DockerHostExistsException, DockerHostInvalidException, DockerHostNotFoundException {
        dockerHostRepositoryManager.addDockerHost(dockerHost());
        DockerNetworkIpamSpec dockerNetworkIpamSpec = new DockerNetworkIpamSpec("10.10.0.0/24", "10.10.0.254");
        DockerContainerNetDetails dockerContainerNetDetails = new DockerContainerNetDetails(8080, dockerNetworkIpamSpec);
        DockerContainerVolumesDetails dockerContainerVolumesDetails = new DockerContainerVolumesDetails("/home/directory");
        DockerContainer dockerContainer = new DockerContainer();
        dockerContainer.setNetworkDetails(dockerContainerNetDetails);
        dockerContainer.setVolumesDetails(dockerContainerVolumesDetails);
        nmServiceRepositoryManager.storeService(new NmServiceInfo(deploymentId, applicationId, clientId, oxidizedTemplate()));
        nmServiceRepositoryManager.updateDockerContainer(deploymentId, dockerContainer);
        nmServiceRepositoryManager.updateDockerHost(deploymentId, dockerHostRepositoryManager.loadByName("dh"));
        appDeploymentRepository.save(new AppDeployment(deploymentId, clientId, Identifier.newInstance("")));
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

    public static DockerContainerTemplate oxidizedTemplate() {
        DockerContainerTemplate oxidizedTemplate =
                new DockerContainerTemplate("oxidized/oxidized:latest");
        oxidizedTemplate.setEnvVariables(Arrays.asList("CONFIG_RELOAD_INTERVAL=600"));
        oxidizedTemplate.setExposedPort(new DockerContainerPortForwarding(DockerContainerPortForwarding.Protocol.TCP, 8888));
        oxidizedTemplate.setContainerVolumes(Arrays.asList("/root/.config/oxidized"));
        return oxidizedTemplate;
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
