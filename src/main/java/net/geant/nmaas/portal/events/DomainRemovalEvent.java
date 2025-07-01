package net.geant.nmaas.portal.events;

import lombok.Getter;
import lombok.ToString;
import net.geant.nmaas.portal.api.domain.DomainView;
import org.springframework.context.ApplicationEvent;

@Getter
@ToString
public class DomainRemovalEvent extends ApplicationEvent {
    private final DomainView domainView;
    private final boolean hardRemoval;

    public DomainRemovalEvent(Object source, DomainView domainView, boolean hardRemoval) {
        super(source);
        this.domainView = domainView;
        this.hardRemoval = hardRemoval;
    }
}
