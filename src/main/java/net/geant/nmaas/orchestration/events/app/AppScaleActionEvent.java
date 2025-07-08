package net.geant.nmaas.orchestration.events.app;

import lombok.Getter;
import net.geant.nmaas.orchestration.AppScaleDirection;
import net.geant.nmaas.orchestration.Identifier;
import org.springframework.context.ApplicationEvent;

@Getter
public class AppScaleActionEvent extends ApplicationEvent {

    private final Identifier deploymentId;
    private final AppScaleDirection direction;

    public AppScaleActionEvent(Object source, Identifier deploymentId, AppScaleDirection direction) {
        super(source);
        this.deploymentId = deploymentId;
        this.direction = direction;
    }

}
