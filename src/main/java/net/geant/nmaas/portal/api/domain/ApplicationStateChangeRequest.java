package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationStateChangeRequest {

    private ApplicationState state;

    private String reason;

    @Accessors(fluent = true)
    private boolean shouldSendNotification;
}
