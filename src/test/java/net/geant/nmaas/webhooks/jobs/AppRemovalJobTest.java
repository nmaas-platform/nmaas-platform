package net.geant.nmaas.webhooks.jobs;

import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.portal.api.domain.WebhookEventDto;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import net.geant.nmaas.portal.service.impl.WebhookEventService;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.web.client.RestClient;

import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppRemovalJobTest {

    private final RestClient restClient = RestClient.create();
    private final WebhookEventService webhookEventService = mock(WebhookEventService.class);
    private final AppDeploymentRepositoryManager appDeploymentRepositoryManager = mock(AppDeploymentRepositoryManager.class);

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
                new WebhookEventDto(10L, "webhook-name", "https://example.webhook-url.pl", WebhookEventType.APPLICATION_REMOVAL));
        when(appDeploymentRepositoryManager.load(Identifier.newInstance("id"))).thenReturn(
                new AppDeployment());

        assertThrows(JobExecutionException.class, () -> {
            AppRemovalJob job = new AppRemovalJob(restClient, webhookEventService, new ModelMapper(), appDeploymentRepositoryManager);
            job.execute(jobExecutionContext);
        });
    }

}
