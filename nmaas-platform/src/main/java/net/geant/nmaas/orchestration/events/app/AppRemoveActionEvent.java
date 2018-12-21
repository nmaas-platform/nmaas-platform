package net.geant.nmaas.orchestration.events.app;

import net.geant.nmaas.orchestration.entities.Identifier;

public class AppRemoveActionEvent extends AppBaseEvent {

    public AppRemoveActionEvent(Object source, Identifier deploymentId) {
        super(source, deploymentId);
    }

}
