package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostInvalidException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.*;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.InetAddress;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NmServiceRepositoryManagerTest {

    @Autowired
    private NmServiceRepositoryManager repositoryManager;

    @Autowired
    private DockerHostRepositoryManager dockerHostRepositoryManager;

    private Identifier deploymentId = Identifier.newInstance("deploymentId");

    private Identifier deploymentId2 = Identifier.newInstance("deploymentId2");

    private Identifier clientId = Identifier.newInstance("clientId");

    @Before
    public void setup() throws Exception {
        NmServiceInfo service = new NmServiceInfo(deploymentId, clientId, oxidizedTemplate());
        repositoryManager.storeService(service);
        dockerHostRepositoryManager.addDockerHost(dockerHost("dh"));
    }

    @After
    public void clean() throws DockerHostNotFoundException, DockerHostInvalidException, InvalidDeploymentIdException {
        repositoryManager.removeService(deploymentId);
        dockerHostRepositoryManager.removeDockerHost("dh");
    }

    @Test
    public void shouldStoreUpdateAndRemoveServiceInfo() throws InvalidDeploymentIdException, DockerHostNotFoundException, DockerHostInvalidException {
        NmServiceInfo service = new NmServiceInfo(deploymentId2, clientId, oxidizedTemplate());
        repositoryManager.storeService(service);
        assertThat(repositoryManager.loadService(deploymentId), is(notNullValue()));
        assertThat(repositoryManager.loadClientId(deploymentId), equalTo(clientId));
        assertThat(repositoryManager.loadCurrentState(deploymentId), equalTo(NmServiceDeploymentState.INIT));
        assertThat(repositoryManager.loadService(deploymentId).getHost(), is(nullValue()));
        repositoryManager.updateDockerHost(deploymentId, dockerHostRepositoryManager.loadByName("dh"));
        assertThat(repositoryManager.loadService(deploymentId).getHost().getId(), is(notNullValue()));
        repositoryManager.updateDockerContainer(deploymentId, dockerContainer());
        repositoryManager.updateDockerContainerDeploymentId(deploymentId, "containerId");
        assertThat(repositoryManager.loadService(deploymentId).getDockerContainer().getDeploymentId(), equalTo("containerId"));
        repositoryManager.updateDockerContainerNetworkDetails(deploymentId,
                new DockerContainerNetDetails(9090, new DockerNetworkIpamSpec("192.168.0.0/24", "192.168.0.254")));
        repositoryManager.removeService(deploymentId2);
    }

    @Test(expected = Exception.class)
    public void shouldNotAllowForTwoServicesWithTheSameDeploymentId() throws InvalidDeploymentIdException {
        NmServiceInfo service = new NmServiceInfo(deploymentId, clientId, oxidizedTemplate());
        repositoryManager.storeService(service);
    }

    @Test
    public void shouldUpdateStateUponNotification() throws InvalidDeploymentIdException {
        assertThat(repositoryManager.loadCurrentState(deploymentId), equalTo(NmServiceDeploymentState.INIT));
        repositoryManager.notifyStateChange(new NmServiceDeploymentStateChangeEvent(this, deploymentId, NmServiceDeploymentState.READY_FOR_DEPLOYMENT));
        assertThat(repositoryManager.loadCurrentState(deploymentId), equalTo(NmServiceDeploymentState.READY_FOR_DEPLOYMENT));
    }

    public static DockerContainerTemplate oxidizedTemplate() {
        DockerContainerTemplate oxidizedTemplate =
                new DockerContainerTemplate("oxidized/oxidized:latest");
        oxidizedTemplate.setEnvVariables(Arrays.asList("CONFIG_RELOAD_INTERVAL=600"));
        oxidizedTemplate.setExposedPort(new DockerContainerPortForwarding(DockerContainerPortForwarding.Protocol.TCP, 8888));
        oxidizedTemplate.setContainerVolumes(Arrays.asList("/root/.config/oxidized"));
        return oxidizedTemplate;
    }

    private static DockerHost dockerHost(String hostName) throws Exception {
        return new DockerHost(hostName,
                InetAddress.getByName("192.168.0.1"),
                9999,
                InetAddress.getByName("192.168.0.1"),
                "eth0",
                "eth1",
                InetAddress.getByName("192.168.1.1"),
                "/home/mgmt/ansible/volumes",
                false);
    }

    private static DockerContainer dockerContainer() {
        DockerContainer dockerContainer = new DockerContainer();
        dockerContainer.setVolumesDetails(new DockerContainerVolumesDetails("/home/volume"));
        return dockerContainer;
    }

}
