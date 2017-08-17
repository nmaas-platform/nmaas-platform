package net.geant.nmaas.utils.ssh;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.function.Predicate;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class SingleCommandExecutor {

    private final static Logger log = LogManager.getLogger(SingleCommandExecutor.class);

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
		connect();
        String output = execute(command);
        validateOutput(output, command.isOutputCorrect());
        disconnect();
	}

    private void connect() throws SshConnectionException {
        log.debug("Connecting to " + hostname);
        connector = new SshConnector(hostname, port, credentials);
	}

    private String execute(Command command) throws SshConnectionException, CommandExecutionException {
        log.debug("Executing command: " + command.asString());
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