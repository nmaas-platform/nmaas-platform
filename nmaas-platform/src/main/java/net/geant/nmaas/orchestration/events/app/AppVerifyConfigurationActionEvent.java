package net.geant.nmaas.orchestration.events.app;

import net.geant.nmaas.orchestration.entities.Identifier;

public class AppVerifyConfigurationActionEvent extends AppBaseEvent {

    public AppVerifyConfigurationActionEvent(Object source, Identifier deploymentId){
        super(source, deploymentId);
    }
}