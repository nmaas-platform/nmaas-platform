package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostInvalidException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.nmservice.deployment.ContainerOrchestrator;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeService;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.DockerNetworkLifecycleManager;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.InetAddress;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("docker-compose")
public class RemoveServiceTest {

    @Autowired
    private ContainerOrchestrator manager;

    @Autowired
    private DockerHostRepositoryManager dockerHostRepositoryManager;
    @Autowired
    private NmServiceRepositoryManager nmServiceRepositoryManager;

    @MockBean
    private DockerComposeCommandExecutor composeCommandExecutor;
    @MockBean
    private DockerNetworkLifecycleManager dockerNetworkLifecycleManager;

    private Identifier deploymentId = Identifier.newInstance("deploymentId");
    private Identifier clientId = Identifier.newInstance("10");
    private Identifier applicationId = Identifier.newInstance("1");

    @Before
    public void setup() throws Exception {
        dockerHostRepositoryManager.addDockerHost(dockerHost());
        DockerHost dockerHost = dockerHostRepositoryManager.loadPreferredDockerHost();
        storeNmServiceInfo(dockerHost);
    }

    @After
    public void clean() throws InvalidDeploymentIdException, DockerHostNotFoundException, DockerHostInvalidException {
        nmServiceRepositoryManager.removeService(deploymentId);
        dockerHostRepositoryManager.removeDockerHost("dh1");
    }

    @Test
    public void shouldRemoveService() throws Exception {
        manager.removeNmService(deploymentId);
        verify(dockerNetworkLifecycleManager, times(1)).removeNetwork(any());
    }

    private void storeNmServiceInfo(DockerHost dockerHost) {
        DockerComposeNmServiceInfo serviceInfo = new DockerComposeNmServiceInfo(deploymentId, applicationId, clientId, null);
        serviceInfo.setHost(dockerHost);
        serviceInfo.setDockerComposeService(dockerComposeService());
        nmServiceRepositoryManager.storeService(serviceInfo);
    }

    private static DockerComposeService dockerComposeService() {
        DockerComposeService dockerComposeService = new DockerComposeService();
        dockerComposeService.setAttachedVolumeName("/volume");
        dockerComposeService.setPublicPort(1000);
        return dockerComposeService;
    }

    private static DockerHost dockerHost() throws Exception {
        return new DockerHost("dh1",
                InetAddress.getByName("192.168.0.1"),
                9999,
                InetAddress.getByName("192.168.0.1"),
                "eth0",
                "eth1",
                InetAddress.getByName("192.168.1.1"),
                "/home/mgmt/scripts",
                "/home/mgmt/volumes",
                true);
    }

}
