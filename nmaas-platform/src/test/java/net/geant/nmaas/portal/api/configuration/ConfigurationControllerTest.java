package net.geant.nmaas.portal.api.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.portal.BaseControllerTest;
import net.geant.nmaas.portal.persistent.entity.Configuration;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import net.geant.nmaas.portal.persistent.repositories.ConfigurationRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ConfigurationControllerTest extends BaseControllerTest {

    @Autowired
    private ConfigurationManager configurationManager;

    @Autowired
    private ConfigurationRepository repository;

    private MockMvc mvc;

    private User user;

    private static String URL_PREFIX = "/api/configuration";

    @Before
    public void init(){
        mvc = createMVC();
        user = UsersHelper.ADMIN;
        repository.deleteAll();
    }

    @Test
    public void shouldAddNewConfiguration() throws Exception {
        Configuration configuration = new Configuration(true, false);
        mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization","Bearer " + getValidTokenForUser(user))
                .content(new ObjectMapper().writeValueAsString(configuration))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        MvcResult mvcResult = mvc.perform(get(URL_PREFIX).contentType(MediaType.APPLICATION_JSON)
                .header("Authorization","Bearer " + getValidTokenForUser(user)))
                .andReturn();
        assertThat(mvcResult.getResponse().getContentAsString(), containsString("\"maintenance\":true"));
    }

    @Test
    public void shouldUpdateConfiguration() throws Exception {
        MvcResult mvcPostResult = mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization","Bearer " + getValidTokenForUser(user))
                .content(new ObjectMapper().writeValueAsString(new Configuration(false, false)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        Long id = Long.parseLong(mvcPostResult.getResponse().getContentAsString());
        Configuration configuration = new Configuration(true, false);
        configuration.setId(id);
        mvc.perform(put(URL_PREFIX+"/{id}",id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization","Bearer " + getValidTokenForUser(user))
                .content(new ObjectMapper().writeValueAsString(configuration))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        MvcResult mvcResult = mvc.perform(get(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization","Bearer " + getValidTokenForUser(user)))
                .andReturn();
        assertThat(mvcResult.getResponse().getContentAsString(), containsString("\"maintenance\":true"));
    }
}
