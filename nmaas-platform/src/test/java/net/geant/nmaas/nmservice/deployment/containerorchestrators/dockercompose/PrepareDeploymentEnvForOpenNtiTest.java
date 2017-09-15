package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostInvalidException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.nmservice.deployment.ContainerOrchestrator;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFileTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeService;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerApiClient;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.DockerHostNetworkRepositoryManager;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.nmservice.deployment.entities.DockerHostNetwork;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("docker-compose")
public class PrepareDeploymentEnvForOpenNtiTest extends BasePrepareDeploymentEnvTest {

    private final static String OPENNTI_DOCKER_COMPOSE_TEMPLATE_XML_FILE = "src/test/shell/data/apps/templates/dockercompose/app4-template1.json";

    @Before
    public void setup() throws Exception {
        setup(OPENNTI_DOCKER_COMPOSE_TEMPLATE_XML_FILE);
    }

    @After
    public void clean() throws InvalidDeploymentIdException, DockerHostNotFoundException, DockerHostInvalidException {
        super.clean();
    }

    @Test
    public void shouldBuildAndStoreComposeFileFromOpenNtiComposeTemplateXml() throws Exception {
        manager.prepareDeploymentEnvironment(deploymentId);
        assertThat(contentOfGeneratedComposeFile(), allOf(
                        containsString("1000:"),
                        containsString("/volume"),
                        containsString("deploymentId-opennti"),
                        containsString("10.10.1.1"),
                        containsString("10.10.1.2"),
                        containsString("10.10.1.3"),
                        containsString("10.10.1.4"),
                        containsString("nmaas-access"),
                        containsString("nmaas-dcn-10-vlan500")));
    }

}
