package net.geant.nmaas.externalservices.inventory.dockerhosts;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.externalservices.api.DockerHostManagerRestController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerHostManagerRestControllerTest {

    @Autowired
    private DockerHostRepository dockerHostRepository;

    private MockMvc mvc;

    @Before
    public void init() {
        mvc = MockMvcBuilders.standaloneSetup(new DockerHostManagerRestController(dockerHostRepository)).build();
    }

    @Test
    public void shouldAddNewHDockerHost() throws Exception {
        mvc.perform(post("/platform/api/management/dockerhosts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(initNewDockerHost()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertEquals(initNewDockerHost(), dockerHostRepository.loadByName("GN4-ANSIBLE-HOST2"));
    }

    @Test
    public void shouldRemoveDockerHost() throws Exception {
        int sizeBefore = dockerHostRepository.loadAll().size();
        mvc.perform(delete("/platform/api/management/dockerhosts/{name}", "GN4-DOCKER-3"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(sizeBefore - 1, dockerHostRepository.loadAll().size());
    }

    @Test
    public void shouldUpdateDockerHost() throws Exception {
        mvc.perform(post("/platform/api/management/dockerhosts/{name}", "GN4-ANSIBLE-HOST")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(initNewDockerHost()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertNotNull(dockerHostRepository.loadByName("GN4-ANSIBLE-HOST2"));
        boolean removedFormRepo = false;
        try {
            dockerHostRepository.loadByName("GN4-ANSIBLE-HOST");
        } catch (DockerHostNotFoundException ex) {
            removedFormRepo = true;
        }
        assertTrue(removedFormRepo);
    }

    @Test
    public void shouldListAllHDockerHost() throws Exception {
        MvcResult result = mvc.perform(get("/platform/api/management/dockerhosts"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(
                dockerHostRepository.loadAll().size(),
                ((List<DockerHost>) new ObjectMapper().readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<List<DockerHost>>() {})).size());
    }

    @Test
    public void shoulFetchDockerHostByName() throws Exception {
        MvcResult result = mvc.perform(get("/platform/api/management/dockerhosts/{name}", "GN4-DOCKER-1"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(
                "GN4-DOCKER-1",
                ((DockerHost) new ObjectMapper().readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<DockerHost>() {})).getName());
    }

    @Test
    public void shoulPreferredDockerHost() throws Exception {
        MvcResult result = mvc.perform(get("/platform/api/management/dockerhosts/firstpreferred"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(
                "GN4-DOCKER-1",
                ((DockerHost) new ObjectMapper().readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<DockerHost>() {})).getName());
    }

    private DockerHost initNewDockerHost() throws UnknownHostException {
        return new DockerHost(
                "GN4-ANSIBLE-HOST2",
                InetAddress.getByName("192.168.0.1"),
                9999,
                InetAddress.getByName("192.168.0.1"),
                "eth0",
                "eth1",
                InetAddress.getByName("192.168.1.1"),
                "/home/mgmt/ansible/volumes",
                false);
    }
}
