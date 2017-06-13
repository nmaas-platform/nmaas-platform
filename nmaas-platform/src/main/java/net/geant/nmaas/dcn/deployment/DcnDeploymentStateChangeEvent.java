package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.context.ApplicationEvent;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DcnDeploymentStateChangeEvent extends ApplicationEvent {

    private Identifier clientId;

    private DcnDeploymentState state;

    public DcnDeploymentStateChangeEvent(Object source, Identifier clientId, DcnDeploymentState state) {
        super(source);
        this.clientId = clientId;
        this.state = state;
    }

    public Identifier getClientId() {
        return clientId;
    }

    public DcnDeploymentState getState() {
        return state;
    }

    @Override
    public String toString() {
        return "DcnDeploymentStateChangeEvent{" +
                "clientId=" + clientId +
                ", state=" + state +
                '}';
    }
}
