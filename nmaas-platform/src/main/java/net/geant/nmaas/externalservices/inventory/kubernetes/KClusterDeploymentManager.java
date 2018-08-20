package net.geant.nmaas.externalservices.inventory.kubernetes;

import java.util.Optional;

public interface KClusterDeploymentManager {

    Optional<String> getStorageClass(String domain);

    Boolean getUseInClusterGitLabInstance();

}
