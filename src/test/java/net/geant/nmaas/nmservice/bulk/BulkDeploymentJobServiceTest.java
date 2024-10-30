package net.geant.nmaas.nmservice.bulk;

import net.geant.nmaas.nmservice.deployment.bulks.BulkDeploymentJobEntry;
import net.geant.nmaas.nmservice.deployment.bulks.BulkDeploymentJobRepository;
import net.geant.nmaas.nmservice.deployment.bulks.BulkDeploymentJobService;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
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

public class BulkDeploymentJobServiceTest {

    AppDeploymentMonitor appDeploymentMonitor = mock(AppDeploymentMonitor.class);

    AppDeploymentRepositoryManager appDeploymentRepositoryManager = mock(AppDeploymentRepositoryManager.class);

    BulkDeploymentJobRepository bulkDeploymentJobRepository = mock(BulkDeploymentJobRepository.class);

    ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

    BulkDeploymentJobService underService;

    @BeforeEach
    void setup() {
        underService = new BulkDeploymentJobService(appDeploymentMonitor, appDeploymentRepositoryManager, bulkDeploymentJobRepository, eventPublisher);
        underService.deploymentLimitPerMinute = 2;
    }

    @Test
    public void shouldTriggerDeployment() {
        BulkDeploymentJobEntry entry = new BulkDeploymentJobEntry(1L, new Identifier(UUID.randomUUID().toString()));
        BulkDeploymentJobEntry entry2 = new BulkDeploymentJobEntry(2L, new Identifier(UUID.randomUUID().toString()));
        BulkDeploymentJobEntry entry3 = new BulkDeploymentJobEntry(3L, new Identifier(UUID.randomUUID().toString()));

        when(bulkDeploymentJobRepository.findAll()).thenReturn(List.of(entry, entry2, entry3));
        when(appDeploymentMonitor.state(any())).thenReturn(AppLifecycleState.REQUESTED);
        when(appDeploymentRepositoryManager.loadState(any())).thenReturn(AppDeploymentState.REQUEST_VALIDATED);
        underService.checkStatusAndDeploy();

        //Limit set to 2 in properties
        verify(eventPublisher, times(2)).publishEvent(any());
    }

    @Test
    public void shouldTriggerDeleteEntryJob() {
        BulkDeploymentJobEntry entry = new BulkDeploymentJobEntry(1L, new Identifier(UUID.randomUUID().toString()));
        BulkDeploymentJobEntry entry2 = new BulkDeploymentJobEntry(2L, new Identifier(UUID.randomUUID().toString()));
        BulkDeploymentJobEntry entry3 = new BulkDeploymentJobEntry(3L, new Identifier(UUID.randomUUID().toString()));
        bulkDeploymentJobRepository.save(entry);
        bulkDeploymentJobRepository.save(entry2);
        bulkDeploymentJobRepository.save(entry3);


//        when(bulkDeploymentJobRepository.findAll()).thenReturn(List.of(entry, entry2, entry3));
        when(appDeploymentMonitor.state(any())).thenReturn(AppLifecycleState.APPLICATION_DEPLOYED);
        when(appDeploymentRepositoryManager.loadState(any())).thenReturn(AppDeploymentState.APPLICATION_DEPLOYMENT_FAILED);
        underService.checkStatusAndDeploy();

        //Limit set to 2 in properties
        verify(eventPublisher, times(0)).publishEvent(any());
        assertEquals(0, bulkDeploymentJobRepository.findAll().size());
    }
}
