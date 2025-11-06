package net.geant.nmaas.webhooks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.geant.nmaas.portal.domain.DomainGroupView;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;

@AllArgsConstructor
@Getter
@Setter
public class DomainGroupWebhookDto {

    private DomainGroupView domainGroup;
    private String action;
    private WebhookEventType webhookEventType;

}
