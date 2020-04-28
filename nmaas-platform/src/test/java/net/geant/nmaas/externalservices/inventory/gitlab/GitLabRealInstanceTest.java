package net.geant.nmaas.externalservices.inventory.gitlab;

import lombok.extern.log4j.Log4j2;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
public class GitLabRealInstanceTest {

    @Disabled
    @Test
    public void shouldConnectToTestInstanceAndRemoveAllRepositories() throws GitLabApiException {
        GitLabApi api = new GitLabApi(GitLabApi.ApiVersion.V4, "https://gitlab.nmaas.qalab.geant.net", "1_2GyoByrZnbX1ykVrxB");
        List<Project> projects = api.getProjectApi().getProjects();
        projects.forEach(p -> {
            try {
                api.getProjectApi().deleteProject(p.getId());
            } catch (GitLabApiException e) {
                log.info(String.format("Removal failed for project %d", p.getId()));
            }
        });
        assertEquals(0, api.getProjectApi().getProjects().size());
    }

}
