package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.externalservices.inventory.gitlab.GitLabManager;
import net.geant.nmaas.nmservice.configuration.gitlab.GitLabConfigHandler;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.ProjectApi;
import org.gitlab4j.api.models.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GitlabConfigUploaderTest {

    private KubernetesRepositoryManager repositoryManager = mock(KubernetesRepositoryManager.class);
    private GitLabManager gitLabManager = mock(GitLabManager.class);

    private GitLabConfigHandler uploader;

    @BeforeEach
    public void setup() {
        uploader = new GitLabConfigHandler(repositoryManager, null, gitLabManager);
    }

    @Test
    public void shouldBuildHttpUrlToRepo() throws InvalidDeploymentIdException, GitLabApiException {
        ProjectApi projectApi = mock(ProjectApi.class);
        Project project = mock(Project.class);
        when(projectApi.getProject(anyInt())).thenReturn(project);
        when(project.getHttpUrlToRepo()).thenReturn("http://example.gitlab.com/group/project.git");
        when(gitLabManager.getGitlabServer()).thenReturn("test-server");
        when(gitLabManager.getGitlabPort()).thenReturn(80);
        when(gitLabManager.projects()).thenReturn(projectApi);
        String result = uploader.getHttpUrlToRepo(1);
        assertThat(result, is("http://test-server:80/group/project.git"));
    }
}
