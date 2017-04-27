package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainer;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerNetDetails;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNetwork;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNetworkIpamSpec;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerContainerManagerTest {

    @Autowired
    private DockerContainerManager manager;

    @Test
    public void shouldCheckIfContainerIpAddressAlreadyAssigned() {
        String containerIpAddress = "192.168.0.1";
        assertThat(manager.addressAlreadyAssigned(containerIpAddress, containers()), is(true));
        containerIpAddress = "192.168.0.3";
        assertThat(manager.addressAlreadyAssigned(containerIpAddress, containers()), is(false));
        containerIpAddress = "192.168.0.5";
        assertThat(manager.addressAlreadyAssigned(containerIpAddress, containers()), is(false));
    }

    @Test
    public void shouldObtainIpAddressForNewContainer() {
        DockerNetwork dockerNetwork = new DockerNetwork(null, null, 5, "192.168.0.0/24", "192.168.0.254");
        dockerNetwork.setDockerContainers(containers());
        assertThat(manager.obtainIpAddressForNewContainer(dockerNetwork), equalTo("192.168.0.3"));
    }

    private List<DockerContainer> containers() {
        DockerContainer dockerContainer1 = new DockerContainer();
        dockerContainer1.setNetworkDetails(new DockerContainerNetDetails(1, new DockerNetworkIpamSpec("192.168.0.1", "", "")));
        DockerContainer dockerContainer2 = new DockerContainer();
        dockerContainer2.setNetworkDetails(new DockerContainerNetDetails(1, new DockerNetworkIpamSpec("192.168.0.2", "", "")));
        DockerContainer dockerContainer3 = new DockerContainer();
        dockerContainer3.setNetworkDetails(new DockerContainerNetDetails(1, new DockerNetworkIpamSpec("192.168.0.4", "", "")));
        return Arrays.asList(dockerContainer1, dockerContainer2, dockerContainer3);
    }

}
