package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.orchestration.Identifier;
import org.springframework.context.ApplicationEvent;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DcnDeploymentStateChangeEvent extends ApplicationEvent {

    private Identifier deploymentId;

    private DcnDeploymentState state;

    public DcnDeploymentStateChangeEvent(Object source, Identifier deploymentId, DcnDeploymentState state) {
        super(source);
        this.deploymentId = deploymentId;
        this.state = state;
    }

    public Identifier getDeploymentId() {
        return deploymentId;
    }

    public DcnDeploymentState getState() {
        return state;
    }

}
