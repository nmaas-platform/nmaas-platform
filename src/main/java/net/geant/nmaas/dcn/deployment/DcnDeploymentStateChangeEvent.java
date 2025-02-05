package net.geant.nmaas.dcn.deployment;

import lombok.Getter;
import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import org.springframework.context.ApplicationEvent;

@Getter
public class DcnDeploymentStateChangeEvent extends ApplicationEvent {

    private final String domain;
    private final DcnDeploymentState state;

    public DcnDeploymentStateChangeEvent(Object source, String domain, DcnDeploymentState state) {
        super(source);
        this.domain = domain;
        this.state = state;
    }

    @Override
    public String toString() {
        return "DcnDeploymentStateChangeEvent{" +
                "domain=" + domain +
                ", state=" + state +
                '}';
    }
}
