package net.geant.nmaas.externalservices.inventory.kubernetes;

public interface KClusterDeploymentManager {

    String getDefaultPersistenceClass();

    Boolean getUseInClusterGitLabInstance();

}
