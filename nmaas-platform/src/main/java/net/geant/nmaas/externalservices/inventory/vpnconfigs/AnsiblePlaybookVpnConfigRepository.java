package net.geant.nmaas.externalservices.inventory.vpnconfigs;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores list of Ansible playbooks VPN configurations.
 *
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
@Service
public class AnsiblePlaybookVpnConfigRepository {

    private Map<Long, AnsiblePlaybookVpnConfig> customerSideVpnConfigs = new HashMap<>();
    private Map<String, AnsiblePlaybookVpnConfig> cloudSideVpnConfigs = new HashMap<>();
    public final static String DEFAULT_DOCKERHOST_NAME = "docker-host-1";
    public final static long DEFAULT_CUSTOMER_ID = 1L;

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
                    "Ansible playbook VPN cloud configuration for DockerHost " +  dockerHostName +  " exists in the repository.");
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
        return customerVpnConfig.copy();
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
        return customerVpnConfig.copy();
    }

    /**
     * Loads all customer side {@link AnsiblePlaybookVpnConfig} instances
     * @return {@link Map} of {@link AnsiblePlaybookVpnConfig} instances by customer id as key
     */
    public Map<Long, AnsiblePlaybookVpnConfig> loadAllClientVpnConfigs () {
        return new HashMap<>(customerSideVpnConfigs);
    }

    /**
     * Loads all cloud side {@link AnsiblePlaybookVpnConfig} instances
     * @return {@link Map} of {@link AnsiblePlaybookVpnConfig} instances by DockerHost name as a key
     */
    public Map<String, AnsiblePlaybookVpnConfig> loadAllCloudVpnConfigs () {
        return new HashMap<>(cloudSideVpnConfigs);
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

    public void removeAllCloudVpnConfigs() {
        cloudSideVpnConfigs = new HashMap<>();
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

    public void removeAllClientVpnConfigs() {
        customerSideVpnConfigs = new HashMap<>();
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
