package net.geant.nmaas.orchestration.events.app;

import lombok.Getter;
import lombok.Setter;
import net.geant.nmaas.orchestration.Identifier;
import org.springframework.context.ApplicationEvent;

@Getter
public class AppAutoDeploymentStatusUpdateEvent extends ApplicationEvent {

    private final Identifier bulkDeploymentId;
    private final Identifier deploymentId;

    @Setter
    private int waitIntervalBeforeNextCheckInSeconds = 0;
    @Setter
    private int eventTimeOutInSeconds = 3600;

    public AppAutoDeploymentStatusUpdateEvent(Object source, Identifier bulkDeploymentId, Identifier deploymentId) {
        super(source);
        this.bulkDeploymentId = bulkDeploymentId;
        this.deploymentId = deploymentId;
    }

    public AppAutoDeploymentStatusUpdateEvent(Object source, Identifier bulkDeploymentId, Identifier deploymentId, int eventTimeOutInSeconds) {
        super(source);
        this.bulkDeploymentId = bulkDeploymentId;
        this.deploymentId = deploymentId;
        this.eventTimeOutInSeconds = eventTimeOutInSeconds;
    }

}
