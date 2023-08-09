package net.geant.nmaas.orchestration.events.app;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AppAutoDeploymentReviewEvent extends ApplicationEvent {

    public AppAutoDeploymentReviewEvent(Object source) {
        super(source);
    }

}
