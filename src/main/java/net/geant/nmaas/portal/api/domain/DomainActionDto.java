package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;

@AllArgsConstructor
@Getter
@Setter
public class DomainActionDto {

    private DomainBase domainView;
    private String action;
    private WebhookEventType webhookEventType;
}
