package net.geant.nmaas.nmservice.configuration.api;

import net.geant.nmaas.nmservice.configuration.entities.GitLabProject;
import net.geant.nmaas.nmservice.configuration.repositories.GitLabProjectRepository;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class GitLabWebhookControllerSecTest extends BaseControllerTestSetup {

    @MockBean
    private GitLabProjectRepository gitLabProjectRepository;

    @BeforeEach
    public void setup(){
        GitLabProject project = new GitLabProject();
        project.setWebhookId("1");
        project.setWebhookToken("correct-token");
        when(gitLabProjectRepository.findByWebhookId(project.getWebhookId())).thenReturn(Optional.of(project));
        createMVC();
    }

    @Test
    public void shouldNotAuthorizeWithoutToken() throws Exception {
        mvc.perform(post("/api/gitlab/webhooks/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldNotAuthorizeWithIncorrectToken() throws Exception {
        mvc.perform(post("/api/gitlab/webhooks/1")
                .header("X-Gitlab-Token", "incorrect-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldNotAuthorizeOnMissingProject() throws Exception {
        mvc.perform(post("/api/gitlab/webhooks/incorrectProjectId")
                .header("X-Gitlab-Token", "correct-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldAuthorizeWithGitlabCorrectHeader() throws Exception {
        mvc.perform(post("/api/gitlab/webhooks/1")
            .header("X-Gitlab-Token", "correct-token"))
                .andExpect(status().isNotFound());
    }

}
