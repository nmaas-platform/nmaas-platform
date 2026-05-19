package net.geant.nmaas.orchestration.events.app;

import lombok.Getter;
import net.geant.nmaas.orchestration.Identifier;

public class AppRestartActionEvent extends AppBaseEvent {

    @Getter
    String userInitiator;

    public AppRestartActionEvent(Object source, Identifier deploymentId) {
        super(source, deploymentId);
    }
    public AppRestartActionEvent(Object source, Identifier deploymentId, String userInitiator) {
        super(source, deploymentId);
        this.userInitiator = userInitiator;
    }

}
