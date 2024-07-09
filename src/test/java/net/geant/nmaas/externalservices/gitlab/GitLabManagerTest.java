package net.geant.nmaas.externalservices.gitlab;

import net.geant.nmaas.externalservices.gitlab.exceptions.GitLabInvalidConfigurationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GitLabManagerTest {

    private final GitLabManager manager = new GitLabManager();

    @Test
    void shouldValidateGitLabInstance() {
        Exception thrown;

        thrown = assertThrows(IllegalArgumentException.class, manager::validateGitLabInstance);
        assertTrue(thrown.getMessage().contains("GitLab api URL is null or empty"));

        manager.setGitLabApiUrl("http://localhost:8080");
        thrown = assertThrows(IllegalArgumentException.class, manager::validateGitLabInstance);
        assertThat(thrown.getMessage()).contains("GitLab token is null or empty");

        manager.setGitLabToken("token");
        assertThrows(GitLabInvalidConfigurationException.class, manager::validateGitLabInstance);
    }

    @Test
    void shouldPrepareApiBaseUrl() {
        manager.setGitLabApiUrl("http://localhost:8080");
        assertEquals("http://localhost:8080", manager.getApiUrl());

        manager.setGitLabApiUrl("http://localhost:8080/api/v4");
        assertEquals("http://localhost:8080", manager.getApiUrl());
    }

}
