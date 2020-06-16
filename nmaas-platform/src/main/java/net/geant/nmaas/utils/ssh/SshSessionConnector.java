package net.geant.nmaas.utils.ssh;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor
@Log4j2
public class SshSessionConnector {

	private SSHClient client;
	private Session session;
	private Session.Shell shell;

	public SshSessionConnector(String hostname, int port, BasicCredentials credentials, KeyProvider keyProvider) {
		connect(hostname, port);
		if(isConnected()) {
			authenticate(credentials, keyProvider);
		}
		if(isAuthenticated()) {
			openSession();
		}
	}

	public InputStream getInputStream() {
		return this.shell.getInputStream();
	}

	public InputStream getErrorStream() {
		return this.shell.getErrorStream();
	}

	private void connect(String hostname, int port) {
		try {
			client = new SSHClient();
			client.addHostKeyVerifier((arg0, arg1, arg2) -> true);
			client.connect(hostname, port);
		} catch (IOException ex) {
			client = null;
			throw new SshConnectionException("Unable to connect -> " + ex.getMessage());
		}
	}
	
	private void authenticate(BasicCredentials credentials, KeyProvider keyProvider) {
		if (client == null || !isConnected())
			throw new SshConnectionException("Not connected.");
		try {
			client.authPublickey(credentials.getUsername(), keyProvider);
		} catch(IOException ex) {
			throw new SshConnectionException("Unable to authenticate due to some errors -> " + ex.getMessage());
		}
	}

	private void openSession() {
		try {
			this.session = client.startSession();
			this.session.allocateDefaultPTY();
			this.shell = this.session.startShell();
		} catch (ConnectionException | TransportException e) {
			throw new SshConnectionException("Unable to start ssh session -> " + e.getMessage());
		}
	}


	private void closeSession() {
		try {
			this.session.close();
			this.client.disconnect();
		} catch (IOException e) {
			throw new SshConnectionException("Unable to stop ssh session -> " + e.getMessage());
		}
	}

	/**
	 * executes single command in session
	 * @param command
	 */
	public void executeCommandInSession(String command) {
		if(!isSessionOpened()){
			throw new SshConnectionException("Session is not opened");
		}
		try {
			this.shell.getOutputStream().write(command.getBytes());
		} catch (IOException e) {
			throw new SshConnectionException("Unable to stop execute command in session -> " + e.getMessage());
		}
	}


	public String executeSingleCommand(String command) {
		if(!isAuthenticated())
			throw new SshConnectionException("Not authenticated connection to " + client.getRemoteAddress());
		try (Session session = client.startSession()){
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

	public void close() {
		if (client != null) {
			if(isSessionOpened()) {
				closeSession();
			}
			try {
				client.disconnect();
			} catch (IOException e) {
				log.warn(e.getMessage());
			}
			client = null;
		}
	}

    private boolean exitStatusIndicatesThatSomethingWentWrong(int exitStatus) {
        return exitStatus != 0;
    }

	private boolean isConnected() {
		return client.isConnected();
	}

	private boolean isAuthenticated() {
		return (isConnected() && client.isAuthenticated());
	}

	private boolean isSessionOpened() {
		return isConnected() && isAuthenticated() && this.session.isOpen();
	}

}
