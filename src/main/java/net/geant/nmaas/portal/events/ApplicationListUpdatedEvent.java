package net.geant.nmaas.portal.events;

import lombok.Getter;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import org.springframework.context.ApplicationEvent;

@Getter
public class ApplicationListUpdatedEvent extends ApplicationEvent {

    private final String name;
    private final String version;
    private final ApplicationAction action;
    private final AppDeploymentSpec deploymentSpec;

    public ApplicationListUpdatedEvent(Object source, String name, String version, ApplicationAction action, AppDeploymentSpec deploymentSpec) {
        super(source);
        this.name = name;
        this.version = version;
        this.action = action;
        this.deploymentSpec = deploymentSpec;
    }

    public enum ApplicationAction {
        ADDED,
        UPDATED,
        DELETED;
    }

    @Override
    public String toString() {
        return "ApplicationListUpdatedEvent{" +
                "name=" + name + ", version=" + version + ", action=" + action + ", deploymentSpec=" + ((deploymentSpec != null) ? "provided" : "empty") +
                '}';
    }

}
