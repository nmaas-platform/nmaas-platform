package net.geant.nmaas.orchestration.events;

import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.context.ApplicationEvent;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class AppVerifyDcnActionEvent extends ApplicationEvent {

    private Identifier deploymentId;

    public AppVerifyDcnActionEvent(Object source, Identifier deploymentId) {
        super(source);
        this.deploymentId = deploymentId;
    }

    public Identifier getDeploymentId() {
        return deploymentId;
    }
}
