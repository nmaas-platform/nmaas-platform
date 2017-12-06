package net.geant.nmaas.orchestration.events.dcn;

import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.BaseEvent;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DcnDeployActionEvent extends BaseEvent {

    public DcnDeployActionEvent(Object source, Identifier clientId) {
        super(source, clientId);
    }

}
