package net.geant.nmaas.portal.api.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.api.configuration.model.ConfigurationView;
import net.geant.nmaas.portal.api.i18n.api.I18nDto;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UsersHelper;
import net.geant.nmaas.portal.persistence.repositories.ConfigurationRepository;
import net.geant.nmaas.portal.persistence.repositories.DomainRepository;
import net.geant.nmaas.portal.persistence.repositories.InternationalizationSimpleRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class ConfigurationControllerTest extends BaseControllerTestSetup {

    private static final String URL_PREFIX = "/api/v1/configuration";

    private final ConfigurationRepository repository;
    private final ConfigurationManager configManager;
    private final InternationalizationSimpleRepository intRepo;
    private final DomainRepository domainRepository;

    public ConfigurationControllerTest(@Autowired ConfigurationRepository repository,
                                       @Autowired ConfigurationManager configManager,
                                       @Autowired InternationalizationSimpleRepository intRepo,
                                       @Autowired DomainRepository domainRepository) {
        this.repository = repository;
        this.configManager = configManager;
        this.intRepo = intRepo;
        this.domainRepository = domainRepository;
    }

    private User user;

    @BeforeEach
    void init() {
        mvc = createMVC();
        user = UsersHelper.ADMIN;
        if (intRepo.findAll().stream().noneMatch(lang -> lang.getLanguage().equalsIgnoreCase("en"))) {
            intRepo.save(new I18nDto("en", true, "{\"content\":\"content\"}").getAsInternationalizationSimple());
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
        ConfigurationView configuration = new ConfigurationView(null, true, false, "en", false, false, new ArrayList<>(), true, true, true, "0 */1 * * * ?", 2, 60, 10, "", "0 */1 * * * ?", null, 10);
        mvc.perform(post(URL_PREFIX)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + getValidTokenForUser(user))
                        .content(new ObjectMapper().writeValueAsString(configuration))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        MvcResult mvcResult = mvc.perform(get(URL_PREFIX).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                .andReturn();
        assertThat(mvcResult.getResponse().getContentAsString(), containsString("\"maintenance\":true"));
    }

    @Test
    void shouldUpdateConfiguration() throws Exception {
        Long id = repository.findAll().getFirst().getId();
        ConfigurationView configuration = new ConfigurationView(null, true, false, "en", false, false, new ArrayList<>(), true, true, true, "0 */1 * * * ?", 2, 60, 10, "", "0 */1 * * * ?", null, 10);
        configuration.setId(id);
        mvc.perform(put(URL_PREFIX + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + getValidTokenForUser(user))
                        .content(new ObjectMapper().writeValueAsString(configuration))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        MvcResult mvcResult = mvc.perform(get(URL_PREFIX)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                .andReturn();
        assertThat(mvcResult.getResponse().getContentAsString(), containsString("\"maintenance\":true"));
    }

    @Test
    void shouldUpdateConfigurationWithDefaultSsoUserDomain() throws Exception {
        Domain domain = domainRepository.save(new Domain("name", "codename"));
        Long id = repository.findAll().getFirst().getId();
        ConfigurationView configuration = new ConfigurationView(null, true, false, "en", false, false, new ArrayList<>(), true, true, true, "0 */1 * * * ?", 2, 60, 10, "", "0 */1 * * * ?", null, 10);
        configuration.setId(id);
        configuration.setDefaultDomainForSsoUsers(domain.getId());
        mvc.perform(put(URL_PREFIX + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + getValidTokenForUser(user))
                        .content(new ObjectMapper().writeValueAsString(configuration))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        MvcResult mvcResult = mvc.perform(get(URL_PREFIX)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                .andReturn();
        assertThat(mvcResult.getResponse().getContentAsString(), containsString("\"maintenance\":true"));
    }

}
