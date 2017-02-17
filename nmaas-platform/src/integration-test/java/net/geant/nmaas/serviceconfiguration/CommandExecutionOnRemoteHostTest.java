package net.geant.nmaas.serviceconfiguration;

import net.geant.nmaas.nmservice.configuration.ssh.BasicCredentials;
import net.geant.nmaas.nmservice.configuration.ssh.NmServiceConfigDownloadCommand;
import net.geant.nmaas.nmservice.configuration.ssh.SshConnection;
import net.geant.nmaas.nmservice.configuration.ssh.SingleCommandSshConnection;
import org.junit.Test;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class CommandExecutionOnRemoteHostTest {

    @Test
    public void shouldExecuteSimpleCommandOnRemoteHost() throws SshConnection.ConnectionException {
        String hostname = "10.134.250.1";
        int port = 22;
        BasicCredentials credentials = new BasicCredentials("nmaasconfig", "----");
        SingleCommandSshConnection service = new SingleCommandSshConnection(hostname, port, credentials);
        service.connect();
        service.executeSingleCommandAndDisconnect(NmServiceConfigDownloadCommand.command(
                "bm1hYXNUZXN0OnNmRiQjNGZ3YkVl",
                "http://10.135.0.103:9000/api/configs",
                "remote1234",
                "/home/mgmt/nmaasplatform/volumes/testDir",
                "remoteconfig1234.txt"
        ));
    }

}
