package net.geant.nmaas.orchestration.events.app;

import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.BaseEvent;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class AppDeploymentErrorEvent extends BaseEvent {

    public AppDeploymentErrorEvent(Object source, Identifier deploymentId) {
        super(source, deploymentId);
    }

}
