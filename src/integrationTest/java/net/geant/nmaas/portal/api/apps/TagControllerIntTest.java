package net.geant.nmaas.portal.api.apps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.api.dto.applications.AppDescriptionDto;
import net.geant.nmaas.api.dto.applications.AppTagDto;
import net.geant.nmaas.api.dto.applications.ApplicationBaseView;
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
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.ApplicationState;
import net.geant.nmaas.portal.persistence.entity.ApplicationVersion;
import net.geant.nmaas.portal.persistence.entity.ConfigWizardTemplate;
import net.geant.nmaas.portal.persistence.entity.UsersHelper;
import net.geant.nmaas.portal.persistence.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistence.repositories.ApplicationRepository;
import net.geant.nmaas.portal.persistence.repositories.TagRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class TagControllerIntTest extends BaseControllerTestSetup {

    private final ApplicationService applicationService;

    private final ApplicationBaseService applicationBaseService;

    private final TagRepository tagRepository;

    private final ApplicationRepository appRepository;

    private final ApplicationBaseRepository applicationBaseRepository;

    private final ModelMapper modelMapper;

    public TagControllerIntTest(@Autowired ApplicationService applicationService, @Autowired ApplicationBaseService applicationBaseService,
                                @Autowired TagRepository tagRepository, @Autowired ApplicationRepository appRepository,
                                @Autowired ApplicationBaseRepository applicationBaseRepository, @Autowired ModelMapper modelMapper) {
        this.applicationService = applicationService;
        this.applicationBaseService = applicationBaseService;
        this.tagRepository = tagRepository;
        this.appRepository = appRepository;
        this.applicationBaseRepository = applicationBaseRepository;
        this.modelMapper = modelMapper;
    }

    @BeforeEach
    void setup() {
        this.mvc = createMVC();
        ApplicationBase base1 = applicationBaseService.create(
                modelMapper.map(
                        getDefaultApplicationBaseView("app1"),
                        ApplicationBase.class
                )
        );
        ApplicationBase base2 = applicationBaseService.create(
                modelMapper.map(
                        getDefaultApplicationBaseView("app2"),
                        ApplicationBase.class
                )
        );
        Application app1 = this.applicationService.create(getDefaultApplication(base1.getName()));
        Application app2 = this.applicationService.create(getDefaultApplication(base2.getName()));

        base1.getVersions().add(
                new ApplicationVersion(app1.getVersion(), app1.getState(), app1.getId())
        );
        base1 = applicationBaseService.update(base1);
        base2.getVersions().add(
                new ApplicationVersion(app2.getVersion(), app2.getState(), app2.getId())
        );
        base2 = applicationBaseService.update(base2);
    }

    @AfterEach
    void teardown() {
        this.appRepository.deleteAll();
        this.applicationBaseRepository.deleteAll();
        this.tagRepository.deleteAll();
    }

    @Test
    void thereShouldBeTwoTagsInRepositoryAfterSetup() {
        assertEquals(2, tagRepository.count());
    }

    @Test
    void shouldGetAllTags() throws Exception {
        MvcResult result = mvc.perform(get("/api/tags")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN)))
                .andExpect(status().isOk())
                .andReturn();
        Set<String> resultSet = new ObjectMapper().readValue(result.getResponse().getContentAsByteArray(), new TypeReference<Set<String>>() {
        });
        assertTrue(resultSet.contains("tag1"));
    }

    @Test
    void shouldGetAppByTagWhenThereAreActiveApplications() throws Exception {
        MvcResult result = mvc.perform(get("/api/tags/tag1")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN)))
                .andExpect(status().isOk())
                .andReturn();
        Set<ApplicationBaseView> resultSet = new ObjectMapper().readValue(result.getResponse().getContentAsByteArray(), new TypeReference<Set<ApplicationBaseView>>() {
        });

        assertEquals(2, resultSet.size());
    }

    @Test
    void shouldGetEmptyCollection() throws Exception {
        MvcResult result = mvc.perform(get("/api/tags/deprecated")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN)))
                .andExpect(status().isOk())
                .andReturn();
        Set<ApplicationBaseView> resultSet = new ObjectMapper().readValue(result.getResponse().getContentAsByteArray(), new TypeReference<Set<ApplicationBaseView>>() {
        });
        assertTrue(resultSet.isEmpty());
    }

    private ApplicationBaseView getDefaultApplicationBaseView(String name) {
        return ApplicationBaseView.builder()
                .name(name)
                .owner("admin")
                .descriptions(
                        Collections.singletonList(
                                new AppDescriptionDto("en", "description", "full description")
                        )
                )
                .tags(Set.of(new AppTagDto(null, "tag1"), new AppTagDto(null, "tag2")))
                .build();
    }

    private Application getDefaultApplication(String name) {
        List<AppStorageVolume> svList = new ArrayList<>();
        svList.add(new AppStorageVolume(null, ServiceStorageVolumeType.MAIN, 5, new HashMap<>()));
        List<AppAccessMethod> mvList = new ArrayList<>();
        mvList.add(AppAccessMethod.builder().type(ServiceAccessMethodType.DEFAULT).name("name1").tag("tag1").build());
        mvList.add(AppAccessMethod.builder().type(ServiceAccessMethodType.EXTERNAL).name("name2").tag("tag2").build());
        mvList.add(AppAccessMethod.builder().type(ServiceAccessMethodType.INTERNAL).name("name3").tag("tag3").build());
        Application application = new Application();
        application.setName(name);
        application.setVersion("1.0.0");
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
