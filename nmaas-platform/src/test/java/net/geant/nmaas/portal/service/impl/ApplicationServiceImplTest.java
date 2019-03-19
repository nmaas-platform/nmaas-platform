package net.geant.nmaas.portal.service.impl;

import java.util.Arrays;
import java.util.Collections;
import net.geant.nmaas.nmservice.configuration.entities.AppConfigurationSpec;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api.KubernetesChartView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api.KubernetesTemplateView;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesChart;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.portal.api.domain.AppConfigurationSpecView;
import net.geant.nmaas.portal.api.domain.AppDescriptionView;
import net.geant.nmaas.portal.api.domain.ApplicationView;
import net.geant.nmaas.portal.api.domain.ConfigWizardTemplateView;
import net.geant.nmaas.portal.persistent.entity.AppDescription;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import net.geant.nmaas.portal.persistent.entity.ConfigWizardTemplate;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceImplTest {

    @Mock
    ApplicationRepository applicationRepository;

    @InjectMocks
    ApplicationServiceImpl applicationService;

    private ModelMapper modelMapper = new ModelMapper();

    @BeforeEach
    public void setup(){
        applicationService = new ApplicationServiceImpl(applicationRepository, new ModelMapper());
    }

    @Test
    public void createMethodShouldThrowExceptionDueToNullRequest(){
        assertThrows(IllegalArgumentException.class, () -> applicationService.create(null, null));
    }

    @Test
    public void shouldThrowExceptionDueToIncorrectName(){
        assertThrows(IllegalArgumentException.class, () -> {
            ApplicationView applicationView = getDefaultAppView();
            applicationView.setName("");
            applicationService.create(applicationView, "admin");
        });
    }

    @Test
    public void shouldThrowExceptionDueToIncorrectVersion(){
        assertThrows(IllegalArgumentException.class, () -> {
            ApplicationView applicationView = getDefaultAppView();
            applicationView.setVersion("");
            applicationService.create(applicationView, "admin");
        });
    }

    @Test
    public void shouldThrowExceptionDueToIncorrectOwner(){
        assertThrows(IllegalArgumentException.class, () -> {
            ApplicationView applicationView = getDefaultAppView();
            applicationService.create(applicationView, null);
        });
    }

    @Test
    public void shouldThrowExceptionDueToIncorrectNameAndVersion(){
        assertThrows(IllegalStateException.class, () -> {
            ApplicationView applicationView = getDefaultAppView();
            when(applicationRepository.existsByNameAndVersion(applicationView.getName(), applicationView.getVersion())).thenReturn(true);
            applicationService.create(applicationView, "admin");
        });
    }

    @Test
    public void shouldThrowExceptionDueToAppDeploymentSpec(){
        assertThrows(IllegalArgumentException.class, () -> {
            ApplicationView applicationView = getDefaultAppView();
            applicationView.setAppDeploymentSpec(null);
            applicationService.create(applicationView, "admin");
        });
    }

    @Test
    public void shouldThrowExceptionDueToIncorrectConfigTemplate(){
        assertThrows(IllegalArgumentException.class, () -> {
            ApplicationView applicationView = getDefaultAppView();
            applicationView.setConfigTemplate(null);
            applicationService.create(applicationView, "admin");
        });

    }

    @Test
    public void shouldThrowExceptionDueToIncorrectDescriptions(){
        assertThrows(IllegalArgumentException.class, () -> {
            ApplicationView applicationView = getDefaultAppView();
            applicationView.setDescriptions(null);
            applicationService.create(applicationView, "admin");
        });
    }

    @Test
    public void shouldThrowExceptionDueToEmptyDescriptions(){
        assertThrows(IllegalArgumentException.class, () -> {
            ApplicationView applicationView = getDefaultAppView();
            applicationView.setDescriptions(Collections.emptyList());
            applicationService.create(applicationView, "admin");
        });
    }

    @Test
    public void shouldThrowExceptionWhenEnglishDescriptionIsMissing(){
        assertThrows(IllegalStateException.class, () -> {
            ApplicationView applicationView = getDefaultAppView();
            applicationView.setDescriptions(Collections.singletonList(new AppDescriptionView("pl", "test", "test")));
            applicationService.create(applicationView, "admin");
        });
    }

    @Test
    public void createMethodShouldReturnApplicationObject(){
        Application application = new Application("test","testversion","owner");
        when(applicationRepository.save(isA(Application.class))).thenReturn(application);
        ApplicationView applicationView = getDefaultAppView();
        Application result = applicationService.create(applicationView,"owner");
        assertNotNull(result);
        assertEquals("test", result.getName());
    }

    @Test
    public void shouldAddMissingDescriptions(){
        Application application = new Application("test","testversion","owner");
        when(applicationRepository.save(isA(Application.class))).thenReturn(application);
        ApplicationView applicationView = getDefaultAppView();
        applicationView.setDescriptions(Arrays.asList(new AppDescriptionView("pl", "", ""), new AppDescriptionView("en", "test", "testfull")));
        applicationView.setConfigTemplate(new ConfigWizardTemplateView("template"));
        Application result = applicationService.create(applicationView,"owner");
        assertTrue(StringUtils.isNotEmpty(result.getDescriptions().get(1).getBriefDescription()));
        assertTrue(StringUtils.isNotEmpty(result.getDescriptions().get(1).getFullDescription()));
        assertEquals(result.getDescriptions().get(1).getBriefDescription(), result.getDescriptions().get(0).getBriefDescription());
        assertEquals(result.getDescriptions().get(1).getFullDescription(), result.getDescriptions().get(0).getFullDescription());
    }

    @Test
    public void updateMethodShouldThrowExceptionDueToNullPassedAsParameter(){
        assertThrows(IllegalArgumentException.class, () -> {
            applicationService.update(null);
        });
    }

    @Test
    public void updateMethodShouldThrowExceptionDueToEmptyName(){
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = modelMapper.map(getDefaultAppView(), Application.class);
            app.setName("");
            applicationService.update(app);
        });
    }

    @Test
    public void updateMethodShouldThrowExceptionDueToEmptyVersion(){
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = modelMapper.map(getDefaultAppView(), Application.class);
            app.setVersion("");
            applicationService.update(app);
        });
    }

    @Test
    public void updateMethodShouldThrowExceptionDueToEmptyOwner(){
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = modelMapper.map(getDefaultAppView(), Application.class);
            app.setOwner("");
            applicationService.update(app);
        });
    }

    @Test
    public void updateMethodShouldThrowExceptionDueToNullAppDeploymentSpec(){
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = modelMapper.map(getDefaultAppView(), Application.class);
            app.setAppDeploymentSpec(null);
            applicationService.update(app);
        });
    }

    @Test
    public void updateMethodShouldThrowExceptionDueToNullConfigTemplate(){
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = modelMapper.map(getDefaultAppView(), Application.class);
            app.setConfigWizardTemplate(null);
            applicationService.update(app);
        });
    }

    @Test
    public void updateMethodShouldThrowExceptionDueToEmptyConfigTemplate(){
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = modelMapper.map(getDefaultAppView(), Application.class);
            app.setConfigWizardTemplate(new ConfigWizardTemplate(""));
            applicationService.update(app);
        });
    }

    @Test
    public void updateMethodShouldThrowExceptionDueToNullDescriptions(){
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = modelMapper.map(getDefaultAppView(), Application.class);
            app.setDescriptions(null);
            applicationService.update(app);
        });
    }

    @Test
    public void updateMethodShouldThrowExceptionDueToEmptyDescriptions(){
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = modelMapper.map(getDefaultAppView(), Application.class);
            app.setDescriptions(Collections.emptyList());
            applicationService.update(app);
        });
    }

    @Test
    public void updateMethodShouldThrowExceptionDueToNullKubernetesTemplate(){
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = modelMapper.map(getDefaultAppView(), Application.class);
            app.getAppDeploymentSpec().setKubernetesTemplate(null);
            applicationService.update(app);
        });
    }

    @Test
    public void updateMethodShouldThrowExceptionDueToDefaultStorageSpaceLowerThanZero(){
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = modelMapper.map(getDefaultAppView(), Application.class);
            app.getAppDeploymentSpec().setDefaultStorageSpace(-3);
            applicationService.update(app);
        });
    }

    @Test
    public void updateMethodShouldThrowExceptionDueToNullDefaultStorageSpace(){
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = modelMapper.map(getDefaultAppView(), Application.class);
            app.getAppDeploymentSpec().setDefaultStorageSpace(null);
            applicationService.update(app);
        });
    }

    @Test
    public void updateMethodShouldThrowExceptionDueToNullKubernetesChart(){
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = modelMapper.map(getDefaultAppView(), Application.class);
            app.getAppDeploymentSpec().getKubernetesTemplate().setChart(null);
            applicationService.update(app);
        });
    }

    @Test
    public void updateMethodShouldThrowExceptionDueToEmptyKubernetesChartName(){
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = modelMapper.map(getDefaultAppView(), Application.class);
            app.getAppDeploymentSpec().getKubernetesTemplate().getChart().setName("");
            applicationService.update(app);
        });
    }

    @Test
    public void updateMethodShouldReturnApplicationObject(){
        Application application = new Application("test", "testversion","owner");
        application.setId(1L);
        application.setLicense("MIT");
        when(applicationRepository.save(isA(Application.class))).thenReturn(application);
        application.setLicense("Apache-2.0");
        application.setAppDeploymentSpec(new AppDeploymentSpec());
        application.getAppDeploymentSpec().setDefaultStorageSpace(1);
        application.getAppDeploymentSpec().setKubernetesTemplate(new KubernetesTemplate());
        application.getAppDeploymentSpec().getKubernetesTemplate().setChart(new KubernetesChart("chart", "version"));
        application.setConfigWizardTemplate(new ConfigWizardTemplate("test-template"));
        application.setAppConfigurationSpec(new AppConfigurationSpec());
        application.setDescriptions(Collections.singletonList(new AppDescription()));
        Application result = applicationService.update(application);
        assertNotNull(result);
        assertNotEquals("MIT", result.getLicense());
    }

    @Test
    public void deleteMethodShouldTrowExceptionDueToNullPassedAsId(){
        assertThrows(IllegalArgumentException.class, () -> applicationService.delete(null));
    }

    @Test
    public void deleteMethodShouldSetApplicationAsDeleted(){
        Application application = new Application("test", "testversion","owner");
        application.setId((long) 0);
        application.setState(ApplicationState.ACTIVE);
        when(applicationRepository.findById(anyLong())).thenReturn(Optional.of(application));
        when(applicationRepository.save(isA(Application.class))).thenReturn(application);
        applicationService.delete((long) 0);
        verify(applicationRepository).findById(anyLong());
        verify(applicationRepository).save(isA(Application.class));
    }

    @Test
    public void findApplicationShouldThrowExceptionDueToNullId(){
        assertThrows(IllegalArgumentException.class, () -> applicationService.findApplication(null));
    }

    @Test
    public void findApplicationShouldReturnApplicationObject(){
        Application application = new Application("test", "testversion","owner");
        when(applicationRepository.findById(anyLong())).thenReturn(Optional.of(application));
        Optional<Application> result = applicationService.findApplication((long) 0);
        assertTrue(result.isPresent());
    }

    @Test
    public void findAllShouldReturnList(){
        List<Application> testList = new ArrayList<>();
        Application test = new Application("test", "testversion","owner");
        testList.add(test);
        when(applicationRepository.findAll()).thenReturn(testList);
        List<Application> result = applicationService.findAll();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void shouldChangeApplicationState(){
        Application app = modelMapper.map(getDefaultAppView(), Application.class);
        app.setState(ApplicationState.NEW);
        applicationService.changeApplicationState(app, ApplicationState.ACTIVE);
        verify(applicationRepository, times(1)).save(any());
    }

    @Test
    public void shouldNotChangeApplicationStateDueToForbiddenStateChange(){
        assertThrows(IllegalStateException.class, () -> {
            Application app = modelMapper.map(getDefaultAppView(), Application.class);
            app.setState(ApplicationState.DELETED);
            applicationService.changeApplicationState(app, ApplicationState.ACTIVE);
        });
    }

    private ApplicationView getDefaultAppView(){
        ApplicationView applicationView = new ApplicationView();
        applicationView.setName("test");
        applicationView.setVersion("testversion");
        applicationView.setOwner("owner");
        applicationView.setDescriptions(Collections.singletonList(new AppDescriptionView("en", "test", "testfull")));
        net.geant.nmaas.portal.api.domain.AppDeploymentSpec appDeploymentSpec = new net.geant.nmaas.portal.api.domain.AppDeploymentSpec();
        appDeploymentSpec.setKubernetesTemplate(new KubernetesTemplateView(new KubernetesChartView("name", "version"), "archive"));
        appDeploymentSpec.setDefaultStorageSpace(1);
        applicationView.setAppDeploymentSpec(appDeploymentSpec);
        applicationView.setConfigTemplate(new ConfigWizardTemplateView("template"));
        applicationView.setAppConfigurationSpec(new AppConfigurationSpecView());
        applicationView.getAppConfigurationSpec().setConfigFileRepositoryRequired(false);
        return applicationView;
    }

}