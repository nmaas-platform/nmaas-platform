package net.geant.nmaas.externalservices.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.externalservices.inventory.network.BasicCustomerNetworkAttachPoint;
import net.geant.nmaas.externalservices.inventory.network.CustomerNetworkMonitoredEquipment;
import net.geant.nmaas.externalservices.inventory.network.repositories.BasicCustomerNetworkAttachPointRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.core.MediaType;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.collection.IsEmptyCollection.emptyCollectionOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test-engine.properties")
public class CustomerNetworkAttachPointManagerRestControllerTest {

    private final static String URL_PREFIX = "/platform/api/management/network";

    private static final long FIRST_CUSTOMER_ID = 1L;

    private static final String EXAMPLE_CUSTOMER_NETWORK_ATTACH_POINT_JSON = "" +
            "{" +
                "\"customerId\":\"" + FIRST_CUSTOMER_ID + "\"," +
                "\"routerName\":\"R4\"," +
                "\"routerId\":\"172.16.4.4\"," +
                "\"routerInterfaceName\":\"ge-0/0/4\"," +
                "\"routerInterfaceUnit\":\"144\"," +
                "\"routerInterfaceVlan\":\"8\"," +
                "\"bgpLocalIp\":\"192.168.144.4\"," +
                "\"bgpNeighborIp\":\"192.168.144.14\"," +
                "\"asNumber\":\"64522\"," +
                "\"monitoredEquipment\": {" +
                    "\"addresses\": [" +
                        "\"11.11.11.11\"," +
                        "\"22.22.22.22\"," +
                        "\"33.33.33.33\"," +
                        "\"44.44.44.44\"," +
                        "\"55.55.55.55\"" +
                    "]," +
                    "\"networks\": []" +
                "}" +
            "}";

    @Autowired
    private BasicCustomerNetworkAttachPointRepository basicCustomerNetworkAttachPointRepository;

    private MockMvc mvc;

    @Before
    public void init() {
        mvc = MockMvcBuilders
                .standaloneSetup(new CustomerNetworkAttachPointManagerRestController(basicCustomerNetworkAttachPointRepository))
                .build();
    }

    @After
    public void cleanRepository() {
        basicCustomerNetworkAttachPointRepository.deleteAll();
    }

    @Test
    public void shouldAddNewCustomerNetworkAttachPoint() throws Exception {
        mvc.perform(post(URL_PREFIX + "/customernetworks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(customerNetworkAttachPoint(FIRST_CUSTOMER_ID)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertThat(basicCustomerNetworkAttachPointRepository.findByCustomerId(FIRST_CUSTOMER_ID).isPresent(), is(true));
        assertThat(
                basicCustomerNetworkAttachPointRepository.findByCustomerId(FIRST_CUSTOMER_ID).get().getCustomerId(),
                equalTo(FIRST_CUSTOMER_ID));
    }

    @Transactional
    @Test
    public void shouldAddNewCustomerNetworkAttachPointFromJson() throws Exception {
        mvc.perform(post(URL_PREFIX + "/customernetworks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(EXAMPLE_CUSTOMER_NETWORK_ATTACH_POINT_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertThat(basicCustomerNetworkAttachPointRepository.findByCustomerId(FIRST_CUSTOMER_ID).isPresent(), is(true));
        assertThat(
                basicCustomerNetworkAttachPointRepository.findByCustomerId(FIRST_CUSTOMER_ID).get().getCustomerId(),
                equalTo(FIRST_CUSTOMER_ID));
        assertThat(
                basicCustomerNetworkAttachPointRepository.findByCustomerId(FIRST_CUSTOMER_ID).get().getRouterName(),
                equalTo("R4"));
        assertThat(
                basicCustomerNetworkAttachPointRepository.findByCustomerId(FIRST_CUSTOMER_ID).get().getAsNumber(),
                equalTo("64522"));
        CustomerNetworkMonitoredEquipment monitoredEquipment = basicCustomerNetworkAttachPointRepository.findByCustomerId(FIRST_CUSTOMER_ID).get().getMonitoredEquipment();
        assertThat(monitoredEquipment, is(notNullValue()));
        assertThat(monitoredEquipment.getNetworks(), emptyCollectionOf(String.class));
        assertThat(monitoredEquipment.getAddresses(), contains("11.11.11.11", "22.22.22.22", "33.33.33.33", "44.44.44.44", "55.55.55.55"));
    }

    @Test
    public void shouldNotAddExistingCustomerNetworkAttachPoint() throws Exception {
        basicCustomerNetworkAttachPointRepository.save(customerNetworkAttachPoint(FIRST_CUSTOMER_ID));
        mvc.perform(post(URL_PREFIX + "/customernetworks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(customerNetworkAttachPoint(FIRST_CUSTOMER_ID)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void shouldNotAddInvalidCustomerNetworkAttachPoint() throws Exception {
        BasicCustomerNetworkAttachPoint basicCustomerNetworkAttachPoint = customerNetworkAttachPoint(FIRST_CUSTOMER_ID);
        basicCustomerNetworkAttachPoint.setAsNumber(null);
        mvc.perform(post(URL_PREFIX + "/customernetworks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(basicCustomerNetworkAttachPoint))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void shouldUpdateCustomerNetworkAttachPoint() throws Exception {
        basicCustomerNetworkAttachPointRepository.save(customerNetworkAttachPoint(FIRST_CUSTOMER_ID));
        BasicCustomerNetworkAttachPoint modifiedBasicCustomerNetworkAttachPoint = customerNetworkAttachPoint(FIRST_CUSTOMER_ID);
        modifiedBasicCustomerNetworkAttachPoint.setAsNumber("50000");
        mvc.perform(put(URL_PREFIX + "/customernetworks/{customerid}", FIRST_CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(modifiedBasicCustomerNetworkAttachPoint))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertThat(
                basicCustomerNetworkAttachPointRepository.findByCustomerId(FIRST_CUSTOMER_ID).get().getAsNumber(),
                equalTo("50000"));
    }

    @Test
    public void shouldNotUpdateNotExistingCustomerNetworkAttachPoint() throws Exception {
        mvc.perform(put(URL_PREFIX + "/customernetworks/{customerid}", FIRST_CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(customerNetworkAttachPoint(FIRST_CUSTOMER_ID)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldNotUpdateInvalidCustomerNetworkAttachPoint() throws Exception {
        basicCustomerNetworkAttachPointRepository.save(customerNetworkAttachPoint(FIRST_CUSTOMER_ID));
        BasicCustomerNetworkAttachPoint invalidBasicCustomerNetworkAttachPoint = customerNetworkAttachPoint(FIRST_CUSTOMER_ID);
        invalidBasicCustomerNetworkAttachPoint.setAsNumber(null);
        mvc.perform(put(URL_PREFIX + "/customernetworks/{customerid}", FIRST_CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(invalidBasicCustomerNetworkAttachPoint))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void shouldGetAllCustomerNetworkAttachPoints() throws Exception {
        mvc.perform(get(URL_PREFIX + "/customernetworks"))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldGetCustomerNetworkAttachPointByCustomerId() throws Exception {
        basicCustomerNetworkAttachPointRepository.save(customerNetworkAttachPoint(FIRST_CUSTOMER_ID));
        MvcResult result = mvc.perform(get(URL_PREFIX + "/customernetworks/{customerid}", FIRST_CUSTOMER_ID))
                .andExpect(status().isOk()).andReturn();
        assertThat(
                ((BasicCustomerNetworkAttachPoint) new ObjectMapper().readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<BasicCustomerNetworkAttachPoint>() {})).getAsNumber(),
                equalTo("64522"));
    }

    @Test
    public void shouldNotGetCustomerNetworkAttachPointByCustomerId() throws Exception {
        mvc.perform(get(URL_PREFIX + "/customernetworks/{customerid}", FIRST_CUSTOMER_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldRemoveCustomerNetworkAttachPoint() throws Exception {
        basicCustomerNetworkAttachPointRepository.save(customerNetworkAttachPoint(FIRST_CUSTOMER_ID));
        assertThat(basicCustomerNetworkAttachPointRepository.findByCustomerId(FIRST_CUSTOMER_ID).isPresent(), is(true));
        mvc.perform(delete(URL_PREFIX + "/customernetworks/{customerid}", FIRST_CUSTOMER_ID))
                .andExpect(status().isNoContent());
        assertThat(basicCustomerNetworkAttachPointRepository.findByCustomerId(FIRST_CUSTOMER_ID).isPresent(), is(false));
    }

    @Test
    public void shouldNotRemoveNotExistingCustomerNetworkAttachPoint() throws Exception {
        mvc.perform(delete(URL_PREFIX + "/customernetworks/{customerid}", FIRST_CUSTOMER_ID))
                .andExpect(status().isNotFound());
    }

    private static BasicCustomerNetworkAttachPoint customerNetworkAttachPoint(Long customerId) {
        BasicCustomerNetworkAttachPoint customerNetworkAttachPoint = new BasicCustomerNetworkAttachPoint();
        customerNetworkAttachPoint.setCustomerId(customerId);
        customerNetworkAttachPoint.setRouterName("R4");
        customerNetworkAttachPoint.setRouterId("172.16.4.4");
        customerNetworkAttachPoint.setRouterInterfaceName("ge-0/0/4");
        customerNetworkAttachPoint.setRouterInterfaceUnit("144");
        customerNetworkAttachPoint.setRouterInterfaceVlan("8");
        customerNetworkAttachPoint.setBgpLocalIp("192.168.144.4");
        customerNetworkAttachPoint.setBgpNeighborIp("192.168.144.14");
        customerNetworkAttachPoint.setAsNumber("64522");
        return customerNetworkAttachPoint;
    }

}
