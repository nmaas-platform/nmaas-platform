package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.exceptions.ConfigurationNotFoundException;
import net.geant.nmaas.nmservice.configuration.repositories.NmServiceConfigurationRepository;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import net.geant.nmaas.utils.ssh.CommandExecutionException;
import net.geant.nmaas.utils.ssh.SingleCommandExecutor;
import net.geant.nmaas.utils.ssh.SshConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class ConfigDownloadCommandExecutor {

    @Autowired
    private Environment env;

    @Autowired
    private NmServiceConfigurationRepository configurations;

    void executeConfigDownloadCommand(String configId, DockerHost host, String targetDirectoryName) throws CommandExecutionException {
        try {
            final String authorizationHash = generateHash(env.getProperty("api.client.config.download.username"), env.getProperty("api.client.config.download.password"));
            final String sourceUrl = env.getProperty("app.config.download.url");
            final String targetDirectoryFullPath = constructTargetDirectoryFullPath(host, targetDirectoryName);
            final String configurationFileName = configurations.getConfigFileNameByConfigId(configId).orElseThrow(() -> new ConfigurationNotFoundException(configId));
            ConfigDownloadCommand command =  ConfigDownloadCommand.command(
                    authorizationHash,
                    sourceUrl,
                    configId,
                    targetDirectoryFullPath,
                    configurationFileName);
            SingleCommandExecutor.getExecutor(
                    host.getPublicIpAddress().getHostAddress(),
                    env.getProperty("app.config.ssh.username")).executeSingleCommand(command);
        } catch (ConfigurationNotFoundException
                | SshConnectionException
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

    String generateHash(String username, String password) {
        return DatatypeConverter.printBase64Binary((username + ":" + password).getBytes(Charset.forName("UTF-8")));
    }

}
