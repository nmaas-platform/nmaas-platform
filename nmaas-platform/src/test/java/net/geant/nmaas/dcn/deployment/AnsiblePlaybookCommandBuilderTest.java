package net.geant.nmaas.dcn.deployment;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class AnsiblePlaybookCommandBuilderTest {

    private static final String PLAIN_DCN_NAME = "3vnhgwcn95ngcj5eogx";

    private static final String ENCODED_PLAYBOOK_ID_FOR_CLIENT_SIDE_ROUTER = AnsiblePlaybookIdentifierConverter.encodeForClientSideRouter(PLAIN_DCN_NAME);

    private static final String ENCODED_PLAYBOOK_ID_FOR_CLOUD_SIDE_ROUTER = AnsiblePlaybookIdentifierConverter.encodeForCloudSideRouter(PLAIN_DCN_NAME);

    private static final String EXAMPLE_COMPLETE_ANSIBLE_PLAYBOOK_COMMAND_FOR_CLIENT_SIDE_ROUTER_CONFIG =
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

    private static final String EXAMPLE_COMPLETE_ANSIBLE_PLAYBOOK_COMMAND_FOR_CLIENT_SIDE_ROUTER_CONFIG_REMOVAL =
            "ansible-playbook " +
                    "-v -i /ansible-playbook-dir/hosts " +
                    "-v /ansible-playbook-dir/pb-nmaas-vpn-asbr-delete.yml " +
                    "--limit=R4 " +
                    "-e NMAAS_CUSTOMER_VRF_ID=NMAAS-C-AS64522 " +
                    "-e NMAAS_CUSTOMER_PHYSICAL_INTERFACE=ge-0/0/4 " +
                    "-e NMAAS_CUSTOMER_INTERFACE_UNIT=144 " +
                    "-e NMAAS_CUSTOMER_SERVICE_ID=" + ENCODED_PLAYBOOK_ID_FOR_CLIENT_SIDE_ROUTER;

    private static final String EXAMPLE_COMPLETE_ANSIBLE_PLAYBOOK_COMMAND_FOR_CLOUD_SIDE_ROUTER_CONFIG =
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

    private static final String EXAMPLE_COMPLETE_ANSIBLE_PLAYBOOK_COMMAND_FOR_CLOUD_SIDE_ROUTER_CONFIG_REMOVAL =
            "ansible-playbook " +
                    "-v -i /ansible-playbook-dir/hosts " +
                    "-v /ansible-playbook-dir/pb-nmaas-vpn-iaas-delete.yml " +
                    "--limit=R3 " +
                    "-e NMAAS_CUSTOMER_VRF_ID=NMAAS-C-AS64522 " +
                    "-e NMAAS_CUSTOMER_PHYSICAL_INTERFACE=ge-0/0/4 " +
                    "-e NMAAS_CUSTOMER_INTERFACE_UNIT=239 " +
                    "-e NMAAS_CUSTOMER_POLICY_COMMUNITY_OPTIONS=NMAAS-C-AS64522-COMMUNITY " +
                    "-e NMAAS_CUSTOMER_POLICY_STATEMENT_CONNECTED=NMAAS-C-AS64522-CONNECTED->OTHER " +
                    "-e NMAAS_CUSTOMER_POLICY_STATEMENT_IMPORT=NMAAS-C-AS64522-IMPORT " +
                    "-e NMAAS_CUSTOMER_POLICY_STATEMENT_EXPORT=NMAAS-C-AS64522-EXPORT " +
                    "-e NMAAS_CUSTOMER_SERVICE_ID=" + ENCODED_PLAYBOOK_ID_FOR_CLOUD_SIDE_ROUTER;

    @Test
    public void shouldBuildCorrectAnsibleCommandForClientSideRouterConfig() {
        final List<String> commands = AnsiblePlaybookCommandBuilder.command(
                AnsiblePlaybookVpnConfig.Action.ADD,
                AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE,
                AnsiblePlaybookVpnConfigDefaults.ansiblePlaybookForClientSideRouter(),
                ENCODED_PLAYBOOK_ID_FOR_CLIENT_SIDE_ROUTER);
        for (String command : commands)
            assertThat(EXAMPLE_COMPLETE_ANSIBLE_PLAYBOOK_COMMAND_FOR_CLIENT_SIDE_ROUTER_CONFIG, Matchers.stringContainsInOrder(Arrays.asList(command)));
    }

    @Test
    public void shouldBuildCorrectAnsibleCommandForClientSideRouterConfigRemoval() {
        final List<String> commands = AnsiblePlaybookCommandBuilder.command(
                AnsiblePlaybookVpnConfig.Action.REMOVE,
                AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE,
                AnsiblePlaybookVpnConfigDefaults.ansiblePlaybookForClientSideRouter(),
                ENCODED_PLAYBOOK_ID_FOR_CLIENT_SIDE_ROUTER);
        for (String command : commands)
            assertThat(EXAMPLE_COMPLETE_ANSIBLE_PLAYBOOK_COMMAND_FOR_CLIENT_SIDE_ROUTER_CONFIG_REMOVAL, Matchers.stringContainsInOrder(Arrays.asList(command)));
    }

    @Test
    public void shouldBuildCorrectAnsibleCommandForCloudSideRouterConfig() {
        final List<String> commands = AnsiblePlaybookCommandBuilder.command(
                AnsiblePlaybookVpnConfig.Action.ADD,
                AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE,
                AnsiblePlaybookVpnConfigDefaults.ansiblePlaybookForCloudSideRouter(),
                ENCODED_PLAYBOOK_ID_FOR_CLOUD_SIDE_ROUTER);
        for (String command : commands)
            assertThat(EXAMPLE_COMPLETE_ANSIBLE_PLAYBOOK_COMMAND_FOR_CLOUD_SIDE_ROUTER_CONFIG, Matchers.stringContainsInOrder(Arrays.asList(command)));
    }

    @Test
    public void shouldBuildCorrectAnsibleCommandForCloudSideRouterConfigRemoval() {
        final List<String> commands = AnsiblePlaybookCommandBuilder.command(
                AnsiblePlaybookVpnConfig.Action.REMOVE,
                AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE,
                AnsiblePlaybookVpnConfigDefaults.ansiblePlaybookForCloudSideRouter(),
                ENCODED_PLAYBOOK_ID_FOR_CLOUD_SIDE_ROUTER);
        for (String command : commands)
            assertThat(EXAMPLE_COMPLETE_ANSIBLE_PLAYBOOK_COMMAND_FOR_CLOUD_SIDE_ROUTER_CONFIG_REMOVAL, Matchers.stringContainsInOrder(Arrays.asList(command)));
    }

}