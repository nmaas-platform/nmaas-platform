package net.geant.nmaas.orchestration.events.dcn;

import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.context.ApplicationEvent;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DcnVerifyRequestActionEvent extends ApplicationEvent {

    private Identifier clientId;

    public DcnVerifyRequestActionEvent(Object source, Identifier clientId) {
        super(source);
        this.clientId = clientId;
    }

    public Identifier getClientId() {
        return clientId;
    }
}
