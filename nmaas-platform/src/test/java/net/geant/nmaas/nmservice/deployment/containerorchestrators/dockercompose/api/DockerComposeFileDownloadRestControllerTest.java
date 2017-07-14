package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.api;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories.DockerComposeFile;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories.DockerComposeFileRepository;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerComposeFileDownloadRestControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    private DockerComposeFileRepository composeFileRepository;

    private MockMvc mvc;

    private Identifier deploymentId = Identifier.newInstance("deploymentId");

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Test
    public void shouldReturnSimpleComposeFile() throws Exception {
        byte[] composeFileBytes = new byte[]{1,2,3,4,5};
        composeFileRepository.storeFileContent(deploymentId, new DockerComposeFile(composeFileBytes));
        mvc.perform(get("/platform/api/dockercompose/files/{deploymentId}", deploymentId.value())
                .with(httpBasic(context.getEnvironment().getProperty("api.client.config.download.username"), context.getEnvironment().getProperty("api.client.config.download.password"))))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment;filename=" + DockerComposeFile.DEFAULT_DOCKER_COMPOSE_FILE_NAME))
                .andExpect(content().contentTypeCompatibleWith("application/octet-stream"))
                .andExpect(content().bytes(composeFileBytes));
    }

    @Test
    public void shouldForbidDownload() throws Exception {
        mvc.perform(get("/platform/api/dockercompose/files/{deploymentId}", deploymentId.value())
                .with(httpBasic("testClient", "testPassword")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturnNotFoundOnMissingComposeFileWithProvidedDeploymentId() throws Exception {
        mvc.perform(get("/platform/api/dockercompose/files/{deploymentId}", deploymentId.value() + "invalid-string")
                .with(httpBasic(context.getEnvironment().getProperty("api.client.config.download.username"), context.getEnvironment().getProperty("api.client.config.download.password"))))
                .andExpect(status().isNotFound());
    }

}
