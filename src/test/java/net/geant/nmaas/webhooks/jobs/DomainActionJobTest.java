package net.geant.nmaas.webhooks.jobs;

import net.geant.nmaas.portal.api.domain.DomainView;
import net.geant.nmaas.portal.api.domain.WebhookEventDto;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.WebhookEventType;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.impl.WebhookEventService;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.web.client.RestClient;

import java.security.GeneralSecurityException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DomainActionJobTest {

    private final RestClient restClient = RestClient.create();
    private final WebhookEventService webhookEventService = mock(WebhookEventService.class);
    private final DomainService domainService = mock(DomainService.class);

    @Test
    void shouldExecuteSampleJob() throws GeneralSecurityException {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("webhookId", 10L);
        DomainView domain = new DomainView();
        domain.setId(1L);
        domain.setName("name");
        domain.setCodename("codename");
        dataMap.put("domain", domain);
        dataMap.put("action", "create");
        JobDetail jobDetail = mock(JobDetail.class);
        when(jobDetail.getJobDataMap()).thenReturn(dataMap);
        JobExecutionContext jobExecutionContext = mock(JobExecutionContext.class);
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        when(webhookEventService.getById(10L)).thenReturn(
                new WebhookEventDto(10L, "webhook-name", "https://example.webhook-url.pl", WebhookEventType.DOMAIN_ACTION));
        when(domainService.findDomain(1L)).thenReturn(Optional.of(new Domain("name", "codename")));

        assertThrows(JobExecutionException.class, () -> {
            DomainActionJob job = new DomainActionJob(restClient, webhookEventService, new ModelMapper());
            job.execute(jobExecutionContext);
        });
    }

}
