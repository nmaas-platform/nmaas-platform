package net.geant.nmaas.orchestration.events.app;

import lombok.Getter;
import net.geant.nmaas.orchestration.Identifier;

public class AppRemoveActionEvent extends AppBaseEvent {
    @Getter
    String username;

    public AppRemoveActionEvent(Object source, Identifier deploymentId) {
        super(source, deploymentId);
    }
    public AppRemoveActionEvent(Object source, Identifier deploymentId, String username) {
        super(source, deploymentId);
        this.username = username;
    }

}
