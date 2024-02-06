package net.geant.nmaas.portal.events;

import lombok.Getter;
import lombok.ToString;
import lombok.Value;
import org.springframework.context.ApplicationEvent;

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
    }

}