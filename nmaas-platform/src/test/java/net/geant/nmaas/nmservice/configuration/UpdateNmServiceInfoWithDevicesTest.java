package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.exceptions.UserConfigHandlingException;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
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

    private Identifier deploymentId1 = Identifier.newInstance("deploymentId1");
    private Identifier applicationId1 = Identifier.newInstance("applicationId1");
    private Identifier clientId = Identifier.newInstance("clientId");

    @Before
    public void setup() {
        NmServiceInfo serviceInfo = new NmServiceInfo(deploymentId1, applicationId1, clientId);
        nmServiceRepositoryManager.storeService(serviceInfo);
    }

    @After
    public void cleanRepositories() throws InvalidDeploymentIdException {
        nmServiceRepositoryManager.removeService(deploymentId1);
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
        configurationsPreparer.updateStoredNmServiceInfoWithListOfManagedDevices(deploymentId1, modelFromJson);
        assertThat(nmServiceRepositoryManager.loadService(deploymentId1).getManagedDevicesIpAddresses(), Matchers.contains("192.168.1.1", "10.10.3.2"));
    }

    @Test
    public void shouldUpdateNmServiceInfoWithDevicesFromNavConfig() throws InvalidDeploymentIdException, UserConfigHandlingException {
        AppConfiguration appConfiguration = new AppConfiguration("{}");
        final Map<String, Object> modelFromJson = configurationsPreparer.createModelFromJson(appConfiguration);
        configurationsPreparer.updateStoredNmServiceInfoWithListOfManagedDevices(deploymentId1, modelFromJson);
        assertThat(nmServiceRepositoryManager.loadService(deploymentId1).getManagedDevicesIpAddresses(), Matchers.emptyCollectionOf(String.class));
    }

}
