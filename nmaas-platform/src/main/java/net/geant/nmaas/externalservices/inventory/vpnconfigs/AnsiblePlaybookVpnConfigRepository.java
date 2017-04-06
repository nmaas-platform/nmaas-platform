package net.geant.nmaas.externalservices.inventory.vpnconfigs;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores a static list of Ansible playbooks vpn configurations.
 *
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
public class AnsiblePlaybookVpnConfigRepository {

    private Map<Long, AnsiblePlaybookVpnConfig> customerSideVpnConfigs = new HashMap<>();
    private Map<String, AnsiblePlaybookVpnConfig> cloudSideVpnConfigs = new HashMap<>();

    {
        AnsiblePlaybookVpnConfig customerVpnConig = new AnsiblePlaybookVpnConfig(AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE);
        customerVpnConig.setTargetRouter("R4");
        customerVpnConig.setVrfId("NMAAS-C-AS64522");
        customerVpnConig.setLogicalInterface("ge-0/0/4.144");
        customerVpnConig.setVrfRd("172.16.4.4:8");
        customerVpnConig.setVrfRt("64522L:8");
        customerVpnConig.setBgpGroupId("INET-VPN-NMAAS-C-64522");
        customerVpnConig.setBgpNeighborIp("192.168.144.14");
        customerVpnConig.setAsn("64522");
        customerVpnConig.setPhysicalInterface("ge-0/0/4");
        customerVpnConig.setInterfaceUnit("144");
        customerVpnConig.setInterfaceVlan("8");
        customerVpnConig.setBgpLocalIp("192.168.144.4");
        customerVpnConig.setBgpLocalCidr("24");
        customerSideVpnConfigs.put(1L, customerVpnConig);

        AnsiblePlaybookVpnConfig cloudVpnConig = new AnsiblePlaybookVpnConfig(AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE);
        cloudVpnConig.setTargetRouter("R3");
        cloudVpnConig.setVrfId("NMAAS-C-AS64522");
        cloudVpnConig.setLogicalInterface("ge-0/0/4.239");
        cloudVpnConig.setVrfRd("172.16.3.3:8");
        cloudVpnConig.setVrfRt("64522L:8");
        cloudVpnConig.setBgpGroupId("INET-VPN-NMAAS-C-64522");
        cloudVpnConig.setBgpNeighborIp("192.168.239.9");
        cloudVpnConig.setAsn("64522");
        cloudVpnConig.setPhysicalInterface("ge-0/0/4");
        cloudVpnConig.setInterfaceUnit("239");
        cloudVpnConig.setInterfaceVlan("239");
        cloudVpnConig.setBgpLocalIp("192.168.239.3");
        cloudVpnConig.setBgpLocalCidr("24");
        cloudVpnConig.setPolicyCommunityOptions("NMAAS-C-AS64522-COMMUNITY");
        cloudVpnConig.setPolicyStatementConnected("NMAAS-C-AS64522-CONNECTED->OTHER");
        cloudVpnConig.setPolicyStatementImport("NMAAS-C-AS64522-IMPORT");
        cloudVpnConig.setPolicyStatementExport("NMAAS-C-AS64522-EXPORT");
        cloudSideVpnConfigs.put("GN4-DOCKER-1", cloudVpnConig);
    }
    
    public void addCustomerVpnConfig(long customerId, AnsiblePlaybookVpnConfig customerVpnConfig)
            throws AnsiblePlaybookVpnConfigInvalidException, AnsiblePlaybookVpnConfigExistsException {
        validateCustomerId(customerId);
        validateCustomerVpnConfig(customerVpnConfig);
        try {
            loadCustomerVpnConfigByCustomerId(customerId);
            throw new AnsiblePlaybookVpnConfigExistsException(
                    "Anisble playbook VPN configuration for customer " +  customerId +  " exists in the repository.");
        } catch (AnsiblePlaybookVpnConfigNotFoundException ex) {
            customerVpnConfig.setType(AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE);
            customerSideVpnConfigs.put(customerId, customerVpnConfig);
        }
    }

    public AnsiblePlaybookVpnConfig loadCustomerVpnConfigByCustomerId(long customerId)
            throws AnsiblePlaybookVpnConfigNotFoundException {
        AnsiblePlaybookVpnConfig customerVpnConfig = customerSideVpnConfigs.get(customerId);
        if(customerVpnConfig == null) {
            throw new AnsiblePlaybookVpnConfigNotFoundException(
                    "Did not find Ansible playbook configuration for customer " + customerId + " in the repository");
        }
        return customerVpnConfig;
    }

    private void validateCustomerVpnConfig(AnsiblePlaybookVpnConfig customerVpnConfig) {

    }

    protected void validateCustomerId(long customerId) throws AnsiblePlaybookVpnConfigInvalidException {
        if(customerId < 0) {
            throw new AnsiblePlaybookVpnConfigInvalidException("Customer ID has to be bigger then 0 (" + customerId + ")");
        }
    }
}
