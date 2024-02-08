package net.geant.nmaas.portal.events;

import lombok.Getter;
import lombok.ToString;
import lombok.Value;
import net.geant.nmaas.portal.api.bulk.KeyValue;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
@ToString
public class DomainCreatedEvent extends ApplicationEvent {

    private final DomainSpec domain;

    public DomainCreatedEvent(Object source, DomainSpec domain) {
        super(source);
        this.domain = domain;
    }

    @Value
    public static class DomainSpec {
        Long domainId;
        String domainName;
        String domainCodename;
        List<KeyValue> annotations;
    }

}