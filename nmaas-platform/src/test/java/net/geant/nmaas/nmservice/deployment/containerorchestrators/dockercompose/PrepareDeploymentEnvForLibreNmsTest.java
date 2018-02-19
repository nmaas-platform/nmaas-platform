package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostInvalidException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test-compose.properties")
public class PrepareDeploymentEnvForLibreNmsTest extends BasePrepareDeploymentEnvTest {

    private final static String LIBRENMS_DOCKER_COMPOSE_TEMPLATE_XML_FILE = "src/test/shell/data/apps/templates/dockercompose/app1-template1.json";

    @Before
    public void setup() throws Exception {
        setup(LIBRENMS_DOCKER_COMPOSE_TEMPLATE_XML_FILE);
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
                        containsString("deploymentId-librenms"),
                        containsString("10.10.1.1"),
                        containsString("nmaas-access"),
                        containsString("nmaas-dcn-10-vlan500")));
    }

}
