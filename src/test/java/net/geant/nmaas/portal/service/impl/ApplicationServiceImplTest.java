package net.geant.nmaas.portal.service.impl;

import com.google.common.collect.ImmutableSet;
import net.geant.nmaas.nmservice.configuration.entities.AppConfigurationSpec;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.HelmChartRepositoryEmbeddable;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesChart;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceStorageVolumeType;
import net.geant.nmaas.orchestration.entities.AppAccessMethod;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.AppStorageVolume;
import net.geant.nmaas.portal.events.ApplicationListUpdatedEvent;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import net.geant.nmaas.portal.persistent.entity.ConfigWizardTemplate;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApplicationServiceImplTest {

    ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
    ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

    ApplicationServiceImpl applicationService;

    @BeforeEach
    void setup(){
        applicationService = new ApplicationServiceImpl(applicationRepository, eventPublisher);
    }

    @Test
    void updateMethodShouldThrowExceptionDueToNullPassedAsParameter(){
        assertThrows(IllegalArgumentException.class, () -> applicationService.update(null));
    }

    @Test
    void updateMethodShouldThrowExceptionDueToEmptyName(){
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = getDefaultApplication();
            app.setName("");
            applicationService.update(app);
        });
    }

    @Test
    void updateMethodShouldThrowExceptionDueToEmptyVersion(){
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = getDefaultApplication();
            app.setVersion("");
            applicationService.update(app);
        });
    }

    @Test
    void updateMethodShouldThrowExceptionDueToNullAppDeploymentSpec(){
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = getDefaultApplication();
            app.setAppDeploymentSpec(null);
            applicationService.update(app);
        });
    }

    @Test
    void updateMethodShouldThrowExceptionDueToNullConfigTemplate(){
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = getDefaultApplication();
            app.setConfigWizardTemplate(null);
            applicationService.update(app);
        });
    }

    @Test
    void updateMethodShouldThrowExceptionDueToEmptyConfigTemplate(){
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = getDefaultApplication();
            app.setConfigWizardTemplate(new ConfigWizardTemplate(""));
            applicationService.update(app);
        });
    }

    @Test
    void updateMethodShouldThrowExceptionDueToNullKubernetesTemplate(){
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = getDefaultApplication();
            app.getAppDeploymentSpec().setKubernetesTemplate(null);
            applicationService.update(app);
        });
    }

    @Test
    void updateMethodShouldThrowExceptionDueToNullKubernetesChart(){
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = getDefaultApplication();
            app.getAppDeploymentSpec().getKubernetesTemplate().setChart(null);
            applicationService.update(app);
        });
    }

    @Test
    public void updateMethodShouldThrowExceptionDueToEmptyKubernetesChartName(){
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = getDefaultApplication();
            app.getAppDeploymentSpec().getKubernetesTemplate().getChart().setName("");
            applicationService.update(app);
        });
    }

    @Test
    void updateMethodShouldReturnApplicationObject(){
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
    void deleteMethodShouldTrowExceptionDueToNullPassedAsId(){
        assertThrows(IllegalArgumentException.class, () -> applicationService.delete(null));
    }

    @Test
    void deleteMethodShouldSetApplicationAsDeleted(){
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
    void findApplicationShouldThrowExceptionDueToNullId(){
        assertThrows(IllegalArgumentException.class, () -> applicationService.findApplication(null));
    }

    @Test
    void findApplicationShouldReturnApplicationObject(){
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
    void shouldNotChangeApplicationStateDueToForbiddenStateChange(){
        assertThrows(IllegalStateException.class, () -> {
            Application app = getDefaultApplication();
            app.setState(ApplicationState.DELETED);
            applicationService.changeApplicationState(app, ApplicationState.ACTIVE);
        });
    }

    private Application getDefaultApplication(){
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
        appDeploymentSpec.setStorageVolumes(ImmutableSet.of(new AppStorageVolume(12L, ServiceStorageVolumeType.MAIN, 2, null)));
        appDeploymentSpec.setAccessMethods(ImmutableSet.of(new AppAccessMethod(13L, ServiceAccessMethodType.DEFAULT, "name", "tag", AppAccessMethod.ConditionType.NONE, null, null)));
        application.setAppDeploymentSpec(appDeploymentSpec);
        application.setConfigWizardTemplate(new ConfigWizardTemplate(1L, "template"));
        AppConfigurationSpec appConfigurationSpec = new AppConfigurationSpec();
        appConfigurationSpec.setConfigFileRepositoryRequired(false);
        appConfigurationSpec.setConfigUpdateEnabled(false);
        application.setAppConfigurationSpec(appConfigurationSpec);
        return application;
    }

}