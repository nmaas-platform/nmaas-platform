package net.geant.nmaas.nmservice.configuration.gitlab;

import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import org.gitlab4j.api.models.RepositoryFile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GitLabConfigHelperTest {

    @Test
    public void shouldCreateCommittedFile() {
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
    public void shouldCreateCommittedFileWithoutDirectory() {
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

}
