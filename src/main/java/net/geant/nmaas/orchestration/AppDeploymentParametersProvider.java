package net.geant.nmaas.orchestration;

import java.util.Map;

public interface AppDeploymentParametersProvider {

    Map<String, String> deploymentParameters(Identifier deploymentId);

}
