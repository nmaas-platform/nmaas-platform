package net.geant.nmaas.nmservice;

import lombok.Getter;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.Identifier;
import org.springframework.context.ApplicationEvent;

import java.util.EnumMap;

public class NmServiceDeploymentStateChangeEvent extends ApplicationEvent {

    @Getter
    private Identifier deploymentId;

    @Getter
    private NmServiceDeploymentState state;

    private EnumMap<EventDetailType, String> details = new EnumMap<>(EventDetailType.class);

    @Getter
    private String errorMessage;

    public NmServiceDeploymentStateChangeEvent(Object source, Identifier deploymentId, NmServiceDeploymentState state, String errorMessage) {
        super(source);
        this.deploymentId = deploymentId;
        this.state = state;
        this.errorMessage = errorMessage;
    }

    public void addDetail(EventDetailType type, String value) {
        this.details.put(type, value);
    }

    public String getDetail(EventDetailType type) {
        return this.details.get(type);
    }

    @Override
    public String toString() {
        return "NmServiceDeploymentStateChangeEvent{" +
                "deploymentId=" + deploymentId +
                ", state=" + state +
                '}';
    }

    public enum EventDetailType {
        NEW_APPLICATION_ID
    }

}
