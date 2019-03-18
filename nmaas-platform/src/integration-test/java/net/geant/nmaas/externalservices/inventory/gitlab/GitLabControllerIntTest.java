package net.geant.nmaas.externalservices.inventory.gitlab;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.externalservices.inventory.gitlab.model.GitLabView;
import net.geant.nmaas.externalservices.inventory.gitlab.repositories.GitLabRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class GitLabControllerIntTest {

    @Autowired
    private GitLabManager manager;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private GitLabRepository repository;

    private MockMvc mvc;

    private static String URL_PREFIX = "/api/management/gitlab";

    @BeforeEach
    public void init(){
        mvc = MockMvcBuilders.standaloneSetup(new GitLabController(manager, modelMapper)).build();
    }

    @AfterEach
    public void cleanup(){
        repository.deleteAll();
    }

    @Test
    public void shouldAddAndRemoveNewGitlabConfig() throws Exception{
        MvcResult result = mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(simpleGitlabConfig()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        assertEquals(1, manager.getAllGitlabConfig().size());
        mvc.perform(delete(URL_PREFIX+"/{id}", Long.parseLong(result.getResponse().getContentAsString())))
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
        MvcResult result = mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(simpleGitlabConfig()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        GitLabView updated = simpleGitlabConfig();
        updated.setId(Long.parseLong(result.getResponse().getContentAsString()));
        updated.setToken("newtesttoken");
        mvc.perform(put(URL_PREFIX+"/{id}", updated.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(updated))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        MvcResult result2 = mvc.perform(get(URL_PREFIX+"/{id}",updated.getId()))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result2.getResponse().getContentAsString(), containsString("newtesttoken"));
    }

    @Test
    public void shouldNotUpdateNotExistingGitlabConfig() throws Exception{
        mvc.perform(put(URL_PREFIX+"/{id}",1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(simpleGitlabConfig()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldNotAddSecondGitlabConfig() throws Exception{
        mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(simpleGitlabConfig()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(simpleGitlabConfig()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void shouldReturnGitlabConfigById() throws Exception{
        MvcResult mvcResult = mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(simpleGitlabConfig()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        MvcResult result = mvc.perform(get(URL_PREFIX+"/{id}", Long.parseLong(mvcResult.getResponse().getContentAsString())))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(Long.parseLong(mvcResult.getResponse().getContentAsString()), ((GitLabView) new ObjectMapper().readValue(result.getResponse().getContentAsString(), new TypeReference<GitLabView>(){})).getId().longValue());
    }

    @Test
    public void shouldNotReturnGitLabConfigByNotExistingId() throws Exception{
        mvc.perform(get(URL_PREFIX+"/{id}",1L))
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


    private GitLabView simpleGitlabConfig(){
        GitLabView config = new GitLabView();
        config.setPort(80);
        config.setServer("11.10.1.1");
        config.setToken("testtoken");
        return config;
    }

}
