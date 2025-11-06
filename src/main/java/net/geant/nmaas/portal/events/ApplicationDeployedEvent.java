package net.geant.nmaas.portal.events;

import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@Getter
@ToString
public class ApplicationDeployedEvent extends ApplicationEvent {

    private final String deploymentId;

    public ApplicationDeployedEvent(Object source, String deploymentId) {
        super(source);
        this.deploymentId = deploymentId;
    }

}