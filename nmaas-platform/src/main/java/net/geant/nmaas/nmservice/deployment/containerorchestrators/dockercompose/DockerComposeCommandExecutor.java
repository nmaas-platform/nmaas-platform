package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFile;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import net.geant.nmaas.utils.ssh.SingleCommandExecutor;
import net.geant.nmaas.utils.ssh.SshConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;

@Component
public class DockerComposeCommandExecutor {

    private Environment env;

    @Autowired
    public DockerComposeCommandExecutor(Environment env){
        this.env = env;
    }

    void executeComposeFileDownloadCommand(Identifier deploymentId, DockerHost host) {
        try {
            final String authorizationHash = generateHash(env.getProperty("app.compose.download.client.username"), env.getProperty("app.compose.download.client.password"));
            final String sourceUrl = env.getProperty("app.compose.download.url");
            final String targetDirectoryFullPath = host.getWorkingPath() + "/" + deploymentId.value();
            final String composeFileName = DockerComposeFile.DEFAULT_DOCKER_COMPOSE_FILE_NAME;
            DockerComposeFileDownloadCommand command = DockerComposeFileDownloadCommand.command(
                    authorizationHash,
                    sourceUrl,
                    deploymentId.value(),
                    targetDirectoryFullPath,
                    composeFileName);
            SingleCommandExecutor.getExecutor(
                    host.getPublicIpAddress().getHostAddress(),
                    env.getProperty("app.compose.ssh.username")).executeSingleCommand(command);
        } catch (SshConnectionException
                | CommandExecutionException e) {
            throw new CommandExecutionException("Failed to execute compose file download command -> " + e.getMessage());
        }
    }

    String generateHash(String username, String password) {
        return DatatypeConverter.printBase64Binary((username + ":" + password).getBytes(Charset.forName("UTF-8")));
    }

    void executeComposeUpCommand(Identifier deploymentId, DockerHost host) {
        executeComposeCommand(deploymentId, DockerComposeCommand.CommandType.UP, host);
    }

    void executeComposeDownCommand(Identifier deploymentId, DockerHost host) {
        executeComposeCommand(deploymentId, DockerComposeCommand.CommandType.DOWN, host);
    }

    void executeComposePullCommand(Identifier deploymentId, DockerHost host) {
        executeComposeCommand(deploymentId, DockerComposeCommand.CommandType.PULL, host);
    }

    void executeComposeStopCommand(Identifier deploymentId, DockerHost host) {
        executeComposeCommand(deploymentId, DockerComposeCommand.CommandType.STOP, host);
    }

    void executeComposeRemoveCommand(Identifier deploymentId, DockerHost host) {
        executeComposeCommand(deploymentId, DockerComposeCommand.CommandType.REMOVE, host);
    }

    void executeComposeExecCommand(Identifier deploymentId, DockerHost host, String commandBody) {
        executeComposeCommand(deploymentId, DockerComposeCommand.CommandType.EXEC, commandBody, host);
    }

    private void executeComposeCommand(Identifier deploymentId, DockerComposeCommand.CommandType commandType, DockerHost host) {
        executeComposeCommand(deploymentId, commandType, null, host);
    }

    private void executeComposeCommand(Identifier deploymentId, DockerComposeCommand.CommandType commandType, String commandBody, DockerHost host) {
        try {
            final String targetDirectoryFullPath = host.getWorkingPath() + "/" + deploymentId.value();
            final DockerComposeCommand command = DockerComposeCommand.command(commandType, commandBody, targetDirectoryFullPath);
            SingleCommandExecutor.getExecutor(
                    host.getPublicIpAddress().getHostAddress(),
                    env.getProperty("app.compose.ssh.username")).executeSingleCommand(command);
        } catch (SshConnectionException
                | CommandExecutionException e) {
            throw new CommandExecutionException("Failed to execute compose " + commandType.name() + " command -> " + e.getMessage());
        }
    }
}
