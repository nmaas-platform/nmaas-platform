package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerContainerTemplatesTests {

    private static final String jsonRepresentationOfOxidizedTemplate =
            "{\"image\":\"oxidized/oxidized:latest\",\"exposedPort\":{\"protocol\":\"TCP\",\"targetPort\":8888},\"envVariables\":[\"CONFIG_RELOAD_INTERVAL=600\"],\"envVariablesInSpecRequired\":false,\"containerVolumes\":[\"/root/.config/oxidized\"]}";

    private DockerContainerTemplate oxidizedTemplate;

    @Before
    public void setup() {
        oxidizedTemplate =
                new DockerContainerTemplate("oxidized/oxidized:latest");
        oxidizedTemplate.setEnvVariables(Arrays.asList("CONFIG_RELOAD_INTERVAL=600"));
        oxidizedTemplate.setEnvVariablesInSpecRequired(false);
        oxidizedTemplate.setExposedPort(new DockerContainerPortForwarding(DockerContainerPortForwarding.Protocol.TCP, 8888));
        oxidizedTemplate.setContainerVolumes(Arrays.asList("/root/.config/oxidized"));
    }

    @Test
    public void shouldSerializeOxidizedContainerTemplate() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        DockerContainerTemplate fromJson = mapper.readValue(jsonRepresentationOfOxidizedTemplate, DockerContainerTemplate.class);
        assertThat(fromJson, equalTo(oxidizedTemplate));
    }

    @Test
    public void shouldCreateACopy() {
        DockerContainerTemplate copy = DockerContainerTemplate.copy(oxidizedTemplate);
        assertThat(copy, equalTo(oxidizedTemplate));
        oxidizedTemplate.setImage("newImage");
        assertThat(copy.getImage(), equalTo("oxidized/oxidized:latest"));
    }

}
