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
        assertEquals(repositoryFile.getFilePath(), "configFileDirectory/configFileName");
        assertEquals(repositoryFile.getContent(), "configFileContent");
    }

}
