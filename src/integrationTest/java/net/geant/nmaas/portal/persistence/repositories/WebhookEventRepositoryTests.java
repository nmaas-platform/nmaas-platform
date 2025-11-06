package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.WebhookEvent;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class WebhookEventRepositoryTests {

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private WebhookEventRepository repository;

    @Test
    void shouldPersistWebhookEvent() {
        WebhookEvent testEvent = new WebhookEvent(null, "wh1", "https://target.wh1", WebhookEventType.DOMAIN_ACTION);
        WebhookEvent saved = repository.save(testEvent);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void shouldFindWebhookEventByDeploymentWithEmptyDomain() {
        WebhookEvent testEvent = new WebhookEvent(null, "wh1", "https://target.wh1", WebhookEventType.APPLICATION_REMOVAL);
        // domain is null
        testEvent.setDomain(null);
        repository.save(testEvent);

        assertThat(repository.findIdByEventType(WebhookEventType.APPLICATION_REMOVAL)).isNotEmpty();

        List<Long> ids = repository.findIdByEventTypeAndDomain(WebhookEventType.APPLICATION_REMOVAL, "d1");
        assertThat(ids).isNotEmpty();
    }

    @Test
    void shouldFindWebhookEventByDeploymentWithDomain() {
        WebhookEvent testEvent = new WebhookEvent(null, "wh1", "https://target.wh1", WebhookEventType.APPLICATION_DEPLOYMENT);
        testEvent.setDomain(domainRepository.save(new Domain("D1", "d1")));
        repository.save(testEvent);
        WebhookEvent testEvent2 = new WebhookEvent(null, "wh1", "https://target.wh1", WebhookEventType.APPLICATION_DEPLOYMENT);
        testEvent2.setDomain(domainRepository.save(new Domain("D2", "d2")));
        repository.save(testEvent2);
        WebhookEvent testEvent3 = new WebhookEvent(null, "wh1", "https://target.wh1", WebhookEventType.APPLICATION_DEPLOYMENT);
        testEvent3.setDomain(null);
        repository.save(testEvent3);

        assertThat(repository.findIdByEventType(WebhookEventType.APPLICATION_DEPLOYMENT)).isNotEmpty();

        List<Long> ids = repository.findIdByEventTypeAndDomain(WebhookEventType.APPLICATION_DEPLOYMENT, "d1");
        assertThat(ids).hasSize(2);
        System.out.println(ids);
    }

}
