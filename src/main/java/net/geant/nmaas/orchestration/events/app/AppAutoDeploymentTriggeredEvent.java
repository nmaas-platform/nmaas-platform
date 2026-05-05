package net.geant.nmaas.orchestration.events.app;

import lombok.Getter;
import net.geant.nmaas.api.dto.applications.AppConfigurationDto;
import net.geant.nmaas.orchestration.Identifier;
import org.springframework.context.ApplicationEvent;

// Currently not used
@Getter
public class AppAutoDeploymentTriggeredEvent extends ApplicationEvent {

    private final Identifier bulkDeploymentId;
    private final Identifier deploymentId;
    private final AppConfigurationDto appConfigurationView;

    public AppAutoDeploymentTriggeredEvent(Object source, Identifier bulkDeploymentId, Identifier deploymentId, AppConfigurationDto appConfigurationDto) {
        super(source);
        this.bulkDeploymentId = bulkDeploymentId;
        this.deploymentId = deploymentId;
        this.appConfigurationView = appConfigurationDto;
    }

}
