package net.geant.nmaas.portal.events;

import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@Getter
@ToString
public class UserDomainAssignmentEvent extends ApplicationEvent {

    private final Long domainId;
    private final Long userId;
    private final String role;
    private final String action;

    public UserDomainAssignmentEvent(Object source, Long domainId, Long userId, String role, String action) {
        super(source);
        this.domainId = domainId;
        this.userId = userId;
        this.role = role;
        this.action = action;
    }

}
