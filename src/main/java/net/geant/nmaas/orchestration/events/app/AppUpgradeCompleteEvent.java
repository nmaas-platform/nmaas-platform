package net.geant.nmaas.orchestration.events.app;

import net.geant.nmaas.orchestration.Identifier;

public class AppUpgradeCompleteEvent extends AppBaseEvent {

    private final Identifier applicationId;

    public AppUpgradeCompleteEvent(Object source, Identifier deploymentId, Identifier applicationId) {
        super(source, deploymentId);
        this.applicationId = applicationId;
    }

    public Identifier getApplicationId() {
        return applicationId;
    }

}
