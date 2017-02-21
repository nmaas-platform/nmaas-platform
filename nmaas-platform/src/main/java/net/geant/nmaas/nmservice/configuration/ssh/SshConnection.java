package net.geant.nmaas.nmservice.configuration.ssh;

import net.geant.nmaas.nmservice.configuration.exceptions.CommandExecutionException;
import net.geant.nmaas.nmservice.configuration.exceptions.SshConnectionException;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SshConnection {

	private SSHClient ssh;

	public SshConnection(String hostname, int port, BasicCredentials credentials) throws SshConnectionException {
		connect(hostname, port);
		if(isConnected())
			authenticate(credentials);
	}
	
	private void connect(String hostname, int port) throws SshConnectionException {
		try {
			ssh = new SSHClient();
			ssh.addHostKeyVerifier((arg0, arg1, arg2) -> true);
			ssh.connect(hostname, port);
		} catch (IOException ex) {
			ssh = null;
			throw new SshConnectionException("Unable to connect.", ex);
		}
	}
	
	private void authenticate(BasicCredentials credentials) throws SshConnectionException {
		if (ssh == null || !isConnected())
			throw new SshConnectionException("Not connected.");
		try {
			ssh.authPassword(credentials.getUsername(), credentials.getPassword());
		} catch(IOException ex) {
			throw new SshConnectionException("Unable to authenticate due to some errors.", ex);
		}
	}
	
	public void executeSingleCommand(String command) throws SshConnectionException, CommandExecutionException {
		Session session = null;
		if(!isAuthenticated())
			throw new SshConnectionException("Not authenticated connection to " + ssh.getRemoteAddress());
		try {
			session = ssh.startSession();
			final Session.Command c = session.exec(command);
			String output = IOUtils.readFully(c.getErrorStream()).toString();
			if (output.contains(HttpStatus.UNAUTHORIZED.toString())
					|| output.contains(HttpStatus.FORBIDDEN.toString())
					|| output.contains(HttpStatus.NOT_FOUND.toString()))
				throw new CommandExecutionException("Problem with downloading the configuration file -> details: " + output + ")");
			c.join(5, TimeUnit.SECONDS);
			if (c.getExitStatus() == 0)
				throw new CommandExecutionException("Command execution failed (exit status: " + c.getExitStatus() + "; details: " + output + ")");
		} catch (IOException ex) {
			throw new SshConnectionException("Unable to read command execution error message -> ", ex);
		} finally {
			if (session != null) {
				try {
					session.close();
				} catch (Exception e) {
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
	
}
