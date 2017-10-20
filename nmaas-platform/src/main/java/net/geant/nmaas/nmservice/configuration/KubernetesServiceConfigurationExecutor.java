package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Profile("kubernetes")
public class KubernetesServiceConfigurationExecutor implements NmServiceConfigurationProvider {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void configureNmService(Identifier deploymentId, Identifier applicationId, AppConfiguration configuration) throws NmServiceConfigurationFailedException {

        notifyStateChangeListeners(deploymentId, NmServiceDeploymentState.CONFIGURED);
    }

    private void notifyStateChangeListeners(Identifier deploymentId, NmServiceDeploymentState state) {
        applicationEventPublisher.publishEvent(new NmServiceDeploymentStateChangeEvent(this, deploymentId, state));
    }

}
