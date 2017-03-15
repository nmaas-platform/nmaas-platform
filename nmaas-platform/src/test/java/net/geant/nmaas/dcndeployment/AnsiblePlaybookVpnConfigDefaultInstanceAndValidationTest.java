package net.geant.nmaas.dcndeployment;

import net.geant.nmaas.dcn.deployment.AnsiblePlaybookVpnConfig;
import net.geant.nmaas.dcn.deployment.AnsiblePlaybookVpnConfigDefaults;
import net.geant.nmaas.dcn.deployment.exceptions.ConfigNotValidException;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class AnsiblePlaybookVpnConfigDefaultInstanceAndValidationTest {

    private AnsiblePlaybookVpnConfig validConfig;

    @Before
    public void prepareExampleVpnConfig() {
        validConfig = new AnsiblePlaybookVpnConfig();
        validConfig.setTargetRouter("DEFAULT_NMAAS_CUSTOMER_TARGET_ROUTER");
        validConfig.setVrfId("DEFAULT_NMAAS_CUSTOMER_VRF_ID");
        validConfig.setLogicalInterface("DEFAULT_NMAAS_CUSTOMER_LOGICAL_INTERFACE");
        validConfig.setVrfRd("DEFAULT_NMAAS_CUSTOMER_VRF_RD");
        validConfig.setVrfRt("DEFAULT_NMAAS_CUSTOMER_VRF_RT");
        validConfig.setBgpGroupId("DEFAULT_NMAAS_CUSTOMER_BGP_GROUP_ID");
        validConfig.setBgpNeighborIp("DEFAULT_NMAAS_CUSTOMER_BGP_NEIGHBOR_IP");
        validConfig.setAsn("DEFAULT_NMAAS_CUSTOMER_ASN");
        validConfig.setPhysicalInterface("DEFAULT_NMAAS_CUSTOMER_PHYSICAL_INTERFACE");
        validConfig.setId("DEFAULT_NMAAS_CUSTOMER_ID");
        validConfig.setBgpLocalIp("DEFAULT_NMAAS_CUSTOMER_BGP_LOCAL_IP");
        validConfig.setBgpLocalCidr("DEFAULT_NMAAS_CUSTOMER_BGP_LOCAL_CIDR");
    }

    @Test
    public void shouldReturnDefaultInstanceAndValidateIt() throws ConfigNotValidException {
        AnsiblePlaybookVpnConfigDefaults.ansiblePlaybookForClientSideRouter().validate();
    }

    @Test
    public void shouldValidateCompleteVpnConfig() throws ConfigNotValidException {
        validConfig.validate();
    }

    @Test(expected = ConfigNotValidException.class)
    public void shouldThrowExceptionOnMissingParam() throws ConfigNotValidException {
        validConfig.setTargetRouter(null);
        validConfig.validate();
    }

}
