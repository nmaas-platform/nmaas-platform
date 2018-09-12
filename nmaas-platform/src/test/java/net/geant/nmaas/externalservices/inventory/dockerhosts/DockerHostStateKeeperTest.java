package net.geant.nmaas.externalservices.inventory.dockerhosts;

import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostStateNotFoundException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerNetworkIpam;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test-compose.properties")
public class DockerHostStateKeeperTest {

    private static final String DOCKER_HOST_NAME_1 = "GN4-DOCKER-1";
    private static final String DOCKER_HOST_NAME_2 = "GN4-DOCKER-2";
    private static final String DOCKER_HOST_NAME_3 = "GN4-DOCKER-3";
    private static final Identifier DEPLOYMENT_ID_1 = Identifier.newInstance("deploymentId1");
    private static final Identifier DEPLOYMENT_ID_2 = Identifier.newInstance("deploymentId2");
    private static final Identifier DEPLOYMENT_ID_3 = Identifier.newInstance("deploymentId3");
    private static final String DOMAIN_1 = "domain1";
    private static final String DOMAIN_2 = "domain2";
    private static final String DOMAIN_3 = "domain3";

    @Autowired
    private DockerHostRepositoryManager dockerHostRepositoryManager;
    @Autowired
    private DockerHostStateKeeper dockerHostStateKeeper;

    @Before
    public void init() {
        DockerHostRepositoryInit.addDefaultDockerHost(dockerHostRepositoryManager);
    }

    @After
    public void clean() throws DockerHostStateNotFoundException {
        DockerHostRepositoryInit.removeDefaultDockerHost(dockerHostRepositoryManager);
        dockerHostStateKeeper.removeAllAssignments(DOCKER_HOST_NAME_1);
        dockerHostStateKeeper.removeAllAssignments(DOCKER_HOST_NAME_2);
    }

    @Test
    public void shouldAssignPorts() throws DockerHostNotFoundException, DockerHostStateNotFoundException {
        assertThat(dockerHostStateKeeper.assignPortForContainer(DOCKER_HOST_NAME_1, DEPLOYMENT_ID_1), equalTo(1000));
        assertThat(dockerHostStateKeeper.assignPortForContainer(DOCKER_HOST_NAME_1, DEPLOYMENT_ID_2), equalTo(1001));
        assertThat(dockerHostStateKeeper.getAssignedPort(DOCKER_HOST_NAME_1, DEPLOYMENT_ID_1), equalTo(1000));
        assertThat(dockerHostStateKeeper.assignPortForContainer(DOCKER_HOST_NAME_2, DEPLOYMENT_ID_3), equalTo(1000));
        dockerHostStateKeeper.removePortAssignment(DOCKER_HOST_NAME_1, DEPLOYMENT_ID_1);
        dockerHostStateKeeper.removePortAssignment(DOCKER_HOST_NAME_1, DEPLOYMENT_ID_1);
        assertThat(dockerHostStateKeeper.assignPortForContainer(DOCKER_HOST_NAME_1, DEPLOYMENT_ID_1), equalTo(1000));
        assertThat(dockerHostStateKeeper.getAssignedPort(DOCKER_HOST_NAME_2, DEPLOYMENT_ID_1), is(nullValue()));
    }

    @Test(expected = DockerHostStateNotFoundException.class)
    public void shouldThrowExceptionOnMissingStateWhenGettingPortAssignment() throws DockerHostStateNotFoundException {
        dockerHostStateKeeper.getAssignedPort(DOCKER_HOST_NAME_3, DEPLOYMENT_ID_1);
    }

    @Test(expected = DockerHostStateNotFoundException.class)
    public void shouldThrowExceptionOnMissingStateWhenRemovingPortAssignment() throws DockerHostStateNotFoundException {
        dockerHostStateKeeper.removePortAssignment(DOCKER_HOST_NAME_3, DEPLOYMENT_ID_1);
    }

    @Test
    public void shouldAssignVlan() throws DockerHostNotFoundException, DockerHostStateNotFoundException {
        assertThat(dockerHostStateKeeper.assignVlanForNetwork(DOCKER_HOST_NAME_1, DOMAIN_1), equalTo(500));
        assertThat(dockerHostStateKeeper.assignVlanForNetwork(DOCKER_HOST_NAME_1, DOMAIN_2), equalTo(501));
        assertThat(dockerHostStateKeeper.assignVlanForNetwork(DOCKER_HOST_NAME_1, DOMAIN_3), equalTo(502));
        dockerHostStateKeeper.removeVlanAssignment(DOCKER_HOST_NAME_1, DOMAIN_2);
        dockerHostStateKeeper.removeVlanAssignment(DOCKER_HOST_NAME_1, DOMAIN_2);
        assertThat(dockerHostStateKeeper.getAssignedVlan(DOCKER_HOST_NAME_1, DOMAIN_3), equalTo(502));
        assertThat(dockerHostStateKeeper.assignVlanForNetwork(DOCKER_HOST_NAME_1, DOMAIN_2), equalTo(501));
        assertThat(dockerHostStateKeeper.assignVlanForNetwork(DOCKER_HOST_NAME_2, DOMAIN_1), equalTo(500));
        assertThat(dockerHostStateKeeper.getAssignedVlan(DOCKER_HOST_NAME_2, DOMAIN_2), is(nullValue()));
    }

    @Test(expected = DockerHostStateNotFoundException.class)
    public void shouldThrowExceptionOnMissingStateWhenGettingVlanAssignment() throws DockerHostStateNotFoundException {
        dockerHostStateKeeper.getAssignedVlan(DOCKER_HOST_NAME_3, DOMAIN_1);
    }

    @Test(expected = DockerHostStateNotFoundException.class)
    public void shouldThrowExceptionOnMissingStateWhenRemovingVlanAssignment() throws DockerHostStateNotFoundException {
        dockerHostStateKeeper.removeVlanAssignment(DOCKER_HOST_NAME_3, DOMAIN_1);
    }

    @Test
    public void shouldAssignAddressPools() throws DockerHostNotFoundException, DockerHostStateNotFoundException {
        DockerNetworkIpam addressPool = dockerHostStateKeeper.assignAddressPoolForNetwork(DOCKER_HOST_NAME_1, DOMAIN_1);
        assertThat(addressPool.getIpRangeWithMask(), equalTo("10.11.1.0/24"));
        assertThat(addressPool.getSubnetWithMask(), equalTo(addressPool.getIpRangeWithMask()));
        assertThat(addressPool.getGateway(), equalTo("10.11.1.254"));
        assertThat(addressPool.getIpAddressOfContainer(), equalTo("10.11.1.1"));
        addressPool = dockerHostStateKeeper.assignAddressPoolForNetwork(DOCKER_HOST_NAME_1, DOMAIN_2);
        assertThat(addressPool.getIpRangeWithMask(), equalTo("10.11.2.0/24"));
        assertThat(addressPool.getSubnetWithMask(), equalTo(addressPool.getIpRangeWithMask()));
        assertThat(addressPool.getGateway(), equalTo("10.11.2.254"));
        assertThat(addressPool.getIpAddressOfContainer(), equalTo("10.11.2.1"));
        assertThat(dockerHostStateKeeper.getAssignedAddressPool(DOCKER_HOST_NAME_1, DOMAIN_1), is(notNullValue()));
        dockerHostStateKeeper.removeAddressPoolAssignment(DOCKER_HOST_NAME_1, DOMAIN_1);
        dockerHostStateKeeper.removeAddressPoolAssignment(DOCKER_HOST_NAME_1, DOMAIN_1);
        assertThat(dockerHostStateKeeper.getAssignedAddressPool(DOCKER_HOST_NAME_1, DOMAIN_1), is(nullValue()));
        addressPool = dockerHostStateKeeper.assignAddressPoolForNetwork(DOCKER_HOST_NAME_2, DOMAIN_3);
        assertThat(addressPool.getIpRangeWithMask(), equalTo("10.12.1.0/24"));
        assertThat(addressPool.getSubnetWithMask(), equalTo(addressPool.getIpRangeWithMask()));
        assertThat(addressPool.getGateway(), equalTo("10.12.1.254"));
        assertThat(addressPool.getIpAddressOfContainer(), equalTo("10.12.1.1"));
    }

    @Test(expected = DockerHostStateNotFoundException.class)
    public void shouldThrowExceptionOnMissingStateWhenGettingAddressAssignment() throws DockerHostStateNotFoundException {
        dockerHostStateKeeper.getAssignedAddressPool(DOCKER_HOST_NAME_3, DOMAIN_1);
    }

    @Test(expected = DockerHostStateNotFoundException.class)
    public void shouldThrowExceptionOnMissingStateWhenRemovingAddressAssignment() throws DockerHostStateNotFoundException {
        dockerHostStateKeeper.removeAddressPoolAssignment(DOCKER_HOST_NAME_3, DOMAIN_1);
    }

}
