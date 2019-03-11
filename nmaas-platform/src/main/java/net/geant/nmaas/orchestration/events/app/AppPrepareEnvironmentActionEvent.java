package net.geant.nmaas.orchestration.events.app;

import net.geant.nmaas.orchestration.Identifier;

public class AppPrepareEnvironmentActionEvent extends AppBaseEvent {

    public AppPrepareEnvironmentActionEvent(Object source, Identifier deploymentId) {
        super(source, deploymentId);
    }

}
