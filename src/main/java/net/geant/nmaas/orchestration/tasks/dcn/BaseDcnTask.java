package net.geant.nmaas.orchestration.tasks.dcn;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.dcn.deployment.DcnDeploymentProvidersManager;

@RequiredArgsConstructor
abstract class BaseDcnTask {

    protected final DcnDeploymentProvidersManager providersManager;

}
