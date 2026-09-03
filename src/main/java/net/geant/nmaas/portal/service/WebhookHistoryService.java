package net.geant.nmaas.portal.service;

import net.geant.nmaas.api.dto.webhooks.WebhookEventDto;
import net.geant.nmaas.api.dto.webhooks.WebhookHistoryDto;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface WebhookHistoryService {

    void create(
            WebhookEventDto webhook,
            Object payload,
            Integer responseStatus,
            String responseBody,
            String requestBody
    );

    WebhookHistoryDto getById(Long id);

    List<WebhookHistoryDto> search(
            Long webhookEventId,
            WebhookEventType eventType,
            String domainCodename,
            LocalDateTime from,
            LocalDateTime to
    );

    Page<WebhookHistoryDto> search(
            Long webhookEventId,
            WebhookEventType eventType,
            String domainCodename,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );

    List<WebhookHistoryDto> search(
            Long webhookEventId,
            WebhookEventType eventType,
            Long domainId,
            LocalDateTime from,
            LocalDateTime to
    );

    Page<WebhookHistoryDto> search(
            Long webhookEventId,
            WebhookEventType eventType,
            Long domainId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );
}
