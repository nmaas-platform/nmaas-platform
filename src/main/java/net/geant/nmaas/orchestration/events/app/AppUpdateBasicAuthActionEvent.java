package net.geant.nmaas.orchestration.events.app;

import lombok.Getter;
import net.geant.nmaas.orchestration.Identifier;

@Getter
public class AppUpdateBasicAuthActionEvent extends AppBaseEvent {

    private final String basicAuthUsername;
    private final String basicAuthPassword;

    public AppUpdateBasicAuthActionEvent(Object source, Identifier relatedTo, String basicAuthUsername, String basicAuthPassword) {
        super(source, relatedTo);
        this.basicAuthUsername = basicAuthUsername;
        this.basicAuthPassword = basicAuthPassword;
    }

}
