package net.geant.nmaas.servicedeployment.orchestrators.dockerswarm;

import net.geant.nmaas.servicedeployment.ServiceDeploymentConfig;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServiceDeploymentConfig.class)
@AutoConfigureMockMvc
public class DockerRestApiControllerTest {

    @Autowired
    private MockMvc mvc;

    @Ignore
    @Test
    public void shouldListBasicInfo() throws Exception {
        MvcResult result = mvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getContentAsString(), org.hamcrest.Matchers.containsString("docker-1"));
    }

    @Ignore
    @Test
    public void shouldListServices() throws Exception {
        mvc.perform(get("/api/services"))
                .andExpect(status().isOk());
    }

    @Ignore
    @Test
    public void shouldDeployTestService() throws Exception {
        MvcResult result = mvc.perform(post("/api/services"))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
    }

}
