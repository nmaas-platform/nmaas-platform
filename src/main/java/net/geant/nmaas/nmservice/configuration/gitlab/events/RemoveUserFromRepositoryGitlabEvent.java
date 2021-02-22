package net.geant.nmaas.nmservice.configuration.gitlab.events;

import lombok.Getter;
import lombok.Setter;
import net.geant.nmaas.nmservice.configuration.gitlab.GitLabEventsListener;
import net.geant.nmaas.orchestration.Identifier;

@Getter
@Setter
public class RemoveUserFromRepositoryGitlabEvent extends GitlabEvent {
    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    protected RemoveUserFromRepositoryGitlabEvent(Object source) {
        super(source);
    }

    public RemoveUserFromRepositoryGitlabEvent(Object source, String userUsername, Identifier deploymentId) {
        super(source);
        this.userUsername = userUsername;
        this.deploymentId = deploymentId;
    }

    private String userUsername;
    private Identifier deploymentId;

    @Override
    public void visit(GitLabEventsListener listener) {
        listener.handleGitlabEvent(this);
    }
}
