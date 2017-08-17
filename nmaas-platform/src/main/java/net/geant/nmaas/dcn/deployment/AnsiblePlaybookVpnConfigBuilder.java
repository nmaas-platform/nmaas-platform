package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.entities.AnsiblePlaybookVpnConfig;
import net.geant.nmaas.externalservices.inventory.network.CloudAttachPoint;
import net.geant.nmaas.externalservices.inventory.network.CustomerNetworkAttachPoint;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class AnsiblePlaybookVpnConfigBuilder {

    private static final String COMMON_ID_PREFIX = "NMAAS-C-AS";
    private static final String BGP_GROUP_ID_PREFIX = "INET-VPN-NMAAS-C-";
    private static final String DEFAULT_BGP_CIDR = "24";

    static AnsiblePlaybookVpnConfig fromCloudAttachPoint(AnsiblePlaybookVpnConfig customerRouterConfig, CloudAttachPoint cloudAttachPoint) {
        AnsiblePlaybookVpnConfig playbookVpnConfig = new AnsiblePlaybookVpnConfig();
        playbookVpnConfig.setType(AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE);
        playbookVpnConfig.setTargetRouter(cloudAttachPoint.getRouterName());
        playbookVpnConfig.setTargetRouterId(cloudAttachPoint.getRouterId());
        playbookVpnConfig.setPhysicalInterface(cloudAttachPoint.getRouterInterfaceName());
        playbookVpnConfig.setAsn(customerRouterConfig.getAsn());
        playbookVpnConfig.setVrfId(customerRouterConfig.getVrfId());
        playbookVpnConfig.setVrfRd(cloudVrfRd(cloudAttachPoint.getRouterId(), customerRouterConfig.getInterfaceVlan()));
        playbookVpnConfig.setVrfRt(customerRouterConfig.getVrfRt());
        playbookVpnConfig.setBgpLocalCidr(DEFAULT_BGP_CIDR);
        playbookVpnConfig.setBgpGroupId(customerRouterConfig.getBgpGroupId());
        playbookVpnConfig.setPolicyCommunityOptions(cloudPolicyCommunityOptions(customerRouterConfig.getAsn()));
        playbookVpnConfig.setPolicyStatementConnected(cloudPolicyStatementConnected(customerRouterConfig.getAsn()));
        playbookVpnConfig.setPolicyStatementImport(cloudPolicyStatementImport(customerRouterConfig.getAsn()));
        playbookVpnConfig.setPolicyStatementExport(cloudPolicyStatementExport(customerRouterConfig.getAsn()));
        return playbookVpnConfig;
    }

    private static String cloudVrfRd(String routerId, String routerInterfaceVlan) {
        return routerId + ":" + routerInterfaceVlan;
    }

    private static String cloudPolicyCommunityOptions(String asNumber) {
        return COMMON_ID_PREFIX + asNumber + "-COMMUNITY";
    }

    private static String cloudPolicyStatementConnected(String asNumber) {
        return COMMON_ID_PREFIX + asNumber + "-CONNECTED->OTHER";
    }

    private static String cloudPolicyStatementImport(String asNumber) {
        return COMMON_ID_PREFIX + asNumber + "-IMPORT";
    }

    private static String cloudPolicyStatementExport(String asNumber) {
        return COMMON_ID_PREFIX + asNumber + "-EXPORT";
    }

    static AnsiblePlaybookVpnConfig fromCustomerNetworkAttachPoint(CustomerNetworkAttachPoint customerNetworkAttachPoint) {
        AnsiblePlaybookVpnConfig playbookVpnConfig = new AnsiblePlaybookVpnConfig();
        playbookVpnConfig.setType(AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE);
        playbookVpnConfig.setTargetRouter(customerNetworkAttachPoint.getRouterName());
        playbookVpnConfig.setTargetRouterId(customerNetworkAttachPoint.getRouterId());
        playbookVpnConfig.setPhysicalInterface(customerNetworkAttachPoint.getRouterInterfaceName());
        playbookVpnConfig.setInterfaceUnit(customerNetworkAttachPoint.getRouterInterfaceUnit());
        playbookVpnConfig.setInterfaceVlan(customerNetworkAttachPoint.getRouterInterfaceVlan());
        playbookVpnConfig.setLogicalInterface(customerLogicalInterface(customerNetworkAttachPoint.getRouterInterfaceName(), customerNetworkAttachPoint.getRouterInterfaceUnit()));
        playbookVpnConfig.setBgpLocalIp(customerNetworkAttachPoint.getBgpLocalIp());
        playbookVpnConfig.setBgpNeighborIp(customerNetworkAttachPoint.getBgpNeighborIp());
        playbookVpnConfig.setBgpLocalCidr(DEFAULT_BGP_CIDR);
        playbookVpnConfig.setAsn(customerNetworkAttachPoint.getAsNumber());
        playbookVpnConfig.setVrfId(customerVrfId(customerNetworkAttachPoint.getAsNumber()));
        playbookVpnConfig.setVrfRd(customerVrfRd(customerNetworkAttachPoint.getRouterId(), customerNetworkAttachPoint.getRouterInterfaceVlan()));
        playbookVpnConfig.setVrfRt(customerVrfRt(customerNetworkAttachPoint.getAsNumber(), customerNetworkAttachPoint.getRouterInterfaceVlan()));
        playbookVpnConfig.setBgpGroupId(customerBgpGroupId(customerNetworkAttachPoint.getAsNumber()));
        return playbookVpnConfig;
    }

    private static String customerLogicalInterface(String routerInterfaceName, String routerInterfaceUnit) {
        return routerInterfaceName + "." + routerInterfaceUnit;
    }

    private static String customerVrfId(String asNumber) {
        return COMMON_ID_PREFIX + asNumber;
    }

    private static String customerVrfRd(String routerId, String routerInterfaceVlan) {
        return routerId + ":" + routerInterfaceVlan;
    }

    private static String customerVrfRt(String asNumber, String routerInterfaceVlan) {
        return asNumber + "L:" + routerInterfaceVlan;
    }

    private static String customerBgpGroupId(String asNumber) {
        return BGP_GROUP_ID_PREFIX + asNumber;
    }

}
