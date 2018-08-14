package net.geant.nmaas.externalservices.inventory.kubernetes;

public interface KClusterDeploymentManager {

    String getStorageClass(String domain);

    Boolean getUseInClusterGitLabInstance();

}
