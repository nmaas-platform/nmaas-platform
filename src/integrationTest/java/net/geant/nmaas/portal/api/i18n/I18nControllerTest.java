package net.geant.nmaas.portal.api.i18n;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.api.i18n.api.I18nBaseDto;
import net.geant.nmaas.portal.api.i18n.api.I18nDto;
import net.geant.nmaas.portal.persistence.entity.UsersHelper;
import net.geant.nmaas.portal.persistence.repositories.InternationalizationSimpleRepository;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class I18nControllerTest extends BaseControllerTestSetup {

    @Autowired
    private InternationalizationSimpleRepository repository;

    @BeforeEach
    void setup() {
        this.mvc = createMVC();
        this.repository.save(new I18nDto("pl", true, "{\"content\":\"content\"}").getAsInternationalizationSimple());
    }

    @AfterEach
    void tearDown() {
        this.repository.findAll().stream()
                .filter(lang -> !lang.getLanguage().equalsIgnoreCase("en"))
                .forEach(lang -> repository.delete(lang));
    }

    @Test
    void shouldSaveNewLanguage() {
        assertDoesNotThrow(() -> {
            mvc.perform(post("/api/i18n/de?enabled=true")
                    .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString("{\"test\":\"newtest\"}"))
            ).andExpect(status().isAccepted());
        });
    }

    @Test
    void shouldUpdateLanguage() {
        assertDoesNotThrow(() -> {
            mvc.perform(patch("/api/i18n/pl")
                    .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString("{\"test\":\"newtest\"}"))
            ).andExpect(status().isAccepted());
        });
    }

    @Test
    void shouldGetLanguage() throws Exception {
        MvcResult result = mvc.perform(get("/api/i18n/pl")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        I18nDto lang = new ObjectMapper().readValue(result.getResponse().getContentAsString(), I18nDto.class);
        assertEquals("pl", lang.getLanguage());
    }

    @Test
    void shouldGetAllSupportedLanguage() throws Exception {
        MvcResult result = mvc.perform(get("/api/i18n/all")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
    }

    @Test
    void shouldDisableLanguage() {
        assertDoesNotThrow(() -> {
            mvc.perform(put("/api/i18n/state")
                            .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(new I18nBaseDto(false, "pl")))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent())
                    .andReturn();
        });
    }

    @Test
    void shouldGetLanguageContent() throws Exception {
        MvcResult result = mvc.perform(get("/api/i18n/content/pl")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
    }

    @Test
    void shouldGetAllEnabledLanguages() throws Exception {
        MvcResult result = mvc.perform(get("/api/i18n/all/enabled")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
    }

}
