package net.geant.nmaas.externalservices.inventory.gitlab;

import net.geant.nmaas.externalservices.inventory.gitlab.entities.GitLab;
import net.geant.nmaas.externalservices.inventory.gitlab.model.GitLabView;
import net.geant.nmaas.externalservices.inventory.gitlab.repositories.GitLabRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class GitLabManagerIntTest {

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
        assertThat("GitLab server is wrong", manager.getGitlabServer().equals("10.10.1.1"));
        assertThat("GitLab port is wrong", manager.getGitlabPort() == 80);
    }

    @Test
    public void shouldRetrieveGitlabConfig() {
        repository.save(simpleGitlabConfig());
        List<GitLabView> allGitlabConfigViews = manager.getAllGitlabConfig();
        assertEquals(1, allGitlabConfigViews.size());
        assertNotNull(allGitlabConfigViews.get(0).getId());
        assertEquals(80, allGitlabConfigViews.get(0).getPort());
        assertEquals("10.10.1.1", allGitlabConfigViews.get(0).getServer());
        assertEquals("testtoken", allGitlabConfigViews.get(0).getToken());
    }

    private GitLab simpleGitlabConfig(){
        GitLab config = new GitLab();
        config.setPort(80);
        config.setServer("10.10.1.1");
        config.setToken("testtoken");
        return config;
    }

}
