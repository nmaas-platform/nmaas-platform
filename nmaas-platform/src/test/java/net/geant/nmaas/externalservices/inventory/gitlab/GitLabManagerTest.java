package net.geant.nmaas.externalservices.inventory.gitlab;

import net.geant.nmaas.externalservices.inventory.gitlab.entities.GitLab;
import net.geant.nmaas.externalservices.inventory.gitlab.exceptions.GitLabInvalidConfigurationException;
import net.geant.nmaas.externalservices.inventory.gitlab.repositories.GitLabRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GitLabManagerTest {

    private GitLabRepository gitLabRepository = mock(GitLabRepository.class);
    private ModelMapper modelMapper = mock(ModelMapper.class);

    private GitLabManager manager;

    @BeforeEach
    public void setup() {
        manager = new GitLabManager(gitLabRepository, modelMapper);
    }

    @Test
    public void shouldValidateGitLabInstance() {
        GitLab gitLab = new GitLab();
        Exception thrown;

        when(gitLabRepository.count()).thenReturn(0L);
        thrown = assertThrows(IllegalStateException.class, () -> manager.validateGitLabInstance());
        assertTrue(thrown.getMessage().contains("Found 0 gitlab config instead of one"));

        when(gitLabRepository.count()).thenReturn(1L);
        when(gitLabRepository.findAll()).thenReturn(Collections.singletonList(gitLab));
        thrown = assertThrows(IllegalArgumentException.class, () -> manager.validateGitLabInstance());
        assertTrue(thrown.getMessage().contains("GitLab port is null"));

        gitLab.setPort(8080);
        when(gitLabRepository.findAll()).thenReturn(Collections.singletonList(gitLab));
        thrown = assertThrows(IllegalArgumentException.class, () -> manager.validateGitLabInstance());
        assertTrue(thrown.getMessage().contains("GitLab server is null or empty"));

        gitLab.setServer("localhost");
        when(gitLabRepository.findAll()).thenReturn(Collections.singletonList(gitLab));
        thrown = assertThrows(IllegalArgumentException.class, () -> manager.validateGitLabInstance());
        assertTrue(thrown.getMessage().contains("GitLab token is null or empty"));

        gitLab.setToken("token");
        when(gitLabRepository.findAll()).thenReturn(Collections.singletonList(gitLab));
        assertThrows(GitLabInvalidConfigurationException.class, () -> manager.validateGitLabInstance());
    }

}
