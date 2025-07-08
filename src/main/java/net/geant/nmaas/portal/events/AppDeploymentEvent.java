package net.geant.nmaas.portal.events;

import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@Getter
@ToString
public class AppDeploymentEvent extends ApplicationEvent {

    private final String deploymentIdStr;

    public AppDeploymentEvent(Object source, String deploymentIdStr) {
        super(source);
        this.deploymentIdStr = deploymentIdStr;
    }

}