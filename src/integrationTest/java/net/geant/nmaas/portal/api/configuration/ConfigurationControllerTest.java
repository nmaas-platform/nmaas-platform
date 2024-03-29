package net.geant.nmaas.portal.api.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.api.i18n.api.InternationalizationView;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import net.geant.nmaas.portal.persistent.repositories.ConfigurationRepository;
import net.geant.nmaas.portal.persistent.repositories.InternationalizationSimpleRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ConfigurationControllerTest extends BaseControllerTestSetup {

    private static final String URL_PREFIX = "/api/configuration";

    @Autowired
    private ConfigurationRepository repository;

    @Autowired
    private ConfigurationManager configManager;

    @Autowired
    private InternationalizationSimpleRepository intRepo;

    private User user;

    @BeforeEach
    void init() {
        mvc = createMVC();
        user = UsersHelper.ADMIN;
        if(intRepo.findAll().stream().noneMatch(lang -> lang.getLanguage().equalsIgnoreCase("en"))){
            intRepo.save(new InternationalizationView("en", true, "{\"content\":\"content\"}").getAsInternationalizationSimple());
        }
    }

    @AfterEach
    void tearDown() {
        ConfigurationView config = this.configManager.getConfiguration();
        config.setSsoLoginAllowed(false);
        config.setMaintenance(false);
        config.setDefaultLanguage("en");
        this.configManager.updateConfiguration(config.getId(), config);
    }

    @Test
    void shouldAddNewConfiguration() throws Exception {
        repository.deleteAll();
        ConfigurationView configuration = new ConfigurationView(true, false, "en", false, false, new ArrayList<>());
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
    void shouldUpdateConfiguration() throws Exception {
        Long id = repository.findAll().get(0).getId();
        ConfigurationView configuration = new ConfigurationView(true, false, "en", false, false, new ArrayList<>());
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
