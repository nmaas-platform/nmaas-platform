package net.geant.nmaas.utils.ssh;

import lombok.extern.log4j.Log4j2;

import java.util.function.Predicate;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Log4j2
public class SingleCommandExecutor {

	private String hostname;

	private int port;

	private BasicCredentials credentials;

	private SshConnector connector = null;

	private static SingleCommandExecutor defaultExecutor;

    public static SingleCommandExecutor getExecutor(String hostname, String username) {
        if (defaultExecutor != null)
            return defaultExecutor;
        return new SingleCommandExecutor(hostname, 22, new BasicCredentials(username, null));
    }

	public static SingleCommandExecutor getExecutor(String hostname, BasicCredentials credentials) {
        if (defaultExecutor != null)
            return defaultExecutor;
		return new SingleCommandExecutor(hostname, 22, credentials);
	}

    public static SingleCommandExecutor getExecutor(String hostname, int port, BasicCredentials credentials) {
        if (defaultExecutor != null)
            return defaultExecutor;
        return new SingleCommandExecutor(hostname, port, credentials);
    }

    public static void setDefaultExecutor(SingleCommandExecutor executor) {
        defaultExecutor = executor;
    }

	private SingleCommandExecutor(String hostname, int port, BasicCredentials credentials) {
		this.hostname = hostname;
		this.port = port;
		this.credentials = credentials;
	}

	public void executeSingleCommand(Command command) throws SshConnectionException, CommandExecutionException {
        executeCommand(command);
	}

    public String executeSingleCommandAndReturnOutput(Command command) throws SshConnectionException, CommandExecutionException {
        return executeCommand(command);
    }

    private String executeCommand(Command command) throws SshConnectionException, CommandExecutionException {
        connect();
        String output = execute(command);
        validateOutput(output, command.isOutputCorrect());
        disconnect();
        return output;
    }

    private void connect() throws SshConnectionException {
        log.info("Connecting to " + hostname);
        connector = new SshConnector(hostname, port, credentials);
	}

    private String execute(Command command) throws SshConnectionException, CommandExecutionException {
        log.info("Executing command: " + command.asString());
        return connector.executeSingleCommand(command.asString());
    }

    void validateOutput(String output, Predicate<String> isOutputCorrect) throws CommandExecutionException {
        if (isOutputCorrect.negate().test(output))
            throw new CommandExecutionException("Identified problem with command execution based on output -> details: " + output + ")");
    }

	private void disconnect() {
		if(connector != null) {
			connector.close();
			connector = null;
		}
	}

}