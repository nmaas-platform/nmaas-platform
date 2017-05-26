package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.utils.ssh.Command;

import java.util.function.Predicate;

/**
 * Represents the configuration file download command to be executed on remote machine.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class ConfigDownloadCommand implements Command {

	private static final String MKDIR_COMMAND = "mkdir -p";
	private static final String WGET_COMMAND = "wget";
	private static final String HEADER_OPTION = "--header";
	private static final String AUTHORIZATION_HEADER_NAME = "Authorization";
	private static final String AUTHORIZATION_TYPE_BASIC = "Basic";
	private static final String OUTPUT_FILE_OPTION = "-O";
	private static final String CONNECT_TIMEOUT_OPTION = "--connect-timeout";
	private static final int DEFAULT_CONNECT_TIMEOUT = 3;
	private static final String CONNECT_TRIES_OPTION = "--tries";
	private static final int DEFAULT_CONNECT_TRIES = 2;
	private static final String SPACE = " ";
	private static final String IS = "=";
	private static final String SLASH = "/";
	private static final String QUOTE = "\"";
	private static final String COLON = ":";
	private static final String DOUBLE_AMPERSAND = "&&";

	public static ConfigDownloadCommand command(
			String authorizationHash,
			String sourceUrl,
			String configId,
			String targetDirectory,
			String targetFile) {
		sourceUrl = appendSlashToSourceUrlIfRequired(sourceUrl);
		targetDirectory = appendSlashToTargetDirectoryIfRequired(targetDirectory);
		StringBuilder sb = new StringBuilder();
		sb.append(MKDIR_COMMAND)
				.append(SPACE)
				.append(targetDirectory)
				.append(SPACE);
		sb.append(DOUBLE_AMPERSAND)
				.append(SPACE);
		sb.append(WGET_COMMAND)
				.append(SPACE)
				.append(CONNECT_TIMEOUT_OPTION)
				.append(IS)
				.append(DEFAULT_CONNECT_TIMEOUT)
				.append(SPACE)
				.append(CONNECT_TRIES_OPTION)
				.append(IS)
				.append(DEFAULT_CONNECT_TRIES)
				.append(SPACE)
				.append(HEADER_OPTION)
				.append(IS)
				.append(QUOTE)
				.append(AUTHORIZATION_HEADER_NAME)
				.append(COLON)
				.append(SPACE)
				.append(AUTHORIZATION_TYPE_BASIC)
				.append(SPACE)
				.append(authorizationHash)
				.append(QUOTE)
				.append(SPACE)
				.append(sourceUrl)
				.append(configId)
				.append(SPACE)
				.append(OUTPUT_FILE_OPTION)
				.append(SPACE)
				.append(targetDirectory)
				.append(targetFile);
		return new ConfigDownloadCommand(sb.toString());
	}

	private static String appendSlashToSourceUrlIfRequired(String sourceUrl) {
		if (!sourceUrl.endsWith(SLASH))
			sourceUrl = sourceUrl + SLASH;
		return sourceUrl;
	}

	private static String appendSlashToTargetDirectoryIfRequired(String targetDirectory) {
		if (!targetDirectory.endsWith(SLASH))
			targetDirectory = targetDirectory + SLASH;
		return targetDirectory;
	}

	private String command;

	private ConfigDownloadCommand(String command) {
		this.command = command;
	}

	@Override
	public String asString() {
		return command;
	}

	@Override
	public Predicate<String> isOutputCorrect() {
		return o -> o.contains("connected") && o.contains("... 200");
	}
}
