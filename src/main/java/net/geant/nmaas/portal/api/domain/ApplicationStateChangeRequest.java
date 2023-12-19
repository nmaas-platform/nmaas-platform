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

    private String notificationText;

    public boolean shouldSendNotification() {
        return shouldSendNotification;
    }

    public ApplicationStateChangeRequest(ApplicationState state, String reason, boolean shouldSendNotification) {
        this.state = state;
        this.reason = reason;
        this.shouldSendNotification = shouldSendNotification;
        this.notificationText = "";
    }
}
