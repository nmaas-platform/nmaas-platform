package net.geant.nmaas;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.swarm.Service;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;
import java.util.List;

@SpringBootApplication
public class DockerApplication {

	private static final String DOCKER_MANAGER_API = "http://10.134.250.81:2375";

	public static void main(String[] args) {
		SpringApplication.run(DockerApplication.class, args);

		final DockerClient docker = DefaultDockerClient.builder().uri(URI.create(DOCKER_MANAGER_API)).build();
		final List<Service> services;
		try {
			services = docker.listServices();
			services.stream().map(s -> s.spec().name()).forEach(System.out::println);

		} catch (DockerException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
}
