package net.geant.nmaas.orchestration.events;

import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.context.ApplicationEvent;

public abstract class BaseEvent extends ApplicationEvent {

    private Identifier relatedTo;

    public BaseEvent(Object source, Identifier relatedTo) {
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
