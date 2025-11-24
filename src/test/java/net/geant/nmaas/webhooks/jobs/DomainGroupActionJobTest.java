package net.geant.nmaas.webhooks.jobs;

import net.geant.nmaas.portal.domain.DomainGroupView;
import net.geant.nmaas.portal.domain.WebhookEventDto;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import net.geant.nmaas.portal.persistence.entity.WebhookHistory;
import net.geant.nmaas.portal.persistence.repositories.DomainRepository;
import net.geant.nmaas.portal.persistence.repositories.WebhookHistoryRepository;
import net.geant.nmaas.portal.service.WebhookHistoryService;
import net.geant.nmaas.portal.service.impl.WebhookEventService;
import net.geant.nmaas.portal.service.impl.WebhookHistoryServiceImpl;
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

class DomainGroupActionJobTest {

    private final RestClient restClient = RestClient.create();
    private final WebhookEventService webhookEventService = mock(WebhookEventService.class);
    private final DomainRepository domainRepository = mock(DomainRepository.class);
    private final WebhookHistoryRepository webhookHistoryRepository = mock(WebhookHistoryRepository.class);

    private final ModelMapper mapper = new ModelMapper();
    private final WebhookHistoryService webhookHistoryService = new WebhookHistoryServiceImpl(webhookHistoryRepository, domainRepository, mapper);

    @Test
    void shouldExecuteSampleJob() throws GeneralSecurityException {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("webhookId", 10L);
        dataMap.put("domainId", 1L);
        DomainGroupView group = DomainGroupView.builder().id(50L).codename("g1").name("Group 1").build();
        dataMap.put("domainGroup", group);
        JobDetail jobDetail = mock(JobDetail.class);
        when(jobDetail.getJobDataMap()).thenReturn(dataMap);
        JobExecutionContext jobExecutionContext = mock(JobExecutionContext.class);
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        when(webhookEventService.getById(10L)).thenReturn(
                new WebhookEventDto(10L, "webhook-name", "https://example.webhook-url.pl", WebhookEventType.DOMAIN_GROUP_ACTION));

        assertThrows(JobExecutionException.class, () -> {
            DomainGroupActionJob job = new DomainGroupActionJob(restClient, webhookEventService, mapper, webhookHistoryService);
            job.execute(jobExecutionContext);
        });
        verify(webhookEventService).getById(10L);
        ArgumentCaptor<WebhookHistory> webhookHistoryCaptor = ArgumentCaptor.forClass(WebhookHistory.class);
        verify(webhookHistoryRepository).save(webhookHistoryCaptor.capture());
        WebhookHistory savedHistory = webhookHistoryCaptor.getValue();
        assertNotNull(savedHistory);
        assertEquals(WebhookEventType.DOMAIN_GROUP_ACTION, savedHistory.getEventType());
        assertEquals("https://example.webhook-url.pl", savedHistory.getUrl());
        assertNull(savedHistory.getResponseBody());
        assertNotNull(savedHistory.getExecutionTimestamp());
    }

}
