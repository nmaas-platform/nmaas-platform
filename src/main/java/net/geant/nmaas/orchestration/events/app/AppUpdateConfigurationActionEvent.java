package net.geant.nmaas.orchestration.events.app;

import lombok.Getter;
import net.geant.nmaas.orchestration.Identifier;
import org.apache.commons.lang3.Validate;

public class AppUpdateConfigurationActionEvent extends AppBaseEvent {

    @Getter
    private final String userInitiator;

    public AppUpdateConfigurationActionEvent(Object source, Identifier deploymentId, String userInitiator) {
        super(source, deploymentId);
        Validate.isTrue(userInitiator != null && !userInitiator.isEmpty(), "User initiator cannot be null or empty");
        this.userInitiator = userInitiator;
    }

}
