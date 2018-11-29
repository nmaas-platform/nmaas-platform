package net.geant.nmaas.orchestration.events.app;

import net.geant.nmaas.orchestration.entities.Identifier;

public class AppUpdateConfigurationEvent extends AppBaseEvent {

    public AppUpdateConfigurationEvent(Object source, Identifier relatedTo) {
        super(source, relatedTo);
    }
}
