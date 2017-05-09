package net.geant.nmaas.externalservices.inventory.vpnconfigs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.dcn.deployment.AnsiblePlaybookVpnConfigRepositoryInit;
import net.geant.nmaas.dcn.deployment.entities.AnsiblePlaybookVpnConfig;
import net.geant.nmaas.externalservices.api.AnsiblePlaybookVpnConfRestController;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.ws.rs.core.MediaType;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AnsiblePlaybookVpnConfRestControllerTest {

    private final static String URL_PREFIX = "/platform/api/management/vpnconfigs";
    private static final String FIRST_NEW_DOCKERHOST_NAME = "GN4-DOCKER-2";
    private static final String SECOND_NEW_DOCKERHOST_NAME = "GN4-DOCKER-3";
    private static final String THIRD_NEW_DOCKERHOST_NAME = "GN4-DOCKER-4";
    private static final String EXISTING_DOCKERHOST_NAME = AnsiblePlaybookVpnConfigRepository.DEFAULT_DOCKERHOST_NAME;
    private static final String FIRST_NEW_ROUTER_NAME = "R5";
    private static final String EXISTING_CLOUD_ROUTER_NAME = "R3";
    private static final String EXISTING_CUSTOMER_ROUTER_NAME = "R4";
    private static final long FIRST_NEW_CUSTOMER_ID = 2L;
    private static final long SECOND_NEW_CUSTOMER_ID = 3L;
    private static final long THIRD_NEW_CUSTOMER_ID = 4L;
    private static final long EXISTING_CUSTOMER_ID = 1L;
    private static final String NEW_CLOUD_INTERFACE_VLAN = "13";
    private static final String NEW_CUSTOMER_INTERFACE_VLAN = "14";

    @Autowired
    private AnsiblePlaybookVpnConfigRepository repository;

    @Autowired
    private AnsiblePlaybookVpnConfigRepositoryInit repositoryInit;

    private MockMvc mvc;

    @Before
    public void init() throws AnsiblePlaybookVpnConfigInvalidException, AnsiblePlaybookVpnConfigExistsException {
        repositoryInit.initWithDefaults();
        mvc = MockMvcBuilders.standaloneSetup(new AnsiblePlaybookVpnConfRestController(repository)).build();
    }

    @After
    public void cleanRepository() throws AnsiblePlaybookVpnConfigNotFoundException {
        repositoryInit.clean();
    }

    @Test
    public void shouldAddNewCloudVpnConfig() throws Exception {
        mvc.perform(post(URL_PREFIX + "/cloud/{hostname}", FIRST_NEW_DOCKERHOST_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(setNewVpnConfig(FIRST_NEW_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertEquals(
                setNewVpnConfig(FIRST_NEW_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE),
                repository.loadCloudVpnConfigByDockerHost(FIRST_NEW_DOCKERHOST_NAME));
    }

    @Test
    public void shouldNotAddExistingCloudVpnConfig() throws Exception {
        mvc.perform(post(URL_PREFIX + "/cloud/{hostname}", EXISTING_DOCKERHOST_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(setNewVpnConfig(FIRST_NEW_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    public void shouldNotAddInvalidCloudVpnConfig() throws Exception {
        mvc.perform(post(URL_PREFIX + "/cloud/{hostname}", SECOND_NEW_DOCKERHOST_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new AnsiblePlaybookVpnConfig()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void shouldUpdateCloudVpnConfig() throws Exception {
        AnsiblePlaybookVpnConfig updatedVpnConfig = setNewVpnConfig(EXISTING_CLOUD_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE);
        updatedVpnConfig.setInterfaceVlan(NEW_CLOUD_INTERFACE_VLAN);
        mvc.perform(put(URL_PREFIX + "/cloud/{hostname}", EXISTING_DOCKERHOST_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(updatedVpnConfig))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertEquals(
                NEW_CLOUD_INTERFACE_VLAN,
                repository.loadCloudVpnConfigByDockerHost(EXISTING_DOCKERHOST_NAME).getInterfaceVlan());
    }

    @Test
    public void shouldNotUpdateNotExistingCloudVpnConfig() throws Exception {
        mvc.perform(put(URL_PREFIX + "/cloud/{hostname}", SECOND_NEW_DOCKERHOST_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(setNewVpnConfig(FIRST_NEW_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldNotUpdateInvalidCloudVpnConfig() throws Exception {
        mvc.perform(put(URL_PREFIX + "/cloud/{hostname}", EXISTING_CUSTOMER_ROUTER_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new AnsiblePlaybookVpnConfig()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void shouldGetAllCloudVpnConfigs() throws Exception {
        mvc.perform(get(URL_PREFIX + "/cloud"))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldGetCloudVpnConfigsByDockerHostName() throws Exception {
        MvcResult result = mvc.perform(get(URL_PREFIX + "/cloud/{hostname}", EXISTING_DOCKERHOST_NAME))
                .andExpect(status().isOk()).andReturn();
        assertEquals(
                EXISTING_CLOUD_ROUTER_NAME,
                ((AnsiblePlaybookVpnConfig) new ObjectMapper().readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<AnsiblePlaybookVpnConfig>() {})).getTargetRouter());
    }

    @Test
    public void shouldNotGetCloudVpnConfigsByDockerHostName() throws Exception {
        mvc.perform(get(URL_PREFIX + "/cloud/{hostname}", SECOND_NEW_DOCKERHOST_NAME))
                .andExpect(status().isNotFound());
    }

    @Test(expected = AnsiblePlaybookVpnConfigNotFoundException.class)
    public void shouldRemoveCloudVpnConfig() throws Exception {
        mvc.perform(post(URL_PREFIX + "/cloud/{hostname}", THIRD_NEW_DOCKERHOST_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(setNewVpnConfig(FIRST_NEW_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLOUD_SIDE)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mvc.perform(delete(URL_PREFIX + "/cloud/{hostname}", THIRD_NEW_DOCKERHOST_NAME))
                .andExpect(status().isNoContent());
        repository.loadCloudVpnConfigByDockerHost(THIRD_NEW_DOCKERHOST_NAME);
    }

    @Test
    public void shouldNotRemoveNotExisitngCloudVpnConfig() throws Exception {
        mvc.perform(delete(URL_PREFIX + "/cloud/{hostname}", THIRD_NEW_DOCKERHOST_NAME))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldAddNewCustomerVpnConfig() throws Exception {
        mvc.perform(post(URL_PREFIX + "/customer/{consumerid}", FIRST_NEW_CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(setNewVpnConfig(FIRST_NEW_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertEquals(
                setNewVpnConfig(FIRST_NEW_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE),
                repository.loadCustomerVpnConfigByCustomerId(FIRST_NEW_CUSTOMER_ID));
    }

    @Test
    public void shouldNotAddExistingCustomerVpnConfig() throws Exception {
        mvc.perform(post(URL_PREFIX + "/customer/{consumerid}", EXISTING_CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(setNewVpnConfig(FIRST_NEW_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    public void shouldNotAddInvalidCustomerVpnConfig() throws Exception {
        mvc.perform(post(URL_PREFIX + "/customer/{consumerid}", SECOND_NEW_CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new AnsiblePlaybookVpnConfig()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void shouldUpdateCustomerVpnConfig() throws Exception {
        AnsiblePlaybookVpnConfig updatedVpnConfig = setNewVpnConfig(EXISTING_CUSTOMER_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE);
        updatedVpnConfig.setInterfaceVlan(NEW_CUSTOMER_INTERFACE_VLAN);
        mvc.perform(put(URL_PREFIX + "/customer/{customerid}", EXISTING_CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(updatedVpnConfig))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertEquals(
                NEW_CUSTOMER_INTERFACE_VLAN,
                repository.loadCustomerVpnConfigByCustomerId(EXISTING_CUSTOMER_ID).getInterfaceVlan());
    }

    @Test
    public void shouldNotUpdateNotExistingConsumerVpnConfig() throws Exception {
        mvc.perform(put(URL_PREFIX + "/customer/{customerid}", SECOND_NEW_CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(setNewVpnConfig(FIRST_NEW_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldNotUpdateInvalidConsumerVpnConfig() throws Exception {
        mvc.perform(put(URL_PREFIX + "/customer/{customerid}", EXISTING_CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new AnsiblePlaybookVpnConfig()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void shouldGetAllCustomerVpnConfigs() throws Exception {
        mvc.perform(get(URL_PREFIX + "/customer"))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldGetCustomerVpnConfigsByCustomerId() throws Exception {
        MvcResult result = mvc.perform(get(URL_PREFIX + "/customer/{customerid}", EXISTING_CUSTOMER_ID))
                .andExpect(status().isOk()).andReturn();
        assertEquals(
                EXISTING_CUSTOMER_ROUTER_NAME,
                ((AnsiblePlaybookVpnConfig) new ObjectMapper().readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<AnsiblePlaybookVpnConfig>() {})).getTargetRouter());
    }

    @Test
    public void shouldNotCustomerVpnConfigsByCustomerId() throws Exception {
        mvc.perform(get(URL_PREFIX + "/customer/{customerid}", SECOND_NEW_CUSTOMER_ID))
                .andExpect(status().isNotFound());
    }

    @Test(expected = AnsiblePlaybookVpnConfigNotFoundException.class)
    public void shouldRemoveCustomerVpnConfig() throws Exception {
        mvc.perform(post(URL_PREFIX + "/customer/{customerid}", THIRD_NEW_CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(setNewVpnConfig(FIRST_NEW_ROUTER_NAME, AnsiblePlaybookVpnConfig.Type.CLIENT_SIDE)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mvc.perform(delete(URL_PREFIX + "/customer/{customerid}", THIRD_NEW_CUSTOMER_ID))
                .andExpect(status().isNoContent());
        repository.loadCustomerVpnConfigByCustomerId(THIRD_NEW_CUSTOMER_ID);
    }

    @Test
    public void shouldNotRemoveNotExistingCustomerVpnConfig() throws Exception {
        mvc.perform(delete(URL_PREFIX + "/customer/{customerid}", THIRD_NEW_CUSTOMER_ID))
                .andExpect(status().isNotFound());
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
