package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.utils.ssh.Command;

import java.util.function.Predicate;

public class DockerComposeUpCommand implements Command {

	private static final String CD = "cd";
	private static final String DOCKER_COMPOSE = "docker-compose";
	private static final String UP = "up";
	private static final String SPACE = " ";
	private static final String DOUBLE_AMPERSAND = "&&";

	public static DockerComposeUpCommand command(String targetDirectory) {
		StringBuilder sb = new StringBuilder();
		sb.append(CD)
				.append(SPACE)
				.append(targetDirectory)
				.append(SPACE);
		sb.append(DOUBLE_AMPERSAND)
				.append(SPACE);
		sb.append(DOCKER_COMPOSE)
				.append(SPACE)
				.append(UP);
		return new DockerComposeUpCommand(sb.toString());
	}

	private String command;

	private DockerComposeUpCommand(String command) {
		this.command = command;
	}

	@Override
	public String asString() {
		return command;
	}

	@Override
	public Predicate<String> isOutputCorrect() {
		return o -> true;
	}
}
