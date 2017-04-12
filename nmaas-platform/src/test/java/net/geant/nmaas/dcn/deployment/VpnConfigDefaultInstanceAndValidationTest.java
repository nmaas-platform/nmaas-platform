package net.geant.nmaas.dcn.deployment;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class VpnConfigDefaultInstanceAndValidationTest {

    private AnsiblePlaybookVpnConfig ansiblePlaybookValidConfigForClientSideRouter;

    private AnsiblePlaybookVpnConfig ansiblePlaybookValidConfigForCloudSideRouter;

    @Before
    public void prepareExampleVpnConfig() {
        ansiblePlaybookValidConfigForClientSideRouter = new AnsiblePlaybookVpnConfig(AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE);
        ansiblePlaybookValidConfigForClientSideRouter.setTargetRouter("DEFAULT_NMAAS_CUSTOMER_TARGET_ROUTER");
        ansiblePlaybookValidConfigForClientSideRouter.setVrfId("DEFAULT_NMAAS_CUSTOMER_VRF_ID");
        ansiblePlaybookValidConfigForClientSideRouter.setLogicalInterface("DEFAULT_NMAAS_CUSTOMER_LOGICAL_INTERFACE");
        ansiblePlaybookValidConfigForClientSideRouter.setVrfRd("DEFAULT_NMAAS_CUSTOMER_VRF_RD");
        ansiblePlaybookValidConfigForClientSideRouter.setVrfRt("DEFAULT_NMAAS_CUSTOMER_VRF_RT");
        ansiblePlaybookValidConfigForClientSideRouter.setBgpGroupId("DEFAULT_NMAAS_CUSTOMER_BGP_GROUP_ID");
        ansiblePlaybookValidConfigForClientSideRouter.setBgpNeighborIp("DEFAULT_NMAAS_CUSTOMER_BGP_NEIGHBOR_IP");
        ansiblePlaybookValidConfigForClientSideRouter.setAsn("DEFAULT_NMAAS_CUSTOMER_ASN");
        ansiblePlaybookValidConfigForClientSideRouter.setPhysicalInterface("DEFAULT_NMAAS_CUSTOMER_PHYSICAL_INTERFACE");
        ansiblePlaybookValidConfigForClientSideRouter.setInterfaceUnit("DEFAULT_NMAAS_CUSTOMER_INTERFACE_UNIT");
        ansiblePlaybookValidConfigForClientSideRouter.setInterfaceVlan("DEFAULT_NMAAS_CUSTOMER_INTERFACE_VLAN");
        ansiblePlaybookValidConfigForClientSideRouter.setBgpLocalIp("DEFAULT_NMAAS_CUSTOMER_BGP_LOCAL_IP");
        ansiblePlaybookValidConfigForClientSideRouter.setBgpLocalCidr("DEFAULT_NMAAS_CUSTOMER_BGP_LOCAL_CIDR");

        ansiblePlaybookValidConfigForCloudSideRouter = new AnsiblePlaybookVpnConfig(AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE);
        ansiblePlaybookValidConfigForCloudSideRouter.setTargetRouter("DEFAULT_NMAAS_CLOUD_TARGET_ROUTER");
        ansiblePlaybookValidConfigForCloudSideRouter.setVrfId("DEFAULT_NMAAS_CLOUD_VRF_ID");
        ansiblePlaybookValidConfigForCloudSideRouter.setLogicalInterface("DEFAULT_NMAAS_CLOUD_LOGICAL_INTERFACE");
        ansiblePlaybookValidConfigForCloudSideRouter.setVrfRd("DEFAULT_NMAAS_CLOUD_VRF_RD");
        ansiblePlaybookValidConfigForCloudSideRouter.setVrfRt("DEFAULT_NMAAS_CLOUD_VRF_RT");
        ansiblePlaybookValidConfigForCloudSideRouter.setBgpGroupId("DEFAULT_NMAAS_CLOUD_BGP_GROUP_ID");
        ansiblePlaybookValidConfigForCloudSideRouter.setBgpNeighborIp("DEFAULT_NMAAS_CLOUD_BGP_NEIGHBOR_IP");
        ansiblePlaybookValidConfigForCloudSideRouter.setAsn("DEFAULT_NMAAS_CLOUD_ASN");
        ansiblePlaybookValidConfigForCloudSideRouter.setPhysicalInterface("DEFAULT_NMAAS_CLOUD_PHYSICAL_INTERFACE");
        ansiblePlaybookValidConfigForCloudSideRouter.setInterfaceUnit("DEFAULT_NMAAS_CLOUD_INTERFACE_UNIT");
        ansiblePlaybookValidConfigForCloudSideRouter.setInterfaceVlan("DEFAULT_NMAAS_CLOUD_INTERFACE_VLAN");
        ansiblePlaybookValidConfigForCloudSideRouter.setBgpLocalIp("DEFAULT_NMAAS_CLOUD_BGP_LOCAL_IP");
        ansiblePlaybookValidConfigForCloudSideRouter.setBgpLocalCidr("DEFAULT_NMAAS_CLOUD_BGP_LOCAL_CIDR");
        ansiblePlaybookValidConfigForCloudSideRouter.setPolicyCommunityOptions("DEFAULT_NMAAS_CLOUD_POLICY_COMMUNITY_OPTIONS");
        ansiblePlaybookValidConfigForCloudSideRouter.setPolicyStatementConnected("DEFAULT_NMAAS_CLOUD_POLICY_STATEMENT_CONNECTED");
        ansiblePlaybookValidConfigForCloudSideRouter.setPolicyStatementImport("DEFAULT_NMAAS_CLOUD_POLICY_STATEMENT_IMPORT");
        ansiblePlaybookValidConfigForCloudSideRouter.setPolicyStatementExport("DEFAULT_NMAAS_CLOUD_POLICY_STATEMENT_EXPORT");
    }

    @Test
    public void shouldReturnDefaultInstanceAndValidateIt() throws AnsiblePlaybookVpnConfig.ConfigNotValidException {
        AnsiblePlaybookVpnConfigDefaults.ansiblePlaybookForClientSideRouter().validate();
        AnsiblePlaybookVpnConfigDefaults.ansiblePlaybookForCloudSideRouter().validate();
    }

    @Test
    public void shouldValidateCompleteAnsiblePlaybookVpnConfigForClientSideRouter() throws AnsiblePlaybookVpnConfig.ConfigNotValidException {
        ansiblePlaybookValidConfigForClientSideRouter.validate();
    }

    @Test
    public void shouldValidateCompleteAnsiblePlaybookVpnConfigForCloudSideRouter() throws AnsiblePlaybookVpnConfig.ConfigNotValidException {
        ansiblePlaybookValidConfigForCloudSideRouter.validate();
    }

    @Test(expected = AnsiblePlaybookVpnConfig.ConfigNotValidException.class)
    public void shouldThrowExceptionOnMissingParam() throws AnsiblePlaybookVpnConfig.ConfigNotValidException {
        ansiblePlaybookValidConfigForClientSideRouter.setTargetRouter(null);
        ansiblePlaybookValidConfigForClientSideRouter.validate();
    }

}
