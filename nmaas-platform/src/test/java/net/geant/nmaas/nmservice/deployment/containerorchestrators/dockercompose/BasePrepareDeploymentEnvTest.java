package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostInvalidException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.nmservice.deployment.ContainerOrchestrator;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFileTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeService;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerApiClient;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.DockerHostNetworkRepositoryManager;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.entities.DockerHostNetwork;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.junit.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public abstract class BasePrepareDeploymentEnvTest {

    @Autowired
    protected ContainerOrchestrator manager;

    @Autowired
    private DockerHostRepositoryManager dockerHostRepositoryManager;
    @Autowired
    private DockerHostNetworkRepositoryManager dockerHostNetworkRepositoryManager;
    @Autowired
    private DockerComposeServiceRepositoryManager nmServiceRepositoryManager;
    @Autowired
    private ApplicationRepository applicationRepository;

    @MockBean
    private DockerApiClient dockerApiClient;
    @MockBean
    private DockerComposeCommandExecutor composeCommandExecutor;

    protected Identifier deploymentId = Identifier.newInstance("deploymentId");
    private Identifier clientId = Identifier.newInstance("10");
    private Identifier applicationId;
    private DockerComposeFileTemplate template;

    public void setup(String composeFileTemplatePath) throws Exception {
        dockerHostRepositoryManager.addDockerHost(dockerHost());
        DockerHost dockerHost = dockerHostRepositoryManager.loadPreferredDockerHost();
        dockerHostNetworkRepositoryManager.storeNetwork(dockerHostNetwork(clientId, dockerHost));
        prepareTestComposeFileTemplate(composeFileTemplatePath);
        applicationId = storeTestApplication();
        storeNmServiceInfo(dockerHost);
    }

    private void prepareTestComposeFileTemplate(String composeFileTemplatePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        template = mapper.readValue(
                new String(Files.readAllBytes(Paths.get(composeFileTemplatePath))),
                DockerComposeFileTemplate.class);
    }

    private Identifier storeTestApplication() {
        Application application = new Application("genericname");
        AppDeploymentSpec appDeploymentSpec = new AppDeploymentSpec();
        appDeploymentSpec.setDockerComposeFileTemplate(template);
        application.setAppDeploymentSpec(appDeploymentSpec);
        return Identifier.newInstance(String.valueOf(applicationRepository.save(application).getId()));
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

    @After
    public void clean() throws InvalidDeploymentIdException, DockerHostNotFoundException, DockerHostInvalidException {
        nmServiceRepositoryManager.removeService(deploymentId);
        dockerHostRepositoryManager.removeDockerHost("dh1");
        applicationRepository.deleteAll();
    }

    protected String contentOfGeneratedComposeFile() throws InvalidDeploymentIdException {
        return nmServiceRepositoryManager.loadService(deploymentId).getDockerComposeFile().getComposeFileContent();
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

    private static DockerHostNetwork dockerHostNetwork(Identifier clientId, DockerHost dockerHost) {
        return new DockerHostNetwork(clientId,
                dockerHost,
                500,
                "10.10.1.0/24",
                "10.10.1.254");
    }

}
