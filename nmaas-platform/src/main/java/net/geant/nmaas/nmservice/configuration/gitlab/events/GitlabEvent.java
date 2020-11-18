package net.geant.nmaas.nmservice.configuration.gitlab.events;

import net.geant.nmaas.nmservice.configuration.gitlab.GitLabEventsListener;
import org.springframework.context.ApplicationEvent;

abstract public class GitlabEvent extends ApplicationEvent {
    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public GitlabEvent(Object source) {
        super(source);
    }

    /**
     * let the event choose how it will be handled
     * @param listener - listener object to handle the event
     */
    abstract public void visit(GitLabEventsListener listener);
}
