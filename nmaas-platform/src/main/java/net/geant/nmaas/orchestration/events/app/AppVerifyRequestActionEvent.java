package net.geant.nmaas.orchestration.events.app;

import net.geant.nmaas.orchestration.entities.Identifier;

public class AppVerifyRequestActionEvent extends AppBaseEvent {

    public AppVerifyRequestActionEvent(Object source, Identifier deploymentId) {
        super(source, deploymentId);
    }

}
