package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.exceptions.UserConfigHandlingException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerEngineServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerPortForwarding;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerEngineNmServiceInfo;
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
    private DockerEngineServiceRepositoryManager nmServiceRepositoryManager;

    private Identifier deploymentId1 = Identifier.newInstance("deploymentId1");
    private Identifier applicationId1 = Identifier.newInstance("applicationId1");
    private Identifier deploymentId2 = Identifier.newInstance("deploymentId2");
    private Identifier applicationId2 = Identifier.newInstance("applicationId2");
    private Identifier clientId = Identifier.newInstance("clientId");

    @Before
    public void setup() {
        DockerEngineNmServiceInfo serviceInfo = new DockerEngineNmServiceInfo(deploymentId1, applicationId1, clientId, oxidizedTemplate());
        nmServiceRepositoryManager.storeService(serviceInfo);
        serviceInfo = new DockerEngineNmServiceInfo(deploymentId2, applicationId2, clientId, null);
        nmServiceRepositoryManager.storeService(serviceInfo);
    }

    @After
    public void cleanRepositories() throws InvalidDeploymentIdException {
        nmServiceRepositoryManager.removeService(deploymentId1);
        nmServiceRepositoryManager.removeService(deploymentId2);
    }

    @Test
    public void shouldUpdateNmServiceInfoWithDevicesFromOxidizedConfig() throws InvalidDeploymentIdException, UserConfigHandlingException {
        AppConfiguration appConfiguration = new AppConfiguration(AppConfigurationJsonToMapTest.EXAMPLE_OXIDIZED_CONFIG_FORM_INPUT);
        final Map<String, Object> modelFromJson = configurationsPreparer.createModelFromJson(appConfiguration);
        configurationsPreparer.updateStoredNmServiceInfoWithListOfManagedDevices(deploymentId1, modelFromJson);
        assertThat(nmServiceRepositoryManager.loadService(deploymentId1).getManagedDevicesIpAddresses(), Matchers.contains("1.1.1.1", "2.2.2.2"));
    }

    @Test
    public void shouldUpdateNmServiceInfoWithDevicesFromLibreNmsConfig() throws InvalidDeploymentIdException, UserConfigHandlingException {
        AppConfiguration appConfiguration = new AppConfiguration(AppConfigurationJsonToMapTest.EXAMPLE_LIBRENMS_CONFIG_FORM_INPUT);
        final Map<String, Object> modelFromJson = configurationsPreparer.createModelFromJson(appConfiguration);
        configurationsPreparer.updateStoredNmServiceInfoWithListOfManagedDevices(deploymentId2, modelFromJson);
        assertThat(nmServiceRepositoryManager.loadService(deploymentId2).getManagedDevicesIpAddresses(), Matchers.contains("192.168.1.1", "10.10.3.2"));
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
