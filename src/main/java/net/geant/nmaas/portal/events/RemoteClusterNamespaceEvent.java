package net.geant.nmaas.portal.events;

import lombok.Getter;
import net.geant.nmaas.portal.api.domain.KeyValueView;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class RemoteClusterNamespaceEvent extends ApplicationEvent {

    private final Long remoteClusterId;
    private final String domainCodename;
    private final List<KeyValueView> annotations;

    public RemoteClusterNamespaceEvent(Object source, Long remoteClusterId, String domainCodename, List<KeyValueView> annotations) {
        super(source);
        this.remoteClusterId = remoteClusterId;
        this.domainCodename = domainCodename;
        this.annotations = annotations;
    }

}
