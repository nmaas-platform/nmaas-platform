package net.geant.nmaas.externalservices.inventory.gitlab;

import net.geant.nmaas.externalservices.inventory.gitlab.entities.Gitlab;
import net.geant.nmaas.externalservices.inventory.gitlab.repositories.GitlabRepository;
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
public class GitlabManagerTest {
    @Autowired
    private GitlabRepository repository;

    @Autowired
    private ModelMapper modelMapper;

    private GitlabManager manager;

    @Before
    public void setup(){
        manager = new GitlabManager(repository, modelMapper);
    }

    @After
    public void cleanup(){
        repository.deleteAll();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionOnTooManyConfigs(){
        repository.save(simpleGitlabConfig(1L));
        repository.save(simpleGitlabConfig(2L));
        manager.getGitLabApiToken();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowAnExceptionOfMissingConfig(){
        manager.getGitLabApiToken();
    }

    @Test
    public void shouldRetrieveGitlabDetails(){
        repository.save(simpleGitlabConfig(1L));
        assertThat("GitLab token is wrong",manager.getGitLabApiToken().equals("testtoken"));
        assertThat("GitLab url is wrong",manager.getGitLabApiUrl().equals("http://10.10.1.1:80"));
        assertThat("GitLab api version is wrong",manager.getGitLabApiVersion().equals("v4"));
    }

    private Gitlab simpleGitlabConfig(Long id){
        Gitlab config = new Gitlab();
        config.setId(id);
        config.setApiVersion("v4");
        config.setPort(80);
        config.setServer("10.10.1.1");
        config.setToken("testtoken");
        return config;
    }

}
