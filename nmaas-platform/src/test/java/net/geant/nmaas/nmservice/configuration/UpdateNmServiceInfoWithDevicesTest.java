package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerPortForwarding;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerTemplate;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class UpdateNmServiceInfoWithDevicesTest {

    @Autowired
    private NmServiceConfigurationsPreparer configurationsPreparer;

    @Autowired
    private NmServiceRepositoryManager nmServiceRepositoryManager;

    private Identifier deploymentId = Identifier.newInstance("deploymentId");

    private Identifier clientId = Identifier.newInstance("clientId");

    private AppConfiguration appConfiguration;

    @Before
    public void setup() {
        NmServiceInfo serviceInfo = new NmServiceInfo(deploymentId, clientId, oxidizedTemplate());
        nmServiceRepositoryManager.storeService(serviceInfo);
        deploymentId = Identifier.newInstance("deploymentId");
        appConfiguration = new AppConfiguration("" +
                "{\"routers\": [\"1.1.1.1\",\"2.2.2.2\"], " +
                "\"oxidizedUsername\":\"user\", " +
                "\"oxidizedPassword\":\"pass\"}");
    }

    @After
    public void cleanRepositories() throws InvalidDeploymentIdException {
        nmServiceRepositoryManager.removeService(deploymentId);
    }

    @Test
    public void shouldUpdateNmServiceInfoWithDevices() throws InvalidDeploymentIdException, IOException {
        final Map<String, Object> modelFromJson = configurationsPreparer.getModelFromJson(appConfiguration);
        configurationsPreparer.updateStoredNmServiceInfoWithListOfManagedDevices(deploymentId, modelFromJson);
        assertThat(nmServiceRepositoryManager.loadService(deploymentId).getManagedDevicesIpAddresses(), Matchers.contains("1.1.1.1", "2.2.2.2"));
    }

    public static DockerContainerTemplate oxidizedTemplate() {
        DockerContainerTemplate oxidizedTemplate =
                new DockerContainerTemplate("oxidized/oxidized:latest");
        oxidizedTemplate.setEnvVariables(Arrays.asList("CONFIG_RELOAD_INTERVAL=600"));
        oxidizedTemplate.setExposedPort(new DockerContainerPortForwarding(DockerContainerPortForwarding.Protocol.TCP, 8888));
        oxidizedTemplate.setContainerVolumes(Arrays.asList("/root/.config/oxidized"));
        return oxidizedTemplate;
    }

}
