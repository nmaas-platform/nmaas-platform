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

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test-compose.properties")
public class DockerComposeServiceRepositoryManagerTest {

    @Autowired
    private DockerComposeServiceRepositoryManager manager;

    private static final String DOMAIN = "domain";
    private static final String DEPLOYMENT_NAME = "deploymentName";
    private Identifier deploymentId = Identifier.newInstance("deploymentId");

    @Test
    public void shouldAddUpdateAndRemoveNmServiceInfo() throws InvalidDeploymentIdException {
        DockerComposeNmServiceInfo info = new DockerComposeNmServiceInfo(deploymentId, DEPLOYMENT_NAME, DOMAIN, 20, null);
        manager.storeService(info);
        DockerComposeNmServiceInfo storedInfo = manager.loadService(deploymentId);
        assertThat(storedInfo, is(notNullValue()));
        assertThat(storedInfo.getDockerComposeFileTemplate(), is(nullValue()));
        storedInfo.setDockerComposeFileTemplate(new DockerComposeFileTemplate("testContent"));
        manager.updateService(storedInfo);
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
