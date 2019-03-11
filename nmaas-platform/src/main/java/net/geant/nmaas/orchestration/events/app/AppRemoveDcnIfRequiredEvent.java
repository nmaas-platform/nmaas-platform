package net.geant.nmaas.orchestration.events.app;

import net.geant.nmaas.orchestration.Identifier;

public class AppRemoveDcnIfRequiredEvent extends AppBaseEvent {

    public AppRemoveDcnIfRequiredEvent(Object source, Identifier deploymentId) {
        super(source, deploymentId);
    }

}
