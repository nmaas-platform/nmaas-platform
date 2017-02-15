package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.AppConfiguration;
import net.geant.nmaas.orchestration.AppDeploymentStateChanger;
import net.geant.nmaas.orchestration.Identifier;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface NmServiceConfigurationProvider extends AppDeploymentStateChanger {

    void configureNmService(Identifier deploymentId, AppConfiguration configuration) throws InvalidDeploymentIdException;

}
