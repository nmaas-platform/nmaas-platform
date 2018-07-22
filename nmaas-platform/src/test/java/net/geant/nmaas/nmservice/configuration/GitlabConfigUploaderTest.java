package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.externalservices.inventory.gitlab.GitLabManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterDeploymentManager;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.ProjectApi;
import org.gitlab4j.api.models.Project;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GitlabConfigUploaderTest {

    private NmServiceRepositoryManager repositoryManager = mock(NmServiceRepositoryManager.class);
    private GitLabManager gitLabManager = mock(GitLabManager.class);
    private KClusterDeploymentManager kClusterDeployment = mock(KClusterDeploymentManager.class);

    private GitLabConfigUploader uploader;

    @Before
    public void setup() {
        uploader = new GitLabConfigUploader(
                repositoryManager,
                null,
                gitLabManager,
                kClusterDeployment);
    }

    @Test
    public void shouldGenerateProperRepoCloneUrlForInClusterGitLab() throws InvalidDeploymentIdException {
        when(kClusterDeployment.getUseInClusterGitLabInstance()).thenReturn(true);
        when(gitLabManager.getGitLabRepositoryAccessUsername()).thenReturn("test-user");
        String result = uploader.getGitCloneUrl("user", "password", "http://gitlab.test.pl");
        assertThat(result, is("http://" + "test-user" + "@gitlab.test.pl"));
    }

    @Test
    public void shouldGenerateProperRepoCloneUrlForExternalGitLab() throws InvalidDeploymentIdException {
        when(kClusterDeployment.getUseInClusterGitLabInstance()).thenReturn(false);
        String result = uploader.getGitCloneUrl("user", "password", "http://gitlab.test.pl");
        assertThat(result, is("http://user:password@gitlab.test.pl"));
    }

    @Test
    public void shouldBuildHttpUrlToRepo() throws InvalidDeploymentIdException, GitLabApiException {
        GitLabApi gitLabApi = mock(GitLabApi.class);
        ProjectApi projectApi = mock(ProjectApi.class);
        Project project = mock(Project.class);
        when(gitLabApi.getProjectApi()).thenReturn(projectApi);
        when(projectApi.getProject(anyInt())).thenReturn(project);
        when(project.getHttpUrlToRepo())
                .thenReturn("http://example.gitlab.com/group/project.git");
        when(gitLabManager.getGitLabApiUrl()).thenReturn("test-server");
        uploader.setGitlab(gitLabApi);
        String result = uploader.getHttpUrlToRepo(1);
        assertThat(result, is("http://test-server/group/project.git"));
    }
}
