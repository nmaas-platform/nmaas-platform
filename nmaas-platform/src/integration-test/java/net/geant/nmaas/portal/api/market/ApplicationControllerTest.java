package net.geant.nmaas.portal.api.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api.KubernetesChartView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api.KubernetesTemplateView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.api.domain.*;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import net.geant.nmaas.portal.persistent.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class ApplicationControllerTest extends BaseControllerTestSetup {

    @Autowired
    private ApplicationBaseRepository appBaseRepo;

    @Autowired
    private ApplicationRepository appRepo;

    @Autowired
    private ApplicationService appService;

    @Autowired
    private ApplicationBaseService appBaseService;

    private ObjectMapper objectMapper;

    @Autowired
    private ModelMapper modelMapper;

    private final static String DEFAULT_APP_NAME = "defApp";

    @BeforeEach
    void setup(){
        this.mvc = createMVC();
        this.objectMapper = new ObjectMapper();
        createDefaultApp();
    }

    @AfterEach
    void tearDown(){
        this.appRepo.deleteAll();
        this.appBaseRepo.deleteAll();
    }

    @Test
    void shouldGetApplications() throws Exception {
        MvcResult result = mvc.perform(get("/api/apps")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
        assertTrue(result.getResponse().getContentAsString().contains(DEFAULT_APP_NAME));
    }

    @Test
    void shouldGetAllApplications() throws Exception {
        MvcResult result = mvc.perform(get("/api/apps/all")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
        assertTrue(result.getResponse().getContentAsString().contains(DEFAULT_APP_NAME));
    }

    @Test
    void shouldAddApplication() throws Exception {
        MvcResult result = mvc.perform(post("/api/apps")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getDefaultAppView("newApp")))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        Id appId = objectMapper.readValue(result.getResponse().getContentAsString(), Id.class);
        assertNotNull(appId);
        assertNotNull(appId.getId());
    }

    @Test
    @Transactional
    public void shouldUpdateApplication() throws Exception {
        mvc.perform(post("/api/apps")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getDefaultAppView("updateApp")))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        ApplicationView app = modelMapper.map(appRepo.findByName("updateApp").get(0), ApplicationView.class);
        mvc.perform(patch("/api/apps")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(app))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void shouldUpdateAppBase() throws Exception {
        mvc.perform(post("/api/apps")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getDefaultAppView("updateApp")))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        ApplicationView app = modelMapper.map(appRepo.findByName("updateApp").get(0), ApplicationView.class);
        mvc.perform(patch("/api/apps/base")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(app))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteApplication() throws Exception {
        long id = appRepo.findAll().get(0).getId();
        mvc.perform(delete("/api/apps/" + id)
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertEquals(ApplicationState.DELETED, appRepo.findAll().get(0).getState());
    }

    @Test
    void shouldGetAppBase() throws Exception {
        long id = appBaseRepo.findAll().get(0).getId();
        MvcResult result = mvc.perform(get("/api/apps/base/" + id)
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ApplicationBriefView app = objectMapper.readValue(result.getResponse().getContentAsString(), ApplicationBriefView.class);
        assertEquals(DEFAULT_APP_NAME, app.getName());
    }

    @Test
    void shouldGetLatestAppVersion() throws Exception{
        MvcResult result = mvc.perform(get("/api/apps/" + DEFAULT_APP_NAME + "/latest")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ApplicationView app = objectMapper.readValue(result.getResponse().getContentAsString(), ApplicationView.class);
        assertEquals(DEFAULT_APP_NAME, app.getName());
        assertEquals("1.1.0", app.getVersion());

    }

    @Test
    void shouldGetApp() throws Exception {
        long id = appRepo.findAll().get(0).getId();
        MvcResult result = mvc.perform(get("/api/apps/" + id)
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ApplicationView app = objectMapper.readValue(result.getResponse().getContentAsString(), ApplicationView.class);
        assertEquals(DEFAULT_APP_NAME, app.getName());
        assertEquals("1.1.0", app.getVersion());

        assertTrue(result.getResponse().getContentAsString().contains("name1"));
        assertTrue(result.getResponse().getContentAsString().contains("name2"));
        assertTrue(result.getResponse().getContentAsString().contains("name3"));
        assertTrue(result.getResponse().getContentAsString().contains("tag1"));
        assertTrue(result.getResponse().getContentAsString().contains("tag2"));
        assertTrue(result.getResponse().getContentAsString().contains("tag3"));
    }

    @Test
    @Transactional
    public void shouldChangeAppState() throws Exception {
        long id = appRepo.findAll().get(0).getId();
        mvc.perform(patch("/api/apps/state/" + id)
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .content(objectMapper.writeValueAsString(new ApplicationStateChangeRequest(ApplicationState.DISABLED, "reason")))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertEquals(ApplicationState.DISABLED, appRepo.getOne(id).getState());
    }

    private void createDefaultApp(){
        ApplicationView app1Request = getDefaultAppView(DEFAULT_APP_NAME);
        Application app = this.appService.create(app1Request, "admin");
        app1Request.setId(app.getId());
        appBaseService.createApplicationOrAddNewVersion(app1Request);
    }

    private ApplicationView getDefaultAppView(String name){
        List<AppAccessMethodView> lst = new ArrayList<>();
        lst.add(new AppAccessMethodView(ServiceAccessMethodType.DEFAULT, "name1", "tag1", new HashMap<>()));
        lst.add(new AppAccessMethodView(ServiceAccessMethodType.EXTERNAL, "name2", "tag2", new HashMap<>()));
        lst.add(new AppAccessMethodView(ServiceAccessMethodType.INTERNAL, "name3", "tag3", new HashMap<>()));
        ApplicationView applicationView = new ApplicationView();
        applicationView.setName(name);
        applicationView.setVersion("1.1.0");
        applicationView.setOwner("admin");
        applicationView.setState(ApplicationState.ACTIVE);
        applicationView.setDescriptions(Collections.singletonList(new AppDescriptionView("en", "test", "testfull")));
        AppDeploymentSpecView appDeploymentSpec = new AppDeploymentSpecView();
        appDeploymentSpec.setKubernetesTemplate(new KubernetesTemplateView(new KubernetesChartView("name", "version"), "archive"));
        appDeploymentSpec.setDefaultStorageSpace(10);
        appDeploymentSpec.setAccessMethods(lst);
        applicationView.setAppDeploymentSpec(appDeploymentSpec);
        applicationView.setConfigWizardTemplate(new ConfigWizardTemplateView("{}"));
        applicationView.setAppConfigurationSpec(new AppConfigurationSpecView());
        applicationView.getAppConfigurationSpec().setConfigFileRepositoryRequired(false);
        return applicationView;
    }
}
