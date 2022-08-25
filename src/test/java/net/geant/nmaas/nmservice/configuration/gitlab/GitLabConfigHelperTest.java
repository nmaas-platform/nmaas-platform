package net.geant.nmaas.nmservice.configuration.gitlab;

import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import org.gitlab4j.api.models.RepositoryFile;
import org.gitlab4j.api.models.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GitLabConfigHelperTest {

    @Test
    void shouldCreateCommittedFile() {
        NmServiceConfiguration configuration = new NmServiceConfiguration(
                "configId",
                "configFileName",
                "configFileDirectory",
                "configFileContent"
        );
        RepositoryFile repositoryFile = GitLabConfigHelper.committedFile(configuration);
        assertEquals("configFileDirectory/configFileName", repositoryFile.getFilePath());
        assertEquals("configFileContent", repositoryFile.getContent());
    }

    @Test
    void shouldCreateCommittedFileWithoutDirectory() {
        NmServiceConfiguration configuration = new NmServiceConfiguration(
                "configId",
                "configFileName",
                null,
                "configFileContent"
        );
        RepositoryFile repositoryFile = GitLabConfigHelper.committedFile(configuration);
        assertEquals("configFileName", repositoryFile.getFilePath());
        assertEquals("configFileContent", repositoryFile.getContent());

        configuration = new NmServiceConfiguration(
                "configId",
                "configFileName",
                "",
                "configFileContent"
        );
        repositoryFile = GitLabConfigHelper.committedFile(configuration);
        assertEquals("configFileName", repositoryFile.getFilePath());
        assertEquals("configFileContent", repositoryFile.getContent());
    }

    @Test
    void shouldPrepareGitLabUsername() {
        assertEquals("user_name", GitLabConfigHelper.prepareGitLabUsername("user@name.git"));
        assertEquals("__user_name", GitLabConfigHelper.prepareGitLabUsername("@#user#name.atom"));
    }

    @Test
    void shouldCreateUserObject() {
        User result = GitLabConfigHelper.createStandardUser("username", "email", "name");
        assertEquals("username", result.getUsername());
        assertEquals("email", result.getEmail());
        assertEquals("name", result.getName());
    }

    @Test
    void shouldCreateUserObjectWhenNameNotProvided() {
        User result1 = GitLabConfigHelper.createStandardUser("username", "email", "");
        assertEquals("username", result1.getUsername());
        assertEquals("email", result1.getEmail());
        assertEquals("username", result1.getName());

        User result2 = GitLabConfigHelper.createStandardUser("username", "email", null);
        assertEquals("username", result2.getUsername());
        assertEquals("email", result2.getEmail());
        assertEquals("username", result2.getName());

        User result3 = GitLabConfigHelper.createStandardUser("username", "email", " ");
        assertEquals("username", result3.getUsername());
        assertEquals("email", result3.getEmail());
        assertEquals("username", result3.getName());

    }

}
