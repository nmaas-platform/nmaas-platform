package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFileTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeNmServiceInfo;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test-compose.properties")
public class DockerComposeServiceRepositoryManagerTest {

    @Autowired
    private DockerComposeServiceRepositoryManager manager;

    private Identifier deploymentId = Identifier.newInstance("deploymentId");
    private Identifier applicationId = Identifier.newInstance("applicationId");
    private Identifier clientId = Identifier.newInstance("clientId");

    @Test
    public void shouldAddUpdateAndRemoveNmServiceInfo() throws InvalidDeploymentIdException {
        DockerComposeNmServiceInfo info = new DockerComposeNmServiceInfo(deploymentId, applicationId, clientId, null);
        manager.storeService(info);
        DockerComposeNmServiceInfo storedInfo = manager.loadService(deploymentId);
        assertThat(storedInfo, is(notNullValue()));
        assertThat(storedInfo.getDockerComposeFileTemplate(), is(nullValue()));
        storedInfo.setDockerComposeFileTemplate(new DockerComposeFileTemplate("testContent"));
        manager.storeService(storedInfo);
        storedInfo = manager.loadService(deploymentId);
        assertThat(storedInfo.getDockerComposeFileTemplate(), is(notNullValue()));
        manager.removeAllServices();
        try {
            manager.loadService(deploymentId);
        } catch (InvalidDeploymentIdException e) {
            return;
        }
        assert false;
    }

}
