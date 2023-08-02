package net.geant.nmaas.orchestration.events.app;

import lombok.Getter;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import org.springframework.context.ApplicationEvent;

@Getter
public class AppAutoDeploymentTriggeredEvent extends ApplicationEvent {

    private final Identifier bulkDeploymentId;
    private final Identifier deploymentId;
    private final AppConfigurationView appConfigurationView;

    public AppAutoDeploymentTriggeredEvent(Object source, Identifier bulkDeploymentId, Identifier deploymentId, AppConfigurationView appConfigurationView) {
        super(source);
        this.bulkDeploymentId = bulkDeploymentId;
        this.deploymentId = deploymentId;
        this.appConfigurationView = appConfigurationView;
    }

}
