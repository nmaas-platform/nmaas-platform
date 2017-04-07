package net.geant.nmaas.nmservice;

import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.context.ApplicationEvent;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class NmServiceDeploymentStateChangeEvent extends ApplicationEvent {

    private Identifier deploymentId;

    private NmServiceDeploymentState state;

    public NmServiceDeploymentStateChangeEvent(Object source, Identifier deploymentId, NmServiceDeploymentState state) {
        super(source);
        this.deploymentId = deploymentId;
        this.state = state;
    }

    public Identifier getDeploymentId() {
        return deploymentId;
    }

    public NmServiceDeploymentState getState() {
        return state;
    }
}
