package net.geant.nmaas.portal.api.market;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import net.geant.nmaas.nmservice.configuration.entities.AppConfigurationSpec;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesChart;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceStorageVolumeType;
import net.geant.nmaas.orchestration.entities.AppAccessMethod;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.AppStorageVolume;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.api.domain.*;
import net.geant.nmaas.portal.persistent.entity.*;
import net.geant.nmaas.portal.persistent.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import net.geant.nmaas.portal.persistent.repositories.TagRepository;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class TagControllerIntTest extends BaseControllerTestSetup {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationBaseService applicationBaseService;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ApplicationRepository appRepository;

    @Autowired
    private ApplicationBaseRepository appBaseRepo;


    @BeforeEach
    public void setup(){
        this.mvc = createMVC();

        ModelMapper modelMapper = new ModelMapper();

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
                new ApplicationVersion(app1.getVersion(), app1.getState(), app1.getId() )
        );
        base1 = applicationBaseService.update(base1);
        base2.getVersions().add(
                new ApplicationVersion(app2.getVersion(), app2.getState(), app2.getId() )
        );
        base2 = applicationBaseService.update(base2);
    }

    @AfterEach
    public void teardown(){
        this.appRepository.deleteAll();
        this.appBaseRepo.deleteAll();
        this.tagRepository.deleteAll();
    }

    @Test
    public void thereShouldBeTwoTagsInRepositoryAfterSetup() {
        assertEquals(2, tagRepository.count());
    }

    @Test
    public void shouldGetAllTags() throws Exception{
        MvcResult result = mvc.perform(get("/api/tags")
                .header("Authorization","Bearer " + getValidTokenForUser(UsersHelper.ADMIN)))
                .andExpect(status().isOk())
                .andReturn();
        Set<String> resultSet = new ObjectMapper().readValue(result.getResponse().getContentAsByteArray(), new TypeReference<Set<String>>(){});
        System.out.println(result.getResponse().getContentAsString());
        assertTrue(resultSet.contains("tag1"));
    }

    @Test
    public void shouldGetAppByTagWhenThereAreActiveApplications() throws Exception {
        MvcResult result = mvc.perform(get("/api/tags/tag1")
                .header("Authorization","Bearer " + getValidTokenForUser(UsersHelper.ADMIN)))
                .andExpect(status().isOk())
                .andReturn();
        Set<ApplicationBaseView> resultSet = new ObjectMapper().readValue(result.getResponse().getContentAsByteArray(), new TypeReference<Set<ApplicationBaseView>>(){});
        assertEquals(2, resultSet.size());
    }

    @Test
    public void shouldGetEmptyCollection() throws Exception {
        MvcResult result = mvc.perform(get("/api/tags/deprecated")
                .header("Authorization","Bearer " + getValidTokenForUser(UsersHelper.ADMIN)))
                .andExpect(status().isOk())
                .andReturn();
        Set<ApplicationBaseView> resultSet = new ObjectMapper().readValue(result.getResponse().getContentAsByteArray(), new TypeReference<Set<ApplicationBaseView>>(){});
        assertTrue(resultSet.isEmpty());
    }

    private ApplicationBaseView getDefaultApplicationBaseView(String name) {
        return  ApplicationBaseView.builder()
                .name(name)
                .owner("admin")
                .descriptions(
                        Collections.singletonList(
                                new AppDescriptionView("en", "description", "full description")
                        )
                )
                .tags(
                        ImmutableSet.of(
                                new TagView(null, "tag1"),
                                new TagView(null, "tag2")
                        )
                )
                .build();
    }

    private Application getDefaultApplication(String name) {
        List<AppStorageVolume> svList = new ArrayList<>();
        svList.add(new AppStorageVolume(null, ServiceStorageVolumeType.MAIN, 5, new HashMap<>()));
        List<AppAccessMethod> mvList = new ArrayList<>();
        mvList.add(new AppAccessMethod(null, ServiceAccessMethodType.DEFAULT, "name1", "tag1", new HashMap<>()));
        mvList.add(new AppAccessMethod(null, ServiceAccessMethodType.EXTERNAL, "name2", "tag2", new HashMap<>()));
        mvList.add(new AppAccessMethod(null, ServiceAccessMethodType.INTERNAL, "name3", "tag3", new HashMap<>()));
        Application application = new Application();
        application.setName(name);
        application.setVersion("1.0.0");
        application.setState(ApplicationState.ACTIVE);
        application.setCreationDate(LocalDateTime.now());
        AppDeploymentSpec appDeploymentSpec = new AppDeploymentSpec();
        appDeploymentSpec.setKubernetesTemplate(new KubernetesTemplate(null, new KubernetesChart(null, "name", "version"), "archive", null));
        appDeploymentSpec.setStorageVolumes(new HashSet<>(svList));
        appDeploymentSpec.setAccessMethods(new HashSet<>(mvList));
        application.setAppDeploymentSpec(appDeploymentSpec);
        application.setConfigWizardTemplate(new ConfigWizardTemplate(null, "{}"));
        application.setAppConfigurationSpec(new AppConfigurationSpec());
        application.getAppConfigurationSpec().setConfigFileRepositoryRequired(false);
        return application;
    }
}
