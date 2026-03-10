package net.geant.nmaas.portal.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.api.dto.users.UserViewMinimal;
import net.geant.nmaas.nmservice.configuration.entities.AppConfigurationSpec;
import net.geant.nmaas.nmservice.deployment.bulks.BulkDeploymentQueueEntry;
import net.geant.nmaas.nmservice.deployment.bulks.BulkDeploymentQueueRepository;
import net.geant.nmaas.nmservice.deployment.bulks.BulkDeploymentQueueService;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.events.app.AppAutoDeploymentReviewEvent;
import net.geant.nmaas.orchestration.events.app.AppAutoDeploymentStatusUpdateEvent;
import net.geant.nmaas.portal.api.bulk.CsvApplication;
import net.geant.nmaas.portal.api.bulk.model.BulkQueueDetails;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.BulkDeployment;
import net.geant.nmaas.portal.persistence.entity.BulkDeploymentEntry;
import net.geant.nmaas.portal.persistence.entity.BulkDeploymentState;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.repositories.BulkDeploymentEntryRepository;
import net.geant.nmaas.portal.persistence.repositories.BulkDeploymentRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.ApplicationSubscriptionService;
import net.geant.nmaas.portal.service.BulkApplicationService;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEvent;
import tools.jackson.databind.json.JsonMapper;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.geant.nmaas.portal.api.bulk.BulkType.APPLICATION;
import static net.geant.nmaas.portal.persistence.entity.BulkDeploymentState.COMPLETED;
import static net.geant.nmaas.portal.persistence.entity.BulkDeploymentState.PENDING;
import static net.geant.nmaas.portal.persistence.entity.BulkDeploymentState.PROCESSING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
class BulkApplicationServiceImplTest {

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

    private final UserService userService = mock(UserService.class);
    private final ModelMapper modelMapper = new ModelMapper();

    private final BulkDeploymentQueueRepository bulkDeploymentQueueRepository = mock(BulkDeploymentQueueRepository.class);
    private final BulkDeploymentQueueService bulkDeploymentQueueService = mock(BulkDeploymentQueueService.class);
    private final AppDeploymentRepositoryManager appDeploymentRepositoryManager = mock(AppDeploymentRepositoryManager.class);
    private final ConfigurationManager configurationManager = mock(ConfigurationManager.class);

    final BulkApplicationService bulkApplicationService = new BulkApplicationServiceImpl(applicationBaseService, applicationService,
            domainService, applicationSubscriptionService, userService, applicationInstanceService, appDeploymentMonitor, appLifecycleManager,
            bulkDeploymentRepository, bulkDeploymentEntryRepository, modelMapper, bulkDeploymentQueueRepository,
            appDeploymentRepositoryManager, configurationManager, new JsonMapper());

    @Test
    void shouldHandleBulkDeployment() {
        HashSetValuedHashMap<String, String> parameters = new HashSetValuedHashMap<>();
        parameters.put("param.key1", "value1");
        CsvApplication csvApplication = new CsvApplication("domain1", "testAppInstance", TEST_APP_VERSION, parameters);
        Domain domain = new Domain(1L, "domain1", "domain1");
        Domain global = new Domain(0L, "GLOBAL", "GLOBAL");
        ApplicationBase applicationBase = new ApplicationBase(110L, TEST_APP_NAME);
        when(applicationBaseService.exists(TEST_APP_NAME)).thenReturn(true);
        when(applicationBaseService.findByName(TEST_APP_NAME)).thenReturn(applicationBase);
        Application application = new Application(1L, TEST_APP_NAME, TEST_APP_VERSION);
        application.setAppConfigurationSpec(new AppConfigurationSpec());
        when(applicationService.findApplication(TEST_APP_NAME, TEST_APP_VERSION)).thenReturn(Optional.of(application));
        when(domainService.findDomain(anyString())).thenReturn(Optional.of(domain));
        when(domainService.getGlobalDomain()).thenReturn(Optional.of(global));
        when(applicationInstanceService.isNameAvailableInDomain(anyString(), any())).thenReturn(true);
        AppInstance appInstance = new AppInstance(application, domain, "testAppInstance", false);
        appInstance.setId(100L);
        when(applicationInstanceService.create(any(Domain.class), any(Application.class), anyString(), anyBoolean())).thenReturn(appInstance);
        when(bulkDeploymentEntryRepository.save(any(BulkDeploymentEntry.class))).then(AdditionalAnswers.returnsFirstArg());
        when(bulkDeploymentRepository.save(any(BulkDeployment.class))).thenReturn(new BulkDeployment());
        User user = new User("Test");
        user.setId(1L);
        when(userService.findById(any())).thenReturn(Optional.of(user));

        bulkApplicationService.handleBulkDeployment(TEST_APP_NAME, List.of(csvApplication), testUser(), 2);

        verify(applicationSubscriptionService).subscribe(110L, domain.getId(), true);
        verify(appLifecycleManager).initApplicationDeployment(any());
        ArgumentCaptor<AppInstance> appInstanceArgumentCaptor = ArgumentCaptor.forClass(AppInstance.class);
        verify(applicationInstanceService, times(2)).update(appInstanceArgumentCaptor.capture());
        Map<String, String> deploymentParametersMap = new JsonMapper().readValue(
                appInstanceArgumentCaptor.getAllValues().get(1).getConfiguration(), Map.class
        );
        assertEquals("value1", deploymentParametersMap.get("key1"));
        verify(bulkDeploymentEntryRepository).save(any());
        ArgumentCaptor<BulkDeployment> bulkDeploymentArgumentCaptor = ArgumentCaptor.forClass(BulkDeployment.class);
        verify(bulkDeploymentRepository).save(bulkDeploymentArgumentCaptor.capture());
        BulkDeployment bulkDeployment = bulkDeploymentArgumentCaptor.getValue();
        assertEquals(PROCESSING, bulkDeployment.getState());
        assertEquals(APPLICATION, bulkDeployment.getType());
        assertEquals(testUser().getId(), bulkDeployment.getCreator().getId());
        assertEquals(1, bulkDeployment.getEntries().size());
        assertEquals(PENDING, bulkDeployment.getEntries().getFirst().getState());
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
        event.setWaitIntervalBeforeNextCheckInSeconds(1);
        when(bulkDeploymentEntryRepository.findById(bulkDeploymentId.longValue())).thenReturn(Optional.of(new BulkDeploymentEntry()));
        when(appDeploymentMonitor.state(deploymentId)).thenReturn(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS);

        ApplicationEvent result = bulkApplicationService.handleDeploymentStatusUpdate(event);

        verify(bulkDeploymentEntryRepository, times(0)).save(any(BulkDeploymentEntry.class));
        assertNotNull(result);
        assertInstanceOf(AppAutoDeploymentStatusUpdateEvent.class, result);
    }

    @Test
    void shouldHandleDeploymentStatusUpdateAndTimeout() {
        Identifier bulkDeploymentId = Identifier.newInstance(1L);
        Identifier deploymentId = Identifier.newInstance(2L);
        AppAutoDeploymentStatusUpdateEvent event = new AppAutoDeploymentStatusUpdateEvent(this, bulkDeploymentId, deploymentId);
        event.setEventTimeOutInSeconds(1);
        event.setWaitIntervalBeforeNextCheckInSeconds(2);
        when(bulkDeploymentEntryRepository.findById(bulkDeploymentId.longValue())).thenReturn(Optional.of(new BulkDeploymentEntry()));
        when(appDeploymentMonitor.state(deploymentId)).thenReturn(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS);

        ApplicationEvent result = bulkApplicationService.handleDeploymentStatusUpdate(event);

        ArgumentCaptor<BulkDeploymentEntry> argumentCaptor = ArgumentCaptor.forClass(BulkDeploymentEntry.class);
        verify(bulkDeploymentEntryRepository).save(argumentCaptor.capture());
        assertEquals(BulkDeploymentState.FAILED, argumentCaptor.getValue().getState());
        assertInstanceOf(AppAutoDeploymentReviewEvent.class, result);
    }

    @Test
    void shouldHandleDeploymentReview() {
        AppAutoDeploymentReviewEvent event = new AppAutoDeploymentReviewEvent(this);
        User user = new User("Test");
        user.setId(1L);
        BulkDeployment bAppToBeCompleted = new BulkDeployment(
                1L, user, OffsetDateTime.now(), PROCESSING, APPLICATION,
                new ArrayList<>(List.of(new BulkDeploymentEntry(10L, APPLICATION, COMPLETED, true, null))), 2, false, null);
        BulkDeployment bAppProcessing = new BulkDeployment(
                2L, user, OffsetDateTime.now(), PROCESSING, APPLICATION,
                new ArrayList<>(List.of(new BulkDeploymentEntry(11L, APPLICATION, PROCESSING, true, null))), 2, false, null);
        when(bulkDeploymentRepository.findByTypeAndState(APPLICATION, PROCESSING))
                .thenReturn(List.of(bAppToBeCompleted, bAppProcessing));

        bulkApplicationService.handleDeploymentReview(event);

        verify(bulkDeploymentRepository).findByTypeAndState(APPLICATION, PROCESSING);
        ArgumentCaptor<BulkDeployment> bulkDeploymentArgumentCaptor = ArgumentCaptor.forClass(BulkDeployment.class);
        verify(bulkDeploymentRepository, times(1)).save(bulkDeploymentArgumentCaptor.capture());
        assertEquals(COMPLETED, bulkDeploymentArgumentCaptor.getValue().getState());
    }

    @Test
    void shouldGetQueueDetails() {
        List<BulkDeploymentQueueEntry> queueEntries = new ArrayList<>();
        queueEntries.add(BulkDeploymentQueueEntry.builder().id(1L).bulkEntryId(1L).deploymentId(new Identifier()).state(BulkDeploymentQueueEntry.QueryEntryState.WAITING).build());
        queueEntries.add(BulkDeploymentQueueEntry.builder().id(2L).bulkEntryId(2L).deploymentId(new Identifier()).state(BulkDeploymentQueueEntry.QueryEntryState.IN_PROGRESS).build());
        when(bulkDeploymentQueueRepository.findAll()).thenReturn(queueEntries);
        List<BulkDeploymentEntry> bulkEntries = new ArrayList<>();
        bulkEntries.add(BulkDeploymentEntry.builder().id(1L).type(APPLICATION).state(PENDING).created(true).build());
        bulkEntries.add(BulkDeploymentEntry.builder().id(2L).type(APPLICATION).state(PENDING).created(true).build());
        when(bulkDeploymentEntryRepository.findAll()).thenReturn(bulkEntries);

        BulkDeployment bulkDeployment = new BulkDeployment();
        bulkDeployment.getEntries().addAll(bulkDeploymentEntryRepository.findAll());
        bulkDeployment.setParallelDeploymentsLimit(1);
        when(bulkDeploymentRepository.findBulkIdByBulkEntryId(anyLong())).thenReturn(1L);
        when(bulkDeploymentRepository.findById(1L)).thenReturn(Optional.of(bulkDeployment));


        bulkDeploymentQueueService.handleQueue();
        BulkQueueDetails details = bulkApplicationService.getQueueDetails(1L);

        assertEquals(1, details.getJobInQueue());
        assertEquals(1, details.getJobInProcess());
        assertEquals(1L, details.getJobInProcessId());
        assertEquals(1, details.getBulkJobInQueue());
        assertEquals(0, details.getJobDone());
    }

    @Test
    void shouldGetQueueDetailsGlobal() {
        List<BulkDeploymentQueueEntry> queueEntries = new ArrayList<>();
        queueEntries.add(BulkDeploymentQueueEntry.builder().id(1L).bulkEntryId(1L).deploymentId(new Identifier()).state(BulkDeploymentQueueEntry.QueryEntryState.WAITING).build());
        queueEntries.add(BulkDeploymentQueueEntry.builder().id(2L).bulkEntryId(2L).deploymentId(new Identifier()).state(BulkDeploymentQueueEntry.QueryEntryState.IN_PROGRESS).build());
        when(bulkDeploymentQueueRepository.findAll()).thenReturn(queueEntries);
        List<BulkDeploymentEntry> bulkEntries = new ArrayList<>();
        bulkEntries.add(BulkDeploymentEntry.builder().id(1L).type(APPLICATION).state(PENDING).created(true).build());
        bulkEntries.add(BulkDeploymentEntry.builder().id(2L).type(APPLICATION).state(PENDING).created(true).build());
        bulkEntries.add(BulkDeploymentEntry.builder().id(3L).type(APPLICATION).state(PENDING).created(true).build());
        when(bulkDeploymentEntryRepository.findAll()).thenReturn(bulkEntries);

        BulkDeployment bulkDeployment = new BulkDeployment();
        bulkDeployment.getEntries().addAll(bulkDeploymentEntryRepository.findAll());
        bulkDeployment.setParallelDeploymentsLimit(1);
        when(bulkDeploymentRepository.findBulkIdByBulkEntryId(anyLong())).thenReturn(1L);
        when(bulkDeploymentRepository.findById(1L)).thenReturn(Optional.of(bulkDeployment));

        bulkDeploymentQueueService.handleQueue();
        BulkQueueDetails details = bulkApplicationService.getQueueDetails(1L);

        assertEquals(1, details.getJobInQueue());
        assertEquals(1, details.getJobInProcess());
        assertEquals(1L, details.getJobInProcessId());
        assertEquals(1, details.getBulkJobInQueue());
        assertEquals(1, details.getJobDone());
    }

    private static UserViewMinimal testUser() {
        UserViewMinimal testUser = new UserViewMinimal();
        testUser.setId(1L);
        testUser.setUsername("username");
        return testUser;
    }

}