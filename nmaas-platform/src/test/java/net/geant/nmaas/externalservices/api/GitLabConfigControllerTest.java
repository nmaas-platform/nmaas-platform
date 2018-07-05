package net.geant.nmaas.externalservices.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.externalservices.api.model.GitLabView;
import net.geant.nmaas.externalservices.inventory.gitlab.GitLabManager;
import net.geant.nmaas.externalservices.inventory.gitlab.entities.GitLab;
import net.geant.nmaas.externalservices.inventory.gitlab.repositories.GitLabRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test-k8s.properties")
public class GitLabConfigControllerTest {
    @Autowired
    private GitLabManager manager;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private GitLabRepository repository;

    private MockMvc mvc;

    private static String URL_PREFIX = "/api/management/gitlab";

    @Before
    public void init(){
        mvc = MockMvcBuilders.standaloneSetup(new GitLabConfigController(manager)).build();
    }

    @After
    public void cleanup(){
        repository.deleteAll();
    }

    @Test
    public void shouldAddAndRemoveNewGitlabConfig() throws Exception{
        mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(simpleGitlabConfig(1L)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertEquals(1, manager.getAllGitlabConfig().size());
        mvc.perform(delete(URL_PREFIX+"/{id}", 1L))
                .andExpect(status().isNoContent());
        assertEquals(0, manager.getAllGitlabConfig().size());
    }

    @Test
    public void shouldNotRemoveNotExistingGitlabConfig() throws Exception{
        mvc.perform(delete(URL_PREFIX+"/{id}",1L))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldUpdateExistingGitlabConfig() throws Exception{
        mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(simpleGitlabConfig(1L)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        GitLab updated = simpleGitlabConfig(1L);
        updated.setToken("newtesttoken");
        mvc.perform(put(URL_PREFIX+"/{id}", updated.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(updated))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        MvcResult result = mvc.perform(get(URL_PREFIX+"/{id}",updated.getId()))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getContentAsString(), containsString("newtesttoken"));
    }

    @Test
    public void shouldNotUpdateNotExistingGitlabConfig() throws Exception{
        mvc.perform(put(URL_PREFIX+"/{id}",1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(simpleGitlabConfig(1L)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldNotAddSecondGitlabConfig() throws Exception{
        mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(simpleGitlabConfig(1L)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(simpleGitlabConfig(2L)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void shouldReturnGitlabConfigById() throws Exception{
        mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(simpleGitlabConfig(1L)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        MvcResult result = mvc.perform(get(URL_PREFIX+"/{id}", 1L))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(1L, ((GitLab) new ObjectMapper().readValue(result.getResponse().getContentAsString(), new TypeReference<GitLab>(){})).getId().longValue());
    }

    @Test
    public void shouldNotReturnGitLabConfigByNotExistingId() throws Exception{
        mvc.perform(get(URL_PREFIX+"{id}",1L))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnListOfGitlabConfig()throws Exception{
        MvcResult result = mvc.perform(get(URL_PREFIX))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(manager.getAllGitlabConfig().size(),
                ((List<GitLabView>) new ObjectMapper().readValue(result.getResponse().getContentAsString(), new TypeReference<List<GitLabView>>(){})).size());
    }


    private GitLab simpleGitlabConfig(Long id){
        GitLab config = new GitLab();
        config.setId(id);
        config.setApiVersion("v4");
        config.setPort(80);
        config.setServer("11.10.1.1");
        config.setToken("testtoken");
        return config;
    }
}
