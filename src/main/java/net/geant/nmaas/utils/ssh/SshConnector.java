package net.geant.nmaas.utils.ssh;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor
@Log4j2
public class SshConnector {

	private SSHClient ssh;

	SshConnector(String hostname, int port, BasicCredentials credentials) {
		connect(hostname, port);
		if (isConnected()) {
			authenticate(credentials);
		}
	}

	private void connect(String hostname, int port) {
		try {
			ssh = new SSHClient();
			ssh.addHostKeyVerifier(new DummyHostKeyVerifier());
			ssh.connect(hostname, port);
		} catch (IOException ex) {
			ssh = null;
			throw new SshConnectionException("Unable to connect -> " + ex.getMessage());
		}
	}
	
	private void authenticate(BasicCredentials credentials) {
		if (ssh == null || !isConnected())
			throw new SshConnectionException("Not connected.");
		try {
			ssh.authPublickey(credentials.getUsername());
		} catch(IOException ex) {
			throw new SshConnectionException("Unable to authenticate due to some errors -> " + ex.getMessage());
		}
	}
	
	String executeSingleCommand(String command) {
		if(!isAuthenticated())
			throw new SshConnectionException("Not authenticated connection to " + ssh.getRemoteAddress());
		try (Session session = ssh.startSession()){
			final Session.Command c = session.exec(command);
			String error = IOUtils.readFully(c.getErrorStream()).toString();
			String output = IOUtils.readFully(c.getInputStream()).toString();
			c.join(5, TimeUnit.SECONDS);
			if (exitStatusIndicatesThatSomethingWentWrong(c.getExitStatus()))
				throw new CommandExecutionException("Command execution failed (exit status: " + c.getExitStatus() + "; details: " + error + ")");
			return output;
		} catch (IOException ex) {
			throw new SshConnectionException("Unable to read command execution error message -> " + ex.getMessage());
		}
	}

	void close() {
		if (ssh != null) {
			try {
				ssh.disconnect();
			} catch (IOException e) {
				log.warn(e.getMessage());
			}
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
