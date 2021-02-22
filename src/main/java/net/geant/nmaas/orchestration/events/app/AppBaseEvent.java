package net.geant.nmaas.orchestration.events.app;

import net.geant.nmaas.orchestration.Identifier;
import org.springframework.context.ApplicationEvent;

public abstract class AppBaseEvent extends ApplicationEvent {

    private Identifier relatedTo;

    public AppBaseEvent(Object source, Identifier relatedTo) {
        super(source);
        this.relatedTo = relatedTo;
    }

    public Identifier getRelatedTo() {
        return relatedTo;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{related to " + relatedTo + "}";
    }

}
