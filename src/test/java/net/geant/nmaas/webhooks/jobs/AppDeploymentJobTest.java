package net.geant.nmaas.webhooks.jobs;

import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.portal.domain.WebhookEventDto;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import net.geant.nmaas.portal.persistence.entity.WebhookHistory;
import net.geant.nmaas.portal.persistence.repositories.WebhookHistoryRepository;
import net.geant.nmaas.portal.service.WebhookHistoryService;
import net.geant.nmaas.portal.service.impl.WebhookEventService;
import net.geant.nmaas.portal.service.impl.WebhookHistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.web.client.RestClient;

import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppDeploymentJobTest {

    private final RestClient restClient = RestClient.create();
    private final WebhookEventService webhookEventService = mock(WebhookEventService.class);
    private final AppDeploymentRepositoryManager appDeploymentRepositoryManager = mock(AppDeploymentRepositoryManager.class);

    private final ModelMapper mapper = new ModelMapper();

    private final WebhookHistoryRepository webhookHistoryRepository = mock(WebhookHistoryRepository.class);

    private WebhookHistoryService webhookHistoryService;

    @BeforeEach
    void setUp() {
        webhookHistoryService = new WebhookHistoryServiceImpl(webhookHistoryRepository, mapper);
    }

    @Test
    void shouldExecuteSampleJob() throws GeneralSecurityException {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("webhookId", 10L);
        dataMap.put("deploymentId", "id");
        JobDetail jobDetail = mock(JobDetail.class);
        when(jobDetail.getJobDataMap()).thenReturn(dataMap);
        JobExecutionContext jobExecutionContext = mock(JobExecutionContext.class);
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        when(webhookEventService.getById(10L)).thenReturn(
                new WebhookEventDto(10L, "webhook-name", "https://example.webhook-url.pl", WebhookEventType.APPLICATION_DEPLOYMENT));
        when(appDeploymentRepositoryManager.load(Identifier.newInstance("id"))).thenReturn(
                new AppDeployment());

        assertThrows(JobExecutionException.class, () -> {
            AppDeploymentJob job = new AppDeploymentJob(restClient, webhookEventService, mapper, webhookHistoryService, appDeploymentRepositoryManager);
            job.execute(jobExecutionContext);
        });

        ArgumentCaptor<WebhookHistory> webhookHistoryCaptor = ArgumentCaptor.forClass(WebhookHistory.class);
        verify(webhookHistoryRepository).save(webhookHistoryCaptor.capture());
        WebhookHistory savedHistory = webhookHistoryCaptor.getValue();
        assertNotNull(savedHistory);
        assertEquals(WebhookEventType.APPLICATION_DEPLOYMENT, savedHistory.getEventType());
        assertEquals("https://example.webhook-url.pl", savedHistory.getUrl());
        assertNull(savedHistory.getResponseBody());
        assertNotNull(savedHistory.getExecutionTimestamp());
    }

}
