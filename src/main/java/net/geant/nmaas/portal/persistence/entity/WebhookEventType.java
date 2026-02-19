package net.geant.nmaas.portal.persistence.entity;

import net.geant.nmaas.api.dto.webhooks.WebhookEventTypeDto;

public enum WebhookEventType {

    DOMAIN_ACTION,
    DOMAIN_GROUP_ACTION,
    APPLICATION_DEPLOYMENT,
    APPLICATION_REMOVAL,
    USER_ASSIGNMENT;

    public static WebhookEventType from(WebhookEventTypeDto dto) {
        return WebhookEventType.valueOf(dto.name());
    }

}