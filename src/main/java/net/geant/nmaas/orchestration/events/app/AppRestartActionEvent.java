package net.geant.nmaas.orchestration.events.app;

import net.geant.nmaas.orchestration.Identifier;

public class AppRestartActionEvent extends AppBaseEvent {

    public AppRestartActionEvent(Object source, Identifier deploymentId) {
        super(source, deploymentId);
    }

}
