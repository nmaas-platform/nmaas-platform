package net.geant.nmaas.externalservices.inventory.kubernetes;

import io.fabric8.kubernetes.client.KubernetesClient;

public interface KClusterApiManager {

    KubernetesClient getApiClient();

    boolean getUseClusterApi();

}
