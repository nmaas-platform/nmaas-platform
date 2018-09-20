package net.geant.nmaas.externalservices.inventory.kubernetes;

import java.util.Optional;
import net.geant.nmaas.orchestration.entities.Identifier;

public interface KClusterDeploymentManager {

    Optional<String> getStorageClass(String domain);

    Boolean getUseInClusterGitLabInstance();

    String getStorageSpace(Identifier deploymentId);

}
