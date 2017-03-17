package net.geant.nmaas.dcn.deployment;

public class AnsiblePlaybookVpnConfigDefaults {

    private static final String DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER = "R4";
    private static final String DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_VRF_ID = "NMAAS-C-AS64522";
    private static final String DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_LOGICAL_INTERFACE = "ge-0/0/4.144";
    private static final String DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_VRF_RD = "172.16.4.4:8";
    private static final String DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_VRF_RT = "64522L:8";
    private static final String DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_BGP_GROUP_ID = "INET-VPN-NMAAS-C-64522";
    private static final String DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_BGP_NEIGHBOR_IP = "192.168.144.14";
    private static final String DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_ASN = "64522";
    private static final String DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_PHYSICAL_INTERFACE = "ge-0/0/4";
    private static final String DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_INTERFACE_UNIT ="144";
    private static final String DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_INTERFACE_VLAN = "8";
    private static final String DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_BGP_LOCAL_IP = "192.168.144.4";
    private static final String DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_BGP_LOCAL_CIDR = "24";

//  limit=R4
//  NMAAS_CUSTOMER_VRF_ID=NMAAS-C-AS64522
//  NMAAS_CUSTOMER_LOGICAL_INTERFACE=ge-0/0/4.144
//  NMAAS_CUSTOMER_VRF_RD=172.16.4.4:8
//  NMAAS_CUSTOMER_VRF_RT=64522L:8
//  NMAAS_CUSTOMER_BGP_GROUP_ID=INET-VPN-NMAAS-C-64522
//  NMAAS_CUSTOMER_BGP_NEIGHBOR_IP=192.168.144.14
//  NMAAS_CUSTOMER_ASN=64522
//  NMAAS_CUSTOMER_PHYSICAL_INTERFACE=ge-0/0/4
//  NMAAS_CUSTOMER_INTERFACE_UNIT=144
//  NMAAS_CUSTOMER_INTERFACE_VLAN=8
//  NMAAS_CUSTOMER_BGP_LOCAL_IP=192.168.144.4
//  NMAAS_CUSTOMER_BGP_LOCAL_CIDR=24

    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER = "R3";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_VRF_ID = "NMAAS-C-AS64522";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_LOGICAL_INTERFACE = "ge-0/0/4.239";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_VRF_RD = "172.16.3.3:8";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_VRF_RT = "64522L:8";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_BGP_GROUP_ID = "INET-VPN-NMAAS-C-64522";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_BGP_NEIGHBOR_IP = "192.168.239.9";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_ASN = "64522";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_PHYSICAL_INTERFACE = "ge-0/0/4";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_INTERFACE_UNIT ="239";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_INTERFACE_VLAN = "239";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_BGP_LOCAL_IP = "192.168.239.3";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_BGP_LOCAL_CIDR = "24";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_POLICY_COMMUNITY_OPTIONS = "NMAAS-C-AS64522-COMMUNITY";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_POLICY_STATEMENT_CONNECTED = "NMAAS-C-AS64522-CONNECTED->OTHER";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_POLICY_STATEMENT_IMPORT = "NMAAS-C-AS64522-IMPORT";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_POLICY_STATEMENT_EXPORT = "NMAAS-C-AS64522-EXPORT";

//  limit=R3
//  NMAAS_CUSTOMER_VRF_ID=NMAAS-C-AS64522
//  NMAAS_CUSTOMER_LOGICAL_INTERFACE=ge-0/0/4.239
//  NMAAS_CUSTOMER_VRF_RD=172.16.3.3:8
//  NMAAS_CUSTOMER_VRF_RT=64522L:8
//  NMAAS_CUSTOMER_BGP_GROUP_ID=INET-VPN-NMAAS-C-64522
//  NMAAS_CUSTOMER_BGP_NEIGHBOR_IP=192.168.239.9
//  NMAAS_CUSTOMER_ASN=64522
//  NMAAS_CUSTOMER_PHYSICAL_INTERFACE=ge-0/0/4
//  NMAAS_CUSTOMER_INTERFACE_UNIT=239
//  NMAAS_CUSTOMER_INTERFACE_VLAN=239
//  NMAAS_CUSTOMER_BGP_LOCAL_IP=192.168.239.3
//  NMAAS_CUSTOMER_BGP_LOCAL_CIDR=24
//  NMAAS_CUSTOMER_POLICY_COMMUNITY_OPTIONS=NMAAS-C-AS64522-COMMUNITY
//  NMAAS_CUSTOMER_POLICY_STATEMENT_CONNECTED=NMAAS-C-AS64522-CONNECTED->OTHER
//  NMAAS_CUSTOMER_POLICY_STATEMENT_IMPORT=NMAAS-C-AS64522-IMPORT
//  NMAAS_CUSTOMER_POLICY_STATEMENT_EXPORT=NMAAS-C-AS64522-EXPORT

    private static AnsiblePlaybookVpnConfig ansiblePlaybookForClientSideRouter;

    private static AnsiblePlaybookVpnConfig ansiblePlaybookForCloudSideRouter;

    static {
        ansiblePlaybookForClientSideRouter = new AnsiblePlaybookVpnConfig(AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE);
        ansiblePlaybookForClientSideRouter.setTargetRouter(DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER);
        ansiblePlaybookForClientSideRouter.setVrfId(DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_VRF_ID);
        ansiblePlaybookForClientSideRouter.setLogicalInterface(DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_LOGICAL_INTERFACE);
        ansiblePlaybookForClientSideRouter.setVrfRd(DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_VRF_RD);
        ansiblePlaybookForClientSideRouter.setVrfRt(DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_VRF_RT);
        ansiblePlaybookForClientSideRouter.setBgpGroupId(DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_BGP_GROUP_ID);
        ansiblePlaybookForClientSideRouter.setBgpNeighborIp(DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_BGP_NEIGHBOR_IP);
        ansiblePlaybookForClientSideRouter.setAsn(DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_ASN);
        ansiblePlaybookForClientSideRouter.setPhysicalInterface(DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_PHYSICAL_INTERFACE);
        ansiblePlaybookForClientSideRouter.setInterfaceUnit(DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_INTERFACE_UNIT);
        ansiblePlaybookForClientSideRouter.setInterfaceVlan(DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_INTERFACE_VLAN);
        ansiblePlaybookForClientSideRouter.setBgpLocalIp(DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_BGP_LOCAL_IP);
        ansiblePlaybookForClientSideRouter.setBgpLocalCidr(DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_BGP_LOCAL_CIDR);

        ansiblePlaybookForCloudSideRouter = new AnsiblePlaybookVpnConfig(AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE);
        ansiblePlaybookForCloudSideRouter.setTargetRouter(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER);
        ansiblePlaybookForCloudSideRouter.setVrfId(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_VRF_ID);
        ansiblePlaybookForCloudSideRouter.setLogicalInterface(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_LOGICAL_INTERFACE);
        ansiblePlaybookForCloudSideRouter.setVrfRd(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_VRF_RD);
        ansiblePlaybookForCloudSideRouter.setVrfRt(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_VRF_RT);
        ansiblePlaybookForCloudSideRouter.setBgpGroupId(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_BGP_GROUP_ID);
        ansiblePlaybookForCloudSideRouter.setBgpNeighborIp(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_BGP_NEIGHBOR_IP);
        ansiblePlaybookForCloudSideRouter.setAsn(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_ASN);
        ansiblePlaybookForCloudSideRouter.setPhysicalInterface(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_PHYSICAL_INTERFACE);
        ansiblePlaybookForCloudSideRouter.setInterfaceUnit(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_INTERFACE_UNIT);
        ansiblePlaybookForCloudSideRouter.setInterfaceVlan(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_INTERFACE_VLAN);
        ansiblePlaybookForCloudSideRouter.setBgpLocalIp(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_BGP_LOCAL_IP);
        ansiblePlaybookForCloudSideRouter.setBgpLocalCidr(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_BGP_LOCAL_CIDR);
        ansiblePlaybookForCloudSideRouter.setPolicyCommunityOptions(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_POLICY_COMMUNITY_OPTIONS);
        ansiblePlaybookForCloudSideRouter.setPolicyStatementConnected(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_POLICY_STATEMENT_CONNECTED);
        ansiblePlaybookForCloudSideRouter.setPolicyStatementImport(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_POLICY_STATEMENT_IMPORT);
        ansiblePlaybookForCloudSideRouter.setPolicyStatementExport(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_POLICY_STATEMENT_EXPORT);
    }

    public static AnsiblePlaybookVpnConfig ansiblePlaybookForClientSideRouter() {
        return ansiblePlaybookForClientSideRouter.copy();
    }

    public static AnsiblePlaybookVpnConfig ansiblePlaybookForCloudSideRouter() {
        return ansiblePlaybookForCloudSideRouter.copy();
    }

}
