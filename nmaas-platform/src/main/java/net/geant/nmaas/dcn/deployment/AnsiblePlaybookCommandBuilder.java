package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.entities.AnsiblePlaybookVpnConfig;

import java.util.ArrayList;
import java.util.List;

import static net.geant.nmaas.dcn.deployment.entities.AnsiblePlaybookVpnConfig.Action;
import static net.geant.nmaas.dcn.deployment.entities.AnsiblePlaybookVpnConfig.Type;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
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
    private static final String ANSIBLE_PLAYBOOK_NAME_FOR_CLIENT_SIDE_ROUTER_CONFIG = "pb-nmaas-vpn-asbr-config.yml";
    private static final String ANSIBLE_PLAYBOOK_NAME_FOR_CLOUD_SIDE_ROUTER_CONFIG = "pb-nmaas-vpn-iaas-config.yml";
    private static final String ANSIBLE_PLAYBOOK_NAME_FOR_CLIENT_SIDE_ROUTER_CONFIG_REMOVAL = "pb-nmaas-vpn-asbr-delete.yml";
    private static final String ANSIBLE_PLAYBOOK_NAME_FOR_CLOUD_SIDE_ROUTER_CONFIG_REMOVAL = "pb-nmaas-vpn-iaas-delete.yml";
    private static final String ANSIBLE_RUN_SCRIPT_PARAM_LIMIT_KEY = "--limit";
    private static final String IS = "=";

    public static List<String> command(Action action, Type type, AnsiblePlaybookVpnConfig vpn, String encodedPlaybookId) {
        if (action == null || type == null)
            return null;
        List<String> commands = new ArrayList<>();
        commands.add(ANSIBLE_RUN_SCRIPT_NAME);
        commands.add("-v");
        commands.add("-i");
        commands.add(ANSIBLE_PLAYBOOK_DIR + "/hosts");
        commands.add("-v");
        if (commandForRouterConfiguration(action)) {
            if (commandForCloudSideRouter(type))
                commands.add(ANSIBLE_PLAYBOOK_DIR + "/" + ANSIBLE_PLAYBOOK_NAME_FOR_CLOUD_SIDE_ROUTER_CONFIG);
            else
                commands.add(ANSIBLE_PLAYBOOK_DIR + "/" + ANSIBLE_PLAYBOOK_NAME_FOR_CLIENT_SIDE_ROUTER_CONFIG);
        } else {
            if (commandForCloudSideRouter(type))
                commands.add(ANSIBLE_PLAYBOOK_DIR + "/" + ANSIBLE_PLAYBOOK_NAME_FOR_CLOUD_SIDE_ROUTER_CONFIG_REMOVAL);
            else
                commands.add(ANSIBLE_PLAYBOOK_DIR + "/" + ANSIBLE_PLAYBOOK_NAME_FOR_CLIENT_SIDE_ROUTER_CONFIG_REMOVAL);
        }
        commands.add(ANSIBLE_RUN_SCRIPT_PARAM_LIMIT_KEY + IS + vpn.getTargetRouter());
        commands.add("-e");
        commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_VRF_ID_KEY + IS + vpn.getVrfId());
        if (commandForRouterConfiguration(action)) {
            commands.add("-e");
            commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_LOGICAL_INTERFACE_KEY + IS + vpn.getLogicalInterface());
        }
        if (commandForRouterConfiguration(action)) {
            commands.add("-e");
            commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_VRF_RD_KEY + IS + vpn.getVrfRd());
        }
        if (commandForRouterConfiguration(action)) {
            commands.add("-e");
            commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_VRF_RT_KEY + IS + vpn.getVrfRt());
        }
        if (commandForRouterConfiguration(action)) {
            commands.add("-e");
            commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_BGP_GROUP_ID_KEY + IS + vpn.getBgpGroupId());
        }
        if (commandForRouterConfiguration(action)) {
            commands.add("-e");
            commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_BGP_NEIGHBOR_IP_KEY + IS + vpn.getBgpNeighborIp());
        }
        if (commandForRouterConfiguration(action)) {
            commands.add("-e");
            commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_ASN_KEY + IS + vpn.getAsn());
        }
        commands.add("-e");
        commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_PHYSICAL_INTERFACE_KEY + IS + vpn.getPhysicalInterface());
        commands.add("-e");
        commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_INTERFACE_UNIT_KEY + IS + vpn.getInterfaceUnit());
        if (commandForRouterConfiguration(action)) {
            commands.add("-e");
            commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_INTERFACE_VLAN_KEY + IS + vpn.getInterfaceVlan());
        }
        if (commandForRouterConfiguration(action)) {
            commands.add("-e");
            commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_BGP_LOCAL_IP_KEY + IS + vpn.getBgpLocalIp());
        }
        if (commandForRouterConfiguration(action)) {
            commands.add("-e");
            commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_BGP_LOCAL_CIDR_KEY + IS + vpn.getBgpLocalCidr());
        }
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
        commands.add(ANSIBLE_RUN_SCRIPT_PARAM_EXTRA_VARS_NMAAS_CUSTOMER_SERVICE_ID_KEY + IS + encodedPlaybookId);
        return commands;
    }

    private static boolean commandForRouterConfiguration(Action action) {
        return action.equals(Action.ADD);
    }

    private static boolean commandForCloudSideRouter(Type type) {
        return type.equals(Type.CLOUD_SIDE);
    }

}
