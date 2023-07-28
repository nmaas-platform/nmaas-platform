package net.geant.nmaas.orchestration.events.app;

import lombok.Getter;
import net.geant.nmaas.orchestration.Identifier;
import org.springframework.context.ApplicationEvent;

@Getter
public class AppAutoDeploymentStatusUpdateEvent extends ApplicationEvent {

    private final Identifier bulkDeploymentId;
    private final Identifier deploymentId;

    public AppAutoDeploymentStatusUpdateEvent(Object source, Identifier bulkDeploymentId,  Identifier deploymentId) {
        super(source);
        this.bulkDeploymentId = bulkDeploymentId;
        this.deploymentId = deploymentId;
    }

}
