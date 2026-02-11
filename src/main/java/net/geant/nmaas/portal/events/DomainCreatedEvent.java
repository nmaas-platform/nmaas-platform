package net.geant.nmaas.portal.events;

import lombok.Getter;
import lombok.ToString;
import net.geant.nmaas.api.dto.KeyValueView;
import net.geant.nmaas.portal.persistence.entity.Domain;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
@ToString
public class DomainCreatedEvent extends ApplicationEvent {

    private final DomainSpec domain;
    private final Domain domainEntity;

    public DomainCreatedEvent(Object source, DomainSpec domain, Domain domainEntity) {
        super(source);
        this.domain = domain;
        this.domainEntity = domainEntity;
    }

    public record DomainSpec(Long domainId, String domainName, String domainCodename, List<KeyValueView> annotations) {
    }

}