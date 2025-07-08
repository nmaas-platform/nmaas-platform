package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.geant.nmaas.portal.persistent.entity.WebhookEventType;

@AllArgsConstructor
@Getter
@Setter
public class DomainGroupWebhookDto {

    private DomainGroupView domainGroup;
    private String action;
    private WebhookEventType webhookEventType;

}
