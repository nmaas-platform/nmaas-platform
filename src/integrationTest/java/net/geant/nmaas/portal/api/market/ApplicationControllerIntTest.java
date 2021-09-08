package net.geant.nmaas.portal.api.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.configuration.entities.AppConfigurationSpec;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api.HelmChartRepositoryView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api.KubernetesChartView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api.KubernetesTemplateView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.*;
import net.geant.nmaas.orchestration.entities.AppAccessMethod;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.AppStorageVolume;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.api.domain.*;
import net.geant.nmaas.portal.persistent.entity.*;
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

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Log4j2
class ApplicationControllerIntTest extends BaseControllerTestSetup {

    @Autowired
    private ApplicationBaseRepository applicationBaseRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationBaseService applicationBaseService;

    private ObjectMapper objectMapper;

    @Autowired
    private ModelMapper modelMapper;

    private final static String DEFAULT_APP_NAME = "test";

    private ApplicationBase defaultAppBase;
    private Application defaultApplication;

    @BeforeEach
    void setup() {
        this.mvc = createMVC();
        this.objectMapper = new ObjectMapper();

        this.defaultAppBase = this.applicationBaseService.create(getDefaultApplicationBase());
        this.defaultApplication = this.applicationService.create(getDefaultApplication());
        this.defaultAppBase.getVersions().add(
                new ApplicationVersion(this.defaultApplication.getVersion(), this.defaultApplication.getState(), this.defaultApplication.getId())
        );
        this.defaultAppBase = this.applicationBaseService.update(this.defaultAppBase);
    }

    @AfterEach
    void tearDown() {
        this.applicationRepository.deleteAll();
        this.applicationBaseRepository.deleteAll();
    }

    @Test
    void shouldGetApplications() throws Exception {
        MvcResult result = mvc.perform(get("/api/apps/base")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
        assertTrue(result.getResponse().getContentAsString().contains(DEFAULT_APP_NAME));
    }

    @Test
    void shouldGetAllApplications() throws Exception {
        MvcResult result = mvc.perform(get("/api/apps/base/all")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(result.getResponse().getContentAsString()));
        assertTrue(result.getResponse().getContentAsString().contains(DEFAULT_APP_NAME));
    }

    @Test
    void shouldAddApplication() throws Exception {
        ApplicationBase newApplicationBase = new ApplicationBase(null, "new");
        newApplicationBase.setDescriptions(Collections.singletonList(
                new AppDescription(null, "en", "Description", "Full description")
        ));

        MvcResult result = mvc.perform(post("/api/apps")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ApplicationDTO(
                                        modelMapper.map(newApplicationBase, ApplicationBaseView.class),
                                        modelMapper.map(getNewApplication(newApplicationBase.getName(), "1.2.3"), ApplicationView.class)
                                )
                        ))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        Id appId = objectMapper.readValue(result.getResponse().getContentAsString(), Id.class);
        assertNotNull(appId);
        assertNotNull(appId.getId());
    }

    @Test
    public void shouldUpdateApplicationVersion() throws Exception {
        ApplicationView applicationView = modelMapper.map(this.defaultApplication, ApplicationView.class);
        applicationView.setConfigWizardTemplate(new ConfigWizardTemplateView(null, "{}"));

        mvc.perform(patch("/api/apps/version")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                modelMapper.map(this.defaultApplication, ApplicationView.class)
                        ))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // simulate bug from NMAAS-844
        applicationView.getAppDeploymentSpec().getAccessMethods().iterator().next().getDeployParameters().putIfAbsent("NEW.PARAM", "value");
        applicationView.getAppDeploymentSpec().getStorageVolumes().iterator().next().getDeployParameters().putIfAbsent("NEW.PARAM", "value");

        applicationView.getAppDeploymentSpec().getAccessMethods().add(new AppAccessMethodView(null, ServiceAccessMethodType.DEFAULT, "name4", "tag4", new HashMap<>()));
        applicationView.getAppDeploymentSpec().getAccessMethods().add(new AppAccessMethodView(null, ServiceAccessMethodType.DEFAULT, "name5", "tag5", new HashMap<>()));
        applicationView.getAppDeploymentSpec().getStorageVolumes().add(new AppStorageVolumeView(null, ServiceStorageVolumeType.SHARED, 5, new HashMap<>()));
        applicationView.getAppDeploymentSpec().getStorageVolumes().add(new AppStorageVolumeView(null, ServiceStorageVolumeType.SHARED, 5, new HashMap<>()));

        mvc.perform(patch("/api/apps/version")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applicationView))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

//        Application test = applicationRepository.findByName(applicationView.getName()).get(0);

        MvcResult result = mvc.perform(get("/api/apps/version/" + applicationView.getId())
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ApplicationView test = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ApplicationView.class);

        assertEquals(applicationView.getAppDeploymentSpec().getStorageVolumes().size(), test.getAppDeploymentSpec().getStorageVolumes().size());
        assertEquals(applicationView.getAppDeploymentSpec().getAccessMethods().size(), test.getAppDeploymentSpec().getAccessMethods().size());
    }

    @Test
    public void shouldUpdateAppBase() {
        assertDoesNotThrow(() -> {
            mvc.perform(patch("/api/apps/base")
                            .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    modelMapper.map(defaultAppBase, ApplicationBase.class)
                            ))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        });
    }

    @Test
    void shouldDeleteApplication() throws Exception {
        long id = this.defaultApplication.getId();
        mvc.perform(delete("/api/apps/" + id)
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertEquals(ApplicationState.DELETED, applicationRepository.findAll().get(0).getState());
    }

    @Test
    void shouldGetAppBase() throws Exception {
        long id = this.defaultAppBase.getId();
        MvcResult result = mvc.perform(get("/api/apps/base/" + id)
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ApplicationBaseView app = objectMapper.readValue(result.getResponse().getContentAsString(), ApplicationBaseView.class);
        assertEquals(DEFAULT_APP_NAME, app.getName());
    }

    @Test
    void shouldGetLatestAppVersion() throws Exception {
        MvcResult result = mvc.perform(get("/api/apps/" + DEFAULT_APP_NAME + "/latest")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ApplicationDTO app = objectMapper.readValue(result.getResponse().getContentAsString(), ApplicationDTO.class);
        assertEquals(DEFAULT_APP_NAME, app.getApplicationBase().getName());
        assertEquals("1.1.0", app.getApplication().getVersion());
    }

    @Test
    void shouldGetApp() throws Exception {
        long id = applicationRepository.findAll().get(0).getId();
        MvcResult result = mvc.perform(get("/api/apps/" + id)
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ApplicationDTO app = objectMapper.readValue(result.getResponse().getContentAsString(), ApplicationDTO.class);
        assertEquals(DEFAULT_APP_NAME, app.getApplicationBase().getName());
        assertEquals("1.1.0", app.getApplication().getVersion());

        assertEquals(3, app.getApplication().getAppDeploymentSpec().getAccessMethods().size());

        assertTrue(result.getResponse().getContentAsString().contains("name1"));
        assertTrue(result.getResponse().getContentAsString().contains("name2"));
        assertTrue(result.getResponse().getContentAsString().contains("name3"));
        assertTrue(result.getResponse().getContentAsString().contains("tag1"));
        assertTrue(result.getResponse().getContentAsString().contains("tag2"));
        assertTrue(result.getResponse().getContentAsString().contains("tag3"));
    }

    @Test
    public void shouldChangeAppState() throws Exception {
        long id = this.defaultApplication.getId();
        mvc.perform(patch("/api/apps/state/" + id)
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .content(objectMapper.writeValueAsString(new ApplicationStateChangeRequest(ApplicationState.DISABLED, "reason")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        MvcResult result = mvc.perform(get("/api/apps/version/" + id)
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ApplicationView applicationView = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ApplicationView.class);
        assertEquals(ApplicationState.DISABLED, applicationView.getState());
    }

    @Test
    public void shouldAddNewVersion() {
        AppDeploymentSpecView appDeploymentSpec = new AppDeploymentSpecView();
        appDeploymentSpec.setKubernetesTemplate(
                new KubernetesTemplateView(
                        null,
                        new KubernetesChartView(null, "name", "version"),
                        "archive",
                        null,
                        new HelmChartRepositoryView("test", "http://test")
                )
        );
        appDeploymentSpec.setStorageVolumes(new ArrayList<>());
        appDeploymentSpec.getStorageVolumes().add(new AppStorageVolumeView(null, ServiceStorageVolumeType.MAIN, 5, new HashMap<>()));
        appDeploymentSpec.setAccessMethods(new ArrayList<>());
        appDeploymentSpec.getAccessMethods().add(new AppAccessMethodView(null, ServiceAccessMethodType.DEFAULT, "name1", "tag1", new HashMap<>()));
        appDeploymentSpec.getAccessMethods().add(new AppAccessMethodView(null, ServiceAccessMethodType.EXTERNAL, "name2", "tag2", new HashMap<>()));
        appDeploymentSpec.getAccessMethods().add(new AppAccessMethodView(null, ServiceAccessMethodType.INTERNAL, "name3", "tag3", new HashMap<>()));

        AppConfigurationSpecView appConfigurationSpec = new AppConfigurationSpecView();
        appConfigurationSpec.getTemplates().add(new ConfigFileTemplateView(null, null, "name", "dir", "content"));

        ApplicationView view = ApplicationView.builder()
                .name(DEFAULT_APP_NAME)
                .version("3.0.0")
                .appConfigurationSpec(appConfigurationSpec)
                .appDeploymentSpec(appDeploymentSpec)
                .configWizardTemplate(new ConfigWizardTemplateView(null, "{}"))
                .configUpdateWizardTemplate(null)
                .build();

        assertDoesNotThrow(() -> {
            mvc.perform(post("/api/apps/version")
                            .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(view))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());
        });
    }

    private ApplicationBase getDefaultApplicationBase() {
        ApplicationBase applicationBase = new ApplicationBase(null, "test");
        applicationBase.setOwner("admin");
        applicationBase.setLicense("");
        applicationBase.setLicenseUrl("");
        applicationBase.setSourceUrl("");
        applicationBase.setIssuesUrl("");
        applicationBase.setNmaasDocumentationUrl("");
        applicationBase.setWwwUrl("");
        applicationBase.setDescriptions(Collections.singletonList(
                new AppDescription(null, "en", "Description", "Full description")
        ));
        applicationBase.setVersions(new HashSet<>());
        applicationBase.setComments(new ArrayList<>());
        applicationBase.setScreenshots(new ArrayList<>());
        applicationBase.setLogo(null);
        applicationBase.setTags(new HashSet<>());
        return applicationBase;
    }

    private Application getNewApplication(String name, String version) {
        List<AppStorageVolume> svList = new ArrayList<>();
        svList.add(new AppStorageVolume(null, ServiceStorageVolumeType.MAIN, 5, new HashMap<>()));
        List<AppAccessMethod> mvList = new ArrayList<>();
        mvList.add(new AppAccessMethod(null, ServiceAccessMethodType.DEFAULT, "name1", "tag1", new HashMap<>()));
        mvList.add(new AppAccessMethod(null, ServiceAccessMethodType.EXTERNAL, "name2", "tag2", new HashMap<>()));
        mvList.add(new AppAccessMethod(null, ServiceAccessMethodType.INTERNAL, "name3", "tag3", new HashMap<>()));
        Application application = new Application();
        application.setName(name);
        application.setVersion(version);
        AppDeploymentSpec appDeploymentSpec = new AppDeploymentSpec();
        appDeploymentSpec.setKubernetesTemplate(
                new KubernetesTemplate(
                        null,
                        new KubernetesChart(null, "name", "version"),
                        "archive",
                        null,
                        new HelmChartRepositoryEmbeddable("test", "http://test")
                )
        );
        appDeploymentSpec.setStorageVolumes(new HashSet<>(svList));
        appDeploymentSpec.setAccessMethods(new HashSet<>(mvList));
        application.setAppDeploymentSpec(appDeploymentSpec);
        application.setConfigWizardTemplate(new ConfigWizardTemplate(null, "{}"));
        application.setAppConfigurationSpec(new AppConfigurationSpec());
        application.getAppConfigurationSpec().setConfigFileRepositoryRequired(false);
        return application;
    }

    private Application getDefaultApplication() {
        List<AppStorageVolume> svList = new ArrayList<>();
        svList.add(new AppStorageVolume(null, ServiceStorageVolumeType.MAIN, 5, new HashMap<>()));
        List<AppAccessMethod> mvList = new ArrayList<>();
        mvList.add(new AppAccessMethod(null, ServiceAccessMethodType.DEFAULT, "name1", "tag1", new HashMap<>()));
        mvList.add(new AppAccessMethod(null, ServiceAccessMethodType.EXTERNAL, "name2", "tag2", new HashMap<>()));
        mvList.add(new AppAccessMethod(null, ServiceAccessMethodType.INTERNAL, "name3", "tag3", new HashMap<>()));
        Application application = new Application();
        application.setName("test");
        application.setVersion("1.1.0");
        application.setState(ApplicationState.ACTIVE);
        application.setCreationDate(LocalDateTime.now());
        AppDeploymentSpec appDeploymentSpec = new AppDeploymentSpec();
        appDeploymentSpec.setKubernetesTemplate(
                new KubernetesTemplate(
                        null,
                        new KubernetesChart(null, "name", "version"),
                        "archive",
                        null,
                        new HelmChartRepositoryEmbeddable("test", "http://test")
                )
        );
        appDeploymentSpec.setStorageVolumes(new HashSet<>(svList));
        appDeploymentSpec.setAccessMethods(new HashSet<>(mvList));
        application.setAppDeploymentSpec(appDeploymentSpec);
        application.setConfigWizardTemplate(new ConfigWizardTemplate(null, "{}"));
        application.setAppConfigurationSpec(new AppConfigurationSpec());
        application.getAppConfigurationSpec().setConfigFileRepositoryRequired(false);
        return application;
    }

}
