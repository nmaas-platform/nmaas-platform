package net.geant.nmaas.portal.events;

import lombok.Getter;
import net.geant.nmaas.api.dto.KeyValueDto;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class RemoteClusterNamespaceEvent extends ApplicationEvent {

    private final Long remoteClusterId;
    private final String domainCodename;
    private final List<KeyValueDto> annotations;

    public RemoteClusterNamespaceEvent(Object source, Long remoteClusterId, String domainCodename, List<KeyValueDto> annotations) {
        super(source);
        this.remoteClusterId = remoteClusterId;
        this.domainCodename = domainCodename;
        this.annotations = annotations;
    }

}
