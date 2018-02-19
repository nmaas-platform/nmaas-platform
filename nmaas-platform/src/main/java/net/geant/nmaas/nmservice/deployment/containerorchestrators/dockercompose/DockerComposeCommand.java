package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.utils.ssh.Command;

import java.util.function.Predicate;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerComposeCommand implements Command {

	private static final String CD = "cd";
	private static final String DOCKER_COMPOSE = "docker-compose";
	private static final String SPACE = " ";
	private static final String DOUBLE_AMPERSAND = "&&";

	public static DockerComposeCommand command(CommandType commandType, String targetDirectory) {
		return command(commandType, null, targetDirectory);
	}

	public static DockerComposeCommand command(CommandType commandType, String commandBody, String targetDirectory) {
		StringBuilder sb = new StringBuilder();
		sb.append(CD)
				.append(SPACE)
				.append(targetDirectory)
				.append(SPACE);
		sb.append(DOUBLE_AMPERSAND)
				.append(SPACE);
		sb.append(DOCKER_COMPOSE)
				.append(SPACE)
				.append(commandType.value);
		if (commandBody != null && !commandBody.isEmpty())
		    sb.append(SPACE)
                    .append(commandBody);
		return new DockerComposeCommand(sb.toString());
	}

	private String command;

	private DockerComposeCommand(String command) {
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

	public enum CommandType {
		UP("up -d"), DOWN("down"), PULL("pull"), STOP("stop"), REMOVE("rm -f"), EXEC("exec");

		private String value;

		CommandType(String value) {
			this.value = value;
		}
	}
}
