package net.geant.nmaas.portal.events;

import lombok.Getter;
import lombok.ToString;
import net.geant.nmaas.api.dto.domains.DomainDto;
import org.springframework.context.ApplicationEvent;

@Getter
@ToString
public class DomainRemovalEvent extends ApplicationEvent {
    private final DomainDto domain;
    private final boolean hardRemoval;

    public DomainRemovalEvent(Object source, DomainDto domain, boolean hardRemoval) {
        super(source);
        this.domain = domain;
        this.hardRemoval = hardRemoval;
    }

}
