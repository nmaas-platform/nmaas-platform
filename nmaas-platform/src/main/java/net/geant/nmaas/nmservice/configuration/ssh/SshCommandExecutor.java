package net.geant.nmaas.nmservice.configuration.ssh;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.nmservice.configuration.NmServiceConfiguration;
import net.geant.nmaas.nmservice.configuration.NmServiceConfigurationRepository;
import net.geant.nmaas.orchestration.Identifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class SshCommandExecutor {

    private static final String LINUX_FILE_SEPARATOR = "/";

    @Autowired
    private NmServiceConfigurationRepository configurations;

    public void executeConfigDownloadCommand(Identifier deploymentId, String configId, DockerHost host)  {
        try {
            final NmServiceConfiguration configuration = configurations.loadConfig(configId);
            final String authorizationHash = "";
            final String targetDirectory = host.getVolumesPath() + "/" + deploymentId.toString();
            NmServiceConfigDownloadCommand.command(null, null, configId, targetDirectory, configuration.getConfigFileName());
        } catch (NmServiceConfigurationRepository.ConfigurationNotFoundException e) {
            e.printStackTrace();
        }
    }

}
