package net.geant.nmaas.portal.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.nmservice.configuration.entities.AppConfigurationSpec;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.events.app.AppAutoDeploymentReviewEvent;
import net.geant.nmaas.orchestration.events.app.AppAutoDeploymentStatusUpdateEvent;
import net.geant.nmaas.orchestration.events.app.AppAutoDeploymentTriggeredEvent;
import net.geant.nmaas.portal.api.bulk.CsvApplication;
import net.geant.nmaas.portal.api.domain.UserViewMinimal;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.BulkDeployment;
import net.geant.nmaas.portal.persistent.entity.BulkDeploymentEntry;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.repositories.BulkDeploymentEntryRepository;
import net.geant.nmaas.portal.persistent.repositories.BulkDeploymentRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.ApplicationSubscriptionService;
import net.geant.nmaas.portal.service.BulkApplicationService;
import net.geant.nmaas.portal.service.DomainService;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.geant.nmaas.portal.api.bulk.BulkType.APPLICATION;
import static net.geant.nmaas.portal.persistent.entity.BulkDeploymentState.COMPLETED;
import static net.geant.nmaas.portal.persistent.entity.BulkDeploymentState.PROCESSING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BulkApplicationServiceImplTest {

    private static final String TEST_APP_NAME = "testApplication";
    private static final String TEST_APP_VERSION = "testVersion";

    private final ApplicationBaseService applicationBaseService = mock(ApplicationBaseService.class);
    private final ApplicationService applicationService = mock(ApplicationService.class);
    private final DomainService domainService = mock(DomainService.class);
    private final ApplicationSubscriptionService applicationSubscriptionService = mock(ApplicationSubscriptionService.class);
    private final ApplicationInstanceService applicationInstanceService = mock(ApplicationInstanceService.class);
    private final AppDeploymentMonitor appDeploymentMonitor = mock(AppDeploymentMonitor.class);
    private final AppLifecycleManager appLifecycleManager = mock(AppLifecycleManager.class);
    private final BulkDeploymentRepository bulkDeploymentRepository = mock(BulkDeploymentRepository.class);
    private final BulkDeploymentEntryRepository bulkDeploymentEntryRepository = mock(BulkDeploymentEntryRepository.class);
    private final ModelMapper modelMapper = new ModelMapper();
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

    final BulkApplicationService bulkApplicationService = new BulkApplicationServiceImpl(applicationBaseService, applicationService,
            domainService, applicationSubscriptionService, applicationInstanceService, appDeploymentMonitor, appLifecycleManager,
            bulkDeploymentRepository, bulkDeploymentEntryRepository, modelMapper, eventPublisher);

    @Test
    void shouldHandleBulkDeployment() throws JsonProcessingException {
        HashSetValuedHashMap<String, String> parameters = new HashSetValuedHashMap<>();
        parameters.put("param.key1", "value1");
        CsvApplication csvApplication = new CsvApplication("domain1", "testAppInstance", TEST_APP_VERSION, parameters);
        Domain domain = new Domain(1L,"domain1", "domain1");
        Domain global = new Domain(0L,"GLOBAL", "GLOBAL");
        ApplicationBase applicationBase = new ApplicationBase(110L, TEST_APP_NAME);
        when(applicationBaseService.exists(TEST_APP_NAME)).thenReturn(true);
        when(applicationBaseService.findByName(TEST_APP_NAME)).thenReturn(applicationBase);
        Application application = new Application(1L, TEST_APP_NAME, TEST_APP_VERSION);
        application.setAppConfigurationSpec(new AppConfigurationSpec());
        when(applicationService.findApplication(TEST_APP_NAME, TEST_APP_VERSION)).thenReturn(Optional.of(application));
        when(domainService.findDomain(anyString())).thenReturn(Optional.of(domain));
        when(domainService.getGlobalDomain()).thenReturn(Optional.of(global));
        AppInstance appInstance = new AppInstance(application, domain, "testAppInstance", false);
        appInstance.setId(100L);
        when(applicationInstanceService.create(any(Domain.class), any(Application.class), anyString(), anyBoolean())).thenReturn(appInstance);
        when(bulkDeploymentEntryRepository.save(any(BulkDeploymentEntry.class))).then(AdditionalAnswers.returnsFirstArg());
        when(bulkDeploymentRepository.save(any(BulkDeployment.class))).thenReturn(new BulkDeployment());

        bulkApplicationService.handleBulkDeployment(TEST_APP_NAME, List.of(csvApplication), testUser());

        verify(applicationSubscriptionService).subscribe(110L, domain.getId(), true);
        verify(appLifecycleManager).deployApplication(any());
        ArgumentCaptor<AppInstance> appInstanceArgumentCaptor = ArgumentCaptor.forClass(AppInstance.class);
        verify(applicationInstanceService, times(2)).update(appInstanceArgumentCaptor.capture());
        Map<String, String> deploymentParametersMap = new ObjectMapper().readValue(
                appInstanceArgumentCaptor.getAllValues().get(1).getConfiguration(), Map.class
        );
        assertEquals("value1", deploymentParametersMap.get("key1"));
        verify(bulkDeploymentEntryRepository).save(any());
        verify(eventPublisher).publishEvent(any(AppAutoDeploymentTriggeredEvent.class));
        ArgumentCaptor<BulkDeployment> bulkDeploymentArgumentCaptor = ArgumentCaptor.forClass(BulkDeployment.class);
        verify(bulkDeploymentRepository).save(bulkDeploymentArgumentCaptor.capture());
        BulkDeployment bulkDeployment = bulkDeploymentArgumentCaptor.getValue();
        assertEquals(PROCESSING, bulkDeployment.getState());
        assertEquals(APPLICATION, bulkDeployment.getType());
        assertEquals(testUser().getId(), bulkDeployment.getCreatorId());
        assertEquals(1, bulkDeployment.getEntries().size());
        assertEquals(PROCESSING, bulkDeployment.getEntries().get(0).getState());
    }

    @Test
    void shouldHandleDeploymentStatusUpdate() {
        Identifier bulkDeploymentId = Identifier.newInstance(1L);
        Identifier deploymentId = Identifier.newInstance(2L);
        AppAutoDeploymentStatusUpdateEvent event = new AppAutoDeploymentStatusUpdateEvent(this, bulkDeploymentId, deploymentId);
        when(bulkDeploymentEntryRepository.findById(bulkDeploymentId.longValue())).thenReturn(Optional.of(new BulkDeploymentEntry()));
        when(appDeploymentMonitor.state(deploymentId)).thenReturn(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED);

        ApplicationEvent result = bulkApplicationService.handleDeploymentStatusUpdate(event);

        ArgumentCaptor<BulkDeploymentEntry> bulkDeploymentEntryArgumentCaptor = ArgumentCaptor.forClass(BulkDeploymentEntry.class);
        verify(bulkDeploymentEntryRepository).save(bulkDeploymentEntryArgumentCaptor.capture());
        BulkDeploymentEntry bulkDeploymentEntry = bulkDeploymentEntryArgumentCaptor.getValue();
        assertEquals(COMPLETED, bulkDeploymentEntry.getState());
        assertInstanceOf(AppAutoDeploymentReviewEvent.class, result);
    }

    @Test
    void shouldHandleDeploymentStatusUpdateAndTriggerAnotherCheck() {
        Identifier bulkDeploymentId = Identifier.newInstance(1L);
        Identifier deploymentId = Identifier.newInstance(2L);
        AppAutoDeploymentStatusUpdateEvent event = new AppAutoDeploymentStatusUpdateEvent(this, bulkDeploymentId, deploymentId);
        event.setWaitIntervalBeforeNextCheckInMillis(100);
        when(bulkDeploymentEntryRepository.findById(bulkDeploymentId.longValue())).thenReturn(Optional.of(new BulkDeploymentEntry()));
        when(appDeploymentMonitor.state(deploymentId)).thenReturn(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS);

        ApplicationEvent result = bulkApplicationService.handleDeploymentStatusUpdate(event);

        verify(bulkDeploymentEntryRepository, times(0)).save(any(BulkDeploymentEntry.class));
        assertNotNull(result);
        assertInstanceOf(AppAutoDeploymentStatusUpdateEvent.class, result);
    }

    @Test
    void shouldHandleDeploymentReview() {
        Identifier bulkDeploymentId = Identifier.newInstance(1L);
        AppAutoDeploymentReviewEvent event = new AppAutoDeploymentReviewEvent(this);
        BulkDeployment bAppToBeCompleted = new BulkDeployment(
                1L, 1L, OffsetDateTime.now(), PROCESSING, APPLICATION,
                new ArrayList<>(List.of(new BulkDeploymentEntry(10L, APPLICATION, COMPLETED, true, null))));
        BulkDeployment bAppProcessing = new BulkDeployment(
                2L, 1L, OffsetDateTime.now(), PROCESSING, APPLICATION,
                new ArrayList<>(List.of(new BulkDeploymentEntry(11L, APPLICATION, PROCESSING, true, null))));
        when(bulkDeploymentRepository.findByTypeAndState(APPLICATION, PROCESSING))
                .thenReturn(List.of(bAppToBeCompleted, bAppProcessing));

        bulkApplicationService.handleDeploymentReview(event);

        verify(bulkDeploymentRepository).findByTypeAndState(APPLICATION, PROCESSING);
        ArgumentCaptor<BulkDeployment> bulkDeploymentArgumentCaptor = ArgumentCaptor.forClass(BulkDeployment.class);
        verify(bulkDeploymentRepository, times(1)).save(bulkDeploymentArgumentCaptor.capture());
        assertEquals(COMPLETED, bulkDeploymentArgumentCaptor.getValue().getState());
    }

    private static UserViewMinimal testUser() {
        UserViewMinimal testUser = new UserViewMinimal();
        testUser.setId(1L);
        testUser.setUsername("username");
        return testUser;
    }

}
