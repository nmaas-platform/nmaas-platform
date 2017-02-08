package net.geant.nmaas.nmservicedeployment.containerorchestrators.dockerswarm.service;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.NotFoundException;
import com.spotify.docker.client.messages.Info;
import net.geant.nmaas.externalservices.inventory.dockerswams.DockerSwarmManager;
import org.springframework.stereotype.Service;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class SwarmStatusClient {

    public Info info(DockerSwarmManager swarmManager) throws NotFoundException {
        DockerClient apiClient = null;
        try {
            apiClient = DefaultDockerClient.builder().uri(swarmManager.getApiUri()).build();
            return apiClient.info();
        } catch (DockerException | InterruptedException e) {
            e.printStackTrace();
            throw new NotFoundException("Failed to connect and retrieve basic info");
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

}
