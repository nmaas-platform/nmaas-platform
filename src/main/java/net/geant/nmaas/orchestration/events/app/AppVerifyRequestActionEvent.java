package net.geant.nmaas.orchestration.events.app;

import lombok.Getter;
import net.geant.nmaas.orchestration.Identifier;

public class AppVerifyRequestActionEvent extends AppBaseEvent {

    @Getter
    private Long userInitiatorId;

    public AppVerifyRequestActionEvent(Object source, Identifier deploymentId) {
        super(source, deploymentId);
    }
    public AppVerifyRequestActionEvent(Object source, Identifier deploymentId, Long userInitiatorId) {
        super(source, deploymentId);
        this.userInitiatorId = userInitiatorId;
    }

}
