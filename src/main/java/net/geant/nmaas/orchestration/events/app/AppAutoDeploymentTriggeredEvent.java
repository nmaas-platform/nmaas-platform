package net.geant.nmaas.orchestration.events.app;

import lombok.Getter;
import net.geant.nmaas.orchestration.Identifier;
import org.apache.commons.collections4.MultiValuedMap;
import org.springframework.context.ApplicationEvent;

@Getter
public class AppAutoDeploymentTriggeredEvent extends ApplicationEvent {

    private final Identifier deploymentId;
    private final MultiValuedMap<String, String> configurationParameters;

    public AppAutoDeploymentTriggeredEvent(Object source, Identifier deploymentId, MultiValuedMap<String, String> configurationParameters) {
        super(source);
        this.deploymentId = deploymentId;
        this.configurationParameters = configurationParameters;
    }

}
