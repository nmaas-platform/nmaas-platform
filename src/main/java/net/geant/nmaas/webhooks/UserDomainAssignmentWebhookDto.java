package net.geant.nmaas.webhooks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.portal.domain.DomainBase;
import net.geant.nmaas.portal.domain.UserView;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class UserDomainAssignmentWebhookDto {

    private UserView user;
    private DomainBase domain;
    private Role role;
    private String action;
    private WebhookEventType webhookEventType;
}
