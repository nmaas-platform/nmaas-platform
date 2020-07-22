package net.geant.nmaas.utils.ssh;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.api.shell.connectors.AsyncConnector;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * This is implementation of the SSH connector that allows executing ssh commands in scope of shell session
 * How to use:
 * 0. Prepare key pair
 * 1. Create instance
 * 2. Get Input Stream
 * 3. Execute command in session
 * 4. Read result from input stream
 * 5. finally close connection
 */
@NoArgsConstructor
@Log4j2
public class SshSessionConnector implements AsyncConnector {

	private SSHClient client;
	private Session session;
	private Session.Shell shell;

	/**
	 * Creates SSH connection using public and private keys
	 * Connection->Authentication->Session->Shell
	 * @param hostname target
	 * @param port connection port
	 * @param credentials BasicCredentials (username is enough - this connector won't attempt password authentication)
	 * @param keyProvider private-public key pair
	 */
	public SshSessionConnector(String hostname, int port, BasicCredentials credentials, KeyProvider keyProvider) {
		connect(hostname, port);
		if(isConnected()) {
			authenticate(credentials, keyProvider);
		}
		if(isAuthenticated()) {
			openSession();
		}
	}

	/**
	 * Test purpose constructor
	 * @param client - already connected (mocked) SSHClient instance
	 * @param credentials
	 * @param keyProvider
	 */
	public SshSessionConnector(SSHClient client, BasicCredentials credentials, KeyProvider keyProvider) {
		this.client = client;
		if(isConnected()) {
			authenticate(credentials, keyProvider);
		}
		if(isAuthenticated()) {
			openSession();
		}
	}

	/**
	 * Use this input stream to obtain results from executed commands
	 * Returns entire shell appearance
	 * @return InputStream with shell output
	 */
	public InputStream getInputStream() {
		return this.shell.getInputStream();
	}

	/**
	 * Returns stream of errors
	 * @return InputStream with errors
	 */
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
	 * utilizes shell OutputStream to execute commands during shell session
	 * the outcome of executed command will be available in resulting InputStream
	 * @param command command to be executed
	 */
	public void executeCommand(String command) {
		if(!isSessionOpened()){
			throw new SshConnectionException("Session is not opened");
		}
		try {
			// write command to stream
			// endline is essential for command to be executed
			this.shell.getOutputStream().write((command + "\n").getBytes());
			this.shell.getOutputStream().flush();
			log.debug("Command:\t" + command + "\t written to shell stream");
		} catch (IOException e) {
			throw new SshConnectionException("Unable to stop execute command in session -> " + e.getMessage());
		}
	}


	/**
	 * Executes command SYNCHRONOUSLY in single scope - this should not affect shell session
	 * Result is available immediately as String
	 * @param command command to be executed
	 * @return outcome of executed command
	 */
	public String executeSingleCommand(String command) {
		if(!isAuthenticated())
			throw new SshConnectionException("Not authenticated connection to " + client.getRemoteAddress());
		try (Session session = client.startSession()){
			final Session.Command c = session.exec(command);
			String error = IOUtils.readFully(c.getErrorStream()).toString();
			String output = IOUtils.readFully(c.getInputStream()).toString();
			c.join(5, TimeUnit.SECONDS);
			if (exitStatusIndicatesThatSomethingWentWrong(c.getExitStatus())) {
				return error;
			}
			return output;
		} catch (IOException ex) {
			throw new SshConnectionException("Unable to read command execution error message -> " + ex.getMessage());
		}
	}

	/**
	 * closes session and disconnects client
	 * connection cannot be re-created after using this method using this instance
	 */
	public void close() {
		if (client != null) {
			if(isSessionOpened()) {
				closeSession();
			}
			client = null;
		}
	}

    private boolean exitStatusIndicatesThatSomethingWentWrong(int exitStatus) {
        return exitStatus != 0;
    }

	public boolean isConnected() {
		return client != null && client.isConnected();
	}

	public boolean isAuthenticated() {
		return isConnected() && client.isAuthenticated();
	}

	public boolean isSessionOpened() {
		return isConnected() && isAuthenticated() && this.session.isOpen();
	}

}
