package net.geant.nmaas.portal.api.i18n;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.api.i18n.api.InternationalizationBriefView;
import net.geant.nmaas.portal.api.i18n.api.InternationalizationView;
import net.geant.nmaas.portal.persistent.entity.Internationalization;
import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import net.geant.nmaas.portal.persistent.repositories.InternationalizationRepository;
import org.apache.commons.lang.StringUtils;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class InternationalizationControllerTest extends BaseControllerTestSetup {

    @Autowired
    private InternationalizationRepository repository;

    @BeforeEach
    public void setup(){
        this.mvc = createMVC();
        this.repository.deleteAll();
        this.repository.save(Internationalization.builder().language("pl").content("{\"test\":\"test\"}").enabled(true).build());
    }

    @Test
    public void shouldSaveNewLanguage() throws Exception{
        mvc.perform(post("/api/i18n/en?enabled=true")
                .header("Authorization","Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString("{\"test\":\"newtest\"}"))
        ).andExpect(status().isAccepted());
    }

    @Test
    public void shouldUpdateLanguage() throws Exception {
        mvc.perform(patch("/api/i18n/pl")
                .header("Authorization","Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString("{\"test\":\"newtest\"}"))
        ).andExpect(status().isAccepted());
    }

    @Test
    public void shouldGetLanguage() throws Exception {
        MvcResult result = mvc.perform(get("/api/i18n/pl")
                .header("Authorization","Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        InternationalizationView lang = new ObjectMapper().readValue(result.getResponse().getContentAsString(), InternationalizationView.class);
        assertEquals("pl", lang.getLanguage());
    }

    @Test
    public void shouldGetAllSupportedLanguage() throws Exception {
        MvcResult result = mvc.perform(get("/api/i18n/all")
                .header("Authorization","Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
    }

    @Test
    public void shouldDisableLanguage() throws Exception {
        mvc.perform(put("/api/i18n/state")
                .header("Authorization","Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new InternationalizationBriefView(false, "pl")))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    public void shouldGetLanguageContent() throws Exception {
        MvcResult result = mvc.perform(get("/api/i18n/content/pl")
                .header("Authorization","Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
    }

    @Test
    public void shouldGetAllEnabledLanguages() throws Exception {
        MvcResult result = mvc.perform(get("/api/i18n/all/enabled")
                .header("Authorization","Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
    }
}
