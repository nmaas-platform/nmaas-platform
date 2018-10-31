package net.geant.nmaas.externalservices.inventory.kubernetes;

import java.util.Optional;

public interface KClusterDeploymentManager {

    Optional<String> getStorageClass(String domain);

    Boolean getUseInClusterGitLabInstance();

    String getSMTPServerHostname();

    Integer getSMTPServerPort();

    Optional<String> getSMTPServerUsername();

    Optional<String> getSMTPServerPassword();

    boolean getForceDedicatedWorkers();
}
