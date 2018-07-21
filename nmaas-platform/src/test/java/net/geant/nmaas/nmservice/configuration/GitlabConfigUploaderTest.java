package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.externalservices.inventory.gitlab.GitLabManager;
import net.geant.nmaas.externalservices.inventory.kubernetes.KClusterDeploymentManager;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
        String result = uploader.getGitCloneUrl("user", "password", "http://gitlab.test.pl");
        assertThat(result, is("http://" + GitLabConfigUploader.DEFAULT_REPO_CLONE_USER + "@gitlab.test.pl"));
    }

    @Test
    public void shouldGenerateProperRepoCloneUrlForExternalGitLab() throws InvalidDeploymentIdException {
        when(kClusterDeployment.getUseInClusterGitLabInstance()).thenReturn(false);
        String result = uploader.getGitCloneUrl("user", "password", "http://gitlab.test.pl");
        assertThat(result, is("http://user:password@gitlab.test.pl"));
    }

}
