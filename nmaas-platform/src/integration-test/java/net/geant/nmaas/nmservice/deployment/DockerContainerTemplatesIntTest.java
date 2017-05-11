package net.geant.nmaas.nmservice.deployment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.docker.client.exceptions.DockerException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerApiClient;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerTemplate;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerContainerTemplatesIntTest {

    @Autowired
    private DockerHostRepositoryManager dockerHostRepositoryManager;

    private static final String jsonRepresentationOfOxidizedTemplate =
            "{\"image\":\"oxidized/oxidized:latest\",\"exposedPort\":{\"protocol\":\"TCP\",\"targetPort\":8888},\"envVariables\":[\"CONFIG_RELOAD_INTERVAL=600\"],\"containerVolumes\":[\"/root/.config/oxidized\"]}";

    @Ignore
    @Test
    public void shouldSerializeOxidizedContainerTemplateAndPullImage() throws IOException, DockerHostNotFoundException, DockerException, InterruptedException {
        ObjectMapper mapper = new ObjectMapper();
        DockerContainerTemplate fromJson = mapper.readValue(jsonRepresentationOfOxidizedTemplate, DockerContainerTemplate.class);
        new DockerApiClient().pull(dockerHostRepositoryManager.loadPreferredDockerHost().apiUrl(), fromJson.getImage());
    }

}
