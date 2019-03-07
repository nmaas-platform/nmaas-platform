package net.geant.nmaas.orchestration.events.app;

import net.geant.nmaas.orchestration.Identifier;

public class AppVerifyServiceActionEvent extends AppBaseEvent {

    public AppVerifyServiceActionEvent(Object source, Identifier deploymentId) {
        super(source, deploymentId);
    }

}
