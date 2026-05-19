package net.geant.nmaas.kubernetes;

import net.geant.nmaas.kubernetes.remote.entities.KCluster;

public interface KubernetesClusterNamespaceService {

    String namespace(String domain);

    String namespace(KCluster remoteCluster, String domain);

}
