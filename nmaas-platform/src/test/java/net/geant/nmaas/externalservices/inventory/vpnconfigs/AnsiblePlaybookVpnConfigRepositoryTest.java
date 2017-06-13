package net.geant.nmaas.externalservices.inventory.vpnconfigs;

import net.geant.nmaas.dcn.deployment.AnsiblePlaybookVpnConfigRepositoryInit;
import net.geant.nmaas.dcn.deployment.entities.AnsiblePlaybookVpnConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

/**
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AnsiblePlaybookVpnConfigRepositoryTest {

    @Autowired
    private AnsiblePlaybookVpnConfigRepository repository;

    @Autowired
    private AnsiblePlaybookVpnConfigRepositoryInit repositoryInit;

    private static final String NEW_ROUTER_NAME = "R13";
    private static final String NEW_DOCKER_HOST_NAME = "GN4-DOCKER-2";
    private static final String EXISTING_DOCKER_HOST_NAME = AnsiblePlaybookVpnConfigRepositoryInit.TEST_DOCKER_HOST_NAME;
    private static final String EXISTING_CLIENT_SIDE_ROUTER_NAME = "R4";
    private static final String EXISTING_CLOUD_SIDE_ROUTER_NAME = "R3";
    private static final long EXISTING_CLIENT_ID = AnsiblePlaybookVpnConfigRepositoryInit.TEST_CUSTOMER_ID;
    private static final long NEW_CLIENT_ID = 2L;

    @Before
    public void populateRepositoryWithDefaults()
            throws AnsiblePlaybookVpnConfigInvalidException, AnsiblePlaybookVpnConfigExistsException {
        repositoryInit.initWithDefaults();
    }

    @After
    public void cleanRepository() throws AnsiblePlaybookVpnConfigNotFoundException {
        repositoryInit.clean();
    }

    @Test
    public void shouldAddNewCloudVpnConfig () throws Exception {
        repository.addCloudVpnConfig(NEW_DOCKER_HOST_NAME, setNewVpnConfig(NEW_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE));
        assertEquals(setNewVpnConfig(NEW_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE), repository.loadCloudVpnConfigByDockerHost(NEW_DOCKER_HOST_NAME));
    }

    @Test(expected = AnsiblePlaybookVpnConfigInvalidException.class)
    public void shouldThrowInvalidExceptionWhenAddingCloudConfig() throws Exception {
        repository.addCloudVpnConfig(null, setNewVpnConfig(NEW_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE));
    }

    @Test(expected = AnsiblePlaybookVpnConfigExistsException.class)
    public void shouldThrowExistsExceptionWhenAddingCloudConfig() throws Exception {
        repository.addCloudVpnConfig(EXISTING_DOCKER_HOST_NAME, setNewVpnConfig(NEW_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE));
    }

    @Test
    public void shouldAddNewCustomerVpnConfig () throws Exception {
        repository.addCustomerVpnConfig(NEW_CLIENT_ID, setNewVpnConfig(NEW_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE));
        assertEquals(setNewVpnConfig(NEW_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE), repository.loadCustomerVpnConfigByCustomerId(NEW_CLIENT_ID));
    }

    @Test(expected = AnsiblePlaybookVpnConfigInvalidException.class)
    public void shouldThrowInvalidExceptionWhenAddingClientConfig() throws Exception {
        repository.addCustomerVpnConfig(-1, setNewVpnConfig(NEW_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE));
    }

    @Test(expected = AnsiblePlaybookVpnConfigExistsException.class)
    public void shouldThrowExistsExceptionWhenAddingClientConfig() throws Exception {
        repository.addCustomerVpnConfig(EXISTING_CLIENT_ID, setNewVpnConfig(NEW_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE));
    }

    @Test
    public void shouldLoadCloudConfigByDockerHostName () throws Exception {
        assertEquals(EXISTING_CLOUD_SIDE_ROUTER_NAME, repository.loadCloudVpnConfigByDockerHost(EXISTING_DOCKER_HOST_NAME).getTargetRouter());
    }

    @Test(expected = AnsiblePlaybookVpnConfigNotFoundException.class)
    public void shouldThrowNotFoundExceptionForLoadCloudConfig() throws Exception {
        repository.loadCloudVpnConfigByDockerHost(EXISTING_DOCKER_HOST_NAME + "wrong");
    }

    @Test
    public void shouldLoadClientConfigByDockerHostName () throws Exception {
        assertEquals(EXISTING_CLIENT_SIDE_ROUTER_NAME, repository.loadCustomerVpnConfigByCustomerId(EXISTING_CLIENT_ID).getTargetRouter());
    }

    @Test(expected = AnsiblePlaybookVpnConfigNotFoundException.class)
    public void shouldThrowNotFoundExceptionForLoadClientConfig() throws Exception {
        repository.loadCustomerVpnConfigByCustomerId(EXISTING_CLIENT_ID + 100);
    }

    @Test
    public void shouldLoadAllClientVpnConfigs () {
        assertEquals(EXISTING_CLIENT_SIDE_ROUTER_NAME, repository.loadAllClientVpnConfigs().get(EXISTING_CLIENT_ID).getTargetRouter());
        assertEquals(1, repository.loadAllClientVpnConfigs().size());
    }

    @Test
    public void shouldLoadAllCloudVpnConfigs () {
        assertEquals(EXISTING_CLOUD_SIDE_ROUTER_NAME, repository.loadAllCloudVpnConfigs().get(EXISTING_DOCKER_HOST_NAME).getTargetRouter());
        assertEquals(1, repository.loadAllClientVpnConfigs().size());
    }

    @Test
    public void shouldUpdateCustomerVpnConfig() throws Exception {
        repository.updateCustomerVpnConfig(EXISTING_CLIENT_ID, setNewVpnConfig(NEW_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE));
        assertEquals(1, repository.loadAllClientVpnConfigs().size());
        assertEquals(NEW_ROUTER_NAME, repository.loadCustomerVpnConfigByCustomerId(EXISTING_CLIENT_ID).getTargetRouter());
    }

    @Test(expected = AnsiblePlaybookVpnConfigInvalidException.class)
    public void shouldThrowInvalidExceptionForUpdateCustomerVpnConfig() throws Exception {
        repository.updateCustomerVpnConfig(-1, setNewVpnConfig(NEW_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE));
    }

    @Test(expected = AnsiblePlaybookVpnConfigNotFoundException.class)
    public void shouldThrowNotFoundExceptionForUpdateCustomerVpnConfig() throws Exception {
        repository.updateCustomerVpnConfig(EXISTING_CLIENT_ID + 100, setNewVpnConfig(NEW_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE));
    }

    @Test
    public void shouldUpdateCloudVpnConfig() throws Exception {
        repository.updateCloudVpnConfig(EXISTING_DOCKER_HOST_NAME, setNewVpnConfig(NEW_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE));
        assertEquals(1, repository.loadAllClientVpnConfigs().size());
        assertEquals(NEW_ROUTER_NAME, repository.loadCloudVpnConfigByDockerHost(EXISTING_DOCKER_HOST_NAME).getTargetRouter());
    }

    @Test(expected = AnsiblePlaybookVpnConfigInvalidException.class)
    public void shouldThrowInvalidExceptionForUpdateCloudVpnConfig() throws Exception {
        repository.updateCloudVpnConfig(null, setNewVpnConfig(NEW_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE));
    }

    @Test(expected = AnsiblePlaybookVpnConfigNotFoundException.class)
    public void shouldThrowNotFoundExceptionForUpdateCloudVpnConfig() throws Exception {
        repository.updateCloudVpnConfig(EXISTING_DOCKER_HOST_NAME + "wrong", setNewVpnConfig(NEW_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE));
    }

    @Test(expected = AnsiblePlaybookVpnConfigNotFoundException.class)
    public void shouldRemoveCloudVpnConfig() throws Exception {
        repository.removeCloudVpnConfig(EXISTING_DOCKER_HOST_NAME);
        repository.loadCloudVpnConfigByDockerHost(EXISTING_DOCKER_HOST_NAME);
    }

    @Test(expected = AnsiblePlaybookVpnConfigNotFoundException.class)
    public void shouldThrowNotFoundExceptionDuringCloudVpnConfigRemoval() throws Exception {
        repository.removeCloudVpnConfig(EXISTING_DOCKER_HOST_NAME + "wrong");
    }

    @Test(expected = AnsiblePlaybookVpnConfigNotFoundException.class)
    public void shouldRemoveConsumerVpnConfig() throws Exception {
        repository.removeClientVpnConfig(EXISTING_CLIENT_ID);
        repository.loadCustomerVpnConfigByCustomerId(EXISTING_CLIENT_ID);
    }

    @Test(expected = AnsiblePlaybookVpnConfigNotFoundException.class)
    public void shouldThrowNotFoundExceptionDuringClientVpnConfigRemoval() throws Exception {
        repository.removeClientVpnConfig(EXISTING_CLIENT_ID + 100);
    }

    private AnsiblePlaybookVpnConfig setNewVpnConfig(String newVpnConfig, AnsiblePlaybookVpnConfig.Type type) {
        AnsiblePlaybookVpnConfig customerVpnConig = new AnsiblePlaybookVpnConfig(type);
        customerVpnConig.setTargetRouter(newVpnConfig);
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
        if (type.equals(AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE)) {
            customerVpnConig.setPolicyCommunityOptions("NMAAS-C-AS64522-COMMUNITY");
            customerVpnConig.setPolicyStatementConnected("NMAAS-C-AS64522-CONNECTED->OTHER");
            customerVpnConig.setPolicyStatementImport("NMAAS-C-AS64522-IMPORT");
            customerVpnConig.setPolicyStatementExport("NMAAS-C-AS64522-EXPORT");
        }
        return customerVpnConig;
    }
}
