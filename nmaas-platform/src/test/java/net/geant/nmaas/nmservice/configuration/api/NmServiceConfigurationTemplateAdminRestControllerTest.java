package net.geant.nmaas.nmservice.configuration.api;

import net.geant.nmaas.nmservice.configuration.repositories.NmServiceConfigFileTemplatesRepository;
import net.geant.nmaas.portal.BaseControllerTest;
import net.geant.nmaas.portal.persistent.entity.Role;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class NmServiceConfigurationTemplateAdminRestControllerTest extends BaseControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    private NmServiceConfigFileTemplatesRepository templatesRepository;

    private MockMvc mvc;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @After
    public void cleanRepository() {
        templatesRepository.deleteAll();
    }

    @Test
    public void shouldStoreAndLoadTemplate() throws Exception {
        String token = getValidUserTokenFor(Role.ROLE_SUPERADMIN);
        mvc.perform(post("/platform/api/management/configurations/templates")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(templateJson()))
                .andExpect(status().isCreated());
        MvcResult result = mvc.perform(get("/platform/api/management/configurations/templates")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getContentAsString(), is(notNullValue()));
    }

    @Test
    public void shouldAuthAndForbidSimpleGet() throws Exception {
        String token = getValidUserTokenFor(Role.ROLE_USER);
        mvc.perform(get("/platform/api/management/configurations/templates")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    private String templateJson() {
        return "{\n" +
                "  \"applicationId\":1,\n" +
                "  \"configFileName\":\"addhosts.cfg\",\n" +
                "  \"configFileTemplateContent\":\"........\"" +
                "}";
    }
}
