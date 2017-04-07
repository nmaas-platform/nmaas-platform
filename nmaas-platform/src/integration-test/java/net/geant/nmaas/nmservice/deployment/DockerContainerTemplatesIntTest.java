package net.geant.nmaas.nmservice.deployment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.docker.client.exceptions.DockerException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerApiClientFactory;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerContainerTemplate;
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
    private DockerHostRepository dockerHostRepository;

    private static final String jsonRepresentationOfOxidizedTemplate =
            "{\"image\":\"oxidized/oxidized:latest\",\"exposedPort\":{\"protocol\":\"TCP\",\"targetPort\":8888},\"envVariables\":[\"CONFIG_RELOAD_INTERVAL=600\"],\"envVariablesInSpecRequired\":false,\"containerVolumes\":[\"/root/.config/oxidized\"]}";

    @Ignore
    @Test
    public void shouldSerializeOxidizedContainerTemplateAndPullImage() throws IOException, DockerHostNotFoundException, DockerException, InterruptedException {
        ObjectMapper mapper = new ObjectMapper();
        DockerContainerTemplate fromJson = mapper.readValue(jsonRepresentationOfOxidizedTemplate, DockerContainerTemplate.class);
        DockerApiClientFactory.client(dockerHostRepository.loadPreferredDockerHost().apiUrl()).pull(fromJson.getImage());
    }

}
