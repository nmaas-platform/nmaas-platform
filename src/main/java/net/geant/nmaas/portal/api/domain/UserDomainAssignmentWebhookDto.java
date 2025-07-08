package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.geant.nmaas.portal.persistent.entity.Role;

@AllArgsConstructor
@Getter
@Setter
public class UserDomainAssignmentWebhookDto {

    private UserView user;
    private DomainView domain;
    private Role role;
    private String action;
}
