package net.geant.nmaas.orchestration.tasks.dcn;

import net.geant.nmaas.dcn.deployment.DcnDeploymentProvidersManager;
import org.springframework.beans.factory.annotation.Autowired;

abstract class BaseDcnTask {

    @Autowired
    DcnDeploymentProvidersManager providersManager;

}
