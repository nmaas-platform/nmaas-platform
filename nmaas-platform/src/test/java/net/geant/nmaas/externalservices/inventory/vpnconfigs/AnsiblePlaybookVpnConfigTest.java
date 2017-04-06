package net.geant.nmaas.externalservices.inventory.vpnconfigs;

import org.junit.Before;
import org.junit.Test;

import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
public class AnsiblePlaybookVpnConfigTest {
    private AnsiblePlaybookVpnConfigRepository repository;

    @Before
    public void init () {
        repository = new AnsiblePlaybookVpnConfigRepository();
    }

    @Test
    public void shouldValidateIpAddress() throws UnknownHostException {
        assertEquals(true, new AnsiblePlaybookVpnConfig().validateIpAddress("172.16.3.3:8"));
        assertEquals(true, new AnsiblePlaybookVpnConfig().validateIpAddress("172.16.3.3"));
    }

    @Test
    public void shouldNotValidateIpAddress () {
        assertEquals(false, new AnsiblePlaybookVpnConfig().validateIpAddress("172-16.3.3"));
    }

    @Test
    public void shouldNotValidateAnsiblePlaybookVpnConfig () throws AnsiblePlaybookVpnConfigInvalidException {
        try {
            new AnsiblePlaybookVpnConfig(AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE).validate();
            assertTrue(false);
        } catch (AnsiblePlaybookVpnConfigInvalidException ex) {
            assertEquals(setEmptyExceptionMessage(), ex.getMessage());
        }
    }

    @Test
    public void shouldValidateCustomerVpnConfig ()
            throws AnsiblePlaybookVpnConfigNotFoundException, AnsiblePlaybookVpnConfigInvalidException {
        repository.loadCustomerVpnConfigByCustomerId(1L).validate();
        assertTrue(true);
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    private String setEmptyExceptionMessage() {
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
        return messageException.toString();
    }
}
