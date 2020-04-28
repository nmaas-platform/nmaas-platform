package net.geant.nmaas.externalservices.inventory.gitlab;

import net.geant.nmaas.externalservices.inventory.gitlab.entities.GitLab;
import net.geant.nmaas.externalservices.inventory.gitlab.repositories.GitLabRepository;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GitLabRealInstanceTest {

    private GitLabRepository repository = mock(GitLabRepository.class);

    private GitLabManager manager;

    @BeforeEach
    public void setup(){
        when(repository.count()).thenReturn(1L);
        when(repository.findAll()).thenReturn(Collections.singletonList(
                new GitLab("gitlab.nmaas.qalab.geant.net", "1_2GyoByrZnbX1ykVrxB", 80)
            )
        );
        manager = new GitLabManager(repository, null);
    }

    @Test
    public void shouldThrowExceptionOnTooManyConfigs() throws GitLabApiException {
        assertDoesNotThrow(() -> {
            List<Project> projects = (manager.projects().getProjects());
            assertTrue(projects.size() > 0);
        });
    }

}
