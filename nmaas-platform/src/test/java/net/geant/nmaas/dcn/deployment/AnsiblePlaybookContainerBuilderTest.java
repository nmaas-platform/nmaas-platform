package net.geant.nmaas.dcn.deployment;

import com.spotify.docker.client.messages.ContainerConfig;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.ContainerNetworkDetails;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.ContainerNetworkIpamSpec;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class AnsiblePlaybookContainerBuilderTest {

    private static final String PLAIN_DCN_NAME = "3vnhgwcn95ngcj5eogx";

    private static final String ENCODED_PLAYBOOK_ID_FOR_CLIENT_SIDE_ROUTER = AnsiblePlaybookIdentifierConverter.encodeForClientSideRouter(PLAIN_DCN_NAME);

    private static final String ENCODED_PLAYBOOK_ID_FOR_CLOUD_SIDE_ROUTER = AnsiblePlaybookIdentifierConverter.encodeForCloudSideRouter(PLAIN_DCN_NAME);

    private static final String EXAMPLE_COMPLETE_PLAYBOOK_FOR_CLIENT_SIDE_ROUTER_DOCKER_RUN_COMMAND =
            "docker run " +
                    "--name=nmaas-ansible " +
                    "--volume=/home/docker/ansible-docker/ansible.cfg:/etc/ansible/ansible.cfg " +
                    "--volume=/home/docker/ansible-docker/pb-nmaas-vpn-asbr-config.yml:/ansible/pb-nmaas-vpn-asbr-config.yml " +
                    "--volume=/home/docker/ansible-docker/working-dir/config-set:/ansible-config-set " +
                    "--volume=/home/docker/ansible-docker/:/ansible-playbook-dir " +
                    "--volume=/home/docker/.ssh/id_rsa:/root/.ssh/id_rsa " +
                    "nmaas/ansible:2.3.0 " +
                    "ansible-playbook " +
                    "-v -i /ansible-playbook-dir/hosts " +
                    "-v /ansible-playbook-dir/pb-nmaas-vpn-asbr-config.yml " +
                    "--limit=R4 " +
                    "-e NMAAS_CUSTOMER_VRF_ID=NMAAS-C-AS64522 " +
                    "-e NMAAS_CUSTOMER_LOGICAL_INTERFACE=ge-0/0/4.144 " +
                    "-e NMAAS_CUSTOMER_VRF_RD=172.16.4.4:8 " +
                    "-e NMAAS_CUSTOMER_VRF_RT=64522L:8 " +
                    "-e NMAAS_CUSTOMER_BGP_GROUP_ID=INET-VPN-NMAAS-C-64522 " +
                    "-e NMAAS_CUSTOMER_BGP_NEIGHBOR_IP=192.168.144.14 " +
                    "-e NMAAS_CUSTOMER_ASN=64522 " +
                    "-e NMAAS_CUSTOMER_PHYSICAL_INTERFACE=ge-0/0/4 " +
                    "-e NMAAS_CUSTOMER_INTERFACE_UNIT=144 " +
                    "-e NMAAS_CUSTOMER_INTERFACE_VLAN=8 " +
                    "-e NMAAS_CUSTOMER_BGP_LOCAL_IP=192.168.144.4 " +
                    "-e NMAAS_CUSTOMER_BGP_LOCAL_CIDR=24 " +
                    "-e NMAAS_CUSTOMER_SERVICE_ID=" + ENCODED_PLAYBOOK_ID_FOR_CLIENT_SIDE_ROUTER;

    private static final String EXAMPLE_COMPLETE_PLAYBOOK_FOR_CLOUD_SIDE_ROUTER_DOCKER_RUN_COMMAND =
            "docker run " +
                    "--name=nmaas-ansible " +
                    "--volume=/home/docker/ansible-docker/ansible.cfg:/etc/ansible/ansible.cfg " +
                    "--volume=/home/docker/ansible-docker/pb-nmaas-vpn-iaas-config.yml:/ansible/pb-nmaas-vpn-iaas-config.yml " +
                    "--volume=/home/docker/ansible-docker/working-dir/config-set:/ansible-config-set " +
                    "--volume=/home/docker/ansible-docker/:/ansible-playbook-dir " +
                    "--volume=/home/docker/.ssh/id_rsa:/root/.ssh/id_rsa " +
                    "nmaas/ansible:2.3.0 " +
                    "ansible-playbook " +
                    "-v -i /ansible-playbook-dir/hosts " +
                    "-v /ansible-playbook-dir/pb-nmaas-vpn-iaas-config.yml " +
                    "--limit=R3 " +
                    "-e NMAAS_CUSTOMER_VRF_ID=NMAAS-C-AS64522 " +
                    "-e NMAAS_CUSTOMER_LOGICAL_INTERFACE=ge-0/0/4.239 " +
                    "-e NMAAS_CUSTOMER_VRF_RD=172.16.3.3:8 " +
                    "-e NMAAS_CUSTOMER_VRF_RT=64522L:8 " +
                    "-e NMAAS_CUSTOMER_BGP_GROUP_ID=INET-VPN-NMAAS-C-64522 " +
                    "-e NMAAS_CUSTOMER_BGP_NEIGHBOR_IP=192.168.239.9 " +
                    "-e NMAAS_CUSTOMER_ASN=64522 " +
                    "-e NMAAS_CUSTOMER_PHYSICAL_INTERFACE=ge-0/0/4 " +
                    "-e NMAAS_CUSTOMER_INTERFACE_UNIT=239 " +
                    "-e NMAAS_CUSTOMER_INTERFACE_VLAN=239 " +
                    "-e NMAAS_CUSTOMER_BGP_LOCAL_IP=192.168.239.3 " +
                    "-e NMAAS_CUSTOMER_BGP_LOCAL_CIDR=24 " +
                    "-e NMAAS_CUSTOMER_POLICY_COMMUNITY_OPTIONS=NMAAS-C-AS64522-COMMUNITY " +
                    "-e NMAAS_CUSTOMER_POLICY_STATEMENT_CONNECTED=NMAAS-C-AS64522-CONNECTED->OTHER " +
                    "-e NMAAS_CUSTOMER_POLICY_STATEMENT_IMPORT=NMAAS-C-AS64522-IMPORT " +
                    "-e NMAAS_CUSTOMER_POLICY_STATEMENT_EXPORT=NMAAS-C-AS64522-EXPORT " +
                    "-e NMAAS_CUSTOMER_SERVICE_ID=" + ENCODED_PLAYBOOK_ID_FOR_CLOUD_SIDE_ROUTER;

    @Autowired
    private AnsiblePlaybookContainerBuilder containerConfigBuilder;

    @Test
    public void shouldBuildAnsiblePlaybookContainerConfigForClientSideRouter() {
        ContainerConfig containerConfig = containerConfigBuilder.buildContainerForClientSideRouterConfig(
                AnsiblePlaybookVpnConfigDefaults.ansiblePlaybookForClientSideRouter(),
                ENCODED_PLAYBOOK_ID_FOR_CLIENT_SIDE_ROUTER);
        assertThat(EXAMPLE_COMPLETE_PLAYBOOK_FOR_CLIENT_SIDE_ROUTER_DOCKER_RUN_COMMAND, stringContainsInOrder(Arrays.asList(containerConfig.image())));
        for (String volumeEntry : containerConfig.hostConfig().binds())
            assertThat(EXAMPLE_COMPLETE_PLAYBOOK_FOR_CLIENT_SIDE_ROUTER_DOCKER_RUN_COMMAND, stringContainsInOrder(Arrays.asList(volumeEntry)));
        for (String commandEntry : containerConfig.cmd())
            assertThat(EXAMPLE_COMPLETE_PLAYBOOK_FOR_CLIENT_SIDE_ROUTER_DOCKER_RUN_COMMAND, stringContainsInOrder(Arrays.asList(commandEntry)));
    }

    @Test
    public void shouldBuildAnsiblePlaybookContainerConfigForCloudSideRouter() {
        ContainerConfig containerConfig = containerConfigBuilder.buildContainerForCloudSideRouterConfig(
                AnsiblePlaybookVpnConfigDefaults.ansiblePlaybookForCloudSideRouter(),
                ENCODED_PLAYBOOK_ID_FOR_CLOUD_SIDE_ROUTER);
        assertThat(EXAMPLE_COMPLETE_PLAYBOOK_FOR_CLOUD_SIDE_ROUTER_DOCKER_RUN_COMMAND, stringContainsInOrder(Arrays.asList(containerConfig.image())));
        for (String volumeEntry : containerConfig.hostConfig().binds())
            assertThat(EXAMPLE_COMPLETE_PLAYBOOK_FOR_CLOUD_SIDE_ROUTER_DOCKER_RUN_COMMAND, stringContainsInOrder(Arrays.asList(volumeEntry)));
        for (String commandEntry : containerConfig.cmd())
            assertThat(EXAMPLE_COMPLETE_PLAYBOOK_FOR_CLOUD_SIDE_ROUTER_DOCKER_RUN_COMMAND, stringContainsInOrder(Arrays.asList(commandEntry)));
    }

    @Test
    public void shouldMergeConfigWithProvidedNetworkDetails() {
        AnsiblePlaybookVpnConfig config = AnsiblePlaybookVpnConfigDefaults.ansiblePlaybookForCloudSideRouter();
        ContainerNetworkDetails networkDetails = new ContainerNetworkDetails(
                1024,
                new ContainerNetworkIpamSpec("10.11.1.0/24", "10.11.1.254"),
                123);
        config.merge(networkDetails);
        assertThat(config.getInterfaceUnit(), equalTo("123"));
        assertThat(config.getInterfaceVlan(), equalTo("123"));
        assertThat(config.getLogicalInterface(), equalTo("ge-0/0/4.123"));
        assertThat(config.getBgpLocalIp(), equalTo("10.11.1.254"));
        assertThat(config.getBgpNeighborIp(), equalTo("10.11.1.1"));
    }

}