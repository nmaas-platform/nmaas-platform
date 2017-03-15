package net.geant.nmaas.dcndeployment;

import net.geant.nmaas.dcn.deployment.AnsiblePlaybookCommandBuilder;
import net.geant.nmaas.dcn.deployment.AnsiblePlaybookVpnConfig;
import net.geant.nmaas.dcn.deployment.AnsiblePlaybookVpnConfigDefaults;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class AnsiblePlaybookCommandCreationTest {

    private static final String SERVICE_ID = "3vnhgwcn95ngcj5eogx";

    private static final String EXAMPLE_COMPLETE_ANSIBLE_PLAYBOOK_COMMAND =
            "ansible-playbook " +
                "-v -i /ansible-playbook-dir/hosts " +
                "-v /ansible-playbook-dir/pb-nmaas-vpn-client-test-config.yml " +
                "--limit=R4 " +
                "--extra-vars=" +
                    "\"NMAAS_CUSTOMER_VRF_ID=NMAAS-C-AS65538 " +
                    "NMAAS_CUSTOMER_LOGICAL_INTERFACE=ge-0/0/3.8 " +
                    "NMAAS_CUSTOMER_VRF_RD=182.16.4.4:8 " +
                    "NMAAS_CUSTOMER_VRF_RT=65525L:8 " +
                    "NMAAS_CUSTOMER_BGP_GROUP_ID=INET-VPN-NMAAS-C-65538 " +
                    "NMAAS_CUSTOMER_BGP_NEIGHBOR_IP=192.168.48.8 " +
                    "NMAAS_CUSTOMER_ASN=65538 " +
                    "NMAAS_CUSTOMER_PHYSICAL_INTERFACE=ge-0/0/3 " +
                    "NMAAS_CUSTOMER_ID=8 " +
                    "NMAAS_CUSTOMER_BGP_LOCAL_IP=192.168.48.4 " +
                    "NMAAS_CUSTOMER_BGP_LOCAL_CIDR=24 " +
                    "NMAAS_CUSTOMER_SERVICE_ID=" + SERVICE_ID + "\"";

    @Test
    public void shouldBuildCorrectCommand() {
        final List<String> commands = AnsiblePlaybookCommandBuilder.command(AnsiblePlaybookVpnConfigDefaults.ansiblePlaybookForClientSideRouter(), SERVICE_ID);
        for (String command : commands)
            assertThat(EXAMPLE_COMPLETE_ANSIBLE_PLAYBOOK_COMMAND, Matchers.stringContainsInOrder(Arrays.asList(command)));
    }

}
