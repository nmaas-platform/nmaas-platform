package net.geant.nmaas.orchestration.events.app;

import net.geant.nmaas.orchestration.Identifier;

public class AppApplyConfigurationActionEvent extends AppBaseEvent {

    public AppApplyConfigurationActionEvent(Object source, Identifier deploymentId) {
        super(source, deploymentId);
    }

}
