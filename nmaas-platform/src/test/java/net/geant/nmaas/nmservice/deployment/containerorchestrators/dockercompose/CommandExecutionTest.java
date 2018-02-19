package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFile;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test-compose.properties")
public class CommandExecutionTest {

    private static final String CORRECT_COMPOSE_FILE_DOWNLOAD_COMMAND =
                    "mkdir -p /home/user/dir/deploymentId/ " +
                    "&& " +
                    "wget --connect-timeout=3 --tries=2 --header=\"Authorization: Basic dXNlcjpwYXNz\" http://1.1.1.1:9999/platform/api/dockercompose/files/deploymentId -O /home/user/dir/deploymentId/docker-compose.yml";

    private static final String CORRECT_DOCKER_COMPOSE_UP_COMMAND =
                    "cd /home/user/dir/deploymentId/ " +
                    "&& " +
                    "docker-compose up -d";

    private static final String CORRECT_DOCKER_COMPOSE_PULL_COMMAND =
            "cd /home/user/dir/deploymentId/ " +
                    "&& " +
                    "docker-compose pull";

    private static final String CORRECT_DOCKER_COMPOSE_STOP_COMMAND =
            "cd /home/user/dir/deploymentId/ " +
                    "&& " +
                    "docker-compose stop";

    private static final String CORRECT_DOCKER_COMPOSE_REMOVE_COMMAND =
            "cd /home/user/dir/deploymentId/ " +
                    "&& " +
                    "docker-compose rm -f";

    private static final String CORRECT_DOCKER_COMPOSE_EXEC_COMMAND =
            "cd /home/user/dir/deploymentId/ " +
                    "&& " +
                    "docker-compose exec service ip route add ...";

    @Autowired
    private DockerComposeCommandExecutor dockerComposeCommandExecutor;

    @Test
    public void shouldPrepareComposeFileDownloadCommandString() {
        String sourceUrl = "http://1.1.1.1:9999/platform/api/dockercompose/files/";
        Identifier deploymentId = Identifier.newInstance("deploymentId");
        String targetDirectory = "/home/user/dir/deploymentId/";
        String targetFile = DockerComposeFile.DEFAULT_DOCKER_COMPOSE_FILE_NAME;
        DockerComposeFileDownloadCommand command = DockerComposeFileDownloadCommand.command(
                dockerComposeCommandExecutor.generateHash("user", "pass"),
                sourceUrl,
                deploymentId.value(),
                targetDirectory,
                targetFile);
        assertThat(command.asString(), equalTo(CORRECT_COMPOSE_FILE_DOWNLOAD_COMMAND));
    }

    @Test
    public void shouldReturnCorrectUsernamePasswordHash() {
        assertThat(dockerComposeCommandExecutor.generateHash("user", "pass"), equalTo("dXNlcjpwYXNz"));
    }

    @Test
    public void shouldPrepareDockerComposeUpCommandString() {
        String targetDirectory = "/home/user/dir/deploymentId/";
        DockerComposeCommand command = DockerComposeCommand.command(DockerComposeCommand.CommandType.UP, targetDirectory);
        assertThat(command.asString(), equalTo(CORRECT_DOCKER_COMPOSE_UP_COMMAND));
    }

    @Test
    public void shouldPrepareDockerComposePullCommandString() {
        String targetDirectory = "/home/user/dir/deploymentId/";
        DockerComposeCommand command = DockerComposeCommand.command(DockerComposeCommand.CommandType.PULL, targetDirectory);
        assertThat(command.asString(), equalTo(CORRECT_DOCKER_COMPOSE_PULL_COMMAND));
    }

    @Test
    public void shouldPrepareDockerComposeStopCommandString() {
        String targetDirectory = "/home/user/dir/deploymentId/";
        DockerComposeCommand command = DockerComposeCommand.command(DockerComposeCommand.CommandType.STOP, targetDirectory);
        assertThat(command.asString(), equalTo(CORRECT_DOCKER_COMPOSE_STOP_COMMAND));
    }

    @Test
    public void shouldPrepareDockerComposeRemoveCommandString() {
        String targetDirectory = "/home/user/dir/deploymentId/";
        DockerComposeCommand command = DockerComposeCommand.command(DockerComposeCommand.CommandType.REMOVE, targetDirectory);
        assertThat(command.asString(), equalTo(CORRECT_DOCKER_COMPOSE_REMOVE_COMMAND));
    }

    @Test
    public void shouldPrepareDockerComposeExecCommandString() {
        String targetDirectory = "/home/user/dir/deploymentId/";
        String commandBody = "service ip route add ...";
        DockerComposeCommand command = DockerComposeCommand.command(DockerComposeCommand.CommandType.EXEC, commandBody, targetDirectory);
        assertThat(command.asString(), equalTo(CORRECT_DOCKER_COMPOSE_EXEC_COMMAND));
    }

}
