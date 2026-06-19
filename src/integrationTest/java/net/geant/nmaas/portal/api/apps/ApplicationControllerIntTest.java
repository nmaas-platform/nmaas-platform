package net.geant.nmaas.portal.api.apps;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.api.dto.Id;
import net.geant.nmaas.api.dto.applications.AppAccessMethodDto;
import net.geant.nmaas.api.dto.applications.AppConfigurationSpecDto;
import net.geant.nmaas.api.dto.applications.AppDeploymentSpecDto;
import net.geant.nmaas.api.dto.applications.AppStorageVolumeDto;
import net.geant.nmaas.api.dto.applications.ApplicationBaseDto;
import net.geant.nmaas.api.dto.applications.ApplicationBaseInfoDto;
import net.geant.nmaas.api.dto.applications.ApplicationDto;
import net.geant.nmaas.api.dto.applications.ApplicationStateChangeRequest;
import net.geant.nmaas.api.dto.applications.ApplicationStateDto;
import net.geant.nmaas.api.dto.applications.ConfigFileTemplateDto;
import net.geant.nmaas.api.dto.applications.ConfigWizardTemplateDto;
import net.geant.nmaas.api.dto.applications.HelmChartRepositoryDto;
import net.geant.nmaas.api.dto.applications.KubernetesChartDto;
import net.geant.nmaas.api.dto.applications.KubernetesTemplateDto;
import net.geant.nmaas.api.dto.applications.ServiceAccessMethodTypeDto;
import net.geant.nmaas.api.dto.applications.ServiceStorageVolumeTypeDto;
import net.geant.nmaas.nmservice.configuration.entities.AppConfigurationSpec;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.HelmChartRepositoryEmbeddable;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesChart;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceStorageVolumeType;
import net.geant.nmaas.orchestration.entities.AppAccessMethod;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.AppStorageVolume;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.api.configuration.TestCacheConfig;
import net.geant.nmaas.portal.persistence.entity.AppDescription;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.ApplicationState;
import net.geant.nmaas.portal.persistence.entity.ApplicationVersion;
import net.geant.nmaas.portal.persistence.entity.ConfigWizardTemplate;
import net.geant.nmaas.portal.persistence.entity.UsersHelper;
import net.geant.nmaas.portal.persistence.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistence.repositories.ApplicationRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

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

@SpringBootTest(properties = "spring.cache.type=none")
@Slf4j
@Import(TestCacheConfig.class)
class ApplicationControllerIntTest extends BaseControllerTestSetup {

    private final ApplicationBaseRepository applicationBaseRepository;

    private final ApplicationRepository applicationRepository;

    private final ApplicationService applicationService;

    private final ApplicationBaseService applicationBaseService;

    private final ModelMapper modelMapper;

    private final ObjectMapper objectMapper;

    public ApplicationControllerIntTest(@Autowired ApplicationBaseRepository applicationBaseRepository, @Autowired ApplicationRepository applicationRepository,
                                        @Autowired ApplicationService applicationService, @Autowired ApplicationBaseService applicationBaseService,
                                        @Autowired ModelMapper modelMapper, @Autowired ObjectMapper objectMapper) {
        this.applicationBaseRepository = applicationBaseRepository;
        this.applicationRepository = applicationRepository;
        this.applicationService = applicationService;
        this.applicationBaseService = applicationBaseService;
        this.modelMapper = modelMapper;
        this.objectMapper = objectMapper;
    }

    private static final String APP_1_NAME = "testApp1";
    private static final String APP_2_NAME = "testApp2";

    private ApplicationBase testApp1Base;
    private Application testApp1;

    @BeforeEach
    void setup() {
        this.mvc = createMVC();

        this.testApp1Base = this.applicationBaseService.create(getDefaultApplicationBase(APP_1_NAME));
        this.testApp1 = this.applicationService.create(getDefaultApplication(APP_1_NAME, "1.1.0", ApplicationState.ACTIVE));
        this.testApp1Base.getVersions().addAll(
                List.of(
                        new ApplicationVersion(this.testApp1.getVersion(), this.testApp1.getState(), this.testApp1.getId()),
                        new ApplicationVersion("1.1.1", ApplicationState.ACTIVE,
                                this.applicationService.create(getDefaultApplication(APP_1_NAME, "1.1.1", ApplicationState.ACTIVE)).getId()),
                        new ApplicationVersion("1.1.2", ApplicationState.DISABLED,
                                this.applicationService.create(getDefaultApplication(APP_1_NAME, "1.1.2", ApplicationState.DISABLED)).getId())
                )
        );
        this.testApp1Base = this.applicationBaseService.update(this.testApp1Base);

        ApplicationBase testApp2Base = this.applicationBaseService.create(getDefaultApplicationBase(APP_2_NAME));
        Application testApp2 = this.applicationService.create(getDefaultApplication(APP_2_NAME, "2.0.0", ApplicationState.DISABLED));
        testApp2Base.getVersions().add(new ApplicationVersion(testApp2.getVersion(), testApp2.getState(), testApp2.getId()));
        testApp2Base = this.applicationBaseService.update(testApp2Base);
    }

    @AfterEach
    void tearDown() {
        this.applicationRepository.deleteAll();
        this.applicationBaseRepository.deleteAll();
    }

    @Test
    @CacheEvict(value = "applicationBaseS", allEntries = true)
    public void shouldGetActiveApplications() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/apps/base")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ApplicationBaseInfoDto[] resultView = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ApplicationBaseInfoDto[].class);
        assertEquals(1, resultView.length);
        assertEquals(APP_1_NAME, resultView[0].getName());
    }

    @Test
    void shouldGetAllApplications() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/apps/base/all")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ApplicationBaseDto[] resultView = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ApplicationBaseDto[].class);
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

        MvcResult result = mvc.perform(post("/api/v1/apps")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ApplicationController.ApplicationCompleteView(
                                        modelMapper.map(newApplicationBase, ApplicationBaseDto.class),
                                        modelMapper.map(getNewApplication(newApplicationBase.getName(), "1.2.3"), ApplicationDto.class)
                                )
                        ))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        Id appId = objectMapper.readValue(result.getResponse().getContentAsString(), Id.class);
        assertNotNull(appId);
        assertNotNull(appId.id());
    }

    @Test
    void shouldUpdateApplicationVersion() throws Exception {
        ApplicationDto applicationView = modelMapper.map(this.testApp1, ApplicationDto.class);
        applicationView.setConfigWizardTemplate(new ConfigWizardTemplateDto(null, "{}"));

        mvc.perform(patch("/api/v1/apps/version")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                modelMapper.map(this.testApp1, ApplicationDto.class)
                        ))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // simulate bug from NMAAS-844
        applicationView.getAppDeploymentSpec().getAccessMethods().getFirst().getDeployParameters().putIfAbsent("NEW.PARAM", "value");
        applicationView.getAppDeploymentSpec().getStorageVolumes().getFirst().getDeployParameters().putIfAbsent("NEW.PARAM", "value");

        applicationView.getAppDeploymentSpec().getAccessMethods()
                .add(new AppAccessMethodDto(null, ServiceAccessMethodTypeDto.DEFAULT, "name4", "tag4", null, null, null));
        applicationView.getAppDeploymentSpec().getAccessMethods()
                .add(new AppAccessMethodDto(null, ServiceAccessMethodTypeDto.DEFAULT, "name5", "tag5", null, null, null));
        applicationView.getAppDeploymentSpec().getStorageVolumes()
                .add(new AppStorageVolumeDto(null, ServiceStorageVolumeTypeDto.SHARED, 5, new HashMap<>()));
        applicationView.getAppDeploymentSpec().getStorageVolumes()
                .add(new AppStorageVolumeDto(null, ServiceStorageVolumeTypeDto.SHARED, 5, new HashMap<>()));

        mvc.perform(patch("/api/v1/apps/version")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applicationView))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        MvcResult result = mvc.perform(get("/api/v1/apps/version/" + applicationView.getId())
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ApplicationDto test = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ApplicationDto.class);

        assertEquals(applicationView.getAppDeploymentSpec().getStorageVolumes().size(), test.getAppDeploymentSpec().getStorageVolumes().size());
        assertEquals(applicationView.getAppDeploymentSpec().getAccessMethods().size(), test.getAppDeploymentSpec().getAccessMethods().size());
    }

    @Test
    void shouldUpdateAppBase() {
        assertDoesNotThrow(() -> {
            mvc.perform(patch("/api/v1/apps/base")
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
        mvc.perform(delete("/api/v1/apps/" + id)
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertEquals(ApplicationState.DELETED, applicationRepository.findAll().getFirst().getState());
    }

    @Test
    void shouldGetAppBase() throws Exception {
        long id = this.testApp1Base.getId();
        MvcResult result = mvc.perform(get("/api/v1/apps/base/" + id)
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ApplicationBaseDto app = objectMapper.readValue(result.getResponse().getContentAsString(), ApplicationBaseDto.class);
        assertEquals(APP_1_NAME, app.getName());
    }

    @Test
    void shouldGetAppBaseByName() throws Exception {
        String name = this.testApp1Base.getName();
        MvcResult result = mvc.perform(get("/api/v1/apps/base/name/" + name)
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ApplicationBaseDto app = objectMapper.readValue(result.getResponse().getContentAsString(), ApplicationBaseDto.class);
        assertEquals(APP_1_NAME, app.getName());
    }

    @Test
    void shouldGetLatestAppVersion() throws Exception {
        this.applicationService.create(getDefaultApplication(APP_1_NAME, "1.3.0", ApplicationState.DISABLED));
        this.applicationService.create(getDefaultApplication(APP_1_NAME, "1.2.0", ApplicationState.ACTIVE));
        MvcResult result = mvc.perform(get("/api/v1/apps/" + APP_1_NAME + "/latest")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ApplicationController.ApplicationCompleteView app = objectMapper.readValue(result.getResponse().getContentAsString(), ApplicationController.ApplicationCompleteView.class);
        assertEquals(APP_1_NAME, app.getApplicationBase().getName());
        assertEquals("1.2.0", app.getApplication().getVersion());
    }

    @Test
    void shouldGetApp() throws Exception {
        long id = applicationRepository.findAll().getFirst().getId();
        MvcResult result = mvc.perform(get("/api/v1/apps/" + id)
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ApplicationController.ApplicationCompleteView app = objectMapper.readValue(result.getResponse().getContentAsString(), ApplicationController.ApplicationCompleteView.class);
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
    void shouldChangeAppState() throws Exception {
        long id = this.testApp1.getId();
        mvc.perform(patch("/api/v1/apps/state/" + id)
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .content(objectMapper.writeValueAsString(new ApplicationStateChangeRequest(ApplicationStateDto.DISABLED, "reason", false)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        MvcResult result = mvc.perform(get("/api/v1/apps/version/" + id)
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        ApplicationDto applicationView = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ApplicationDto.class);
        assertEquals(ApplicationStateDto.DISABLED, applicationView.getState());

        //reverse state to active again
        mvc.perform(patch("/api/v1/apps/state/" + id)
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .content(objectMapper.writeValueAsString(new ApplicationStateChangeRequest(ApplicationStateDto.ACTIVE, "reason", false)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAddNewVersion() {
        AppDeploymentSpecDto appDeploymentSpec = new AppDeploymentSpecDto();
        appDeploymentSpec.setKubernetesTemplate(
                new KubernetesTemplateDto(
                        null,
                        new KubernetesChartDto(null, "name", "version"),
                        "archive",
                        null,
                        new HelmChartRepositoryDto("tooLongNameToMatchTheConstraint", "http://test")
                )
        );
        appDeploymentSpec.setStorageVolumes(new ArrayList<>());
        appDeploymentSpec.getStorageVolumes().add(new AppStorageVolumeDto(null, ServiceStorageVolumeTypeDto.MAIN, 5, new HashMap<>()));
        appDeploymentSpec.setAccessMethods(new ArrayList<>());
        appDeploymentSpec.getAccessMethods().addAll(List.of(
                new AppAccessMethodDto(null, ServiceAccessMethodTypeDto.DEFAULT, "name1", "tag1", null, null, null),
                new AppAccessMethodDto(null, ServiceAccessMethodTypeDto.EXTERNAL, "name2", "tag2", null, null, null),
                new AppAccessMethodDto(null, ServiceAccessMethodTypeDto.INTERNAL, "name3", "tag3", null, null, null)
        ));

        AppConfigurationSpecDto appConfigurationSpec = new AppConfigurationSpecDto(null, new ArrayList<>(), true, false, false);
        appConfigurationSpec.templates().add(new ConfigFileTemplateDto(null, null, "name", "dir", "content"));

        ApplicationDto view = ApplicationDto.builder()
                .name(APP_1_NAME)
                .version("3.0.0")
                .appConfigurationSpec(appConfigurationSpec)
                .appDeploymentSpec(appDeploymentSpec)
                .configWizardTemplate(new ConfigWizardTemplateDto(null, "{}"))
                .configUpdateWizardTemplate(null)
                .build();

        assertDoesNotThrow(() -> {
            mvc.perform(post("/api/v1/apps/version")
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
        mvList.add(AppAccessMethod.builder().type(ServiceAccessMethodType.DEFAULT).name("name1").tag("tag1").build());
        mvList.add(AppAccessMethod.builder().type(ServiceAccessMethodType.EXTERNAL).name("name2").tag("tag2").build());
        mvList.add(AppAccessMethod.builder().type(ServiceAccessMethodType.INTERNAL).name("name3").tag("tag3").build());
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
        mvList.add(AppAccessMethod.builder().type(ServiceAccessMethodType.DEFAULT).name("name1").tag("tag1").build());
        mvList.add(AppAccessMethod.builder().type(ServiceAccessMethodType.EXTERNAL).name("name2").tag("tag2").build());
        mvList.add(AppAccessMethod.builder().type(ServiceAccessMethodType.INTERNAL).name("name3").tag("tag3").build());
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
