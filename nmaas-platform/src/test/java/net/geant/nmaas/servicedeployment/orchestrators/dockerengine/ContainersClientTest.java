package net.geant.nmaas.servicedeployment.orchestrators.dockerengine;

import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.container.DockerContainerClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ContainersClientTest {

    @Autowired
    private DockerContainerClient containerClient;

    @Test
    public void shouldInjectManager() {
        assertNotNull(containerClient);
    }

}
