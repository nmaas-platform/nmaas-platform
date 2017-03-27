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
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_INTERFACE_UNIT_KEY = "NMAAS_CUSTOMER_INTERFACE_UNIT";
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_INTERFACE_VLAN_KEY = "NMAAS_CUSTOMER_INTERFACE_VLAN";
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_BGP_LOCAL_IP_KEY = "NMAAS_CUSTOMER_BGP_LOCAL_IP";
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_BGP_LOCAL_CIDR_KEY = "NMAAS_CUSTOMER_BGP_LOCAL_CIDR";
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_POLICY_COMMUNITY_OPTIONS_KEY = "NMAAS_CUSTOMER_POLICY_COMMUNITY_OPTIONS";
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_POLICY_STATEMENT_CONNECTED_KEY = "NMAAS_CUSTOMER_POLICY_STATEMENT_CONNECTED";
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_POLICY_STATEMENT_IMPORT_KEY = "NMAAS_CUSTOMER_POLICY_STATEMENT_IMPORT";
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_POLICY_STATEMENT_EXPORT_KEY = "NMAAS_CUSTOMER_POLICY_STATEMENT_EXPORT";
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_SERVICE_ID_KEY = "NMAAS_CUSTOMER_SERVICE_ID";

    private static final String ANSIBLE_RUN_SCRIPT_NAME = "ansible-playbook";
    private static final String ANSIBLE_PLAYBOOK_DIR = "/ansible-playbook-dir";
    private static final String ANSIBLE_PLAYBOOK_NAME_FOR_CLIENT_SIDE_ROUTER = "pb-nmaas-vpn-asbr-config.yml";
    private static final String ANSIBLE_PLAYBOOK_NAME_FOR_CLOUD_SIDE_ROUTER = "pb-nmaas-vpn-iaas-config.yml";
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_LIMIT_KEY = "--limit";
    private static final String IS = "=";

    public static List<String> command(AnsiblePlaybookVpnConfig.Type type, AnsiblePlaybookVpnConfig vpn, String encodedPlaybookId) {
        if (type == null)
            return null;
        List<String> commands = new ArrayList<>();
        commands.add(ANSIBLE_RUN_SCRIPT_NAME);
        commands.add("-v");
        commands.add("-i");
        commands.add(ANSIBLE_PLAYBOOK_DIR + "/hosts");
        commands.add("-v");
        if (commandForCloudSideRouter(type))
            commands.add(ANSIBLE_PLAYBOOK_DIR + "/" + ANSIBLE_PLAYBOOK_NAME_FOR_CLOUD_SIDE_ROUTER);
        else
            commands.add(ANSIBLE_PLAYBOOK_DIR + "/" + ANSIBLE_PLAYBOOK_NAME_FOR_CLIENT_SIDE_ROUTER);
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
        commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_INTERFACE_UNIT_KEY + IS + vpn.getInterfaceUnit());
        commands.add("-e");
        commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_INTERFACE_VLAN_KEY + IS + vpn.getInterfaceVlan());
        commands.add("-e");
        commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_BGP_LOCAL_IP_KEY + IS + vpn.getBgpLocalIp());
        if (commandForCloudSideRouter(type)) {
            commands.add("-e");
            commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_POLICY_COMMUNITY_OPTIONS_KEY + IS + vpn.getPolicyCommunityOptions());
            commands.add("-e");
            commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_POLICY_STATEMENT_CONNECTED_KEY + IS + vpn.getPolicyStatementConnected());
            commands.add("-e");
            commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_POLICY_STATEMENT_IMPORT_KEY + IS + vpn.getPolicyStatementImport());
            commands.add("-e");
            commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_POLICY_STATEMENT_EXPORT_KEY + IS + vpn.getPolicyStatementExport());
        }
        commands.add("-e");
        commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_BGP_LOCAL_CIDR_KEY + IS + vpn.getBgpLocalCidr());

        commands.add("-e");
        commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_SERVICE_ID_KEY + IS + encodedPlaybookId);
        return commands;
    }

    private static boolean commandForCloudSideRouter(AnsiblePlaybookVpnConfig.Type type) {
        return type.equals(AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE);
    }

}
