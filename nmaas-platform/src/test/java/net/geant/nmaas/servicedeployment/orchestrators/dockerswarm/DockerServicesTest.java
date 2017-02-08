package net.geant.nmaas.servicedeployment.orchestrators.dockerswarm;

import net.geant.nmaas.nmservicedeployment.containerorchestrators.dockerswarm.service.SwarmServicesClient;
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
public class DockerServicesTest {

    @Autowired
    private SwarmServicesClient servicesClient;

    @Test
    public void shouldInjectManager() {
        assertNotNull(servicesClient);
    }

}
