package net.geant.nmaas.externalservices.inventory.vpnconfigs;

import net.geant.nmaas.dcn.deployment.entities.AnsiblePlaybookVpnConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
public class AnsiblePlaybookVpnConfigTest {
    private static final String TOO_LONG_VALUE = "1234567890123456789012345678901234567890123456789012345678901";

    @Test
    public void shouldValidateIpAddress() throws Exception {
        assertEquals(true, new AnsiblePlaybookVpnConfig().validateIpAddress("172.16.3.3:8"));
        assertEquals(true, new AnsiblePlaybookVpnConfig().validateIpAddress("172.16.3.3"));
    }

    @Test
    public void shouldNotValidateIpAddress () {
        assertEquals(false, new AnsiblePlaybookVpnConfig().validateIpAddress("172-16.3.3"));
    }

    @Test
    public void shouldNotValidateEmptyPropertiesForCustomer () throws Exception {
        try {
            new AnsiblePlaybookVpnConfig(AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE).validate();
            assertTrue(false);
        } catch (AnsiblePlaybookVpnConfigInvalidException ex) {
            assertEquals(setEmptyExceptionMessage(AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE), ex.getMessage());
        }
    }

    @Test
    public void shouldNotValidateEmptyPropertiesForCloud () throws Exception {
        try {
            new AnsiblePlaybookVpnConfig(AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE).validate();
            assertTrue(false);
        } catch (AnsiblePlaybookVpnConfigInvalidException ex) {
            assertEquals(setEmptyExceptionMessage(AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE), ex.getMessage());
        }
    }

    @Test
    public void shouldNotValidateTooLongPropertiesForCloud () throws Exception {
        try {
            setTooLongValuesToVpnConfig(AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE).validate();
            assertTrue(false);
        } catch (AnsiblePlaybookVpnConfigInvalidException ex) {
            assertEquals(setTooLongExceptionMessage(AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE), ex.getMessage());
        }
    }

    @Test
    public void shouldNotValidateTooLongPropertiesForCustomer () throws Exception {
        try {
            setTooLongValuesToVpnConfig(AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE).validate();
            assertTrue(false);
        } catch (AnsiblePlaybookVpnConfigInvalidException ex) {
            assertEquals(setTooLongExceptionMessage(AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE), ex.getMessage());
        }
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    private String setEmptyExceptionMessage(AnsiblePlaybookVpnConfig.Type type) {
        StringBuilder messageException = new StringBuilder();
        messageException.append("Target Router is NULL or empty\n");
        messageException.append("VRF ID is NULL or empty\n");
        messageException.append("Logical Interface is NULL or empty\n");
        messageException.append("VRF RD is NULL or empty\n");
        messageException.append("VRF RT is NULL or empty\n");
        messageException.append("BGP Group ID is NULL or empty\n");
        messageException.append("BGP Neighbor IP is NULL or empty\n");
        messageException.append("ASN is NULL or empty\n");
        messageException.append("Physical Interface is NULL or empty\n");
        messageException.append("Interface Unit is NULL or empty\n");
        messageException.append("Interface VLAN is NULL or empty\n");
        messageException.append("BGP Local IP is NULL or empty\n");
        messageException.append("BGP Local CIDR is NULL or empty\n");
        if (type.equals(AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE)) {
            messageException.append("Policy Community Options is NULL or empty\n");
            messageException.append("Policy Statement Connected is NULL or empty\n");
            messageException.append("Policy Statement Import is NULL or empty\n");
            messageException.append("Policy Statement Export is NULL or empty\n");
        }
        return messageException.toString();
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    private String setTooLongExceptionMessage(AnsiblePlaybookVpnConfig.Type type) {
        int max = AnsiblePlaybookVpnConfig.MAX_PROPERTY_LENGTH;
        StringBuilder messageException = new StringBuilder();
        messageException.append("Target Router is too long (max ").append(max).append(" characters)\n");
        messageException.append("VRF ID is too long (max ").append(max).append(" characters)\n");
        messageException.append("Logical Interface is too long (max ").append(max).append(" characters)\n");
        messageException.append("VRF RD is too long (max ").append(max).append(" characters)\n");
        messageException.append("VRF RT is too long (max ").append(max).append(" characters)\n");
        messageException.append("BGP Group ID is too long (max ").append(max).append(" characters)\n");
        messageException.append("BGP Neighbor IP is too long (max ").append(max).append(" characters)\n");
        messageException.append("ASN is too long (max ").append(max).append(" characters)\n");
        messageException.append("Physical Interface is too long (max ").append(max).append(" characters)\n");
        messageException.append("Interface Unit is too long (max ").append(max).append(" characters)\n");
        messageException.append("Interface VLAN is too long (max ").append(max).append(" characters)\n");
        messageException.append("BGP Local IP is too long (max ").append(max).append(" characters)\n");
        messageException.append("BGP Local CIDR is too long (max ").append(max).append(" characters)\n");
        if (type.equals(AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE)) {
            messageException.append("Policy Community Options is too long (max ").append(max).append(" characters)\n");
            messageException.append("Policy Statement Connected is too long (max ").append(max).append(" characters)\n");
            messageException.append("Policy Statement Import is too long (max ").append(max).append(" characters)\n");
            messageException.append("Policy Statement Export is too long (max ").append(max).append(" characters)\n");
        }
        return messageException.toString();
    }

    private AnsiblePlaybookVpnConfig setTooLongValuesToVpnConfig(AnsiblePlaybookVpnConfig.Type type) {
        AnsiblePlaybookVpnConfig customerVpnConig = new AnsiblePlaybookVpnConfig(type);
        customerVpnConig.setTargetRouter(TOO_LONG_VALUE);
        customerVpnConig.setVrfId(TOO_LONG_VALUE);
        customerVpnConig.setLogicalInterface(TOO_LONG_VALUE);
        customerVpnConig.setVrfRd(TOO_LONG_VALUE);
        customerVpnConig.setVrfRt(TOO_LONG_VALUE);
        customerVpnConig.setBgpGroupId(TOO_LONG_VALUE);
        customerVpnConig.setBgpNeighborIp(TOO_LONG_VALUE);
        customerVpnConig.setAsn(TOO_LONG_VALUE);
        customerVpnConig.setPhysicalInterface(TOO_LONG_VALUE);
        customerVpnConig.setInterfaceUnit(TOO_LONG_VALUE);
        customerVpnConig.setInterfaceVlan(TOO_LONG_VALUE);
        customerVpnConig.setBgpLocalIp(TOO_LONG_VALUE);
        customerVpnConig.setBgpLocalCidr(TOO_LONG_VALUE);
        if (type.equals(AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE)) {
            customerVpnConig.setPolicyCommunityOptions(TOO_LONG_VALUE);
            customerVpnConig.setPolicyStatementConnected(TOO_LONG_VALUE);
            customerVpnConig.setPolicyStatementImport(TOO_LONG_VALUE);
            customerVpnConig.setPolicyStatementExport(TOO_LONG_VALUE);
        }
        return customerVpnConig;
    }
}
