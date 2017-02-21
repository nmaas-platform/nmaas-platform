package net.geant.nmaas.nmservice.configuration.ssh;

import net.geant.nmaas.nmservice.configuration.exceptions.CommandExecutionException;
import net.geant.nmaas.nmservice.configuration.exceptions.SshConnectionException;

public class SingleCommandSshConnection {

	private String hostname;

	private int port;

	private BasicCredentials credentials;

	public static SingleCommandSshConnection getConnection(String hostname, BasicCredentials credentials) {
		return new SingleCommandSshConnection(hostname, 22, credentials);
	}

	private SshConnection connection = null;
	
	public SingleCommandSshConnection(String hostname, int port, BasicCredentials credentials) {
		this.hostname = hostname;
		this.port = port;
		this.credentials = credentials;
	}

	public void executeSingleCommand(NmServiceConfigDownloadCommand nmServiceConfigDownloadCommand)
			throws SshConnectionException, CommandExecutionException {
		connect();
		if(connection != null)
			connection.executeSingleCommand(nmServiceConfigDownloadCommand.getCommand());
		disconnect();
	}

	private void connect() throws SshConnectionException {
		if (connection == null)
			connection = new SshConnection(hostname, port, credentials);
	}

	private void disconnect() {
		if(connection != null) {
			connection.close();
			connection = null;
		}
	}

}
