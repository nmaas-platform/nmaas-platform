package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Profile("kubernetes")
public class KubernetesServiceConfigurationExecutor implements NmServiceConfigurationProvider {

    @Override
    public void configureNmService(Identifier deploymentId, Identifier applicationId, AppConfiguration configuration) throws NmServiceConfigurationFailedException {

    }

}
