package net.geant.nmaas.nmservice.configuration.gitlab;

import net.geant.nmaas.gitlab.GitLabManager;
import net.geant.nmaas.nmservice.configuration.entities.GitLabProject;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.orchestration.AppConfigRepositoryAccessDetails;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.service.ConfigurationManager;
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
import static net.geant.nmaas.nmservice.configuration.gitlab.GitLabConfigHelper.groupPath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GitLabConfigHandlerTest {

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
        when(projectApi.getProject(anyLong())).thenReturn(project);
        when(project.getHttpUrlToRepo()).thenReturn("http://example.gitlab.com/group/project.git");
        when(gitLabManager.getGitLabApiUrl()).thenReturn("http://test-server:80");
        when(gitLabManager.projects()).thenReturn(projectApi);

        String result = handler.getHttpUrlToRepo(1L);

        assertThat(result).isEqualTo("http://test-server:80/group/project.git");
    }

    @Test
    void shouldBuildSshUrlToRepo() throws GitLabApiException {
        ProjectApi projectApi = mock(ProjectApi.class);
        Project project = mock(Project.class);
        when(projectApi.getProject(anyLong())).thenReturn(project);
        when(project.getSshUrlToRepo()).thenReturn("ssh://git@ssh.gitlab.qalab.nmaas.eu:5022/groups-tests/q1-tests-icinga2-56.git");
        when(gitLabManager.projects()).thenReturn(projectApi);

        String result = handler.getSshUrlToRepo(1L);

        assertThat(result).isEqualTo("ssh://git@ssh.gitlab.qalab.nmaas.eu:5022/groups-tests/q1-tests-icinga2-56.git");
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
        User gitLabUser = new User().withId(120L).withUsername("test_user.eu");
        UserApi userApi = mock(UserApi.class);
        when(userApi.getOptionalUser("test_user.eu")).thenReturn(Optional.of(gitLabUser));
        when(gitLabManager.users()).thenReturn(userApi);
        GroupApi groupApi = mock(GroupApi.class);
        when(groupApi.getOptionalGroup(any())).thenReturn(Optional.of(new Group().withId(220L)));
        when(gitLabManager.groups()).thenReturn(groupApi);
        ProjectApi projectApi = mock(ProjectApi.class);
        Project project = new Project().withId(350L);
        project.setHttpUrlToRepo("https://repo.url.pl/DOMAIN/PROJECT");
        project.setSshUrlToRepo("git@repo.url.pl");
        when(projectApi.createProject(220L, descriptiveDeploymentId.value())).thenReturn(project);
        when(projectApi.getProject(350L)).thenReturn(project);
        when(gitLabManager.projects()).thenReturn(projectApi);
        when(repositoryManager.loadDescriptiveDeploymentId(deploymentId)).thenReturn(descriptiveDeploymentId);
        when(repositoryManager.loadDomain(deploymentId)).thenReturn("DOMAIN");

        handler.createRepository(deploymentId, "test@user.eu");

        verify(userApi).getOptionalUser("test_user.eu");
        verify(projectApi).addMember(350L, 120L, PROJECT_MEMBER_MAINTAINER_ACCESS_LEVEL);
    }

    @Test
    void shouldCreateRepositoryWithTopLevel() throws GitLabApiException {
        Identifier deploymentId = Identifier.newInstance("deploymentId");
        Identifier descriptiveDeploymentId = Identifier.newInstance("descriptiveDeploymentId");
        User gitLabUser = new User().withId(120L).withUsername("test_user.eu");
        UserApi userApi = mock(UserApi.class);
        when(userApi.getOptionalUser("test_user.eu")).thenReturn(Optional.of(gitLabUser));
        when(gitLabManager.users()).thenReturn(userApi);
        when(gitLabManager.isSharedInstance()).thenReturn(true);
        when(gitLabManager.getTopLevelGroupName()).thenReturn("toplevel");
        when(gitLabManager.getTopLevelGroupPath()).thenReturn(Optional.of("toplevel"));
        GroupApi groupApi = mock(GroupApi.class);
        Group topLevel = new Group();
        topLevel.setId(200L);
        topLevel.setName("toplevel");
        topLevel.setPath("toplevel");
        when(groupApi.getOptionalGroup("toplevel")).thenReturn(Optional.of(topLevel));
        when(repositoryManager.loadDomain(deploymentId)).thenReturn("DOMAIN");
        when(groupApi.getOptionalGroup("toplevel/groups-DOMAIN")).thenReturn(Optional.of(new Group().withId(220L)));
        when(gitLabManager.groups()).thenReturn(groupApi);
        ProjectApi projectApi = mock(ProjectApi.class);
        Project project = new Project().withId(350L);
        project.setHttpUrlToRepo("https://repo.url.pl/DOMAIN/PROJECT");
        project.setSshUrlToRepo("git@repo.url.pl");
        when(projectApi.createProject(220L, descriptiveDeploymentId.value())).thenReturn(project);
        when(projectApi.getProject(350L)).thenReturn(project);
        when(gitLabManager.projects()).thenReturn(projectApi);
        when(repositoryManager.loadDescriptiveDeploymentId(deploymentId)).thenReturn(descriptiveDeploymentId);

        handler.createRepository(deploymentId, "test@user.eu");

        verify(userApi).getOptionalUser("test_user.eu");
        verify(projectApi).addMember(350L, 120L, PROJECT_MEMBER_MAINTAINER_ACCESS_LEVEL);
    }

}
