package net.geant.nmaas.portal.api.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.configuration.entities.AppConfigurationSpec;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api.HelmChartRepositoryView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api.KubernetesChartView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api.KubernetesTemplateView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.HelmChartRepositoryEmbeddable;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesChart;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceStorageVolumeType;
import net.geant.nmaas.orchestration.entities.AppAccessMethod;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.AppStorageVolume;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.api.domain.AppAccessMethodView;
import net.geant.nmaas.portal.api.domain.AppConfigurationSpecView;
import net.geant.nmaas.portal.api.domain.AppDeploymentSpecView;
import net.geant.nmaas.portal.api.domain.AppStorageVolumeView;
import net.geant.nmaas.portal.api.domain.ApplicationBaseView;
import net.geant.nmaas.portal.api.domain.ApplicationDTO;
import net.geant.nmaas.portal.api.domain.ApplicationStateChangeRequest;
import net.geant.nmaas.portal.api.domain.ApplicationView;
import net.geant.nmaas.portal.api.domain.ConfigFileTemplateView;
import net.geant.nmaas.portal.api.domain.ConfigWizardTemplateView;
import net.geant.nmaas.portal.api.domain.Id;
import net.geant.nmaas.portal.persistent.entity.AppDescription;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import net.geant.nmaas.portal.persistent.entity.ApplicationVersion;
import net.geant.nmaas.portal.persistent.entity.ConfigWizardTemplate;
import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import net.geant.nmaas.portal.persistent.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    private final static String APP_1_NAME = "testApp1";
    private final static String APP_2_NAME = "testApp2";

    private ApplicationBase testApp1Base;
    private Application testApp1;

    private ApplicationBase testApp2Base;
    private Application testApp2;

    @BeforeEach
    void setup() {
        this.mvc = createMVC();
        this.objectMapper = new ObjectMapper();

        this.testApp1Base = this.applicationBaseService.create(getDefaultApplicationBase(APP_1_NAME));
        this.testApp1 = this.applicationService.create(getDefaultApplication(APP_1_NAME, "1.1.0", ApplicationState.ACTIVE));
        this.testApp1Base.getVersions().add(
                new ApplicationVersion(this.testApp1.getVersion(), this.testApp1.getState(), this.testApp1.getId())
        );
        this.testApp1Base = this.applicationBaseService.update(this.testApp1Base);

        this.testApp2Base = this.applicationBaseService.create(getDefaultApplicationBase(APP_2_NAME));
        this.testApp2 = this.applicationService.create(getDefaultApplication(APP_2_NAME, "2.0.0", ApplicationState.DISABLED));
        this.testApp2Base.getVersions().add(
                new ApplicationVersion(this.testApp2.getVersion(), this.testApp2.getState(), this.testApp2.getId())
        );
        this.testApp2Base = this.applicationBaseService.update(this.testApp2Base);
    }

    @AfterEach
    void tearDown() {
        this.applicationRepository.deleteAll();
        this.applicationBaseRepository.deleteAll();
    }

    @Test
    void shouldGetActiveApplications() throws Exception {
        MvcResult result = mvc.perform(get("/api/apps/base")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ApplicationBaseView[] resultView = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ApplicationBaseView[].class);
        assertEquals(1, resultView.length);
        assertEquals(APP_1_NAME, resultView[0].getName());
    }

    @Test
    void shouldGetAllApplications() throws Exception {
        MvcResult result = mvc.perform(get("/api/apps/base/all")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ApplicationBaseView[] resultView = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ApplicationBaseView[].class);
        assertEquals(2, resultView.length);
        assertTrue(Arrays.stream(resultView).anyMatch(app -> app.getName().equals(APP_1_NAME)));
        assertTrue(Arrays.stream(resultView).anyMatch(app -> app.getName().equals(APP_2_NAME)));
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
        ApplicationView applicationView = modelMapper.map(this.testApp1, ApplicationView.class);
        applicationView.setConfigWizardTemplate(new ConfigWizardTemplateView(null, "{}"));

        mvc.perform(patch("/api/apps/version")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                modelMapper.map(this.testApp1, ApplicationView.class)
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
                                    modelMapper.map(testApp1Base, ApplicationBase.class)
                            ))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        });
    }

    @Test
    void shouldDeleteApplication() throws Exception {
        long id = this.testApp1.getId();
        mvc.perform(delete("/api/apps/" + id)
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertEquals(ApplicationState.DELETED, applicationRepository.findAll().get(0).getState());
    }

    @Test
    void shouldGetAppBase() throws Exception {
        long id = this.testApp1Base.getId();
        MvcResult result = mvc.perform(get("/api/apps/base/" + id)
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ApplicationBaseView app = objectMapper.readValue(result.getResponse().getContentAsString(), ApplicationBaseView.class);
        assertEquals(APP_1_NAME, app.getName());
    }

    @Test
    void shouldGetLatestAppVersion() throws Exception {
        this.applicationService.create(getDefaultApplication(APP_1_NAME, "1.3.0", ApplicationState.DISABLED));
        this.applicationService.create(getDefaultApplication(APP_1_NAME, "1.2.0", ApplicationState.ACTIVE));
        MvcResult result = mvc.perform(get("/api/apps/" + APP_1_NAME + "/latest")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ApplicationDTO app = objectMapper.readValue(result.getResponse().getContentAsString(), ApplicationDTO.class);
        assertEquals(APP_1_NAME, app.getApplicationBase().getName());
        assertEquals("1.2.0", app.getApplication().getVersion());
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
        assertEquals(APP_1_NAME, app.getApplicationBase().getName());
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
        long id = this.testApp1.getId();
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
                        new HelmChartRepositoryView("tooLongNameToMatchTheConstraint", "http://test")
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
                .name(APP_1_NAME)
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

    private ApplicationBase getDefaultApplicationBase(String name) {
        ApplicationBase applicationBase = new ApplicationBase(null, name);
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

    private Application getDefaultApplication(String name, String version, ApplicationState state) {
        List<AppStorageVolume> svList = new ArrayList<>();
        svList.add(new AppStorageVolume(null, ServiceStorageVolumeType.MAIN, 5, new HashMap<>()));
        List<AppAccessMethod> mvList = new ArrayList<>();
        mvList.add(new AppAccessMethod(null, ServiceAccessMethodType.DEFAULT, "name1", "tag1", new HashMap<>()));
        mvList.add(new AppAccessMethod(null, ServiceAccessMethodType.EXTERNAL, "name2", "tag2", new HashMap<>()));
        mvList.add(new AppAccessMethod(null, ServiceAccessMethodType.INTERNAL, "name3", "tag3", new HashMap<>()));
        Application application = new Application();
        application.setName(name);
        application.setVersion(version);
        application.setState(state);
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
