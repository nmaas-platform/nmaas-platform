package net.geant.nmaas.orchestration;

import org.springframework.context.ApplicationEvent;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class AppDeploymentErrorEvent extends ApplicationEvent {

    private Identifier deploymentId;

    public AppDeploymentErrorEvent(Object source, Identifier deploymentId) {
        super(source);
        this.deploymentId = deploymentId;
    }

    public Identifier getDeploymentId() {
        return deploymentId;
    }
}
