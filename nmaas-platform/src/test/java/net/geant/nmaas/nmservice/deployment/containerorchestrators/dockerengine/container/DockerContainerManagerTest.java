package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainer;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerContainerManagerTest {

    @Autowired
    private DockerContainerManager manager;

    private Identifier deploymentId = Identifier.newInstance("deploymentId");

    @Test
    public void shouldDeclareNewContainerForDeployment() {
        final DockerContainer container = manager.declareNewContainerForDeployment(deploymentId);
        assertThat(container, is(notNullValue()));
        assertThat(container.getDeploymentId(), is(nullValue()));
        assertThat(container.getNetworkDetails(), is(nullValue()));
        assertThat(container.getVolumesDetails(), is(notNullValue()));
        assertThat(container.getVolumesDetails().getAttachedVolumeName(), is(equalTo(deploymentId.getValue() + "-1")));
    }

}
