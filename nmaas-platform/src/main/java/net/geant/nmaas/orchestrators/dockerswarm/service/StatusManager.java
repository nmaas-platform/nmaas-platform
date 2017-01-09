package net.geant.nmaas.orchestrators.dockerswarm.service;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.NotFoundException;
import com.spotify.docker.client.messages.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class StatusManager {

    @Autowired
    private DockerClient docker;

    public Info info() throws NotFoundException {
        try {
            return docker.info();
        } catch (DockerException | InterruptedException e) {
            e.printStackTrace();
            throw new NotFoundException("Failed to connect and retrieve basic info");
        }
    }

}
