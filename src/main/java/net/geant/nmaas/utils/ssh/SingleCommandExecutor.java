package net.geant.nmaas.utils.ssh;

import lombok.extern.log4j.Log4j2;

import java.util.function.Predicate;

@Log4j2
public class SingleCommandExecutor {

	private String hostname;

	private int port;

	private BasicCredentials credentials;

	private SshConnector connector = null;

	private static SingleCommandExecutor defaultExecutor;

    public static SingleCommandExecutor getExecutor(String hostname, String username) {
        if (defaultExecutor != null) {
            return defaultExecutor;
        }
        return new SingleCommandExecutor(hostname, 22, new BasicCredentials(username, null));
    }

	public static SingleCommandExecutor getExecutor(String hostname, BasicCredentials credentials) {
        if (defaultExecutor != null) {
            return defaultExecutor;
        }
		return new SingleCommandExecutor(hostname, 22, credentials);
	}

    public static SingleCommandExecutor getExecutor(String hostname, int port, BasicCredentials credentials) {
        if (defaultExecutor != null) {
            return defaultExecutor;
        }
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

	public void executeSingleCommand(Command command) {
        executeCommand(command);
	}

    public String executeSingleCommandAndReturnOutput(Command command) {
        return executeCommand(command);
    }

    private String executeCommand(Command command) {
        connect();
        String output = execute(command);
        validateOutput(output, command.isOutputCorrect());
        disconnect();
        return output;
    }

    private void connect() {
        log.debug("Connecting to " + hostname);
        connector = new SshConnector(hostname, port, credentials);
	}

    private String execute(Command command) {
        log.debug("Executing command: " + command.asString());
        return connector.executeSingleCommand(command.asString());
    }

    void validateOutput(String output, Predicate<String> isOutputCorrect) {
        if (isOutputCorrect.negate().test(output)) {
            throw new CommandExecutionException("Identified problem with command execution based on output -> details: " + output + ")");
        }
    }

	private void disconnect() {
		if (connector != null) {
			connector.close();
			connector = null;
		}
	}

}
