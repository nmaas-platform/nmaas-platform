package net.geant.nmaas.nmservice.configuration.gitlab;

import net.geant.nmaas.nmservice.configuration.GitConfigHandler;
import net.geant.nmaas.nmservice.configuration.entities.GitLabProject;
import net.geant.nmaas.nmservice.configuration.gitlab.events.AddUserToRepositoryGitlabEvent;
import net.geant.nmaas.nmservice.configuration.gitlab.events.RemoveUserFromRepositoryGitlabEvent;
import net.geant.nmaas.nmservice.configuration.gitlab.events.UserSshKeysUpdatedGitlabEvent;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.orchestration.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class GitLabEventListenerTest {

    private KubernetesRepositoryManager repositoryManager = mock(KubernetesRepositoryManager.class);

    private GitConfigHandler gitConfigHandler = mock(GitConfigHandler.class);

    private GitLabEventsListener eventsListener = new GitLabEventsListener(repositoryManager, gitConfigHandler);

    @BeforeEach
    public void setup() {
        GitLabProject project = mock(GitLabProject.class);
        when(project.getId()).thenReturn(1L);
        when(repositoryManager.loadGitLabProject(any())).thenReturn(Optional.of(project));
    }

    @Test
    public void addMemberToProjectGitlabEventShouldDelegateToGitConfigHandlerMethods() {
        AddUserToRepositoryGitlabEvent event = new AddUserToRepositoryGitlabEvent(
                "source",
                "username",
                "email",
                "name",
                new ArrayList<>(),
                new Identifier("12")
        );

        eventsListener.handleGitlabEvent(event);

        verify(gitConfigHandler, times(1)).createUser(anyString(), anyString(), anyString(), anyList());
        verify(gitConfigHandler, times(1)).addMemberToProject(anyInt(), anyString());
    }

    @Test
    public void removeMemberFromProjectGitlabEventShouldDelegateToGitConfigHandlerMethod() {
        RemoveUserFromRepositoryGitlabEvent event = new RemoveUserFromRepositoryGitlabEvent(
                "source",
                "username",
                new Identifier("12")
        );

        eventsListener.handleGitlabEvent(event);

        verify(gitConfigHandler, times(1)).removeMemberFromProject(anyInt(), anyString());
    }

    @Test
    public void updateUserSshKeysGitlabEventShouldDelegateToGitConfigHandlerCreateUserMethod() {
        UserSshKeysUpdatedGitlabEvent event = new UserSshKeysUpdatedGitlabEvent(
                "source",
                "username",
                new ArrayList<>()
        );

        eventsListener.handleGitlabEvent(event);

        verify(gitConfigHandler, times(1)).createUser(eq(event.getUserUsername()), eq(event.getUserEmail()), eq(event.getUserEmail()), anyList());
    }
}
