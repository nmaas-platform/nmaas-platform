package net.geant.nmaas.nmservice.configuration.gitlab.events;

import lombok.Getter;
import lombok.Setter;
import net.geant.nmaas.nmservice.configuration.gitlab.GitLabEventsListener;

import java.util.List;

@Getter
@Setter
public class UserSshKeysUpdatedGitlabEvent extends GitlabEvent {
    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    protected UserSshKeysUpdatedGitlabEvent(Object source) {
        super(source);
    }

    private String userUsername;
    private List<String> userSshKeys;

    public UserSshKeysUpdatedGitlabEvent(Object source, String userUsername, List<String> userSshKeys) {
        super(source);
        this.userUsername = userUsername;
        this.userSshKeys = userSshKeys;
    }

    @Override
    public void visit(GitLabEventsListener listener) {
        listener.handleGitlabEvent(this);
    }
}
