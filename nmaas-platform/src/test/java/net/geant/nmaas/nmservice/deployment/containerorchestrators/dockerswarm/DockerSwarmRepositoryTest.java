package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerswarm;

import net.geant.nmaas.externalservices.inventory.dockerswams.DockerSwarmNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerswams.DockerSwarmsRepository;
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
public class DockerSwarmRepositoryTest {

    @Autowired
    private DockerSwarmsRepository swarms;

    @Test
    public void shouldLoadDefaultSwarmManager() throws DockerSwarmNotFoundException {
        assertNotNull(swarms.loadPreferredDockerSwarmManager());
    }

}
