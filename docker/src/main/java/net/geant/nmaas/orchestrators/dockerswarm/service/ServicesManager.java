package net.geant.nmaas.orchestrators.dockerswarm.service;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.NotFoundException;
import com.spotify.docker.client.messages.ServiceCreateResponse;
import com.spotify.docker.client.messages.swarm.ContainerSpec;
import com.spotify.docker.client.messages.swarm.ServiceSpec;
import com.spotify.docker.client.messages.swarm.TaskSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.spotify.docker.client.messages.swarm.Task.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class ServicesManager {

    @Autowired
    private DockerClient docker;

    public String deployTestService() throws NotFoundException {

        ServiceSpec service = new ServiceSpec.Builder()
                .withName("test-tomcat-" + System.currentTimeMillis())
                .withTaskTemplate(
                        new TaskSpec.Builder()
                                .withContainerSpec(
                                        new ContainerSpec.Builder()
                                                .withImage("tomcat:alpine")
                                                .build()
                                )
                        .build())
                .build();

        try {
            ServiceCreateResponse response = docker.createService(service);
            System.out.println("Service id: " + response.id());
            return response.id();

        } catch (DockerException | InterruptedException e) {
            e.printStackTrace();
            throw new NotFoundException("Could not create given service");
        }

    }

    public void tasks() throws DockerException, InterruptedException {

        System.out.println(docker.listTasks());

        System.out.println(docker.listTasks(Criteria.builder().withServiceName("test-tomcat-2").build()));

    }

    public List<com.spotify.docker.client.messages.swarm.Service> listServices() throws NotFoundException {
        final List<com.spotify.docker.client.messages.swarm.Service> services;
        try {
            services = docker.listServices();
            //return services.stream().map(s -> s.spec().name()).collect(Collectors.toList());
            return services;

        } catch (DockerException | InterruptedException e ) {
            e.printStackTrace();
            throw new NotFoundException("Failed to retrieve services");
        }
    }

}
