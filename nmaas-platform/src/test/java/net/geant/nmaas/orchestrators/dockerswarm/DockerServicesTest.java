package net.geant.nmaas.orchestrators.dockerswarm;

import com.spotify.docker.client.exceptions.DockerException;
import net.geant.nmaas.orchestrators.dockerswarm.service.ServicesManager;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerServicesTest {

    @Autowired
    private ServicesManager services;

    @Ignore
    @Test
    public void shouldListTasks() throws DockerException, InterruptedException {
        services.tasks();
    }

}
