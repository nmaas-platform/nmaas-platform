package net.geant.nmaas.nmservice;

import lombok.Getter;
import net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState;
import net.geant.nmaas.orchestration.Identifier;
import org.springframework.context.ApplicationEvent;

import java.util.EnumMap;

public class NmServiceDeploymentStateChangeEvent extends ApplicationEvent {

    @Getter
    private final Identifier deploymentId;

    @Getter
    private final ServiceDeploymentState state;

    private final EnumMap<EventDetailType, String> details = new EnumMap<>(EventDetailType.class);

    @Getter
    private final String errorMessage;

    public NmServiceDeploymentStateChangeEvent(Object source, Identifier deploymentId, ServiceDeploymentState state, String errorMessage) {
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
                ", details=" + details +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }

    public enum EventDetailType {
        NEW_APPLICATION_ID,
        UPGRADE_TRIGGER_TYPE
    }

}
