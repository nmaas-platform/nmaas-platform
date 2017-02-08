package net.geant.nmaas.dcndeployment;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.NmaasPlatformConfiguration;
import net.geant.nmaas.dcndeployment.api.AnsiblePlaybookStatus;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
        mvc.perform(get("/api/dcns")
                .with(user("test").roles(NmaasPlatformConfiguration.AUTH_ROLE_NMAAS_TEST_CLIENT)))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldAuthAndForbidSimpleGet() throws Exception {
        mvc.perform(get("/api/dcns")
                .with(user("test").roles(NmaasPlatformConfiguration.AUTH_ROLE_ANSIBLE_CLIENT)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldAuthAndCallNotificationPost() throws Exception {
        mvc.perform(post("/api/dcns/notifications/{serviceId}/status", DcnIdentifierConverter.encode("testDcn"))
                .with(user("test").roles(NmaasPlatformConfiguration.AUTH_ROLE_ANSIBLE_CLIENT))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new AnsiblePlaybookStatus("success")))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    public void shouldAuthAndForbidNotificationPost() throws Exception {
        mvc.perform(post("/api/dcns/notifications/{serviceId}/status", DcnIdentifierConverter.encode("testDcn"))
                .with(user("test").roles(NmaasPlatformConfiguration.AUTH_ROLE_NMAAS_TEST_CLIENT))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new AnsiblePlaybookStatus("success")))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

}
