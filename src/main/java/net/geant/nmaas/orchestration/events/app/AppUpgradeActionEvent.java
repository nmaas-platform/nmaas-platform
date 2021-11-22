package net.geant.nmaas.orchestration.events.app;

import net.geant.nmaas.orchestration.Identifier;

public class AppUpgradeActionEvent extends AppBaseEvent {

    private Identifier applicationId;

    public AppUpgradeActionEvent(Object source, Identifier deploymentId, Identifier applicationId) {
        super(source, deploymentId);
        this.applicationId = applicationId;
    }

    public Identifier getApplicationId() {
        return applicationId;
    }

}
