package net.geant.nmaas.portal.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ApplicationActivatedEvent extends ApplicationEvent {

    private final String name;
    private final String version;

    public ApplicationActivatedEvent(Object source, String name, String version) {
        super(source);
        this.name = name;
        this.version = version;
    }

    @Override
    public String toString() {
        return "ApplicationActivatedEvent{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
