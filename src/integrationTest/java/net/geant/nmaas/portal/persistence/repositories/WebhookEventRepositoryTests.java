package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.WebhookEvent;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import org.junit.jupiter.api.AfterEach;
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

    @AfterEach
    void cleanup() {
        repository.deleteAll();
    }

    @Test
    void shouldPersistWebhookEvent() {
        WebhookEvent testEvent = new WebhookEvent(null, "wh1", "https://target.wh1", WebhookEventType.DOMAIN_ACTION);
        WebhookEvent saved = repository.save(testEvent);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void shouldFindWebhookEventByRemovalWithEmptyDomain() {
        WebhookEvent testEvent = new WebhookEvent(null, "wh1", "https://target.wh1", WebhookEventType.APPLICATION_REMOVAL);
        testEvent.setDomain(null);
        repository.save(testEvent);

        assertThat(repository.findIdByEventType(WebhookEventType.APPLICATION_REMOVAL)).isNotEmpty();
    }

    @Test
    void shouldFindWebhookEventByDeploymentWithDomain() {
        WebhookEvent testEvent = new WebhookEvent(null, "wh1", "https://target.wh1", WebhookEventType.APPLICATION_DEPLOYMENT);
        testEvent.setDomain(storeDomain("D1", "d1"));
        repository.save(testEvent);
        WebhookEvent testEvent2 = new WebhookEvent(null, "wh2", "https://target.wh2", WebhookEventType.APPLICATION_DEPLOYMENT);
        testEvent2.setDomain(storeDomain("D2", "d2"));
        repository.save(testEvent2);
        WebhookEvent testEvent3 = new WebhookEvent(null, "wh3", "https://target.wh3", WebhookEventType.APPLICATION_DEPLOYMENT);
        testEvent3.setDomain(null);
        repository.save(testEvent3);

        assertThat(repository.findIdByEventType(WebhookEventType.APPLICATION_DEPLOYMENT)).isNotEmpty();
        List<Long> ids = repository.findIdByEventTypeAndDomain(WebhookEventType.APPLICATION_DEPLOYMENT, "d1");
        assertThat(ids).hasSize(2);
    }

    @Test
    void shouldFindGlobalWebhookAndWebhookOfGivenDomainByCodename() {
        repository.save(globalWebhook(WebhookEventType.APPLICATION_DEPLOYMENT));
        repository.save(domainWebhook(WebhookEventType.APPLICATION_DEPLOYMENT, storeDomain("D1", "d1")));
        Domain otherDomain = storeDomain("D2", "d2");
        repository.save(domainWebhook(WebhookEventType.APPLICATION_DEPLOYMENT, otherDomain));

        List<Long> ids = repository.findIdByEventTypeAndDomain(WebhookEventType.APPLICATION_DEPLOYMENT, "d2");

        assertThat(ids).hasSize(2);
    }

    @Test
    void shouldFindGlobalWebhookAndWebhookOfGivenDomainByDomainId() {
        repository.save(globalWebhook(WebhookEventType.USER_ASSIGNMENT));
        repository.save(domainWebhook(WebhookEventType.USER_ASSIGNMENT, storeDomain("D1", "d1")));
        Domain otherDomain = storeDomain("D2", "d2");
        repository.save(domainWebhook(WebhookEventType.USER_ASSIGNMENT, otherDomain));

        List<Long> ids = repository.findIdByEventTypeAndDomain(WebhookEventType.USER_ASSIGNMENT, otherDomain.getId());

        assertThat(ids).hasSize(2);
    }

    private WebhookEvent globalWebhook(WebhookEventType eventType) {
        WebhookEvent webhook = new WebhookEvent(null, "global", "https://target.global", eventType);
        webhook.setDomain(null);
        return webhook;
    }

    private WebhookEvent domainWebhook(WebhookEventType eventType, Domain domain) {
        WebhookEvent webhook = new WebhookEvent(null, "scoped", "https://target.scoped", eventType);
        webhook.setDomain(domain);
        return webhook;
    }

    private Domain storeDomain(String name, String codename) {
        return domainRepository.findByCodename(codename)
                .orElseGet(() -> domainRepository.save(new Domain(name, codename)));
    }

}
