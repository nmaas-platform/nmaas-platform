package net.geant.nmaas.externalservices.inventory.dockerhosts;

import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostInvalidException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainer;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNetwork;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerNetworkIpamSpec;
import org.junit.After;
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
public class DockerHostStateKeeperTest {

    private static final String DOCKER_HOST_NAME_1 = "GN4-DOCKER-1";
    private static final String DOCKER_HOST_NAME_2 = "GN4-DOCKER-2";
    private static final DockerContainer CONTAINER_1 = new DockerContainer();
    private static final DockerContainer CONTAINER_2 = new DockerContainer();
    private static final DockerContainer CONTAINER_3 = new DockerContainer();
    private static final DockerNetwork NETWORK_1 = new DockerNetwork();
    private static final DockerNetwork NETWORK_2 = new DockerNetwork();
    private static final DockerNetwork NETWORK_3 = new DockerNetwork();

    @Autowired
    private DockerHostRepositoryManager dockerHostRepositoryManager;

    @Autowired
    private DockerHostStateKeeper dockerHostStateKeeper;

    @Before
    public void init() {
        DockerHostRepositoryInit.addDefaultDockerHost(dockerHostRepositoryManager);
    }

    @After
    public void clean() {
        DockerHostRepositoryInit.removeDefaultDockerHost(dockerHostRepositoryManager);
    }

    @Test
    public void shouldAssignPorts() throws DockerHostNotFoundException, DockerHostState.MappingNotFoundException, DockerHostInvalidException {
        int assignedPort = dockerHostStateKeeper.assignPortForContainer(DOCKER_HOST_NAME_1, CONTAINER_1);
        assertThat(assignedPort, equalTo(1000));
        assignedPort = dockerHostStateKeeper.assignPortForContainer(DOCKER_HOST_NAME_1, CONTAINER_2);
        assertThat(assignedPort, equalTo(1001));
        assignedPort = dockerHostStateKeeper.getAssignedPort(DOCKER_HOST_NAME_1, CONTAINER_1);
        assertThat(assignedPort, equalTo(1000));
        assignedPort = dockerHostStateKeeper.assignPortForContainer(DOCKER_HOST_NAME_2, CONTAINER_3);
        assertThat(assignedPort, equalTo(1000));
    }

    @Test
    public void shouldAssignAddressPools() throws DockerHostNotFoundException, DockerHostInvalidException {
        DockerNetworkIpamSpec addressPool = dockerHostStateKeeper.assignAddressPoolForNetwork(DOCKER_HOST_NAME_1, NETWORK_1);
        assertThat(addressPool.getIpRangeWithMask(), equalTo("10.11.1.0/24"));
        assertThat(addressPool.getSubnetWithMask(), equalTo(addressPool.getIpRangeWithMask()));
        assertThat(addressPool.getGateway(), equalTo("10.11.1.254"));
        assertThat(addressPool.getIpAddressOfContainer(), equalTo("10.11.1.1"));
        addressPool = dockerHostStateKeeper.assignAddressPoolForNetwork(DOCKER_HOST_NAME_1, NETWORK_2);
        assertThat(addressPool.getIpRangeWithMask(), equalTo("10.11.2.0/24"));
        assertThat(addressPool.getSubnetWithMask(), equalTo(addressPool.getIpRangeWithMask()));
        assertThat(addressPool.getGateway(), equalTo("10.11.2.254"));
        assertThat(addressPool.getIpAddressOfContainer(), equalTo("10.11.2.1"));
        addressPool = dockerHostStateKeeper.assignAddressPoolForNetwork(DOCKER_HOST_NAME_2, NETWORK_3);
        assertThat(addressPool.getIpRangeWithMask(), equalTo("10.12.1.0/24"));
        assertThat(addressPool.getSubnetWithMask(), equalTo(addressPool.getIpRangeWithMask()));
        assertThat(addressPool.getGateway(), equalTo("10.12.1.254"));
        assertThat(addressPool.getIpAddressOfContainer(), equalTo("10.12.1.1"));
    }

}
