package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;

@AllArgsConstructor
@Getter
@Setter
public class UserDomainAssignmentWebhookDto {

    private UserView user;
    private DomainBase domain;
    private Role role;
    private String action;
    private WebhookEventType webhookEventType;
}
