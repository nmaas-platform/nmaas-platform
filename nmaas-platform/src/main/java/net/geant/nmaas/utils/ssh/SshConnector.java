package net.geant.nmaas.utils.ssh;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class SshConnector {

	private SSHClient ssh;

	SshConnector(String hostname, int port, BasicCredentials credentials) throws SshConnectionException {
		connect(hostname, port);
		if(isConnected())
			authenticate(credentials);
	}

	public SshConnector() {}
	
	private void connect(String hostname, int port) throws SshConnectionException {
		try {
			ssh = new SSHClient();
			ssh.addHostKeyVerifier((arg0, arg1, arg2) -> true);
			ssh.connect(hostname, port);
		} catch (IOException ex) {
			ssh = null;
			throw new SshConnectionException("Unable to connect -> " + ex.getMessage());
		}
	}
	
	private void authenticate(BasicCredentials credentials) throws SshConnectionException {
		if (ssh == null || !isConnected())
			throw new SshConnectionException("Not connected.");
		try {
			ssh.authPublickey(credentials.getUsername());
		} catch(IOException ex) {
			throw new SshConnectionException("Unable to authenticate due to some errors -> " + ex.getMessage());
		}
	}
	
	String executeSingleCommand(String command) throws SshConnectionException, CommandExecutionException {
		if(!isAuthenticated())
			throw new SshConnectionException("Not authenticated connection to " + ssh.getRemoteAddress());
		try (Session session = ssh.startSession()){
			final Session.Command c = session.exec(command);
			String output = IOUtils.readFully(c.getErrorStream()).toString();
			c.join(5, TimeUnit.SECONDS);
			if (exitStatusIndicatesThatSomethingWentWrong(c.getExitStatus()))
				throw new CommandExecutionException("Command execution failed (exit status: " + c.getExitStatus() + "; details: " + output + ")");
			return output;
		} catch (IOException ex) {
			throw new SshConnectionException("Unable to read command execution error message -> " + ex.getMessage());
		}
	}

	void close() {
		if (ssh != null) {
			try {
				ssh.disconnect();
			} catch (IOException e) { }
			ssh = null;
		}
	}

    private boolean exitStatusIndicatesThatSomethingWentWrong(int exitStatus) {
        return exitStatus != 0;
    }

	private boolean isConnected() {
		return ssh.isConnected();
	}

	private boolean isAuthenticated() {
		return (isConnected() && ssh.isAuthenticated());
	}

}
