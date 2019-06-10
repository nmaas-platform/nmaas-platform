package net.geant.nmaas.portal.api.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Set;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.api.domain.AppConfigurationSpecView;
import net.geant.nmaas.portal.api.domain.AppDeploymentSpec;
import net.geant.nmaas.portal.api.domain.AppDescriptionView;
import net.geant.nmaas.portal.api.domain.ApplicationBriefView;
import net.geant.nmaas.portal.api.domain.ApplicationView;
import net.geant.nmaas.portal.api.domain.ConfigWizardTemplateView;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import net.geant.nmaas.portal.service.ApplicationService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class TagControllerTest extends BaseControllerTestSetup {

    @Autowired
    private ApplicationService appService;

    @Autowired
    private ApplicationRepository appRepository;

    @BeforeEach
    public void setup(){
        this.mvc = createMVC();
        this.appRepository.deleteAll();
        this.appService.create(getDefaultApp("disabledAPP", ApplicationState.DISABLED), "admin");
        this.appService.create(getDefaultApp("deletedAPP", ApplicationState.DELETED), "admin");
    }

    @Test
    public void shouldGetAllApps() throws Exception{
        MvcResult result = mvc.perform(get("/api/tags")
                .header("Authorization","Bearer " + getValidTokenForUser(UsersHelper.ADMIN)))
                .andExpect(status().isOk())
                .andReturn();
        Set<String> resultSet = new ObjectMapper().readValue(result.getResponse().getContentAsByteArray(), Set.class);
        assertTrue(resultSet.contains("tag1"));
    }

    @Test
    public void shouldGetAppByTag() throws Exception {
        MvcResult result = mvc.perform(get("/api/tags/tag1")
                .header("Authorization","Bearer " + getValidTokenForUser(UsersHelper.ADMIN)))
                .andExpect(status().isOk())
                .andReturn();
        Set<ApplicationBriefView> resultSet = new ObjectMapper().readValue(result.getResponse().getContentAsByteArray(), Set.class);
        assertEquals(2, resultSet.size());
    }

    @Test
    public void shouldGetEmptyCollection() throws Exception {
        MvcResult result = mvc.perform(get("/api/tags/deprecated")
                .header("Authorization","Bearer " + getValidTokenForUser(UsersHelper.ADMIN)))
                .andExpect(status().isOk())
                .andReturn();
        Set<ApplicationBriefView> resultSet = new ObjectMapper().readValue(result.getResponse().getContentAsByteArray(), Set.class);
        assertTrue(resultSet.isEmpty());
    }

    private ApplicationView getDefaultApp(String name, ApplicationState state){
        ApplicationView app = new ApplicationView();
        app.setName(name);
        app.setDescriptions(Arrays.asList(new AppDescriptionView("en", "description", "fullDescription")));
        app.setVersion("1.1.0");
        app.setTags(ImmutableSet.of("tag1", "tag2"));
        app.setAppConfigurationSpec(new AppConfigurationSpecView());
        app.setAppDeploymentSpec(new AppDeploymentSpec());
        app.getAppDeploymentSpec().setDefaultStorageSpace(20);
        app.setConfigWizardTemplate(new ConfigWizardTemplateView("config"));
        app.setState(state);
        return app;
    }
}
