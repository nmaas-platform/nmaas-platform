package net.geant.nmaas.orchestration.events.app;

import net.geant.nmaas.orchestration.Identifier;

public class AppRemoveFailedActionEvent extends AppBaseEvent {

    public AppRemoveFailedActionEvent(Object source, Identifier deploymentId) {
        super(source, deploymentId);
    }
}
