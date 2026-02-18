package net.geant.nmaas.portal.service;

import net.geant.nmaas.api.dto.webhooks.WebhookEventDto;
import net.geant.nmaas.api.dto.webhooks.WebhookHistoryDto;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;

import java.time.LocalDateTime;
import java.util.List;

public interface WebhookHistoryService {

    void create(WebhookEventDto webhook, Object payload, Integer responseStatus, String responseBody);

    WebhookHistoryDto getById(Long id);

    List<WebhookHistoryDto> search(Long webhookEventId, WebhookEventType eventType, String domainCodename,
                                   LocalDateTime from, LocalDateTime to);

    List<WebhookHistoryDto> search(Long webhookEventId, WebhookEventType eventType, Long domainId,
                                   LocalDateTime from, LocalDateTime to);
}
