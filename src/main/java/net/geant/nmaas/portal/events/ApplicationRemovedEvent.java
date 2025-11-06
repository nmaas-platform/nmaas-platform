package net.geant.nmaas.portal.events;

import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@Getter
@ToString
public class ApplicationRemovedEvent extends ApplicationEvent {

    private final String deploymentId;

    public ApplicationRemovedEvent(Object source, String deploymentId) {
        super(source);
        this.deploymentId = deploymentId;
    }

}