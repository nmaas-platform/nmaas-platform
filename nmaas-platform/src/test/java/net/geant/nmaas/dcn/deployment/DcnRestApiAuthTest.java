package net.geant.nmaas.dcn.deployment;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.dcn.deployment.DcnIdentifierConverter;
import net.geant.nmaas.dcn.deployment.api.AnsiblePlaybookStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DcnRestApiAuthTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private Filter springSecurityFilterChain;

    private MockMvc mvc;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Test
    public void shouldAuthAndCallSimpleGet() throws Exception {
        mvc.perform(get("/platform/api/dcns")
                .with(httpBasic(context.getEnvironment().getProperty("api.client.nmaas.test.username"), context.getEnvironment().getProperty("api.client.nmaas.test.password"))))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldAuthAndForbidSimpleGet() throws Exception {
        mvc.perform(get("/platform/api/dcns")
                .with(httpBasic(context.getEnvironment().getProperty("api.client.ansible.username"), context.getEnvironment().getProperty("api.client.ansible.password"))))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldAuthAndCallNotificationPost() throws Exception {
        mvc.perform(post("/platform/api/dcns/notifications/{serviceId}/status", DcnIdentifierConverter.encode("testDcn"))
                .with(httpBasic(context.getEnvironment().getProperty("api.client.ansible.username"), context.getEnvironment().getProperty("api.client.ansible.password")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new AnsiblePlaybookStatus("success")))
                .accept(MediaType.APPLICATION_JSON))
        		.andExpect(status().isCreated());
    }

    @Test
    public void shouldAuthAndForbidNotificationPost() throws Exception {
        mvc.perform(post("/platform/api/dcns/notifications/{serviceId}/status", DcnIdentifierConverter.encode("testDcn"))
                .with(httpBasic(context.getEnvironment().getProperty("api.client.nmaas.test.username"), context.getEnvironment().getProperty("api.client.nmaas.test.password")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new AnsiblePlaybookStatus("success")))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

}
