package net.geant.nmaas.portal.api.domain;

import lombok.*;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationStateChangeRequest {

    private ApplicationState state;

    private String reason;

    @Getter(AccessLevel.NONE)
    private boolean shouldSendNotification;

    public boolean shouldSendNotification() {
        return shouldSendNotification;
    }
}
