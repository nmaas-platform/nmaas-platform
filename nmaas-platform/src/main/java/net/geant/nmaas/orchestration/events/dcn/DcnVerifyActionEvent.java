package net.geant.nmaas.orchestration.events.dcn;

import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.context.ApplicationEvent;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DcnVerifyActionEvent extends ApplicationEvent {

    private Identifier clientId;

    public DcnVerifyActionEvent(Object source, Identifier clientId) {
        super(source);
        this.clientId = clientId;
    }

    public Identifier getClientId() {
        return clientId;
    }
}
