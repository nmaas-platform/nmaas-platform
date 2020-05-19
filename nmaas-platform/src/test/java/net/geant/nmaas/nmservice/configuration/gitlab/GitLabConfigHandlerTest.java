package net.geant.nmaas.nmservice.configuration.gitlab;

import net.geant.nmaas.externalservices.inventory.gitlab.GitLabManager;
import net.geant.nmaas.nmservice.configuration.entities.GitLabProject;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.orchestration.AppConfigRepositoryAccessDetails;
import net.geant.nmaas.orchestration.Identifier;
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

public class GitLabConfigHandlerTest {

    private KubernetesRepositoryManager repositoryManager = mock(KubernetesRepositoryManager.class);
    private GitLabManager gitLabManager = mock(GitLabManager.class);

    private GitLabConfigHandler handler;

    @BeforeEach
    public void setup() {
        handler = new GitLabConfigHandler(repositoryManager, null, gitLabManager);
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
        String result = handler.getHttpUrlToRepo(1);
        assertThat(result, is("http://test-server:80/group/project.git"));
    }

    @Test
    public void shouldBuildSshUrlToRepo() throws GitLabApiException {
        ProjectApi projectApi = mock(ProjectApi.class);
        Project project = mock(Project.class);
        when(projectApi.getProject(anyInt())).thenReturn(project);
        when(project.getSshUrlToRepo()).thenReturn("git@gitlab.nmaas.eu:groups-pllab/pllab-oxidized-142.git");
        when(gitLabManager.projects()).thenReturn(projectApi);
        String result = handler.getSshUrlToRepo(1);
        assertThat(result, is("git@gitlab.nmaas.eu/groups-pllab/pllab-oxidized-142.git"));
    }

    @Test
    public void shouldRetrieveRepositoryCloneUrl() {
        Identifier deploymentId = Identifier.newInstance(1L);
        GitLabProject gitLabProject = new GitLabProject(deploymentId, "", "", "", "testCloneUrl", null);
        when(repositoryManager.loadGitLabProject(deploymentId)).thenReturn(gitLabProject);
        AppConfigRepositoryAccessDetails repositoryAccessDetails = handler.configRepositoryAccessDetails(deploymentId);
        assertThat(repositoryAccessDetails.getCloneUrl(), is("testCloneUrl"));
    }
    
}
