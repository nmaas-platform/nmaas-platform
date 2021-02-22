package net.geant.nmaas.externalservices.inventory.gitlab;

import net.geant.nmaas.externalservices.inventory.gitlab.exceptions.GitLabInvalidConfigurationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GitLabManagerTest {

    private GitLabManager manager;

    @Test
    public void shouldValidateGitLabInstance() {
        manager = new GitLabManager();
        Exception thrown;

        thrown = assertThrows(IllegalArgumentException.class, () -> manager.validateGitLabInstance());
        assertTrue(thrown.getMessage().contains("GitLab address is null or empty"));

        manager.setGitLabAddress("localhost");
        thrown = assertThrows(IllegalArgumentException.class, () -> manager.validateGitLabInstance());
        assertTrue(thrown.getMessage().contains("GitLab port is null"));

        manager.setGitLabPort(8080);
        thrown = assertThrows(IllegalArgumentException.class, () -> manager.validateGitLabInstance());
        assertTrue(thrown.getMessage().contains("GitLab token is null or empty"));

        manager.setGitLabToken("token");
        assertThrows(GitLabInvalidConfigurationException.class, () -> manager.validateGitLabInstance());
    }

}
