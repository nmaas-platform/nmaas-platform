package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import org.springframework.context.ApplicationEvent;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DcnDeploymentStateChangeEvent extends ApplicationEvent {

    private String domain;

    private DcnDeploymentState state;

    public DcnDeploymentStateChangeEvent(Object source, String domain, DcnDeploymentState state) {
        super(source);
        this.domain = domain;
        this.state = state;
    }

    public String getDomain() {
        return domain;
    }

    public DcnDeploymentState getState() {
        return state;
    }

    @Override
    public String toString() {
        return "DcnDeploymentStateChangeEvent{" +
                "domain=" + domain +
                ", state=" + state +
                '}';
    }
}
