package net.geant.nmaas.nmservice.configuration.ssh;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class NmServiceConfigurationMiscTest {

    @Autowired
    private Environment env;

    @Autowired
    private SshCommandExecutor sshCommandExecutor;

    @Test
    public void shouldReturnCorrectUsernamePasswordHash() {
        assertThat(sshCommandExecutor.generateHash("user", "pass"), equalTo("dXNlcjpwYXNz"));
    }

    private static final String CORRECT_CONFIG_DOWNLOAD_COMMAND =
            "mkdir -p /dir1/dir2/ && wget --connect-timeout=3 --tries=2 --header=\"Authorization: Basic dXNlcjpwYXNz\" http://1.1.1.1:9999/api/configs/3827934 -O /dir1/dir2/configfile1";
    @Test
    public void shouldPrepareConfigDownloadCommandString() {
        String sourceUrl = "http://1.1.1.1:9999/api/configs";
        String configId = "3827934";
        String targetDirectory = "/dir1/dir2";
        String targetFile = "configfile1";
        NmServiceConfigDownloadCommand command = NmServiceConfigDownloadCommand.command(
                sshCommandExecutor.generateHash("user", "pass"),
                sourceUrl,
                configId,
                targetDirectory,
                targetFile);
        assertThat(command.getCommand(), equalTo(CORRECT_CONFIG_DOWNLOAD_COMMAND));
    }

    @Test
    public void shouldProperlyInterpretSshConnectionOutput() {
        String output = "aslksjakld connected. klsjdlkasn ... 200";
        SshConnection sshConnectionUnderTest = new SshConnection();
        assertThat(sshConnectionUnderTest.outputIndicatesThatSomethingWentWrong(output), is(false));
        output = "aslksjakld connected. klsjdlkasn ... 401";
        assertThat(sshConnectionUnderTest.outputIndicatesThatSomethingWentWrong(output), is(true));
        output = "aslksjakld failure";
        assertThat(sshConnectionUnderTest.outputIndicatesThatSomethingWentWrong(output), is(true));
    }

    @Test
    public void shouldProperlyInterpretSshConnectionExitStatus() {
        SshConnection sshConnectionUnderTest = new SshConnection();
        assertThat(sshConnectionUnderTest.exitStatusIndicatesThatSomethingWentWrong(1), is(true));
        assertThat(sshConnectionUnderTest.exitStatusIndicatesThatSomethingWentWrong(13), is(true));
        assertThat(sshConnectionUnderTest.exitStatusIndicatesThatSomethingWentWrong(0), is(false));
    }

    @Test
    public void shouldReadProperUrlFromProperties() {
        final String sourceUrl = env.getProperty("app.config.download.url");
        final Integer serverPort = env.getProperty("server.port", Integer.class);
        assertThat(sourceUrl, containsString(serverPort.toString()));
    }

}
