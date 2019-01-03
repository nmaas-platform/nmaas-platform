package net.geant.nmaas.externalservices.inventory.network;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.externalservices.inventory.network.entities.DomainNetworkAttachPoint;
import net.geant.nmaas.externalservices.inventory.network.repositories.DomainNetworkAttachPointRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.core.MediaType;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DomainNetworkAttachPointManagerRestControllerTest {

    private static final String CORRECT_DOMAIN_NAME = "domainName";
    private static final String CORRECT_URL = "/api/management/domains/" + CORRECT_DOMAIN_NAME + "/network";
    private static final String INCORRECT_DOMAIN_NAME = "missingDomainName";
    private static final String INCORRECT_URL = "/api/management/domains/" + INCORRECT_DOMAIN_NAME + "/network";

    private static final String EXAMPLE_CUSTOMER_NETWORK_ATTACH_POINT_JSON = "" +
            "{" +
            "\"domain\":\"" + CORRECT_DOMAIN_NAME + "\"," +
            "\"routerName\":\"R4\"," +
            "\"routerId\":\"172.16.4.4\"," +
            "\"routerInterfaceName\":\"ge-0/0/4\"," +
            "\"routerInterfaceUnit\":\"144\"," +
            "\"routerInterfaceVlan\":\"8\"," +
            "\"bgpLocalIp\":\"192.168.144.4\"," +
            "\"bgpNeighborIp\":\"192.168.144.14\"," +
            "\"asNumber\":\"64522\"," +
            "\"networks\": []" +
            "}" +
            "}";

    @Autowired
    private DomainNetworkAttachPointRepository repository;

    private MockMvc mvc;

    @Autowired
    private ModelMapper modelMapper;

    @Before
    public void init() {
        mvc = MockMvcBuilders
                .standaloneSetup(new DomainManagerController(repository, modelMapper))
                .build();
    }

    @After
    public void cleanRepository() {
        repository.deleteAll();
    }

    @Test
    public void shouldSetDomainNetworkAttachPoint() throws Exception {
        mvc.perform(post(CORRECT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(domainNetworkAttachPoint(CORRECT_DOMAIN_NAME)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertThat(repository.findByDomain(CORRECT_DOMAIN_NAME).isPresent(),
                is(true));
        assertThat(repository.findByDomain(CORRECT_DOMAIN_NAME).get().getDomain(),
                equalTo(CORRECT_DOMAIN_NAME));
    }

    @Transactional
    @Test
    public void shouldSetDomainNetworkAttachPointFromJson() throws Exception {
        mvc.perform(post(CORRECT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(EXAMPLE_CUSTOMER_NETWORK_ATTACH_POINT_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertThat(repository.findByDomain(CORRECT_DOMAIN_NAME).isPresent(), is(true));
        assertThat(
                repository.findByDomain(CORRECT_DOMAIN_NAME).get().getDomain(),
                equalTo(CORRECT_DOMAIN_NAME));
        assertThat(
                repository.findByDomain(CORRECT_DOMAIN_NAME).get().getRouterName(),
                equalTo("R4"));
        assertThat(
                repository.findByDomain(CORRECT_DOMAIN_NAME).get().getAsNumber(),
                equalTo("64522"));
    }

    @Test
    public void shouldNotUpdateInvalidDomainNetworkAttachPoint() throws Exception {
        DomainNetworkAttachPoint basicCustomerNetworkAttachPoint = domainNetworkAttachPoint(CORRECT_DOMAIN_NAME);
        basicCustomerNetworkAttachPoint.setAsNumber(null);
        mvc.perform(post(CORRECT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(basicCustomerNetworkAttachPoint))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void shouldUpdateDomainNetworkAttachPoint() throws Exception {
        repository.save(domainNetworkAttachPoint(CORRECT_DOMAIN_NAME));
        DomainNetworkAttachPoint modifiedDomainNetworkAttachPoint = domainNetworkAttachPoint(CORRECT_DOMAIN_NAME);
        modifiedDomainNetworkAttachPoint.setAsNumber("50000");
        mvc.perform(post(CORRECT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(modifiedDomainNetworkAttachPoint))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertThat(repository.findByDomain(CORRECT_DOMAIN_NAME).get().getAsNumber(),
                equalTo("50000"));
    }

    @Test
    public void shouldGetCustomerNetworkAttachPointByCustomerId() throws Exception {
        repository.save(domainNetworkAttachPoint(CORRECT_DOMAIN_NAME));
        MvcResult result = mvc.perform(get(CORRECT_URL))
                .andExpect(status().isOk()).andReturn();
        assertThat(
                ((DomainNetworkAttachPoint) new ObjectMapper().readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<DomainNetworkAttachPoint>() {
                        })).getAsNumber(),
                equalTo("64522"));
    }

    @Test
    public void shouldNotGetCustomerNetworkAttachPointByCustomerId() throws Exception {
        mvc.perform(get(INCORRECT_URL))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldRemoveCustomerNetworkAttachPoint() throws Exception {
        repository.save(domainNetworkAttachPoint(CORRECT_DOMAIN_NAME));
        assertThat(repository.findByDomain(CORRECT_DOMAIN_NAME).isPresent(), is(true));
        mvc.perform(delete(CORRECT_URL))
                .andExpect(status().isNoContent());
        assertThat(repository.findByDomain(CORRECT_DOMAIN_NAME).isPresent(), is(false));
    }

    @Test
    public void shouldNotRemoveNotExistingCustomerNetworkAttachPoint() throws Exception {
        mvc.perform(delete(INCORRECT_URL))
                .andExpect(status().isNotFound());
    }

    private static DomainNetworkAttachPoint domainNetworkAttachPoint(String domainName) {
        DomainNetworkAttachPoint domainNetworkAttachPoint = new DomainNetworkAttachPoint();
        domainNetworkAttachPoint.setDomain(domainName);
        domainNetworkAttachPoint.setRouterName("R4");
        domainNetworkAttachPoint.setRouterId("172.16.4.4");
        domainNetworkAttachPoint.setRouterInterfaceName("ge-0/0/4");
        domainNetworkAttachPoint.setRouterInterfaceUnit("144");
        domainNetworkAttachPoint.setRouterInterfaceVlan("8");
        domainNetworkAttachPoint.setBgpLocalIp("192.168.144.4");
        domainNetworkAttachPoint.setBgpNeighborIp("192.168.144.14");
        domainNetworkAttachPoint.setAsNumber("64522");
        return domainNetworkAttachPoint;
    }

}
