package net.geant.nmaas.nmservicedeployment;

import net.geant.nmaas.deploymentorchestration.AppDeploymentStateChanger;
import net.geant.nmaas.nmservicedeployment.nmservice.NmServiceDeploymentState;
import net.geant.nmaas.nmservicedeployment.nmservice.NmServiceSpec;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface ServiceDeploymentProvider extends AppDeploymentStateChanger {

    void verifyRequest(NmServiceSpec serviceSpec);

    void prepareDeploymentEnvironment(String serviceName);

    void deployNmService(String serviceName);

    NmServiceDeploymentState checkService(String serviceName);

    void removeNmService(String serviceName);

}
