package net.geant.nmaas.dcn.deployment;

import java.util.ArrayList;
import java.util.List;

public class AnsiblePlaybookCommandBuilder {

    private static final String ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_VRF_ID_KEY = "NMAAS_CUSTOMER_VRF_ID";
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_LOGICAL_INTERFACE_KEY = "NMAAS_CUSTOMER_LOGICAL_INTERFACE";
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_VRF_RD_KEY = "NMAAS_CUSTOMER_VRF_RD";
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_VRF_RT_KEY = "NMAAS_CUSTOMER_VRF_RT";
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_BGP_GROUP_ID_KEY = "NMAAS_CUSTOMER_BGP_GROUP_ID";
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_BGP_NEIGHBOR_IP_KEY = "NMAAS_CUSTOMER_BGP_NEIGHBOR_IP";
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_ASN_KEY = "NMAAS_CUSTOMER_ASN";
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_PHYSICAL_INTERFACE_KEY = "NMAAS_CUSTOMER_PHYSICAL_INTERFACE";
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_ID_KEY = "NMAAS_CUSTOMER_ID";
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_BGP_LOCAL_IP_KEY = "NMAAS_CUSTOMER_BGP_LOCAL_IP";
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_BGP_LOCAL_CIDR_KEY = "NMAAS_CUSTOMER_BGP_LOCAL_CIDR";
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_SERVICE_ID_KEY = "NMAAS_CUSTOMER_SERVICE_ID";

    private static final String ANSIBLE_RUN_SCRIPT_NAME = "ansible-playbook";
    private static final String ANSIBLE_PLAYBOOK_DIR = "/ansible-playbook-dir";
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_HOSTS = "-v -i " + ANSIBLE_PLAYBOOK_DIR + "/hosts";
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_PLAYBOOK = "-v " + ANSIBLE_PLAYBOOK_DIR + "/pb-nmaas-vpn-client-test-config.yml";
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_LIMIT_KEY = "--limit";
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_KEY = "--extra-vars";
    private static final String SPACE = " ";
    private static final String IS = "=";
    private static final String QUOTE = "\"";

    public static List<String> command(AnsiblePlaybookVpnConfig vpn, String serviceId) {
        List<String> commands = new ArrayList<>();
        commands.add(ANSIBLE_RUN_SCRIPT_NAME);
        commands.add("-v");
        commands.add("-i");
        commands.add(ANSIBLE_PLAYBOOK_DIR + "/hosts");
        commands.add("-v");
        commands.add(ANSIBLE_PLAYBOOK_DIR + "/pb-nmaas-vpn-client-test-config.yml");
        commands.add(ANSIBLE_RUN_SCRIPT_PARAM_LIMIT_KEY + IS + vpn.getTargetRouter());
        commands.add("-e");
        commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_VRF_ID_KEY + IS + vpn.getVrfId());
        commands.add("-e");
        commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_LOGICAL_INTERFACE_KEY + IS + vpn.getLogicalInterface());
        commands.add("-e");
        commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_VRF_RD_KEY + IS + vpn.getVrfRd());
        commands.add("-e");
        commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_VRF_RT_KEY + IS + vpn.getVrfRt());
        commands.add("-e");
        commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_BGP_GROUP_ID_KEY + IS + vpn.getBgpGroupId());
        commands.add("-e");
        commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_BGP_NEIGHBOR_IP_KEY + IS + vpn.getBgpNeighborIp());
        commands.add("-e");
        commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_ASN_KEY + IS + vpn.getAsn());
        commands.add("-e");
        commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_PHYSICAL_INTERFACE_KEY + IS + vpn.getPhysicalInterface());
        commands.add("-e");
        commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_ID_KEY + IS + vpn.getId());
        commands.add("-e");
        commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_BGP_LOCAL_IP_KEY + IS + vpn.getBgpLocalIp());
        commands.add("-e");
        commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_BGP_LOCAL_CIDR_KEY + IS + vpn.getBgpLocalCidr());
        commands.add("-e");
        commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_SERVICE_ID_KEY + IS + serviceId);
        return commands;
    }

}
