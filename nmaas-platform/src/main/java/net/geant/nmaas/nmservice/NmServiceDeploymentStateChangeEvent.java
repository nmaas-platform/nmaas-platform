package net.geant.nmaas.nmservice;

import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.context.ApplicationEvent;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class NmServiceDeploymentStateChangeEvent extends ApplicationEvent {

    private Identifier deploymentId;

    private NmServiceDeploymentState state;

    private String errorMessage;

    public NmServiceDeploymentStateChangeEvent(Object source, Identifier deploymentId, NmServiceDeploymentState state, String errorMessage) {
        super(source);
        this.deploymentId = deploymentId;
        this.state = state;
        this.errorMessage = errorMessage;
    }

    public Identifier getDeploymentId() {
        return deploymentId;
    }

    public NmServiceDeploymentState getState() {
        return state;
    }

    public String getErrorMessage(){return errorMessage;}

    @Override
    public String toString() {
        return "NmServiceDeploymentStateChangeEvent{" +
                "deploymentId=" + deploymentId +
                ", state=" + state +
                '}';
    }
}
