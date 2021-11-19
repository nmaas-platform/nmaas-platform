package net.geant.nmaas.nmservice.configuration.gitlab;

import net.geant.nmaas.nmservice.configuration.gitlab.events.AddUserToRepositoryGitlabEvent;
import net.geant.nmaas.nmservice.configuration.gitlab.events.RemoveUserFromRepositoryGitlabEvent;
import net.geant.nmaas.orchestration.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class GitLabEventsListenerIntTest {

    @MockBean
    private GitLabEventsListener gitLabEventsListener;

    @Autowired
    private ApplicationEventPublisher publisher;

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

        publisher.publishEvent(event);

        verify(gitLabEventsListener, timeout(200).times(1)).gitlabEventListener(any(AddUserToRepositoryGitlabEvent.class));
    }

    @Test
    public void removeMemberFromProjectGitlabEventShouldDelegateToGitConfigHandlerMethod() {
        RemoveUserFromRepositoryGitlabEvent event = new RemoveUserFromRepositoryGitlabEvent(
                "source",
                "username",
                new Identifier("12")
        );

        publisher.publishEvent(event);

        verify(gitLabEventsListener, timeout(200).times(1)).gitlabEventListener(any(RemoveUserFromRepositoryGitlabEvent.class));
    }

}
