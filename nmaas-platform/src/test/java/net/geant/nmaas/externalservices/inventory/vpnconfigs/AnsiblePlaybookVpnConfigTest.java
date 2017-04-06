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

    private String setEmptyExceptionMessage() {
        return  "Target Router is NULL or empty\n" +
                "VRF ID is NULL or empty\n" +
                "Logical Interface is NULL or empty\n" +
                "VRF RD is NULL or empty\n" +
                "VRF RT is NULL or empty\n" +
                "BGP Group ID is NULL or empty\n" +
                "BGP Neighbor IP is NULL or empty\n" +
                "ASN is NULL or empty\n" +
                "Physical Interface is NULL or empty\n" +
                "Interface Unit is NULL or empty\n" +
                "Interface VLAN is NULL or empty\n" +
                "BGP Local IP is NULL or empty\n" +
                "BGP Local CIDR is NULL or empty\n";
    }
}
