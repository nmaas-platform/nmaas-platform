package net.geant.nmaas.dcn.deployment;

public class AnsiblePlaybookVpnConfigDefaults {

    private static final String DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER = "R4";
    private static final String DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_VRF_ID = "NMAAS-C-AS65538";
    private static final String DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_LOGICAL_INTERFACE = "ge-0/0/3.8";
    private static final String DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_VRF_RD = "182.16.4.4:8";
    private static final String DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_VRF_RT = "65525L:8";
    private static final String DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_BGP_GROUP_ID = "INET-VPN-NMAAS-C-65538";
    private static final String DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_BGP_NEIGHBOR_IP = "192.168.48.8";
    private static final String DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_ASN = "65538";
    private static final String DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_PHYSICAL_INTERFACE = "ge-0/0/3";
    private static final String DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_ID = "8";
    private static final String DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_BGP_LOCAL_IP = "192.168.48.4";
    private static final String DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_BGP_LOCAL_CIDR = "24";

    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER = "R4";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_VRF_ID = "NMAAS-C-AS65538";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_LOGICAL_INTERFACE = "ge-0/0/3.8";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_VRF_RD = "182.16.4.4:8";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_VRF_RT = "65525L:8";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_BGP_GROUP_ID = "INET-VPN-NMAAS-C-65538";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_BGP_NEIGHBOR_IP = "192.168.48.8";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_ASN = "65538";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_PHYSICAL_INTERFACE = "ge-0/0/3";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_ID = "8";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_BGP_LOCAL_IP = "192.168.48.4";
    private static final String DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_BGP_LOCAL_CIDR = "24";

    private static AnsiblePlaybookVpnConfig ansiblePlaybookForClientSideRouter;

    private static AnsiblePlaybookVpnConfig ansiblePlaybookForCloudSideRouter;

    static {
        ansiblePlaybookForClientSideRouter = new AnsiblePlaybookVpnConfig();
        ansiblePlaybookForClientSideRouter.setTargetRouter(DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER);
        ansiblePlaybookForClientSideRouter.setVrfId(DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_VRF_ID);
        ansiblePlaybookForClientSideRouter.setLogicalInterface(DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_LOGICAL_INTERFACE);
        ansiblePlaybookForClientSideRouter.setVrfRd(DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_VRF_RD);
        ansiblePlaybookForClientSideRouter.setVrfRt(DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_VRF_RT);
        ansiblePlaybookForClientSideRouter.setBgpGroupId(DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_BGP_GROUP_ID);
        ansiblePlaybookForClientSideRouter.setBgpNeighborIp(DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_BGP_NEIGHBOR_IP);
        ansiblePlaybookForClientSideRouter.setAsn(DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_ASN);
        ansiblePlaybookForClientSideRouter.setPhysicalInterface(DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_PHYSICAL_INTERFACE);
        ansiblePlaybookForClientSideRouter.setId(DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_ID);
        ansiblePlaybookForClientSideRouter.setBgpLocalIp(DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_BGP_LOCAL_IP);
        ansiblePlaybookForClientSideRouter.setBgpLocalCidr(DEFAULT_NMAAS_CUSTOMER_SIDE_ROUTER_CONFIG_BGP_LOCAL_CIDR);

        ansiblePlaybookForCloudSideRouter = new AnsiblePlaybookVpnConfig();
        ansiblePlaybookForCloudSideRouter.setTargetRouter(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER);
        ansiblePlaybookForCloudSideRouter.setVrfId(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_VRF_ID);
        ansiblePlaybookForCloudSideRouter.setLogicalInterface(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_LOGICAL_INTERFACE);
        ansiblePlaybookForCloudSideRouter.setVrfRd(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_VRF_RD);
        ansiblePlaybookForCloudSideRouter.setVrfRt(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_VRF_RT);
        ansiblePlaybookForCloudSideRouter.setBgpGroupId(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_BGP_GROUP_ID);
        ansiblePlaybookForCloudSideRouter.setBgpNeighborIp(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_BGP_NEIGHBOR_IP);
        ansiblePlaybookForCloudSideRouter.setAsn(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_ASN);
        ansiblePlaybookForCloudSideRouter.setPhysicalInterface(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_PHYSICAL_INTERFACE);
        ansiblePlaybookForCloudSideRouter.setId(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_ID);
        ansiblePlaybookForCloudSideRouter.setBgpLocalIp(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_BGP_LOCAL_IP);
        ansiblePlaybookForCloudSideRouter.setBgpLocalCidr(DEFAULT_NMAAS_CLOUD_SIDE_ROUTER_CONFIG_BGP_LOCAL_CIDR);
    }

    public static AnsiblePlaybookVpnConfig ansiblePlaybookForClientSideRouter() {
        return ansiblePlaybookForClientSideRouter;
    }

    public static AnsiblePlaybookVpnConfig ansiblePlaybookForCloudSideRouter() {
        return ansiblePlaybookForCloudSideRouter;
    }

}
