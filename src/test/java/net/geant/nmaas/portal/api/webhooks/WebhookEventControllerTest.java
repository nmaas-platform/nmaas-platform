package net.geant.nmaas.portal.api.webhooks;

import net.geant.nmaas.api.dto.Id;
import net.geant.nmaas.api.dto.domains.DomainBaseDto;
import net.geant.nmaas.api.dto.webhooks.WebhookEventDto;
import net.geant.nmaas.api.dto.webhooks.WebhookEventTypeDto;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistence.entity.WebhookEvent;
import net.geant.nmaas.portal.service.impl.WebhookEventService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.security.GeneralSecurityException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebhookEventControllerTest {

    private final WebhookEventService webhookEventService = mock(WebhookEventService.class);
    private final WebhookEventController controller = new WebhookEventController(webhookEventService);

    @Test
    void shouldCreateWebhook() throws GeneralSecurityException {
        WebhookEventDto request = webhookEventDto(10L, null);
        WebhookEvent created = new WebhookEvent();
        created.setId(55L);
        when(webhookEventService.create(request)).thenReturn(created);

        ResponseEntity<Id> result = controller.createWebhook(request);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(55L, result.getBody().id());
        verify(webhookEventService).create(request);
    }

    @Test
    void shouldThrowOnUpdateWhenPathIdDoesNotMatchBodyId() {
        WebhookEventDto request = webhookEventDto(12L, null);

        assertThrows(ProcessingException.class, () -> controller.updateWebhook(13L, request));
    }

    @Test
    void shouldUpdateWebhookInDomain() throws GeneralSecurityException {
        WebhookEventDto request = webhookEventDto(20L, domain(2L));
        when(webhookEventService.update(2L, request)).thenReturn(request);

        ResponseEntity<WebhookEventDto> result = controller.updateWebhookInDomain(2L, 20L, request, null);

        assertEquals(200, result.getStatusCode().value());
        assertSame(request, result.getBody());
        verify(webhookEventService).update(2L, request);
    }

    @Test
    void shouldThrowOnCreateWebhookInDomainWhenDomainDoesNotMatch() {
        WebhookEventDto request = webhookEventDto(null, domain(3L));

        assertThrows(IllegalArgumentException.class, () -> controller.createWebhookInDomain(2L, request));
    }

    @Test
    void shouldThrowOnUpdateWebhookInDomainWhenIdentifiersDoNotMatch() {
        WebhookEventDto request = webhookEventDto(20L, domain(3L));

        assertThrows(ProcessingException.class, () -> controller.updateWebhookInDomain(2L, 20L, request, null));
    }

    @Test
    void shouldGetAllWebhooksPageable() {
        PageRequest pageable = PageRequest.of(0, 15);
        Page<WebhookEventDto> page = new PageImpl<>(List.of(webhookEventDto(1L, null)));
        when(webhookEventService.getAllWebhooks(pageable, "hook")).thenReturn(page);

        Page<WebhookEventDto> result = controller.getAllWebhooksPageable(pageable, "hook");

        assertSame(page, result);
        verify(webhookEventService).getAllWebhooks(pageable, "hook");
    }

    private static WebhookEventDto webhookEventDto(Long id, DomainBaseDto domain) {
        WebhookEventDto dto = new WebhookEventDto(id, "webhook", "https://example.test/webhook", WebhookEventTypeDto.APPLICATION_DEPLOYMENT);
        dto.setDomain(domain);
        return dto;
    }

    private static DomainBaseDto domain(Long id) {
        DomainBaseDto dto = new DomainBaseDto();
        dto.setId(id);
        dto.setCodename("D" + id);
        dto.setName("domain-" + id);
        return dto;
    }
}
