package net.geant.nmaas;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@SpringBootApplication
public class DockerApplication {

	@Autowired
	Environment env;

	public static void main(String[] args) {
		SpringApplication.run(DockerApplication.class, args);
	}

	@Bean
	public DockerClient dockerClient() {
		return DefaultDockerClient.builder().uri(env.getProperty("docker.api")).build();
	}

}
