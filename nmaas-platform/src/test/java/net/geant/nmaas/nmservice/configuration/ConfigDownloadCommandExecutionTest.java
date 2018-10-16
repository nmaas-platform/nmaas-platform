package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostAlreadyExistsException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostInvalidException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigFileNotFoundException;
import net.geant.nmaas.nmservice.configuration.exceptions.FileTransferException;
import net.geant.nmaas.nmservice.configuration.repositories.NmServiceConfigFileRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.DockerComposeServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFileTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeService;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.ssh.Command;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import net.geant.nmaas.utils.ssh.SingleCommandExecutor;
import net.geant.nmaas.utils.ssh.SshConnectionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test-compose.properties")
public class ConfigDownloadCommandExecutionTest {

    @Autowired
    private DockerHostRepositoryManager dockerHostRepositoryManager;
    @Autowired
    private DockerComposeServiceRepositoryManager serviceRepositoryManager;
    @Autowired
    private NmServiceConfigFileRepository configurations;
    @Autowired
    private ConfigurationFileTransferProvider configDownloadCommandExecutor;

    private SingleCommandExecutor executor = mock(SingleCommandExecutor.class);

    private Identifier deploymentId = Identifier.newInstance("deploymentId");
    private String configId1 = "id1";

    // for password: testpass
    private static final String CORRECT_CONFIG_DOWNLOAD_COMMAND =
            "mkdir -p /home/mgmt/volumes/testVolumeName/ && wget --connect-timeout=3 --tries=2 --header=\"Authorization: Basic Y29uZmlnVGVzdDp0ZXN0cGFzcw==\" http://localhost:9000/api/configs/id1 -O /home/mgmt/volumes/testVolumeName/fileName1";

    @Before
    public void setup() throws UnknownHostException, DockerHostAlreadyExistsException, DockerHostInvalidException, DockerHostNotFoundException {
        dockerHostRepositoryManager.addDockerHost(dockerHost());
        DockerComposeNmServiceInfo nmServiceInfo = new DockerComposeNmServiceInfo(
                deploymentId,
                "deploymentName",
                "domain",
                20,
                new DockerComposeFileTemplate("testContent"));
        nmServiceInfo.setHost(dockerHostRepositoryManager.loadPreferredDockerHost());
        DockerComposeService dockerComposeService = new DockerComposeService();
        dockerComposeService.setAttachedVolumeName("testVolumeName");
        dockerComposeService.setPublicPort(8080);
        nmServiceInfo.setDockerComposeService(dockerComposeService);
        serviceRepositoryManager.storeService(nmServiceInfo);
        NmServiceConfiguration conf1 = new NmServiceConfiguration(configId1, "fileName1", "fileContent1");
        configurations.save(conf1);
        SingleCommandExecutor.setDefaultExecutor(executor);
    }

    @After
    public void cleanup() throws DockerHostNotFoundException, DockerHostInvalidException {
        serviceRepositoryManager.removeAllServices();
        dockerHostRepositoryManager.removeDockerHost("dh");
        configurations.deleteAll();
        SingleCommandExecutor.setDefaultExecutor(null);
    }

    @Test
    public void shouldPrepareConfigDownloadCommandString() throws FileTransferException, ConfigFileNotFoundException, InvalidDeploymentIdException, SshConnectionException, CommandExecutionException {
        configDownloadCommandExecutor.transferConfigFiles(deploymentId, Arrays.asList(configId1),true);
        ArgumentCaptor<Command> commandArgumentCaptor = ArgumentCaptor.forClass(Command.class);
        verify(executor, times(1)).executeSingleCommand(commandArgumentCaptor.capture());
        assertThat(commandArgumentCaptor.getValue().asString(), equalTo(CORRECT_CONFIG_DOWNLOAD_COMMAND));
    }

    private static DockerHost dockerHost() throws UnknownHostException {
        return new DockerHost(
                "dh",
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
