package net.geant.nmaas.servicedeployment;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ServiceDeploymentConfig {

	@Autowired
	Environment env;

	@Bean
	public DockerClient dockerClient() {
		return DefaultDockerClient.builder().uri(env.getProperty("docker.api")).build();
	}

}
