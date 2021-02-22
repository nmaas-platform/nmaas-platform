package net.geant.nmaas.orchestration.events.app;

import net.geant.nmaas.orchestration.Identifier;

public class AppRequestNewOrVerifyExistingDcnEvent extends AppBaseEvent {

    public AppRequestNewOrVerifyExistingDcnEvent(Object source, Identifier deploymentId) {
        super(source, deploymentId);
    }

}
