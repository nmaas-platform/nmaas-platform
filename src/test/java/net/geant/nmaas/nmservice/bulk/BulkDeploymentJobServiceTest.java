package net.geant.nmaas.nmservice.bulk;

import net.geant.nmaas.nmservice.deployment.bulks.BulkDeploymentQueueEntry;
import net.geant.nmaas.nmservice.deployment.bulks.BulkDeploymentQueueRepository;
import net.geant.nmaas.nmservice.deployment.bulks.BulkDeploymentQueueService;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.portal.api.configuration.ConfigurationView;
import net.geant.nmaas.portal.service.BulkApplicationService;
import net.geant.nmaas.portal.service.ConfigurationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BulkDeploymentJobServiceTest {

    AppDeploymentMonitor appDeploymentMonitor = mock(AppDeploymentMonitor.class);
    AppDeploymentRepositoryManager appDeploymentRepositoryManager = mock(AppDeploymentRepositoryManager.class);
    BulkDeploymentQueueRepository bulkDeploymentQueueRepository = mock(BulkDeploymentQueueRepository.class);
    ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    BulkApplicationService bulkApplicationService = mock(BulkApplicationService.class);

    AppLifecycleManager appLifecycleManager = mock(AppLifecycleManager.class);

    ConfigurationManager configurationManager = mock(ConfigurationManager.class);

    BulkDeploymentQueueService underTest;

    @BeforeEach
    void setup() {
        underTest = new BulkDeploymentQueueService(appDeploymentMonitor, appDeploymentRepositoryManager, bulkDeploymentQueueRepository, eventPublisher, bulkApplicationService, appLifecycleManager, configurationManager);
       when(configurationManager.getConfiguration()).thenReturn(ConfigurationView.builder().parallelDeploymentsLimit(2).build());
    }

    @Test
    void shouldTriggerDeployment() {
        BulkDeploymentQueueEntry entry = new BulkDeploymentQueueEntry(1L, new Identifier(UUID.randomUUID().toString()), 1L, "params");
        BulkDeploymentQueueEntry entry2 = new BulkDeploymentQueueEntry(2L, new Identifier(UUID.randomUUID().toString()), 2L, "params");
        BulkDeploymentQueueEntry entry3 = new BulkDeploymentQueueEntry(3L, new Identifier(UUID.randomUUID().toString()), 3L, "params");
        when(bulkDeploymentQueueRepository.findAll()).thenReturn(List.of(entry, entry2, entry3));
        when(appDeploymentMonitor.state(any())).thenReturn(AppLifecycleState.REQUESTED);
        when(appDeploymentRepositoryManager.loadState(any())).thenReturn(AppDeploymentState.REQUEST_VALIDATED);
        underTest.handleQueue();

        //Limit set to 2 in properties
        verify(eventPublisher, times(2)).publishEvent(any());
    }

    @Test
    void shouldTriggerDeleteEntryJob() {
        BulkDeploymentQueueEntry entry = new BulkDeploymentQueueEntry(1L, new Identifier(UUID.randomUUID().toString()), 1L, "params");
        BulkDeploymentQueueEntry entry2 = new BulkDeploymentQueueEntry(2L, new Identifier(UUID.randomUUID().toString()), 2L, "params");
        BulkDeploymentQueueEntry entry3 = new BulkDeploymentQueueEntry(3L, new Identifier(UUID.randomUUID().toString()), 3L, "params");
        bulkDeploymentQueueRepository.save(entry);
        bulkDeploymentQueueRepository.save(entry2);
        bulkDeploymentQueueRepository.save(entry3);

//        when(bulkDeploymentJobRepository.findAll()).thenReturn(List.of(entry, entry2, entry3));
        when(appDeploymentMonitor.state(any())).thenReturn(AppLifecycleState.APPLICATION_DEPLOYED);
        when(appDeploymentRepositoryManager.loadState(any())).thenReturn(AppDeploymentState.APPLICATION_DEPLOYMENT_FAILED);
        underTest.handleQueue();

        //Limit set to 2 in properties
        verify(eventPublisher, times(0)).publishEvent(any());
        assertEquals(0, bulkDeploymentQueueRepository.findAll().size());
    }

}
