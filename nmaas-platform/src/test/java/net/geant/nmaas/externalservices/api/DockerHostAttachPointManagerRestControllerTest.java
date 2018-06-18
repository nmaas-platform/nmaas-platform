package net.geant.nmaas.externalservices.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.externalservices.inventory.network.DockerHostAttachPoint;
import net.geant.nmaas.externalservices.inventory.network.repositories.DockerHostAttachPointRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.ws.rs.core.MediaType;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test-engine.properties")
public class DockerHostAttachPointManagerRestControllerTest {

    private final static String URL_PREFIX = "/api/management/network";

    private static final String FIRST_DOCKER_HOST_NAME = "dh-1";
    private static final String SECOND_DOCKER_HOST_NAME = "dh-2";

    private static final String EXAMPLE_DOCKER_HOST_ATTACH_POINT_JSON = "" +
            "{" +
                "\"dockerHostName\":\"" + FIRST_DOCKER_HOST_NAME + "\"," +
                "\"routerName\":\"R3\"," +
                "\"routerId\":\"172.16.3.3\"," +
                "\"routerInterfaceName\":\"ge-0/0/4\"" +
            "}";

    @Autowired
    private DockerHostAttachPointRepository dockerHostAttachPointRepository;

    private MockMvc mvc;

    @Before
    public void init() {
        mvc = MockMvcBuilders
                .standaloneSetup(new DockerHostAttachPointManagerRestController(dockerHostAttachPointRepository))
                .build();
    }

    @After
    public void cleanRepository() {
        dockerHostAttachPointRepository.deleteAll();
    }

    @Test
    public void shouldAddNewDockerHostAttachPoint() throws Exception {
        mvc.perform(post(URL_PREFIX + "/dockerhosts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dockerHostAttachPoint(FIRST_DOCKER_HOST_NAME)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertThat(dockerHostAttachPointRepository.findByDockerHostName(FIRST_DOCKER_HOST_NAME).isPresent(), is(true));
        assertThat(
                dockerHostAttachPoint(FIRST_DOCKER_HOST_NAME).getRouterName(),
                equalTo(dockerHostAttachPointRepository.findByDockerHostName(FIRST_DOCKER_HOST_NAME).get().getRouterName()));
    }

    @Test
    public void shouldAddNewDockerHostAttachPointFromJson() throws Exception {
        mvc.perform(post(URL_PREFIX + "/dockerhosts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(EXAMPLE_DOCKER_HOST_ATTACH_POINT_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertThat(
                dockerHostAttachPointRepository.findByDockerHostName(FIRST_DOCKER_HOST_NAME).isPresent(),
                is(true));
        assertThat(
                dockerHostAttachPointRepository.findByDockerHostName(FIRST_DOCKER_HOST_NAME).get().getRouterName(),
                equalTo("R3"));
        assertThat(
                dockerHostAttachPointRepository.findByDockerHostName(FIRST_DOCKER_HOST_NAME).get().getRouterId(),
                equalTo("172.16.3.3"));
        assertThat(
                dockerHostAttachPointRepository.findByDockerHostName(FIRST_DOCKER_HOST_NAME).get().getRouterInterfaceName(),
                equalTo("ge-0/0/4"));
    }

    @Test
    public void shouldNotAddExistingDockerHostAttachPoint() throws Exception {
        dockerHostAttachPointRepository.save(dockerHostAttachPoint(FIRST_DOCKER_HOST_NAME));
        mvc.perform(post(URL_PREFIX + "/dockerhosts", FIRST_DOCKER_HOST_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dockerHostAttachPoint(FIRST_DOCKER_HOST_NAME)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void shouldNotAddInvalidDockerHostAttachPoint() throws Exception {
        DockerHostAttachPoint dockerHostAttachPoint = dockerHostAttachPoint(FIRST_DOCKER_HOST_NAME);
        dockerHostAttachPoint.setRouterInterfaceName(null);
        mvc.perform(post(URL_PREFIX + "/dockerhosts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dockerHostAttachPoint))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void shouldUpdateDockerHostAttachPoint() throws Exception {
        dockerHostAttachPointRepository.save(dockerHostAttachPoint(FIRST_DOCKER_HOST_NAME));
        DockerHostAttachPoint modifiedDockerHostAttachPoint = dockerHostAttachPoint(FIRST_DOCKER_HOST_NAME);
        modifiedDockerHostAttachPoint.setRouterInterfaceName("eth1");
        mvc.perform(put(URL_PREFIX + "/dockerhosts/{name}", FIRST_DOCKER_HOST_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(modifiedDockerHostAttachPoint))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertThat(dockerHostAttachPointRepository.findByDockerHostName(FIRST_DOCKER_HOST_NAME).isPresent(), is(true));
        assertThat(
                dockerHostAttachPointRepository.findByDockerHostName(FIRST_DOCKER_HOST_NAME).get().getRouterInterfaceName(),
                equalTo("eth1"));
    }

    @Test
    public void shouldNotUpdateNotExistingDockerHostAttachPoint() throws Exception {
        dockerHostAttachPointRepository.save(dockerHostAttachPoint(FIRST_DOCKER_HOST_NAME));
        mvc.perform(put(URL_PREFIX + "/dockerhosts/{name}", SECOND_DOCKER_HOST_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dockerHostAttachPoint(SECOND_DOCKER_HOST_NAME)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldNotUpdateInvalidDockerHostAttachPoint() throws Exception {
        dockerHostAttachPointRepository.save(dockerHostAttachPoint(FIRST_DOCKER_HOST_NAME));
        DockerHostAttachPoint modifiedDockerHostAttachPoint = dockerHostAttachPoint(FIRST_DOCKER_HOST_NAME);
        modifiedDockerHostAttachPoint.setRouterInterfaceName(null);
        mvc.perform(put(URL_PREFIX + "/dockerhosts/{name}", FIRST_DOCKER_HOST_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(modifiedDockerHostAttachPoint))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void shouldGetAllDockerHostAttachPoints() throws Exception {
        mvc.perform(get(URL_PREFIX + "/dockerhosts"))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldGetDockerHostAttachPointByDockerHostName() throws Exception {
        dockerHostAttachPointRepository.save(dockerHostAttachPoint(FIRST_DOCKER_HOST_NAME));
        MvcResult result = mvc.perform(get(URL_PREFIX + "/dockerhosts/{name}", FIRST_DOCKER_HOST_NAME))
                .andExpect(status().isOk()).andReturn();
        assertThat(
                ((DockerHostAttachPoint) new ObjectMapper().readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<DockerHostAttachPoint>() {})).getDockerHostName(),
                equalTo(FIRST_DOCKER_HOST_NAME));
    }

    @Test
    public void shouldNotGetDockerHostAttachPointByDockerHostName() throws Exception {
        dockerHostAttachPointRepository.save(dockerHostAttachPoint(FIRST_DOCKER_HOST_NAME));
        mvc.perform(get(URL_PREFIX + "/dockerhosts/{name}", SECOND_DOCKER_HOST_NAME))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldRemoveDockerHostAttachPoint() throws Exception {
        Long id = dockerHostAttachPointRepository.save(dockerHostAttachPoint(FIRST_DOCKER_HOST_NAME)).getId();
        assertThat(dockerHostAttachPointRepository.exists(id), is(true));
        mvc.perform(delete(URL_PREFIX + "/dockerhosts/{name}", FIRST_DOCKER_HOST_NAME))
                .andExpect(status().isNoContent());
        assertThat(dockerHostAttachPointRepository.exists(id), is(false));
    }

    @Test
    public void shouldNotRemoveNotExistingDockerHostAttachPoint() throws Exception {
        mvc.perform(delete(URL_PREFIX + "/dockerhosts/{name}", FIRST_DOCKER_HOST_NAME))
                .andExpect(status().isNotFound());
    }

    public static DockerHostAttachPoint dockerHostAttachPoint(String hostName) {
        DockerHostAttachPoint dockerHostAttachPoint = new DockerHostAttachPoint();
        dockerHostAttachPoint.setRouterName("R1");
        dockerHostAttachPoint.setRouterId("1.1.1.1");
        dockerHostAttachPoint.setRouterInterfaceName("eth0");
        dockerHostAttachPoint.setDockerHostName(hostName);
        return dockerHostAttachPoint;
    }

}
