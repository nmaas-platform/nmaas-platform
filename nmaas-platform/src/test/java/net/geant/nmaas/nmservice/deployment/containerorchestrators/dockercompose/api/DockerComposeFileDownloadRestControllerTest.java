package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.api;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.DockerComposeServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFile;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeNmServiceInfo;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
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
@ActiveProfiles("docker-compose")
public class DockerComposeFileDownloadRestControllerTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private Filter springSecurityFilterChain;
    @Autowired
    private DockerComposeServiceRepositoryManager repositoryManager;

    private MockMvc mvc;
    private Identifier deploymentId = Identifier.newInstance("deploymentId");
    private Identifier clientId = Identifier.newInstance("clientId");
    private Identifier applicationId = Identifier.newInstance("applicationId");
    private String composeFileContent = "simple content";

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
        DockerComposeNmServiceInfo nmServiceInfo = new DockerComposeNmServiceInfo(deploymentId, applicationId, clientId, null);
        nmServiceInfo.setDockerComposeFile(new DockerComposeFile(composeFileContent));
        repositoryManager.storeService(nmServiceInfo);
    }

    @After
    public void clean() {
        repositoryManager.removeAllServices();
    }

    @Test
    public void shouldReturnSimpleComposeFile() throws Exception {
        mvc.perform(get("/platform/api/dockercompose/files/{deploymentId}", deploymentId.value())
                .with(httpBasic(context.getEnvironment().getProperty("app.compose.download.client.username"), context.getEnvironment().getProperty("app.compose.download.client.password"))))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment;filename=" + DockerComposeFile.DEFAULT_DOCKER_COMPOSE_FILE_NAME))
                .andExpect(content().contentTypeCompatibleWith("application/octet-stream"))
                .andExpect(content().string(composeFileContent));
    }

    @Test
    public void shouldForbidDownload() throws Exception {
        mvc.perform(get("/platform/api/dockercompose/files/{deploymentId}", deploymentId.value())
                .with(httpBasic("testClient", "testPassword")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturnNotFoundOnMissingDeploymentAndMissingComposeFileWithProvidedDeploymentId() throws Exception {
        mvc.perform(get("/platform/api/dockercompose/files/{deploymentId}", deploymentId.value() + "invalid-string")
                .with(httpBasic(context.getEnvironment().getProperty("app.compose.download.client.username"), context.getEnvironment().getProperty("app.compose.download.client.password"))))
                .andExpect(status().isNotFound());
        Identifier deploymentId = Identifier.newInstance("newDeploymentId");
        DockerComposeNmServiceInfo nmServiceInfo = new DockerComposeNmServiceInfo(deploymentId, clientId, applicationId, null);
        repositoryManager.storeService(nmServiceInfo);
        mvc.perform(get("/platform/api/dockercompose/files/{deploymentId}", deploymentId.value())
                .with(httpBasic(context.getEnvironment().getProperty("app.compose.download.client.username"), context.getEnvironment().getProperty("app.compose.download.client.password"))))
                .andExpect(status().isNotFound());
    }

}