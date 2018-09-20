package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.exceptions.ConfigFileNotFoundException;
import net.geant.nmaas.nmservice.configuration.exceptions.FileTransferException;
import net.geant.nmaas.nmservice.configuration.repositories.NmServiceConfigFileRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.DockerNmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import net.geant.nmaas.utils.ssh.SingleCommandExecutor;
import net.geant.nmaas.utils.ssh.SshConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Implementation of the {@link ConfigurationFileTransferProvider} interface tailored for deployments for which
 * configuration files should be put in a shared volume directory on a Docker Host.
 */
@Component
@Profile("env_docker-compose")
public class DockerHostConfigDownloadCommandExecutor implements ConfigurationFileTransferProvider {

    private Environment env;

    private DockerNmServiceRepositoryManager serviceRepositoryManager;

    private NmServiceConfigFileRepository configurations;

    @Autowired
    public DockerHostConfigDownloadCommandExecutor(Environment env, DockerNmServiceRepositoryManager serviceRepositoryManager, NmServiceConfigFileRepository configurations){
        this.env = env;
        this.serviceRepositoryManager = serviceRepositoryManager;
        this.configurations = configurations;
    }

    private String authorizationHash;
    @Value("${app.config.download.url}")
    private String sourceUrl;
    @Value("${app.config.ssh.username}")
    private String sshUsername;

    @Override
    @Loggable(LogLevel.INFO)
    public void transferConfigFiles(Identifier deploymentId, List<String> configIds, boolean configFileRepositoryRequired)
            throws InvalidDeploymentIdException, ConfigFileNotFoundException, FileTransferException {
        DockerHost host = serviceRepositoryManager.loadDockerHost(deploymentId);
        String attachedVolumeName = serviceRepositoryManager.loadAttachedVolumeName(deploymentId);
        final String targetDirectoryFullPath = constructTargetDirectoryFullPath(host, attachedVolumeName);
        for (String configFileId : configIds) {
            String configFileName = configurations.getConfigFileNameByConfigId(configFileId)
                    .orElseThrow(() -> new ConfigFileNotFoundException("Configuration file with id " + configFileId + " not found"));
            ConfigDownloadCommand command = buildCommand(configFileId, configFileName, targetDirectoryFullPath);
            try {
                executeConfigDownloadCommand(command, host);
            } catch (CommandExecutionException e) {
                throw new FileTransferException("Failed to transfer configuration file with id " + configFileId + " -> " + e.getMessage());
            }
        }
    }

    private ConfigDownloadCommand buildCommand(String configFileId, String configFileName, String targetDirectoryFullPath) {
        return ConfigDownloadCommand.command(
                authorizationHash,
                sourceUrl,
                configFileId,
                targetDirectoryFullPath,
                configFileName);
    }

    private void executeConfigDownloadCommand(ConfigDownloadCommand command, DockerHost host) throws CommandExecutionException {
        try {
            SingleCommandExecutor.getExecutor(host.getPublicIpAddress().getHostAddress(), sshUsername)
                    .executeSingleCommand(command);
        } catch ( SshConnectionException
                | CommandExecutionException e) {
            throw new CommandExecutionException("Failed to execute configuration download command -> " + e.getMessage());
        }
    }

    private String constructTargetDirectoryFullPath(DockerHost host, String targetDirectoryName) {
        // if target directory name starts with a slash it is assumed that it is a complete path to volume directory
        // and therefore it is returned without any modifications
        if (targetDirectoryName.startsWith("/"))
            return targetDirectoryName;
        return host.getVolumesPath() + "/" + targetDirectoryName;
    }

    @PostConstruct
    void generateHash() {
        String username = env.getProperty("app.config.download.client.username");
        String password = env.getProperty("app.config.download.client.password");
        authorizationHash = DatatypeConverter.printBase64Binary((username + ":" + password).getBytes(Charset.forName("UTF-8")));
    }

    String getAuthorizationHash() {
        return authorizationHash;
    }
}
