package net.geant.nmaas.nmservice.configuration.gitlab;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.configuration.GitConfigHandler;
import net.geant.nmaas.nmservice.configuration.entities.GitLabProject;
import net.geant.nmaas.nmservice.configuration.gitlab.events.AddUserToRepositoryGitlabEvent;
import net.geant.nmaas.nmservice.configuration.gitlab.events.GitlabEvent;
import net.geant.nmaas.nmservice.configuration.gitlab.events.RemoveUserFromRepositoryGitlabEvent;
import net.geant.nmaas.nmservice.configuration.gitlab.events.UserSshKeysUpdatedGitlabEvent;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
@Profile("env_kubernetes")
@Log4j2
public class GitLabEventsListener {

    private final KubernetesRepositoryManager repositoryManager;
    protected final GitConfigHandler gitConfigHandler;

    public GitLabEventsListener(KubernetesRepositoryManager repositoryManager, GitConfigHandler gitConfigHandler) {
        this.repositoryManager = repositoryManager;
        this.gitConfigHandler = gitConfigHandler;
    }

    /**
     * Main method for handling gitlab events
     * Oh yes, the visitor DP
     * @param event - any gitlab event
     */
    @EventListener
    public void gitlabEventListener(GitlabEvent event) {
        event.visit(this);
    }

    /**
     * Listens to {@link AddUserToRepositoryGitlabEvent}
     * After event is received, it calls {@link GitConfigHandler} methods to add or update new user and assign it to requested project
     * @param event - an event object containing user data and deployment id
     */
    public void handleGitlabEvent(AddUserToRepositoryGitlabEvent event) {
        log.info(String.format("[ADD GITLAB PROJECT MEMBER EVENT] [%s]", event.getUserUsername()));
        GitLabProject project = loadGitlabProject(event.getDeploymentId());

        this.gitConfigHandler.createUser(event.getUserUsername(), event.getUserEmail(), event.getUserName(), event.getUserSshKeys());
        this.gitConfigHandler.addMemberToProject(project.getProjectId(), event.getUserName());

    }

    /**
     * Listens to {@link RemoveUserFromRepositoryGitlabEvent}
     * After event is received, it calls {@link GitConfigHandler} methods remove user form project member list
     * @param event - an event object containing username and deployment id
     */
    public void handleGitlabEvent(RemoveUserFromRepositoryGitlabEvent event) {
        log.info(String.format("[REMOVE GITLAB PROJECT MEMBER EVENT] [%s]", event.getUserUsername()));
        GitLabProject project = loadGitlabProject(event.getDeploymentId());

        this.gitConfigHandler.removeMemberFromProject(project.getProjectId(), event.getUserUsername());
    }

    /**
     * TODO
     * @param event - an event object
     */
    public void handleGitlabEvent(UserSshKeysUpdatedGitlabEvent event) {
        log.info(String.format("[UPDATE USER SSH KEYS GITLAB EVENT] [%s]", event.getUserUsername()));
        // TODO
    }

    protected GitLabProject loadGitlabProject(Identifier deploymentId){
        Optional<GitLabProject> optionalGitLabProject = repositoryManager.loadGitLabProject(deploymentId);
        if(!optionalGitLabProject.isPresent()) {
            log.warn("Requested gitlab project does not exist");
            throw new ProcessingException("Requested gitlab project does not exist");
        }
        return optionalGitLabProject.get();
    }
}
