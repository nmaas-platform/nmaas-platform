package net.geant.nmaas.nmservice.configuration.ssh;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.nmservice.configuration.exceptions.CommandExecutionException;
import net.geant.nmaas.nmservice.configuration.exceptions.SshConnectionException;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfigurationRepository;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component("sshCommandExecutor")
public class SshCommandExecutor {

    private final static Logger log = LogManager.getLogger(SshCommandExecutor.class);

    @Autowired
    private Environment env;

    @Autowired
    private NmServiceConfigurationRepository configurations;

    public void executeConfigDownloadCommand(String configId, DockerHost host, String targetDirectoryName) throws CommandExecutionException {
        try {
            final String authorizationHash = generateHash(env.getProperty("api.client.config.download.username"), env.getProperty("api.client.config.download.password"));
            final String sourceUrl = env.getProperty("app.config.download.url");
            final String targetDirectoryFullPath = host.getVolumesPath() + "/" + targetDirectoryName;
            final String configurationFileName = configurations.loadConfig(configId).getConfigFileName();
            NmServiceConfigDownloadCommand command =  NmServiceConfigDownloadCommand.command(
                    authorizationHash,
                    sourceUrl,
                    configId,
                    targetDirectoryFullPath,
                    configurationFileName);
            BasicCredentials credentials = new BasicCredentials(env.getProperty("app.config.ssh.username"));
            log.debug("Connecting to " + host.getPublicIpAddress().getHostAddress());
            log.debug("Command: " + command.getCommand());
            SingleCommandSshConnection.getConnection(host.getPublicIpAddress().getHostAddress(), credentials).executeSingleCommand(command);
        } catch (NmServiceConfigurationRepository.ConfigurationNotFoundException
                | SshConnectionException
                | CommandExecutionException e) {
            throw new CommandExecutionException("Failed to execute configuration download command -> " + e.getMessage());
        }
    }

    String generateHash(String username, String password) {
        return DatatypeConverter.printBase64Binary((username + ":" + password).getBytes(Charset.forName("UTF-8")));
    }

}
