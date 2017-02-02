package net.geant.nmaas.servicedeployment.orchestrators.dockerengine;

import com.spotify.docker.client.DefaultDockerClient;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerApiClientFactory {

    public static DefaultDockerClient client(String uri) {
        return DefaultDockerClient.builder().uri(uri).build();
    }

}
