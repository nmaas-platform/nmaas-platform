package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Implementation of the {@link NmServiceConfigurationProvider} interface tailored for NM service deployments based on
 * Kubernetes and in general using a central git repository to store and exchange configuration files.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Profile("kubernetes")
public class KubernetesServiceConfigurator implements NmServiceConfigurationProvider {

    @Autowired
    private ConfigurationFileTransferProvider fileTransfer;

    @Autowired
    private ServiceConfigurationExecutor configurationExecutor;

    /**
     * Triggers {@link ServiceConfigurationExecutor} to execute configuration process using a custom {@link ConfigurationFileTransferProvider}.
     *
     * @param deploymentId unique identifier of service deployment
     * @param applicationId identifier of the application / service
     * @param appConfiguration application instance configuration data provided by the user
     * @throws NmServiceConfigurationFailedException if any error condition occurs
     */
    @Override
    @Loggable(LogLevel.INFO)
    public void configureNmService(Identifier deploymentId, Identifier applicationId, AppConfiguration appConfiguration)
            throws NmServiceConfigurationFailedException {
        configurationExecutor.configure(deploymentId, applicationId, appConfiguration, fileTransfer);
    }

}
