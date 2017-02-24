package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.DeploymentIdToNmServiceNameMapper;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceRepository;
import net.geant.nmaas.orchestration.AppConfiguration;
import net.geant.nmaas.orchestration.Identifier;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class UpdateNmServiceInfoWithDevicesTest {

    @Autowired
    private SimpleNmServiceConfigurationExecutor configurationExecutor;

    @Autowired
    private DeploymentIdToNmServiceNameMapper deploymentIdToNmServiceNameMapper;

    @Autowired
    private NmServiceRepository nmServiceRepository;

    private String nmServiceName = "testNmServiceName1";

    private Identifier deploymentId;

    private AppConfiguration appConfiguration;

    @Before
    public void setup() {
        NmServiceInfo serviceInfo = new NmServiceInfo(nmServiceName, NmServiceDeploymentState.INIT, null);
        nmServiceRepository.storeService(serviceInfo);
        deploymentIdToNmServiceNameMapper.storeMapping(deploymentId, nmServiceName);
        appConfiguration = new AppConfiguration();
        appConfiguration.setJsonInput("" +
                "{\"routers\": [\"1.1.1.1\",\"2.2.2.2\"], " +
                "\"oxidizedUsername\":\"user\", " +
                "\"oxidizedPassword\":\"pass\"}");
    }

    @Test
    public void shouldUpdateNmServiceInfoWithDevices() throws DeploymentIdToNmServiceNameMapper.EntryNotFoundException, NmServiceRepository.ServiceNotFoundException, IOException {
        final Map<String, Object> modelFromJson = configurationExecutor.getModelFromJson(appConfiguration);
        configurationExecutor.updateStoredNmServiceInfoWithListOfManagedDevices(deploymentId, modelFromJson);
        assertThat(nmServiceRepository.loadService(nmServiceName).getManagedDevicesIpAddresses(), Matchers.contains("1.1.1.1", "2.2.2.2"));
    }

}
