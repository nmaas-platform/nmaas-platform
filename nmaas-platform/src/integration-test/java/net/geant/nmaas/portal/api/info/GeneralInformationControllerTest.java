package net.geant.nmaas.portal.api.info;

import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import org.apache.commons.lang.StringUtils;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class GeneralInformationControllerTest extends BaseControllerTestSetup {

    @BeforeEach
    public void setup(){
        this.mvc = this.createMVC();
    }

    @Test
    public void shouldReturnGitProperties() throws Exception {
        MvcResult result = mvc.perform(get("/api/info/git"))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains("commitName"));
    }

    @Test
    public void shouldReturnChangelog() throws Exception{
        MvcResult result = mvc.perform(get("/api/info/changelog"))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
    }

}
