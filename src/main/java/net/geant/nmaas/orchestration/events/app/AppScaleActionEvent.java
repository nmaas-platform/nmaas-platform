package net.geant.nmaas.orchestration.events.app;

import net.geant.nmaas.orchestration.ScaleDirection;
import org.springframework.context.ApplicationEvent;
import net.geant.nmaas.orchestration.Identifier;


public class AppScaleActionEvent extends ApplicationEvent {

    private final Identifier deploymentId;
    private final ScaleDirection direction;

    public AppScaleActionEvent(Object source, Identifier deploymentId, ScaleDirection direction) {
        super(source);
        this.deploymentId = deploymentId;
        this.direction = direction;
    }

    public Identifier getDeploymentId() {
        return deploymentId;
    }

    public ScaleDirection getDirection() {
        return direction;
    }

}
