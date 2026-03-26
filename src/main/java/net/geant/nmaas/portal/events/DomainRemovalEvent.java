package net.geant.nmaas.portal.events;

import lombok.Getter;
import lombok.ToString;
import net.geant.nmaas.api.dto.domains.DomainDto;
import org.springframework.context.ApplicationEvent;

@Getter
@ToString
public class DomainRemovalEvent extends ApplicationEvent {
    private final DomainDto domainView;
    private final boolean hardRemoval;

    public DomainRemovalEvent(Object source, DomainDto domainView, boolean hardRemoval) {
        super(source);
        this.domainView = domainView;
        this.hardRemoval = hardRemoval;
    }

}
