package net.geant.nmaas.orchestration.events.app;

import net.geant.nmaas.orchestration.entities.Identifier;

public class AppDeploymentErrorEvent extends AppBaseEvent {

    public AppDeploymentErrorEvent(Object source, Identifier deploymentId) {
        super(source, deploymentId);
    }

}
