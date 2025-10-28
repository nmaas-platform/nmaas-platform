package net.geant.nmaas.webhooks.jobs;

import net.geant.nmaas.portal.api.domain.WebhookEventDto;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserDomainAssignmentJobTest {

    private final RestClient restClient = RestClient.create();
    private final WebhookEventService webhookEventService = mock(WebhookEventService.class);
    private final DomainService domainService = mock(DomainService.class);
    private final UserService userService = mock(UserService.class);

    @Test
    void shouldExecuteSampleJob() throws GeneralSecurityException {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("webhookId", 10L);
        dataMap.put("domainId", 1L);
        dataMap.put("userId", 8L);
        dataMap.put("role", Role.ROLE_DOMAIN_ADMIN.name());
        dataMap.put("action", "ADD");
        JobDetail jobDetail = mock(JobDetail.class);
        when(jobDetail.getJobDataMap()).thenReturn(dataMap);
        JobExecutionContext jobExecutionContext = mock(JobExecutionContext.class);
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        when(webhookEventService.getById(10L)).thenReturn(
                new WebhookEventDto(10L, "webhook-name", "https://example.webhook-url.pl", WebhookEventType.USER_ASSIGNMENT));
        when(domainService.findDomain(1L)).thenReturn(Optional.of(new Domain("name", "codename")));
        when(userService.findById(8L)).thenReturn(Optional.of(new User("name", true)));

        assertThrows(JobExecutionException.class, () -> {
            UserDomainAssignmentJob job =
                    new UserDomainAssignmentJob(restClient, webhookEventService, new ModelMapper(), domainService, userService);
            job.execute(jobExecutionContext);
        });
        verify(webhookEventService).getById(10L);
    }

}
