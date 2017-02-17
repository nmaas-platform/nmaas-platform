package net.geant.nmaas.nmservice.configuration.ssh;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SshConnection {

	private SSHClient ssh;

	public SshConnection(String hostname, int port, BasicCredentials credentials) throws ConnectionException {
		connect(hostname, port);
		if(isConnected())
			authenticate(credentials);
	}
	
	private void connect(String hostname, int port) throws ConnectionException {
		try {
			ssh = new SSHClient();
			ssh.addHostKeyVerifier((arg0, arg1, arg2) -> true);
			ssh.connect(hostname, port);
		} catch (IOException ex) {
			ssh = null;
			throw new ConnectionException("Unable to connect.", ex);
		}
	}
	
	private void authenticate(BasicCredentials credentials) throws ConnectionException {
		if (ssh == null || !isConnected())
			throw new ConnectionException("Not connected.");
		try {
			ssh.authPassword(credentials.getUsername(), credentials.getPassword());
		} catch(IOException ex) {
			throw new ConnectionException("Unable to authenticate due to some errors.", ex);
		}
	}
	
	public void executeSingleCommand(String command) throws ConnectionException {
		Session session = null;
		if(!isAuthenticated())
			throw new ConnectionException("Not authenticated connection to " + ssh.getRemoteAddress());		
		try {
			session = ssh.startSession();
			final Session.Command c = session.exec(command);
			String output = ((IOUtils.readFully(c.getInputStream()).toString()));
			c.join(5, TimeUnit.SECONDS);
			System.out.println(output);
		} catch (IOException ex) {
			throw new ConnectionException("Unable to execute command ", ex);
		} finally {
			if (session != null) {
				try {
					session.close();
				} catch (TransportException | net.schmizz.sshj.connection.ConnectionException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public boolean isConnected() {
		return ssh.isConnected();
	}
	
	public boolean isAuthenticated() {
		return (isConnected() && ssh.isAuthenticated());
	}

	public void close() {
		if (ssh != null) {
			try {
				ssh.disconnect();
			} catch (IOException e) { }
			ssh = null;
		}
	}
	
	public class ConnectionException extends Exception {

		public ConnectionException(String message) {
			super(message);
		}

		public ConnectionException(String message, Throwable cause) {
			super(message, cause);
		}

	}
}
