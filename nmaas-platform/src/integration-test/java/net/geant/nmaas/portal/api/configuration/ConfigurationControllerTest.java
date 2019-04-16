package net.geant.nmaas.portal.api.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import net.geant.nmaas.portal.persistent.repositories.ConfigurationRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ConfigurationControllerTest extends BaseControllerTestSetup {

    @Autowired
    private ConfigurationManager configurationManager;

    @Autowired
    private ConfigurationRepository repository;

    private User user;

    private static String URL_PREFIX = "/api/configuration";

    @BeforeEach
    public void init(){
        mvc = createMVC();
        user = UsersHelper.ADMIN;
        repository.deleteAll();
    }

    @Test
    public void shouldAddNewConfiguration() throws Exception {
        ConfigurationView configuration = new ConfigurationView(true, false, "en");
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
        mvc.perform(post("/api/i18n/en?enabled=true")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization","Bearer " + getValidTokenForUser(user))
                .content("[]"));
        MvcResult mvcPostResult = mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization","Bearer " + getValidTokenForUser(user))
                .content(new ObjectMapper().writeValueAsString(new ConfigurationView(false, false, "en")))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        Long id = Long.parseLong(mvcPostResult.getResponse().getContentAsString());
        ConfigurationView configuration = new ConfigurationView(true, false, "en");
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
