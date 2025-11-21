package net.geant.nmaas.portal.service;


import net.geant.nmaas.portal.domain.WebhookEventDto;
import net.geant.nmaas.portal.domain.WebhookHistoryDto;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;
import java.util.List;

public interface WebhookHistoryService {

    void create(WebhookEventDto webhook, Object payload, Integer responseStatus, String responseBody);

    WebhookHistoryDto getById(Long id);

    List<WebhookHistoryDto> search(WebhookEventType eventType,
                                   String domainCodename,
                                   String url,
                                   LocalDateTime from,
                                   LocalDateTime to);
}
