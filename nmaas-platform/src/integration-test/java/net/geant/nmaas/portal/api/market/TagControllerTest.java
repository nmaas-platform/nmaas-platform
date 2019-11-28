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
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import net.geant.nmaas.portal.persistent.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import net.geant.nmaas.portal.persistent.repositories.DomainRepository;
import net.geant.nmaas.portal.persistent.repositories.TagRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationService;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MvcResult;

import javax.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
public class TagControllerTest extends BaseControllerTestSetup {

    @Autowired
    private ApplicationService appService;

    @Autowired
    private ApplicationBaseService appBaseService;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ApplicationRepository appRepository;

    @Autowired
    private ApplicationBaseRepository appBaseRepo;

    @Autowired
    private DomainRepository domainRepository;

    @BeforeEach
    public void setup(){
        this.mvc = createMVC();

        Domain domain = new Domain("Domain1", "d1");
        domainRepository.save(domain);

        ApplicationView app1Request = getDefaultApp("disabledAPP", ApplicationState.DISABLED);
        ApplicationView app2Request = getDefaultApp("deletedAPP", ApplicationState.DELETED);
        Application app = this.appService.create(app1Request, "admin");
        Application app2 = this.appService.create(app2Request, "admin");
        app1Request.setId(app.getId());
        app2Request.setId(app2.getId());

        appBaseService.createApplicationOrAddNewVersion(app1Request);
        appBaseService.createApplicationOrAddNewVersion(app2Request);
    }

    @AfterEach
    public void teardown(){
        this.domainRepository.deleteAll();
        this.appRepository.deleteAll();
        this.appBaseRepo.deleteAll();
        this.tagRepository.deleteAll();
    }

    @Test
//    @Transactional
    public void shouldGetAllApps() throws Exception{
        MvcResult result = mvc.perform(get("/api/tags")
                .header("Authorization","Bearer " + getValidTokenForUser(UsersHelper.ADMIN)))
                .andExpect(status().isOk())
                .andReturn();
        Set<String> resultSet = new ObjectMapper().readValue(result.getResponse().getContentAsByteArray(), Set.class);
        assertTrue(resultSet.contains("tag1"));
    }

    @Test
//    @Transactional
    public void shouldGetAppByTag() throws Exception {
        MvcResult result = mvc.perform(get("/api/tags/tag1")
                .header("Authorization","Bearer " + getValidTokenForUser(UsersHelper.ADMIN)))
                .andExpect(status().isOk())
                .andReturn();
        Set<ApplicationBriefView> resultSet = new ObjectMapper().readValue(result.getResponse().getContentAsByteArray(), Set.class);
        assertEquals(2, resultSet.size());
    }

    @Test
//    @Transactional
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
