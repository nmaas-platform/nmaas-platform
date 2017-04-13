package net.geant.nmaas.externalservices.inventory.vpnconfigs;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores a static list of Ansible playbooks VPN configurations.
 *
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
@Service
public class AnsiblePlaybookVpnConfigRepository {

    private Map<Long, AnsiblePlaybookVpnConfig> customerSideVpnConfigs = new HashMap<>();
    private Map<String, AnsiblePlaybookVpnConfig> cloudSideVpnConfigs = new HashMap<>();
    private final static String DEFAULT_DOCKERHOST_NAME = "GN4-DOCKER-1";
    private final static long DEFAULT_CUSTOMER_ID = 1L;

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
        customerSideVpnConfigs.put(DEFAULT_CUSTOMER_ID, customerVpnConig);

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
        cloudSideVpnConfigs.put(DEFAULT_DOCKERHOST_NAME, cloudVpnConig);
    }

    /**
     * Store {@link AnsiblePlaybookVpnConfig} client instance in the repository
     * @param customerId Client unique identifier
     * @param customerVpnConfig New {@link AnsiblePlaybookVpnConfig} instance
     * @throws AnsiblePlaybookVpnConfigInvalidException when invalid input
     * @throws AnsiblePlaybookVpnConfigExistsException when Ansible playbook VPN configuration exists in the repository
     */
    public void addCustomerVpnConfig(long customerId, AnsiblePlaybookVpnConfig customerVpnConfig)
            throws AnsiblePlaybookVpnConfigInvalidException, AnsiblePlaybookVpnConfigExistsException {
        validateCustomerId(customerId);
        validateVpnConfig(customerVpnConfig);
        try {
            loadCustomerVpnConfigByCustomerId(customerId);
            throw new AnsiblePlaybookVpnConfigExistsException(
                    "Anisble playbook VPN configuration for customer " +  customerId +  " exists in the repository.");
        } catch (AnsiblePlaybookVpnConfigNotFoundException ex) {
            customerVpnConfig.setType(AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE);
            customerSideVpnConfigs.put(customerId, customerVpnConfig);
        }
    }

    /**
     * Updates {@link AnsiblePlaybookVpnConfig} client instance in the repository
     * @param customerId Client unique identifier
     * @param customerVpnConfig Updated {@link AnsiblePlaybookVpnConfig} instance
     * @throws AnsiblePlaybookVpnConfigInvalidException when invalid input
     * @throws AnsiblePlaybookVpnConfigNotFoundException when Ansible playbook VPN configuration doeas not exist in the repository
     */
    public void updateCustomerVpnConfig(long customerId, AnsiblePlaybookVpnConfig customerVpnConfig)
            throws AnsiblePlaybookVpnConfigInvalidException, AnsiblePlaybookVpnConfigNotFoundException {
        validateCustomerId(customerId);
        validateVpnConfig(customerVpnConfig);
        try {
            loadCustomerVpnConfigByCustomerId(customerId);
            customerVpnConfig.setType(AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE);
            customerSideVpnConfigs.put(customerId, customerVpnConfig);
        } catch (AnsiblePlaybookVpnConfigNotFoundException ex) {
            throw new AnsiblePlaybookVpnConfigNotFoundException (
                    "Anisble playbook VPN configuration for customer " +  customerId +  " does not exist in the repository.");
        }
    }

    /**
     * Store {@link AnsiblePlaybookVpnConfig} cloud instance in the repository
     * @param dockerHostName DockerHost unique identifier
     * @param cloudVpnConfig New {@link AnsiblePlaybookVpnConfig} instance
     * @throws AnsiblePlaybookVpnConfigInvalidException when invalid input
     * @throws AnsiblePlaybookVpnConfigExistsException when Ansible playbook VPN configuration exists in the repository
     */
    public void addCloudVpnConfig(String dockerHostName, AnsiblePlaybookVpnConfig cloudVpnConfig)
            throws AnsiblePlaybookVpnConfigInvalidException, AnsiblePlaybookVpnConfigExistsException {
        validateDockerHostName(dockerHostName);
        validateVpnConfig(cloudVpnConfig);
        try {
            loadCloudVpnConfigByDockerHost(dockerHostName);
            throw new AnsiblePlaybookVpnConfigExistsException(
                    "Anisble playbook VPN cloud configuration for DockerHost " +  dockerHostName +  " exists in the repository.");
        } catch (AnsiblePlaybookVpnConfigNotFoundException ex) {
            cloudVpnConfig.setType(AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE);
            cloudSideVpnConfigs.put(dockerHostName, cloudVpnConfig);
        }
    }

    /**
     * Updates {@link AnsiblePlaybookVpnConfig} cloud instance in the repository
     * @param dockerHostName DockerHost unique identifier
     * @param cloudVpnConfig Updated {@link AnsiblePlaybookVpnConfig} instance
     * @throws AnsiblePlaybookVpnConfigInvalidException when invalid input
     * @throws AnsiblePlaybookVpnConfigNotFoundException when Ansible playbook VPN configuration doeas not exist in the repository
     */
    public void updateCloudVpnConfig(String dockerHostName, AnsiblePlaybookVpnConfig cloudVpnConfig)
            throws AnsiblePlaybookVpnConfigInvalidException, AnsiblePlaybookVpnConfigNotFoundException {
        validateDockerHostName(dockerHostName);
        validateVpnConfig(cloudVpnConfig);
        try {
            loadCloudVpnConfigByDockerHost(dockerHostName);
            cloudVpnConfig.setType(AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE);
            cloudSideVpnConfigs.put(dockerHostName, cloudVpnConfig);
        } catch (AnsiblePlaybookVpnConfigNotFoundException ex) {
            throw new AnsiblePlaybookVpnConfigNotFoundException(
                    "Anisble playbook VPN configuration for DockerHost " +  dockerHostName +  " does not exist in the repository.");
        }
    }

    /**
     * Loads {@link AnsiblePlaybookVpnConfig} instance from the repository by client id
     * @param customerId Client unique identifier
     * @return {@link AnsiblePlaybookVpnConfig} instance
     * @throws AnsiblePlaybookVpnConfigNotFoundException when Ansible playbook VPN configuration does not exists in the repository
     */
    public AnsiblePlaybookVpnConfig loadCustomerVpnConfigByCustomerId(long customerId)
            throws AnsiblePlaybookVpnConfigNotFoundException {
        AnsiblePlaybookVpnConfig customerVpnConfig = customerSideVpnConfigs.get(customerId);
        if(customerVpnConfig == null) {
            throw new AnsiblePlaybookVpnConfigNotFoundException(
                    "Did not find Ansible playbook configuration for customer " + customerId + " in the repository");
        }
        return customerVpnConfig;
    }

    /**
     * Loads {@link AnsiblePlaybookVpnConfig} instance from the repository by Docker host name
     * @param hostName Docker host unique name
     * @return {@link AnsiblePlaybookVpnConfig} instance
     * @throws AnsiblePlaybookVpnConfigNotFoundException when Ansible playbook VPN configuration does not exists in the repository
     */
    public AnsiblePlaybookVpnConfig loadCloudVpnConfigByDockerHost (String hostName)
            throws AnsiblePlaybookVpnConfigNotFoundException {
        AnsiblePlaybookVpnConfig customerVpnConfig = cloudSideVpnConfigs.get(hostName);
        if(customerVpnConfig == null) {
            throw new AnsiblePlaybookVpnConfigNotFoundException(
                    "Did not find Ansible playbook cloud configuration for DockerHost " + hostName + " in the repository");
        }
        return customerVpnConfig;
    }

    /**
     * Loads all customer side {@link AnsiblePlaybookVpnConfig} instances
     * @return {@link Map} of {@link AnsiblePlaybookVpnConfig} instances by customer id as key
     */
    public Map<Long, AnsiblePlaybookVpnConfig> loadAllClientVpnConfigs () {
        return customerSideVpnConfigs;
    }

    /**
     * Loads all cloud side {@link AnsiblePlaybookVpnConfig} instances
     * @return {@link Map} of {@link AnsiblePlaybookVpnConfig} instances by DockerHost name as a key
     */
    public Map<String, AnsiblePlaybookVpnConfig> loadAllCloudVpnConfigs () {
        return cloudSideVpnConfigs;
    }

    /**
     * Removes {@link AnsiblePlaybookVpnConfig} cloud instance from the repository
     * @param hostName Unique Docker host name
     * @throws AnsiblePlaybookVpnConfigNotFoundException when Ansible playbook VPN configuration does not exists in the repository
     */
    public void removeCloudVpnConfig(String hostName)
            throws AnsiblePlaybookVpnConfigNotFoundException {
        loadCloudVpnConfigByDockerHost(hostName);
        cloudSideVpnConfigs.remove(hostName);
    }

    /**
     * Removes {@link AnsiblePlaybookVpnConfig} client instance from the repository
     * @param consumerId Client unique identifier
     * @throws AnsiblePlaybookVpnConfigNotFoundException when Ansible playbook VPN configuration does not exists in the repository
     */
    public void removeClientVpnConfig(long consumerId)
            throws AnsiblePlaybookVpnConfigNotFoundException {
        loadCustomerVpnConfigByCustomerId(consumerId);
        customerSideVpnConfigs.remove(consumerId);
    }

    /**
     * Loads default Ansible playbook VPN customer configuration
     * @return {@link AnsiblePlaybookVpnConfig} instance
     * @throws AnsiblePlaybookVpnConfigNotFoundException when Ansible playbook VPN configuration does not exists in the repository
     */
    public AnsiblePlaybookVpnConfig loadDefaultCustomerVpnConfig() throws AnsiblePlaybookVpnConfigNotFoundException {
        return loadCustomerVpnConfigByCustomerId(DEFAULT_CUSTOMER_ID);
    }

    /**
     * Loads default Ansible playbook VPN cloud configuration
     * @return {@link AnsiblePlaybookVpnConfig} instance
     * @throws AnsiblePlaybookVpnConfigNotFoundException when Ansible playbook VPN configuration does not exists in the repository
     */
    public AnsiblePlaybookVpnConfig loadDefaultCloudVpnConfig() throws AnsiblePlaybookVpnConfigNotFoundException {
        return loadCloudVpnConfigByDockerHost(DEFAULT_DOCKERHOST_NAME);
    }

    private void validateVpnConfig(AnsiblePlaybookVpnConfig customerVpnConfig)
            throws AnsiblePlaybookVpnConfigInvalidException {
        customerVpnConfig.validate();
    }

    private void validateCustomerId(long customerId) throws AnsiblePlaybookVpnConfigInvalidException {
        if(customerId < 0) {
            throw new AnsiblePlaybookVpnConfigInvalidException("Customer ID has to be bigger then 0 (" + customerId + ")");
        }
    }

    private void validateDockerHostName(String dockerHostName)
            throws AnsiblePlaybookVpnConfigInvalidException {
        if(dockerHostName == null) {
            throw new AnsiblePlaybookVpnConfigInvalidException("DockerHost name cannot be null.");
        }
    }
}
