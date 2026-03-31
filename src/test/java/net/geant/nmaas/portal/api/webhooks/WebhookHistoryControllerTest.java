package net.geant.nmaas.portal.api.webhooks;

import net.geant.nmaas.api.dto.webhooks.WebhookEventTypeDto;
import net.geant.nmaas.api.dto.webhooks.WebhookHistoryDto;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import net.geant.nmaas.portal.service.WebhookHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebhookHistoryControllerTest {

    private final WebhookHistoryService webhookHistoryService = mock(WebhookHistoryService.class);
    private final WebhookHistoryController controller = new WebhookHistoryController(webhookHistoryService);

    @Test
    void shouldGetWebhookHistory() {
        WebhookHistoryDto dto = historyDto(1L, 10L);
        when(webhookHistoryService.getById(1L)).thenReturn(dto);

        ResponseEntity<WebhookHistoryDto> result = controller.getWebhookHistory(1L);

        assertEquals(200, result.getStatusCode().value());
        assertSame(dto, result.getBody());
        verify(webhookHistoryService).getById(1L);
    }

    @Test
    void shouldSearchWebhookHistory() {
        LocalDateTime from = LocalDateTime.of(2026, 3, 1, 9, 0);
        LocalDateTime to = LocalDateTime.of(2026, 3, 2, 11, 0);
        List<WebhookHistoryDto> expected = List.of(historyDto(2L, 20L));
        when(webhookHistoryService.search(20L, WebhookEventType.DOMAIN_ACTION, "D1", from, to)).thenReturn(expected);

        ResponseEntity<List<WebhookHistoryDto>> result = controller.search(20L, WebhookEventType.DOMAIN_ACTION, "D1", from, to);

        assertSame(expected, result.getBody());
        verify(webhookHistoryService).search(20L, WebhookEventType.DOMAIN_ACTION, "D1", from, to);
    }

    @Test
    void shouldSearchWebhookHistoryInDomain() {
        LocalDateTime from = LocalDateTime.of(2026, 3, 1, 8, 0);
        LocalDateTime to = LocalDateTime.of(2026, 3, 1, 12, 0);
        List<WebhookHistoryDto> expected = List.of(historyDto(3L, 30L));
        when(webhookHistoryService.search(30L, WebhookEventType.APPLICATION_DEPLOYMENT, 7L, from, to)).thenReturn(expected);

        ResponseEntity<List<WebhookHistoryDto>> result = controller.searchInDomain(7L, 30L, WebhookEventType.APPLICATION_DEPLOYMENT, from, to);

        assertSame(expected, result.getBody());
        verify(webhookHistoryService).search(30L, WebhookEventType.APPLICATION_DEPLOYMENT, 7L, from, to);
    }

    private static WebhookHistoryDto historyDto(Long id, Long eventId) {
        return new WebhookHistoryDto(
                id,
                eventId,
                WebhookEventTypeDto.APPLICATION_DEPLOYMENT,
                "D1",
                "https://example.test/webhook",
                "{\"ok\":true}",
                200,
                "accepted",
                LocalDateTime.of(2026, 3, 1, 10, 0)
        );
    }
}
