package net.geant.nmaas.externalservices.inventory.dockerhosts;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.ContainerNetworkIpamSpec;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

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
    public void shouldAssignPorts() throws DockerHostNotFoundException, DockerHostState.MappingNotFoundException {
        List<Integer> assignedPorts = stateKeeper.assignPorts(DOCKER_HOST_NAME_1, 2, SERVICE_NAME_1);
        System.out.println(assignedPorts);
        assertThat(assignedPorts.size(), equalTo(2));
        assertThat(assignedPorts, hasItem(1000));
        assertThat(assignedPorts, hasItem(1001));
        assignedPorts = stateKeeper.assignPorts(DOCKER_HOST_NAME_1, 1, SERVICE_NAME_2);
        System.out.println(assignedPorts);
        assertThat(assignedPorts, hasItem(1002));
        assignedPorts = stateKeeper.getAssignedPorts(DOCKER_HOST_NAME_1, SERVICE_NAME_1);
        assertThat(assignedPorts.size(), equalTo(2));
        assertThat(assignedPorts, hasItem(1000));
        assertThat(assignedPorts, hasItem(1001));

        assignedPorts = stateKeeper.assignPorts(DOCKER_HOST_NAME_2, 3, SERVICE_NAME_3);
        System.out.println(assignedPorts);
        assertThat(assignedPorts.size(), equalTo(3));
        assertThat(assignedPorts, hasItem(1000));
        assertThat(assignedPorts, hasItem(1001));
        assertThat(assignedPorts, hasItem(1002));
    }

    @Test
    public void shouldAssignAddressPools() throws DockerHostNotFoundException {
        ContainerNetworkIpamSpec addressPool = stateKeeper.assignAddressPool(DOCKER_HOST_NAME_1, SERVICE_NAME_1);
        assertThat(addressPool.getIpRangeWithMask(), equalTo("10.11.1.0/24"));
        assertThat(addressPool.getSubnetWithMask(), equalTo(addressPool.getIpRangeWithMask()));
        assertThat(addressPool.getGateway(), equalTo("10.11.1.254"));
        addressPool = stateKeeper.assignAddressPool(DOCKER_HOST_NAME_1, SERVICE_NAME_2);
        assertThat(addressPool.getIpRangeWithMask(), equalTo("10.11.2.0/24"));
        assertThat(addressPool.getSubnetWithMask(), equalTo(addressPool.getIpRangeWithMask()));
        assertThat(addressPool.getGateway(), equalTo("10.11.2.254"));
        addressPool = stateKeeper.assignAddressPool(DOCKER_HOST_NAME_2, SERVICE_NAME_3);
        assertThat(addressPool.getIpRangeWithMask(), equalTo("10.12.1.0/24"));
        assertThat(addressPool.getSubnetWithMask(), equalTo(addressPool.getIpRangeWithMask()));
        assertThat(addressPool.getGateway(), equalTo("10.12.1.254"));
    }

}
