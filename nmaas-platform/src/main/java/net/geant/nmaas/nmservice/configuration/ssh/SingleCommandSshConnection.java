package net.geant.nmaas.nmservice.configuration.ssh;

public class SingleCommandSshConnection {

	String hostname;
	int port;
	BasicCredentials credentials;
	
	SshConnection connection = null;
	
	public SingleCommandSshConnection(String hostname, int port, BasicCredentials credentials) {
		this.hostname = hostname;
		this.port = port;
		this.credentials = credentials;
	}
	
	public void connect() throws SshConnection.ConnectionException {
		if (connection == null)
			connection = new SshConnection(hostname, port, credentials);
	}
	
	public void executeSingleCommandAndDisconnect(NmServiceConfigDownloadCommand nmServiceConfigDownloadCommand)
			throws SshConnection.ConnectionException {
		if(connection != null)
			connection.executeSingleCommand(nmServiceConfigDownloadCommand.getCommand());
		disconnect();
	}

	void disconnect() {
		if(connection != null) {
			connection.close();
			connection = null;
		}
	}

}
