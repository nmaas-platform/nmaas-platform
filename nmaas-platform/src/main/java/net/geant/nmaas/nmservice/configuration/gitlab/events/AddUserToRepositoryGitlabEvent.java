package net.geant.nmaas.nmservice.configuration.gitlab.events;

import lombok.Getter;
import lombok.Setter;
import net.geant.nmaas.nmservice.configuration.gitlab.GitLabEventsListener;
import net.geant.nmaas.orchestration.Identifier;

import java.util.List;

@Getter
@Setter
public class AddUserToRepositoryGitlabEvent extends GitlabEvent {
    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    protected AddUserToRepositoryGitlabEvent(Object source) {
        super(source);
    }

    private String userUsername;
    private String userEmail;
    private String userName;
    private List<String> userSshKeys;
    private Identifier deploymentId;

    public AddUserToRepositoryGitlabEvent(Object source, String userUsername, String userEmail, String userName, List<String> userSshKeys, Identifier deploymentId) {
        super(source);
        this.userUsername = userUsername;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userSshKeys = userSshKeys;
        this.deploymentId = deploymentId;
    }

    @Override
    public void visit(GitLabEventsListener listener) {
        listener.handleGitlabEvent(this);
    }
}
