package net.geant.nmaas.orchestration.events.app;

import lombok.Getter;
import net.geant.nmaas.orchestration.Identifier;

public class AppApplyConfigurationActionEvent extends AppBaseEvent {

    @Getter
    private String userInitiator;

    public AppApplyConfigurationActionEvent(Object source, Identifier deploymentId) {
        super(source, deploymentId);
    }
    public AppApplyConfigurationActionEvent(Object source, Identifier deploymentId, String userInitiator) {
        super(source, deploymentId);
        this.userInitiator = userInitiator;
    }

}
