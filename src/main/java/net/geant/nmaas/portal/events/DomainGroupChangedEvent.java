package net.geant.nmaas.portal.events;

import lombok.Getter;
import lombok.ToString;
import net.geant.nmaas.api.dto.domains.DomainGroupDto;
import org.springframework.context.ApplicationEvent;

@Getter
@ToString
public class DomainGroupChangedEvent extends ApplicationEvent {

    private final String action;
    private final DomainGroupDto domainGroup;

    public DomainGroupChangedEvent(Object source, String action, DomainGroupDto domainGroup) {
        super(source);
        this.action = action;
        this.domainGroup = domainGroup;
    }

}