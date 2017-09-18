package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.externalservices.inventory.network.BasicCustomerNetworkAttachPoint;
import net.geant.nmaas.externalservices.inventory.network.CustomerNetworkMonitoredEquipment;
import net.geant.nmaas.externalservices.inventory.network.repositories.BasicCustomerNetworkAttachPointRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeService;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeServiceComponent;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.DockerNetworkResourceManager;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerOrchestratorInternalErrorException;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class StaticRoutingConfigManagerTest {

    @Autowired
    private StaticRoutingConfigManager manager;
    @Autowired
    private BasicCustomerNetworkAttachPointRepository customerNetworks;
    @MockBean
    private DockerComposeCommandExecutor composeCommandExecutor;
    @MockBean
    private DockerNetworkResourceManager dockerNetworkResourceManager;

    private Identifier customerId = Identifier.newInstance("1");
    private BasicCustomerNetworkAttachPoint customerNetwork = new BasicCustomerNetworkAttachPoint();
    private CustomerNetworkMonitoredEquipment equipment = new CustomerNetworkMonitoredEquipment();
    private NmServiceInfo service;

    @Before
    public void setup() throws ContainerOrchestratorInternalErrorException {
        service = new NmServiceInfo();
        service.setClientId(customerId);
        DockerComposeServiceComponent component1 = new DockerComposeServiceComponent();
        component1.setDeploymentName("deployedComponentName1");
        DockerComposeServiceComponent component2 = new DockerComposeServiceComponent();
        component2.setDeploymentName("deployedComponentName2");
        DockerComposeService dockerComposeService = new DockerComposeService();
        dockerComposeService.setServiceComponents(Arrays.asList(component1, component2));
        service.setDockerComposeService(dockerComposeService);
        customerNetwork.setCustomerId(customerId.longValue());
        customerNetwork.setAsNumber("");
        customerNetwork.setRouterId("");
        customerNetwork.setRouterName("");
        customerNetwork.setRouterInterfaceVlan("");
        customerNetwork.setRouterInterfaceName("");
        customerNetwork.setRouterInterfaceUnit("");
        customerNetwork.setBgpNeighborIp("");
        customerNetwork.setBgpLocalIp("");
        when(dockerNetworkResourceManager.obtainGatewayFromClientNetwork(any())).thenReturn("172.16.1.254");
    }

    @After
    public void cleanup() {
        customerNetworks.deleteAll();
    }

    @Test
    public void shouldAddRoutesForCustomerNetworkDevices() throws Exception {
        equipment.setAddresses(new ArrayList<>(Arrays.asList("10.10.1.1", "10.10.2.2", "10.10.3.3")));
        customerNetwork.setMonitoredEquipment(equipment);
        customerNetworks.save(customerNetwork);
        manager.configure(service);
        verify(composeCommandExecutor, times(6)).executeComposeExecCommand(any(), any(), any());
    }

    @Test
    public void shouldAddRoutesForCustomerNetworkDevicesTwice() throws Exception {
        equipment.setAddresses(new ArrayList<>(Arrays.asList("10.10.1.1", "10.10.2.2", "10.10.3.3")));
        customerNetwork.setMonitoredEquipment(equipment);
        customerNetworks.save(customerNetwork);
        manager.configure(service);
        verify(composeCommandExecutor, times(6)).executeComposeExecCommand(any(), any(), any());
        reset(composeCommandExecutor);
        manager.configure(service);
        verify(composeCommandExecutor, times(6)).executeComposeExecCommand(any(), any(), any());
    }

    @Test
    public void shouldAddRoutesForCustomerNetworkDevicesAndUserProvidedDevices() throws Exception {
        equipment.setAddresses(new ArrayList<>(Arrays.asList("10.10.1.1", "10.10.2.2", "10.10.3.3")));
        customerNetwork.setMonitoredEquipment(equipment);
        customerNetworks.save(customerNetwork);
        service.setManagedDevicesIpAddresses(new ArrayList<>(Arrays.asList("10.10.3.3", "10.10.4.4", "10.10.5.5")));
        manager.configure(service);
        ArgumentCaptor<String> commandBody = ArgumentCaptor.forClass(String.class);
        verify(composeCommandExecutor, times(10)).executeComposeExecCommand(any(), any(), commandBody.capture());
        assertThat(commandBody.getAllValues().stream().filter(c -> c.contains("/32")).count(), equalTo(10L));
    }

    @Test
    public void shouldAddRoutesForCustomerNetworkDevicesAndSubnetsAndUserProvidedDevices() throws Exception {
        equipment.setAddresses(new ArrayList<>(Arrays.asList("10.10.1.1", "10.10.2.2", "10.10.3.3")));
        equipment.setNetworks(new ArrayList<>(Arrays.asList("10.11.0.0/24", "10.11.1.0/24")));
        customerNetwork.setMonitoredEquipment(equipment);
        customerNetworks.save(customerNetwork);
        service.setManagedDevicesIpAddresses(new ArrayList<>(Arrays.asList("10.10.3.3", "10.10.4.4", "10.10.5.5")));
        manager.configure(service);
        ArgumentCaptor<String> commandBody = ArgumentCaptor.forClass(String.class);
        verify(composeCommandExecutor, times(14)).executeComposeExecCommand(any(), any(), commandBody.capture());
        assertThat(commandBody.getAllValues().stream().filter(c -> c.contains("/32")).count(), equalTo(10L));
        assertThat(commandBody.getAllValues().stream().filter(c -> c.contains("/24")).count(), equalTo(4L));
    }
}
