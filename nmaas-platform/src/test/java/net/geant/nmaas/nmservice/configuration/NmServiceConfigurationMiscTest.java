package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.ssh.BasicCredentials;
import net.geant.nmaas.nmservice.configuration.ssh.NmServiceConfigDownloadCommand;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class NmServiceConfigurationMiscTest {

    @Test
    public void shouldReturnCorrectUsernamePasswordHash() {
        BasicCredentials credentials = new BasicCredentials("user", "pass");
        assertThat(credentials.generateHash(), equalTo("dXNlcjpwYXNz"));
    }

    private static final String CORRECT_CONFIG_DOWNLOAD_COMMAND =
            "wget --header=\"Authorization: Basic dXNlcjpwYXNz\" http://1.1.1.1:9999/api/configs/3827934 -O /dir1/dir2/configfile1";
    @Test
    public void shouldPrepareConfigDownloadCommandString() {
        BasicCredentials credentials = new BasicCredentials("user", "pass");
        String sourceUrl = "http://1.1.1.1:9999/api/configs";
        String configId = "3827934";
        String targetDirectory = "/dir1/dir2";
        String targetFile = "configfile1";
        NmServiceConfigDownloadCommand command = NmServiceConfigDownloadCommand.command(
                credentials.generateHash(),
                sourceUrl,
                configId,
                targetDirectory,
                targetFile);
        assertThat(command.getCommand(), equalTo(CORRECT_CONFIG_DOWNLOAD_COMMAND));
    }

}
