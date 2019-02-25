package net.geant.nmaas.externalservices.inventory.gitlab;

import net.geant.nmaas.externalservices.inventory.gitlab.entities.GitLab;
import net.geant.nmaas.externalservices.inventory.gitlab.repositories.GitLabRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class GitLabManagerTest {

    @Autowired
    private GitLabRepository repository;

    @Autowired
    private ModelMapper modelMapper;

    private GitLabManager manager;

    @BeforeEach
    public void setup(){
        manager = new GitLabManager(repository, modelMapper);
    }

    @AfterEach
    public void cleanup(){
        repository.deleteAll();
    }

    @Test
    public void shouldThrowExceptionOnTooManyConfigs(){
        assertThrows(IllegalStateException.class, () -> {
            repository.save(simpleGitlabConfig());
            repository.save(simpleGitlabConfig());
            manager.getGitLabApiToken();
        });
    }

    @Test
    public void shouldThrowAnExceptionOfMissingConfig(){
        assertThrows(IllegalStateException.class, () -> {
            manager.getGitLabApiToken();
        });
    }

    @Test
    public void shouldRetrieveGitlabDetails(){
        repository.save(simpleGitlabConfig());
        assertThat("GitLab token is wrong", manager.getGitLabApiToken().equals("testtoken"));
        assertThat("GitLab url is wrong", manager.getGitLabApiUrl().equals("http://10.10.1.1:80"));
    }

    private GitLab simpleGitlabConfig(){
        GitLab config = new GitLab();
        config.setPort(80);
        config.setServer("10.10.1.1");
        config.setToken("testtoken");
        config.setRepositoryAccessUsername("nmaas-conf-automation");
        return config;
    }

}
