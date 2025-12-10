package net.geant.nmaas.portal.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class DomainActionDto {

    private DomainBase domainView;
    private String action;
    private WebhookEventType webhookEventType;
}
