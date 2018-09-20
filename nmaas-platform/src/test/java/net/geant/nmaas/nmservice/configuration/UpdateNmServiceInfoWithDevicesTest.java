package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.exceptions.UserConfigHandlingException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.DockerComposeServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFileTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeNmServiceInfo;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test-compose.properties")
public class UpdateNmServiceInfoWithDevicesTest {

    @Autowired
    private NmServiceConfigurationFilePreparer configurationsPreparer;
    @Autowired
    private DockerComposeServiceRepositoryManager nmServiceRepositoryManager;

    private static final String DOMAIN = "domain";
    private static final String DEPLOYMENT_NAME_1 = "deploymentName1";
    private static final String DEPLOYMENT_NAME_2 = "deploymentName2";
    private Identifier deploymentId1 = Identifier.newInstance("deploymentId1");
    private Identifier deploymentId2 = Identifier.newInstance("deploymentId2");

    @Before
    public void setup() {
        DockerComposeNmServiceInfo serviceInfo = new DockerComposeNmServiceInfo(deploymentId1, DEPLOYMENT_NAME_1, DOMAIN, null);
        nmServiceRepositoryManager.storeService(serviceInfo);
        serviceInfo = new DockerComposeNmServiceInfo(deploymentId2, DEPLOYMENT_NAME_2, DOMAIN, new DockerComposeFileTemplate("testContent"));
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

}
