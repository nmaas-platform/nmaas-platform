package net.geant.nmaas.nmservice.configuration.gitlab;

import net.geant.nmaas.externalservices.gitlab.GitLabManager;
import net.geant.nmaas.nmservice.configuration.entities.GitLabProject;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.orchestration.AppConfigRepositoryAccessDetails;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.GroupApi;
import org.gitlab4j.api.ProjectApi;
import org.gitlab4j.api.UserApi;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static net.geant.nmaas.nmservice.configuration.gitlab.GitLabConfigHelper.PROJECT_MEMBER_MAINTAINER_ACCESS_LEVEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GitLabConfigHandlerTest {

    private final KubernetesRepositoryManager repositoryManager = mock(KubernetesRepositoryManager.class);
    private final GitLabManager gitLabManager = mock(GitLabManager.class);

    private GitLabConfigHandler handler;

    @BeforeEach
    void setup() {
        handler = new GitLabConfigHandler(repositoryManager, null, gitLabManager);
    }

    @Test
    void shouldBuildHttpUrlToRepo() throws InvalidDeploymentIdException, GitLabApiException {
        ProjectApi projectApi = mock(ProjectApi.class);
        Project project = mock(Project.class);
        when(projectApi.getProject(anyInt())).thenReturn(project);
        when(project.getHttpUrlToRepo()).thenReturn("http://example.gitlab.com/group/project.git");
        when(gitLabManager.getGitlabServer()).thenReturn("test-server");
        when(gitLabManager.getGitlabPort()).thenReturn(80);
        when(gitLabManager.projects()).thenReturn(projectApi);

        String result = handler.getHttpUrlToRepo(1);

        assertThat(result).isEqualTo("http://test-server:80/group/project.git");
    }

    @Test
    void shouldBuildSshUrlToRepo() throws GitLabApiException {
        ProjectApi projectApi = mock(ProjectApi.class);
        Project project = mock(Project.class);
        when(projectApi.getProject(anyInt())).thenReturn(project);
        when(project.getSshUrlToRepo()).thenReturn("git@gitlab.nmaas.eu:groups-pllab/pllab-oxidized-142.git");
        when(gitLabManager.projects()).thenReturn(projectApi);

        String result = handler.getSshUrlToRepo(1);

        assertThat(result).isEqualTo("git@gitlab.nmaas.eu/groups-pllab/pllab-oxidized-142.git");
    }

    @Test
    void shouldRetrieveRepositoryCloneUrl() {
        Identifier deploymentId = Identifier.newInstance(1L);
        GitLabProject gitLabProject = new GitLabProject(deploymentId, "", "", "", "testCloneUrl", null);
        when(repositoryManager.loadGitLabProject(deploymentId)).thenReturn(Optional.of(gitLabProject));

        AppConfigRepositoryAccessDetails repositoryAccessDetails = handler.configRepositoryAccessDetails(deploymentId);

        assertThat(repositoryAccessDetails.getCloneUrl()).isEqualTo("testCloneUrl");
    }

    @Test
    void shouldCreateNewUser() throws GitLabApiException {
        UserApi userApi = mock(UserApi.class);
        when(userApi.getOptionalUser("test_user.eu")).thenReturn(Optional.empty());
        when(gitLabManager.users()).thenReturn(userApi);

        handler.createUser("test@user.eu", "test@user.eu", "test@user.eu", null);

        ArgumentCaptor<User> gitLabUserRequest = ArgumentCaptor.forClass(User.class);
        verify(gitLabManager.users()).createUser(gitLabUserRequest.capture(), anyString(), anyBoolean());
        assertThat(gitLabUserRequest.getValue().getUsername()).isEqualTo("test_user.eu");
    }

    @Test
    void shouldCreateRepository() throws GitLabApiException {
        Identifier deploymentId = Identifier.newInstance("deploymentId");
        Identifier descriptiveDeploymentId = Identifier.newInstance("descriptiveDeploymentId");
        User gitLabUser = new User().withId(120).withUsername("test_user.eu");
        UserApi userApi = mock(UserApi.class);
        when(userApi.getOptionalUser("test_user.eu")).thenReturn(Optional.of(gitLabUser));
        when(gitLabManager.users()).thenReturn(userApi);
        GroupApi groupApi = mock(GroupApi.class);
        when(groupApi.getOptionalGroup(any())).thenReturn(Optional.of(new Group().withId(220)));
        when(gitLabManager.groups()).thenReturn(groupApi);
        ProjectApi projectApi = mock(ProjectApi.class);
        Project project = new Project().withId(350);
        project.setHttpUrlToRepo("https://repo.url.pl/DOMAIN/PROJECT");
        when(projectApi.createProject(220, descriptiveDeploymentId.value())).thenReturn(project);
        when(projectApi.getProject(350)).thenReturn(project);
        when(gitLabManager.projects()).thenReturn(projectApi);
        when(repositoryManager.loadDescriptiveDeploymentId(deploymentId)).thenReturn(descriptiveDeploymentId);
        when(repositoryManager.loadDomain(deploymentId)).thenReturn("DOMAIN");

        handler.createRepository(deploymentId,"test@user.eu");

        verify(userApi).getOptionalUser("test_user.eu");
        verify(projectApi).addMember(350, 120, PROJECT_MEMBER_MAINTAINER_ACCESS_LEVEL);
    }
    
}
