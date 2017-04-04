package net.geant.nmaas.externalservices.inventory.dockerhosts;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.ContainerNetworkIpamSpec;
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
    private static final String SERVICE_NAME_1 = "service1";
    private static final String SERVICE_NAME_2 = "service2";
    private static final String SERVICE_NAME_3 = "service3";

    @Autowired
    private DockerHostStateKeeper stateKeeper;

    @Test
    public void shouldAssignPorts() throws DockerHostNotFoundException, DockerHostState.MappingNotFoundException, DockerHostInvalidException {
        int assignedPort = stateKeeper.assignPort(DOCKER_HOST_NAME_1, SERVICE_NAME_1);
        assertThat(assignedPort, equalTo(1000));
        assignedPort = stateKeeper.assignPort(DOCKER_HOST_NAME_1, SERVICE_NAME_2);
        assertThat(assignedPort, equalTo(1001));
        assignedPort = stateKeeper.getAssignedPort(DOCKER_HOST_NAME_1, SERVICE_NAME_1);
        assertThat(assignedPort, equalTo(1000));
        assignedPort = stateKeeper.assignPort(DOCKER_HOST_NAME_2, SERVICE_NAME_3);
        assertThat(assignedPort, equalTo(1000));
    }

    @Test
    public void shouldAssignAddressPools() throws DockerHostNotFoundException, DockerHostInvalidException {
        ContainerNetworkIpamSpec addressPool = stateKeeper.assignAddressPool(DOCKER_HOST_NAME_1, SERVICE_NAME_1);
        assertThat(addressPool.getIpRangeWithMask(), equalTo("10.11.1.0/24"));
        assertThat(addressPool.getSubnetWithMask(), equalTo(addressPool.getIpRangeWithMask()));
        assertThat(addressPool.getGateway(), equalTo("10.11.1.254"));
        assertThat(addressPool.getIpAddressOfContainer(), equalTo("10.11.1.1"));
        addressPool = stateKeeper.assignAddressPool(DOCKER_HOST_NAME_1, SERVICE_NAME_2);
        assertThat(addressPool.getIpRangeWithMask(), equalTo("10.11.2.0/24"));
        assertThat(addressPool.getSubnetWithMask(), equalTo(addressPool.getIpRangeWithMask()));
        assertThat(addressPool.getGateway(), equalTo("10.11.2.254"));
        assertThat(addressPool.getIpAddressOfContainer(), equalTo("10.11.2.1"));
        addressPool = stateKeeper.assignAddressPool(DOCKER_HOST_NAME_2, SERVICE_NAME_3);
        assertThat(addressPool.getIpRangeWithMask(), equalTo("10.12.1.0/24"));
        assertThat(addressPool.getSubnetWithMask(), equalTo(addressPool.getIpRangeWithMask()));
        assertThat(addressPool.getGateway(), equalTo("10.12.1.254"));
        assertThat(addressPool.getIpAddressOfContainer(), equalTo("10.12.1.1"));
    }

}
