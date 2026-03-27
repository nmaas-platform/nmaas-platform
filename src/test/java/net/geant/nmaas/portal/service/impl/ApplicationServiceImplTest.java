package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.nmservice.configuration.entities.AppConfigurationSpec;
import net.geant.nmaas.nmservice.configuration.entities.ConfigFileTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.HelmChartRepositoryEmbeddable;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesChart;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceStorageVolumeType;
import net.geant.nmaas.orchestration.entities.AppAccessMethod;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.AppStorageVolume;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.events.ApplicationListUpdatedEvent;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.ApplicationState;
import net.geant.nmaas.portal.persistence.entity.ConfigWizardTemplate;
import net.geant.nmaas.portal.persistence.repositories.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApplicationServiceImplTest {

    ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
    ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    ConfigurationTemplateSanitizerService configurationTemplateSanitizerService = mock(ConfigurationTemplateSanitizerService.class);

    ApplicationServiceImpl applicationService;

    @BeforeEach
    void setup() {
        applicationService = new ApplicationServiceImpl(applicationRepository, eventPublisher, configurationTemplateSanitizerService);
    }

    @Test
    void updateMethodShouldThrowExceptionDueToNullPassedAsParameter() {
        assertThrows(IllegalArgumentException.class, () -> applicationService.update(null));
    }

    @Test
    void updateMethodShouldThrowExceptionDueToEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = getDefaultApplication();
            app.setName("");
            applicationService.update(app);
        });
    }

    @Test
    void updateMethodShouldThrowExceptionDueToEmptyVersion() {
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = getDefaultApplication();
            app.setVersion("");
            applicationService.update(app);
        });
    }

    @Test
    void updateMethodShouldThrowExceptionDueToNullAppDeploymentSpec() {
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = getDefaultApplication();
            app.setAppDeploymentSpec(null);
            applicationService.update(app);
        });
    }

    @Test
    void updateMethodShouldThrowExceptionDueToNullConfigTemplate() {
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = getDefaultApplication();
            app.setConfigWizardTemplate(null);
            applicationService.update(app);
        });
    }

    @Test
    void updateMethodShouldThrowExceptionDueToEmptyConfigTemplate() {
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = getDefaultApplication();
            app.setConfigWizardTemplate(new ConfigWizardTemplate(""));
            applicationService.update(app);
        });
    }

    @Test
    void updateMethodShouldThrowExceptionDueToNullKubernetesTemplate() {
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = getDefaultApplication();
            app.getAppDeploymentSpec().setKubernetesTemplate(null);
            applicationService.update(app);
        });
    }

    @Test
    void updateMethodShouldThrowExceptionDueToNullKubernetesChart() {
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = getDefaultApplication();
            app.getAppDeploymentSpec().getKubernetesTemplate().setChart(null);
            applicationService.update(app);
        });
    }

    @Test
    void updateMethodShouldThrowExceptionDueToEmptyKubernetesChartName() {
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = getDefaultApplication();
            app.getAppDeploymentSpec().getKubernetesTemplate().getChart().setName("");
            applicationService.update(app);
        });
    }

    @Test
    void updateMethodShouldReturnApplicationObject() {
        Application application = new Application("test", "testversion");
        application.setId(1L);
        when(applicationRepository.save(isA(Application.class))).thenReturn(application);
        AppDeploymentSpec appDeploymentSpec = new AppDeploymentSpec();
        appDeploymentSpec.setKubernetesTemplate(
                new KubernetesTemplate(
                        new KubernetesChart("chart", "version"), null, null)
        );
        appDeploymentSpec.setStorageVolumes(Collections.singleton(new AppStorageVolume(ServiceStorageVolumeType.MAIN, 2, null)));
        appDeploymentSpec.setAccessMethods(Collections.singleton(new AppAccessMethod(ServiceAccessMethodType.DEFAULT, "name", "tag", null)));
        application.setAppDeploymentSpec(appDeploymentSpec);
        application.setConfigWizardTemplate(new ConfigWizardTemplate("test-template"));
        application.setAppConfigurationSpec(new AppConfigurationSpec());
        Application result = applicationService.update(application);
        assertNotNull(result);
        ArgumentCaptor<ApplicationListUpdatedEvent> event = ArgumentCaptor.forClass(ApplicationListUpdatedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(event.capture());
        assertEquals(ApplicationListUpdatedEvent.ApplicationAction.UPDATED, event.getValue().getAction());
    }

    @Test
    void deleteMethodShouldTrowExceptionDueToNullPassedAsId() {
        assertThrows(IllegalArgumentException.class, () -> applicationService.delete(null));
    }

    @Test
    void deleteMethodShouldSetApplicationAsDeleted() {
        Application application = new Application("test", "testversion");
        application.setId((long) 0);
        application.setState(ApplicationState.ACTIVE);
        when(applicationRepository.findById(anyLong())).thenReturn(Optional.of(application));
        when(applicationRepository.save(isA(Application.class))).thenReturn(application);
        applicationService.delete((long) 0);
        verify(applicationRepository).findById(anyLong());
        verify(applicationRepository).save(isA(Application.class));
        ArgumentCaptor<ApplicationListUpdatedEvent> event = ArgumentCaptor.forClass(ApplicationListUpdatedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(event.capture());
        assertEquals(ApplicationListUpdatedEvent.ApplicationAction.DELETED, event.getValue().getAction());
    }

    @Test
    void findApplicationShouldThrowExceptionDueToNullId() {
        assertThrows(IllegalArgumentException.class, () -> applicationService.findApplication(null));
    }

    @Test
    void findApplicationShouldReturnApplicationObject() {
        Application application = new Application("test", "testversion");
        when(applicationRepository.findById(anyLong())).thenReturn(Optional.of(application));
        Optional<Application> result = applicationService.findApplication((long) 0);
        assertTrue(result.isPresent());
    }

    @Test
    void findAllShouldReturnList() {
        List<Application> testList = new ArrayList<>();
        Application test = new Application("test", "testversion");
        testList.add(test);
        when(applicationRepository.findAll()).thenReturn(testList);
        List<Application> result = applicationService.findAll();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldChangeApplicationState() {
        Application app = getDefaultApplication();
        app.setState(ApplicationState.NEW);
        applicationService.changeApplicationState(app, ApplicationState.ACTIVE);
        verify(applicationRepository).save(any());
    }

    @Test
    void shouldNotChangeApplicationStateDueToForbiddenStateChange() {
        assertThrows(IllegalStateException.class, () -> {
            Application app = getDefaultApplication();
            app.setState(ApplicationState.DELETED);
            applicationService.changeApplicationState(app, ApplicationState.ACTIVE);
        });
    }

    @Test
    void createShouldThrowWhenIdIsProvided() {
        Application app = getDefaultApplication();
        app.setId(10L);

        assertThrows(ProcessingException.class, () -> applicationService.create(app));
    }

    @Test
    void createShouldClearNestedIdsAndPublishAddedEvent() {
        Application app = getDefaultApplication();
        app.setId(null);
        app.setConfigUpdateWizardTemplate(new ConfigWizardTemplate(2L, "update-template"));
        app.getAppConfigurationSpec().setId(3L);
        app.getAppConfigurationSpec().getTemplates().add(new ConfigFileTemplate(4L, 5L, "a.txt", "/", "x"));
        app.getAppDeploymentSpec().setId(6L);

        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0, Application.class));

        Application result = applicationService.create(app);

        assertNotNull(result);
        assertEquals(null, result.getConfigWizardTemplate().getId());
        assertEquals(null, result.getConfigUpdateWizardTemplate().getId());
        assertEquals(null, result.getAppConfigurationSpec().getId());
        assertEquals(null, result.getAppConfigurationSpec().getTemplates().getFirst().getId());
        assertEquals(null, result.getAppDeploymentSpec().getId());
        verify(eventPublisher).publishEvent(any(ApplicationListUpdatedEvent.class));
    }

    @Test
    void deleteShouldNotSaveWhenTransitionIsNotAllowed() {
        Application application = new Application("test", "testversion");
        application.setId(1L);
        application.setState(ApplicationState.DELETED);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        applicationService.delete(1L);

        verify(applicationRepository).findById(1L);
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void findLatestVersionShouldReturnNewestByCreationDate() {
        Application older = new Application("grafana", "1.0.0");
        older.setCreationDate(java.time.LocalDateTime.now().minusDays(2));
        Application newer = new Application("grafana", "2.0.0");
        newer.setCreationDate(java.time.LocalDateTime.now().minusDays(1));
        when(applicationRepository.findByName("grafana")).thenReturn(List.of(older, newer));

        Application result = applicationService.findApplicationLatestVersion("grafana");

        assertEquals("2.0.0", result.getVersion());
    }

    @Test
    void findLatestVersionShouldThrowForBlankName() {
        assertThrows(IllegalArgumentException.class, () -> applicationService.findApplicationLatestVersion(" "));
    }

    @Test
    void findLatestVersionShouldThrowWhenMissing() {
        when(applicationRepository.findByName("missing")).thenReturn(List.of());

        assertThrows(MissingElementException.class, () -> applicationService.findApplicationLatestVersion("missing"));
    }

    @Test
    void changeApplicationStateToDeletedShouldAppendSuffix() {
        Application app = getDefaultApplication();
        app.setState(ApplicationState.ACTIVE);
        app.setName("my-app");

        applicationService.changeApplicationState(app, ApplicationState.DELETED);

        assertTrue(app.getName().startsWith("my-app_DELETED_"));
        verify(applicationRepository).save(app);
    }

    @Test
    void findAllActiveVersionNumbersShouldReturnOnlyActiveVersions() {
        Application active = getDefaultApplication();
        active.setId(11L);
        active.setName("prom");
        active.setState(ApplicationState.ACTIVE);
        active.getAppDeploymentSpec().getKubernetesTemplate().getChart().setVersion("1.0.0");

        Application inactive = getDefaultApplication();
        inactive.setId(12L);
        inactive.setName("prom");
        inactive.setState(ApplicationState.NEW);
        inactive.getAppDeploymentSpec().getKubernetesTemplate().getChart().setVersion("2.0.0");

        when(applicationRepository.findByName("prom")).thenReturn(List.of(active, inactive));

        Map<String, Long> versions = applicationService.findAllActiveVersionNumbers("prom");

        assertEquals(1, versions.size());
        assertEquals(11L, versions.get("1.0.0"));
    }

    @Test
    void findAllActiveVersionNumbersShouldThrowForBlankName() {
        assertThrows(IllegalArgumentException.class, () -> applicationService.findAllActiveVersionNumbers(""));
    }

    @Test
    void checkAndUpdateAllConfigurationTemplatesShouldSanitizeAndSave() {
        Application app = getDefaultApplication();
        app.setId(77L);
        app.setConfigUpdateWizardTemplate(new ConfigWizardTemplate("old-update"));
        when(applicationRepository.findAll()).thenReturn(List.of(app));
        when(configurationTemplateSanitizerService.sanitizeConfigurationJson("template")).thenReturn("sanitized-create");
        when(configurationTemplateSanitizerService.sanitizeConfigurationJson("old-update")).thenReturn("sanitized-update");

        applicationService.checkAndUpdateAllConfigurationTemplates();

        assertEquals("sanitized-create", app.getConfigWizardTemplate().getTemplate());
        assertEquals("sanitized-update", app.getConfigUpdateWizardTemplate().getTemplate());
        verify(applicationRepository).save(app);
    }

    @Test
    void setMissingPropertiesShouldSetApplicationIdForTemplates() {
        Application app = getDefaultApplication();
        app.getAppConfigurationSpec().setTemplates(new ArrayList<>(List.of(
                new ConfigFileTemplate(null, null, "f1", "/", "x"),
                new ConfigFileTemplate(null, null, "f2", "/", "y")
        )));

        applicationService.setMissingProperties(app, 501L);

        assertEquals(501L, app.getAppConfigurationSpec().getTemplates().get(0).getApplicationId());
        assertEquals(501L, app.getAppConfigurationSpec().getTemplates().get(1).getApplicationId());
    }

    private Application getDefaultApplication() {
        Application application = new Application();
        application.setName("test");
        application.setVersion("testversion");
        AppDeploymentSpec appDeploymentSpec = new AppDeploymentSpec();
        appDeploymentSpec.setKubernetesTemplate(new KubernetesTemplate(
                1L,
                new KubernetesChart(1L, "name", "version"),
                "archive",
                null,
                new HelmChartRepositoryEmbeddable("test", "http://test"))
        );
        appDeploymentSpec.setStorageVolumes(Set.of(new AppStorageVolume(12L, ServiceStorageVolumeType.MAIN, 2, null)));
        appDeploymentSpec.setAccessMethods(Set.of(new AppAccessMethod(13L, ServiceAccessMethodType.DEFAULT, "name", "tag", AppAccessMethod.ConditionType.NONE, null, null)));
        application.setAppDeploymentSpec(appDeploymentSpec);
        application.setConfigWizardTemplate(new ConfigWizardTemplate(1L, "template"));
        AppConfigurationSpec appConfigurationSpec = new AppConfigurationSpec();
        appConfigurationSpec.setConfigFileRepositoryRequired(false);
        appConfigurationSpec.setConfigUpdateEnabled(false);
        application.setAppConfigurationSpec(appConfigurationSpec);
        return application;
    }

}
