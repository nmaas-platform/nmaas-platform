package net.geant.nmaas.portal.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RemoteClusterNamespaceEvent extends ApplicationEvent {

    private final Long remoteClusterId;

    public RemoteClusterNamespaceEvent(Object source, Long remoteClusterId) {
        super(source);
        this.remoteClusterId = remoteClusterId;
    }

}
