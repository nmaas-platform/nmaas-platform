package net.geant.nmaas.externalservices.inventory.gitlab;

import net.geant.nmaas.externalservices.inventory.gitlab.entities.GitLab;
import net.geant.nmaas.externalservices.inventory.gitlab.repositories.GitLabRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test-k8s.properties")
public class GitLabManagerTest {
    @Autowired
    private GitLabRepository repository;

    @Autowired
    private ModelMapper modelMapper;

    private GitLabManager manager;

    @Before
    public void setup(){
        manager = new GitLabManager(repository, modelMapper);
    }

    @After
    public void cleanup(){
        repository.deleteAll();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionOnTooManyConfigs(){
        repository.save(simpleGitlabConfig());
        repository.save(simpleGitlabConfig());
        manager.getGitLabApiToken();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowAnExceptionOfMissingConfig(){
        manager.getGitLabApiToken();
    }

    @Test
    public void shouldRetrieveGitlabDetails(){
        repository.save(simpleGitlabConfig());
        assertThat("GitLab token is wrong",manager.getGitLabApiToken().equals("testtoken"));
        assertThat("GitLab url is wrong",manager.getGitLabApiUrl().equals("http://10.10.1.1:80"));
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
