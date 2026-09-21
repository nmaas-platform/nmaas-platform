package net.geant.nmaas.orchestration.events.app;

import lombok.Getter;
import net.geant.nmaas.orchestration.Identifier;

public class AppUpdateConfigurationActionEvent extends AppBaseEvent {

    @Getter
    private final String userInitiator;

    public AppUpdateConfigurationActionEvent(Object source, Identifier deploymentId, String userInitiator) {
        super(source, deploymentId);
        this.userInitiator = userInitiator;
    }

}
