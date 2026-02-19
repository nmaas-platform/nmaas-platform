package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.api.dto.webhooks.WebhookEventTypeDto;
import net.geant.nmaas.api.dto.webhooks.WebhookHistoryDto;
import net.geant.nmaas.portal.persistence.entity.WebhookHistory;
import org.modelmapper.AbstractConverter;

public class WebhookHistoryConverter extends AbstractConverter<WebhookHistory, WebhookHistoryDto> {

    @Override
    protected WebhookHistoryDto convert(WebhookHistory source) {
        return new WebhookHistoryDto(
                source.getId(), source.getWebhookEventId(), WebhookEventTypeDto.valueOf(source.getEventType().name()),
                source.getDomainCodename(), source.getUrl(), source.getRequestBody(), source.getResponseStatus(),
                source.getResponseBody(), source.getExecutionTimestamp()
        );
    }

}
