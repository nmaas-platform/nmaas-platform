package net.geant.nmaas.externalservices.inventory.kubernetes;

public interface KClusterDeploymentManager {

    String getPersistenceClass(String domain);

    Boolean getUseInClusterGitLabInstance();

}
