package net.geant.nmaas.orchestration.events.dcn;

import org.springframework.context.ApplicationEvent;

public abstract class DcnBaseEvent extends ApplicationEvent {

    private String relatedTo;

    public DcnBaseEvent(Object source, String relatedTo) {
        super(source);
        this.relatedTo = relatedTo;
    }

    public String getRelatedTo() {
        return relatedTo;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{related to " + relatedTo + "}";
    }

}
