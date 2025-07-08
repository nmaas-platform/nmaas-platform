package net.geant.nmaas.portal.events;

import lombok.Getter;
import lombok.ToString;
import net.geant.nmaas.portal.api.domain.DomainGroupView;
import org.springframework.context.ApplicationEvent;

@Getter
@ToString
public class DomainGroupChangedEvent extends ApplicationEvent {

    private final String action;
    private final DomainGroupView domainGroup;

    public DomainGroupChangedEvent(Object source, String action, DomainGroupView domainGroup) {
        super(source);
        this.action = action;
        this.domainGroup = domainGroup;
    }

}